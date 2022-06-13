---
title: TCP-四次挥手/断开连接
toc: true
date: 2021-07-04 16:34:11
tags: TCP
categories: [Computer Network, TCP]
---

# TCP断开连接


## 四次挥手
<img src="https://oscimg.oschina.net/oscnet/up-8d9a6b405e874bc02b91e453bd277c46d46.png" width=450 height=550>

- 开始客户端和服务端都是处理【established】状态
- 客户端发送「FIN」报文之后，进入FIN-WAIT-1状态
- 服务端收到客户端的FIN之后，恢复一个ACK，同时进入CLOSE_WAIT状态
- 客户端接收到ACK之后，进入到FIN-WAIT-2状态
- 服务端接着发送FIN报文，同时进入LAST-ACK状态
- 客户端接收到服务端的FIN报文之后，发送ACK报文，并进入TIME_WAIT状态
- 客户端在经历2个MSL时间之后，进入CLOSE状态
- 服务端接收到客户端的ACK之后，进入CLOSE状态

> 并不是所有的四次挥手都是上述流程，当客户端和服务端同时发送关闭连接的请求如下👇：

<img src="https://oscimg.oschina.net/oscnet/up-d859884b062bda56a946ae9e3c0c148235b.png" width=450 height=550>

可以看到双方都主动发起断开请求所以各自都是主动发起方，状态会从 FIN_WAIT_1 都进入到 CLOSING 这个过度状态然后再到 TIME_WAIT。

> 挥手一定需要四次吗？

假设 client 已经没有数据发送给 server 了，所以它发送 FIN 给 server 表明自己数据发完了，不再发了，如果这时候 server 还是有数据要发送给 client 那么它就是先回复 ack ，然后继续发送数据。
等 server 数据发送完了之后再向 client 发送 FIN 表明它也发完了，然后等 client 的 ACK 这种情况下就会有四次挥手。
那么假设 client 发送 FIN 给 server 的时候 server 也没数据给 client，那么 server 就可以将 ACK 和它的 FIN 一起发给client ，然后等待 client 的 ACK，这样不就三次挥手了？

> 为什么要有 TIME_WAIT?

断开连接发起方在接受到接受方的 FIN 并回复 ACK 之后并没有直接进入 CLOSED 状态，而是进行了一波等待，等待时间为 2MSL。MSL 是 Maximum Segment Lifetime，即报文最长生存时间，RFC 793 定义的 MSL 时间是 2 分钟，Linux 实际实现是 30s，那么 2MSL 是一分钟。
> <font color=red >那么为什么要等 2MSL 呢？</font>

- 就是怕被动关闭方没有收到最后的 ACK，如果被动方由于网络原因没有到，那么它会再次发送 FIN， 此时如果主动关闭方已经 CLOSED 那就傻了，因此等一会儿。
- 假设立马断开连接，但是又重用了这个连接，就是五元组完全一致，并且序号还在合适的范围内，虽然概率很低但理论上也有可能，那么新的连接会被已关闭连接链路上的一些残留数据干扰，因此给予一定的时间来处理一些残留数据。

> 等待 2MSL 会产生什么问题？

如果服务器主动关闭大量的连接，那么会出现大量的资源占用，需要等到 2MSL 才会释放资源。
如果是客户端主动关闭大量的连接，那么在 2MSL 里面那些端口都是被占用的，端口只有 65535 个，如果端口耗尽了就无法发起送的连接了，不过我觉得这个概率很低，这么多端口你这是要建立多少个连接？


