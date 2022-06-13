---
title: elasticsearch基础api
toc: true
date: 2021-07-05 20:53:09
tags: elasticsearch
categories: [Elasticsearch, Getting Started]
---

## cat API

### 集群健康状态
GET _cat/health?v&pretty
```
epoch      timestamp cluster        status node.total node.data shards pri relo init unassign pending_tasks max_task_wait_time active_shards_percent
1625489855 12:57:35  my-application yellow          1         1     35  35    0    0       23             0                  -                 60.3%
```
或者直接在服务器上调用rest接口：
curl -XGET 'localhost:9200/_cat/health?v&pretty'
```
epoch      timestamp cluster       status node.total node.data shards pri relo init unassign pending_tasks max_task_wait_time active_shards_percent
1475247709 17:01:49  elasticsearch green           1         1      0   0    0    0        0             0                  -                100.0%
```

我们可以看到我们名为 my-application 的集群与 yellow 的 status。

无论何时我们请求集群健康，我们可以获得 green，yellow，或者 red 的 status。Green 表示一切正常（集群功能齐全）， yellow 表示所有数据可用，但是有些副本尚未分配（集群功能齐全），red 意味着由于某些原因有些数据不可用。注意，集群是 red，它仍然具有部分功能（例如，它将继续从可用的分片中服务搜索请求），但是您可能需要尽快去修复它，因为您已经丢失数据了。  

另外，从上面的响应中，我们可以看到共计 1 个 node（节点）和 0 个 shard（分片），因为我们还没有放入数据的。注意，因为我们使用的是默认的集群名称（elasticsearch），并且 Elasticsearch 默认情况下使用 unicast network（单播网络）来发现同一机器上的其它节点。有可能您不小心在您的电脑上启动了多个节点，然后它们全部加入到了单个集群。在这种情况下，你会在上面的响应中看到不止 1 个 node（节点）。


### 查看集群分布

GET _cat/nodes?v&pretty
```
ip         heap.percent ram.percent cpu load_1m load_5m load_15m node.role  master name
172.19.0.1           20          61  15    0.02    0.04     0.29 cdhilmrstw *      redtom-es-1
```


### 查看所有索引
GET _cat/indices?v&pretty
```
health status index                             uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   rd-logstash-2021.06.19            p5iej71MQVW12s2awNv8nw   1   1      61236            0     16.3mb         16.3mb
yellow open   demo_index                        k6VTs7tdS0ysot-rPwxG9A   1   1          1            0      5.5kb          5.5kb
green  open   kibana_sample_data_flights        A7c5DViGSISii8FA0dNlGw   1   0      13059            0      5.6mb          5.6mb
```


### 查看所有索引的数量
GET _cat/count?v&pretty
```
epoch      timestamp count
1625490245 13:04:05  838913
```


### 磁盘分配情况
GET _cat/allocation?v&pretty
```
shards disk.indices disk.used disk.avail disk.total disk.percent host       ip         node
    35      308.7mb    20.1gb    215.9gb    236.1gb            8 172.19.0.1 172.19.0.1 redtom-es-1
    23                                                                                 UNASSIGNED
```


### 查看shard情况
GET _cat/shards?v&pretty
```
index                             shard prirep state        docs   store ip         node
yj_visit_data                     0     p      STARTED         0    208b 172.19.0.1 redtom-es-1
yj_visit_data                     0     r      UNASSIGNED                           
demo_index                        0     p      STARTED         1   5.5kb 172.19.0.1 redtom-es-1
demo_index                        0     r      UNASSIGNED                           
rbtags                            0     p      STARTED         0    208b 172.19.0.1 redtom-es-1
.kibana_1                         0     p      STARTED       280  11.5mb 172.19.0.1 redtom-es-1
.kibana_task_manager_1            0     p      STARTED         5   5.8mb 172.19.0.1 redtom-es-1
```
yj_visit_data 设置了一个副本分区，但是没有副节点，所以节点状态显示未分配；



## 参考资料
> - [Elastic 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/5.0/cat.html)
> - [codingdict.com](https://doc.codingdict.com/elasticsearch/4/)
