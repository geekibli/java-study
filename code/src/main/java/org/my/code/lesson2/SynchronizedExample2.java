package org.my.code.lesson2;

public class SynchronizedExample2 {

    public void foo () throws InterruptedException {
        synchronized (SynchronizedExample2.class) {
            Thread.sleep(2000);
            System.err.println(Thread.currentThread().getName() + "SSds");
        }
    }


    public static void main(String[] args) {
        SynchronizedExample2 s1 = new SynchronizedExample2();
        SynchronizedExample2 s2= new SynchronizedExample2();


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
