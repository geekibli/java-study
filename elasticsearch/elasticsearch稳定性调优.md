---
title: elasticsearch稳定性调优
toc: true
date: 2021-07-15 15:22:49
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---

> [Elasticsearch性能优化总结](https://zhuanlan.zhihu.com/p/43437056)
> [Elasticsearch调优实践](https://cloud.tencent.com/developer/article/1158408)



## 稳定性调优

一 Linux参数调优

修改系统资源限制 👇
单用户可以打开的最大文件数量，可以设置为官方推荐的65536或更大些 `echo "* - nofile 655360" >>/etc/security/limits.conf`
单用户内存地址空间 `echo "* - as unlimited" >>/etc/security/limits.conf`
单用户线程数 `echo "* - nproc 2056474" >>/etc/security/limits.conf`
单用户文件大小 `echo "* - fsize unlimited" >>/etc/security/limits.conf`
单用户锁定内存 `echo "* - memlock unlimited" >>/etc/security/limits.conf`
单进程可以使用的最大map内存区域数量 `echo "vm.max_map_count = 655300" >>/etc/sysctl.conf`
TCP全连接队列参数设置， 这样设置的目的是防止节点数较多（比如超过100）的ES集群中，节点异常重启时全连接队列在启动瞬间打满，造成节点hang住，整个集群响应迟滞的情况 
`echo "net.ipv4.tcp_abort_on_overflow = 1" >>/etc/sysctl.conf `
`echo "net.core.somaxconn = 2048" >>/etc/sysctl.conf`
降低tcp alive time，防止无效链接占用链接数 `echo 300 >/proc/sys/net/ipv4/tcp_keepalive_time`


## ES节点配置

### jvm.options

-Xms和-Xmx设置为相同的值，`推荐设置为机器内存的一半左右，剩余一半留给系统cache使用`。

jvm内存建议不要低于2G，否则有可能因为内存不足导致ES无法正常启动或OOM
jvm建议不要超过32G，否则`jvm会禁用内存对象指针压缩技术`，造成内存浪费

### elasticsearch.yml

设置内存熔断参数，防止写入或查询压力过高导致OOM，具体数值可根据使用场景调整。 
`indices.breaker.total.limit: 30% `
`indices.breaker.request.limit: 6% `
`indices.breaker.fielddata.limit: 3%`


调小查询使用的cache，避免cache占用过多的jvm内存，具体数值可根据使用场景调整。 
`indices.queries.cache.count: 500 `
`indices.queries.cache.size: 5%`


单机多节点时，主从shard分配以ip为依据，分配到不同的机器上，避免单机挂掉导致数据丢失。 
`cluster.routing.allocation.awareness.attributes: ip `
`node.attr.ip: 1.1.1.1`

## ES使用方式

### 节点数较多的集群，增加专有master，提升集群稳定性

ES集群的元信息管理、index的增删操作、节点的加入剔除等集群管理的任务都是由master节点来负责的，master节点定期将最新的集群状态广播至各个节点。所以，master的稳定性对于集群整体的稳定性是至关重要的。当集群的节点数量较大时（比如超过30个节点），集群的管理工作会变得复杂很多。此时应该创建专有master节点，这些节点只负责集群管理，不存储数据，不承担数据读写压力；其他节点则仅负责数据读写，不负责集群管理的工作。

这样把集群管理和数据的写入/查询分离，互不影响，防止因读写压力过大造成集群整体不稳定。 将专有master节点和数据节点的分离，需要修改ES的配置文件，然后滚动重启各个节点。

> 专有master节点的配置文件（conf/elasticsearch.yml）增加如下属性：
> node.master: true 
> node.data: false 
> node.ingest: false 
> 数据节点的配置文件增加如下属性（与上面的属性相反）：
> node.master: false 
> node.data: true 
> node.ingest: true 



### 控制index、shard总数量

上面提到，ES的元信息由master节点管理，定期同步给各个节点，也就是每个节点都会存储一份。这个元信息主要存储在clusterstate中，如所有node元信息（indices、节点各种统计参数）、所有index/shard的元信息（mapping, location, size）、元数据ingest等。

ES在创建新分片时，要根据现有的分片分布情况指定分片分配策略，从而使各个节点上的分片数基本一致，此过程中就需要深入遍历clusterstate。当集群中的index/shard过多时，clusterstate结构会变得过于复杂，导致遍历clusterstate效率低下，集群响应迟滞。基础架构部数据库团队曾经在一个20个节点的集群里，创建了4w+个shard，`导致新建一个index需要60s+才能完成`。 当index/shard数量过多时，可以考虑从以下几方面改进：

- 降低数据量较小的index的shard数量
- 把一些有关联的index合并成一个index
- 数据按某个维度做拆分，写入多个集群
  
### Segment Memory优化

前面提到，ES底层采用Lucene做存储，而Lucene的一个index又由若干segment组成，每个segment都会建立自己的倒排索引用于数据查询。Lucene为了加速查询，为每个segment的倒排做了一层前缀索引，这个索引在Lucene4.0以后采用的数据结构是`FST` (Finite State Transducer)。Lucene加载segment的时候将其全量装载到内存中，加快查询速度。`这部分内存被称为SegmentMemory， 常驻内存，占用heap，无法被GC`。

前面提到，为利用JVM的对象指针压缩技术来节约内存，通常建议JVM内存分配不要超过32G。当集群的数据量过大时，SegmentMemory会吃掉大量的堆内存，而JVM内存空间又有限，此时就需要想办法降低SegmentMemory的使用量了，常用方法有下面几个：

- 定期删除不使用的index
- 对于不常访问的index，可以通过close接口将其关闭，用到时再打开
- 通过force_merge接口强制合并segment，降低segment数量
- 基础架构部数据库团队在此基础上，对FST部分进行了优化，释放高达40%的Segment Memory内存空间。


