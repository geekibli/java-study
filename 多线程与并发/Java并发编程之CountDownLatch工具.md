---
title: Java并发编程之CountDownLatch工具
toc: true
date: 2021-08-04 17:00:07
tags: 多线程
categories:
---

# CountDownLatch

你要问什么是CountDownLatch? 那我可有的说了。

之前干活的时候，有很多处理数据的任务，但是呢，数据量很大，写的java脚本执行下来肯定会比较慢，那怎么办呢，想起来刚毕业那会，有个同事写了一个并发调用的工具，当时感觉碉堡了。

当我查看这个工具的具体实现时，发现它是基于CountDownLatch来封装的，咱当时也没用过CountDownLatch，感觉应该挺难，就直接用了那个工具。

后来发现那个工具使用起来有些繁琐，就比如我刷数据这个事，CountDownLatch直接干是最简单的。

## CountDownLatch是什么

A synchronization aid that allows one or more threads to wait until a set of operations being performed in other threads completes.

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210804172448596.png" alt="image-20210804172448596" style="zoom:50%;" />

按照官方API文档上的介绍呢，CountDownLatch就是一个同步机制，用来实现一个或多个线程一直wait知道另一个线程完成一系列动作。

## CountDownLatch使用

```java
public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        new Thread(()->{
            try {
                Thread.sleep(1000);
                latch.countDown();
                System.out.println(Thread.currentThread().getName() + " execute 111");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(()->{
            try {
                Thread.sleep(5000);
                System.out.println(Thread.currentThread().getName() + " execute 222");
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.println("main thread invoke await");
        latch.await();
        System.out.println("subThread execute end");
    }
}
```

执行结果如下：

```
main thread invoke await
Thread-0 execute 111
Thread-1 execute 222
subThread execute end
```

下面我们就从countDown和await两个方法解析CountDownLatch的运行机制吧

## CountDownLatch实现原理

和ReentrantLock实现独占锁不同的是，CountDownLatch是典型的共享锁。

值得注意的是，CountDownLatch的静态内部类Sync继承了AbstractQueuedSynchronizer并实现了tryAcquireShared方法和tryReleaseShared方法。

下面先从构造方法入手开始学习 👇

```java
public CountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException("count < 0");
    this.sync = new Sync(count);
}
```

初始化count字段，其值是设置在AQS的state字段上面的，当每个线程执行了countDown()之后，`state = state - 1`

当state = 0 时，唤醒之前await的线程。

### await()

**下面是await方法：**

```java
public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}
```

**AQS#acquireSharedInterruptibly(int arg)**

```java
public final void acquireSharedInterruptibly(int arg)
  throws InterruptedException {
  if (Thread.interrupted())
    // 获取中断标志，把中断标志复位，然后把中断异常往上层抛
    throw new InterruptedException();
  if (tryAcquireShared(arg) < 0)
    doAcquireSharedInterruptibly(arg);
}
```

tryAcquireShared(arg)这个方法和之前学习ReentrantLock时是一样的，这是AQS提供的模版方法。

AQS提供模版方法，有每个子类自己去实现逻辑，然后再由AQS本身调用。

**CountDownLatch#tryAcquireShared(int acquires)** 

```java
protected int tryAcquireShared(int acquires) {
    return (getState() == 0) ? 1 : -1;
}
```

getState()返回的是AQS的state值，第一个线程获取是肯定不是0

如果getState()方法返回-1的话，会执行下面的方法：

**AQS#doAcquireSharedInterruptibly**

```java
private void doAcquireSharedInterruptibly(int arg)
  throws InterruptedException {
  final Node node = addWaiter(Node.SHARED);
  boolean failed = true;
  try {
    for (;;) {
      final Node p = node.predecessor();
      if (p == head) {
        int r = tryAcquireShared(arg);
        if (r >= 0) {
          // 表示aqs state = 0
          // 需要把当前线程设置成头节点，并向下传播
          setHeadAndPropagate(node, r);
          p.next = null; // help GC
          failed = false;
          return;
        }
      }
      // 避免一直空转，将前一个节点状态设置成SIGNAL,然后挂起当前线程
      if (shouldParkAfterFailedAcquire(p, node) &&
          parkAndCheckInterrupt())
        // 如果线程中断，则直接抛出异常
        throw new InterruptedException();
    }
  } finally {
    if (failed)
      cancelAcquire(node);
  }
}
```

当countDownLatch的count变成0的时候，主线程await完成，然后被唤醒，继续执行。

**setHeadAndPropagate(Node node, int propagate)**

```java
private void setHeadAndPropagate(Node node, int propagate) {
  Node h = head; // Record old head for check below
  setHead(node);
  /*
     * Try to signal next queued node if:
     *   Propagation was indicated by caller,
     *     or was recorded (as h.waitStatus either before
     *     or after setHead) by a previous operation
     *     (note: this uses sign-check of waitStatus because
     *      PROPAGATE status may transition to SIGNAL.)
     * and
     *   The next node is waiting in shared mode,
     *     or we don't know, because it appears null
     *
     * The conservatism in both of these checks may cause
     * unnecessary wake-ups, but only when there are multiple
     * racing acquires/releases, so most need signals now or soon
     * anyway.
     */
  if (propagate > 0 || h == null || h.waitStatus < 0 ||
      (h = head) == null || h.waitStatus < 0) {
    Node s = node.next;
    if (s == null || s.isShared())
      // 如果后续节点是shard节点，释放
      doReleaseShared();
  }
}
```

### countDown()

```java
public void countDown() {
    sync.releaseShared(1);
}
```

AQS#releaseShared(int arg) 释放共享锁

```java
public final boolean releaseShared(int arg) {
  // 有子类实现
  if (tryReleaseShared(arg)) {
    doReleaseShared();
    return true;
  }
  return false;
}
```

**CountDownLatch#tryReleaseShared(int releases)**

这个方法比较简单，每执行一次countDown(), state = state - 1

最后返回state是否等于0 如果不等于0 说明共享锁不能释放

```java
protected boolean tryReleaseShared(int releases) {
  // Decrement count; signal when transition to zero
  for (;;) {
    int c = getState();
    if (c == 0)
      return false;
    int nextc = c-1;
    if (compareAndSetState(c, nextc))
      return nextc == 0;
  }
}
```



doReleaseShared() 释放共享锁方法

```java
private void doReleaseShared() {
  /*
         * Ensure that a release propagates, even if there are other
         * in-progress acquires/releases.  This proceeds in the usual
         * way of trying to unparkSuccessor of head if it needs
         * signal. But if it does not, status is set to PROPAGATE to
         * ensure that upon release, propagation continues.
         * Additionally, we must loop in case a new node is added
         * while we are doing this. Also, unlike other uses of
         * unparkSuccessor, we need to know if CAS to reset status
         * fails, if so rechecking.
         */
  for (;;) {
    Node h = head;
    if (h != null && h != tail) {
      // 获取头结点的等待状态
      int ws = h.waitStatus;
      if (ws == Node.SIGNAL) {
        if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
          continue;            // loop to recheck cases
        // 释放后继结点
        unparkSuccessor(h);
      }
      else if (ws == 0 &&
               !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
        continue;                // loop on failed CAS
    }
    if (h == head)                   // loop if head changed
      break;
  }
}
```

**unparkSuccessor()执行线程唤醒的方法**

```java
private void unparkSuccessor(Node node) {
  /*
     * If status is negative (i.e., possibly needing signal) try
     * to clear in anticipation of signalling.  It is OK if this
     * fails or if status is changed by waiting thread.
     */
  int ws = node.waitStatus;
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
    for (Node t = tail; t != null && t != node; t = t.prev)
      if (t.waitStatus <= 0)
        s = t;
  }
  if (s != null)
    LockSupport.unpark(s.thread);
}
```

## 参考文章

https://www.jianshu.com/p/128476015902

https://segmentfault.com/a/1190000015807573

