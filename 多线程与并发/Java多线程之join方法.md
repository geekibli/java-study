

> 个人技术博客(IBLi)
[CSDN](https://blog.csdn.net/IBLiplus)      [Github](https://github.com/GaoLeiplus/javaDancer)
[掘金](https://juejin.im/user/4424090522228919)

![](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1600708864104&di=d036727b38da8177b009afc5268c3a1c&imgtype=0&src=http%3A%2F%2Ft8.baidu.com%2Fit%2Fu%3D2247852322%2C986532796%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D1280%26h%3D853)



参考资料
> [1、Java多线程中join方法的理解](https://www.iteye.com/blog/uule-1101994)  
> [2、Thread.join的作用和原理](https://blog.csdn.net/weichi7549/article/details/108449618)   
> [3、Thread.join的作用和原理](https://www.jianshu.com/p/fc51be7e5bc0)  


## join方法

```
join重载方法

1 join()
2 join(long millis)     //参数为毫秒
3 join(long millis,int nanoseconds)    //第一参数为毫秒，第二个参数为纳秒
```
### 功能演示
```
public class JoinDemo implements Runnable{
    public void run() {
        System.err.println("join thread demo ");
    }

    public static void main(String[] args) throws Exception {
        System.err.println("main thread start... ");
        Runnable r = new JoinDemo();
        Thread t = new Thread(r);
        t.setName("ibli joinTest ...");
        t.start();
//        t.join();
        System.err.println("main thread end... ");
    }
}
```
以上将t.join();注释掉，执行结果如下：
```
main thread start...
main thread end...
join thread demo
```
但是把注释去掉，结果如下：
```
main thread start...
join thread demo
main thread end...
```
这是一个非常简单的demo,效果是显而易见的。当main线程去调用t.join()是，会将自己当前线程阻塞，等到t线程执行完成到达完结状态，main线程才可以继续执行。

我们看一下join()设置超时时间的方法：

```
public class JoinDemo implements Runnable{
    public void run() {
        System.err.println("join thread demo ");
        try {
            // 线程睡眠4s
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<String> strings = null;
        System.err.println(strings.get(0));
    }

    public static void main(String[] args) throws Exception {
        System.err.println("main thread start... ");
        Runnable r = new JoinDemo();
        Thread t = new Thread(r);
        t.setName("ibli joinTest ...");
        t.start();
        // 但是主线程join的超时时间是1s
        t.join(1000);
        System.err.println("main thread end... ");
    }
}
```
执行效果：
```
main thread start...
join thread demo
main thread end...
Exception in thread "ibli joinTest ..." java.lang.NullPointerException
	at com.ibli.threadTest.api.JoinDemo.run(JoinDemo.java:14)
	at java.lang.Thread.run(Thread.java:748)
```

> 上面的执行结果可以看到，子线程设置了4s的超时时间，但是主线程在1秒超时后，并没有等待子线程执行完毕，就被唤醒执行后续操作了；这样的预期是否符合你的预期呢？
> 下面我们按照join的源码去分析吧！



### join方法原理

下面是join的原理图

<img src="http://chuantu.xyz/t6/741/1600691667x-1566676025.jpg" width = "400" height = "500" div align=right />




> join()源码

首先会调用join(0)方法，其实是join的重载方法；
```
public final void join() throws InterruptedException {
        join(0);
}
```
下面是join的核心实现：
```
public final synchronized void join(long millis)
    throws InterruptedException {
        long base = System.currentTimeMillis();
        long now = 0;

        // 首先校验参数是否合法
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        // 如果join方法没有参数，则相当于直接调用wait方法
        if (millis == 0) {
            while (isAlive()) {
                wait(0);
            }
        } else {
            while (isAlive()) {
                long delay = millis - now;
                if (delay <= 0) {
                    break;
                }
                wait(delay);
                now = System.currentTimeMillis() - base;
            }
        }
    }
```
下面是isAlive方法的源码
```
public final native boolean isAlive();
```
这是一个本地方法，作用是判断当前的线程是否处于活动状态。什么是活动状态呢？活动状态就是线程已经启动且尚未终止。线程处于正在运行或准备开始运行的状态，就认为线程是“存活”的。

- 这里有一个点要注意，join为什么阻塞的是主线程，而不是子线程呢？

- 不理解的原因是阻塞主线程的方法是放在previousThread这个实例作用，让大家误以为应该阻塞previousThread线程。实际上主线程会持有previousThread这个对象的锁，然后调用wait方法去阻塞，而这个方法的调用者是在主线程中的。所以造成主线程阻塞。

- 其实join()方法的核心在于wait(),在主线程中调用t.join()相当于在main方法中添加 new JoinDemo().wait();是一样的效果；在这里只不过是wait方法写在了子线程的方法中。

- 再次重申一遍，join方法的作用是在主线程阻塞，等在子线程执行完之后，由子线程唤醒主线程，再继续执行主线程调用t.join()方法之后的逻辑。


> 那么主线程是在什么情况下知道要继续执行呢？就是上面说的，主线程其实是由join的子线程在执行完成之后调用的notifyAll()方法，来唤醒等待的线程。怎么证明呢？

其实大家可以去翻看JVM的源码实现，Thread.cpp文件中，有一段代码：
```
void JavaThread::exit(bool destroy_vm, ExitType exit_type) {
  // Notify waiters on thread object. This has to be done after exit() is called
  // on the thread (if the thread is the last thread in a daemon ThreadGroup the
  // group should have the destroyed bit set before waiters are notified).
  ensure_join(this);
}
```
其中调用ensure_join方法
```
static void ensure_join(JavaThread* thread) {
  // We do not need to grap the Threads_lock, since we are operating on ourself.
  Handle threadObj(thread, thread->threadObj());
  assert(threadObj.not_null(), "java thread object must exist");
  ObjectLocker lock(threadObj, thread);
  // Ignore pending exception (ThreadDeath), since we are exiting anyway
  thread->clear_pending_exception();
  // Thread is exiting. So set thread_status field in  java.lang.Thread class to TERMINATED.
  java_lang_Thread::set_thread_status(threadObj(), java_lang_Thread::TERMINATED);
  // Clear the native thread instance - this makes isAlive return false and allows the join()
  // to complete once we've done the notify_all below
  //这里是清除native线程，这个操作会导致isAlive()方法返回false
  java_lang_Thread::set_thread(threadObj(), NULL);
  // 在这里唤醒等待的线程
  lock.notify_all(thread);
  // Ignore pending exception (ThreadDeath), since we are exiting anyway
  thread->clear_pending_exception();
}
```
在JVM的代码中，线程执行结束的最终调用了lock.notify_all(thread)方法来唤醒所有处于等到的线程



## 使用场景
- 比如我们使用Callable执行异步任务，需要在主线程处理任务的返回值时，可以调用join方法；
- 还有一些场景希望线程之间顺序执行的；










## join()方法与sleep()的比较

我们先说一下sleep方法：

- 让当前线程休眠指定时间。
- 休眠时间的准确性依赖于系统时钟和CPU调度机制。
- 不释放已获取的锁资源，如果sleep方法在同步上下文中调用，那么其他线程是无法进- 入到当前同步块或者同步方法中的。
- 可通过调用interrupt()方法来唤醒休眠线程。
- sleep是静态方法，可以在任何地方调用


相比与sleep方法
sleep是静态方法，而且sleep的线程不是放锁资源，而join方法是对象方法，并且在等待的过程中会释放掉对象锁；


> 关于join方法会释放对象锁，那到底是释放的那个对象的锁呢，可以参照 [关于join() 是否会释放锁的一些思考](https://www.cnblogs.com/lwmp/p/11805440.html)



## 一些关于join的面试题
> [一个Thread.join()面试题的思考](https://juejin.im/post/6844903977113354254)

</br>

> <p align="middle">山脚太拥挤 我们更高处见。</p>




