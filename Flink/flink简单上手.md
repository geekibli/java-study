---
title: flink简单上手
toc: true
date: 2021-07-04 22:23:43
tags: Apache Flink
categories: Apache Flink
---

## mac 安装 flink

<b>1、执行 brew install apache-flink 命令</b>
```shell
gaolei:/ gaolei$ brew install apache-flink
Updating Homebrew...
==> Auto-updated Homebrew!  
Updated 1 tap (homebrew/services).
No changes to formulae.

==> Downloading https://archive.apache.org/dist/flink/flink-1.9.1/flink-1.9.1-bin-scala_2.11.tgz
######################################################################## 100.0%
🍺  /usr/local/Cellar/apache-flink/1.9.1: 166 files, 277MB, built in 15 minutes 29 seconds
```

<b>2、执行flink启动脚本</b>
```shell
/usr/local/Cellar/apache-flink/1.9.1/libexec/bin
./start-cluster.sh
```

## WordCount批处理Demo

### 创建maven项目，导入依赖
 注意自己的flink版本 👇👇
```xml
    <dependencies>
        <!-- https://mvnrepository.com/artifact/org.apache.flink/flink-streaming-java -->
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-streaming-java_2.12</artifactId>
            <version>1.9.1</version>
            <scope>provided</scope>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.apache.flink/flink-java -->
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-java</artifactId>
            <version>1.9.1</version>
        </dependency>

        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-clients_2.12</artifactId>
            <version>1.9.1</version>
        </dependency>
    </dependencies>
```

### 编写批处理程序

```java
 public static void main(String[] args) throws Exception {
        // 1、创建执行环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        // 2、读取文件数据
        String inputPath = "/Users/gaolei/Documents/DemoProjects/flink-start/src/main/resources/hello.txt";
        DataSource<String> dataSource = env.readTextFile(inputPath);
        // 对数据集进行处理 按照空格分词展开 转换成（word，1）二元组
        AggregateOperator<Tuple2<String, Integer>> result = dataSource.flatMap(new MyFlatMapper())
                // 按照第一个位置 -> word 分组
                .groupBy(0)
                .sum(1);
        result.print();
    }

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

### 准备数据源文件
```text
hello spark
hello world
hello java
hello flink
how are you
what is your name
```

### 执行结果
```text
(is,1)
(what,1)
(you,1)
(flink,1)
(name,1)
(world,1)
(hello,4)
(your,1)
(are,1)
(java,1)
(how,1)
(spark,1)
```




## flink 处理流式数据

1、通过 `nc -lk <port>` 打开一个socket服务，监听7777端口 用于模拟实时的流数据

2、java代码处理流式数据
```java
public class StreamWordCount {
    public static void main(String[] args) throws Exception {

        // 创建流处理执行环境
        StreamExecutionEnvironment env = StreamContextEnvironment.getExecutionEnvironment();

        // 设置并行度，默认值 = 当前计算机的CPU逻辑核数（设置成1即单线程处理）
        // env.setMaxParallelism(32);

        // 从文件中读取数据
//        String inputPath = "/tmp/Flink_Tutorial/src/main/resources/hello.txt";
//        DataStream<String> inputDataStream = env.readTextFile(inputPath);

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
4、在首次启动的时候遇到一个错误 ❌ 
`Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/flink/streaming/api/datastream/DataStream`
处理方法可参照 参考资料 👇


## 参考资料

> - [Exception in thread "main" java.lang.NoClassDefFoundError 解决方案](https://blog.csdn.net/VIP099/article/details/106457522)
> - https://flink.apache.org/zh/downloads.html
> - https://www.cnblogs.com/zlshtml/p/13796793.html
