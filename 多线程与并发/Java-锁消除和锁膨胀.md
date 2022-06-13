---
title: Java-锁消除和锁膨胀
toc: true
date: 2021-07-28 17:57:00
tags: 多线程
categories: [Develop Lan,Java,多线程与并发]
---
# 锁消除和锁粗化

## 锁消除 （lock eliminate）
```java
public void add(String str1,String str2){
    StringBuffer sb = new StringBuffer();
    sb.append(str1).append(str2);
}
```
我们都知道 StringBuffer 是线程安全的，因为它的关键方法都是被 synchronized 修饰过的，但我们看上面这段代码，我们会发现，sb 这个引用只会在 add 方法中使用，不可能被其它线程引用（因为是局部变量，栈私有），因此 sb 是不可能共享的资源，JVM 会自动消除 StringBuffer 对象内部的锁。


## 锁粗化 （lock coarsening）
```java
public String test(String str){
       int i = 0;
       StringBuffer sb = new StringBuffer():
       while(i < 100){
           sb.append(str);
           i++;
       }
       return sb.toString():
}
```

JVM 会检测到这样一连串的操作都对同一个对象加锁（while 循环内 100 次执行 append，没有锁粗化的就要进行 100  次加锁/解锁），此时 JVM 就会将加锁的范围粗化到这一连串的操作的外部（比如 while 虚幻体外），使得这一连串操作只需要加一次锁即可。


## 锁降级
https://www.zhihu.com/question/63859501

其实，只被VMThread访问，降级也就没啥意义了。所以可以简单认为锁降级不存在！