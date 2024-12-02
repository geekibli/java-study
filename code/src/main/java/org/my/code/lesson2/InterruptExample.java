package org.my.code.lesson2;

public class InterruptExample {

    private static class MyThread1 extends Thread {
        @Override
        public void run() {
            while (true) {

                try {
                    Thread.sleep(2000);
                    System.out.println("Thread run " + Thread.currentThread().isInterrupted());
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new RuntimeException("sssss");
                }
            }


        }
    }


    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new MyThread1();
        thread1.start();
//        thread1.interrupt();
        System.out.println("Main run  " + thread1.isInterrupted());

        Thread.sleep(6000);
        thread1.interrupt();

        System.out.println("Main run  " + thread1.isInterrupted());
    }


}
