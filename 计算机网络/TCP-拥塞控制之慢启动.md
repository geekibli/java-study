---
title: TCP-拥塞控制之慢启动
toc: true
date: 2021-07-04 16:33:19
tags: TCP
categories: [Computer Network, TCP]
---


由于TCP是面向字节流的传输协议，可以发送不定长的字节流数据，TCP连接发送数据时会“先天性”尝试占用整个带宽，而当所有的TCP连接都尝试占用网络带宽时，就会造成网络的堵塞，而TCP慢启动算法则是为了解决这一场景；

## 全局思考
<img src="https://oscimg.oschina.net/oscnet/up-b70475e1aadd0776c54efdd5ecf26ef2606.png" width=700 height=400>

拥塞控制要面向整体思考，如上👆网络拓扑图，当左边的网络节点通过路由交换设备向右边的设备传输报文的时候，中间的某一链路的带宽肯定是一定的，这里假设1000M带宽，当左边R1以700Mb/s的速度向链路中发送数据，同时R2以600Mb/s的速率发送报文，那势必会有300Mb的数据报丢失；「路由交换设备基于存储转发来实现报文的发送」大量报文都是时，路由设备的缓冲队列肯定是慢的，这也会造成某些数据报在网络链路中停留时间过长，从而导致TCP通讯变慢，甚至网络瘫痪；

理想的情况下，当链路带宽占满以后，链路以最大带宽传输数据，当然显示中是不可能的，当发生轻度拥塞时，链路的吞吐量就开始下降了，发展到严重阻塞时，链路的吞吐量会严重地下降，甚至瘫痪；

那么，慢启动是如何发挥作用的呢？

## 拥塞窗口
> 拥塞窗口cwnd(congestion window)


- 通告窗口rwnd(receiver‘s advertised window) 
> 其实就是RCV.WND，标志在TCP首部的Window字段！
- 发送窗口swnd = min(cwnd，rwnd)
> 前面学习滑动窗口的时候提到发送窗口大致等于接受窗口，当引入拥塞窗口时，发送窗口就是拥塞窗口和对方接受窗口的最小值

<img src="https://oscimg.oschina.net/oscnet/up-4fffa8af1fb99c1ce534085f112fa9f065c.png" width=360 height=360>

> 每收到一个ACK，cwnd扩充一倍

慢启动的窗口大小如何设置呢？
如上所示，起初拥塞窗口设置成1个报文段大小，当发送端发送一个报文段并且没有发生丢包时，调整拥塞窗口为2个报文段大小，如果还没有发生丢包，一次类推，知道发生丢包停止；发送窗口以「指数」的方式扩大；慢启动是无法确知网络拥塞程度的情况下，以试探性地方式快速扩大拥塞窗口；


## 慢启动初始窗口

慢启动的拥塞窗口真的就如上面所说的以一个报文段大小作为初始值吗？  

<img src="https://oscimg.oschina.net/oscnet/up-e632e6592fd276be90e5cf65a8365b3ddfb.png" width=360 height=360>  

> 慢启动初始窗口 IW(Initial Window)的变迁

- 1 SMSS:RFC2001(1997)
- 2 - 4 SMSS:RFC2414(1998)
> IW = min (4*SMSS, max (2*SMSS, 4380 bytes))
- 10 SMSS:RFC6928(2013)
> IW = min (10*MSS, max (2*MSS, 14600))

> 其实在实际情况下，互联网中的网页都在10个mss左右，如果还是从1个mss开始，则会浪费3个RTT的时间；  

