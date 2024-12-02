package org.my.code.lesson2;

public class PrintAB {

    private boolean flag = false;

    public synchronized void printA() throws InterruptedException {
        while (flag) {
            wait();
        }
        System.out.println("A");
        flag = true;
        notify();
    }

    public synchronized void printB() throws InterruptedException {
        while (!flag) {
            wait();
        }
        System.out.println("B");
        flag = false;
        notify();
    }


    static class ThreadA implements Runnable {
        private PrintAB printAB;

        public ThreadA(PrintAB printAB) {
            this.printAB = printAB;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    printAB.printA();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ThreadB implements Runnable {
        private PrintAB printAB;

        public ThreadB(PrintAB printAB) {
            this.printAB = printAB;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    printAB.printB();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        PrintAB printAB = new PrintAB();
        Thread threadA = new Thread(new ThreadA(printAB));
        Thread threadB = new Thread(new ThreadB(printAB));

        threadA.start();
        threadB.start();
    }
}
