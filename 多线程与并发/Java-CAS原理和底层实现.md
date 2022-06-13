---
title: Java-CAS原理和底层实现
toc: false
date: 2021-07-28 17:59:17
tags: 多线程
categories: [Develop Lan,Java,多线程与并发]
---

# CAS原理和底层实现


<img src="https://oscimg.oschina.net/oscnet/up-0ed0dcb929342035287eb09818f33416baa.png" width=550 height=400>

## 什么是CAS
CAS是（compare and swap） 的缩写，它能在不加锁的情况下，在多线程的环境下，保证多线程一致性的改动某一值；

## ABA问题
ABA问题是一个线程在CAS比较值和原来是否相等的过程中，别的线程修改过这个值，但是又改回去了，倒置当前线程比较的时候，发现是相等的，但是，中间是被修改过的；  

添加版本号，比较值的时候同时比较版本号


## CAS底层原理

### AtomicInteger:

```java
public final int incrementAndGet() {
        for (;;) {
            int current = get();
            int next = current + 1;
            if (compareAndSet(current, next))
                return next;
        }
    }

public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }
```

### Unsafe:

```java
public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);
```

运用：

```java
package com.mashibing.jol;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class T02_TestUnsafe {

    int i = 0;
    private static T02_TestUnsafe t = new T02_TestUnsafe();

    public static void main(String[] args) throws Exception {
        //Unsafe unsafe = Unsafe.getUnsafe();

        Field unsafeField = Unsafe.class.getDeclaredFields()[0];
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);

        Field f = T02_TestUnsafe.class.getDeclaredField("i");
        long offset = unsafe.objectFieldOffset(f);
        System.out.println(offset);

        boolean success = unsafe.compareAndSwapInt(t, offset, 0, 1);
        System.out.println(success);
        System.out.println(t.i);
        //unsafe.compareAndSwapInt()
    }
}
```

jdk8u: unsafe.cpp:

cmpxchg = compare and exchange

```c++
UNSAFE_ENTRY(jboolean, Unsafe_CompareAndSwapInt(JNIEnv *env, jobject unsafe, jobject obj, jlong offset, jint e, jint x))
  UnsafeWrapper("Unsafe_CompareAndSwapInt");
  oop p = JNIHandles::resolve(obj);
  jint* addr = (jint *) index_oop_from_field_offset_long(p, offset);
  return (jint)(Atomic::cmpxchg(x, addr, e)) == e;
UNSAFE_END
```

### jdk8u: atomic_linux_x86.inline.hpp **93行**

is_MP = Multi Processor  

```c++
inline jint     Atomic::cmpxchg    (jint     exchange_value, volatile jint*     dest, jint     compare_value) {
  int mp = os::is_MP();
  __asm__ volatile (LOCK_IF_MP(%4) "cmpxchgl %1,(%3)"
                    : "=a" (exchange_value)
                    : "r" (exchange_value), "a" (compare_value), "r" (dest), "r" (mp)
                    : "cc", "memory");
  return exchange_value;
}
```

jdk8u: os.hpp is_MP()

```c++
  static inline bool is_MP() {
    // During bootstrap if _processor_count is not yet initialized
    // we claim to be MP as that is safest. If any platform has a
    // stub generator that might be triggered in this phase and for
    // which being declared MP when in fact not, is a problem - then
    // the bootstrap routine for the stub generator needs to check
    // the processor count directly and leave the bootstrap routine
    // in place until called after initialization has ocurred.
    return (_processor_count != 1) || AssumeMP;
  }
```

jdk8u: atomic_linux_x86.inline.hpp

```c++
#define LOCK_IF_MP(mp) "cmp $0, " #mp "; je 1f; lock; 1: "
```

### 最终实现：

底层对应一个汇编指令「lock comxchg」，但是comxchg这条指令不是原子性的，他不能保证在比较的时候，别的线程会不会改变值；而保证线程安全的则是lock这条指令，lock这条指令在执行后面执行的时候锁定一个「北桥信号」，而不是采用纵线锁的方式；

## CAS在JDK中的实现
1、AtomitInteger
2、ConcurrentHashMap
