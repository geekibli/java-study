---
title: Java并发编程之Condition机制底层
toc: true
date: 2021-08-03 12:00:16
tags: 多线程
categories:
---



# Lock框架中的Condition机制

还是看一下之前ReentrantLock中调用condition方法的流程图 👇

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210803140956785.png" alt="image-20210803140956785" style="zoom:40%;" />



任何一个java对象都天然继承于Object类，在线程间实现通信的往往会应用到Object的几个方法，比如wait(),wait(long timeout),wait(long timeout, int nanos)与notify(),notifyAll()几个方法实现等待/通知机制，同样的， 在java Lock体系下依然会有同样的方法实现等待/通知机制。

从整体上来看**Object的wait和notify/notify是与对象监视器配合完成线程间的等待/通知机制，而Condition与Lock配合完成等待通知机制，前者是java底层级别的，后者是语言级别的，具有更高的可控制性和扩展性**。两者除了在使用方式上不同外，在**功能特性**上还是有很多的不同：

1. Condition能够支持不响应中断，而通过使用Object方式不支持；
2. Condition能够支持多个等待队列（new 多个Condition对象），而Object方式只能支持一个；
3. Condition能够支持超时时间的设置，而Object不支持

## 1. Condition接口提供的方法

### 1.1 await方法

**void await() throws InterruptedException**

当前线程进入等待状态，如果其他线程调用condition的signal或者signalAll方法并且当前线程获取Lock从await方法返回，如果在等待状态中被中断会抛出被中断异常；

**long awaitNanos(long nanosTimeout)**

当前线程进入等待状态直到被通知，中断或者**超时**；

**boolean await(long time, TimeUnit unit)throws InterruptedException**

同第二种，支持自定义时间单位

**boolean awaitUntil(Date deadline) throws InterruptedException**

当前线程进入等待状态直到被通知，中断或者**到了某个时间**

### 1.2 signal方法

**void signal()**

唤醒一个等待在condition上的线程，将该线程从**等待队列**中转移到**同步队列**中，如果在同步队列中能够竞争到Lock则可以从等待方法中返回。

**void signalAll()**

与1的区别在于能够唤醒所有等待在condition上的线程。



## 2. Condition在ReentrantLock中的使用

下面先通过一个例子看一下Condition的使用 👇

1、大致流程就是线程1先获取lock之后，执行线程1的方法，然后调用condition.await();方法阻塞当前线程；同时加入Condition等待队列

2、线程1释放lock之后，线程2而已经在同步队列中了，线程2获取lock执行权，执行condition.signal()方法唤醒线程1

3、线程1被唤醒之后，node节点重新添加到同步队列中，等待获取执行权限，在线程2调用了unlock()方法之后，线程1重新获取到lock之后，执行后续流程。

```java
public class ReentrantLockDemo {

    static Lock lock = new ReentrantLock();
    public static void main(String[] args) {
        Condition condition = lock.newCondition();

        new Thread(()->{
            System.err.println("enter thread 1 ");
            lock.lock();
            try {
                try {
                    System.err.println("thread 1 invoke await");
                    condition.await();
                    System.err.println("thread 1 invoked signal");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.err.println("exit thread 1 ");
            }finally {
                lock.unlock();
            }
        }).start();

        new Thread(()->{
            System.err.println("enter thread 2 ");
            lock.lock();
            try {
                System.err.println("thread 2 invoke signal");
                condition.signal();
                System.err.println("exit thread 2 ");
            }finally {
                lock.unlock();
            }
        }).start();
    }
}
```

上面代码的执行结果可以猜想一下

```java
enter thread 1 
thread 1 invoke await
enter thread 2 
thread 2 invoke signal
exit thread 2 
thread 1 invoked signal
exit thread 1 
```



## 3. Condition等待/通知实现原理

要想能够深入的掌握condition还是应该知道它的实现原理，现在我们一起来看看condiiton的源码。创建一个condition对象是通过`lock.newCondition()`,而这个方法实际上是会new出一个**ConditionObject**对象，该类是AQS的一个内部类，和Node类一样，非常重要。

condition是要和lock配合使用的也就是condition和Lock是绑定在一起的，而lock的实现原理又依赖于AQS，自然而然ConditionObject作为AQS的一个内部类无可厚非。

我们知道在锁机制的实现上，AQS内部维护了一个同步队列，如果是独占式锁的话，所有获取锁失败的线程的尾插入到**同步队列**，同样的，condition内部也是使用同样的方式，内部维护了一个 **等待队列**，所有调用condition.await方法的线程会加入到等待队列中，并且线程状态转换为等待状态。

另外注意到ConditionObject中有两个成员变量：

`private transient Node firstWaiter;`

`private transient Node lastWaiter;`

在AQS中condition队列可以存在多个如下所示，但是同步队列之可能是一个，值得注意的是，同步队列是一个双向链表队列，而等待队列是一个单向的队列。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210803142409623.png" alt="image-20210803142409623" style="zoom:33%;" />



下面从await方法入手来学习Condition的机制是如何运转的。

### 3.1 等待await

`public class ConditionObject implements Condition`

AQS#ConditionObject内部类实现了Condition接口的await方法：

```java
public final void await() throws InterruptedException {
  	// 判断线程是否中断
    if (Thread.interrupted())
        throw new InterruptedException();
  	// 将节点添加到等待队列
    Node node = addConditionWaiter();
  	// 进入等待队列中的线程需要释放lock让给别的线程
    int savedState = fullyRelease(node);
    int interruptMode = 0;
  	// 如果节点不在同步队列，则挂起当前线程，知道进入同步队列或者被中断
    while (!isOnSyncQueue(node)) {
        LockSupport.park(this);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
  	// 调用await的线程会一直阻塞在上面的while循环，知道被唤醒或者相应中断，才会执行下面的方法
  	// 进入同步队列尝试获取lock，和之前一样，为了限制一直空转，会在第二次循环之后，park此节点，知道队列中轮到这个线程出队
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null) // clean up if cancelled
      	// 清除掉取消的节点，踢出等待队列
        unlinkCancelledWaiters();
  			//处理被中断的情况
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}
```

**AQS#addConditionWaiter** **添加节点到等待队列**

```java
private Node addConditionWaiter() {
    Node t = lastWaiter;
    // If lastWaiter is cancelled, clean out.
    if (t != null && t.waitStatus != Node.CONDITION) {
        unlinkCancelledWaiters();
        t = lastWaiter;
    }
    Node node = new Node(Thread.currentThread(), Node.CONDITION);
    if (t == null)
        firstWaiter = node;
    else
        t.nextWaiter = node;
    lastWaiter = node;
    return node;
}
```

这个方法应该比较好理解吧，就是添加一个节点，到等待队列。

⚠️ 这里和把节点添加到同步队列还有点区别，不知道大家还有没有印象，在同步队列添加节点的时候，先判断tail是否为空，如果不是空，则直接添加；如果是空，则调用了`enq(Node node)`方法，先生成一个head节点，然后在把当前节点添加到后面，循环了两遍的。

这里是直接创建当前节点，然后将firstWaiter指针指向了node；

**AQS#fullyRelease 释放lock**

```java
final int fullyRelease(Node node) {
    boolean failed = true;
    try {
        int savedState = getState();
        if (release(savedState)) {
            failed = false;
            return savedState;
        } else {
            throw new IllegalMonitorStateException();
        }
    } finally {
        if (failed)
            node.waitStatus = Node.CANCELLED;
    }
}
```

这个方法也不难，想一下，线程都已经调用await方法了，而且上一步就已经把节点添加到了等待队列中了，那么接下来要做什么呢？那肯定是释放锁lock了。对，这个方法就是做这个的。release方法之前已经介绍了，无非就是对state做一下减法，把对战线程清空一下，给新来的线程腾地方。

**下面才是await的关键核心代码**：‼️

```
while (!isOnSyncQueue(node)) {
        LockSupport.park(this);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
```

`isOnSyncQueue(node)`判断当前节点是否在同步队列中，为什么要这个判断呢？原因很简单，当别的线程或者自己调用了signal方法之后，会把当前节点转移到同步队列中，在同步队列中说明什么呢，说明接下来这个线程要去竞争锁了，也就是被唤醒了，当竞争锁成功之后，这个线程就可以await后面的方法了。

`(interruptMode = checkInterruptWhileWaiting(node)) != 0`

如果当前线程被中断，则可以直接跳出循环，去竞争锁。

### 3.2 通知signal

**调用condition的signal或者signalAll方法可以将等待队列中等待时间最长的节点移动到同步队列中**，使得该节点能够有机会获得lock。按照等待队列是先进先出（FIFO）的，所以等待队列的头节点必然会是等待时间最长的节点，也就是每次调用condition的signal方法是将头节点移动到同步队列中。signal方法源码为：

```java
public final void signal() {
    //1. 先检测当前线程是否已经获取lock，如果没有获得锁，肯定是说不通的
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
    //2. 获取等待队列中第一个节点，之后的操作都是针对这个节点
	Node first = firstWaiter;
    if (first != null)
        doSignal(first);
}
```

signal方法首先会检测当前线程是否已经获取lock，如果没有获取lock会直接抛出异常，如果获取的话再得到等待队列的头指针引用的节点，之后的操作的doSignal方法也是基于该节点。下面我们来看看doSignal方法做了些什么事情。

**AQS#doSignal**

```java
private void doSignal(Node first) {
    do {
        if ( (firstWaiter = first.nextWaiter) == null)
            lastWaiter = null;
		//1. 将头结点从等待队列中移除
        first.nextWaiter = null;
		//2. while中transferForSignal方法对头结点做真正的处理
    } while (!transferForSignal(first) &&
             (first = firstWaiter) != null);
}
```

具体逻辑请看注释，真正对头节点做处理的逻辑在**transferForSignal**放，该方法源码为：

```java
final boolean transferForSignal(Node node) {
    /*
     * If cannot change waitStatus, the node has been cancelled.
     */
	  //1. 更新状态为0，加入同步队列的节点的初始状态是0
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
        return false;

    /*
     * Splice onto queue and try to set waitStatus of predecessor to
     * indicate that thread is (probably) waiting. If cancelled or
     * attempt to set waitStatus fails, wake up to resync (in which
     * case the waitStatus can be transiently and harmlessly wrong).
     */
	//2.将该节点移入到同步队列中去
    Node p = enq(node);
    int ws = p.waitStatus;
  	// p节点是node的前置节点，需要将前驱节点的状态设置成Node.SIGNAL
    if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
        LockSupport.unpark(node.thread);
    return true;
}
```

关键逻辑请看注释，这段代码主要做了两件事情

1.将头结点的状态更改为CONDITION；

2.调用enq方法，将该节点尾插入到同步队列中，并且把前驱节点的状态设置成Node.SIGNAL

现在我们可以得出结论：**调用condition的signal的前提条件是当前线程已经获取了lock，该方法会使得等待队列中的头节点即等待时间最长的那个节点移入到同步队列，而移入到同步队列后才有机会使得等待线程被唤醒，即从await方法中的LockSupport.park(this)方法中返回，从而才有机会使得调用await方法的线程成功退出**。




**signalAll方法通知所有等待线程**

sigllAll与sigal方法的区别体现在doSignalAll方法上，前面我们已经知道doSignal方法只会对等待队列的头节点进行操作，而doSignalAll的源码为：

```java
private void doSignalAll(Node first) {
    lastWaiter = firstWaiter = null;
    do {
        Node next = first.nextWaiter;
        first.nextWaiter = null;
        transferForSignal(first);
        first = next;
    } while (first != null);
}
```

该方法只不过时间等待队列中的每一个节点都移入到同步队列中，即“通知”当前调用condition.await()方法的每一个线程。

## 面试题 两个线程交替顺序打印1～100 

```java
package com.ibli.note;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WaitNotifyDemo implements Runnable {
    int count = 1;
    private Condition condition;
    private Lock lock;

    public WaitNotifyDemo(Condition condition, Lock lock) {
        this.condition = condition;
        this.lock = lock;
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                condition.signal();
                if (count > 100) {
                    break;
                }
                System.err.println(Thread.currentThread().getName() + " => " + count);
                count++;
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        WaitNotifyDemo waitNotifyDemo = new WaitNotifyDemo(condition, lock);
        new Thread(waitNotifyDemo).start();
        new Thread(waitNotifyDemo).start();
    }
}
```



## 参考资料
https://juejin.cn/post/6844903602419400718

https://juejin.cn/post/6844903654873382925
