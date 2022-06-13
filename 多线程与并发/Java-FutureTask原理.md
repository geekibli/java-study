---
title: Java-FutureTask原理
toc: true
date: 2021-07-28 17:58:47
tags: 多线程
categories: [Develop Lan,Java,多线程与并发]
---
# FutureTask

### Future方法介绍
```java
public interface Future<V> {
    
    // 取消任务 可中断的方式取消
    boolean cancel(boolean mayInterruptIfRunning);
    
    // 判断任务是否处于取消状态 
    boolean isCancelled();
    
    // 判断异步任务是否执行完成      ==这里使用轮训的方式监听==
    boolean isDone();
    
    // 获取异步线程的执行结果，如果没有执行完成，则一直阻塞到有结果返回；
    V get() throws InterruptedException, ExecutionException;
    
    // 获取异步线程的执行结果，如果没有执行完成，则一直阻塞到设置的时间，有结果返回，没有结果则抛出异常；
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```

### 简单示范Callable&Future

（1）向线程池中提交任务的submit方法不是阻塞方法，而Future.get方法是一个阻塞方法  
（2）submit提交多个任务时，只有所有任务都完成后，才能使用get按照任务的提交顺序得到返回结果，所以一般需要使用future.isDone先判断任务是否全部执行完成，完成后再使用future.get得到结果。（也可以用get (long timeout, TimeUnit unit)方法可以设置超时时间，防止无限时间的等待）

```java
public class FutureTest implements Callable<Integer> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        System.err.println("start call method...");
        Thread.sleep(3000);
        return 1111;
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.err.println("main method start....");
        FutureTest futureTest = new FutureTest();
        Future1Test future1Test = new Future1Test();
        long time = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<String> future = executorService.submit(future1Test);
        if (!future.isDone()) {
            System.err.println("future not done !");
        }
        Future<Integer> future1 = executorService.submit(futureTest);
        // submit提交多个任务时，只有所有任务都完成后，才能使用get按照任务的提交顺序得到返回结果
        // 这里先提交了future1Test，休眠了4s, futureTest休眠了3s，但是等我们get到结果的时候，是消耗的4s时间的；
        System.err.println("cost time: " + (System.currentTimeMillis() - time));
        System.err.println("future: " + future.get());
        System.err.println("future1: " + future1.get());
        System.err.println("main method end....");
        executorService.shutdown();
    }
}
```
##### 执行结果
```java
main method start....
future not done !
// 说明了第一 get()方法是阻塞，第二线程池任务都执行完成之后，按提交任务顺序get结果返回值
cost time: 4
start call method...
future: future 2 test
future1: 1111
main method end....
```

##### 注意点
- 线程池执行任务有两种方式execute和submit，execute是不带返回值的，submit是有返回值的;
- main方法中可以不使用线程池，可以直接创建线程，调用start方法就可以，切记只有在演示代码的时候后。手动直接创建线程的方式还是不要用，因为一旦请求变多，则会创建无数的线程，线程数大于CPU核数，进而导致CPU频繁切换上下分进行调度，性能严重下降。
- 而且线程的数据是存放在内存中的，会占用大量的内存，增加垃圾回收的压力。严重的会发生OOM;
- 异常main方法中我们使用的是Future<String> future接收异步任务执行的放回结果，但实际上Future其实是一个interface，并不能接收返回结果的，那实际我们调用future.get()是，是实例了一个FutureTask对象来接受的；

### FutureTask讲解
下面主要针对Future的实现类FutureTask的几个重要方法展开

#### FutureTask继承关系
```java
public class FutureTask<V> implements RunnableFuture<V> {...}
// 下面是RunnableFuture接口的继承关系

public interface RunnableFuture<V> extends Runnable, Future<V> {
    /**
     * Sets this Future to the result of its computation
     * unless it has been cancelled.
     */
    void run();
}

```

> FutureTask 重要的成员变量

```java
/** The underlying callable; nulled out after running */
private Callable<V> callable;

/** The result to return or exception to throw from get() */
//任务执行结果或者任务异常
private Object outcome; // non-volatile, protected by state reads/writes

/** The thread running the callable; CASed during run() */
//执行任务的线程
private volatile Thread runner;

/** Treiber stack of waiting threads */
//等待节点，关联等待线程
private volatile WaitNode waiters;

private static final sun.misc.Unsafe UNSAFE;
//state字段的内存偏移量     这个在线程池执行任务的时候进行状态判断的时候会用到
private static final long stateOffset;
//runner字段的内存偏移量
private static final long runnerOffset;
//waiters字段的内存偏移量
private static final long waitersOffset;
```
> 定义任务的生命周期

```java
    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;
```
- NORMAL:指的是任务能够正常执行状态  
- EXCEPTIONAL：表示任务执行异常  
- CANCELLED：取消状态，之后的状态都表示任务取消或终端  

下面看一下FutureTask中几个重要的方法

#### 执行结果 | report方法    
> Returns result or throws exception for completed task.  
> 主要是上报异步任务执行的结果或返回任务执行发生的异常

```java
 /**
     * Returns result or throws exception for completed task.
     *
     * @param s completed state value
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)
            return (V)x;
        if (s >= CANCELLED)
            throw new CancellationException();
        throw new ExecutionException((Throwable)x);
    }
```

判断逻辑就是根据参数，也是是任务状态，根据不同的状态处理相应的逻辑。比如NORNAL状态，表示任务正常执行，直接返回结果就可以。如果状态大于CANCELLED，说明任务被取消或终端，会抛出CancellationException()；如果不是异常状态，则抛出ExecutionException；


#### 任务执行 |   run() 
> 执行异步任务

```java
   public void run() {
        // 如果状态 state 不是 NEW，或者设置 runner 值失败
        // 表示有别的线程在此之前调用 run 方法，并成功设置了 runner 值
        // 保证了只有一个线程可以运行 try 代码块中的代码。
        if (state != NEW ||
                !UNSAFE.compareAndSwapObject(this, runnerOffset,
                        null, Thread.currentThread()))
            return;
        //以上state值变更的由CAS操作保证原子性
        
        try {
            Callable<V> c = callable;
            //只有c不为null且状态state为NEW的情况
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    //调用callable的call方法，并获得返回结果
                    result = c.call();
                    //运行成功
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    //设置结果
                    set(result);
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }
```
核心逻辑就是调用Callable的call方法，==result=c.call();== 并且对任务执行的结果或异常信息进行处理；


#### 获取结果 | get() throws InterruptedException, ExecutionException
> 获取异步任务执行的结果或异常信息

```java
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)
            s = awaitDone(false, 0L);
        return report(s);
    }
```
get方法执行两个操作：  
- 判断任务的状态,如果没有执行完成，调用awaitDone方法
- 任务完成，调用我们上面说的report方法，返回任务执行结果


#### 任务阻塞 | awaitDone(boolean timed, long nanos)
> 等到任务执行完成 也是get方法阻塞特性的关键所在

```java
private int awaitDone(boolean timed, long nanos)
        throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;
        // CPU轮转
        for (;;) {
            // 如果线程中断了，将线程移除等待队列，抛出中断异常
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            // 如果任务状态大于完成，则直接返回；
            if (s > COMPLETING) {
                if (q != null)
                    q.thread = null;
                return s;
            }
            // 如果任务完成，但是返回值outcome还没有设置，可以先让出线程执行权，让其他线程执行
            else if (s == COMPLETING) // cannot time out yet
                Thread.yield();
            // 下面是任务还没有执行完成的状态，将线程添加到等待队列
            else if (q == null)
                q = new WaitNode();
            else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                     q.next = waiters, q);
            // 判断get方法是否设置了超时时间
            else if (timed) {
                nanos = deadline - System.nanoTime();
                // 如果超出设置的时间，线程移除等到队列
                if (nanos <= 0L) {
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            }
            // 没有设置超时时间，线程直接阻塞，直到任务完成
            else
                LockSupport.park(this);
        }
    }
    
```
主要执行步骤：

- 判断线程是否被中断，如果被中断了，就从等待的线程栈中移除该等待节点，然后抛出中断异常 
- 读取state,判断任务是否已经完成，如果已经完成或者任务已经取消，此时调用get方法的线程不会阻塞，会直接获取到结果或者拿到异常信息；  
- 如果s == COMPLETING，说明任务已经结束，但是结果还没有保存到outcome中，==此时线程让出执行权，给其他线程先执行；==   
- 如果任务没有执行完成，则需要创建等待节点，等待插入到阻塞队列  
- 判断queued，这里是将c中创建节点q加入队列头。使用Unsafe的CAS方法，对waiters进行赋值，waiters也是一个WaitNode节点，相当于队列头，或者理解为队列的头指针。通过WaitNode可以遍历整个阻塞队列  
- 然后判断超时时间，时间是在调用get方法的时候传输进来的，如果有超时时间，则设置超时时间，如果超出时间，则将线程移除等待队列；如果没有设置时间，则直接阻塞线程；  



#### 取消任务 |  cancel(boolean mayInterruptIfRunning)
```java
@Param mayInterruptIfRunning 是否中断
public boolean cancel(boolean mayInterruptIfRunning) {
    /*
     * 在状态还为NEW的时候，根据参数中的是否允许传递，
     * 将状态流转到INTERRUPTING或者CANCELLED。
     */
        if (!(state == NEW &&
              UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                  mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            return false;
        
        try {    // in case call to interrupt throws exception
            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null)
                        t.interrupt();
                } finally { // final state
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }
    
```


```java
private void finishCompletion() {
    for (WaitNode q; (q = waiters) != null;) {
        // 必须将栈顶CAS为null，否则重读栈顶并重试。
        if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
            // 遍历并唤醒栈中节点对应的线程。
            for (;;) {
                Thread t = q.thread;
                if (t != null) {
                    q.thread = null;
                    LockSupport.unpark(t);
                }
                WaitNode next = q.next;
                if (next == null)
                    break;
                // 将next域置为null，这样对GC友好。
                q.next = null; 
                q = next;
            }
            break;
        }
    }

    /*
     * done方法是暴露给子类的一个钩子方法。
     *
     * 这个方法在ExecutorCompletionService.QueueingFuture中的override实现是把结果加到阻塞队列里。
     * CompletionService谁用谁知道，奥秘全在这。
     */
    done();

    /* 
     * callable置为null主要为了减少内存开销,
     * 更多可以了解JVM memory footprint相关资料。
     */
    callable = null;
}
```


### Callable&Future使用场景

- 异步任务需要拿到返回值
- 多线程并发调用，顺序组装返回值，一些并发框架中会看到相应体现
- 还有一些分布式任务调度的场景，远程调用需要回填执行结果
- 还有很多通信框架中都有体现

- - -


### 参考资料
> (1) [future.get方法阻塞问题的解决，实现按照任务完成的先后顺序获取任务的结果](https://blog.csdn.net/qq_34562093/article/details/90209520)  
> (2) [Java多线程引发的性能问题以及调优策略](https://blog.csdn.net/luofenghan/article/details/78596950#无限制创建线程)  
> (3) [可取消的异步任务——FutureTask用法及解析](https://www.jianshu.com/p/55221d045f39)  
> (4) [FutureTask源码解读](https://www.cnblogs.com/micrari/p/7374513.html)


