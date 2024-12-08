package org.my.code.lesson3;

import java.util.concurrent.locks.ReentrantLock;

public class App {

    public static void main(String[] args) throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();
        lock.lock();

        lock.wait();

        lock.unlock();

    }
}
