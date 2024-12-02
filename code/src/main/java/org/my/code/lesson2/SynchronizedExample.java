package org.my.code.lesson2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SynchronizedExample {

    public void func1() {
        synchronized (this) {
            for (int i = 0; i < 10; i++) {
                System.out.println(i + " " + Thread.currentThread().getName());
            }
        }
    }

    public static void main(String[] args) {
        SynchronizedExample e1 = new SynchronizedExample();

        Thread t1 = new Thread(()->{
           e1.func1();
        });

        Thread t2 = new Thread(()->{
            e1.func1();
        });

        t1.start();
        t2.start();
    }
}
