---
title: flink 提交任务
toc: true
date: 2021-07-06 23:57:04
tags: Apache Flink
categories: Apache Flink
---

下面演示如何通过admin页面提交任务 👇

## 准备task jar

```java
public class StreamWordCount {  
    public static void main(String[] args) throws Exception {

        // 创建流处理执行环境
        StreamExecutionEnvironment env = StreamContextEnvironment.getExecutionEnvironment();

        // 从socket文本流读取数据
        DataStream<String> inputDataStream = env.socketTextStream("localhost", 7777);

        // 基于数据流进行转换计算
        DataStream<Tuple2<String,Integer>> resultStream = inputDataStream.flatMap(new WordCount.MyFlatMapper())
                .keyBy(0)
                .sum(1);

        resultStream.print();

        // 执行任务
        env.execute();
    }
}
```
执行`mvn install -DskipTest` 可以得到相应的jar

## admin提交jar

<img src='https://oscimg.oschina.net/oscnet/up-6f96aa46a523c9cd32f12177775b6d6fab9.png' width=900 height=450> 

提交完jar包之后，需要设置相关参数，这个根据自己的实际情况来设置，下面是参考样例： 

- Enter Class : com.ibli.flink.StreamWordCount  
  也就是程序入口，我们这是写了一个main方法，如果是程序的话，可以写对应bootstrap的启动类
- Program Arguments : --host localhost --port 7777 

点击 `submit`  之后查看提交的任务状态

## 查看任务

<img src='https://oscimg.oschina.net/oscnet/up-f11b32799d87f90c6e78732db82f191be3b.png' width=900 height=450>  

可以看到是有两个任务，并且都是在执行状态；
点击一个任务，还可以查看任务详情信息，和一些其他的信息，非常全面；

<img src="https://oscimg.oschina.net/oscnet/up-9678050d3906aae9ccead23d02927d46851.png" width=900 height=450>

## 查看运行时任务列表
<img src='https://oscimg.oschina.net/oscnet/up-da8bf04ac90ae3c16095a6ebf25175b4452.png' width=900 height=350>


## 查看任务管理列表
<img src="https://oscimg.oschina.net/oscnet/up-a85746f93dc171f3d320a17c07294682bb3.png" width=900 height=450>

点击任务可以跳转到详情页面 👇 下面是执行日志 

<img src= 'https://oscimg.oschina.net/oscnet/up-a11c7ccc522c77d100d57a9c2a08ec9183b.png' width=900 height=500>

我们还可以看到任务执行的标准输出结果✅

<img src="https://oscimg.oschina.net/oscnet/up-3d990a6318fab3a11e7336fdcd271851fda.png" width=900 height=400>

## 任务源数据
通过nc 输入数据，由程序读取7777端口输入流并解析数据
```
gaolei:geekibli gaolei$ nc -lk 7777
hello java
hello flink
```

## 取消任务如下
<img src='https://oscimg.oschina.net/oscnet/up-05199d86b957e0adec4e4d91390a0aebbec.png' width=900 height=400> 

再次查看已完成任务列表 如下：

<img src='https://oscimg.oschina.net/oscnet/up-78c97d4c4aec1641a22165735764c54b969.png' width=900 height=400>