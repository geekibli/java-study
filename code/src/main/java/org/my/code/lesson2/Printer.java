package org.my.code.lesson2;

import java.util.concurrent.atomic.AtomicBoolean;

public class Printer {

    /**
     * 循环打印AB
     *
     * @param args xx
     */
    public static void main(String[] args) {

        Object object = new Object();
        AtomicBoolean flag = new AtomicBoolean(false);

        Thread t1 = new Thread(() -> {
            synchronized (object) {
                while (true) {
                    if (flag.get()) {
                        try {
                            object.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.err.println("AAAA");
                    flag.set(true);
                    object.notify();
                }
            }

        });


        Thread t2 = new Thread(() -> {
            synchronized (object) {
                while (true) {
                    if (!flag.get()) {
                        try {
                            object.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.err.println("BBBB");
                    flag.set(false);
                    object.notify();
                }
            }
        });

        t1.start();
        t2.start();


    }
}
