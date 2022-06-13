---
title: 'JVM-Xms,Xmx和Xss'
toc: true
date: 2021-07-28 21:04:14
tags:
- Java
- JVM
categories: [Develop Lan,Java,JVM]
---

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210730163232526.png" alt="image-20210730163232526" style="zoom:47%;" />

# 性能调优参数Xms，Xmx，Xss的含义

## -Xss

规定了每个线程虚拟机栈及堆栈的大小，一般情况下，256k是足够的，此配置将会影响此进程中并发线程数的大小。
### -Xms

表示初始化JAVA堆的大小及该进程刚创建出来的时候，他的专属JAVA堆的大小，一旦对象容量超过了JAVA堆的初始容量，JAVA堆将会自动扩容到-Xmx大小。

### -Xmx

表示java堆可以扩展到的最大值，在很多情况下，通常将-Xms和-Xmx设置成一样的，因为当堆不够用而发生扩容时，会发生内存抖动影响程序运行时的稳定性。

### 堆内存分配：

JVM初始分配的内存由-Xms指定，默认是物理内存的1/64
JVM最大分配的内存由-Xmx指定，默认是物理内存的1/4
默认空余堆内存小于40%时，JVM就会增大堆直到-Xmx的最大限制；空余堆内存大于70%时，JVM会减少堆直到 -Xms的最小限制。
因此服务器一般设置-Xms、-Xmx相等以避免在每次GC 后调整堆的大小。对象的堆内存由称为垃圾回收器的自动内存管理系统回收。
非堆内存分配：
JVM使用-XX:PermSize设置非堆内存初始值，默认是物理内存的1/64；
由XX:MaxPermSize设置最大非堆内存的大小，默认是物理内存的1/4。
-Xmn2G：设置年轻代大小为2G。
-XX:SurvivorRatio，设置年轻代中Eden区与Survivor区的比值。


## 参考资料
1、[类似-Xms、-Xmn这些参数的含义：](https://blog.csdn.net/a1439775520/article/details/97787160)
2、[JVM三大性能调优参数Xms，Xmx，Xss的含义，你又知道多少呢](https://baijiahao.baidu.com/s?id=1671253445384660292&wfr=spider&for=pc)