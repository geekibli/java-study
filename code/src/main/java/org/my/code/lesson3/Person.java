package org.my.code.lesson3;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class Person {

    volatile int age;

    /**
     * Exception in thread "main" java.lang.IllegalArgumentException: Must be volatile type
     * at java.util.concurrent.atomic.AtomicIntegerFieldUpdater$AtomicIntegerFieldUpdaterImpl.<init>(AtomicIntegerFieldUpdater.java:412)
     * at java.util.concurrent.atomic.AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerFieldUpdater.java:88)
     * at org.my.code.lesson3.Person.main(Person.java:36)
     */
//    int age;

    Person(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "age=" + age +
                '}';
    }


    /**
     * volatile int age;
     * <p>
     * AtomicIntegerFieldUpdater的核心操作是基于 CAS（Compare - and - Swap）机制。CAS 操作在执行时，需要比较内存中的当前值和预期值是否一致，然后决定是否进行更新。
     * 如果字段不是volatile的，那么 CAS 操作可能会基于一个过时的值进行比较。例如，一个线程在进行 CAS 操作之前，另一个线程已经修改了字段的值，
     * 但由于没有volatile保证可见性，进行 CAS 操作的线程可能仍然使用旧的值进行比较，导致更新失败或者出现数据不一致的情况。
     * 而volatile的内存语义确保了 CAS 操作能够基于最新的、正确的值来进行，从而保证了AtomicIntegerFieldUpdater原子操作的准确性和一致性。
     */
    public static void main(String[] args) {

        Person person = new Person(200);

        AtomicIntegerFieldUpdater<Person> ageUpdater = AtomicIntegerFieldUpdater.newUpdater(Person.class, "age");
        ageUpdater.compareAndSet(person, 200, 300);

        System.err.println(person);
    }
}
