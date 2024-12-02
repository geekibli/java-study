package org.my.code.lesson2;

public class ThreadSafeCounter {

    private int count;
    public synchronized void increment() {
        count++;
    }
    public synchronized int getCount() {
        return count;
    }


    static class CounterThread implements Runnable {
        private ThreadSafeCounter counter;
        public CounterThread(ThreadSafeCounter counter) {
            this.counter = counter;
        }
        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        ThreadSafeCounter counter = new ThreadSafeCounter();
        Thread thread1 = new Thread(new CounterThread(counter));
        Thread thread2 = new Thread(new CounterThread(counter));
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println("Count: " + counter.getCount());
    }
}
