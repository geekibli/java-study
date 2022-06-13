---
title: Java多线程之ThreadLocal
toc: true
date: 2021-07-26 16:28:01
tags:  多线程
categories: [Develop Lan,Java,多线程与并发]
---


## ThreadLocalMap结构
<img src='https://user-gold-cdn.xitu.io/2020/7/26/1738b45487065b90?imageView2/0/w/1280/h/960/ignore-error/1'>

ThreadLocal底层实际上是依赖ThreadLocalMap来实现数据存储的，而ThreadLocalMap并不是真正的Map结构，它是基于ThreadLocalMap类中的内部类Entry类型的数组来实现。

```java
 static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
}
```

它的key其实就是当前threadlocal变量，继承了WeakReference。然后`Object value;`实际存储的值。

Thread类中有ThreadLocal类型的变量，如下👇

```java
    /* ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
    ThreadLocal.ThreadLocalMap threadLocals = null;

    /*
     * InheritableThreadLocal values pertaining to this thread. This map is
     * maintained by the InheritableThreadLocal class.
     */
		// 子线程可以获取到inheritableThreadLocals中的值
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
```



<font color=red>**为什么ThreadLocalMap使用ThreadLocal当作key而不是Thread呢？**</font>

1、因为一个线程可能会出现多个ThreadLocal变量，所以一个线程一个ThreadLocalMap（实质上是Entry数组）来存放多个ThreadLocal变量。

2、倘若是Thread作为key，就会变成多个线程共同访问一个ThreadLocalMap，就会变成线程公用的变量，那个每个线程中可能存储多个ThreadLocal变量的情况下，Entry可能真的用到map结果才可以实现呀

3、多个线程共同访问ThreadLocalMap，那么可能会出现ThreadLocalMap提及很大从而降低性能，而且何时销毁这个变量是无法确定的




## ThreadLocal set流程
<img src='https://user-gold-cdn.xitu.io/2020/7/26/1738b454879fe57d?imageView2/0/w/1280/h/960/ignore-error/1'>

下面是set方法的源码

```java
public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
}
```

set的操作就是向Entry数组中添加当前变量和值👇

用数组是因为，我们开发过程中可以一个线程可以有多个TreadLocal来存放不同类型的对象的，但是他们都将放到你当前线程的ThreadLocalMap里，所以肯定要数组来存。

至于Hash冲突，我们先看一下源码：

```java
private void set(ThreadLocal<?> key, Object value) {
           Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }
                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }
            tab[i] = new Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }
```

我从源码里面看到ThreadLocalMap在存储的时候会给每一个ThreadLocal对象一个threadLocalHashCode，在插入过程中，根据ThreadLocal对象的hash值，定位到table中的位置i，**int i = key.threadLocalHashCode & (len-1)**。

然后会判断一下：如果当前位置是空的，就初始化一个Entry对象放在位置i上；

```java
if (k == null) {
    replaceStaleEntry(key, value, i);
    return;
}
```

如果位置i不为空，如果这个Entry对象的key正好是即将设置的key，那么就刷新Entry中的value；

```java
if (k == key) {
    e.value = value;
    return;
}
```

如果位置i的不为空，而且key不等于entry，那就找下一个空位置，直到为空为止。



##### **ThreadLocal是如何引起内存泄漏的？**

如上面Entry的源码大家也看到了，`static class Entry extends WeakReference<ThreadLocal<?>>`

一个ThreadLocal变量存在两条引用：

1、ThreadLocalRef（栈）-》ThreadLocal（key）和 ThreadLocalMap -> ThreadLocal（key）

2、ThreadRef -> Thread -> ThreadLocalMap -> Entry -> Value

内存泄漏值的是ThreadLocal被回收了，ThreadLocalMap -> Entry -> key没有了指向，但是Entry的value的指向还在，长期占用内存，就可能会导致内存泄漏。



##### **如何避免内存泄漏**

ThreadLocal使用完之后及时remove



##### 为什么建议ThreadLocal变量为static类型的

ThreadLocal能够实现线程隔离的关键在于Thread持有自己的一个ThreadLocalMap变量，需要每一个ThreadLocal变量占用一个Entry就可以了，没有必要作为成员变量频繁创建，浪费内存空间。



## 参考资料
> - [Java面试必问：ThreadLocal终极篇 ](https://juejin.cn/post/6854573219916021767)
> - [【对线面试官】ThreadLocal](https://mp.weixin.qq.com/s?__biz=MzU4NzA3MTc5Mg==&mid=2247484118&idx=1&sn=9526a1dc0d42926dd9bcccfc55e6abc2&scene=21#wechat_redirect)

