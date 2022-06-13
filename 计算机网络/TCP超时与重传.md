---
title: TCP超时与重传
toc: true
date: 2021-07-04 16:32:08
tags: TCP
categories: [Computer Network, TCP]
---


## 背景
<image src="https://oscimg.oschina.net/oscnet/up-4f1230ee04f4c197dd5c6de167991878366.png" width=680 height=300>

如上图👆所示，设备A向设备B发送消息，消息在网络中会由于各种各样的问题导致丢失，那么该如何解决上述问题呢？

## 采用定时器重传
> PAR：Positive Acknowledgment with Retransmission

<image src="https://oscimg.oschina.net/oscnet/up-17f5567e452afb45cf18d01e62bdfa33f96.png" width=680 height=330>

**最简单的思路是在发送方设置「 定时器 」：**
- 当设备A发送第一条消息之后，在定时器规定的时间内，如果收到设备B的确认报文，则设备A继续发送下一个报文，同时定时器复位；
- 如果第一条消息发送时间超出了定时器规定的时间，则设备A将重新发送第一条消息，同时重新设置定时器；
- 这种方式是串型发送的，只有第一个消息发送成功之后，才可以发送下一条消息，「 **效率极差** 」；


## 并发能力PAR
> 基于上述PAR效率低下的方式进行改造，在发送端采用并发+定时器的方式进行数据发送；

<image src="https://oscimg.oschina.net/oscnet/up-96114b57ac73a41915bea1a7d11e0145102.png" width=600 height="500">

- 首先设备A可以同时发送多个消息或者报文段，每个报文段具有一个标志字段【#XX】去标志唯一，每个报文段连接具有自己的定时器；
- 设备B规定时间内收到设备A发送的数据之后并且设备A得到设备B的确认之后，设备A将定时器清除
- 同PAR一样，设备B没有在规定的时间内发送确认报文，设备A将这个报文所对应的定时器复位，重新发送这个报文

### 并发发送带来的问题
采用并发的方式发送消息或者报文段固然提升了发送端的性能，但是发送端发送的消息可能接受端不能完全处理，**这是双方报文处理速度或者效率不一致的问题**；

所以对于接收端设备B，应该明确自己可能接受的数据量，并且在确认报文中同步到发送端设备A，设备A根据设备B的处理能力来调整发送数据的大小；也就是上图中的「 limit」；


## 继续延伸
Sequment序列号和Ack序列号的设计理念或者设计初衷是「 **解决应用层字节流的可靠发送** 」 
- 跟踪「应用层」的发送端数据是否送达
- 确定「接收端有序的」接收到「字节流」
- **序列号的值针对的是字节而不是报文** ⚠️⚠️⚠️

> TCP的定位就是面向字节流的！


## TCP序列号如何设计的

<image src="https://oscimg.oschina.net/oscnet/up-f5f274ec300162985b9bef86ebd94f6a81e.png" width=450 height=450>

通过TCP报文头我们可以知道，Sequment序列号包括32位长度；也就是说一个Sequment可以发送2的32次方个字节，大约4G的数量，Sequment就无法表示了，当传输的数据超过“4G”之后，如果这个连接依然要使用的话，Sequment会重新复用；Sequment复用会产生一个问题，也就是序列号回绕；👇

## 序列号回绕
> 序列号回绕 (Protect Against Wrapped Sequence numbers)

<image src="https://oscimg.oschina.net/oscnet/up-4edfd2031f0ec6ebdaa23c497e18df359cd.png" width=800 height=380>


- 当一个连接要发送6G的数据是，A、B、C、D分别发送1G的数据，如果继续使用此连接，E下一次发送数据1G，Seq序列号复用，E报文段的序列号和A报文段的序列号表示相同
- 按照上面的逻辑继续发送数据，F报文段的Seq标志和B报文段的是一样的；
- 加入B报文段在发送过程中丢失了，直到接受端接收了F报文段的同时B报文段到达接受端，接受端该如何区分相同Seq序列号不同数据的报文段呢？
- 其实TCP解决这个问题很简单，就是在每个报文段上添加Tcp Timestamp时间戳，类似于版本号的理念；
- 接收端收到相同Seq序列号的报文段是可以根据时间戳来进行区分；

