---
title: Java并发编程之深入理解ReentrantLock
toc: true
date: 2021-08-02 16:00:33
tags: 多线程
categories:

---

# ReentrantLock

ReentrantLock重入锁，是实现Lock接口的一个类，也是在实际编程中使用频率很高的一个锁，**支持重入性，表示能够对共享资源能够重复加锁，即当前线程获取该锁再次获取不会被阻塞**

## 加锁操作

**支持重入性，表示能够对共享资源能够重复加锁，即当前线程获取该锁再次获取不会被阻塞**

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210802160050888.png" alt="image-20210802160050888" style="zoom:50%;" />

下面以非公平锁的lock方法为例，看一下ReentrantLock源码的实现 👇

### 首先是lock方法

1、进入lock方法首先对调用compareAndSetState(0,1)去尝试获取锁，这一点正是体现了非公平锁

2、如果第一步没有获取到锁，然后执行第二步acquire(1)

```java
final void lock() {
  					// 非公平锁
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }
```

lock方法首先会去cas修改AQS的state状态，独占锁模式下state增加1表示获取锁成功；state设置成功之后，需要将独占线程字段设置成当前线程：`exclusiveOwnerThread = thread;`

### AQS#acquire(1)

如果没有抢占到锁，那么执行下面的acquire方法，这个方法定义在AQS类中

```java
public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
}
```

tryAcquire方法是在子类实现的，在这里我们看一下ReentrantLock的nonfairTryAcquire，也就是非公平锁的实现。

### nonfairTryAcquire(int acquires)方法

下面是ReentrantLock，非公平锁的lock实现代码：

```java
final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
  					// 获取锁
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
  					// 锁冲入
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

int c = getState() == 0 则表示没有线程占有锁，当前线程来加锁时，可以直接使用cas尝试获取锁。

current == getExclusiveOwnerThread() 表示当前线程已经持有线程锁了，` int nextc = c + acquires;`则表示支持锁重入，nextc的值则表示锁重入的次数；

以上如果没有加锁成功，则返回false，然后执行AQS的acquireQueue方法，首先将当前节点封装成`addWaiter(Node.EXCLUSIVE), arg)` 添加到同步队列，同时判断头节点是否获取锁成功，如果成功了，将当前节点添加到头上；

### AQS#addWaiter(Node mode)

添加节点到队列中，Node.EXCLUSIVE独占锁，这里采用的是**尾插法**，在队列的队尾添加新的节点。

```java
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    // 如果队列不是空的，则直接添加到队尾
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    // 如果是空的，则调用enq方法，创建队列，并添加到队尾
    enq(node);
    return node;
}
```

### AQS#enq(final Node node)

第一个线程获取锁的时候，肯定是无锁的状态，根本走不到这一步，最早走到这里的是第二个去获取锁的线程。

当第二个线程执行到该方法是需要执行**两次循环**：

1、t == null时，需要初始化队列

2、执行下一次循环，将node添加到tail,由于这个方法还是处在并发环境下的，所以，设置队尾的时候还是需要cas操作。

```java
private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        if (t == null) { // Must initialize
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

### AQS#acquireQueued(final Node node, int arg) 

<font color=red>这个方法绝对是绝对的AQS核心方法 </font> ‼️

这个方法主要有3个重要操作：

1、判断前置节点是不是head，如果是的话，去尝试获取锁；

2、如果前置节点不是head，要把前置节点的waitState设置成SIGNAL，同时park当前线程，避免一直空转，因为这里是用的  for (;;) {}

3、如果获取锁和park都失败了，则把当前节点设置成cancel状态。

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

### AQS.cancelAcquire(Node node)

`这个方法比较难理解，总结一下就干了下面几个事：`

1、执行到这个方法的node肯定是要取消的，那个需要thread设置成null

2、查看当前节点之前的节点有没有是取消状态的，一起踢出队列

3、把当前节点设置成Node.CANCELLED状态

4、判断node在队列中的位置，如果是队尾的话，把tail指向node的前置节点，并且把前驱节点的next指向null

5、如果不是tail节点，那么判断是不是head，如果不是head，那么，将node的前驱节点的状态设置成Node.SIGNAL，并且把node的前驱节点node的next节点

6、如果node是head节点，那么直接unpark此线程去执行acquire

```java
private void cancelAcquire(Node node) {
        // Ignore if node doesn't exist
        if (node == null)
            return;

        node.thread = null;

        // Skip cancelled predecessors
        Node pred = node.prev;
        while (pred.waitStatus > 0) //cancelled
            node.prev = pred = pred.prev;

        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
        Node predNext = pred.next;

        // Can use unconditional write instead of CAS here.
        // After this atomic step, other Nodes can skip past us.
        // Before, we are free of interference from other threads.
        node.waitStatus = Node.CANCELLED;

        // If we are the tail, remove ourselves.
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
              // if执行的逻辑是把前置节点设置成Node.SIGNAL
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                  	// 把node的前置前置节点的下一个节点指向node的下一个节点，因为上面node已经是Node.CANCELLED状态了，需要踢出队列
                    compareAndSetNext(pred, predNext, next);
            } else {
                // 前置节点是head，此时没有被人竞争锁资源，直接唤醒当前节点
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }
```



上面是以ReentrantLock的非公平锁为例学习了一下ReentrantLock加锁的过程。那么思考一下公平锁和非公平锁的有什么区别呢？🤔

理解了上面的流程之后，下面直接比较源码遍很好理解两者之间的区别！

## 公平锁和非公平锁

如何制定ReentarntLock的公平锁和非公平锁？

```java
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

上面看了NonfairSync#lock的实现，下面看一下FairSync#lock的实现：👇

```java
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;

    final void lock() {
        acquire(1);
    }

    /**
     * Fair version of tryAcquire.  Don't grant access unless
     * recursive call or no waiters or is first.
     */
    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        return false;
    }
}
```

FairSync和NonfairSync都是ReentrantLock的静态内部类，在FairSync的lock方法中，没有下面的代码：

```java
if (compareAndSetState(0, 1))
   setExclusiveOwnerThread(Thread.currentThread());
```

每一个线程都直接调用AQS#acquire(1)方法，而且在ReentrantLock#FairSync#FairSync(int acquires)的实现中，添加了一个判断

```java
 if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
```

也就是`hasQueuedPredecessors`方法，这个方法的作用是判断队列中是否有节点在等待，如果有的话，ReentrantLock#FairSync#FairSync(int acquires)直接返回false，当前节点智能进入到队列中。这两点就是公平锁和非公平锁的明显区别体现。

## 释放锁操作

![image-20210802205517221](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210802205517221.png)

### unlock()

```java
public void unlock() {
        sync.release(1);
}
```

### AQS#release(int arg)

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

tryRelease的具体实现仍是有具体的子类来实现的。

### ReentrantLock#tryRelease(int releases)方法

1、释放锁的逻辑应该比较好理解，是将state做减法。

2、判断state == 0 , 则表示无锁状态，如果不是0，则表示还在线程重入的状态下，同时设置state

```java
protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```

这里注意一点，设置state的时候是直接赋值的，而没有使用cas，为什么？

```java
protected final void setState(int newState) {
    state = newState;
}
```

其实考虑到上下文就很简单了，此时设置state的时候，有两种状态，无锁和重入锁，肯定不会是多线程的场景。所以不需要cas操作。

接着分析上面的AQS#release方法:

当state设置成功之后，需要判断head节点，然后唤醒head的后驱节点的线程，如果存在的话。

```java
private void unparkSuccessor(Node node) {
    /*
     * If status is negative (i.e., possibly needing signal) try
     * to clear in anticipation of signalling.  It is OK if this
     * fails or if status is changed by waiting thread.
     */
    int ws = node.waitStatus;
  	// 
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    /*
     * Thread to unpark is held in successor, which is normally
     * just the next node.  But if cancelled or apparently null,
     * traverse backwards from tail to find the actual
     * non-cancelled successor.
     */
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
      	// 这里是共享锁，在ReentarntLock先跳过
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}
```



## tryLock(long time, TimeUnit unit)

**方法描述如下：**
<font color=blue>在给定的等待时间内并且线程没有被中断以及锁可用的情况下，去获取锁。</font>
如果锁可用，方法会直接返回。
如果锁不可用，则当前线程将会处于不可用状态以达到线程调度目的，并且休眠直到下面三个事件中的一个发生：
①、当前线程获取到锁
②、其他线程中断当前线程
③、指定的等待时间已过
假如当前线程：
在该方法的条目上设置其中断状态或在获取锁时中断，并且支持锁获取中断时，将抛出中断异常，当前线程中断状态会被清除。
假如给定的等待时间已过，将会返回false。

下面具体阅读源码实现,方法的入参指定了等待时间，和时间的单位，有`NANOSECONDS`、`MICROSECONDS`、`MILLISECONDS`、`SECONDS`...等单位。

下面具体阅读源码实现,方法的入参指定了等待时间，和时间的单位，有`NANOSECONDS`、`MICROSECONDS`、`MILLISECONDS`、`SECONDS`...等单位。

```java
public boolean tryLock(long timeout, TimeUnit unit)
        throws InterruptedException {
    return sync.tryAcquireNanos(1, unit.toNanos(timeout));
}
```

方法的内部调用了`Sync`的`tryAcquireNanos`，继续往下

```java
public final boolean tryAcquireNanos(int arg, long nanosTimeout)
        throws InterruptedException {
    //判断中断状态并决定是否抛出中断异常
    if (Thread.interrupted())
        throw new InterruptedException();
    //尝试获取锁，如果成功则返回true，失败则调用doAcquireNanos进行等待
    return tryAcquire(arg) ||
        doAcquireNanos(arg, nanosTimeout);
}
```

`tryAcqure`和之前分析的是同一个方法，不再赘述。
接下来是`doAcquireNanos`方法

```java
private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
    //如果给定的时间值小于等于0，则直接返回false
    if (nanosTimeout <= 0L)
        return false;
    //根据给定参数计算截止时间
    final long deadline = System.nanoTime() + nanosTimeout;
    //将当前线程添加到CLH等待队列
    final Node node = addWaiter(Node.EXCLUSIVE);
    //初始失败标志
    boolean failed = true;
    try {
        //在给定时间内循环/自旋尝试获取锁
        for (;;) {
            //取出前置节点
            final Node p = node.predecessor();
            //如果前置节点为首节点，并且当前线程能够成功获取锁
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC 前首节点出队，帮助GC
                failed = false;
                return true;
            }
            //判断是否等待超时，如果超时，则返回false
            nanosTimeout = deadline - System.nanoTime();
            if (nanosTimeout <= 0L)
                return false;
            //这里判断是否可以阻塞线程并做相应操作，跟之前分析的几个方法不一样的是，这里的阻塞多了一个判断，并且是在有限时间内阻塞，类似于sleep
            if (shouldParkAfterFailedAcquire(p, node) &&
                nanosTimeout > spinForTimeoutThreshold)
                LockSupport.parkNanos(this, nanosTimeout);
            //判断中断状态，并决定是否抛出异常
            if (Thread.interrupted())
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

`doAcquireNanos`的阻塞是有时间限制的，所以能在给定的时间内，返回获取锁的操作结果。






## 参考资料

https://juejin.cn/post/6870099231361728525

https://www.processon.com/view/5f047c16f346fb1ae598b4dd?fromnew=1

https://www.imooc.com/article/51118



