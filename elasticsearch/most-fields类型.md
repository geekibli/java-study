---
title: most_fields类型
toc: true
date: 2021-07-06 20:54:23
tags: elasticsearch
categories: [Elasticsearch, Search in Depth]
---


## 多字段映射
首先要做的事情就是对我们的字段索引两次：一次使用词干模式以及一次非词干模式。为了做到这点，采用 `multifields` 来实现，已经在 [multifields](https://www.elastic.co/guide/cn/elasticsearch/guide/current/multi-fields.html) 有所介绍：

`DELETE /my_index`  
```json
PUT /my_index
{
    "settings": { "number_of_shards": 1 }, 
    "mappings": {
        "my_type": {
            "properties": {
                "title": { 
                    "type":     "string",
                    "analyzer": "english",
                    "fields": {
                        "std":   { 
                            "type":     "string",
                            "analyzer": "standard"
                        }
                    }
                }
            }
        }
    }
}
```

- title 字段使用 english 英语分析器来提取词干。
- title.std 字段使用 standard 标准分析器，所以没有词干提取。


接着索引一些文档：
```json
PUT /my_index/my_type/1
{ "title": "My rabbit jumps" }

PUT /my_index/my_type/2
{ "title": "Jumping jack rabbits" }
```

这里用一个简单 match 查询 title 标题字段是否包含 jumping rabbits （跳跃的兔子）：

```json
GET /my_index/_search
{
   "query": {
        "match": {
            "title": "jumping rabbits"
        }
    }
}
```
因为有了 `english` 分析器，这个查询是在查找以 jump 和 rabbit 这两个被提取词的文档。两个文档的 title 字段都同时包括这两个词，所以两个文档得到的评分也相同：

```json
{
  "hits": [
     {
        "_id": "1",
        "_score": 0.42039964,
        "_source": {
           "title": "My rabbit jumps"
        }
     },
     {
        "_id": "2",
        "_score": 0.42039964,
        "_source": {
           "title": "Jumping jack rabbits"
        }
     }
  ]
}
```

如果只是查询 `title.std` 字段，那么只有文档 2 是匹配的。尽管如此，如果同时查询两个字段，然后使用 `bool` 查询将评分结果 合并 ，那么两个文档都是匹配的（ `title` 字段的作用），而且文档 2 的相关度评分更高（ `title.std` 字段的作用）：

```json
GET /my_index/_search
{
   "query": {
        "multi_match": {
            "query":  "jumping rabbits",
            "type":   "most_fields", 
            "fields": [ "title", "title.std" ]
        }
    }
}
```

我们希望将所有匹配字段的评分合并起来，所以使用 `most_fields` 类型。这让 `multi_match` 查询用 `bool` 查询将两个字段语句包在里面，而不是使用 `dis_max` (最佳字段) 查询。

```json
{
  "hits": [
     {
        "_id": "2",
        "_score": 0.8226396, 
        "_source": {
           "title": "Jumping jack rabbits"
        }
     },
     {
        "_id": "1",
        "_score": 0.10741998, 
        "_source": {
           "title": "My rabbit jumps"
        }
     }
  ]
}
```
文档 2 现在的评分要比文档 1 高。

用广度匹配字段 `title` 包括尽可能多的文档——以提升召回率——同时又使用字段 `title.std` 作为 信号 将相关度更高的文档置于结果顶部。

每个字段对于最终评分的贡献可以通过自定义值 `boost` 来控制。比如，使 `title` 字段更为重要，这样同时也降低了其他信号字段的作用：

```json
GET /my_index/_search
{
   "query": {
        "multi_match": {
            "query":       "jumping rabbits",
            "type":        "most_fields",
            "fields":      [ "title^10", "title.std" ] 
        }
    }
}
```
title 字段的 boost 的值为 10 使它比 title.std 更重要。

## 参考资料
> - [Elasticsearch: 权威指南 » 深入搜索 » 多字段搜索 » 多数字段](https://www.elastic.co/guide/cn/elasticsearch/guide/current/most-fields.html)
