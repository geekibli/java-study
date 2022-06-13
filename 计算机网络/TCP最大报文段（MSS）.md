---
title: TCP最大报文段（MSS）
toc: true
date: 2021-07-04 16:31:56
tags: TCP
categories: [Computer Network, TCP]
---


## MSS产生的背景
我们都知道TCP协议是运输在传输层的协议，它是面向【字节流】的传输协议；
它的上层，应用层传输的数据是无限制的，但是它的下层也就是网络层和链路层由于路由等转发设备有内存等限制是不可能无限制传输任何大小的报文的，它们一定会限制报文的长度，因此 **TCP协议要完成的工作是将从应用层接受到的任意长度数据，切割成多个报文段，MSS就是如何切割报文段的依据。**

<image src="https://oscimg.oschina.net/oscnet/up-004bb74a9d5648d949d9549219c677f568f.png" width=450 height=300>


## 什么是MSS
MSS（Max Segment Size）：仅指 TCP 承载数据，不包含 TCP 头部的大小，参见 RFC879

## MSS 选择目的
- 尽量每个 Segment 报文段携带更多的数据，以减少头部空间占用比率 
- 防止 Segment 被某个设备的 IP 层基于 MTU 拆分
> IP层基于MTU的数据拆分是效率极差的，一个报文段丢失，所有的报文段都要重传

## MSS默认大小
> 默认 MSS:536 字节(默认 MTU576 字节，20 字节 IP 头部，20 字节 TCP 头部)

## MSS在什么时候使用
> 握手阶段协商 MSS 这个在TCP三次握手的文章中已经提及过了！

## MSS 分类
- **发送方最大报文段:** 
> SMSS:SENDER MAXIMUM SEGMENT SIZE
- **接收方最大报文段:** 
> RMSS:RECEIVER MAXIMUM SEGMENT SIZE



## 在TCP常用选项中可以看到【MSS】的选项
<image src="https://oscimg.oschina.net/oscnet/up-c1229634eb30eaa7391516cbee75258c9e1.png" width=750 height=420>


## TCP流与报文段在数据传输中的状态
<image src="https://oscimg.oschina.net/oscnet/up-1a0015bf7b4d8481a4e624b0920bbb43917.png" width=450 height=620>

从上图可以看到，左边客户端在发送字节流数据给到右边客户端，客户端发送一个连续的字节流，会在TCP层按照MSS大小规定进行拆分成多个小的报文段，分别传送到另一个客户端或者其他的接收端；
