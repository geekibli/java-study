---
title: Flink-UDF函数类
toc: true
date: 2021-08-12 14:36:09
tags: Apache Flink
categories:
---



## 函数类

比如说我们常用的MapFunction，FilterFunction，ProcessFunction等，每一步操作都基本上都对应一个Function。

```java
public static class MyFlatMapper implements FlatMapFunction<String, Tuple2<String, Integer>> {
    public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) {
      // 首先按照空格分词
      String[] words = s.split(" ");
      // 遍历所有的word 包装成二元组输出
      for (String word : words) {
        collector.collect(new Tuple2<String, Integer>(word, 1));
      }
    }
}
```

⚠️ 简单滚动聚合函数，比如sum，max是不需要Function。

**好处：**

1、通用型强，可复用

2、可抽象方法，代码简洁

## 匿名函数

不需要单独定义Function，直接在Stream的操作中直接实现，效果和上面👆的完全一样。

```java
DataStream<SensorReading> dataStream = unionStream.map(new MapFunction<String, SensorReading>() {
    @Override
    public SensorReading map(String s) throws Exception {
      String[] strings = s.split(",");
      return new SensorReading(strings[0], new Double(strings[1]), new Double(strings[2]));
    }
});
```

## 富函数

‘‘富函数’’是DataStream API 提供的一个函数类的接口，所有Flink函数类都有其Rich版本，它与常规函数的不同在于，可以获取运行环境的上下文，并包含一些声明周期方法，所以可以实现更加复杂的功能。

`RichMapFunction`，`RichFlatMapFunction`等等

Rich Function有一个生命周期的概念，典型的生命周期方法有 👇

`open()` 方法是rich function的初始化方法，当一个算子比如map被调用之前被调用。

`close()`方法是生命周期中最后一个被调用的方法，做一些清理工作。

> 如果有多个分区的话，每个分区的open方法和close方法都会执行一次

`getRuntimeContext()`获取运行时上下文。

```java
public class RichFunctionDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> dataSource = env.socketTextStream("localhost", 9999);

        DataStream<SensorReading> dataStream = dataSource.map(new MapFunction<String, SensorReading>() {
            @Override
            public SensorReading map(String s) throws Exception {
                String[] strings = s.split(",");
                return new SensorReading(strings[0], new Long(strings[1]), new Double(strings[2]));
            }
        });

        DataStream<Tuple2<String, Integer>> stream = dataStream.map(new MyMapFunction());
        stream.print();
        env.execute();
    }
		
  	// 富函数是抽象类，这里要用继承
    public static class MyMapFunction extends RichMapFunction<SensorReading, Tuple2<String, Integer>> {

        @Override
        public void open(Configuration parameters) throws Exception {
            System.err.println("invoke open");
            // 一般定义状态，或者链接数据库操作
            super.open(parameters);
        }

        @Override
        public Tuple2<String, Integer> map(SensorReading sensorReading) throws Exception {
            RuntimeContext runtimeContext = this.getRuntimeContext();
            System.err.println("runtimeContext.getTaskName() : " + runtimeContext.getTaskName());
            return new Tuple2<>(sensorReading.getSersorId(), runtimeContext.getIndexOfThisSubtask());
        }

        @Override
        public void close() throws Exception {
            super.close();
            System.err.println("invoke close method");
        }
    }
}
```

