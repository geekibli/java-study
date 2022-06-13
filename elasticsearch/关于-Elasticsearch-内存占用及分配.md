---
title: 关于 Elasticsearch 内存占用及分配
toc: true
date: 2021-07-08 10:39:16
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment ]
---

Elasticsearch 和 Lucene 对内存使用情况： 

<img src='https://nereuschen.github.io/2015/09/16/ElasticSearch%E5%86%85%E5%AD%98%E4%BD%BF%E7%94%A8%E5%88%86%E6%9E%90/es-cache.png' width=500 height=550>


Elasticsearch 限制的内存大小是 JAVA 堆空间的大小，不包括Lucene 缓存倒排索引数据空间。

Lucene 中的 倒排索引 segments 存储在文件中，为提高访问速度，都会把它加载到内存中，从而提高 Lucene 性能。所以建议至少留系统一半内存给Lucene。
`Node Query Cache` (负责缓存f ilter 查询结果)，每个节点有一个，被所有 shard 共享，filter query查询结果要么是 yes 要么是no，不涉及 scores 的计算。
集群中每个节点都要配置，默认为：indices.queries.cache.size:10%

`Indexing Buffer` 索引缓冲区，用于存储新索引的文档，当其被填满时，缓冲区中的文档被写入磁盘中的 `segments` 中。节点上所有 `shard` 共享。
缓冲区默认大小： indices.memory.index_buffer_size: 10%
如果缓冲区大小设置了百分百则 indices.memory.min_index_buffer_size 用于这是最小值，默认为 48mb。indices.memory.max_index_buffer_size 用于最大大小，无默认值。

`segments`
segments会长期占用内存，其初衷就是利用OS的cache提升性能。只有在Merge之后，才会释放掉标记为Delete的segments，释放部分内存。

`Shard Request Cache` 用于缓存请求结果，但之缓存request size为0的。比如说 hits.total, aggregations 和 suggestions.
默认最大为indices.requests.cache.size:1%

`Field Data Cache` 字段缓存重要用于对字段进行排序、聚合是使用。因为构建字段数据缓存代价昂贵，所以建议有足够的内训来存储。
`Fielddata` 是 「 延迟 」 加载。如果你从来没有聚合一个分析字符串，就不会加载 fielddata 到内存中，也就不会使用大量的内存，所以可以考虑分配较小的heap给Elasticsearch。因为heap越小意味着Elasticsearch的GC会比较快，并且预留给Lucene的内存也会比较大。。
如果没有足够的内存保存fielddata时，Elastisearch会不断地从磁盘加载数据到内存，并剔除掉旧的内存数据。剔除操作会造成严重的磁盘I/O，并且引发大量的GC，会严重影响Elastisearch的性能。

默认情况下Fielddata会不断占用内存，直到它触发了fielddata circuit breaker。
fielddata circuit breaker会根据查询条件评估这次查询会使用多少内存，从而计算加载这部分内存之后，Field Data Cache所占用的内存是否会超过indices.breaker.fielddata.limit。如果超过这个值，就会触发fielddata circuit breaker，abort这次查询并且抛出异常，防止OOM。

```
indices.breaker.fielddata.limit:60% (默认heap的60%)  (es7之后改成70%)
```
如果设置了indices.fielddata.cache.size，当达到size时，cache会剔除旧的fielddata。

> indices.breaker.fielddata.limit 必须大于 indices.fielddata.cache.size，否则只会触发fielddata circuit breaker，而不会剔除旧的fielddata。


## 配置Elasticsearch堆内存
Elasticsearch默认安装后设置的内存是 `1GB`，这是远远不够用于生产环境的。
有两种方式修改Elasticsearch的堆内存：

> 1. 设置环境变量：`export ES_HEAP_SIZE=10g` 在es启动时会读取该变量；
  2. 启动时作为参数传递给es： `./bin/elasticsearch -Xmx10g -Xms10g`


## 注意点

给es分配内存时要注意，至少要分配一半儿内存留给 Lucene。
分配给 es 的内存最好不要超过 32G ，因为如果堆大小小于 32 GB，JVM 可以利用指针压缩，这可以大大降低内存的使用：每个指针 4 字节而不是 8 字节。如果大于32G 每个指针占用 8字节，并且会占用更多的内存带宽，降低了cpu性能。

还有一点， 要关闭 `swap` 内存交换空间，禁用swapping。频繁的swapping 对服务器来说是致命的。
总结：给es JVM栈的内存最好不要超过32G，留给Lucene的内存越大越好，Lucene把所有的segment都缓存起来，会加快全文检索。


## 关闭交换区
这应该显而易见了，但仍然需要明确的写出来：把内存换成硬盘将毁掉服务器的性能，想象一下：涉及内存的操作是需要快速执行的。如果介质从内存变为了硬盘，一个10微秒的操作变成需要10毫秒。而且这种延迟发生在所有本该只花费10微秒的操作上，就不难理解为什么交换区对于性能来说是噩梦。

最好的选择是禁用掉操作系统的交换区。可以用以下命令：

```s
sudo swapoff -a
```
来禁用，你可能还需要编辑 `/etc/fstab` 文件。细节可以参考你的操作系统文档。

如果实际环境不允许禁用掉 `swap`，你可以尝试降低 `swappiness`。此值控制操作系统使用交换区的积极性。这可以防止在正常情况下使用交换区，但仍允许操作系统在紧急情况下将内存里的东西放到交换区。

对于大多数Linux系统来说，这可以用 `sysctl` 值来配置：

```yaml
vm.swappiness = 1 # 将此值配置为1会比0好，在kernal内核的某些版本中，0可能会引起OOM异常。
```
最后，如果两种方法都不可用，你应该在ElasticSearch的配置中启用 `mlockall.file`。这允许JVM锁定其使用的内存，而避免被放入操作系统交换区。

在elasticsearch.yml中，做如下设置：

```s
bootstrap.mlockall: true
```

## 查看node节点数据

`GET /_cat/nodes?v&h=id,ip,port,v,master,name,heap.current,heap.percent,heap.max,ram.current,ram.percent,ram.max,fielddata.memory_size,fielddata.evictions,query_cache.memory_size,query_cache.evictions, request_cache.memory_size,request_cache.evictions,request_cache.hit_count,request_cache.miss_count`


`GET /_cat/nodes?v&h=id,heap.current,heap.percent,heap.max,ram.current,ram.percent,ram.max,fielddata.memory_size`


`GET /_cat/nodes?v&h=id,fielddata.evictions,query_cache.memory_size,query_cache.evictions, request_cache.memory_size,request_cache.evictions,request_cache.hit_count,request_cache.miss_count`



## 参考文章
> [](https://nereuschen.github.io/2015/09/16/ElasticSearch%E5%86%85%E5%AD%98%E4%BD%BF%E7%94%A8%E5%88%86%E6%9E%90/)