# wait方法和sleep方法区别


## wait(long) 和 wait 有什么区别？

- wait方法必须和notify结合使用才能唤醒线程
- wait(long)在时间结束会自动唤醒


## wait 和 sleep

- wait方法是Object的成员方法，sleep方法是Thread的静态方法
- wait方法必须在同步方法中使用，sleep可以在任何地方使用
- wait会释放锁资源，sleep方法不会释放锁资源


**wait方法从哪里等待，唤醒时，从哪里开始执行**


## notify 和 notifyAll

- notify： 随机唤醒一个线程
- notifyAll：唤醒所有等待的线程


## 交替打印AB


```java
package com.ibli.test;

/**
 * @Author gaolei
 * @Date 2022/6/13 下午9:43
 * @Version 1.0
 */
public class PrintTest {
    static Printer p = new Printer();

    public static void main(String[] args) {
        new Thread(() -> {
            while (true) {
                try {
                    p.print1();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    p.print2();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    static class Printer {

        boolean flag = false;
        public synchronized void print1() throws InterruptedException {
            System.err.println("enter print1 " + flag);
            if (flag) {
                this.wait();
            }
            System.err.println("aaaaa");
            flag = true;
            this.notify();
        }

        public synchronized void print2() throws InterruptedException {
            System.err.println("enter print2 " + flag);
            if (!flag) {
                this.wait();
            }
            System.err.println("bbbb");
            flag = false;
            this.notify();
        }
    }

}

```


## 扩展 循环打印ABC

```java
package com.ibli.test;

/**
 * @Author gaolei
 * @Date 2022/6/13 下午10:25
 * @Version 1.0
 */
public class PrintABC {

    public static void main(String[] args) {
        final SPrinter p = new SPrinter();
        new Thread(()->{
            while(true) {

                try {
                    Thread.sleep(1000);
                    p.printA();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(()->{
            while(true) {
                try {
                    Thread.sleep(1000);
                    p.printB();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(()->{
            while(true) {
                try {
                    Thread.sleep(1000);
                    p.printC();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

class SPrinter {
    int flag = 0;


    public synchronized void printA () throws InterruptedException {
        this.notifyAll();
        if (flag % 3 == 0) {
            System.out.println("AAAAAAA");
            flag++;
            this.wait();
        }

    }

    public synchronized void printB () throws InterruptedException {
        this.notifyAll();
        if (flag % 3 == 1) {
            System.out.println("BBBBBB");
            flag++;
            this.wait();
        }

    }

    public synchronized void printC () throws InterruptedException {
        this.notifyAll();
        if (flag % 3 == 2) {
            System.out.println("CCCC");
           flag++;
           this.wait();
        }
    }

}

```


**还有一种写法：**


![](https://oscimg.oschina.net/oscnet/up-f893f0cd4cc29a2c69aa31fd6bfc83df577.png)



