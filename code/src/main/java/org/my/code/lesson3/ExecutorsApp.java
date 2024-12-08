package org.my.code.lesson3;

import java.util.concurrent.*;

public class ExecutorsApp {

    static class MyThread implements Runnable {

        int seq;

        public MyThread(int seq) {
            this.seq = seq;
        }

        @Override
        public void run() {
            System.err.println(Thread.currentThread().getName() + " " + seq);
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

        }
    }

    public static void main(String[] args) {

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
        fixedThreadPool.submit(new MyThread(1));
        fixedThreadPool.submit(new MyThread(2));


        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

        for (int i = 0; i < 1000 ; i++) {
            cachedThreadPool.submit(new MyThread(i + 1));
        }


        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new MyThread(1) , 3 ,  1 , TimeUnit.SECONDS);
    }
}
