---
title: TCP三次握手
toc: true
date: 2021-07-04 16:31:42
tags: TCP
categories: [Computer Network, TCP]
---

## 握手🤝的目的

- 同步Sequence序列号
> 初始化序
列号ISN （Inital Sequence Number）
- 交换TCP通讯的参数
> 比如最大报文段参数（MSS）、窗口比例因子（Window）、选择性确认（SACK）、制定校验和算法；


## 三次握手握手过程

<image src="https://oscimg.oschina.net/oscnet/up-7ee7a8388d0fc3798344cad07f585bf7961.png" width=500 height=300>

TCP三次握手的大致流程图如上👆  


使用tcpdump抓包分析三次🤝握手报文中Seq和Ack的变化
```
tcpdump  port 80 -c 3 -S
```
![image.png](https://sjwx.easydoc.xyz/47754217/files/kmov1k44.png)  

### 第一次握手🤝
```
IP upay.60734 > 100.100.15.23.http: Flags [S], seq 3800409106, win 29200, options [mss 1460,sackOK,TS val 839851765 ecr 0,nop,wscale 7], length 0
```
客户端upay访问服务端80端口，发送一个「 seq=3800409106 」 ，同时标志位SYN=1，声明此次握手是要建立连接；

### 第二次握手🤝
```
IP 100.100.15.23.http > upay.60734: Flags [S.], seq 1981710286, ack 3800409107, win 14600, options [mss 1440,nop,nop,sackOK,nop,wscale 7], length 0
```
第二次握手，服务端收到客户端的申请连接强求（SYN=1）之后，在服务端自己准备好的情况下，给客户端发送 「 ACK=1 SYN=1 」的确认报文，SYN=1同样也是声明此次报文是建立连接的报文请求； ack= 3800409107 也就是第一个客户端发给服务端的seq+1（ack是接收方下次期望接口报文的开始位置）

### 第三次握手握手
```
IP upay.60734 > 100.100.15.23.http: Flags [.], ack 1981710287, win 229, length 0
```
客户端收到服务器返回的确认报文，确认可以进行连接，发送「 ack = 1981710287 」的确认报文，之后就完成了三次握手，TCP的连接就创建成功了，接下来双方就可以发送数据报了；


## TCP连接创建构成中状态的变更

<image src="https://oscimg.oschina.net/oscnet/up-d6920a59ec1cb6a738265b6f182cbdbe2ba.png" width=550 height=400>

- 首先客户端和服务端都是【CLOSED】状态，客户端发起连接请求之后，进入【SYN-SENT】状态，这个状态维持的时间很短，我们使用netstat去查看tcp连接状态的时候，基本上都不会看到这个状态，而服务端是在【LISTEN】状态，等待客户端的请求；
- 服务端收到客户端请求之后，发送「SYN ACK」确认报文，同时服务端进入【SYN-RECEIVED】状态，等待客户端的确认报文；
- 客户端收到服务端的同步确认请求之后，发送「ACK」确认报文，同时进入【ESTABLISHED】状态，准备后续的数据传输；
- 服务端收到三次握手最后的确认报文之后，进入【ESTABLISHED】状态，至此，一个TCP连接算是建立完成了，后面就是双方的通信了；

## TCB（Transmission Control Block）
> 保存连接使用的源端口、目的端口、目的 ip、序号、 应答序号、对方窗口大小、己方窗口大小、tcp 状态、tcp 输入/输出队列、应用层输出队 列、tcp 的重传有关变量等


## TCP性能优化和安全问题
<image src="https://oscimg.oschina.net/oscnet/up-e99592d430e1219134af66e69aee2e6ccbd.png"  width=550 height=400 >

正如我们了解的TCP三次握手🤝的流程，当有大量SYN请求到达服务端时，会进入到【SYN队列】，服务端收到第二次确认报文之后，会进入【ESTABLISHED】状态，服务端操作系统内核会将连接放入到【ACCEPT】队列中，当Nginx或者Tomcat这些应用程序在调用accept（访问内核）的时候，就是在【ACCEPT】队列中取出连接进行处理；

> 由此可见，【SYN】队列和【ACCEPT】是会影响服务器连接性能的重要因素，所以对于高并发的场景下，这两个队列一定是要设置的比较大的；

### 如何设置SYN队列大小
服务器端 SYN_RCV 状态
- net.ipv4.tcp_max_syn_backlog:SYN_RCVD 状态连接的最大个数
- net.ipv4.tcp_synack_retries:被动建立连接时，发SYN/ACK的重试次数

客户端 SYN_SENT 状态（服务端作为客户端，比如Ngnix转发等）
- net.ipv4.tcp_syn_retries = 6 主动建立连接时，发 SYN 的重试次数
- net.ipv4.ip_local_port_range = 32768 60999 建立连接时的本地端口可用范围


## Fast Open机制
<image src="https://oscimg.oschina.net/oscnet/up-071f997614b36eb8b4511db0f3ba0637d70.png" width=550 height=400 >

TCP如何对连接的次数以及连接时间进行优化的呢？这里提到Fast Open机制；
比如我们有一个Http Get请求，正常的三次握手🤝到收到服务端数据需要2个RTT的时间；FastOpen做出如下优化：
- 第一次创建连接的时候，也是要经历2个RTT时间，但是在服务端发送确认报文的时候，在报文中添加一个cookie；
- 等到下次客户端再需要创建请求的时候，直接将【SYN】和cookie一并带上，可以一次就创建连接，经过一个RTT客户端就可以收到服务端的数据；

#### 如何Linux上打开TCP Fast Open
 net.ipv4.tcp_fastopen:系统开启 TFO 功能 
- 0:关闭
- 1:作为客户端时可以使用 TFO
- 2:作为服务器时可以使用 TFO
- 3:无论作为客户端还是服务器，都可以使用 TFO

### SYN攻击
#### 什么是SYN攻击？
  正常的服务通讯都是由操作系统内核实现的请求报文来创建连接的，但是，可以人为伪造大量不同IP地址的SYN报文，也就是上面👆状态变更图中的SYN请求，但是收到服务端的ACK报文之后，却不发送对于服务端的ACK请求，也就是没有第三次挥手，这样会造成大量处于【SYN-RECEIVED】状态的TCP连接占用大量服务端资源，导致正常的连接无法创建，从而导致系统崩坏；
#### SYN攻击如何查看
```
netstat -nap | grep SYN_RECV
```
> 如果存在大量【SYN-RECEIVED】的连接，就是发生SYN攻击了；

#### 如何规避SYN攻击？

- **net.core.netdev_max_backlog**
> 接收自网卡、但未被内核协议栈处理的报文队列长度

- **net.ipv4.tcp_max_syn_backlog**
> SYN_RCVD 状态连接的最大个数

- **net.ipv4.tcp_abort_on_overflow**
>超出处理能力时，对新来的 SYN 直接回包 RST，丢弃连接

- 设置SYN Timeout
> 由于SYN Flood攻击的效果取决于服务器上保持的SYN半连接数，这个值=SYN攻击的频度 x SYN Timeout，所以通过缩短从接收到SYN报文到确定这个报文无效并丢弃改连接的时间，例如设置为20秒以下，可以成倍的降低服务器的负荷。但过低的SYN Timeout设置可能会影响客户的正常访问。

- 设置SYN Cookie (net.ipv4.tcp_syncookies = 1)
> 就是给每一个请求连接的IP地址分配一个Cookie，如果短时间内连续受到某个IP的重复SYN报文，就认定是受到了攻击，并记录地址信息，以后从这个IP地址来的包会被一概丢弃。这样做的结果也可能会影响到正常用户的访问。

<image src="https://oscimg.oschina.net/oscnet/up-8cce8662408ffa1f5ed678be972248b92ca.png" width=800 height=350>

当 SYN 队列满后，新的 SYN 不进入队列，计算出 cookie 再 以 SYN+ACK 中的序列号返回客户端，正常客户端发报文时， 服务器根据报文中携带的 cookie 重新恢复连接
> 由于 cookie 占用序列号空间，导致此时所有 TCP 可选 功能失效，例如扩充窗口、时间戳等


## TCP_DEFER_ACCEPT

这个是做什么呢？ 正如上面👆操作系统内核展示图所示，内核中维护两个队列【SYN】队列和【ACCEPT】队列，只有当收到客户端的ACK报文之后，连接会进入到【ACCEPT】，同时服务器的状态是
【ESTABLISHED】状态，此时操作系统并不会去激活应用进程，而是会等待，知道收到真正的data分组之后，才会激活应用进程，这是为了提高应用进程的执行效率，避免应用进程的等待；



> TCP三次握手为什么不能是两次或者四次

参见文章：[敖丙用近 40 张图解被问千百遍的 TCP 三次握手和四次挥手面试题](https://mp.weixin.qq.com/s/rX3A_FA19n4pI9HicIEsXg)
