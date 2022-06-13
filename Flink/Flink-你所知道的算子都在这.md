---
title: Flink-你所知道的算子都在这
toc: true
date: 2021-08-11 19:49:20
tags: Apache Flink
categories:
---



好了，看到这的话，Apache Flink基础概念啥的都应该了解差不多了吧，我们几天就See一下，平时用到的StreamApi中各式各样的算子都有什么，然后，我们搞点Demo试一下。

📒 我也是边学边实现一些Demo,这样呢可以方便自己理解，形成体系以后也应该能帮到别人快速学习吧。

这就是地址了👉 https://github.com/geekibli/flink-study  欢迎star！

**下面的Demo都是可以直接运行的**  如果是通过socket获取数据的话，确认现开启端口啊，我用的Mac系统，可以使用以下命令 `nc -lk 9999`

## 提供一个全局获取环境的方法

我们一个静态方法getEnv(), 不然每次还要new，挺麻烦的；

```java
private static StreamExecutionEnvironment getEnv() {
        return StreamExecutionEnvironment.getExecutionEnvironment();
}
```

## POJO类

```java
public class SensorReading implements Serializable {
		private String sersorId;
    private double timestamp;
    private double newtemp;
} 
```

## map

```java
public static void mapTest() throws Exception {
    StreamExecutionEnvironment env = getEnv();
    ArrayList<Integer> nums = Lists.newArrayList();
    nums.add(1);
    nums.add(2);
    nums.add(3);
    DataStreamSource<Integer> source = env.fromCollection(nums);
    SingleOutputStreamOperator<Integer> map = source.map(new MapFunction<Integer, Integer>() {
        @Override
        public Integer map(Integer integer) throws Exception {
            return integer * integer;
        }
    });
    map.print();
    env.execute();
}
```

## keyBy

```java
public static void keyByTest() throws Exception {
        StreamExecutionEnvironment env = getEnv();
        DataStreamSource<Tuple2<String, Integer>> source = env.fromElements(
                new Tuple2<String, Integer>("age", 1),
                new Tuple2<String, Integer>("name", 2),
                new Tuple2<String, Integer>("name", 3),
                new Tuple2<String, Integer>("name", 3));

        source.map(
                new MapFunction<Tuple2<String, Integer>, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                        Integer f1 = stringIntegerTuple2.f1;
                        stringIntegerTuple2.setField(f1 + 10, 1);
                        return stringIntegerTuple2;
                    }
                })
                .keyBy(1)
                .print();
        env.execute();
    }
```

## reduce

```java
public static void reduceTest() throws Exception {
        StreamExecutionEnvironment env = getEnv();
        env.fromElements(
                Tuple2.of(2L, 3L),
                Tuple2.of(1L, 5L),
                Tuple2.of(1L, 5L),
                Tuple2.of(1L, 7L),
                Tuple2.of(2L, 4L),
                Tuple2.of(1L, 5L))
                .keyBy(1)
                .reduce(new ReduceFunction<Tuple2<Long, Long>>() {
                    @Override
                    public Tuple2<Long, Long> reduce(Tuple2<Long, Long> longLongTuple2, Tuple2<Long, Long> t1) throws Exception {
                        return new Tuple2<Long, Long>(t1.f0, longLongTuple2.f1 + t1.f1);
                    }
                })
                .print();
        env.execute();
}
```

还有一个栗子🌰

```java
public static void reduce() throws Exception {
        StreamExecutionEnvironment env = getEnv();
        DataStreamSource<String> dataSource = env.socketTextStream("localhost", 9999);

        DataStream<SensorReading> dataStream = dataSource.map(new MapFunction<String, SensorReading>() {
            @Override
            public SensorReading map(String s) throws Exception {
                String[] strings = s.split(",");
                return new SensorReading(strings[0], new Long(strings[1]), new Double(strings[2]));
            }
        });

        DataStream<SensorReading> sersorId = dataStream.keyBy("sersorId")
                .reduce(new ReduceFunction<SensorReading>() {
                    @Override
                    public SensorReading reduce(SensorReading sensorReading, SensorReading t1) throws Exception {
                        String id = t1.getSersorId();
                        Double time = t1.getTimestamp();
                        return new SensorReading(id, time, Math.max(sensorReading.getNewtemp(), t1.getNewtemp()));
                    }
                });
        sersorId.print();
        env.execute();
}
```

## split|select

```java
public static void splitTest() throws Exception {
        StreamExecutionEnvironment env = getEnv();
        DataStreamSource<String> dataSource = env.socketTextStream("localhost", 9999);

        DataStream<SensorReading> dataStream = dataSource.map(new MapFunction<String, SensorReading>() {
            @Override
            public SensorReading map(String s) throws Exception {
                String[] strings = s.split(",");
                return new SensorReading(strings[0], new Double(strings[1]), new Double(strings[2]));
            }
        });

        SplitStream<SensorReading> split = dataStream.split(new OutputSelector<SensorReading>() {
            @Override
            public Iterable<String> select(SensorReading value) {
                return (value.getNewtemp() > 30) ? Collections.singleton("high") : Collections.singleton("low");
            }
        });

        DataStream<SensorReading> low = split.select("low");
        DataStream<SensorReading> high = split.select("high");
        DataStream<SensorReading> all = split.select("high", "low");

        // connect
        DataStream<Tuple2<String, Double>> highStream = high.map(new MapFunction<SensorReading, Tuple2<String, Double>>() {
            @Override
            public Tuple2<String, Double> map(SensorReading sensorReading) throws Exception {
                return new Tuple2<>(sensorReading.getSersorId(), sensorReading.getNewtemp());
            }
        });

        // 链接之后的stream
        ConnectedStreams<Tuple2<String, Double>, SensorReading> connect = highStream.connect(low);

        SingleOutputStreamOperator<Object> resultStream = connect.map(new CoMapFunction<Tuple2<String, Double>, SensorReading, Object>() {
            @Override
            public Object map1(Tuple2<String, Double> stringDoubleTuple2) throws Exception {
                return new Tuple3<>(stringDoubleTuple2.f0, stringDoubleTuple2.f0, "high temp warning");
            }

            @Override
            public Object map2(SensorReading sensorReading) throws Exception {
                return new Tuple2<>(sensorReading.getSersorId(), "normal temp");
            }
        });

        resultStream.print();
        env.execute();
    }
```

## connect | coMap

如上split方法下面我们是有操作connect的api的

## union

```java
public static void unionTest() throws Exception {
        // 必须是数据类型相同
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> firstStream = env.socketTextStream("localhost", 9999);
        DataStreamSource<String> secondStream = env.socketTextStream("localhost", 7777);


        DataStream<String> unionStream = firstStream.union(secondStream);
        DataStream<SensorReading> dataStream = unionStream.map(new MapFunction<String, SensorReading>() {
            @Override
            public SensorReading map(String s) throws Exception {
                String[] strings = s.split(",");
                return new SensorReading(strings[0], new Double(strings[1]), new Double(strings[2]));
            }
        });
        dataStream.print();
        env.execute();
}
```





// TODO 不断学习 不断补充







