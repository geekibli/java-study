---
title: elasticsearch-reindex
toc: true
date: 2021-07-20 11:59:18
tags: elasticsearch
categories: [Elasticsearch, Search in Depth]
---

## reindex 常规使用
> Reindex要求为源索引中的所有文档启用_source。
Reindex不尝试设置目标索引，它不复制源索引的设置，你应该在运行_reindex操作之前设置目标索引，包括设置映射、碎片计数、副本等。


如下示例将把文档从twitter索引复制到new_twitter索引：
```
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter"
  }
}'
```
下面是返回值：
```json
{
  "took" : 299,
  "timed_out" : false,
  "total" : 2,
  "updated" : 0,
  "created" : 2,
  "deleted" : 0,
  "batches" : 1,
  "version_conflicts" : 0,
  "noops" : 0,
  "retries" : {
    "bulk" : 0,
    "search" : 0
  },
  "throttled_millis" : 0,
  "requests_per_second" : -1.0,
  "throttled_until_millis" : 0,
  "failures" : [ ]
}
```

就像_update_by_query一样，_reindex获取源索引的快照，但它的目标必须是不同的索引，因此不太可能发生版本冲突。可以像index API那样配置dest元素来控制乐观并发控制。仅仅省略version_type(如上所述)或将其设置为internal，都会导致Elasticsearch盲目地将文档转储到目标中，覆盖任何碰巧具有相同类型和id的文档


```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter",
	"version_type": "internal"
  }
}'

```

将version_type设置为external将导致Elasticsearch保存源文件的版本，创建任何缺失的文档，并更新目标索引中比源索引中版本更旧的文档：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter2",
	"version_type": "external"
  }
}'

```

设置op_type=create将导致_reindex只在目标索引中创建缺失的文档。所有现有文件将导致版本冲突：
```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter3",
	"op_type": "create"
  }
}'
```

默认情况下，版本冲突将中止_reindex进程，“conflicts”请求体参数可用于指示_reindex处理关于版本冲突的下一个文档，需要注意的是，其他错误类型的处理不受“conflicts”参数的影响，当在请求体中设置“conflicts”:“proceed”时，_reindex进程将继续处理版本冲突，并返回所遇到的版本冲突计数：
```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "conflicts": "proceed",
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter3",
	"op_type": "create"
  }
}'
```
返回值如下：
```json
{
  "took" : 7,
  "timed_out" : false,
  "total" : 2,
  "updated" : 0,
  "created" : 0,
  "deleted" : 0,
  "batches" : 1,
  "version_conflicts" : 2,
  "noops" : 0,
  "retries" : {
    "bulk" : 0,
    "search" : 0
  },
  "throttled_millis" : 0,
  "requests_per_second" : -1.0,
  "throttled_until_millis" : 0,
  "failures" : [ ]
}
```

可以通过向源添加查询来限制文档。这将只复制由kimchy发出的tweet到new_twitter：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "conflicts": "proceed",
  "source": {
    "index": "twitter",
    "query": {
      "term": {
        "user": "kimchy"
      }
    }
  },
  "dest": {
    "index": "new_twitter2"
  }
}'
```

source中的index可以是一个列表，允许你在一个请求中从多个源复制。这将从twitter和blog索引复制文档：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "conflicts": "proceed",
  "source": {
    "index": ["twitter","blog"]
  },
  "dest": {
    "index": "new_twitter2"
  }
}'
```

注意：Reindex API不处理ID冲突，因此最后编写的文档将“胜出”，但顺序通常是不可预测的，因此依赖这种行为不是一个好主意，相反，可以使用脚本确保id是惟一的。
还可以通过设置大小来限制处理文档的数量，示例将只复制一个单一的文件从twitter到new_twitter：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "size": 1,
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter"
  }
}'
```
如果你想从twitter索引中获得一组特定的文档，你需要使用sort。排序会降低滚动的效率，但在某些上下文中，这样做是值得的。如果可能的话，选择一个比大小和排序更具选择性的查询。这将把10000个文档从twitter复制到new_twitter：
```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "size": 10000,
  "source": {
    "index": "blog2",
    "sort": { "age": "desc" }
  },
  "dest": {
    "index": "new_twitter"
  }
}'
```

source部分支持搜索请求中支持的所有元素。例如，只有原始文档中的一部分字段可以使用源过滤重新索引，如下所示：
```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "twitter",
    "_source": ["user", "_doc"]
  },
  "dest": {
    "index": "new_twitter"
  }
}'
```
与_update_by_query一样，_reindex支持修改文档的脚本。与_update_by_query不同，脚本允许修改文档的元数据。这个例子改变了源文档的版本：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "blog2",
    "version_type": "external"
  },
  "script": {
    "source": "if (ctx._source.foo == \"bar\") {ctx._version++; ctx._source.remove(\"foo\")}",
    "lang": "painless"
  }
}'
```

就像在_update_by_query中一样，你可以设置ctx.op更改在目标索引上执行的操作，值为noop，delete。
设置ctx.op到任何其他字段都会返回一个错误，在ctx中设置任何其他字段也是如此。可以修改以下值：_id、_index
、_version、_routing。
将_version设置为null或将它从ctx映射中清除，就像没有在索引请求中发送版本一样;它将导致在目标索引中覆盖文档，而不管目标上的版本或在_reindex请求中使用的版本类型。
默认情况下，如果_reindex看到一个带有路由的文档，那么该路由将被保留，除非脚本更改了它，你可以设置路由对dest的请求，以改变这一点：
keep：将为每个匹配发送的批量请求上的路由设置为匹配上的路由。这是默认值。
discard：将为每个匹配发送的批量请求上的路由设置为null。
=<some text>：将为每个匹配发送的批量请求上的路由设置为=之后的所有文本。
例如，你可以使用以下请求将所有文档从具有公司名称cat的源索引复制到路由设置为cat的dest索引中：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "source",
    "query": {
      "match": {
        "company": "cat"
      }
    }
  },
  "dest": {
    "index": "dest",
    "routing": "=cat"
  }
}'
```
默认情况下，_reindex使用滚动批次为1000，可以使用源元素中的size字段更改批大小：
```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "source",
    "size": 100
  },
  "dest": {
    "index": "dest",
    "routing": "=cat"
  }
}'
```

_reindex还可以通过像这样指定管道来使用Ingest节点特性：
```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "source"
  },
  "dest": {
    "index": "dest",
    "pipeline": "some_ingest_pipeline"
  }
}'
```




## 远程reindex

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "remote": {
      "host": "http://otherhost:9200",
      "username": "user",
      "password": "pass"
    },
    "index": "source",
    "query": {
      "match": {
        "test": "data"
      }
    }
  },
  "dest": {
    "index": "dest"
  }
}'
```

host参数必须包含scheme, host, port（如：http://otherhost:9200）,也可以加路径（如：http://otherhost:9200/proxy），username和password是可选的，如果远程集群开启了安全认证，那么是必选的，如果需要使用username和password，需要使用https。
远程主机需要设置白名单，可以通过elasticsearch.yml文件里的reindex.remote.whitelist属性进行设置，如果设置多个值可以使用逗号来进行分隔（如：otherhost:9200, another:9200, 127.0.10.*:9200, localhost:*），这里的配置可以忽略scheme，如：

`reindex.remote.whitelist: "otherhost:9200, another:9200, 127.0.10.*:9200, localhost:*"`


必须让每个处理reindex的节点上添加白名单的配置。
这个特性应该适用于可能找到的任何版本的Elasticsearch的远程集群，这应该允许通过从旧版本的集群reindex，将Elasticsearch的任何版本升级到当前版本。
要启用发送到旧版本Elasticsearch的查询，无需验证或修改即可将查询参数直接发送到远程主机，注意：远程reindex不支持手动或者自动slicing。
从远程服务器reindex使用堆上缓冲区，默认最大大小为100mb，如果远程索引包含非常大的文档，则需要使用更小的批处理大小，下面的示例将批处理大小设置为10。


```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "remote": {
      "host": "http://otherhost:9200"
    },
    "index": "source",
    "size": 10,
    "query": {
      "match": {
        "test": "data"
      }
    }
  },
  "dest": {
    "index": "dest"
  }
}'
```

还可以使用socket_timeout字段设置远程连接上的套接字读取超时，使用connect_timeout字段设置连接超时，他们的默认值为30秒，下面示例例将套接字读取超时设置为1分钟，连接超时设置为10秒：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
 "source": {
    "remote": {
      "host": "http://otherhost:9200",
      "socket_timeout": "1m",
      "connect_timeout": "10s"
    },
    "index": "source",
    "query": {
      "match": {
        "test": "data"
      }
    }
  },
  "dest": {
    "index": "dest"
  }
}'
```


## 配置SSL参数

远程reindex支持配置SSL参数，除了在Elasticsearch秘钥库中添加安全设置之外，还需要在elasticsearch.yml文件中进行配置，不可能在_reindex请求体中配置。
支持以下设置：
reindex.ssl.certificate_authorities
应受信任的PEM编码证书文件的路径列表，不同同时指定reindex.ssl.certificate_authorities和reindex.ssl.truststore.path。
reindex.ssl.truststore.path
包含要信任的证书的Java密钥存储文件的路径，这个密钥存储库可以是“JKS”或“PKCS#12”格式，不能同时指定reindex.ssl.certificate_authorities和reindex.ssl.truststore.path。
reindex.ssl.truststore.password
reindex.ssl.truststore.path配置的密码，不能和reindex.ssl.truststore.secure_password一起使用。
reindex.ssl.truststore.secure_password
reindex.ssl.truststore.path配置的密码，不能和reindex.ssl.truststore.password一起使用。
reindex.ssl.truststore.type
reindex.ssl.truststore.path信任存储库的类型，必须是jks或PKCS12，如果reindex.ssl.truststore.path的结束是".p12", ".pfx"或者"pkcs12"，那么该配置的默认值是PKCS12，否则默认值是jks。
reindex.ssl.verification_mode
指示用于防止中间人攻击和伪造证书的验证类型。可以设置为full（验证主机名和证书路径）、certificate（验证证书路径，但不验证主机名）、none（不执行验证——这在生产环境中是强烈不鼓励的），默认是full。
reindex.ssl.certificate
指定PEM编码证书的路径或者证书链用于HTTP客户端身份认证，这个配置还需要设置reindex.ssl.key值，不能同时设置reindex.ssl.certificate和reindex.ssl.keystore.path。
reindex.ssl.key
指定与用于客户端身份验证的证书相关联的PEM编码私钥的路径，不能同时设置reindex.ssl.key和reindex.ssl.keystore.path。
reindex.ssl.key_passphrase
指定用于解密已加密的PEM编码私钥(reindex.ssl.key)的口令，不能与reindex.ssl.secure_key_passphrase一起使用。
reindex.ssl.secure_key_passphrase
指定用于解密已加密的PEM编码私钥(reindex.ssl.key)的口令，不能与reindex.ssl.key_passphrase一起使用。
reindex.ssl.keystore.path
指定密钥存储库的路径，其中包含用于HTTP客户机身份验证的私钥和证书(如果远程集群需要)，这个密钥存储库可以是“JKS”或“PKCS#12”格式，不能同时指定reindex.ssl.key和reindex.ssl.keystore.path。
reindex.ssl.keystore.type
密钥存储库的类型(reindex.ssl.keystore.path)，必须是jks或者PKCS12，如果reindex.ssl.keystore.path的结束是".p12", ".pfx"或者"pkcs12"，那么该配置的默认值是PKCS12，否则默认值是jks。
reindex.ssl.keystore.password
密钥存储库的密码(reindex.ssl.keystore.path)，此设置不能与reindex.ssl.keystore.secure_password一起使用。
reindex.ssl.keystore.secure_password
密钥存储库的密码(reindex.ssl.keystore.path)，此设置不能与reindex.ssl.keystore.password一起使用。
reindex.ssl.keystore.key_password
密钥存储库中密钥的密码(reindex.ssl.keystore.path)，默认为密钥存储库密码，此设置不能与reindex.ssl.keystore.secure_key_password一起使用。
reindex.ssl.keystore.secure_key_password
密钥存储库中密钥的密码(reindex.ssl.keystore.path)，默认为密钥存储库密码，此设置不能与reindex.ssl.keystore.key_password一起使用。

## URL参数

除了标准的pretty参数外， reindex还支持refresh, wait_for_completion, wait_for_active_shards, timeout, scroll和requests_per_second。
发送refresh url参数将导致对所写请求的所有索引进行刷新，这与Index API的refresh参数不同，后者只会刷新接收新数据的碎片，与index API不同的是，它不支持wait_for。
如果请求包含wait_for_completion=false，则Elasticsearch将执行一些执行前检查，启动请求，然后返回一个任务，该任务可与Tasks api一起用于取消或获取任务状态，Elasticsearch还将创建此任务的记录，作为.tasks/task/${taskId}的文档，你可以自己决定是保留或者删除他，当你已经完成了，删除他，这样es会回收他使用的空间。
wait_for_active_shards控制在进行重新索引之前必须激活多少个shard副本，超时控制每个写请求等待不可用碎片变为可用的时间，两者在批量API中的工作方式完全相同，由于_reindex使用滚动搜索，你还可以指定滚动参数来控制“搜索上下文”存活的时间(例如?scroll=10m)，默认值是5分钟。
requests_per_second可以设置为任何正数(1.4、6、1000等)，并通过在每个批中填充等待时间来控制_reindex发出批索引操作的速率，可以通过将requests_per_second设置为-1来禁用。
节流是通过在批之间等待来完成的，这样就可以给_reindex内部使用的滚动设置一个考虑填充的超时，填充时间是批大小除以requests_per_second和写入时间之间的差额，默认情况下批处理大小为1000，所以如果requests_per_second被设置为500：

`target_time = 1000 / 500 per second = 2 seconds
`padding time` = target_time - write_time = 2 seconds - 0.5 seconds = 1.5 seconds`

由于批处理是作为单个_bulk请求发出的，因此较大的批处理大小将导致Elasticsearch创建许多请求，然后等待一段时间再启动下一个请求集，这是“bursty”而不是“smooth”，默认值是-1。

响应体 
```json
{
    "took": 639,
    "timed_out": false,
    "total": 5,
    "updated": 0,
    "created": 5,
    "deleted": 0,
    "batches": 1,
    "noops": 0,
    "version_conflicts": 2,
    "retries": {
	"bulk": 0,
	"search": 0
    },
    "throttled_millis": 0,
    "requests_per_second": 1,
    "throttled_until_millis": 0,
    "failures": []
}
```

took
整个操作花费的总毫秒数。
timed_out
如果在reindex期间执行的任何请求超时，则将此标志设置为true。
total
成功处理的文档数量。
updated
成功更新的文档数量。
created
成功创建的文档的数量。
deleted
成功删除的文档数量。
batches
由reindex回拉的滚动响应的数量。
noops
由于用于reindex的脚本返回了ctx.op的noop值而被忽略的文档数量。
version_conflicts
reindex命中的版本冲突数。
retries
reindex尝试重试的次数,bulk是重试的批量操作的数量，search是重试的搜索操作的数量。
throttled_millis
请求休眠以符合requests_per_second的毫秒数。
requests_per_second
在reindex期间每秒有效执行的请求数。
throttled_until_millis
在_reindex响应中，该字段应该始终等于零，它只有在使用任务API时才有意义，在任务API中，它指示下一次(毫秒)再次执行节流请求，以符合requests_per_second。
failures
出现多个错误以数组返回。


## 使用task api
获取task
```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty&wait_for_completion=false" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "twitter"
  },
  "dest": {
    "index": "new_twitter"
  }
}'
```

返回值为
```json
{
  "task" : "8uQK-B00RiWq03awtJok1Q:18"
}
```
你可以用任务API获取所有正在运行的reindex请求的状态：
`curl -XGET "http://127.0.0.1:9200/_tasks?detailed=true&actions=*reindex&pretty"`
或者根据task id获取
`curl -XGET "http://127.0.0.1:9200/_tasks/8uQK-B00RiWq03awtJok1Q:48?pretty"`
返回值为：
```json
{
  "completed" : true,
  "task" : {
    "node" : "8uQK-B00RiWq03awtJok1Q",
    "id" : 48,
    "type" : "transport",
    "action" : "indices:data/write/reindex",
    "status" : {
      "total" : 0,
      "updated" : 0,
      "created" : 0,
      "deleted" : 0,
      "batches" : 0,
      "version_conflicts" : 0,
      "noops" : 0,
      "retries" : {
        "bulk" : 0,
        "search" : 0
      },
      "throttled_millis" : 0,
      "requests_per_second" : 0.0,
      "throttled_until_millis" : 0
    },
    "description" : "reindex from [twitter] to [new_twitter][_doc]",
    "start_time_in_millis" : 1566216815832,
    "running_time_in_nanos" : 86829,
    "cancellable" : true,
    "headers" : { }
  },
  "error" : {
    "type" : "index_not_found_exception",
    "reason" : "no such index [new_twitter] and [action.auto_create_index] ([twitter,index10,-index1*,+ind*,-myIndex]) doesn't match",
    "index_uuid" : "_na_",
    "index" : "new_twitter"
  }
}
```

## 取消task
任何reindex接口都可以使用task cancel api取消：
```json
 curl -XPOST "http://127.0.0.1:9200/_tasks/8uQK-B00RiWq03awtJok1Q:48/_cancel?pretty"
{
  "node_failures" : [
    {
      "type" : "failed_node_exception",
      "reason" : "Failed node [8uQK-B00RiWq03awtJok1Q]",
      "node_id" : "8uQK-B00RiWq03awtJok1Q",
      "caused_by" : {
        "type" : "resource_not_found_exception",
        "reason" : "task [8uQK-B00RiWq03awtJok1Q:48] doesn't support cancellation"
      }
    }
  ],
  "nodes" : { }
}
```
取消应该很快发生，但可能需要几秒钟，Tasks API将继续列出任务，直到它醒来取消自己。



## rethrottle
可以在url中使用_rethrottle，并使用requests_per_second参数来设置节流：
```json
curl -XPOST "http://127.0.0.1:9200/_reindex/8uQK-B00RiWq03awtJok1Q:250/_rethrottle?requests_per_second=-1&pretty"
{
  "node_failures" : [
    {
      "type" : "failed_node_exception",
      "reason" : "Failed node [8uQK-B00RiWq03awtJok1Q]",
      "node_id" : "8uQK-B00RiWq03awtJok1Q",
      "caused_by" : {
        "type" : "resource_not_found_exception",
        "reason" : "task [8uQK-B00RiWq03awtJok1Q:250] is missing"
      }
    }
  ],
  "nodes" : { }
}
```

## reindex改变属性名称

_reindex可以重命名属性名，假设你创建了一个包含如下文档的索引：
```json
curl -XPOST "http://127.0.0.1:9200/test/_doc/1?refresh&pretty" -H "Content-Type:application/json" -d'
{
  "text": "words words",
  "flag": "foo"
}'
```

在reindex的时候想把flag修改为tag，示例如：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "order"
  },
  "dest": {
    "index": "order2"
  },
  "script": {
    "source": "ctx._source.tag = ctx._source.remove(\"flag\")"
  }
}'

```

查看order2的数据：

```json
curl -XGET "http://127.0.0.1:9200/order2/_doc/1?pretty"
{
  "_index" : "order2",
  "_type" : "_doc",
  "_id" : "1",
  "_version" : 1,
  "_seq_no" : 0,
  "_primary_term" : 1,
  "found" : true,
  "_source" : {
    "text" : "words words",
    "tag" : "foo"
  }
}

```


## 切片
Reindex支持切片滚动，以并行化重新索引过程。这种并行化可以提高效率，并提供一种方便的方法将请求分解为更小的部分

### 手动切片
通过为每个请求提供一个片id和片的总数，手工切片一个重索引请求：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "order",
    "slice": {
      "id": 0,
      "max": 2
    }
  },
  "dest": {
    "index": "order2"
  }
}'
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "order",
    "slice": {
      "id": 1,
      "max": 2
    }
  },
  "dest": {
    "index": "order2"
  }
}'

```

你可以通过以下方法来验证：

`curl -XGET "http://127.0.0.1:9200/_refresh?pretty"`

`curl -XPOST "http://127.0.0.1:9200/order2/_search?size=0&filter_path=hits.total&pretty"`

返回值为：

```json
{
  "hits" : {
    "total" : {
      "value" : 1,
      "relation" : "eq"
    }
  }
}

```


### 自动切面
你还可以让_reindex使用切片滚动自动并行化_uid上的切片，使用slices指定要使用的片数:

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?slices=5&refresh&pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "order"
  },
  "dest": {
    "index": "order2"
  }
}'

```

通过下面请求进行验证：

```json
curl -XPOST "http://127.0.0.1:9200/order2/_search?size=0&filter_path=hits.total&pretty"
{
  "hits" : {
    "total" : {
      "value" : 1,
      "relation" : "eq"
    }
  }
}

```


## reindex多个索引
如果有许多索引需要reindex，通常最好一次reindex一个索引，而不是使用一个glob模式来获取许多索引。这样，如果有任何错误，可以删除部分完成的索引并从该索引重新开始，从而恢复该过程。它还使并行化过程变得相当简单：将索引列表拆分为reindex并并行运行每个列表。
可以使用一次性脚本：

```json
for index in i1 i2 i3 i4 i5; do
  curl -HContent-Type:application/json -XPOST localhost:9200/_reindex?pretty -d'{
    "source": {
      "index": "'$index'"
    },
    "dest": {
      "index": "'$index'-reindexed"
    }
  }'
done

```



##  reindex每日索引

尽管有上述建议，你仍然可以结合使用_reindex和Painless来reindex每日索引，从而将新模板应用于现有文档。
假设有以下文件组成的索引:

`curl -XPUT "http://127.0.0.1:9200/metricbeat-2016.05.30/_doc/1?refresh&pretty" -H "Content-Type:application/json" -d`
{"system.cpu.idle.pct":  0.908}'

`curl -XPUT "http://127.0.0.1:9200/metricbeat-2016.05.31/_doc/1?refresh&pretty" -H "Content-Type:application/json" -d'`
{"system.cpu.idle.pct":  0.105}

metricbeat-*索引的新模板已经加载到Elasticsearch中，但它只适用于新创建的索引。
下面的脚本从索引名称中提取日期，并创建一个附加-1的新索引。所有来自metricbeat-2016.05.31的数据将reindex到metricbeat-2016.05.31-1。

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "source": {
    "index": "metricbeat-*"
  },
  "dest": {
    "index": "metricbeat"
  },
  "script": {
    "lang": "painless",
    "source": "ctx._index = 'metricbeat-' + (ctx._index.substring('metricbeat-'.length(),ctx._index.length())) + '-1'"
  }
}'

```

以前metricbeat索引中的所有文档现在都可以在*-1索引中找到。

`curl -XGET "http://127.0.0.1:9200/metricbeat-2016.05.30-1/_doc/1?pretty"`
`curl -XGET "http://127.0.0.1:9200/metricbeat-2016.05.31-1/_doc/1?pretty"`


前一种方法还可以与更改字段名称结合使用，以便仅将现有数据加载到新索引中，并在需要时重命名任何字段。



## 提取索引中的子集合
_reindex可用于提取索引的随机子集进行测试：

```json
curl -XPOST "http://127.0.0.1:9200/_reindex?pretty" -H "Content-Type:application/json" -d'
{
  "size": 10,
  "source": {
    "index": "twitter",
    "query": {
      "function_score" : {
        "query" : { "match_all": {} },
        "random_score" : {}
      }
    },
    "sort": "_score"    
  },
  "dest": {
    "index": "random_twitter"
  }
}'

```

_reindex默认按_doc排序，因此random_score不会有任何效果，除非你覆盖sort属性为_score。








