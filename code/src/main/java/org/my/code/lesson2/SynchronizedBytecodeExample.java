package org.my.code.lesson2;

/**
 * 对象头与锁状态标记
 * 在 Java 中，每个对象在内存中都有一个对象头（Object Header）。对象头的一部分用于存储对象的锁状态信息。对象头的长度在 32 位和 64 位虚拟机中有所不同，
 * 32 位虚拟机中对象头一般为 32 位，64 位虚拟机中对象头一般为 64 位。
 * 锁状态可以分为以下几种：
 * 无锁状态：对象头中的锁标志位为 01，表示对象目前没有被锁定，多个线程可以自由访问该对象的非同步方法或者代码块。
 * 偏向锁状态：当一个线程访问同步块并获取锁时，对象头的部分位会被设置为偏向锁标志（一般是 101），并且会记录获取偏向锁的线程 ID。
 * 在没有其他线程竞争的情况下，后续该线程再次进入同步块时，只需要检查对象头中的偏向标志和线程 ID 是否匹配自己，就可以直接进入同步块，无需进行同步操作。
 * 轻量级锁状态：当有另外一个线程尝试获取偏向锁时，偏向锁会膨胀为轻量级锁。轻量级锁状态下，对象头中的部分位用于存储指向线程栈帧中锁记录（Lock Record）的指针。
 * 线程通过 CAS（比较并交换）操作来获取轻量级锁，在竞争不激烈的情况下，线程可以通过自旋（不断尝试 CAS 操作）快速获取锁。
 * 重量级锁状态：当多个线程同时竞争轻量级锁，并且自旋一定次数后仍然无法获取锁时，轻量级锁会膨胀为重量级锁。此时，对象头中的锁标志位会被设置为 10，
 * 并且对象会与一个监视器（Monitor）关联。线程获取重量级锁时，会进入操作系统的阻塞队列等待，依赖于操作系统的互斥量（Mutex）来实现同步，这涉及到线程的阻塞和唤醒，开销较大。
 * <p>
 * Monitor（监视器）机制
 * 概念：synchronized的底层实现依赖于 Monitor。每个对象都有一个与之关联的 Monitor，Monitor 可以被看作是一个同步工具，用于控制多个线程对共享资源的访问。
 * 结构和工作原理：
 * Monitor 内部有一个计数器，初始值为 0。当一个线程进入一个synchronized块或者方法时，它会尝试获取对象的 Monitor 锁。如果计数器为 0，
 * 那么这个线程成功获取锁，将计数器加 1，表示这个线程拥有了这个对象的 Monitor 锁，可以执行同步代码。
 * 如果计数器不为 0，说明已经有线程拥有了这个锁。此时，如果是同一个线程再次进入同步块（因为synchronized是可重入的），计数器会再次加 1；
 * 如果是其他线程尝试进入同步块，这个线程会被阻塞，进入 Monitor 的等待队列（Wait - Set），直到拥有锁的线程释放锁，即计数器减为 0。
 * 当线程执行完同步块或者方法中的代码后，会释放锁，将计数器减 1。如果计数器减为 0，并且等待队列中有等待的线程，那么会唤醒一个等待线程，
 * 这个被唤醒的线程会尝试获取锁，将计数器加 1 后继续执行。
 * <p>
 * 字节码层面的实现
 * 当使用synchronized关键字修饰方法或者代码块时，在字节码层面会有相应的体现。
 * 修饰方法：如果synchronized修饰一个实例方法，在字节码中会有一个ACC_SYNCHRONIZED标志位添加到方法的访问标志（access flags）中。
 * 当方法被调用时，执行引擎会检查这个标志位，然后隐式地获取对象的锁（即调用方法的实例对象的 Monitor 锁），如果获取成功，就执行方法体，方法执行结束后自动释放锁。
 * 修饰代码块：对于synchronized修饰的代码块，字节码中会出现monitorenter和monitorexit指令。monitorenter指令用于获取对象的 Monitor 锁，
 * monitorexit指令用于释放锁。在代码块开始处会有monitorenter指令，在代码块结束处和异常处理路径（try - catch - finally结构中的finally块）会有monitorexit指令，
 * 以确保锁在任何情况下都能被正确释放。例如，以下是一个简单的代码及其字节码表示：
 */
public class SynchronizedBytecodeExample {

    public void synchronizedMethod() {
        synchronized (this) {
            // 同步代码块中的内容
        }
    }

    // 对应的字节码（部分关键指令）可能如下：

    /**
     *  0: aload_0
     *  1: dup
     *  2: astore_1
     *  3: monitorenter   // 获取对象的Monitor锁
     *  4: // 同步代码块中的字节码指令
     * ...
     *  10: aload_1
     *  11: monitorexit    // 释放对象的Monitor锁
     *  12: goto 20
     *  15: astore_2
     *  16: aload_1
     *  17: monitorexit    // 异常处理路径中的锁释放
     *  18: aload_2
     *  19: athrow
     *  20: return
     */

    // 从字节码中可以看到monitorenter和monitorexit指令对锁的获取和释放操作，确保了同步代码块的线程安全性。
}