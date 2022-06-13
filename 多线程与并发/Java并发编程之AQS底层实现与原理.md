---
title: Java并发编程之AQS底层实现与原理
toc: true
date: 2021-08-02 15:17:07
tags: 多线程
categories:
---



AQS锁限时等待是如何实现的？

公平锁与非公平锁流程是怎样的？



# 独占锁&共享锁

## 独占锁

即只允许一个线程获取同步状态，当这个线程还没有释放同步状态时，其他线程是获取不了的，只能加入到同步队列，进行等待。

#  公平锁&非公平锁

## 公平锁

**公平策略：**在多个线程争用锁的情况下，公平策略倾向于将访问权授予等待时间最长的线程。也就是说，相当于有一个线程等待队列，先进入等待队列的线程后续会先获得锁，这样按照“先来后到”的原则，对于每一个等待线程都是公平的。

## 非公平锁

在多个线程争用锁的情况下，能够最终获得锁的线程是随机的（由底层OS调度）。

> *注意：一般情况下，使用公平策略的程序在多线程访问时，总体吞吐量（即速度很慢，常常极其慢）比较低，因为此时在线程调度上面的开销比较大。*

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210802175827435.png" alt="image-20210802175827435" style="zoom:50%;" />

# AQS是什么

同步器是用来构建锁和其他同步组件的基础框架，它的实现主要依赖于一个int类型的成员变量来表示同步状态以及一个FIFO队列构建等待队列。它的子类必须重写AQS定义的几个protected修饰的用来改变同步状态的方法，其他方法主要是用来实现排队和阻塞机制的。

同步器是实现锁的关键，在锁的实现中聚合同步器，利用同步器实现锁的语义，可以这样理解两者的关系：

锁是面向使用者的，它定义了使用者和锁交互的接口，隐藏了实现的细节，同步器是面向锁的实现者，它简化了锁的实现方式，屏蔽了同步状态的管理，线程的排队，等待和唤醒等底层操作。

**AQS的设计是使用模版方法设计模式，它将一个方法开放给子类重写，而同步器给同步组件所提供的模版方法又会重新调用子类所重写的方法。**

# AQS核心思想

如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的工作线程，并且将共享资源设置为锁定状态。

如果被请求的共享资源被占用，那么就需要一套线程阻塞等待以及被唤醒时锁分配的机制，这个机制AQS是用CLH队列锁实现的，即将暂时获取不到锁的线程加入到队列中。

1、AQS使用一个int成员变量来表示同步状态

2、使用Node实现FIFO队列，可以用于构建锁或者其他同步装置

AQS资源共享方式：独占Exclusive（排它锁模式）和共享Share（共享锁模式）

> AQS它的所有子类中，要么实现并使用了它的独占功能的api，要么使用了共享锁的功能，而不会同时使用两套api，即便是最有名的子类ReentrantReadWriteLock也是通过两个内部类读锁和写锁分别实现了两套api来实现的

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210802175542181.png" alt="image-20210802175542181" style="zoom:50%;" />

## state状态

state状态使用volatile int类型的变量，表示当前同步状态。state的访问方式有三种:

`getState()`
 `setState()`
 `compareAndSetState()`

## Node内部类

Node类是AQS的绝对核心类，AQS基于Node来构建同步队列和Condition队列；

**源码如下：**

```java
static final class Node {
        /** Marker to indicate a node is waiting in shared mode */
        static final Node SHARED = new Node();
        /** Marker to indicate a node is waiting in exclusive mode */
        static final Node EXCLUSIVE = null;

        /** waitStatus value to indicate thread has cancelled */
        static final int CANCELLED =  1;
        /** waitStatus value to indicate successor's thread needs unparking */
        static final int SIGNAL    = -1;
        /** waitStatus value to indicate thread is waiting on condition */
        static final int CONDITION = -2;
        /**
         * waitStatus value to indicate the next acquireShared should
         * unconditionally propagate
         */
        static final int PROPAGATE = -3;
        volatile int waitStatus;
        volatile Node prev;
        volatile Node next;
        volatile Thread thread;
        Node nextWaiter;

        /**
         * Returns true if node is waiting in shared mode.
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }
				// 获取前置节点
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {    // Used to establish initial head or SHARED marker
        }

        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
```

**CANCELLED**
 waitStatus值为1时表示该线程节点已释放（超时、中断），已取消的节点不会再阻塞。

**SIGNAL**
 waitStatus为-1时表示该线程的后续线程需要阻塞，即只要前置节点释放锁，就会通知标识为 SIGNAL 状态的后续节点的线程

**CONDITION**
 waitStatus为-2时，表示该线程在condition队列中阻塞（Condition有使用）

**PROPAGATE**
 waitStatus为-3时，表示该线程以及后续线程进行无条件传播（CountDownLatch中有使用）共享模式下， PROPAGATE 状态的线程处于可运行状态

 # AQS之独占+非公平

## 获取锁acquire

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210802195409121.png" alt="image-20210802195409121" style="zoom:50%;" />

ReentrantLock是AQS独占模式的经典实现，ReentrantLock在构造实例是可以指定是否是fair lock。

```java
 /**
     * Creates an instance of {@code ReentrantLock} with the
     * given fairness policy.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
```

### acquire方法获取许可

下面我们就从锁的获取入手开始解读AQS：

```java
public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
```

### tryAcquire抽象方法

tryAcquire是个protected方法，具体是实现在对应的子类中，这个方法的功能就是尝试去修改state的状态值

```java
protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
}
```

### nonfairTryAcquire非公平锁获取许可

```java
// ReentrantLock 非公平锁进来就开始抢占锁，体现非公平性
final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }
```

以ReentrantLock方法的实现为例，看一下源码：

```java
final boolean nonfairTryAcquire(int acquires) {
						// 获取当前线程
            final Thread current = Thread.currentThread();
            // getState()返回的就是AQS类中的state字段的值
            int c = getState();
            // c == 0 说明当前锁没有被任何线程占有
            if (c == 0) {
            		// 使用cas去修改state的值，独占模式下acquires = 1
                if (compareAndSetState(0, acquires)) {
                		// 修改state成功之后，将独占线程设置成当前线程，并且返回true，表示抢占锁成功
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            // 如果state ！= 0 并且独占线程就是当前线程，表示当前线程持有对象的锁，此时，需要锁重入，state继续累加
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
```

AQS的acquire(int arg)方法中还有一部分就是 `acquireQueued(addWaiter(Node.EXCLUSIVE), arg)`

### addWaiter添加等待队列

我们先看一下addWaiter方法，java.util.concurrent.locks.AbstractQueuedSynchronizer#addWaiter

如果tryAcquire返回FALSE（获取同步状态失败），则调用该方法将当前线程加入到CLH同步队列尾部。

```java
private Node addWaiter(Node mode) {
				// 首先创建一个Node节点
        Node node = new Node(Thread.currentThread(), mode);
        // Try the fast path of enq; backup to full enq on failure
        Node pred = tail;
        // cas 将当前节点设置到同步队列的队尾
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        // 如果上面cas设置没有成功，则通过enq方法将节点添加到队尾
        enq(node);
        return node;
    }
```

java.util.concurrent.locks.AbstractQueuedSynchronizer#enq

```java
 private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { 
                // Must initialize 初始化头节点
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
```

通过自旋，最终将node节点添加到同步队列中。

### acquireQueued获取许可

节点添加到同步队列之中，然后是一个非常重要的方法 ‼️

acquireQueued方法为一个自旋的过程，也就是说当前线程（Node）进入同步队列后，就会进入一个自旋的过程，每个节点都会自省地观察，当条件满足，获取到同步状态后，就可以从这个自旋过程中退出，否则会一直执行下去。

```java
final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
              	// 如果node节点是队列中第二个节点（因为第一个正在执行状态）肯定要队列中从第二个节点开始尝试获取锁
                final Node p = node.predecessor();
              // 第二个节点调用tryAcquire方法
                if (p == head && tryAcquire(arg)) {
                   //把当前节点设置成队列头节点
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                //判断是否需要挂起队列中后续的节点
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
          	// 如果获取锁失败
            if (failed)
                cancelAcquire(node);
        }
    }
```

### shouldParkAfterFailedAcquire方法

shouldParkAfterFailedAcquire将队列后续节点挂起

```java
 private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
          	// 如果前一个节点的waitStatus == Node.SIGNAL 则直接返回true
          	// 因为前一个节点状态是Node.SIGNAL时，才会通知后续节点进行park或者unpark
            return true;
        if (ws > 0) {
            //  static final int CANCELLED =  1;
          	// 取消状态的节点直接在等待队列中去除
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            // 将前一个节点的waitStatus设置成Node.SIGNAL
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
```

```
private final boolean parkAndCheckInterrupt() {
		// 挂起当前线程，走到这肯定是没有拿到执行权的，线程需要挂起等待其他线程释放锁
    LockSupport.park(this);
    return Thread.interrupted();
}
```

最后如果获取失败的话，会调用下面这个方法：

### cancelAcquire取消获取

```java
private void cancelAcquire(Node node) {
    // 如果节点为空，直接返回
    if (node == null)
        return;
    // 由于线程要被取消了，所以将 thread 线程清掉
    node.thread = null;

    // 下面这步表示将 node 的 pre 指向之前第一个非取消状态的结点（即跳过所有取消状态的结点）,waitStatus > 0 表示当前结点状态为取消状态
    Node pred = node.prev;
    while (pred.waitStatus > 0)
        node.prev = pred = pred.prev;

    // 获取经过过滤后的 pre 的 next 结点，这一步主要用在后面的 CAS 设置 pre 的 next 节点上
    Node predNext = pred.next;
    // 将当前结点设置为取消状态
    node.waitStatus = Node.CANCELLED;

    // 如果当前取消结点为尾结点，使用 CAS 则将尾结点设置为其前驱节点，如果设置成功，则尾结点的 next 指针设置为空
    if (node == tail && compareAndSetTail(node, pred)) {
        compareAndSetNext(pred, predNext, null);
    } else {
    // 这一步看得有点绕，我们想想，如果当前节点取消了，那是不是要把当前节点的前驱节点指向当前节点的后继节点
    // 但是我们之前也说了，要唤醒或阻塞结点，须在其前驱节点的状态为 SIGNAL 的条件才能操作，
    //所以在设置 pre 的 next 节点时要保证 pre 结点的状态为 SIGNAL，想通了这一点相信你不难理解以下代码。
        int ws;
        if (pred != head &&
            ((ws = pred.waitStatus) == Node.SIGNAL ||
             (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
            pred.thread != null) {
            Node next = node.next;
            if (next != null && next.waitStatus <= 0)
                compareAndSetNext(pred, predNext, next);
        } else {
        // 如果 pre 为 head，或者  pre 的状态设置 SIGNAL 失败，则直接唤醒后继结点去竞争锁，之前我们说过， SIGNAL 的结点取消（或释放锁）后可以唤醒后继结点
            unparkSuccessor(node);
        }
        node.next = node; // help GC
    }
}
```

##  释放锁release

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

tryRelease方法也是由子类来实现的。

```java
protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
  					// 判断当前线程
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
              	// 将独占线程设置成null，下一个线程获取到锁时会设置成自己的
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
```

下面是执行unparkSuccessor(h);方法了，当前线程释放了锁之后，需要唤醒等待队列中的第二个节点对应的线程。这里注意一点的是，要执行的Node节点的waitStatus肯定是0；？？

```java
    private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
        if (ws < 0)
	          //置零当前线程所在的结点状态，允许失败
            compareAndSetWaitStatus(node, ws, 0);
				// 从第二个节点开始往后找waitStatus<=0的节点，然后执行unpark
        Node s = node.next;
		    // 找到下一个需要唤醒的结点
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }
```



# Condition在AQS中的实现

上面已经介绍了`AQS`所提供的核心功能，当然它还有很多其他的特性，这里我们来继续说下`Condition`这个组件。

Condition是在java 1.5中才出现的，它用来替代传统的`Object`的`wait()`、`notify()`实现线程间的协作，相比使用`Object`的`wait()`、`notify()`，使用`Condition`中的`await()`、`signal()`这种方式实现线程间协作更加安全和高效。因此通常来说比较推荐使用`Condition

其中`AbstractQueueSynchronizer`中实现了`Condition`中的方法，主要对外提供`awaite(Object.wait())`和`signal(Object.notify())`调用。

## Condition在java代码中的应用

```java
public class ReentrantLockDemo {
    static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        Condition condition = lock.newCondition();

        new Thread(() -> {
            lock.lock();
            try {
                System.out.println("线程一加锁成功");
                System.out.println("线程一执行await被挂起");
                condition.await();
                System.out.println("线程一被唤醒成功");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                System.out.println("线程一释放锁成功");
            }
        }).start();

        new Thread(() -> {
            lock.lock();
            try {
                System.out.println("线程二加锁成功");
                condition.signal();
                System.out.println("线程二唤醒线程一");
            } finally {
                lock.unlock();
                System.out.println("线程二释放锁成功");
            }
        }).start();
    }
}
```

线程一调用了condition.await();之后，线程二才可以获取到锁并且执行自己的任务，线程二调用 condition.signal();之后唤醒线程一，但是还没有执行权限，只有在线程二执行完成之后调用lock.unlock();之后，线程一重新回去到锁，然后执行线程一后续的流程。

![image-20210802183143055](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210802183143055.png)

## await方法

await使当前线程释放锁，也就是执行许可，然后进入Condition队列，等待在某个时刻被某个线程唤醒。

```java
public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
  					// 将当前线程封装成Node节点添加的Condition队列中
            Node node = addConditionWaiter();
  					// 添加到Condition队列中的线程需要释放锁资源
            int savedState = fullyRelease(node);
            int interruptMode = 0;
  					// 查看当前节点是不是在同步队列中
            while (!isOnSyncQueue(node)) {
              // 当前节点不在同步队列中，那么直接park挂起
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
  					//  表明已经有的线程调用了signal唤醒当前线程，
  					// 并且节点已经存放到了同步等待队列中，所以可以调用如果acquireQueued请求许可了
  					// savedState是获取许可的个数 这个要和之前释放的许可个数一致
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }
```

**addConditionWaiter方法添加一个节点到Condition队列中**

java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject#addConditionWaiter

```java
private Node addConditionWaiter() {
            Node t = lastWaiter;
            // If lastWaiter is cancelled, clean out.
            if (t != null && t.waitStatus != Node.CONDITION) {
              	// 先检查一遍有没有取消状态的节点，如果有的话，清除掉
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
  					// 将当前线程封装成Node添加到Condition队列中
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }
```

## signal方法

唤醒一个线程

```java
 public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
}
```

### doSignal方法

```java
private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
              // 将Condition队列中的节点状态设置成SIGNAL，并将节点添加到同步队列中
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }
```

### transferForSignal方法

```java
final boolean transferForSignal(Node node) {
        /*
         * If cannot change waitStatus, the node has been cancelled.
         */
         // 将节点状态改成0 
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * Splice onto queue and try to set waitStatus of predecessor to
         * indicate that thread is (probably) waiting. If cancelled or
         * attempt to set waitStatus fails, wake up to resync (in which
         * case the waitStatus can be transiently and harmlessly wrong).
         */
         // 把当前添加到同步队列中，并返回前一个节点
        Node p = enq(node);
        int ws = p.waitStatus;
        // 设置前一个节点的状态为SIGNAL
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
        		// 唤醒当前节点的线程
            LockSupport.unpark(node.thread);
        return true;
    }
```



# 参考资料

https://mp.weixin.qq.com/s/hB5ncpe7_tVovQj1sNlDRA

https://mp.weixin.qq.com/s/iNz6sTen2CSOdLE0j7qu9A

https://github.com/AobingJava/JavaFamily

https://segmentfault.com/a/1190000015804888/

https://juejin.cn/post/6844903997438951437

https://juejin.cn/post/6870099231361728525

