---
title: TCP-RTO重传计数器的计算
toc: true
date: 2021-07-04 16:32:27
tags: TCP
categories: [Computer Network, TCP]
---


> 之前的文章已经介绍了TCP超时重传的过程中使用了定时器的策略，当定时器规定时间内未收到确认报文之后，就会触发报文的重传，同时定时器复位；那么定时器超时时间（RTO Retramission Timeout）是如何计算的呢？


## 什么是RTT？
了解RTO如何计算之前，首先明确一个概念「 **RTT** 」；

<image src="https://oscimg.oschina.net/oscnet/up-9b45623a3b0652d842ffa3eecea13e92183.png" width=550 height=400>   

如上图所示，从client发送第一个「SYN」报文，到Server接受到报文，并且返回「SYN ACK」报文之后，client接受到Server的「ACK」报文之后，client所经历的时间，叫做1个RTT时间；

## 如何在重传下有效测量RTT？
![](https://oscimg.oschina.net/oscnet/up-6c558bfea1825bf5d3b2da74e087cdde43a.png)

如上图两种情况：
第一种，左侧a图所示，当一端发送的数据报丢失后要进行重传，到重传之后接收到确认报文之后，这种场景下该如何计算RTT呢？开始时间是按照第一次发送数据报时间呢还是按照重传数据报的时间呢？

> 按照常理来说，如右侧b图所示，RTT时间应该以RTT2为准；

第二种，左侧b图所示，第一次发送数据报文时，由于网络时延导致RTO时间内没有收到接收段的确认报文，发送端进行重发，但是在刚刚重发之后就收到了第一次报文的确认报文，那这种情况RTT该如何计算呢？

> 如右侧a图所示，RTT时间应该以RTT1为准；

就像上面提及的两种情况，一会以第一个RTT1为准，一会以RTT2为准，那么TCP协议如何正确的计算出RTT呢？

## 使用Timestamp方式计算RTT
之前的文章中在介绍TCP超时与重传的笔记中有介绍通过使用Timtstamp的方式来区分相同Seq序列号的不同报文，
其实在TCP报文首部存储Timestamp的时候，会存储报文的发送时间和确认时间，如下所示：
<image src="https://oscimg.oschina.net/oscnet/up-ad80265bbba417c72a8d02a5c0be7be5f83.png" width=800 height=230> 


## 如何计算RTO？
上面👆说到了RTT如何计算，那个RTO和RTT有什么关系呢？
<image src="https://oscimg.oschina.net/oscnet/up-e25afa62f99830eb02c4c1df0c015dbde8a.png">
 RTO的取值将会影响到TCP的传输效率以及网络的吞吐量；
> 通常来说RTO应该略大于RTT，如果RTO小于RTT，则会造成发送端频繁重发，可能会造成网络阻塞；如果RTO设置的过大，则接受端已经收到确认报文之后的一段时间内仍然不能发送其他报文，会造成两端性能的浪费和网络吞吐量的下降；

### 平滑RTO
网络的RTT是不断的变化的，所以计算RTO的时候，应当考虑RTO的平滑性，尽量避免RTT波动带来的干扰，以抵挡瞬时变化；

**平滑RTO在文档RFC793定义，给出如下计算方式：**
- SRTT (smoothed round-trip time) = ( α * SRTT ) + ((1 - α) * RTT)
> α 从 0到 1(RFC 推荐 0.9)，越大越平滑
- RTO = min[ UBOUND, max[ LBOUND, (β * SRTT) ] ]
> 如 UBOUND为1分钟，LBOUND为 1 秒钟， β从 1.3 到 2 之间 

这种计算方式不适用于 RTT 波动大(方差大)的场景,如果网络的RTT波动很大，会造成RTO调整不及时；


### 追踪RTT方差计算RTO

> RFC6298(RFC2988)，其中α = 1/8， β = 1/4，K = 4，G 为最小时间颗粒:
- **首次计算 RTO，R为第 1 次测量出的 RTT**
```
SRTT(smoothed round-trip time) = R
RTTVAR(round-trip time variation) = R/2
RTO = SRTT + max (G, K*RTTVAR)
```
- **后续计算 RTO，R’为最新测量出的 RTT**
```
SRTT= (1-α)*SRTT+α*R’
RTTVAR=(1-β)*RTTVAR+β*|SRTT-R’|
RTO = SRTT + max (G, K*RTTVAR)
```
