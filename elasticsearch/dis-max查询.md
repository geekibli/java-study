---
title: dis_max查询
toc: true
date: 2021-07-06 20:09:23
tags: elasticsearch
categories: [Elasticsearch, Search in Depth]
---


假设有个网站允许用户搜索博客的内容，以下面两篇博客内容文档为例：

```json
PUT /my_index/my_type/1
{
    "title": "Quick brown rabbits",
    "body":  "Brown rabbits are commonly seen."
}

PUT /my_index/my_type/2
{
    "title": "Keeping pets healthy",
    "body":  "My quick brown fox eats rabbits on a regular basis."
}
```

用户输入词组 `Brown fox` 然后点击搜索按钮。事先，我们并不知道用户的搜索项是会在 title 还是在 body 字段中被找到，但是，用户很有可能是想搜索相关的词组。用肉眼判断，文档 2 的匹配度更高，因为它同时包括要查找的两个词：

现在运行以下 bool 查询：

```json
{
    "query": {
        "bool": {
            "should": [
                { "match": { "title": "Brown fox" }},
                { "match": { "body":  "Brown fox" }}
            ]
        }
    }
}
```
但是我们发现查询的结果是文档 1 的评分更高：

<img src="https://oscimg.oschina.net/oscnet/up-fa805607436bd64629e31ea8ea574c330cf.png" width=920 height=450>  

为了理解导致这样的原因，需要回想一下 bool 是如何计算评分的：

它会执行 should 语句中的两个查询。
加和两个查询的评分。
乘以匹配语句的总数。
除以所有语句总数（这里为：2）。

文档 1 的两个字段都包含 `brown` 这个词，所以两个 `match` 语句都能成功匹配并且有一个评分。文档 2 的 body 字段同时包含 `brown` 和 `fox` 这两个词，但 title 字段没有包含任何词。这样， body 查询结果中的高分，加上 title 查询中的 0 分，然后乘以二分之一，就得到比文档 1 更低的整体评分。

在本例中， title 和 body 字段是相互竞争的关系，所以就需要找到单个 最佳匹配 的字段。

如果不是简单将每个字段的评分结果加在一起，而是将 `最佳匹配` 字段的评分作为查询的整体评分，结果会怎样？这样返回的结果可能是： 同时 包含 `brown` 和 `fox` 的单个字段比反复出现相同词语的多个不同字段有更高的相关度。



## dis_max 查询

不使用 `bool` 查询，可以使用 `dis_max` 即分离 最大化查询 `（Disjunction Max Query）` 。分离（Disjunction）的意思是 或（or） ，这与可以把结合（conjunction）理解成 与（and） 相对应。分离最大化查询（Disjunction Max Query）指的是： 将任何与任一查询匹配的文档作为结果返回，但只将最佳匹配的评分作为查询的评分结果返回 ：

```json
{
    "query": {
        "dis_max": {
            "queries": [
                { "match": { "title": "Brown fox" }},
                { "match": { "body":  "Brown fox" }}
            ]
        }
    }
}
```

得到我们想要的结果为：
<img src="https://oscimg.oschina.net/oscnet/up-f4e15cd66644b0031abe4757efdcfdd2886.png" width=900 height=450>  


## Top-level parameters for dis_maxedit
- `queries`
(Required, array of query objects) Contains one or more query clauses. Returned documents must match one or more of these queries. If a document matches multiple queries, Elasticsearch uses the highest relevance score.
- `tie_breaker`
(Optional, float) Floating point number between 0 and 1.0 used to increase the relevance scores of documents matching multiple query clauses. Defaults to 0.0.

You can use the tie_breaker value to assign higher relevance scores to documents that contain the same term in multiple fields than documents that contain this term in only the best of those multiple fields, without confusing this with the better case of two different terms in the multiple fields.

If a document matches multiple clauses, the dis_max query calculates the relevance score for the document as follows:

Take the relevance score from a matching clause with the highest score.
Multiply the score from any other matching clauses by the tie_breaker value.
Add the highest score to the multiplied scores.
If the tie_breaker value is greater than 0.0, all matching clauses count, but the clause with the highest score counts most.


`dis_max`，只是取分数最高的那个query的分数而已，完全不考虑其他query的分数，这种一刀切的做法，可能导致在有其他query的影响下，score不准确的情况，这时为了使用结果更准确，最好还是要考虑到其他query的影响;
使用 `tie_breaker` 将其他query的分数也考虑进去, `tie_breaker` 参数的意义，将其他query的分数乘以`tie_breaker`，然后综合考虑后与最高分数的那个query的分数综合在一起进行计算，这样做除了取最高分以外，还会考虑其他的query的分数。`tie_breaker`的值，设置在在0~1之间，是个小数就行，没有固定的值

## 参考资料
> - [Elasticsearch: 权威指南 » 深入搜索 » 多字段搜索 » 最佳字段](https://www.elastic.co/guide/cn/elasticsearch/guide/current/_best_fields.html)
> - [Elasticsearch中文文档](https://learnku.com/docs/elasticsearch73/7.3)
> - [Elasticsearch Guide [7.x] » Query DSL » Compound queries » Disjunction max query](https://www.elastic.co/guide/en/elasticsearch/reference/7.x/query-dsl-dis-max-query.html)
