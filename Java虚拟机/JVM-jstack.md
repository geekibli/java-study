---
title: JVM-jstack
toc: true
date: 2021-07-28 17:46:12
tags: JVM
categories: [Develop Lan,Java,JVM]
---

# jstack

## jstack 功能

主要分为两个功能：

a．  针对活着的进程做本地的或远程的线程dump；

b．  针对core文件做线程dump。

jstack用于生成java虚拟机当前时刻的线程快照。线程快照是当前java虚拟机内每一条线程正在执行的方法堆栈的集合，生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等。 线程出现停顿的时候通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么资源。 如果java程序崩溃生成core文件，jstack工具可以用来获得core文件的java stack和native stack的信息，从而可以轻松地知道java程序是如何崩溃和在程序何处发生问题。另外，jstack工具还可以附属到正在运行的java程序中，看到当时运行的java程序的java stack和native stack的信息, 如果现在运行的java程序呈现hung的状态，jstack是非常有用的。




## jstack 操作方式

> jps -l | grep keyword -> pid
jstack pid


jstack结果如下；
```
"lettuce-nioEventLoop-4-1" #639 daemon prio=5 os_prio=0 tid=0x00007ff27025d800 nid=0x258f runnable [0x00007ff262af7000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
	at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
	at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:93)
	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
	- locked <0x000000008988d6a8> (a io.netty.channel.nio.SelectedSelectionKeySet)
	- locked <0x000000008988d770> (a java.util.Collections$UnmodifiableSet)
	- locked <0x000000008988d600> (a sun.nio.ch.EPollSelectorImpl)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:101)
	at io.netty.channel.nio.SelectedSelectionKeySetSelector.select(SelectedSelectionKeySetSelector.java:68)
	at io.netty.channel.nio.NioEventLoop.select(NioEventLoop.java:803)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:457)
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.lang.Thread.run(Thread.java:748)
```


## 参考资料
原文链接：https://blog.csdn.net/weixin_30013175/article/details/113901522