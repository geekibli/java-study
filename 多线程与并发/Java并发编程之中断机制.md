---
title: Java并发编程之中断机制
toc: true
date: 2021-08-03 15:56:37
tags: 多线程
categories:
---

# 中断机制

Java语言提供一种机制来试图“终止”一些特殊的线程，比如一下空转的线程一直消耗系统资源，可以使用中断的方式来停止这一类的线程，这就是Java中断机制。

## 1、中断注意的地方

1、**Java中线程间是协作式，而非抢占式**. 调用一个线程的interrupt() 方法中断一个线程，并不是强行关闭这个线程，只是跟这个线程打个招呼，将线程的中断标志位置为true，线程是否中断，由线程本身决定。

2、isInterrupted() 判定当前线程是否处于中断状态。

3、静态方法 interrupted() 判定当前线程是否处于中断状态，同时中断标志位改为 false。

4、**如果方法里如果抛出中断异常 InterruptedException，则线程的中断标志位会被复位成false**，如果确实是需要中断线程，要求我们自己在catch语句块里再次调用interrupt()。

5、Java 中所有的阻塞方法都会抛出 InterruptedException，比如wait(), join(),sleep()。



## 2、Java中断提供的方法

在Java中提供了3个有关中断的方法：

### Thread.currentThread().isInterrupted()

> 判断当前的线程是否被中断

### thread.interrupt();

> 中断一个线程，将中断标志设置成true

```java
public void interrupt() {
        if (this != Thread.currentThread())
            checkAccess();

        synchronized (blockerLock) {
            Interruptible b = blocker;
            if (b != null) {
                interrupt0();           // Just to set the interrupt flag
                b.interrupt(this);
                return;
            }
        }
        interrupt0();
}
```

### Thread.interrupted()

```java
public static boolean interrupted() {
    return currentThread().isInterrupted(true);
}
```

> 判断线程是否被中断，并清除中断标志，改成false；

验证一下就可以了 👇

```java
public static void main(String[] args) {
    System.err.println(Thread.currentThread().isInterrupted());
    Thread.currentThread().interrupt();
    System.err.println(Thread.currentThread().isInterrupted());
    boolean interrupted = Thread.interrupted();
    System.err.println("interrupted " + interrupted);
    System.err.println(Thread.currentThread().isInterrupted());
}
```



## 3、中断例子

```java
public class InterrupterDemo {

    public static void main(String[] args) throws InterruptedException {

        Thread thread = new Thread(() -> {
            while (true && !Thread.currentThread().isInterrupted()){
                System.err.println(1);
                System.err.println(Thread.interrupted());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("after sleep "+ Thread.currentThread().isInterrupted());
                  Thread.currentThread().interrupt();
                    boolean interrupted = Thread.interrupted();
                    System.err.println("interrupted "+ Thread.currentThread().isInterrupted() + "/ " + interrupted);
                    Thread.currentThread().interrupt();
                    System.err.println("final sleep "+ Thread.currentThread().isInterrupted());
                    break;
                }
                System.err.println(2);
            }
        });

        thread.start();

        Thread.sleep(3000);
        thread.interrupt();
    }
}
```



注意，中断一场不要【 吞掉 】，要不在程序中相应中断一场，进行相应的逻辑处理，或者将一场继续向上抛，由上层处理。



## 参考资料

https://dayarch.top/p/java-concurrency-interrupt-mechnism.html



