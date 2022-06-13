---
title: Java对象头
toc: true
date: 2021-07-28 17:59:59
tags: 多线程
categories: [Develop Lan,Java,多线程与并发]
---


# Java对象头


## JOL查看对象头信息
在项目中引入以下依赖
```
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.9</version>
</dependency>
```
写一个main方法，创建一个Object，然后打印对象信息：
```
 public static void main(String[] args) {
        Object object = new Object();
        System.out.println(ClassLayout.parseInstance(object).toPrintable());
}
```
打印结果如下：
```
java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           e5 01 00 f8 (11100101 00000001 00000000 11111000) (-134217243)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```
由此可知，new Object()在内存中占16个字节，组成部分8字节的markword+4字节的class point+4字节的对齐；

## Java对象在内存中的布局
<image src="https://oscimg.oschina.net/oscnet/up-056ef14e62f5dfde1a5af579dabeb6e4c2a.png" width=250 height=420>

- markword
> 存储sync锁标志，分代年龄等一些关键信息 8字节
- class pointer
> 指向当前对象所属类类型 4字节

**查看java命令默认带的参数命令：** java -XX:+PrintCommandLineFlags -version 
>-XX:InitialHeapSize=134217728 
-XX:MaxHeapSize=2147483648
-XX:+PrintCommandLineFlags 
-XX:+UseCompressedClassPointers 压缩类指针 4字节
-XX:+UseCompressedOops 普通对象指针压缩 4字节
-XX:+UseParallelGC

- instance data
> 寸尺当前对象的实例数据
- padding
> 对齐填充，当对象所占字节数不能被8整除之后，进行填充对齐。 目前的操作系统基本上都是64位的；


## 顺丰面试题，new Object()在内存中占多少个字节

1、如果创建的是空对象，没有实例数据
- 默认开启了class pointer指针压缩
> 8字节markword + 4字节class pointer + 4字节 padding
- 如果关闭了类指针压缩
> 8字节markword + 8字节class pointer

2、如果创建的对象有实力数据，如下对象：
 ```
    Person（int age , String name）
```
- 默认开启了class pointer指针压缩
> 8字节markword + 4字节class pointer + 4字节int + 4字节String + 4字节padding对齐



