package org.my.code.lesson2;

import java.util.concurrent.Semaphore;

public class SemaphoreApp {

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName() + " acquired the resource");
                    // 模拟使用资源
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + " released the resource");
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }
}
