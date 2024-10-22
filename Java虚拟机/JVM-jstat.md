---
title: JVM-jstat
toc: true
date: 2021-07-28 17:46:25
tags: JVM
categories: [Develop Lan,Java,JVM]
---


# jstat


jstat是一个简单的实用工具，在JDK中存在，用于提供与JVM性能相关的统计信息，例如垃圾收集，编译活动。 jstat的主要优势在于，它可以在运行JVM且无需任何先决条件的情况下动态捕获这些指标。 这是什么意思？ 例如，如果要捕获与垃圾回收相关的统计信息，则需要在启动JVM之前传递以下参数：
>  -Xlog:gc*:file={file-path} 

此参数将启用GC日志并将其打印在指定的文件路径中。 假设您尚未传递此参数，那么将不会生成与GC相关的统计信息。 这是jstat可以派上用场的地方。 您可以动态地连接到JVM并捕获GC，编译相关的统计信息如下所示。

## jstat操作

执行命令：
```
jstat -gc -t 11656 10000 30 
```
-gc ：将显示与垃圾收集相关的统计信息

自JVM启动以来的-t时间戳将被打印

11656：目标JVM进程ID

10000：每10,000毫秒（即10秒）将打印一次统计信息。

30 ：将打印30次迭代的统计信息。 因此，以上选项将导致JVM打印指标300秒（即10秒x 30次迭代）。

（请注意，除了-gc之外，您还可以传递其他各种选项来生成不同的数据集。有关不同选项的更多详细信息，请参见此处 。）
打印结果：

```
Timestamp        S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT
        34486.1 1536.0 1536.0  0.0   878.8  226816.0 132809.2  218112.0   113086.4  120664.0 111284.9 14464.0 12928.3    355    3.523   6      1.126    4.649
        34496.3 1536.0 1536.0  0.0   878.8  226816.0 138030.9  218112.0   113086.4  120664.0 111284.9 14464.0 12928.3    355    3.523   6      1.126    4.649
        34506.3 1536.0 1536.0  0.0   878.8  226816.0 195648.1  218112.0   113086.4  120664.0 111284.9 14464.0 12928.3    355    3.523   6      1.126    4.649
```

### 字段解读
S0C –幸存者0区域的容量，以KB为单位

S1C –幸存者1区域的容量，以KB为单位

S0U –幸存者0区域使用的空间以KB为单位

S1U –幸存者1区域以KB为单位使用空间

EC –伊甸园地区容量（KB）

欧盟–伊甸园地区的已利用空间（以KB为单位）

OC –旧区域容量（KB）

OU –旧区域的已利用空间，以KB为单位

MC –元空间区域容量，以KB为单位

MU –元空间区域使用的空间以KB为单位

CCSC –压缩类空间区域的容量，以KB为单位

CCSU –压缩类空间区域以KB为单位使用空间

YGC –迄今为止发生的年轻GC事件的数量

YGCT –到目前为止，年轻GC花费的时间

FGC –迄今为止已发生的完全GC事件的数量

FGCT –到目前为止已花费的完整GC时间

GCT –到目前为止所花费的GC时间总量（基本上是YGCT + FGCT）


## 参考资料
[jstat分析_jstat –分析](https://blog.csdn.net/dnc8371/article/details/107255359) 