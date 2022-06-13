---
title: Java并发编程之搞懂线程池
toc: true
date: 2021-08-04 16:43:19
tags: 多线程
categories:
---



# ThreadPool

## 1. 为什么存在线程池

### 1.1 **降低资源消耗**

通过复用已存在的线程和降低线程关闭的次数来尽可能降低系统性能损耗；（享元模式）

### 1.2 **提升系统响应速度**

通过复用线程，省去创建线程的过程，因此整体上提升了系统的响应速度；

### 1.3 **提高线程的可管理性**

线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，因此，需要使用线程池来管理线程。

至于为什么不允许手动创建线程池，请参见https://dayarch.top/p/why-we-need-to-use-threadpool.html

## 2. 线程池的工作流程

线程池顾名思义，就是由很多线程构成的池子，来一个任务，就从池子中取一个线程，处理这个任务。这是一个简单的理解，实际上线程池的实现和运转是一个非常复杂的过程。

例如线程池肯定不会无限扩大的，否则资源会耗尽；当线程数到达一个阶段，提交的任务会被暂时存储在一个队列中，如果队列内容可以不断扩大，极端下也会耗尽资源，那选择什么类型的队列，当队列满如何处理任务，都有涉及很多内容。线程池总体的工作过程如下图：

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210804144852252.png" alt="image-20210804144852252" style="zoom: 50%;" />


线程池内的线程数的大小相关的概念有两个，一个是核心池大小，还有最大池大小。如果当前的线程个数比核心池个数小，当任务到来，会优先创建一个新的线程并执行任务。当已经到达核心池大小，则把任务放入队列，为了资源不被耗尽，队列的最大容量可能也是有上限的，如果达到队列上限则考虑继续创建新线程执行任务，如果此刻线程的个数已经到达最大池上限，则考虑把任务丢弃。

在 java.util.concurrent 包中，提供了 ThreadPoolExecutor 的实现。

```java
public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
} 
```

## 3. 线程池参数

既然有了刚刚对线程池工作原理对概述，这些参数就很容易理解了：

### 3.1 corePoolSize

 核心池大小，既然如前原理部分所述。需要注意的是在初创建线程池时线程不会立即启动，直到有任务提交才开始启动线程并逐渐时线程数目达到corePoolSize。若想一开始就创建所有核心线程需调用prestartAllCoreThreads方法。

### 3.2 maximumPoolSize

池中允许的最大线程数。需要注意的是当核心线程满且阻塞队列也满时才会判断当前线程数是否小于最大线程数，并决定是否创建新线程。

### 3.3 keepAliveTime 

当线程数大于核心时，多于的空闲线程最多存活时间

### 3.4 unit 

keepAliveTime 参数的时间单位。

### 3.5 workQueue

当线程数目超过核心线程数时用于保存任务的队列。主要有3种类型的BlockingQueue可供选择：无界队列，有界队列和同步移交。将在下文中详细阐述。从参数中可以看到，此队列仅保存实现Runnable接口的任务。 别看这个参数位置很靠后，但是真的很重要，因为楼主的坑就因这个参数而起，这些细节有必要仔细了解清楚。

### 3.6 threadFactory

执行程序创建新线程时使用的工厂。

### 3.7 handler

阻塞队列已满且线程数达到最大值时所采取的饱和策略。java默认提供了4种饱和策略的实现方式：中止、抛弃、抛弃最旧的、调用者运行。将在下文中详细阐述。

## 4. 可选择的阻塞队列BlockingQueue详解

再重复一下新任务进入时线程池的执行策略：
1、如果运行的线程少于corePoolSize，则 Executor始终首选添加新的线程，而不进行排队。（如果当前运行的线程小于corePoolSize，则任务根本不会存入queue中，而是直接运行）

2、如果运行的线程大于等于 corePoolSize，则 Executor始终首选将请求加入队列，而不添加新的线程。
如果无法将请求加入队列，则创建新的线程，除非创建此线程超出 maximumPoolSize，在这种情况下，任务将被拒绝。
主要有3种类型的BlockingQueue：

### 4.1 无界队列

队列大小无限制，常用的为无界的LinkedBlockingQueue，使用该队列做为阻塞队列时要尤其当心，当任务耗时较长时可能会导致大量新任务在队列中堆积最终导致OOM。阅读代码发现，Executors.newFixedThreadPool 采用就是 LinkedBlockingQueue，而楼主踩到的就是这个坑，当QPS很高，发送数据很大，大量的任务被添加到这个无界LinkedBlockingQueue 中，导致cpu和内存飙升服务器挂掉。

### 4.2 有界队列

常用的有两类，一类是遵循FIFO原则的队列如ArrayBlockingQueue与有界的LinkedBlockingQueue，另一类是优先级队列如PriorityBlockingQueue。PriorityBlockingQueue中的优先级由任务的Comparator决定。
使用有界队列时队列大小需和线程池大小互相配合，线程池较小有界队列较大时可减少内存消耗，降低cpu使用率和上下文切换，但是可能会限制系统吞吐量。

在我们的修复方案中，选择的就是这个类型的队列，虽然会有部分任务被丢失，但是我们线上是排序日志搜集任务，所以对部分对丢失是可以容忍的。

### 4.3 同步移交队列

如果不希望任务在队列中等待而是希望将任务直接移交给工作线程，可使用SynchronousQueue作为等待队列。SynchronousQueue不是一个真正的队列，而是一种线程之间移交的机制。要将一个元素放入SynchronousQueue中，必须有另一个线程正在等待接收这个元素。只有在使用无界线程池或者有饱和策略时才建议使用该队列。


## 5. 可选择的饱和策略RejectedExecutionHandler详解

JDK主要提供了4种饱和策略供选择。4种策略都做为静态内部类在ThreadPoolExcutor中进行实现。

### 5.1 AbortPolicy中止策略

该策略是默认饱和策略。

```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
 } 
```

使用该策略时在饱和时会抛出RejectedExecutionException（继承自RuntimeException），调用者可捕获该异常自行处理。


### 5.2 DiscardPolicy抛弃策略

```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
}
```

如上所示，什么都不做。

### 5.3 DiscardOldestPolicy抛弃旧任务策略

```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
} 
```

如代码，先将阻塞队列中的头元素出队抛弃，再尝试提交任务。如果此时阻塞队列使用PriorityBlockingQueue优先级队列，将会导致优先级最高的任务被抛弃，因此不建议将该种策略配合优先级队列使用。

### 5.4 CallerRunsPolicy调用者运行

```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
} 
```

既不抛弃任务也不抛出异常，直接运行任务的run方法，换言之将任务回退给调用者来直接运行。使用该策略时线程池饱和后将由调用线程池的主线程自己来执行任务，因此在执行任务的这段时间里主线程无法再提交新任务，从而使线程池中工作线程有时间将正在处理的任务处理完成。


## 6. Java提供的四种常用线程池解析

既然楼主踩坑就是使用了 JDK 的默认实现，那么再来看看这些默认实现到底干了什么，封装了哪些参数。简而言之 Executors 工厂方法Executors.newCachedThreadPool() 提供了无界线程池，可以进行自动线程回收；Executors.newFixedThreadPool(int) 提供了固定大小线程池，内部使用无界队列；Executors.newSingleThreadExecutor() 提供了单个后台线程。

详细介绍一下上述四种线程池。

### 6.1 newCachedThreadPool

```java
public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
} 
```

在newCachedThreadPool中如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
初看该构造函数时我有这样的疑惑：核心线程池为0，那按照前面所讲的线程池策略新任务来临时无法进入核心线程池，只能进入 SynchronousQueue中进行等待，而SynchronousQueue的大小为1，那岂不是第一个任务到达时只能等待在队列中，直到第二个任务到达发现无法进入队列才能创建第一个线程？
这个问题的答案在上面讲SynchronousQueue时其实已经给出了，要将一个元素放入SynchronousQueue中，必须有另一个线程正在等待接收这个元素。因此即便SynchronousQueue一开始为空且大小为1，第一个任务也无法放入其中，因为没有线程在等待从SynchronousQueue中取走元素。因此第一个任务到达时便会创建一个新线程执行该任务。

### 6.2 newFixedThreadPool

```java
 public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
 }
```

看代码一目了然了，线程数量固定，使用无限大的队列。

### 6.3 newScheduledThreadPool

创建一个定长线程池，支持定时及周期性任务执行。

```java
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
}
```

在来看看ScheduledThreadPoolExecutor（）的构造函数

 ```java
public ScheduledThreadPoolExecutor(int corePoolSize) {
       super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
             new DelayedWorkQueue());
   }
 ```

ScheduledThreadPoolExecutor的父类即ThreadPoolExecutor，因此这里各参数含义和上面一样。值得关心的是DelayedWorkQueue这个阻塞对列，在上面没有介绍，它作为静态内部类就在ScheduledThreadPoolExecutor中进行了实现。简单的说，DelayedWorkQueue是一个无界队列，它能按一定的顺序对工作队列中的元素进行排列。

### 6.4 newSingleThreadExecutor

创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。

```java
public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1));
 } 
```

首先new了一个线程数目为 1 的ScheduledThreadPoolExecutor，再把该对象传入DelegatedScheduledExecutorService中，看看DelegatedScheduledExecutorService的实现代码：

```java
DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
            super(executor);
            e = executor;
} 
```

在看看它的父类

```java
DelegatedExecutorService(ExecutorService executor) { 
           e = executor; 
}
```

其实就是使用装饰模式增强了ScheduledExecutorService（1）的功能，不仅确保只有一个线程顺序执行任务，也保证线程意外终止后会重新创建一个线程继续执行任务。

## 7. 为什么禁止使用 Executors 创建线程池?

<img src='https://oscimg.oschina.net/oscnet/up-9d0200e116259f64c5485a1bbf0d4265c31.png'>

### 7.1 实验证明Executors缺陷

```java
 public class ExecutorsDemo {
        private static ExecutorService executor = Executors.newFixedThreadPool(15);

        public static void main(String[] args) {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                executor.execute(new SubThread());
            }
        }
    }

    class SubThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
        //do nothing }
            }
        }


    }
```

通过指定JVM参数:-Xmx8m -Xms8m运行以上代码，会抛出OOM:

```java
Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
at java.util.concurrent.LinkedBlockingQueue.offer(LinkedBlockingQueue. java:416)
at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor. java:1371)
at com.hollis.ExecutorsDemo.main(ExecutorsDemo.java:16)
```

以上代码指出，ExecutorsDemo.java 的第 16 行，就是代码中的 execu- tor.execute(new SubThread());。

### 7.2 Executors 为什么存在缺陷

通过上面的例子，我们知道了 Executors 创建的线程池存在 OOM 的风险，那 么到底是什么原因导致的呢?我们需要深入 Executors 的源码来分析一下。

其实，在上面的报错信息中，我们是可以看出蛛丝马迹的，在以上的代码中其实 已经说了，真正的导致 OOM 的其实是 LinkedBlockingQueue.offer 方法。

如果读者翻看代码的话，也可以发现，其实底层确实是通过 LinkedBlock- ingQueue 实现的:

```java
    public static ExecutorService newFixedThreadPool(int nThreads) { return new ThreadPoolExecutor(nThreads, nThreads,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
```

如果读者对 Java 中的阻塞队列有所了解的话，看到这里或许就能够明白原因了。

Java 中 的 BlockingQueue 主 要 有 两 种 实 现， 分 别 是 ArrayBlockingQ- ueue 和 LinkedBlockingQueue。

- ArrayBlockingQueue 是一个用数组实现的有界阻塞队列，必须设置容量。
- LinkedBlockingQueue 是一个用链表实现的有界阻塞队列，容量可以选择 进行设置，不设置的话，将是一个无边界的阻塞队列，最大长度为 Integer.MAX_ VALUE。  

这里的问题就出在:不设置的话，将是一个无边界的阻塞队列，最大长度为Integer.MAX_VALUE。也就是说，如果我们不设置 LinkedBlockingQueue 的 容量的话，其默认容量将会是 Integer.MAX_VALUE。

而 newFixedThreadPool 中创建 LinkedBlockingQueue 时，并未指定容 量。此时，LinkedBlockingQueue 就是一个无边界队列，对于一个无边界队列 来说，是可以不断的向队列中加入任务的，这种情况下就有可能因为任务过多而导 致内存溢出问题。

上面提到的问题主要体现在newFixedThreadPool 和 newSingleThreadExecutor 两个工厂方法上，并不是说 newCachedThreadPool 和 newScheduledThreadPool 这两个方法就安全了，这两种方式创建的最大线程数可能是 Integer.MAX_VALUE，而创建这么多线程，必然就有可能导致 OOM。


### 7.3 创建线程池的正确姿势

避免使用 Executors 创建线程池，主要是避免使用其中的默认实现，那么我们 可以自己直接调用 ThreadPoolExecutor 的构造函数来自己创建线程池。在创建的 同时，给 BlockQueue 指定容量就可以了。

```java
    private static ExecutorService executor = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue(10));
```

这种情况下，一旦提交的线程数超过当前可用线程数时，就会抛出 java.util. concurrent.RejectedExecutionException，这是因为当前线程池使用的队列 是有边界队列，队列已经满了便无法继续处理新的请求。但是异常(Exception)总比 发生错误(Error)要好。

除了自己定义 ThreadPoolExecutor 外。还有其他方法。这个时候第一时间 就应该想到开源类库，如 apache 和 guava 等。

作者推荐使用 guava 提供的 ThreadFactoryBuilder 来创建线程池。

```java
    public class ExecutorsDemo {
        private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("demo-pool-%d").build();
        private static ExecutorService pool = new ThreadPoolExecutor(5, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new
                ThreadPoolExecutor. AbortPolicy());
        public static void main(String[] args) {
            for (int i = 0; i < Integer.MAX_VALUE; i++) { pool.execute(new SubThread());
            } }
    }
```

通过上述方式创建线程时，不仅可以避免 OOM 的问题，还可以自定义线程名 称，更加方便的出错的时候溯源。

## 8、ThreadPoolExecutor源码解析

### 8.1 ThreadPoolExecutor类重要属性

```java
//这个属性是用来存放 当前运行的worker数量以及线程池状态的
//int是32位的，这里把int的高3位拿来记录线程池状态的标志位,后29位拿来记录当前运行worker的数量
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
//存放任务的阻塞队列
private final BlockingQueue<Runnable> workQueue;
//worker的集合,用set来存放
private final HashSet<Worker> workers = new HashSet<Worker>();
//历史达到的worker数最大值
private int largestPoolSize;
//当队列满了并且worker的数量达到maxSize的时候,执行具体的拒绝策略
private volatile RejectedExecutionHandler handler;
//超出coreSize的worker的生存时间
private volatile long keepAliveTime;
//常驻worker的数量
private volatile int corePoolSize;
//最大worker的数量,一般当workQueue满了才会用到这个参数
private volatile int maximumPoolSize;
```

### 8.2 ThreadPoolExecutor定义的内部状态

```java
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
private static final int COUNT_BITS = Integer.SIZE - 3;
private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

// runState is stored in the high-order bits
private static final int RUNNING    = -1 << COUNT_BITS;
private static final int SHUTDOWN   =  0 << COUNT_BITS;
private static final int STOP       =  1 << COUNT_BITS;
private static final int TIDYING    =  2 << COUNT_BITS;
private static final int TERMINATED =  3 << COUNT_BITS;

// Packing and unpacking ctl
private static int runStateOf(int c)     { return c & ~CAPACITY; }
private static int workerCountOf(int c)  { return c & CAPACITY; }
private static int ctlOf(int rs, int wc) { return rs | wc; }

```

其中AtomicInteger变量ctl的功能非常强大: 利用低29位表示线程池中线程数，通过高3位表示线程池的运行状态:

- RUNNING: -1 << COUNT_BITS，即高3位为111，该状态的线程池会接收新任务，并处理阻塞队列中的任务；
- SHUTDOWN:  0 << COUNT_BITS，即高3位为000，该状态的线程池不会接收新任务，但会处理阻塞队列中的任务；
- STOP :  1 << COUNT_BITS，即高3位为001，该状态的线程不会接收新任务，也不会处理阻塞队列中的任务，而且会中断正在运行的任务；
- TIDYING :  2 << COUNT_BITS，即高3位为010, 所有的任务都已经终止；
- TERMINATED:  3 << COUNT_BITS，即高3位为011, terminated()方法已经执行完成

### 8.3 execute源码解析

```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
        
    int c = ctl.get();
    if (workerCountOf(c) < corePoolSize) {
    		// 添加任务队列并创建核心线程，然后在执行
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }
  	// 线程池是运行状态并且任务成功添加到队列
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
				// recheck and if necessary 回滚到入队操作前，即倘若线程池shutdown状态，就remove(command)
        //如果线程池没有RUNNING，成功从阻塞队列中删除任务，执行reject方法处理任务
        if (! isRunning(recheck) && remove(command))
            reject(command);
        else if (workerCountOf(recheck) == 0)
        		// 使用普通线程运行任务
            addWorker(null, false);
    }
	   // 往线程池中创建新的线程失败，则reject任务
     else if (!addWorker(command, false))
        reject(command);
}
```

**思考🤔**      **为什么需要double check线程池的状态?**

在多线程环境下，线程池的状态时刻在变化，而ctl.get()是非原子操作，很有可能刚获取了线程池状态后线程池状态就改变了。判断是否将command加入workque是线程池之前的状态。倘若没有double check，万一线程池处于非running状态(在多线程环境下很有可能发生)，那么command永远不会执行。

**addWorker方法**

先看看addWorker方法的注释，方便我们理解源码

```java
Checks if a new worker can be added with respect to current
     * pool state and the given bound (either core or maximum). If so,
     * the worker count is adjusted accordingly, and, if possible, a
     * new worker is created and started, running firstTask as its
     * first task. This method returns false if the pool is stopped or
     * eligible to shut down. It also returns false if the thread
     * factory fails to create a thread when asked.  If the thread
     * creation fails, either due to the thread factory returning
     * null, or due to an exception (typically OutOfMemoryError in
     * Thread.start()), we roll back cleanly.
     *
     
     // 大概翻译如下：
     //1、首先先检查线程池的状态和线程数量是否超过界限
		 //2、如果可以创建的话，需要更新任务的数量，然后运行任务
		 //3、有两种情况这个方法会返回false，线程池stop状态或者shut down状态 
		 //还有一种情况是共创创建线程失败
     4、不管是发生什么异常，例如线程工厂返回null或者是发生了OOM,直接回滚
     
     * @param firstTask the task the new thread should run first (or
     * null if none). Workers are created with an initial first task
     * (in method execute()) to bypass queuing when there are fewer
     * than corePoolSize threads (in which case we always start one),
     * or when the queue is full (in which case we must bypass queue).
     * Initially idle threads are usually created via
     * prestartCoreThread or to replace other dying workers.
     *
     * @param core if true use corePoolSize as bound, else
     * maximumPoolSize. (A boolean indicator is used here rather than a
     * value to ensure reads of fresh values after checking other pool
     * state).
     * @return true if successful
```

**下面是源码**

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // Check if queue empty only if necessary.
        if (rs >= SHUTDOWN &&
            ! (rs == SHUTDOWN &&
               firstTask == null &&
               ! workQueue.isEmpty()))
            return false;

        for (;;) {
            int wc = workerCountOf(c);
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
          	// cas修改c的值
            if (compareAndIncrementWorkerCount(c))
                break retry;
            c = ctl.get();  // Re-read ctl
            if (runStateOf(c) != rs)
                continue retry;
            // else CAS failed due to workerCount change; retry inner loop
        }
    }

    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
        w = new Worker(firstTask);
        final Thread t = w.thread;
        if (t != null) {
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // Recheck while holding lock.
                // Back out on ThreadFactory failure or if
                // shut down before lock acquired.
                int rs = runStateOf(ctl.get());

                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();
                    workers.add(w);
                    int s = workers.size();
                  	// largestPoolSize记录的最大workers长度
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            if (workerAdded) {
               // 线程启动，执行任务(Worker.thread(firstTask).start());
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}
```

线程start之后会执行如下run方法：

```java
 /** Delegates main run loop to outer runWorker  */
        public void run() {
            runWorker(this);
}
```

**下面是runWorker方法**

```java
final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }
```

**通过getTask方法从阻塞队列中获取等待的任务，如果队列中没有任务，getTask方法会被阻塞并挂起，不会占用cpu资源；**

**getTask()方法源码如下**

下面来看一下getTask()方法，这里面涉及到keepAliveTime的使用，从这个方法我们可以看出先吃池是怎么让超过corePoolSize的那部分worker销毁的。

```java
private Runnable getTask() {
    boolean timedOut = false; // Did the last poll() time out?

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // Check if queue empty only if necessary.
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }

        int wc = workerCountOf(c);

        // Are workers subject to culling?
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

        if ((wc > maximumPoolSize || (timed && timedOut))
            && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;
        }

        try {
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                workQueue.take();
            if (r != null)
                return r;
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```

allowCoreThreadTimeOut为false，线程即使空闲也不会被销毁；倘若为ture，在keepAliveTime内仍空闲则会被销毁。

如果线程允许空闲等待而不被销毁timed == false，workQueue.take任务:

如果阻塞队列为空，当前线程会被挂起等待；当队列中有任务加入时，线程被唤醒，take方法返回任务，并执行；

如果线程不允许无休止空闲timed == true, workQueue.poll任务: 

如果在keepAliveTime时间内，阻塞队列还是没有任务，则返回null；
