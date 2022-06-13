---
title: TCP-如何减少小报文提升网络效率
toc: true
date: 2021-07-04 16:32:55
tags: TCP
categories: [Computer Network, TCP]
---



# 如何减少小报文提升网络效率

每一个TCP报文段都包含20字节的IP头部和20字节的TCP首部，如果报文段的数据部分很少的话，网络效率会很差；

## SWS(Silly Window syndrome) 糊涂窗口综合症

<image src="https://oscimg.oschina.net/oscnet/up-777275434842bda3be80da2687c4556ee3b.png" width=600 height=450 >

如上图👆所示场景，在之前的滑动窗口已经了解过，随着服务端处理连接数据能力越来越低，服务端的可用窗口不断压缩，最终导致窗口关闭；


### SWS 避免算法
SWS 避免算法对发送方和接收方都做客
- 接收方
> David D Clark 算法:窗口边界移动值小于 min(MSS, 缓存/2)时，
通知窗口为 0
- 发送方
> Nagle 算法:
1、TCP_NODELAY 用于关闭 Nagle 算法
2、没有已发送未确认报文段时，立刻发送数据
3、存在未确认报文段时，直到:1-没有已发送未确认报文段，或者 2-数据长度达到MSS时再发送


## TCP delayed acknowledgment 延迟确认
实际情况下，没有携带任何数据的ACK报文也会造成网络效率低下的，因为确认报文也包含40字节的头部信息，但仅仅是为了传输ACK=1这样的信息，为了解决这种情况，TCP有一种机制，叫做延迟确认，如下👇：
- 当有响应数据要发送时,ack会随着响应数据立即发送给对方.
- 如果没有响应数据,ack的发送将会有一个延迟,以等待看是否有响应数据可以一起发送
- 如果在等待发送ack期间,对方的第二个数据段又到达了,这时要立即发送ack

### 那个延迟的时间如何设置呢？
<image src="https://oscimg.oschina.net/oscnet/up-5933d7b1310c191603f366eb55669a7cdc8.png" width=400 height=250>

上面👆是Linux操作系统对于TCP延时的定义。

HZ是什呢？其实那是和操作系统的时钟相关的，具体的操作系统间各有差别；
如何查看Linux操作系统下的HZ如何设置呢？
```
cat /boot/config- `-uname -r` | grep '^GONFIG_HZ='
```

## TCP_CORK

> sendfile 零拷贝技术

