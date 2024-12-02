package org.my.code.lesson2;

public class SynchronizedExample3 {

    public synchronized static void foo () throws InterruptedException {

        System.err.println(Thread.currentThread().getName() + "SSds");
        Thread.sleep(2000);
    }


    public static void main(String[] args) {
        SynchronizedExample3 s1 = new SynchronizedExample3();
        SynchronizedExample3 s2= new SynchronizedExample3();


        Thread t1 = new Thread(()->{
            try {
                s1.foo();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread t2 = new Thread(()-> {
            try {
                s2.foo();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t1.start();
        t2.start();
    }

}
