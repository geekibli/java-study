package org.my.code.lesson2;

public class SynchronizedExample4 {

    public synchronized static void foo () throws InterruptedException {
        System.err.println(Thread.currentThread().getName() + "foofoo");
        Thread.sleep(2000);
    }

    public synchronized  void foo2 () throws InterruptedException {
        System.err.println(Thread.currentThread().getName() + "foo2foo2");
        Thread.sleep(2000);
    }


    public static void main(String[] args) {
        SynchronizedExample4 s1 = new SynchronizedExample4();
        SynchronizedExample4 s2= new SynchronizedExample4();


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

        Thread t3 = new Thread(()-> {
            try {
                s1.foo2();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t1.start();
        t2.start();
        t3.start();
    }

}
