---
title: TCP头部
toc: true
date: 2021-07-04 16:31:22
tags: TCP
categories: [Computer Network, TCP]
---

## 带着问题学习  

- 如何校验报文段是否损坏？ 如何CRC校验
- seq和ack是如何计算的？ 
- tcp校验位都有那些？ 6个 分别是什么含义？
- tcp如何计算首部长度？ 偏移量
- TCP Retransmission 重传？ 
- tcp spurious retransmission 又是什么呢？ 
- tcp dup ack 是什么？
- ack与ACK有什么区别？ 分别有什么作用？



## TCP头部结构

![](https://oscimg.oschina.net/oscnet/up-d3e5715ff000040a0b18b2c9374d7e55f53.png)   

学习TCP协议首先要看一下它的报文段是如何组成的；TCP报文段组成由两部分，第一部分是报文头部，第二部分是数据部分；

先看一下报文头，也就是TCP首部的组成；

### 16位端口

16位端口号：告知主机该报文段是来自哪里（源端口Source Port）以及传给哪个上层协议或应用程序（目的端口Destination Port）的。  
进行TCP通信时，客户端通常使用系统自动选择的临时端口号，而服务器则使用知名服务端口号（比如DNS协议对应端口53，HTTP协议对应80，这些端口号可在/etc/services文件中找到）。  

### 序列号（Seq）

占32位，也就是4字节长度，序号范围自然也是是0~2^32-1。TCP是面向字节流的，TCP连接中传送的字节流中的每个字节都按顺序编号。整个要传送的字节流的起始序号必须要在连接建立时设置。首部中的序号字段值指的是本报文段所发送的数据的第一个字节的序号。   

TCP用序列号对数据包进行标记，以便在到达目的地后重新重装，假设当前的序列号为 s，发送数据长度为 l，则下次发送数据时的序列号为 s + l。在建立连接时通常由计算机生成一个随机数作为序列号的初始值。  

**这里存在一个疑问，第一次建立TCP连接的时候，网上一些博客上说seq是client随机生成的，也有的博客说是seq=1； 这里经过我抓包后，看到第一次创建TCP连接的时候，确实是1; **    


### 确认应答号（Ack）
Ack占32位，4个字节长度，表示期望收到对方下一个报文段的序号值。 用作对另一方发送来的TCP报文段的响应。其值是收到的TCP报文段的序号值加1。假设主机A和主机B进行TCP通信，那么A发送出的TCP报文段不仅携带自己的序号，而且包含对B发送来的TCP报文段的确认号。反之，B发送出的TCP报文段也同时携带自己的序号和对A发送来的报文段的确认号。  
TCP的可靠性，是建立在「每一个数据报文都需要确认收到」的基础之上的。  

就是说，通讯的任何一方在收到对方的一个报文之后，都要发送一个相对应的「确认报文」，来表达确认收到。 那么，确认报文，就会包含确认号。    

若确认号=N，则表明：到序号N-1为止的所有数据都已正确收到。    


### 数据偏移 Offset
占 0.5 个字节 (4 位)。 这个字段实际上是指出了TCP报文段的首部长度 ，它指出了TCP报文段的数据起始处距离TCP报文的起始处有多远。
> 注意数据起始处和报文起始处的意思，上面👆已经写到，TCP报文段的组成有两部分，TCP报文首部和数据部分，偏移量记录的是报文段开始和数据开始的长度，也就是报文首部的长度；  

一个数据偏移量 = 4 byte，由于4位二进制数能表示的最大十进制数字是 15，因此数据偏移的最大值是 60 byte，这也侧面限制了TCP首部的最大长度。  

### 保留Reserved
占 0.75 个字节 (6 位)。 保留为今后使用，但目前应置为 0。

### 标志位 TCP Flags
![](https://oscimg.oschina.net/oscnet/up-869702a0e6199a93eb3be514c04e28274a7.png)      
标志位，一共有6个，分别占1位，共6位。 每一位的值只有 0 和 1，分别表达不同意思。 如上图是使用wireshard抓包展示截图；  

#### ACK(Acknowlegemt)  ：确认序号有效  
> 当 ACK = 1 的时候，确认号（Acknowledgemt Number）有效。 一般称携带 ACK 标志的 TCP 报文段为「确认报文段」。为0表示数据段不包含确认信息，确认号被忽略。
TCP 规定，在连接建立后所有传送的报文段都必须把 ACK 设置为 1。  


#### RST(Reset)：重置连接  
> 当 RST = 1 的时候，表示 TCP 连接中出现严重错误，需要释放并重新建立连接。 一般称携带 RST 标志的 TCP 报文段为「复位报文段」。 

#### SYN(SYNchronization)：发起了一个新连接  
> 当 SYN = 1 的时候，表明这是一个请求连接报文段。 一般称携带 SYN 标志的 TCP 报文段为「同步报文段」。 在 TCP 三次握手中的第一个报文就是同步报文段，在连接建立时用来同步序号。
对方若同意建立连接，则应在响应的报文段中使 SYN = 1 和 ACK = 1。

#### PSH (Push): 推送
> 当 PSH = 1 的时候，表示该报文段高优先级，接收方 TCP 应该尽快推送给接收应用程序，而不用等到整个 TCP 缓存都填满了后再交付。  

#### FIN：释放一个连接  
> 当 FIN = 1 时，表示此报文段的发送方的数据已经发送完毕，并要求释放 TCP 连接。
一般称携带 FIN 的报文段为「结束报文段」。
在 TCP 四次挥手释放连接的时候，就会用到该标志。


### 窗口大小 Window Size
占16位。  
该字段明确指出了现在允许对方发送的数据量，它告诉对方本端的 TCP 接收缓冲区还能容纳多少字节的数据，这样对方就可以控制发送数据的速度。 窗口大小的值是指，从本报文段首部中的确认号算起，接收方目前允许对方发送的数据量。
> 例如，假如确认号是701，窗口字段是 1000。这就表明，从 701 号算起，发送此报文段的一方还有接收 1000 （字节序号是 701 ~ 1700） 个字节的数据的接收缓存空间。  

### 校验和 TCP Checksum
占16位。 由发送端填充，接收端对TCP报文段执行【CRC算法】，以检验TCP报文段在传输过程中是否损坏，如果损坏这丢弃。

检验范围包括首部和数据两部分，这也是 TCP 可靠传输的一个重要保障。

### 紧急指针 Urgent Pointer
占 2 个字节。 仅在 URG = 1 时才有意义，它指出本报文段中的紧急数据的字节数。 当 URG = 1 时，发送方 TCP 就把紧急数据插入到本报文段数据的最前面，而在紧急数据后面的数据仍是普通数据。

因此，紧急指针指出了紧急数据的末尾在报文段中的位置。

### 选项
<image src="https://oscimg.oschina.net/oscnet/up-e071765090a2bbf6a9944907b288c70cd4a.png" width="400">

- 每个选项开始是1字节kind字段，说明选项的类型
- kind为0和1的选项，只占一个字节
- 其他kind后有一字节len，表示该选项总长度（包括kind和len）
- kind为11，12，13表示tcp事务

**下面是常用选项：**
<image src="https://oscimg.oschina.net/oscnet/up-1043e2bd27321e5ae0bc13be398e989be3e.png" width=800 height=300>

  

### MTU（最大传输单元）
MTU（最大传输单元）是【链路层】中的网络对数据帧的一个限制，以以太网为例，MTU 为 1500 个字节。一个IP 数据报在以太网中传输，如果它的长度大于该 MTU 值，就要进行分片传输，使得每片数据报的长度小于MTU。分片传输的 IP 数据报不一定按序到达，但 IP 首部中的信息能让这些数据报片按序组装。IP 数据报的分片与重组是在网络层进完成的。


### MSS （最大分段大小）
MSS 是 TCP 里的一个概念（首部的选项字段中）。MSS 是 TCP 数据包每次能够传输的最大数据分段，TCP 报文段的长度大于 MSS 时，要进行分段传输。TCP 协议在建立连接的时候通常要协商双方的 MSS 值，每一方都有用于通告它期望接收的 MSS 选项（MSS 选项只出现在 SYN 报文段中，即 TCP 三次握手的前两次）。MSS 的值一般为 MTU 值减去两个首部大小（需要减去 IP 数据包包头的大小 20Bytes 和 TCP 数据段的包头 20Bytes）所以如果用链路层以太网，MSS 的值往往为 1460。而 Internet 上标准的 MTU 为 576，那么如果不设置，则MSS的默认值就为 536 个字节。TCP报文段的分段与重组是在运输层完成的。


## seq和ack的计算逻辑
<image src="https://oscimg.oschina.net/oscnet/up-710a22f4e6b6c961662879fe2ac6000cca9.png" width=900 height=500> 


## CRC校验


## 参考资料
[TCP协议中的seq/ack序号是如何变化的？](https://www.jianshu.com/p/15754b4e9458)  
[TCP协议详解](https://www.jianshu.com/p/ef892323e68f)  
[TCP协议详解（一）：TCP头部结构](https://blog.csdn.net/baidu_17611285/article/details/80171239)  
[TCP和UDP报文头格式](https://blog.csdn.net/zuochao_2013/article/details/80561793)  
[TCP协议详解](https://juejin.cn/post/6844903685563105293)   
[吃透TCP协议](https://juejin.cn/post/6844904131342123022)  

---

**传送门**    👇

[1、TCP报文头部](https://geekibli.github.io/wiki/TCP头部/)
[2、TCP三次握手](https://geekibli.github.io/wiki/TCP三次握手/)
[3、TCP最大报文段（MSS）](https://geekibli.github.io/wiki/TCP最大报文段（MSS）/)
[4、TCP超时与重传](https://geekibli.github.io/wiki/TCP超时与重传/)
[5、RTO重传计时器的计算](https://geekibli.github.io/wiki/TCP-RTO重传计数器的计算/)
[6、滑动窗口](https://geekibli.github.io/wiki/TCP-滑动窗口/)
[7、提升网络效率](https://geekibli.github.io/wiki/TCP-如何减少小报文提升网络效率/)
[8、TCP拥塞控制之慢启动](https://geekibli.github.io/wiki/TCP-拥塞控制之慢启动/)
[9、TCP拥塞控制之拥塞避免](https://geekibli.github.io/wiki/拥塞避免/)
[10、快速重传与快速恢复](https://geekibli.github.io/wiki/快速重传-快速恢复/)
[11、四次挥手](https://geekibli.github.io/wiki/TCP-四次挥手-断开连接/)