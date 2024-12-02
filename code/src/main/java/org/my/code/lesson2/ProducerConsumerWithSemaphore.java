package org.my.code.lesson2;

import java.util.concurrent.Semaphore;

public class ProducerConsumerWithSemaphore {

    private static Semaphore productAvailable = new Semaphore(0);
    static class Producer implements Runnable {
        @Override
        public void run() {
            // 生产产品的逻辑
            System.out.println("生产者生产了产品");
            // 产品生产完成后，释放一个许可
            productAvailable.release();
        }
    }

    static class Consumer implements Runnable {
        @Override
        public void run() {
            try {
                System.err.println("消费者等待。。。");
                // 消费者尝试获取许可，如果没有产品，会阻塞
                productAvailable.acquire();
                System.out.println("消费者消费了产品");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        Thread producerThread = new Thread(new Producer());
        Thread consumerThread = new Thread(new Consumer());
        consumerThread.start();
        Thread.sleep(1000);
        producerThread.start();
    }

}
