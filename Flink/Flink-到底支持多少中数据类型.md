---
title: Flink-到底支持多少种数据类型
toc: true
date: 2021-08-11 19:50:41
tags: Apache Flink
categories:
---



Flink支持所有的Java和Scala基础数据类型以及其包装类型

支持Tuple元组类型，Flink在Java API中定义了很多Tuple的实现类，从Tuple0 ~ Tuple25类型

Scala样例类 case class，对应Java中的POJO类对象(必须提供无参构造方法 get/set)

其他，比如 Arrays , Lists, Maps, Enums等都是支持的

