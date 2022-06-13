---
title: Java并发编程之Blocking Queue
toc: true
date: 2021-08-03 16:48:32
tags: 多线程
categories:
---



# Blocking Queue

A blocking queue is a queue that blocks when you try to dequeue from it and the queue is empty, or if you try to enqueue items to it and the queue is already full. A thread trying to dequeue from an empty queue is blocked until some other thread inserts an item into the queue. A thread trying to enqueue an item in a full queue is blocked until some other thread makes space in the queue, either by dequeuing one or more items or clearing the queue completely.

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210804131939043.png" alt="image-20210804131939043" style="zoom:50%;" />

阻塞队列两大特性：

- 当队列满时，如果**生产者线程**向队列 put 元素，队列会一直阻塞生产者线程，直到队列可用或者响应中断退出
- 当队列为空时，如果**消费者线程** 从队列里面 take 元素，队列会阻塞消费者线程，直到队列不为空

阻塞队列最常使用在生产者和消费者模型中，生产者生产数据，将数据存放在队列中，消费者消费数据，在队列中取出数据。

阻塞队列在不可用时，下面是各种处理操作的结果：👇

| 方法/处理方式 | 抛出异常  | 返回特殊值 | 一直阻塞 |      超时退出       |
| :-----------: | :-------: | :--------: | :------: | :-----------------: |
|   插入方法    |  add(e)   |  offer(e)  |  put(e)  | offer(e, time,unit) |
|   移除方法    | remove()  |   poll()   |  take()  |   poll(time,unit)   |
|   检查方法    | element() |   peek()   |  不可用  |       不可用        |

### add 抛出异常IllegalStateException

```java
public class ArrayBlockingQueueDemo {
    public static void main(String[] args) {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(1);
        queue.add("a");
        queue.add("b");
        System.err.println("queue size -> " + queue.size());
    }
}
```

**异常信息：**

```java
Exception in thread "main" java.lang.IllegalStateException: Queue full
	at java.util.AbstractQueue.add(AbstractQueue.java:98)
	at java.util.concurrent.ArrayBlockingQueue.add(ArrayBlockingQueue.java:312)
	at com.ibli.note.ArrayBlockingQueueDemo.main(ArrayBlockingQueueDemo.java:15)
```

### element抛出异常NoSuchElementException

```java
public class ArrayBlockingQueueDemo {
    public static void main(String[] args) {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(1);
        System.err.println("queue size -> " + queue.size());
        queue.element();
    }
}
```

**异常信息：**

```java
queue size -> 0
Exception in thread "main" java.util.NoSuchElementException
	at java.util.AbstractQueue.element(AbstractQueue.java:136)
	at com.ibli.note.ArrayBlockingQueueDemo.main(ArrayBlockingQueueDemo.java:14)
```

## ArrayBlockingQueue

底层由数组实现的有界的阻塞队列，它的容量在创建的时候就已经确认了，并且不能修改。

```java
public ArrayBlockingQueue(int capacity, boolean fair) {
    if (capacity <= 0)
        throw new IllegalArgumentException();
    this.items = new Object[capacity];
    lock = new ReentrantLock(fair);
    notEmpty = lock.newCondition();
    notFull =  lock.newCondition();
}
```

默认情况下，ArrayBlockingQueue是不保证线程公平访问队列的，这里所谓的公平与否是指，阻塞的线程能否按照阻塞的先后顺序访问队列，先阻塞先访问，后阻塞后访问。

思考为什么默认情况下是非公平的方式访问呢？ 🤔

> 这个是为了增加系统资源利用率，在不保证公平的情况下，多线程之间之间执行的效率要比公平模式下高的多。

### ArrayBlovkingQueue#put方法

![image-20210804140652447](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210804140652447.png)

下面是put方法源码：

```java
public void put(E e) throws InterruptedException {
    checkNotNull(e);
    final ReentrantLock lock = this.lock;
  	// 加锁
    lock.lockInterruptibly();
    try {
        while (count == items.length)
          	// 队列满了之后，阻塞
            notFull.await();
      	// 向队列中添加元素
        enqueue(e);
    } finally {
      	// 执行完最后释放锁
        lock.unlock();
    }
}
```

下面是添加数据的方法源码：

```java
private void enqueue(E x) {
    // assert lock.getHoldCount() == 1;
    // assert items[putIndex] == null;
    final Object[] items = this.items;
    items[putIndex] = x;
    if (++putIndex == items.length)
        putIndex = 0;
    count++;
  	// 数据添加完之后，唤醒等待队列中的线程到同步队列
    notEmpty.signal();
}
```

‼️唤醒的线程能够抢到锁是不确定的，signal会添加节点到同步队列中等待获取锁。这个可以看一下Condition那篇文章。

ArrayBlockingQueue更多详细细节以及原理跳转链接https://www.jianshu.com/p/a636b3d83911

## LinkedBlockingQueue

LinkedBlockingQueue是用链表实现的有界阻塞队列，同样满足FIFO的特性，与ArrayBlockingQueue相比起来具有更高的吞吐量，为了防止LinkedBlockingQueue容量迅速增，损耗大量内存。通常在创建LinkedBlockingQueue对象时，会指定其大小，如果未指定，容量等于Integer.MAX_VALUE;

> Executors.newFixedThreadPool 阿里巴巴禁止使用Executors来创建线程池

队列大小无限制，常用的为无界的LinkedBlockingQueue，使用该队列做为阻塞队列时要尤其当心，当任务耗时较长时可能会导致大量新任务在队列中堆积最终导致OOM。阅读代码发现，Executors.newFixedThreadPool 采用就是 LinkedBlockingQueue，当QPS很高，发送数据很大，大量的任务被添加到这个无界LinkedBlockingQueue 中，导致cpu和内存飙升服务器挂掉。

### 属性信息

```java
/**
 * 节点类，用于存储数据
 */
static class Node<E> {
    E item;
    Node<E> next;

    Node(E x) { item = x; }
}

/** 阻塞队列的大小，默认为Integer.MAX_VALUE */
private final int capacity;

/** 当前阻塞队列中的元素个数 */
private final AtomicInteger count = new AtomicInteger();

/**
 * 阻塞队列的头结点
 */
transient Node<E> head;

/**
 * 阻塞队列的尾节点
 */
private transient Node<E> last;

/** 获取并移除元素时使用的锁，如take, poll, etc */
private final ReentrantLock takeLock = new ReentrantLock();

/** notEmpty条件对象，当队列没有数据时用于挂起执行删除的线程 */
private final Condition notEmpty = takeLock.newCondition();

/** 添加元素时使用的锁如 put, offer, etc */
private final ReentrantLock putLock = new ReentrantLock();

/** notFull条件对象，当队列数据已满时用于挂起执行添加的线程 */
private final Condition notFull = putLock.newCondition();
```

### 构造函数

```java
public LinkedBlockingQueue() {
    // 默认大小为Integer.MAX_VALUE
    this(Integer.MAX_VALUE);
}

public LinkedBlockingQueue(int capacity) {
    if (capacity <= 0) throw new IllegalArgumentException();
    this.capacity = capacity;
    last = head = new Node<E>(null);
}

public LinkedBlockingQueue(Collection<? extends E> c) {
    this(Integer.MAX_VALUE);
    final ReentrantLock putLock = this.putLock;
    putLock.lock();
    try {
        int n = 0;
        for (E e : c) {
            if (e == null)
                throw new NullPointerException();
            if (n == capacity)
                throw new IllegalStateException("Queue full");
            enqueue(new Node<E>(e));
            ++n;
        }
        count.set(n);
    } finally {
        putLock.unlock();
    }
}
```

### LinkedBlockingQueue#put方法

```java
public void put(E e) throws InterruptedException {
    if (e == null) throw new NullPointerException();
    int c = -1;
    Node<E> node = new Node<E>(e);
    final ReentrantLock putLock = this.putLock;
    final AtomicInteger count = this.count;
    // 获取锁中断
    putLock.lockInterruptibly();
    try {
        //判断队列是否已满，如果已满阻塞等待
        while (count.get() == capacity) {
            notFull.await();
        }
        // 把node放入队列中
        enqueue(node);
        c = count.getAndIncrement();
        // 再次判断队列是否有可用空间，如果有唤醒下一个线程进行添加操作
        if (c + 1 < capacity)
            notFull.signal();
    } finally {
        putLock.unlock();
    }
    // 如果队列中有一条数据，唤醒消费线程进行消费
    if (c == 0)
        signalNotEmpty();
}
```

- 队列已满，阻塞等待。
- 队列未满，创建一个node节点放入队列中，如果放完以后队列还有剩余空间，继续唤醒下一个添加线程进行添加。如果放之前队列中没有元素，放完以后要唤醒消费线程进行消费。

### ArrayBlockingQueue与LinkedBlockingQueue的比较

**相同点**：ArrayBlockingQueue和LinkedBlockingQueue都是通过condition通知机制来实现可阻塞式插入和删除元素，并满足线程安全的特性；

**不同点**：

1、ArrayBlockingQueue底层是采用的数组进行实现，而LinkedBlockingQueue则是采用链表数据结构；

2、ArrayBlockingQueue插入和删除数据，只采用了一个lock，而LinkedBlockingQueue则是在插入和删除分别采用了`putLock`和`takeLock`，这样可以降低线程由于线程无法获取到lock而进入WAITING状态的可能性，从而提高了线程并发执行的效率

更多LinkedBlockingQueue的实现细节参见https://blog.csdn.net/tonywu1992/article/details/83419448

## PriorityBlockingQueue

PriorityBlockingQueue是一个支持优先级的**无界阻塞**队列。默认情况下元素采用自然顺序进行排序，也可以通过自定义类实现compareTo()方法来指定元素排序规则，或者初始化时通过构造器参数Comparator来指定排序规则。

```java
public PriorityBlockingQueue(int initialCapacity,
                                 Comparator<? super E> comparator) {
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.comparator = comparator;
        this.queue = new Object[initialCapacity];
    }
```

使用优先级队列需要注意的点：

1、队列中不允许出现null值，也不允许出现不能排序的元素。

2、队列容量是没有上限的，但是如果插入的元素超过负载，有可能会引起OutOfMemory异常。

> 当我们使用无界队列是都应该注意的点，不能在队列中无限存放数据

3、PriorityBlockingQueue由于是无界的，所以put方法是非阻塞的。

```java
public void put(E e) {
    offer(e); // never need to block  请自行对照上面表格
}
```

可以给定初始容量，这个容量会按照一定的算法自动扩充

```java
// Default array capacity.
private static final int DEFAULT_INITIAL_CAPACITY = 11;

public PriorityBlockingQueue() {
    this(DEFAULT_INITIAL_CAPACITY, null);
}
```

这里默认的容量是 11，由于也是基于数组。

4、`内部只有一个Lock，所以生产消费者不能同时作业`

详情可以参照https://www.cnblogs.com/wyq1995/p/12289462.html

## DelayQueue

DelayQueue顾名思义，具有延时作用的队列。

记得第一次接触延时队列的时候是在看分布式任务调度时看到底层有关延时队列的实现。

DelayQueue 也是一个无界阻塞队列，使用时要注意OOM。

`只有delay时间小于0的元素才能够被取出。`

### 生产者消费者模型

创建一个类，实现Delayed方法，重写getDelay方法和compareTo方法；

```java
public class DelayData implements Delayed {

    private long second;
    private String val;

    public DelayData(long second, String val) {
        long l = System.currentTimeMillis();
        System.err.println(second + " " + l);
        this.second = second + l;
        this.val = val;
    }

    public long getSecond() {
        return second;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diffTime = second - System.currentTimeMillis();
        return unit.convert(diffTime,TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        DelayData tmp = (DelayData) o;
        long result =  second - tmp.getSecond() ;
        return (int) result;
    }

    @Override
    public String toString() {
        return "DelayData{" +
                "second=" + second +
                ", val='" + val + '\'' +
                '}';
    }
}
```

**然后创建两个线程模拟生产者和消费者**

```java
public class DelayQueueDemo {

    public static void main(String[] args) {

        DelayQueue<DelayData> delayQueue = new DelayQueue<DelayData>();

        new Thread(() -> {
            delayQueue.put(new DelayData(5000, "a"));
            delayQueue.put(new DelayData(10000, "b"));
            delayQueue.put(new DelayData(15000, "c"));
        }).start();

        new Thread(() -> {
            boolean flag = true;
            while (true && flag) {
                try {
                    Thread.sleep(1000);
                    System.err.println("执行一次循环  队列长度" + delayQueue.size());
                    DelayData poll = delayQueue.take();
                    if (poll != null){
                        System.err.println(poll.toString());
                    }
                    if (delayQueue.size() == 0){
                        flag = false;
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
```

## SynchronousQueue

**SynchronousQueue**实际上它不是一个真正的队列，因为它不会维护队列中元素的存储空间，与其他队列不同的是，它维护一组线程，这些线程在等待把元素加入或移除队列。适用于生产者少消费者多的情况。

SynchronousQueue是生产者直接把数据给消费者（消费者直接从生产者这里拿数据）。换句话说，**每一个插入操作必须等待一个线程对应的移除操作**。SynchronousQueue又有两种模式：

1、公平模式

　　采用公平锁，并配合一个FIFO队列（Queue）来管理多余的生产者和消费者

2、非公平模式

　　采用非公平锁，并配合一个LIFO栈（Stack）来管理多余的生产者和消费者，这也是SynchronousQueue默认的模式

### 构造方法

```java
 public SynchronousQueue() {
        this(false);
    }
public SynchronousQueue(boolean fair) {
        transferer = fair ? new TransferQueue() : new TransferStack();
}
```

transferer 是一个内部类用于在生产者和消费者之间传递数据

### 实现生产者消费者

下面模拟一个生产者生产数据，两个消费者消费数据。

```java
public class SynchronousQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        SynchronousQueue queue = new SynchronousQueue();
        new Thread(() -> {
            try {
                Thread.sleep(2000L);
                while (true) {
                    Thread.sleep(100);
                    long l = System.currentTimeMillis();
                    queue.put(l);
                    System.out.println(Thread.currentThread().getName() + " 生产者生产数据 :" + l);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                while (true) {
                    Thread.sleep(300);
                    System.out.println(Thread.currentThread().getName() + "消费者消费数据 ： " + queue.take());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                while (true) {
                    Thread.sleep(300);
                    System.out.println(Thread.currentThread().getName() + "消费者消费数据 ： " + queue.take());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
```

代码运行结果：

```
Thread-0 生产者生产数据 :1628055947404
Thread-1消费者消费数据 ： 1628055947404
Thread-0 生产者生产数据 :1628055947506
Thread-2消费者消费数据 ： 1628055947506
Thread-0 生产者生产数据 :1628055947608
Thread-2消费者消费数据 ： 1628055947608
Thread-0 生产者生产数据 :1628055947713
```

SynchronousQueue详细实现细节参见https://blog.csdn.net/yanyan19880509/article/details/52562039



## 参考资料

https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/BlockingQueue.html

http://tutorials.jenkov.com/java-concurrency/index.html

https://www.baeldung.com/java-blocking-queue

https://blog.csdn.net/tonywu1992/article/details/83419448















