---
title: Java并发编程-多线程的发展和意义
toc: true
date: 2021-07-21 19:45:01
tags: 多线程
categories: [Develop Lan,Java,多线程与并发]
---

# 线程基础概念

## 什么是线程

线程是CPU执行任务的基本单位，一个进程中包含一个或者多个线程，一个进程内的多个线程共享进程的资源，每一个线程有自己的独立内存，是线程不共享的。

## 并行与并发
- 并行
   同一时刻，横向有多少个线程可以运行
- 并发
   系统和服务器同一时刻能够承受的并发线程

## 线程的特征
- 异步（不需要等待）
   比如说注册之后发送验证码，验证码的过程可以异步去做不需要客户去在注册接口等待这个时间；
- 并行（CPU核数）   


## Java中线程的使用
- 继承Thread
- 实现Runnalbe
- 实现Callable/Future

## 线程原理

```java
   public class ThreadDemo extend Thread{
      int a = 0;
      public void run(){
         int b = 0;
         b = a + 1;
      }
   }
```

执行start方法，其实是调用JVM相关的指令， thread.cpp

> java thread.start() -> cpp thread.start() -> os指令:create.thread    start.thread
操作系统层面会创建线程，线程创建之后，线程可以启动，（线程启动之后并不一定马上执行）这些线程统一有CPU调度算法来处理；决定那个线程分配给那个执行CPU；
CPU执行线程任务的时候，会调用run方法 -> cpp run方法  -> java  thread.run()

<img src='https://oscimg.oschina.net/oscnet/up-9147a0440e839bc9946fc87147e97b7c793.png' whith=600 height=380>

⚠️ CompletableFuture 异步回调通知，基于Future的优化 


# 线程的生命周期

线程创建，当线程中的指令执行完成之后，run（）结束 线程销毁
其他线程状态
- 等待状态 （sleep join wait）
- 锁阻塞状态 （blocked 竞争锁失败 park）

<img src="https://oscimg.oschina.net/oscnet/up-dc87c94066283689df31680050c67edd7b1.png" width=500 height=450>

```java
public class ThreadStatusDemo {

    public static void main(String[] args) {
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "time waitting").start();



        new Thread(()->{
            while (true){
                synchronized (ThreadStatusDemo.class){
                    try {
                        ThreadStatusDemo.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        },"waitting").start();



        new Thread(new BlockDemo(),"block demo 1").start();
        new Thread(new BlockDemo(),"block demo 2").start();
    }


    static class BlockDemo extends Thread{
        @Override
        public void run() {
            synchronized (BlockDemo.class){
                while (true){
                    try {
                        TimeUnit.SECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
```
`查看线程状态`
```shell 
jps -l
jstack pid
```

## 线程如何停止

interrupt() 停止线程
主动停止方式 -> run方法执行结束
被动停止方式

一般中断线程是在无法控制线程的情况下，比如线程wait ， 线程sleep ， 线程while(true) 
`Thread.currnetThread().isInterrupted()`

> stop方法停止线程 禁止使用 相当于kill线程 不友好

interrupt 功能
- 唤醒阻塞状态的线程
- 修改中断标志，false -> true

## 问题排查












