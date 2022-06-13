---
title: Flink-物理分区函数
toc: true
date: 2021-08-12 16:38:31
tags: Apache Flink
categories:
---

# Flink提供的8种分区函数

## GlobalPartitioner

该分区器会将所有的数据都发送到下游的某个算子实例(subtask id = 0)

```java
//  数据会被分发到下游算子的第一个实例中进行处理
    public static void global() throws Exception {
        StreamExecutionEnvironment env = getEnv().setMaxParallelism(8);
        DataStreamSource<String> dataStream = env.fromElements("hhh", "ggg", "fff", "ddd", "sss", "aaa", "qqq", "www");
        dataStream.flatMap(new RichFlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                collector.collect(s + "_**");
            }
        }).setParallelism(2).global().print("global : ");

        env.execute();
    }
```



## ShufflePartitioner

随机选择一个下游算子实例进行发送

```java
//数据会被随机分发到下游算子的每一个实例中进行
    public static void shuffle() throws Exception {
        StreamExecutionEnvironment env = getEnv();
        DataStreamSource<String> dataStream = env.fromElements("hhh", "ggg", "fff", "ddd", "sss", "aaa", "qqq", "www");
        DataStream<String> broadcast = dataStream.shuffle();
        broadcast.print("shuffle : ");
        env.execute();
    }
```

## BroadcastPartitioner

发送到下游所有的算子实例

```java
//广播分区会将上游数据输出到下游算子的每个实例中。适合于大数据集和小数据集做Jion的场景
    public static void broadcast() throws Exception {
        StreamExecutionEnvironment env = getEnv();
        DataStreamSource<String> dataStream = env.fromElements("hhh", "ggg", "fff", "ddd", "sss", "aaa", "qqq", "www");
        DataStream<String> broadcast = dataStream.broadcast();
        broadcast.print("broadcast : ");
        env.execute();
    }
```



## RebalancePartitioner

通过循环的方式依次发送到下游的task

```java
public static void rebalance() throws Exception {
        StreamExecutionEnvironment env = getEnv().setParallelism(4);
        DataStreamSource<String> dataStream = env.fromElements("hhh", "ggg", "fff", "ddd", "sss", "aaa", "qqq", "www");
        dataStream.map(new RichMapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                return s + "_**";
            }
        }).setParallelism(1).rebalance().print("rebalance : ");

        env.execute();
    }
```



## RescalePartitioner

```java
/**
     * 这种分区器会根据上下游算子的并行度，循环的方式输出到下游算子的每个实例。这里有点难以理解，假设上游并行度为 2，编号为 A 和 B。下游并行度为 4，编号为 1，2，3，4。那么 A 则把数据循环发送给 1 和 2，B 则把数据循环发送给 3 和 4。假设上游并行度为 4，编号为 A，B，C，D。下游并行度为 2，编号为 1，2。那么 A 和 B 则把数据发送给 1，C 和 D 则把数据发送给 2。
     */
    public static void rescale() throws Exception {
        StreamExecutionEnvironment env = getEnv().setParallelism(4);
        DataStreamSource<String> dataStream = env.fromElements("hhh", "ggg", "fff", "ddd", "sss", "aaa", "qqq", "www");
        dataStream.map(new RichMapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                return s + "_**";
            }
        }).setParallelism(1).rescale().print("rescale : ");

        env.execute();
    }
```



## ForwardPartitioner

发送到下游对应的第一个task，保证上下游算子并行度一致，即上有算子与下游算子是1:1的关系

```java
//用于将记录输出到下游本地的算子实例。它要求上下游算子并行度一样。简单的说，ForwardPartitioner用来做数据的控制台打印。
    public static void forward() throws Exception {
        StreamExecutionEnvironment env = getEnv().setParallelism(1);
        DataStreamSource<String> dataStream = env.fromElements("hhh", "ggg", "fff", "ddd", "sss", "aaa", "qqq", "www");
        DataStream<String> broadcast = dataStream.shuffle();
        broadcast.print("shuffle : ");
        env.execute();
    }
```

⚠️ 在上下游的算子没有指定分区器的情况下，如果上下游的算子并行度一致，则使用ForwardPartitioner，否则使用RebalancePartitioner，对于ForwardPartitioner，必须保证上下游算子并行度一致，否则会抛出异常。

## KeyByPartitioner

根据key的分组索引选择发送到相对应的下游subtask

```java
public static void keyBy() throws Exception {
        StreamExecutionEnvironment env = getEnv().setMaxParallelism(8);
        DataStreamSource<String> dataStream = env.fromElements("hhh", "hhh", "hhh", "hhh", "sss", "sss", "sss", "www");
        dataStream.flatMap(new RichFlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                collector.collect(s + "_**");
            }
        }).keyBy(String::toString).print("keyBy : ");

        env.execute();
    }
```



##  CustomPartitionerWrapper

通过Partitioner实例的partition方法(自定义的)将记录输出到下游。

```java
public static void custom() throws Exception {
        StreamExecutionEnvironment env = getEnv().setMaxParallelism(8);
        DataStreamSource<String> dataStream = env.fromElements("hhhh", "hhhss", "hhh", "hhh", "sss", "sss", "sss", "www");
        dataStream.flatMap(new RichFlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                collector.collect(s + "_**");
            }
        }).partitionCustom(new CustomPartitioner(),String::toString)
        .print("custom :");

        env.execute();
    }


    public static class CustomPartitioner implements Partitioner<String> {
        // key: 根据key的值来分区
        // numPartitions: 下游算子并行度
        @Override
        public int partition(String key, int numPartitions) {
            return key.length() % numPartitions;//在此处定义分区策略
        }
    }
```



[Flink的八种分区策略源码解读](https://blog.csdn.net/qq_36039236/article/details/112576091?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0.control&spm=1001.2101.3001.4242)

[Apache Flink 中文文档](https://ci.apache.org/projects/flink/flink-docs-release-1.13/zh/docs/dev/datastream/operators/overview/#custom-partitioning)

