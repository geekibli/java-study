package org.my.code.lesson2;

import java.util.concurrent.Semaphore;

public class PrintABWithSemaphore {

    private static Semaphore semaphoreA = new Semaphore(1);
    private static Semaphore semaphoreB = new Semaphore(0);

    public static void printA() {
        try {
            semaphoreA.acquire();
            System.out.println("A");
            semaphoreB.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void printB() {
        try {
            semaphoreB.acquire();
            System.out.println("B");
            semaphoreA.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main_1(String[] args) {
        Thread threadA = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                printA();
            }
        });
        Thread threadB = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                printB();
            }
        });
        threadA.start();
        threadB.start();
    }


    public static void main(String[] args) throws InterruptedException {
        semaphoreB.acquire();
        System.err.println("ssss"  + semaphoreB.availablePermits());
    }

}
