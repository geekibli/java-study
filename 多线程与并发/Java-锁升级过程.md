---
title: Java-锁升级过程
toc: true
date: 2021-07-28 17:56:39
tags: 多线程
categories: [Develop Lan,Java,多线程与并发]
---
# 锁升级

## 使用JOL查看加锁之后的对象信息
```java
public static void main(String[] args) {
        Object object = new Object();
        System.out.println(ClassLayout.parseInstance(object).toPrintable());
        
        synchronized (object){
            System.out.println(ClassLayout.parseInstance(object).toPrintable());
        }
    }
```
查看打印结果：
```java
java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           e5 01 00 f8 (11100101 00000001 00000000 11111000) (-134217243)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           f0 b8 d0 0f (11110000 10111000 11010000 00001111) (265337072)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           e5 01 00 f8 (11100101 00000001 00000000 11111000) (-134217243)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```


## 锁升级过程
> 锁升级过程： new - 偏向锁 - 轻量级锁 （无锁, 自旋锁，自适应自旋）- 重量级锁

<image src="https://oscimg.oschina.net/oscnet/up-2f555289640d34755ca4288ab080906554b.png" width=750 height=450> 

**自旋锁什么时候升级为重量级锁？**

超过自选次数仍没有获得锁

**为什么有自旋锁还需要重量级锁？**

> 自旋是消耗CPU资源的，如果锁的时间长，或者自旋线程多，CPU会被大量消耗
> 重量级锁有等待队列，所有拿不到锁的进入等待队列，不需要消耗CPU资源

**偏向锁是否一定比自旋锁效率高？**

> 不一定，在明确知道会有多线程竞争的情况下，偏向锁肯定会涉及锁撤销，这时候直接使用自旋锁
> JVM启动过程，会有很多线程竞争（明确），所以默认情况启动时不打开偏向锁，过一段儿时间再打开


### synchronized优化的过程和markword息息相关

用markword中最低的三位代表锁状态 其中1位是偏向锁位 两位是普通锁位

1. Object o = new Object()
   锁 = 0 01 无锁态 
   注意：如果偏向锁打开，默认是匿名偏向状态

2. o.hashCode()
   001 + hashcode

   ```java
   00000001 10101101 00110100 00110110
   01011001 00000000 00000000 00000000
   ```

   little endian big endian 

   00000000 00000000 00000000 01011001 00110110 00110100 10101101 00000000

3. 默认synchronized(o) 
   00 -> 轻量级锁
   默认情况 偏向锁有个时延，默认是4秒
   why? 因为JVM虚拟机自己有一些默认启动的线程，里面有好多sync代码，这些sync代码启动时就知道肯定会有竞争，如果使用偏向锁，就会造成偏向锁不断的进行锁撤销和锁升级的操作，效率较低。

   ```shell
   -XX:BiasedLockingStartupDelay=0
   ```

4. 如果设定上述参数
   new Object () - > 101 偏向锁 ->线程ID为0 -> Anonymous BiasedLock 
   打开偏向锁，new出来的对象，默认就是一个可偏向匿名对象101

5. 如果有线程上锁
   上偏向锁，指的就是，把markword的线程ID改为自己线程ID的过程
   偏向锁不可重偏向 批量偏向 批量撤销

6. 如果有线程竞争
   撤销偏向锁，升级轻量级锁
   线程在自己的线程栈生成LockRecord ，用CAS操作将markword设置为指向自己这个线程的LR的指针，设置成功者得到锁

7. 如果竞争加剧
   竞争加剧：有线程超过10次自旋， -XX:PreBlockSpin， 或者自旋线程数超过CPU核数的一半， 1.6之后，加入自适应自旋 Adapative Self Spinning ， JVM自己控制
   升级重量级锁：-> 向操作系统申请资源，linux mutex , CPU从3级-0级系统调用，线程挂起，进入等待队列，等待操作系统的调度，然后再映射回用户空间

(以上实验环境是JDK11，打开就是偏向锁，而JDK8默认对象头是无锁)
偏向锁默认是打开的，但是有一个时延，如果要观察到偏向锁，应该设定参数

**如果计算过对象的hashCode，则对象无法进入偏向状态！**

> **轻量级锁重量级锁的hashCode存在与什么地方？**
> 答案：线程栈中，轻量级锁的LR中，或是代表重量级锁的ObjectMonitor的成员中

关于epoch: (不重要)

**批量重偏向与批量撤销**渊源：从偏向锁的加锁解锁过程中可看出，当只有一个线程反复进入同步块时，偏向锁带来的性能开销基本可以忽略，但是当有其他线程尝试获得锁时，就需要等到safe point时，再将偏向锁撤销为无锁状态或升级为轻量级，会消耗一定的性能，所以在多线程竞争频繁的情况下，偏向锁不仅不能提高性能，还会导致性能下降。于是，就有了批量重偏向与批量撤销的机制。

**原理**以class为单位，为每个class维护**解决场景**批量重偏向（bulk rebias）机制是为了解决：一个线程创建了大量对象并执行了初始的同步操作，后来另一个线程也来将这些对象作为锁对象进行操作，这样会导致大量的偏向锁撤销操作。批量撤销（bulk revoke）机制是为了解决：在明显多线程竞争剧烈的场景下使用偏向锁是不合适的。

一个偏向锁撤销计数器，每一次该class的对象发生偏向撤销操作时，该计数器+1，当这个值达到重偏向阈值（默认20）时，JVM就认为该class的偏向锁有问题，因此会进行批量重偏向。每个class对象会有一个对应的epoch字段，每个处于偏向锁状态对象的Mark Word中也有该字段，其初始值为创建该对象时class中的epoch的值。每次发生批量重偏向时，就将该值+1，同时遍历JVM中所有线程的栈，找到该class所有正处于加锁状态的偏向锁，将其epoch字段改为新值。下次获得锁时，发现当前对象的epoch值和class的epoch不相等，那就算当前已经偏向了其他线程，也不会执行撤销操作，而是直接通过CAS操作将其Mark Word的Thread Id 改成当前线程Id。当达到重偏向阈值后，假设该class计数器继续增长，当其达到批量撤销的阈值后（默认40），JVM就认为该class的使用场景存在多线程竞争，会标记该class为不可偏向，之后，对于该class的锁，直接走轻量级锁的逻辑。

没错，我就是厕所所长

加锁，指的是锁定对象

## 锁升级的过程

> JDK较早的版本 OS的资源 互斥量 用户态 -> 内核态的转换 重量级 效率比较低

现代版本进行了优化

> 无锁 - 偏向锁 -轻量级锁（自旋锁）-重量级锁

1、偏向锁 - markword 上记录当前线程指针，下次同一个线程加锁的时候，不需要争用，只需要判断线程指针是否同一个，所以，偏向锁，偏向加锁的第一个线程 。hashCode备份在线程栈上 线程销毁，锁降级为无锁

2、有争用 - 锁升级为轻量级锁 - 每个线程有自己的LockRecord在自己的线程栈上，用CAS去争用markword的LR的指针，指针指向哪个线程的LR，哪个线程就拥有锁

3、自旋超过10次，升级为重量级锁 - 如果太多线程自旋 CPU消耗过大，不如升级为重量级锁，进入等待队列（不消耗CPU）-XX:PreBlockSpin

自旋锁在 JDK1.4.2 中引入，使用 -XX:+UseSpinning 来开启。JDK 6 中变为默认开启，并且引入了自适应的自旋锁（适应性自旋锁）。

4、自适应自旋锁意味着自旋的时间（次数）不再固定，而是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。如果在同一个锁对象上，自旋等待刚刚成功获得过锁，并且持有锁的线程正在运行中，那么虚拟机就会认为这次自旋也是很有可能再次成功，进而它将允许自旋等待持续相对更长的时间。如果对于某个锁，自旋很少成功获得过，那在以后尝试获取这个锁时将可能省略掉自旋过程，直接阻塞线程，避免浪费处理器资源。

5、偏向锁由于有锁撤销的过程revoke，会消耗系统资源，所以，在锁争用特别激烈的时候，用偏向锁未必效率高。还不如直接使用轻量级锁。
