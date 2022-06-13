---
title: Java并发编程-深入理解volatile
toc: true
date: 2021-07-29 16:25:05
tags: 多线程
categories:
---

# volatile特性

正确理解volatile

> 多级cache结构 -> 缓存一致性协议（MESI）-> store buffer和invalidate queue -> 内存屏障

## 可见性

volatile的可见性依赖于Java内存模型。 可以参见之前的文章  👉 [Java内存模型](https://geekibli.github.io/wiki/Java%E5%86%85%E5%AD%98%E6%A8%A1%E5%9E%8B/)

`Java内存模型(JavaMemoryModel)`描述了Java程序中各种变量(线程共享变量)的访问规则，以及在JVM中将变量，存储到内存和从内存中读取变量这样的底层细节。

所有的共享变量都存储于主内存，这里所说的变量指的是实例变量和类变量，不包含局部变量，因为局部变量是线程私有的，因此不存在竞争问题。

每一个线程还存在自己的工作内存，线程的工作内存，保留了被线程使用的变量的工作副本。

`线程对变量的所有的操作(读，取)都必须在工作内存中完成，而不能直接读写主内存中的变量`。

不同线程之间也不能直接访问对方工作内存中的变量，线程间变量的值的传递需要通过主内存中转来完成。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210729170756502.png" alt="image-20210729170756502" style="zoom:35%;" />

volatile实现可见性

每个线程操作数据的时候会把数据从主内存读取到自己的工作内存，如果他操作了数据并且写会了，他其他已经读取的线程的变量副本就会失效了，需要都数据进行操作又要再次去主内存中读取了。

volatile保证不同线程对共享变量操作的可见性，也就是说一个线程修改了volatile修饰的变量，当修改写回主内存时，另外一个线程立即看到最新的值。

至于其他线程是如何更新缓存行中的数据以及其他线程的缓存行是如何失效的，可以参见之前的文章。 [Java内存模型](https://geekibli.github.io/wiki/Java%E5%86%85%E5%AD%98%E6%A8%A1%E5%9E%8B/)

###  嗅探机制

在现代计算机中，CPU 的速度是极高的，如果 CPU 需要存取数据时都直接与内存打交道，在存取过程中，CPU 将一直空闲，这是一种极大的浪费，所以，为了提高处理速度，CPU 不直接和内存进行通信，而是在 CPU 与内存之间加入很多寄存器，多级缓存，它们比内存的存取速度高得多，这样就解决了 CPU 运算速度和内存读取速度不一致问题。

由于 CPU 与内存之间加入了缓存，在进行数据操作时，先将数据从内存拷贝到缓存中，CPU 直接操作的是缓存中的数据。但在多处理器下，将可能导致各自的缓存数据不一致（这也是可见性问题的由来），为了保证各个处理器的缓存是一致的，就会实现缓存一致性协议，而**嗅探是实现缓存一致性的常见机制**。

**嗅探机制工作原理**：每个处理器通过监听在总线上传播的数据来检查自己的缓存值是不是过期了，如果处理器发现自己缓存行对应的内存地址修改，就会将当前处理器的缓存行设置无效状态，当处理器对这个数据进行修改操作的时候，会重新从主内存中把数据读到处理器缓存中。

注意：

> 基于 CPU 缓存一致性协议，JVM 实现了 volatile 的可见性，但由于总线嗅探机制，会不断的监听总线，如果大量使用 volatile 会引起总线风暴。所以，volatile 的使用要适合具体场景。

## 重排序

**什么是指令重排序**?

为了提高性能，编译器和处理器常常会对既定的代码执行顺序进行指令重排序。

【源代码】 -> 【编译器优化重排序】-> 【指令集并行重排序】-> 【内存系统重排序】-> 【最终执行指令序列】

一般重排序可以分为如下三种：

- 编译器优化的重排序。编译器在不改变单线程程序语义的前提下，可以重新安排语句的执行顺序;
- 指令级并行的重排序。现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性，处理器可以改变语句对应机器指令的执行顺序;
- 内存系统的重排序。由于处理器使用缓存和读/写缓冲区，这使得加载和存储操作看上去可能是在乱序执行的。

### as-if-serial语义

不管怎么重排序，单线程下的执行结果不能被改变。编译器、runtime和处理器都必须遵守as-if-serial语义。

java编译器会在生成指令系列时在适当的位置会插入`内存屏障`指令来禁止特定类型的处理器重排序。

为了实现volatile的内存语义，JMM会限制特定类型的编译器和处理器重排序，JMM会针对编译器制定volatile重排序规则表：

|  **内存屏障**   | **说明**                                                    |
| :-------------: | :---------------------------------------------------------- |
| StoreStore 屏障 | 禁止上面的普通写和下面的 volatile 写重排序。                |
| StoreLoad 屏障  | 防止上面的 volatile 写与下面可能有的 volatile 读/写重排序。 |
|  LoadLoad 屏障  | 禁止下面所有的普通读操作和上面的 volatile 读重排序。        |
| LoadStore 屏障  | 禁止下面所有的普通写操作和上面的 volatile 读重排序。        |

### happens-before规则

如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须存在happens-before关系。

**volatile域规则：对一个volatile域的写操作，happens-before于任意线程后续对这个volatile域的读。**

### volatile在DCL的应用

```java
public class Singleton {
    public static volatile Singleton singleton;
    /**
     * 构造函数私有，禁止外部实例化
     */
    private Singleton() {};
    public static Singleton getInstance() {
        if (singleton == null) {
            synchronized (singleton.class) {
                if (singleton == null) {
                    singleton = new Singleton();
                }
            }
        }
        return singleton;
    }
}
```

现在我们分析一下为什么要在变量singleton之间加上volatile关键字。要理解这个问题，先要了解对象的构造过程，实例化一个对象其实可以分为三个步骤：

- 分配内存空间。
- 初始化对象。
- 将内存空间的地址赋值给对应的引用。

但是由于操作系统可以`对指令进行重排序`，所以上面的过程也可能会变成如下过程：

- 分配内存空间。
- 将内存空间的地址赋值给对应的引用。
- 初始化对象

如果是这个流程，多线程环境下就可能将一个未初始化的对象引用暴露出来，从而导致不可预料的结果。因此，为了防止这个过程的重排序，我们需要将变量设置为volatile类型的变量。

### 一次性安全发布

```java
public class BackgroundFloobleLoader {
    public volatile Flooble theFlooble;

    public void initInBackground() {
        // do lots of stuff
        theFlooble = new Flooble();  // this is the only write to theFlooble
    }
}

public class SomeOtherClass {
    public void doWork() {
        while (true) { 
            // do some stuff...
            // use the Flooble, but only if it is ready
            if (floobleLoader.theFlooble != null) 
                doSomething(floobleLoader.theFlooble);
        }
    }
}
```

如果 `theFlooble` 引用不是 volatile 类型，`doWork()` 中的代码在解除对 `theFlooble` 的引用时，将会得到一个不完全构造的 `Flooble`。

该模式的一个必要条件是：被发布的对象必须是线程安全的，或者是有效的不可变对象（有效不可变意味着对象的状态在发布之后永远不会被修改）。volatile 类型的引用可以确保对象的发布形式的可见性，但是如果对象的状态在发布后将发生更改，那么就需要额外的同步。

## 无法保证原子性

所谓的原子性是指在一次操作或者多次操作中，要么所有的操作全部都得到了执行并且不会受到任何因素的干扰而中断，要么所有的操作都不执行。

在多线程环境下，volatile 关键字可以保证共享数据的可见性，但是并不能保证对数据操作的原子性。也就是说，多线程环境下，使用 volatile 修饰的变量是**线程不安全的**。

要解决这个问题，我们可以使用锁机制，或者使用原子类（如 AtomicInteger）。

这里特别说一下，对任意单个使用 volatile 修饰的变量的读 / 写是具有原子性，但类似于 `flag = !flag` 这种复合操作不具有原子性。简单地说就是，**单纯的赋值操作是原子性的**。

# volatile 和 synchronized

volatile只能修饰实例变量和类变量，而synchronized可以修饰方法，以及代码块。

volatile保证数据的可见性，但是不保证原子性(多线程进行写操作，不保证线程安全);而synchronized是一种排他(互斥)的机制。

volatile用于禁止指令重排序：可以解决单例双重检查对象初始化代码执行乱序问题。

volatile可以看做是轻量版的synchronized，volatile不保证原子性，但是如果是对一个共享变量进行多个线程的赋值，而没有其他的操作，那么就可以用volatile来代替synchronized，因为赋值本身是有原子性的，而volatile又保证了可见性，所以就可以保证线程安全了。

# 总结

1、volatile修饰符适用于以下场景：某个属性被多个线程共享，其中有一个线程修改了此属性，其他线程可以立即得到修改后的值，比如booleanflag;或者作为触发器，实现轻量级同步。

2、volatile属性的读写操作都是无锁的，它不能替代synchronized，因为它没有提供**原子性和互斥性**。因为无锁，不需要花费时间在获取锁和释放锁_上，所以说它是低成本的。

3、volatile只能作用于属性，我们用volatile修饰属性，这样compilers就不会对这个属性做指令重排序。

4、volatile提供了可见性，任何一个线程对其的修改将立马对其他线程可见，volatile属性不会被线程缓存，始终从主 存中读取。

5、volatile提供了happens-before保证，对volatile变量v的写入happens-before所有其他线程后续对v的读操作。

6、volatile可以使得long和double的赋值是原子的。

7、volatile可以在单例双重检查中实现可见性和禁止指令重排序，从而保证安全性。



---

**参考资料**

[面试官想到，一个Volatile，敖丙都能吹半小时](https://mp.weixin.qq.com/s/Oa3tcfAFO9IgsbE22C5TEg)

[面试官最爱的volatile关键字](https://juejin.cn/post/6844903520760496141)

[一文吃透Volatile，征服面试官](https://juejin.cn/post/6844903959107207175)

[java语言的线程安全volatile用法](https://www.cnblogs.com/lidl/archive/2012/06/25/2561431.html)

[线程安全(上)--彻底搞懂volatile关键字](https://www.cnblogs.com/kubidemanong/p/9505944.html)

