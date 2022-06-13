---
title: JVM-jmap
toc: true
date: 2021-07-28 17:45:41
tags: JVM
categories: [Develop Lan,Java,JVM]
---

# jmap

命令jmap是一个多功能的命令。它可以生成 java 程序的 dump 文件， 也可以查看堆内对象示例的统计信息、查看 ClassLoader 的信息以及 finalizer 队列。

## jmap -heap pid
```java
Attaching to process ID 7183, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 25.242-b08

using thread-local object allocation.
Parallel GC with 4 thread(s)

Heap Configuration:
   MinHeapFreeRatio         = 0
   MaxHeapFreeRatio         = 100
   MaxHeapSize              = 2051014656 (1956.0MB)
   NewSize                  = 42991616 (41.0MB)
   MaxNewSize               = 683671552 (652.0MB)
   OldSize                  = 87031808 (83.0MB)
   NewRatio                 = 2
   SurvivorRatio            = 8
   MetaspaceSize            = 21807104 (20.796875MB)
   CompressedClassSpaceSize = 1073741824 (1024.0MB)
   MaxMetaspaceSize         = 17592186044415 MB
   G1HeapRegionSize         = 0 (0.0MB)

Heap Usage:
PS Young Generation
Eden Space:
   capacity = 233308160 (222.5MB)
   used     = 161611280 (154.12452697753906MB)
   free     = 71696880 (68.37547302246094MB)
   69.26945032698384% used
From Space:
   capacity = 1572864 (1.5MB)
   used     = 899896 (0.8582077026367188MB)
   free     = 672968 (0.6417922973632812MB)
   57.213846842447914% used
To Space:
   capacity = 1572864 (1.5MB)
   used     = 0 (0.0MB)
   free     = 1572864 (1.5MB)
   0.0% used
PS Old Generation
   capacity = 223346688 (213.0MB)
   used     = 115841432 (110.4749984741211MB)
   free     = 107505256 (102.5250015258789MB)
   51.866196466723515% used

41772 interned Strings occupying 4324472 bytes.

```


## 参考资料
[jvm 性能调优工具之 jmap](https://www.jianshu.com/p/a4ad53179df3)  
[JVM调试工具-jmap](https://pandora.blog.csdn.net/article/details/108705081)  
[通过jstack与jmap分析一次线上故障](https://blog.csdn.net/lengyue309/article/details/80590119)                 
  