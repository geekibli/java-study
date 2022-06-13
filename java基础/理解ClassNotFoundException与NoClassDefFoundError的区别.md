---
title: 理解ClassNotFoundException与NoClassDefFoundError的区别
toc: true
date: 2021-09-15 15:43:20
tags: 
- JAVA
categories:
---



## ClassNotFoundException

类加载时在指定路径下没有找到类文件



## NoClassDefFoundError

1、编译时存在某个类，但是运行时却找不到

> 编译完成之后，手动删除一个类的class文件

2、类根本就没有初始化成功，结果你还把它当做正常类使用，所以这事也不小，必须抛出ERROR告诉你不能再使用了





https://cloud.tencent.com/developer/article/1356060

https://blog.csdn.net/u012129558/article/details/81540804
