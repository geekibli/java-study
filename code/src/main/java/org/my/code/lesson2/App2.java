package org.my.code.lesson2;

public class App2 {

    static class Buffer {
        private String data;
        private boolean isEmpty = true;

        public synchronized void put(String data) throws InterruptedException {
            while (!isEmpty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(1000);
            System.err.println("put data: " + data);
            this.data = data;
            isEmpty = false;
            notify();
        }

        public synchronized String get() {
            while (isEmpty) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String result = data;
            isEmpty = true;
            notify();
            return result;
        }
    }

    static class Producer implements Runnable {
        private Buffer buffer;
        public Producer(Buffer buffer) {
            this.buffer = buffer;
        }
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    buffer.put("Data " + i);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class Consumer implements Runnable {
        private Buffer buffer;
        public Consumer(Buffer buffer) {
            this.buffer = buffer;
        }
        @Override
        public void run() {
            for (int i = 0; i < 30; i++) {

                System.out.println(buffer.get());
            }
        }
    }

    public static void main(String[] args) {
        Buffer buffer = new Buffer();
        Thread producerThread = new Thread(new Producer(buffer));
//        Thread producerThread1 = new Thread(new Producer(buffer));
        Thread consumerThread = new Thread(new Consumer(buffer));
        producerThread.start();
//        producerThread1.start();
        consumerThread.start();
    }

}
