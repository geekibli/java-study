---
title: canal同步mysql数据到elasticsearch
toc: true
date: 2021-07-05 11:26:50
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---

## 首先安装elk

推荐大家到elasic中文社区去下载 👉 [【传送】](https://elasticsearch.cn/)  
⚠️ elastcisearch | logstash | kibana 的版本最好保持一直，否则会出现很多坑的，切记！

安装ELK的步骤这里就不做介绍了，可以查看 👉 【TODO】

## 下载安装canal-adapter

canal github传送门 👉  [【Alibaba Canal】](https://github.com/alibaba/canal)

### canal-client 模式

可以参照canal给出的example项目和[官方文档](https://github.com/alibaba/canal/wiki/ClientExample)给出的例子来测试

#### 依赖配置
```xml
<dependency>
    <groupId>com.alibaba.otter</groupId>
    <artifactId>canal.client</artifactId>
    <version>1.1.0</version>
</dependency>
```

#### 创建maven项目
保证canal-server 已经正确启动 👈  然后启动下面服务，操作数据库即可看到控制台的日志输出；
```java
package com.redtom.canal.deploy;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author gaolei
 * @Date 2021/6/30 2:57 下午
 * @Version 1.0
 */
@Slf4j
@Component
class CanalClient implements InitializingBean {

    private final static int BATCH_SIZE = 1000;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("***.***.***.***", 11111),
                "example", "canal", "canal");
        try {
            //打开连接
            connector.connect();
            //订阅数据库表,全部表
            connector.subscribe(".*\\..*");
            //回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拿
            connector.rollback();
            while (true) {
                // 获取指定数量的数据
                Message message = connector.getWithoutAck(BATCH_SIZE);
                //获取批量ID
                long batchId = message.getId();
                //获取批量的数量
                int size = message.getEntries().size();
                //如果没有数据
                if (batchId == -1 || size == 0) {
                    try {
                        //线程休眠2秒
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //如果有数据,处理数据
                    printEntry(message.getEntries());
                }
                //进行 batch id 的确认。确认之后，小于等于此 batchId 的 Message 都会被确认。
                connector.ack(batchId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connector.disconnect();
        }
    }

    /**
     * 打印canal server解析binlog获得的实体类信息
     */
    private static void printEntry(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                //开启/关闭事务的实体类型，跳过
                continue;
            }
            //RowChange对象，包含了一行数据变化的所有特征
            //比如isDdl 是否是ddl变更操作 sql 具体的ddl sql beforeColumns afterColumns 变更前后的数据字段等等
            CanalEntry.RowChange rowChage;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }
            //获取操作类型：insert/update/delete类型
            CanalEntry.EventType eventType = rowChage.getEventType();
            //打印Header信息
            log.info("headers:{} ", String.format("================》; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));
            //判断是否是DDL语句
            if (rowChage.getIsDdl()) {
                log.info("================》;isDdl: true,sql: {}", rowChage.getSql());
            }
            //获取RowChange对象里的每一行数据，打印出来
            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                //如果是删除语句
                if (eventType == CanalEntry.EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                    //如果是新增语句
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                    //如果是更新的语句
                } else {
                    //变更前的数据
                    log.info("------->; before");
                    printColumn(rowData.getBeforeColumnsList());
                    //变更后的数据
                    log.info("------->; after");
                    printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            log.info(" {} :  {}   update= {}", column.getName(), column.getValue(), column.getUpdated());
        }
    }
}

```


### canal-adapter 模式


adapter 配置文件如下
```yaml
server:
  port: 8081
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: non_null

canal.conf:
  mode: tcp #tcp kafka rocketMQ rabbitMQ
  flatMessage: true
  zookeeperHosts:
  syncBatchSize: 1
  batchSize: 1
  retries: 0
  timeout:
  accessKey:
  secretKey:
  consumerProperties:
    # canal tcp consumer
    canal.tcp.server.host: 172.25.101.75:11111
    canal.tcp.zookeeper.hosts:
    canal.tcp.batch.size: 500
    canal.tcp.username:
    canal.tcp.password:
  srcDataSources:
    defaultDS:
      url: jdbc:mysql://xxxx:pppp/database?useUnicode=true
      username: root
      password: pwd
  canalAdapters:
  - instance: example # canal instance Name or mq topic name
    groups:
    - groupId: g1
      outerAdapters:
      - name: logger
      - name: es7
        hosts: 172.25.101.75:9300 # 127.0.0.1:9200 for rest mode
        properties:
          mode: transport # or rest
#          # security.auth: test:123456 #  only used for rest mode
          cluster.name: my-application
#        - name: kudu
#          key: kudu
#          properties:
#            kudu.master.address: 127.0.0.1 # ',' split multi address
```

我的elasticsearch是7.10.0版本的 
`application.yml  bootstrap.yml  es6  es7  hbase  kudu  logback.xml  META-INF  rdb`
所以：👇
```shell
cd es7
biz_order.yml  customer.yml  mytest_user.yml
vim customer.yml
```

customer.yml 配置文件如下：
```yaml
dataSourceKey: defaultDS
destination: example
groupId: g1
esMapping:
  _index: customer
  _id: id
  relations:
    customer_order:
      name: customer
  sql: "select t.id, t.name, t.email from customer t"
  etlCondition: "where t.c_time>={}"
  commitBatch: 3000
```

#### 创建表结构

```sql
CREATE TABLE `customer` (
  `id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `order_id` int(11) DEFAULT NULL,
  `order_serial` varchar(255) DEFAULT NULL,
  `order_time` datetime DEFAULT NULL,
  `customer_order` varchar(255) DEFAULT NULL,
  `c_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```


#### 创建索引
```json
PUT customer
{
  "mappings":{
      "properties":{
        "id": {
          "type": "long"
        },
        "name": {
         "type": "text"
        },
        "email": {
          "type": "text"
        },
        "order_id": {
          "type": "long"
        },
        "order_serial": {
          "type": "text"
        },
        "order_time": {
          "type": "date"
        },
        "customer_order":{
          "type":"join",
          "relations":{
            "customer":"order"
          }
        }
      }
  }
}
```


#### 测试canal-adapter同步效果

##### 创建一条记录

```shell
2021-07-05 11:50:53.725 [pool-3-thread-1] DEBUG c.a.o.canal.client.adapter.es.core.service.ESSyncService - DML: {"data":[{"id":1,"name":"1","email":"1","order_id":1,"order_serial":"1","order_time":1625457046000,"customer_order":"1","c_time":1625457049000}],"database":"redtom_dev","destination":"example","es":1625457053000,"groupId":"g1","isDdl":false,"old":null,"pkNames":[],"sql":"","table":"customer","ts":1625457053724,"type":"INSERT"}
Affected indexes: customer
```
Elastcisearch 效果
```json
{
  "took" : 0,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 1,
      "relation" : "eq"
    },
    "max_score" : 1.0,
    "hits" : [
      {
        "_index" : "customer",
        "_type" : "_doc",
        "_id" : "1",
        "_score" : 1.0,
        "_source" : {
          "name" : "1",
          "email" : "1",
          "customer_order" : {
            "name" : "customer"
          }
        }
      }
    ]
  }
}
```
##### 修改数据
```shell
2021-07-05 11:54:36.402 [pool-3-thread-1] DEBUG c.a.o.canal.client.adapter.es.core.service.ESSyncService - DML: {"data":[{"id":1,"name":"2","email":"2","order_id":2,"order_serial":"2","order_time":1625457046000,"customer_order":"2","c_time":1625457049000}],"database":"redtom_dev","destination":"example","es":1625457275000,"groupId":"g1","isDdl":false,"old":[{"name":"1","email":"1","order_id":1,"order_serial":"1","customer_order":"1"}],"pkNames":[],"sql":"","table":"customer","ts":1625457276401,"type":"UPDATE"}
Affected indexes: customer
```
Elastcisearch 效果
<img src="https://oscimg.oschina.net/oscnet/up-afadff417c35ecb74811967d8e1da10f134.png" width=700 height=400>


##### 删除一条数据

```
2021-07-05 11:56:51.524 [pool-3-thread-1] DEBUG c.a.o.canal.client.adapter.es.core.service.ESSyncService - DML: {"data":[{"id":1,"name":"2","email":"2","order_id":2,"order_serial":"2","order_time":1625457046000,"customer_order":"2","c_time":1625457049000}],"database":"redtom_dev","destination":"example","es":1625457411000,"groupId":"g1","isDdl":false,"old":null,"pkNames":[],"sql":"","table":"customer","ts":1625457411523,"type":"DELETE"}
Affected indexes: customer
```
Elastcisearch 效果
<img src="https://oscimg.oschina.net/oscnet/up-7f49aeda581a547bf528b55bad6d0af27de.png" width=700 height=400>



## 参考资料
> - [使用canal client-adapter完成mysql到es数据同步教程(包括全量和增量)](https://blog.csdn.net/puhaiyang/article/details/100171395)
> - [es 同步问题 #1514 Github issue](https://github.com/alibaba/canal/issues/1514)
> - [canal v1.1.4 文档手册](https://www.bookstack.cn/read/canal-v1.1.4/f5f4adc96eefafe4.md )
> - [Sync es](https://www.bookstack.cn/read/canal-v1.1.4/63ebbe076bc97d0f.md)
