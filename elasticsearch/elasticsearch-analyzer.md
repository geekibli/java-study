---
title: elasticsearch-analyzer
toc: true
date: 2021-07-06 15:02:01
tags: elasticsearch
categories: [Elasticsearch, Getting Started]
---

## 测试常见分析器

`GET /_analyze`
```json
{
  "analyzer": "standard",
  "text": "Oredr it now from Amazon  #fun #girlpower #fatscooter #Fat #Cats"
}
```

`GET /_analyze`  
```json
{
  "analyzer": "english",
  "text": "Oredr it now from Amazon  #fun #girlpower #fatscooter #Fat #Cats"
}

```

`GET /_analyze`   
```json
{
  "analyzer": "simple",
  "text": "Oredr it now from Amazon  #fun #girlpower #fatscooter #Fat Cats"
}
```

`GET /_analyze`
```json
{
  "analyzer": "stop",
  "text": "Oredr it now from Amazon  #fun #girlpower #fatscooter #Fat Cats"
}
```



## 默认分析器

虽然我们可以在字段层级指定分析器，但是如果该层级没有指定任何的分析器，那么我们如何能确定这个字段使用的是哪个分析器呢？  

分析器可以从三个层面进行定义：按字段（per-field）、按索引（per-index）或全局缺省（global default）。Elasticsearch 会按照以下顺序依次处理，直到它找到能够使用的分析器。索引时的顺序如下：

- 字段映射里定义的 analyzer ，否则
- 索引设置中名为 default 的分析器，默认为
- standard 标准分析器

在搜索时，顺序有些许不同：

- 查询自己定义的 analyzer ，否则
- 字段映射里定义的 analyzer ，否则
- 索引设置中名为 default 的分析器，默认为
- standard 标准分析器
  
有时，在索引时和搜索时使用不同的分析器是合理的。我们可能要想为同义词建索引（例如，所有 quick 出现的地方，同时也为 fast 、 rapid 和 speedy 创建索引）。但在搜索时，我们不需要搜索所有的同义词，取而代之的是寻找用户输入的单词是否是 quick 、 fast 、 rapid 或 speedy 。

为了区分，Elasticsearch 也支持一个可选的 `search_analyzer` 映射，它仅会应用于搜索时（ `analyzer` 还用于索引时）。还有一个等价的 `default_search` 映射，用以指定索引层的默认配置。

如果考虑到这些额外参数，一个搜索时的 完整 顺序会是下面这样：

查询自己定义的 `analyzer` ，否则
字段映射里定义的 `search_analyzer` ，否则
字段映射里定义的 `analyzer` ，否则
索引设置中名为 `default_search` 的分析器，默认为
索引设置中名为 `default` 的分析器，默认为`standard` 标准分析器

## 保持简单

多数情况下，会提前知道文档会包括哪些字段。最简单的途径就是在创建索引或者增加类型映射时，为每个全文字段设置分析器。这种方式尽管有点麻烦，但是它让我们可以清楚的看到每个字段每个分析器是如何设置的。

通常，多数字符串字段都是 `not_analyzed` 精确值字段，比如标签（tag）或枚举（enum），而且更多的全文字段会使用默认的 `standard` 分析器或 `english` 或其他某种语言的分析器。这样只需要为少数一两个字段指定自定义分析：或许标题 title 字段需要以支持 输入即查找（find-as-you-type） 的方式进行索引。

可以在索引级别设置中，为绝大部分的字段设置你想指定的 `default` 默认分析器。然后在字段级别设置中，对某一两个字段配置需要指定的分析器。

📒 📒 📒
对于和时间相关的日志数据，通常的做法是每天自行创建索引，由于这种方式不是从头创建的索引，仍然可以用 索引模板（Index Template） 为新建的索引指定配置和映射。



## 参考资料
> - [Elasticsearch: 权威指南 » 深入搜索 » 全文搜索 » 控制分析](https://www.elastic.co/guide/cn/elasticsearch/guide/current/_controlling_analysis.html)

