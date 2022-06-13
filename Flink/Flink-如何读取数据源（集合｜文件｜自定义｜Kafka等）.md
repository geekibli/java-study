---
title: Flink-如何读取数据源（集合｜文件｜自定义｜Kafka等）
toc: true
date: 2021-08-11 19:50:11
tags: Apache Flink
categories:
---



## 读取文件

这里是以txt文件为例，实现WordCount，其他文件类型同理。

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





## 实现自定义数据源

需要自己写一个类，实现SourceFunction接口的run方法和cancle方法，注意⚠️，SourceFunction<SensorReading>的泛型类型必须要写上，不然会报错的。

```java
public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);
        DataStreamSource dataStreamSource = env.addSource(new MySourceFunction());
        dataStreamSource.print();
        env.execute();
    }

    // 实现自定义的source
    public static class MySourceFunction implements SourceFunction<SensorReading> {
        // 定义标识位 控制数据产生
        private boolean running = true;

        public void run(SourceContext ctx) throws Exception {
            // 定义各个随机数生成器
            HashMap<String, Double> sensorMap = new HashMap<String, Double>(10);
            for (int i = 0; i < 10; i++) {
                sensorMap.put("sensor_" + (i + 1), 60 + new Random().nextGaussian() * 20);
            }
            while (running) {
                for (String sensor : sensorMap.keySet()) {
                    double newtemp = sensorMap.get(sensor) + new Random().nextGaussian();
                    sensorMap.put(sensor, newtemp);
                    ctx.collect(new SensorReading(sensor, System.currentTimeMillis(), newtemp));
                }
                Thread.sleep(10000);
            }
        }

        public void cancel() {
            running = false;
        }
    }
```




