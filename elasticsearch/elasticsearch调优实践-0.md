---
title: elasticsearch调优实践
toc: true
date: 2021-07-14 23:12:16
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---

> 从性能和稳定性两方面，从linux参数调优、ES节点配置和ES使用方式三个角度入手，介绍ES调优的基本方案。当然，ES的调优绝不能一概而论，需要根据实际业务场景做适当的取舍和调整

> [Elasticsearch性能优化总结](https://zhuanlan.zhihu.com/p/43437056)
> [Elasticsearch调优实践](https://cloud.tencent.com/developer/article/1158408)


## Linux优化

### 关闭交换分区，防止内存置换降低性能。 
将 `/etc/fstab` 文件中包含swap的行注释掉
`sed -i '/swap/s/^/#/' /etc/fstabswapoff -a`

### 磁盘挂载选项
noatime：禁止记录访问时间戳，提高文件系统读写性能
data=writeback： 不记录data journal，提高文件系统写入性能
barrier=0：barrier保证journal先于data刷到磁盘，上面关闭了journal，这里的barrier也就没必要开启了
nobh：关闭buffer_head，防止内核打断大块数据的IO操作
mount -o noatime,data=writeback,barrier=0,nobh /dev/sda /es_data

### 对于SSD磁盘，采用电梯调度算法
因为SSD提供了更智能的请求调度算法，不需要内核去做多余的调整 (仅供参考)
`echo noop > /sys/block/sda/queue/scheduler`

## ES节点配置

conf/elasticsearch.yml文件：

###  适当增大写入buffer和bulk队列长度，提高写入性能和稳定性

`indices.memory.index_buffer_size: 15%`
`thread_pool.bulk.queue_size: 1024`

### 计算disk使用量时，不考虑正在搬迁的shard
在规模比较大的集群中，可以防止新建shard时扫描所有shard的元数据，提升shard分配速度。
`cluster.routing.allocation.disk.include_relocations: false`


## 三 ES使用方式


###  控制字段的存储选项

ES底层使用Lucene存储数据，主要包括行存（StoreFiled）、列存（DocValues）和倒排索引（InvertIndex）三部分。 大多数使用场景中，没有必要同时存储这三个部分，可以通过下面的参数来做适当调整：

#### StoreFiled
行存，其中占比最大的是source字段，它控制doc原始数据的存储。在写入数据时，ES把doc原始数据的整个json结构体当做一个string，存储为source字段。查询时，可以通过source字段拿到当初写入时的整个json结构体。 所以，如果没有取出整个原始json结构体的需求，可以通过下面的命令，在mapping中关闭source字段或者只在source中存储部分字段，数据查询时仍可通过ES的docvaluefields获取所有字段的值。
注意：关闭source后， update, updatebyquery, reindex等接口将无法正常使用，所以有update等需求的index不能关闭source。
- 关闭 _source
```json
PUT my_index 
{
  "mappings": {
    "my_type": {
      "_source": {
        "enabled": false
      }
    }
  }
}
```

#### _source只存储部分字段
通过includes指定要存储的字段或者通过excludes滤除不需要的字段
```json
PUT my_index
{
  "mappings": {
    "_doc": {
      "_source": {
        "includes": [
          "*.count",
          "meta.*"
        ],
        "excludes": [
          "meta.description",
          "meta.other.*"
        ]
      }
    }
  }
}

```
#### docvalues 控制列存。
ES主要使用列存来支持sorting, aggregations和scripts功能，对于没有上述需求的字段，可以通过下面的命令关闭docvalues，降低存储成本。
```json
PUT my_index
{
  "mappings": {
    "my_type": {
      "properties": {
        "session_id": {
          "type": "keyword",
          "doc_values": false
        }
      }
    }
  }
}
```

#### ndex：控制倒排索引。
ES默认对于所有字段都开启了倒排索引，用于查询。对于没有查询需求的字段，可以通过下面的命令关闭倒排索引。
```json
PUT my_index
{
  "mappings": {
    "my_type": {
      "properties": {
        "session_id": {
          "type": "keyword",
          "index": false
        }
      }
    }
  }
}
```
#### allES的一个特殊的字段
- ES把用户写入json的所有字段值拼接成一个字符串后，做分词，然后保存倒排索引，用于支持整个json的全文检索。
这种需求适用的场景较少，可以通过下面的命令将all字段关闭，节约存储成本和cpu开销。（ES 6.0+以上的版本不再支持_all字段，不需要设置）
```json
PUT /my_index
{
  "mapping": {
    "my_type": {
      "_all": {
        "enabled": false
      }
    }
  }
}
```
#### fieldnames 
  该字段用于exists查询，来确认某个doc里面有无一个字段存在。若没有这种需求，可以将其关闭。
```json
PUT /my_index
{
  "mapping": {
    "my_type": {
      "_field_names": {
        "enabled": false
      }
    }
  }
}
```

### 开启最佳压缩
对于打开了上述_source字段的index，可以通过下面的命令来把lucene适用的压缩算法替换成 DEFLATE，提高数据压缩率。
`PUT /my_index/_settings{    "index.codec": "best_compression"}`


### bulk批量写入
写入数据时尽量使用下面的bulk接口批量写入，提高写入效率。每个bulk请求的doc数量设定区间推荐为1k~1w，具体可根据业务场景选取一个适当的数量。


### 调整translog同步策略
默认情况下，translog的持久化策略是，对于每个写入请求都做一次flush，刷新translog数据到磁盘上。这种频繁的磁盘IO操作是严重影响写入性能的，如果可以接受一定概率的数据丢失（这种硬件故障的概率很小），可以通过下面的命令调整 translog 持久化策略为异步周期性执行，并适当调整translog的刷盘周期。
```json
PUT my_index
{
"settings": {
    "index": {
    "translog": {
        "sync_interval": "5s",
        "durability": "async"
    }
    }
}
}
```
### 调整refresh_interval
写入Lucene的数据，并不是实时可搜索的，ES必须通过refresh的过程把内存中的数据转换成Lucene的完整segment后，才可以被搜索。默认情况下，ES每一秒会refresh一次，产生一个新的segment，这样会导致产生的segment较多，从而segment merge较为频繁，系统开销较大。如果对数据的实时可见性要求较低，可以通过下面的命令提高refresh的时间间隔，降低系统开销。

`PUT my_index{  "settings": {    "index": {        "refresh_interval" : "30s"    }  }}`

### merge并发控制
ES的一个index由多个shard组成，而一个shard其实就是一个Lucene的index，它又由多个segment组成，且Lucene会不断地把一些小的segment合并成一个大的segment，这个过程被称为merge。默认值是Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2))，当节点配置的cpu核数较高时，merge占用的资源可能会偏高，影响集群的性能，可以通过下面的命令调整某个index的merge过程的并发度：

`PUT /my_index/_settings{    "index.merge.scheduler.max_thread_count": 2}`

### 写入数据不指定_id，让ES自动产生
当用户显示指定id写入数据时，ES会先发起查询来确定index中是否已经有相同id的doc存在，若有则先删除原有doc再写入新doc。这样每次写入时，ES都会耗费一定的资源做查询。如果用户写入数据时不指定doc，ES则通过内部算法产生一个随机的id，并且保证id的唯一性，这样就可以跳过前面查询id的步骤，提高写入效率。 所以，在不需要通过id字段去重、update的使用场景中，写入不指定id可以提升写入速率。基础架构部数据库团队的测试结果显示，无id的数据写入性能可能比有_id的高出近一倍，实际损耗和具体测试场景相关。

### routing
对于数据量较大的index，一般会配置多个shard来分摊压力。这种场景下，一个查询会同时搜索所有的shard，然后再将各个shard的结果合并后，返回给用户。对于高并发的小查询场景，每个分片通常仅抓取极少量数据，此时查询过程中的调度开销远大于实际读取数据的开销，且查询速度取决于最慢的一个分片。开启routing功能后，ES会将routing相同的数据写入到同一个分片中（也可以是多个，由index.routingpartitionsize参数控制）。如果查询时指定routing，那么ES只会查询routing指向的那个分片，可显著降低调度开销，提升查询效率。 routing的使用方式如下：
```json
# 写入PUT my_index/my_type/1?routing=user1{  "title": "This is a document"}
# 查询GET my_index/_search?routing=user1,user2 {  "query": {    "match": {      "title": "document"    }  }}
```

### 为string类型的字段选取合适的存储方式
#### 存为text类型的字段（string字段默认类型为text）： 
    做分词后存储倒排索引，支持全文检索，可以通过下面几个参数优化其存储方式：
     -  norms：用于在搜索时计算该doc的_score（代表这条数据与搜索条件的相关度），如果不需要评分，可以将其关闭。
     - indexoptions：控制倒排索引中包括哪些信息（docs、freqs、positions、offsets）。对于不太注重score/highlighting的使用场景，可以设为 docs来降低内存/磁盘资源消耗。
     - fields: 用于添加子字段。对于有sort和聚合查询需求的场景，可以添加一个keyword子字段以支持这两种功能。
    
    ```json
    {
    "mappings": {
        "my_type": {
        "properties": {
            "title": {
            "type": "text",
            "norms": false,
            "index_options": "docs",
            "fields": {
                "raw": {
                "type": "keyword"
                }
            }
            }
        }
        }
    }
    }
    ```    
#### 存为keyword类型的字段
不做分词，不支持全文检索。text分词消耗CPU资源，冗余存储keyword子字段占用存储空间。如果没有全文索引需求，只是要通过整个字段做搜索，可以设置该字段的类型为keyword，提升写入速率，降低存储成本。 设置字段类型的方法有两种：一是创建一个具体的index时，指定字段的类型；二是通过创建template，控制某一类index的字段类型。

-  通过mapping指定 tags 字段为keyword类型
```json
PUT my_index
{
    "mappings": {
        "my_type": {
        "properties": {
            "tags": {
            "type": "keyword"
            }
        }
        }
    }
    }
```
-  通过template，指定my_index*类的index，其所有string字段默认为keyword类型
PUT _template/my_template
```json
{
  "order": 0,
  "template": "my_index*",
  "mappings": {
      "_default_": {
      "dynamic_templates": [
          {
          "strings": {
              "match_mapping_type": "string",
              "mapping": {
              "type": "keyword",
              "ignore_above": 256
              }
          }
          }
      ]
      }
  },
  "aliases": {
      
  }
  }
```

### 查询时，使用query-bool-filter组合取代普通query
默认情况下，ES通过一定的算法计算返回的每条数据与查询语句的相关度，并通过score字段来表征。但对于非全文索引的使用场景，用户并不care查询结果与查询条件的相关度，只是想精确的查找目标数据。此时，可以通过query-bool-filter组合来让ES不计算score，并且尽可能的缓存filter的结果集，供后续包含相同filter的查询使用，提高查询效率。

- 普通查询
  `POST my_index/_search{  "query": {    "term" : { "user" : "Kimchy" }   }}`
- query-bool-filter 加速查询
  `POST my_index/_search{  "query": {    "bool": {      "filter": {        "term": { "user": "Kimchy" }      }    }  }}`



### index按日期滚动，便于管理

写入ES的数据最好通过某种方式做分割，存入不同的index。
常见的做法是将数据按模块/功能分类，写入不同的index，然后按照时间去滚动生成index。这样做的好处是各种数据分开管理不会混淆，也易于提高查询效率。`同时index按时间滚动，数据过期时删除整个index，要比一条条删除数据或deletebyquery效率高很多`，因为删除整个index是直接删除底层文件，而deletebyquery是查询-标记-删除。

举例说明，假如有[modulea,moduleb]两个模块产生的数据，那么index规划可以是这样的：一类index名称是modulea + {日期}，另一类index名称是module_b+ {日期}。对于名字中的日期，可以在写入数据时自己指定精确的日期，也可以通过ES的ingest pipeline中的index-name-processor实现（会有写入性能损耗）。

### 按需控制index的分片数和副本数
分片（shard）：一个ES的index由多个shard组成，每个shard承载index的一部分数据。

副本（replica）：index也可以设定副本数（numberofreplicas），也就是同一个shard有多少个备份。对于查询压力较大的index，可以考虑提高副本数（numberofreplicas），通过多个副本均摊查询压力。

shard数量（numberofshards）设置过多或过低都会引发一些问题：shard数量过多，则批量写入/查询请求被分割为过多的子写入/查询，导致该index的写入、查询拒绝率上升；对于数据量较大的inex，当其shard数量过小时，无法充分利用节点资源，造成机器资源利用率不高 或 不均衡，影响写入/查询的效率。

对于每个index的shard数量，可以根据数据总量、写入压力、节点数量等综合考量后设定，然后根据数据增长状态定期检测下shard数量是否合理。基础架构部数据库团队的推荐方案是：

对于数据量较小（100GB以下）的index，往往写入压力查询压力相对较低，一般设置3~5个shard，numberofreplicas设置为1即可（也就是一主一从，共两副本） 。
对于数据量较大（100GB以上）的index：
一般把单个shard的数据量控制在（20GB~50GB）
让index压力分摊至多个节点：可通过index.routing.allocation.totalshardsper_node参数，强制限定一个节点上该index的shard数量，让shard尽量分配到不同节点上
综合考虑整个index的shard数量，如果shard数量（不包括副本）超过50个，就很可能引发拒绝率上升的问题，此时可考虑把该index拆分为多个独立的index，分摊数据量，同时配合routing使用，降低每个查询需要访问的shard数量。

