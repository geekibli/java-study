# JVM-垃圾收集器



# 1、什么是垃圾收集器

如果说垃圾收集算法是内存回收的方法论，那么垃圾收集器就是内存回收的具体实现。

JVM规范对于垃圾收集器的应该如何实现没有任何规定，因此不同的厂商、不同版本的虚拟机所提供的垃圾收集器差别较大，这里只看HotSpot虚拟机。 就像没有最好的算法一样，垃圾收集器也没有最好，只有最合适。我们能做的就是根据具体的应用场景选择最合适的垃圾收集器。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731155230422.png" alt="image-20210731155230422" style="zoom: 33%;" />

​	上图展示了7种作用于不同分代的收集器，其中用于回收新生代的收集器包括Serial、PraNew、Parallel Scavenge，回收老年代的收集器包括Serial Old、Parallel Old、CMS，还有用于回收整个Java堆的G1收集器。不同收集器之间的连线表示它们可以搭配使用。

# 2、串行，并行和并发

## 2.1 串行

计算机中的串行是用 Serial 表示。A 和 B 两个任务运行在一个 CPU 线程上，在 A 任务执行完之前不可以执行 B。即，在整个程序的运行过程中，仅存在一个运行上下文，即一个调用栈一个堆。程序会按顺序执行每个指令。

## 2.2 并行

并行性指两个或两个以上事件或活动在同一时刻发生。在多道程序环境下，**并行性使多个程序同一时刻可在不同 CPU 上同时执行**。比如，A 和 B 两个任务可以同时运行在不同的 CPU 线程上，效率较高，但受限于 CPU 线程数，如果任务数量超过了 CPU 线程数，那么每个线程上的任务仍然是顺序执行的。

## 2.3 并发

并发指多个线程在宏观(相对于较长的时间区间而言)上表现为同时执行，而实际上是轮流穿插着执行，**并发的实质是一个物理 CPU 在若干道程序之间多路复用**，其目的是提高有限物理资源的运行效率。 并发与并行串行并不是互斥的概念，如果是在一个CPU线程上启用并发，那么自然就还是串行的，而如果在多个线程上启用并发，那么程序的执行就可以是既并发又并行的。

## 2.4 JVM 垃圾收集中的串行、并行和并发

在 JVM 垃圾收集器中也涉及到如上的三个概念。

- 串行（Serial）：使用单线程进行垃圾回收的回收器。
- 并行（Parallel）：指多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。
- 并发（Concurrent）：指用户线程与垃圾收集线程同时执行（但不一定是并行，可能会交替执行），用户程序在继续运行，而垃圾收集器运行在另一个 CPU 上。

在了解了这些概念之后，我们开始具体介绍常用的垃圾收集器。

# 3、主流的垃圾收集器

## 3.1 Serial收集器

Serial（串行）收集器收集器是最基本、历史最悠久的垃圾收集器了（新生代采用复制算法，老生代采用标志整理算法）。大家看名字就知道这个收集器是一个单线程收集器了。 它的 “单线程” 的意义不仅仅意味着它只会使用一条垃圾收集线程去完成垃圾收集工作，更重要的是它在`进行垃圾收集工作的时候必须暂停其他所有的工作线程`（ "Stop The World" ：将用户正常工作的线程全部暂停掉），直到它收集结束。 

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731143820628.png" alt="image-20210731143820628" style="zoom:50%;" />

上图中：

- 新生代采用复制算法，Stop-The-World
- 老年代采用标记-整理算法，Stop-The-World

当它进行GC工作的时候，虽然会造成Stop-The-World，正如每种算法都有存在的原因，该串行收集器也有存在的原因：因为简单而高效（与其他收集器的单线程比），对于限定单个CPU的环境来说，没有线程交互的开销，专心做GC，自然可以获得最高的单线程效率。

所以Serial收集器对于运行在client模式下的应用是一个很好的选择（到目前为止，它依然是虚拟机运行在client模式下的默认新生代收集器） 串行收集器的缺点很明显，虚拟机的开发者当然也是知道这个缺点的，所以一直都在缩减Stop The World的时间。 在后续的垃圾收集器设计中停顿时间在不断缩短（但是仍然还有停顿，寻找最优秀的垃圾收集器的过程仍然在继续）

### 3.1.1 特点

- 针对新生代的收集器；
- 采用复制算法；
- 单线程收集；
- 进行垃圾收集时，必须暂停所有工作线程，直到完成； 即会"Stop The World"；

### 3.1.2 应用场景

- `依然是HotSpot在Client模式下默认的新生代收集器；`
- 也有优于其他收集器的地方： 简单高效（与其他收集器的单线程相比）；
- 对于限定单个CPU的环境来说，Serial收集器没有线程交互（切换）开销，可以获得最高的单线程收集效率；
- 在用户的桌面应用场景中，可用内存一般不大（几十M至一两百M），可以在较短时间内完成垃圾收集（几十MS至一百多MS）,只要不频繁发生，这是可以接受的

### 3.1.3 参数设置

添加该参数来显式的使用串行垃圾收集器: "-XX:+UseSerialGC"

## 3.2 ParNew收集器

>  Serial收集器的多线程版本-使用多条线程进行GC

ParNew收集器其实就是Serial收集器的多线程版本，除了使用多线程进行垃圾收集外，其余行为（控制参数、收集算法、回收策略等等）和Serial收集器完全一样。 它是许多运行在Server模式下的虚拟机的首要选择，除了Serial收集器外，目前只有它能与CMS收集器配合工作。 CMS收集器是一个被认为具有划时代意义的并发收集器，因此如果有一个垃圾收集器能和它一起搭配使用让其更加完美，那这个收集器必然也是一个不可或缺的部分了。 收集器的运行过程如下图所示：

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731144404779.png" alt="image-20210731144404779" style="zoom:50%;" />

### 3.2.1 应用场景：

在Server模式下，ParNew收集器是一个非常重要的收集器，**因为除Serial外，目前只有它能与CMS收集器配合工作**； 但<font color=red>在单个CPU环境中，不会比Serail收集器有更好的效果，因为存在线程交互开销。</font>

### 3.2.2 设置参数

指定使用CMS后，会默认使用ParNew作为新生代收集: "-XX:+UseConcMarkSweepGC" 强制指定使用ParNew:
"-XX:+UseParNewGC" 指定垃圾收集的线程数量，ParNew默认开启的收集线程与CPU的数量相: "-XX:ParallelGCThreads"

### 3.2.3 为什么只有ParNew能与CMS收集器配合

- CMS是HotSpot在JDK1.5推出的第一款真正意义上的并发（Concurrent）收集器，**第一次实现了让垃圾收集线程与用户线程（基本上）同时工作**；
- CMS作为老年代收集器，但却无法与JDK1.4已经存在的新生代收集器Parallel Scavenge配合工作；
- 因为Parallel Scavenge（以及G1）都没有使用传统的GC收集器代码框架，而另外独立实现；而其余几种收集器则共用了部分的框架代码；

## 3.3 Parallel Scavenge收集器

Parallel Scavenge收集器是一个新生代收集器，它也是使用复制算法的收集器，又是并行的多线程收集器。 Parallel Scavenge收集器关注点是吞吐量（如何高效率的利用CPU）。 CMS等垃圾收集器的关注点更多的是用户线程的停顿时间（提高用户体验）。 **所谓吞吐量就是CPU中用于运行用户代码的时间与CPU总消耗时间的比值**。（吞吐量：CPU用于用户代码的时间/CPU总消耗时间的比值，即=运行用户代码的时间/(运行用户代码时间+垃圾收集时间)。比如，虚拟机总共运行了100分钟，其中垃圾收集花掉1分钟，那吞吐量就是99%。） 运行示意图：

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731144823884.png" alt="image-20210731144823884" style="zoom:50%;" />

### 3.3.1 特点

- 新生代收集器；
- 采用复制算法；
- 多线程收集；
- CMS等收集器的关注点是尽可能地缩短垃圾收集时用户线程的停顿时间；而Parallel Scavenge收集器的目标则是达一个可控制的吞吐量（Throughput）；

### 3.3.2 应用场景

- 高吞吐量为目标，即减少垃圾收集时间，让用户代码获得更长的运行时间；
- 当应用程序运行在具有多个CPU上，对暂停时间没有特别高的要求时，即程序主要在后台进行计算，而不需要与用户进行太多交互；
- 例如，那些执行批量处理、订单处理（对账等）、工资支付、科学计算的应用程序；

### 3.3.3 设置参数

Parallel Scavenge收集器提供两个参数用于精确控制吞吐量：

- 控制最大垃圾收集停顿时间 "-XX:MaxGCPauseMillis"
- 控制最大垃圾收集停顿时间，大于0的毫秒数； MaxGCPauseMillis设置得稍小，停顿时间可能会缩短，但也可能会使得吞吐量下降；因为可能导致垃圾收集发生得更频繁； 设置垃圾收集时间占总时间的比率 "-XX:GCTimeRatio"
- 设置垃圾收集时间占总时间的比率，0 < n < 100的整数； GCTimeRatio相当于设置吞吐量大小； 垃圾收集执行时间占应用程序执行时间的比例的计算方法是： 1 / (1 + n) 。 例如，选项-XX:GCTimeRatio=19，设置了垃圾收集时间占总时间的5% = 1/(1+19)；默认值是1% = 1/(1+99)，即n=99； 垃圾收集所花费的时间是年轻一代和老年代收集的总时间； 如果没有满足吞吐量目标，则增加代的内存大小以尽量增加用户程序运行的时间；

## 3.4 Serial Old收集器

Serial收集器的老年代版本，它同样是一个单线程收集器。 它主要有两大用途：一种用途是在JDK1.5以及以前的版本中与Parallel Scavenge收集器搭配使用，另一种用途是作为CMS收集器的后备方案

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731143820628.png" alt="image-20210731143820628" style="zoom:50%;" />

### 3.4.1 特点

- **针对老年代；**
- 采用"标记-整理-压缩"算法（Mark-Sweep-Compact）；
- 单线程收集；

### 3.4.2 应用场景

- 主要用于Client模式；
- 而在Server模式有两大用途： 
  （A）、在JDK1.5及之前，与Parallel Scavenge收集器搭配使用（JDK1.6有Parallel Old收集器可搭配Parallel Scavenge收集器）；
  （B）、作为CMS收集器的后备预案，在并发收集发生Concurrent Mode Failure时使用；

## 3.5 Parallel Old收集器

Parallel Scavenge收集器的老年代版本。 使用多线程和“标记-整理”算法。在注重吞吐量以及CPU资源的场合，都可以优先考虑 Parallel Scavenge收集器和Parallel Old收集器。 在JDK1.6才有的。

### 3.5.1 特点

- **针对老年代；**
- 采用"标记-整理-压缩"算法；
- 多线程收集； Parallel Scavenge/Parallel Old收集器运行示意图如下

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731145301909.png" alt="image-20210731145301909" style="zoom:50%;" />

### 3.5.2 应用场景

- JDK1.6及之后用来代替老年代的Serial Old收集器；
- 特别是在Server模式，多CPU的情况下； 这样在注重吞吐量以及CPU资源敏感的场景，就有了Parallel Scavenge（新生代）加Parallel Old（老年代）收集器的"给力"应用组合；

### 3.5.3 设置参数

指定使用Parallel Old收集器: "-XX:+UseParallelOldGC"

## 3.6 CMS（Concurrent Mark Sweep）收集器

CMS（Concurrent Mark Sweep）收集器是一种以**获取最短回收停顿时间为目标的收集器**。`它非常适合在注重用户体验的应用上使用`。

### 3.6.1 特点

- **针对老年代**
- 基于"标记-清除"算法(不进行压缩操作，会产生内存碎片)
- 以获取最短回收停顿时间为目标
- 并发收集、低停顿
- 需要更多的内存 CMS是HotSpot在JDK1.5推出的第一款真正意义上的并发（Concurrent）收集器； 第一次实现了让垃圾收集线程与用户线程（基本上）同时工作；

### 3.6.2 应用场景

- 与用户交互较多的场景；（如常见WEB、B/S-浏览器/服务器模式系统的服务器上的应用）
- 希望系统停顿时间最短，注重服务的响应速度； 以给用户带来较好的体验；

### 3.6.3 CMS收集器运作过程

从名字中的Mark Sweep这两个词可以看出，CMS收集器是一种 “标记-清除”算法实现的，它的运作过程相比于前面几种垃圾收集器来说更加复杂一些。整个过程可分为四个步骤：

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731145628763.png" alt="image-20210731145628763" style="zoom:50%;" />

- 初始标记： 暂停所有的其他线程，初始标记仅仅标记GC Roots能直接关联到的对象，速度很快；
- 并发标记 并发标记就是进行GC Roots Tracing的过程； 同时开启GC和用户线程，**用一个闭包结构去记录可达对象**。但在这个阶段结束，这个闭包结构并不能保证包含当前所有的可达对象。因为用户线程可能会不断的更新引用域，所以GC线程无法保证可达性分析的实时性。所以这个算法里会跟踪记录这些发生引用更新的地方；
- 重新标记： 重新标记阶段就是为了修正并发标记期间因为用户程序继续运行而导致标记产生变动的那一部分对象的标记记录（采用多线程并行执行来提升效率）；需要"Stop The World"，且停顿时间比初始标记稍长，但远比并发标记短；
- 并发清除： 开启用户线程，同时GC线程开始对为标记的区域做清扫，回收所有的垃圾对象； 由于整个过程耗时最长的并发标记和并发清除过程收集器线程都可以与用户线程一起工作。 所以总体来说，CMS的内存回收是与用户线程一起“并发”执行的。

### 3.6.4参数设置

指定使用CMS收集器 "-XX:+UseConcMarkSweepGC"

### 3.6.5 CMS收集器缺点

#### 3.6.5.1 对CPU资源敏感

面向并发设计的程序都对CPU资源比较敏感（并发程序的特点）。在并发阶段，它虽然不会导致用户线程停顿，但会因为占用了一部分线程（或者说CPU资源）而导致应用程序变慢，总吞吐量会降低。（在对账系统中，不适合使用CMS收集器）。 CMS的默认收集线程数量是=(CPU数量+3)/4； 当CPU数量越多，回收的线程占用CPU就少。 也就是当CPU在4个以上时，并发回收时垃圾收集线程不少于25%的CPU资源，对用户程序影响可能较大；不足4个时，影响更大，可能无法接受。（比如 CPU=2时，那么就启动一个线程回收，占了50%的CPU资源。） （一个回收线程会在回收期间一直占用CPU资源）

针对这种情况，曾出现了"增量式并发收集器"（Incremental Concurrent Mark Sweep/i-CMS）； 类似使用抢占式来模拟多任务机制的思想，让收集线程和用户线程交替运行，减少收集线程运行时间； 但效果并不理想，JDK1.6后就官方不再提倡用户使用。

#### 3.6.5.2 无法处理浮动垃圾

无法处理浮动垃圾,可能出现"Concurrent Mode Failure"失败 在并发清除时，用户线程新产生的垃圾，称为浮动垃圾；

**解决办法：** 这使得并发清除时需要预留一定的内存空间，不能像其他收集器在老年代几乎填满再进行收集； 也可以认为CMS所需要的空间比其他垃圾收集器大； 可以使用"-XX:CMSInitiatingOccupancyFraction"，设置CMS预留老年代内存空间； 

#### 3.6.5.3 产生大量内存碎片

由于CMS是基于“标记+清除”算法来回收老年代对象的，因此长时间运行后会产生大量的空间碎片问题，可能导致新生代对象晋升到老生代失败。 由于碎片过多，将会给大对象的分配带来麻烦。因此会出现这样的情况，**老年代还有很多剩余的空间，但是找不到连续的空间来分配当前对象，这样不得不提前触发一次Full GC**。

- 解决办法 使用"-XX:+UseCMSCompactAtFullCollection"和"-XX:+CMSFullGCsBeforeCompaction"，需要结合使用。
- UseCMSCompactAtFullCollection "-XX:+UseCMSCompactAtFullCollection"

为了解决空间碎片问题，CMS收集器提供−XX:+UseCMSCompactAlFullCollection标志，使得CMS出现上面这种情况时不进行Full GC，而开启内存碎片的合并整理过程； 但合并整理过程无法并发，停顿时间会变长； 默认开启（但不会进行，需要结合CMSFullGCsBeforeCompaction使用）；

- CMSFullGCsBeforeCompaction 由于合并整理是无法并发执行的，空间碎片问题没有了，但是有导致了连续的停顿。因此，可以使用另一个参数−XX:CMSFullGCsBeforeCompaction，表示在多少次不压缩的Full GC之后，对空间碎片进行压缩整理。 可以减少合并整理过程的停顿时间； 默认为0，也就是说每次都执行Full GC，不会进行压缩整理； 由于空间不再连续，CMS需要使用可用"空闲列表"内存分配方式，这比简单实用"碰撞指针"分配内存消耗大；

### 3.6.6 CMS&Parallel Old

总体来看，CMS与Parallel Old垃圾收集器相比，<font color=red>CMS减少了执行老年代垃圾收集时应用暂停的时间； 但却增加了新生代垃圾收集时应用暂停的时间、降低了吞吐量而且需要占用更大的堆空间</font>； （原因：CMS不进行内存空间整理节省了时间，但是可用空间不再是连续的了，垃圾收集也不能简单的使用指针指向下一次可用来为对象分配内存的地址了。

相反，这种情况下，需要使用可用空间列表。即，会创建一个指向未分配区域的列表，每次为对象分配内存时，会从列表中找到一个合适大小的内存区域来为新对象分配内存。这样做的结果是，<font color=red>老年代上的内存的分配比简单实用碰撞指针分配内存消耗大。</font>这也会增加年轻代垃圾收集的额外负担，因为老年代中的大部分对象是在新生代垃圾收集的时候从新生代提升为老年代的。） 当新生代对象无法分配过大对象，就会放到老年代进行分配。

## 3.7 G1收集器

上一代的垃圾收集器(串行serial, 并行parallel, 以及CMS)都把堆内存划分为固定大小的三个部分: 年轻代(young generation), 年老代(old generation), 以及持久代(permanent generation)。

G1（Garbage-First）是JDK7-u4才推出商用的收集器；
G1 (Garbage-First)是一款**面向服务器的垃圾收集器**，主要针对配备多颗处理器及大容量内存的机器。以极高概率满足GC停顿时间要求的同时，还具备高吞吐量性能特征。被视为JDK1.7中HotSpot虚拟机的一个重要进化特征。
**G1的使命是在未来替换CMS，并且在JDK1.9已经成为默认的收集器。**

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731151137749.png" alt="image-20210731151137749" style="zoom:33%;" />



### 3.7.1 特点

#### 3.7.1.1 并行与并发

G1能充分利用CPU、多核环境下的硬件优势，使用多个CPU（CPU或者CPU核心）来缩短stop-The-World停顿时间。部分其他收集器原本需要停顿Java线程执行的GC动作，G1收集器仍然可以通过并发的方式让java程序继续执行。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731154444389.png" alt="image-20210731154444389" style="zoom:50%;" />

#### 3.7.1.2 分代收集

虽然G1可以不需要其他收集器配合就能独立管理整个GC堆，但是还是保留了分代的概念。

- 能独立管理整个GC堆（新生代和老年代），而不需要与其他收集器搭配；
- 能够采用不同方式处理不同时期的对象；
- 虽然保留分代概念，但Java堆的内存布局有很大差别；
- 将整个堆划分为多个大小相等的独立区域（Region）；
- 新生代和老年代不再是物理隔离，它们都是一部分Region（不需要连续）的集合；

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731151108695.png" alt="image-20210731151108695" style="zoom:50%;" />

### 3.7.2 空间整合

与CMS的“标记--清理”算法不同，G1从整体来看是基于“标记整理”算法实现的收集器；从局部上来看是基于“复制”算法实现的。

- 从整体看，是基于标记-整理算法；
- 从局部（两个Region间）看，是基于复制算法； 
  这是一种类似火车算法的实现； 
  不会产生内存碎片，有利于长时间运行；

> （火车算法是分代收集器所用的算法，目的是在成熟对象空间中提供限定时间的渐进收集。在后面一篇中会专门介绍）

### 3.7.3 可预测的停顿

这是G1相对于CMS的另一个大优势，降低停顿时间是G1和CMS共同的关注点，但G1除了追求低停顿外，还能建立可预测的停顿时间模型。可以明确指定M毫秒时间片内，垃圾收集消耗的时间不超过N毫秒。在低停顿的同时实现高吞吐量。

### 3.7.4 G1收集器延伸

#### 3.7.4.1 为什么G1可以实现可预测停顿

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210731154741680.png" alt="image-20210731154741680" style="zoom:50%;" />

可以有计划地避免在Java堆的进行全区域的垃圾收集； G1收集器将内存分大小相等的独立区域（Region），新生代和老年代概念保留，但是已经不再物理隔离。 G1跟踪各个Region获得其收集价值大小，在后台维护一个优先列表； 每次根据允许的收集时间，优先回收价值最大的Region（名称Garbage-First的由来）； 这就保证了在有限的时间内可以获取尽可能高的收集效率；

#### 3.7.4.2 一个对象被不同区域引用的问题

一个Region不可能是孤立的，一个Region中的对象可能被其他任意Region中对象引用，判断对象存活时，是否需要扫描整个Java堆才能保证准确？ 在其他的分代收集器，也存在这样的问题（而G1更突出）：回收新生代也不得不同时扫描老年代？ 这样的话会降低Minor GC的效率；

**解决方法：**

无论G1还是其他分代收集器，JVM都是使用Remembered Set来避免全局扫描： 每个Region都有一个对应的Remembered Set； 每次Reference类型数据写操作时，都会产生一个Write Barrier暂时中断操作； 然后检查将要写入的引用指向的对象是否和该Reference类型数据在不同的Region（其他收集器：检查老年代对象是否引用了新生代对象）； 如果不同，通过CardTable把相关引用信息记录到引用指向对象的所在Region对应的Remembered Set中； 当进行垃圾收集时，在GC根节点的枚举范围加入Remembered Set； 就可以保证不进行全局扫描，也不会有遗漏。

### 3.7.5 应用场景

- **面向服务端应用，针对具有大内存、多处理器的机器；**
- 最主要的应用是为需要低GC延迟，并具有大堆的应用程序提供解决方案； 如：在堆大小约6GB或更大时，可预测的暂停时间可以低于0.5秒； （实践：对账系统中将CMS垃圾收集器修改为G1，降低对账时间20秒以上）

> 具体什么情况下应用G1垃圾收集器比CMS好，可以参考以下几点（但不是绝对）： 超过50％的Java堆被活动数据占用； 对象分配频率或年代的提升频率变化很大； GC停顿时间过长（长于0.5至1秒）； 建议： 如果现在采用的收集器没有出现问题，不用急着去选择G1； 如果应用程序追求低停顿，可以尝试选择G1； 是否代替CMS只有需要实际场景测试才知道。（如果使用G1后发现性能还没有使用CMS好，那么还是选择CMS比较好）

### 3.7.6 设置参数

可以通过下面的参数，来设置一些G1相关的配置。 指定使用G1收集器： "-XX:+UseG1GC"

当整个Java堆的占用率达到参数值时，开始并发标记阶段；默认为45： "-XX:InitiatingHeapOccupancyPercent"

为G1设置暂停时间目标，默认值为200毫秒： "-XX:MaxGCPauseMillis"

设置每个Region大小，范围1MB到32MB；目标是在最小Java堆时可以拥有约2048个Region: "-XX:G1HeapRegionSize"

新生代最小值，默认值5%: "-XX:G1NewSizePercent"

新生代最大值，默认值60%: "-XX:G1MaxNewSizePercent"

设置STW期间，并行GC线程数: "-XX:ParallelGCThreads"

设置并发标记阶段，并行执行的线程数: "-XX:ConcGCThreads"

G1在标记过程中，每个区域的对象活性都被计算，在回收时候，就可以根据用户设置的停顿时间，选择活性较低的区域收集，这样既能保证垃圾回收，又能保证停顿时间，而且也不会降低太多的吞吐量。Remark（重新标记）阶段新算法的运用，以及收集过程中的压缩，都弥补了CMS不足。 引用Oracle官网的一句话：“G1 is planned as the long term replacement for the Concurrent Mark-Sweep Collector (CMS)”。 G1计划作为并发标记-清除收集器(CMS)的长期替代品

# 4、如何选择垃圾收集器

垃圾收集器主要可以分为如下三大类：

- **串行收集器**：Serial和Serial Old
  只能有一个垃圾回收线程执行，用户线程暂停。 适用于内存比较小的嵌入式设备 。
- **并行收集器**[**吞吐量优先**]：Parallel Scanvenge和Parallel Old
  多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。 适用于科学计算、后台处理等若交互场景 。
- **并发收集器**[**停顿时间优先**]：CMS和G1。
  用户线程和垃圾收集线程同时执行(但并不一定是并行的，可能是交替执行的)，垃圾收集线程在执行的时候不会停顿用户线程的运行。 适用于对时间有要求的场景，比如Web应用。

# 参考资料

https://zhuanlan.zhihu.com/p/142273073

https://juejin.cn/post/6844903877024677901

[面试官：你对JVM垃圾收集器了解吗？13连问你是否抗的住！]( https://juejin.cn/post/6844904159817236494)

https://juejin.cn/post/6874060477031579661#heading-32

https://juejin.cn/post/6844904041080684552

https://juejin.cn/post/6844904159817236494#heading-14

https://juejin.cn/post/6844903892774289421#heading-20

