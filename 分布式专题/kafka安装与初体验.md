---
title: kafka安装与初体验
toc: true
date: 2021-07-28 16:43:24
tags: kafka
categories: [Distributed Dir, Kafka]
---

## Kafka的安装

### 安装zookeeper
```text
    brew install zookeeper
```
默认端口：2181  
默认安装位置：/usr/local/Cellar/zookeeper  
配置文件位置：/usr/local/etc/zookeeper  
日志文件位置：/usr/local/var/log/zookeeper/zookeeper.log  

### 启动zookeeper
```text
   nohup zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties &
```
![](https://oscimg.oschina.net/oscnet/up-d339f3f26756d950d6099483c5d033619bd.png)


### 安装Kafka
```text
    brew install kafka
```
默认端口：9092  
默认安装位置：/usr/local/Cellar/kafka  
配置文件位置：/usr/local/etc/kafka  
日志文件位置：/usr/local/var/lib/kafka-logs  

### 启动kafka
nohup kafka-server-start /usr/local/etc/kafka/server.properties &
![](https://oscimg.oschina.net/oscnet/up-6dcc2cdd724f403f0a5f1af44448cb5c523.png)

## 订阅发布Demo

### 创建一个Topic
```text
    kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
```
### 查看创建的Topic
```text
    kafka-topics --list --zookeeper localhost:2181
```

### 生产者生产消息
```text
    kafka-console-producer --broker-list localhost:9092 --topic test
```

### 消费者消费消息
```text
    kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic test1 --from-beginning
```
--from-beginning: 将从第一个消息开始接收


## SpringBoot集成Kafka 
**源码地址：https://gitee.com/IBLiplus/kafka-demo.git**
项目启动前按照上述安装启动步骤，在本地启动kafka.  
创建Maven项目，引入一下依赖  
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>pring-kafka</artifactId>
    <version>2.5.7.RELEASE</version>
</dependency>
```
添加如下配置，端口号可以自己定  
配置文件：  
```yml
server.port=9010
spring.kafka.bootstrap-servers= 127.0.0.1:9092
# 发生错误后，消息重发的次数。
spring.kafka.producer.retries= 0
#当有多个消息需要被发送到同一个分区时，生产者会把它们放在同一个批次里。该参数指定了一个批次可以使用的内存大小，按照字节数计算。
spring.kafka.producer.batch-size= 16384
# 设置生产者内存缓冲区的大小。
spring.kafka.producer.buffer-memory= 33554432
# 键的序列化方式
spring.kafka.producer.key-serializer= org.apache.kafka.common.serialization.StringSerializer
# 值的序列化方式
spring.kafka.producer.value-serializer = org.apache.kafka.common.serialization.StringSerializer
# acks=0 ： 生产者在成功写入消息之前不会等待任何来自服务器的响应。
# acks=1 ： 只要集群的首领节点收到消息，生产者就会收到一个来自服务器成功响应。
# acks=all ：只有当所有参与复制的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应。
spring.kafka.producer.acks= 1
# 自动提交的时间间隔 在spring boot 2.X 版本中这里采用的是值的类型为Duration 需要符合特定的格式，如1S,1M,2H,5D
spring.kafka.consumer.auto-commit-interval= 1S
# 该属性指定了消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该作何处理：
# latest（默认值）在偏移量无效的情况下，消费者将从最新的记录开始读取数据（在消费者启动之后生成的记录）
# earliest ：在偏移量无效的情况下，消费者将从起始位置读取分区的记录
spring.kafka.consumer.auto-offset-reset= earliest
# 是否自动提交偏移量，默认值是true,为了避免出现重复数据和数据丢失，可以把它设置为false,然后手动提交偏移量
spring.kafka.consumer.enable-auto-commit= false
# 键的反序列化方式
spring.kafka.consumer.key-deserializer= org.apache.kafka.common.serialization.StringDeserializer
# 值的反序列化方式
spring.kafka.consumer.value-deserializer= org.apache.kafka.common.serialization.StringDeserializer
# 在侦听器容器中运行的线程数。
spring.kafka.listener.concurrency= 5
#listner负责ack，每调用一次，就立即commit
spring.kafka.listener.ack-mode= manual_immediate
spring.kafka.listener.missing-topics-fatal= false
```
生产者生产消息：
```java
@Component
public class ProductDemo {
    Logger log = LoggerFactory.getLogger(ProductDemo.class);

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    //自定义topic
    public static final String TOPIC_TEST = "topic.test";
    public static final String TOPIC_GROUP1 = "topic.group1";
    public static final String TOPIC_GROUP2 = "topic.group2";

    public void send(String obj) {
        log.info("准备发送消息为：{}", obj);
        //发送消息
        ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(TOPIC_TEST, obj);
        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable throwable) {
                //发送失败的处理
                log.info(TOPIC_TEST + " - 生产者 发送消息失败：" + throwable.getMessage());
            }
            @Override
            public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                //成功的处理
                log.info(TOPIC_TEST + " - 生产者 发送消息成功：" + stringObjectSendResult.toString());
            }
        });
    }
}
```
消费消息
```java
@Component
public class ConsumerDemo {
    Logger log = LoggerFactory.getLogger(ConsumerDemo.class);

    @KafkaListener(topics = ProductDemo.TOPIC_TEST, groupId = ProductDemo.TOPIC_GROUP1)
    public void topic_test(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        Optional message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            Object msg = message.get();
            log.info("topic_test 消费了： Topic:" + topic + ",Message:" + msg);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = ProductDemo.TOPIC_TEST, groupId = ProductDemo.TOPIC_GROUP2)
    public void topic_test1(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        Optional message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            Object msg = message.get();
            log.info("topic_test1 消费了： Topic:" + topic + ",Message:" + msg);
            ack.acknowledge();
        }
    }
}
```
测试接口：
```text
@Resource
private ProductDemo productDemo;

@GetMapping("/kafka/test")
public void testKafka(){
    logger.info("start test");
    productDemo.send("hello kafka");
    logger.info("end test");
}
```


> <p align="middle">山脚太拥挤 我们更高处见。</p>