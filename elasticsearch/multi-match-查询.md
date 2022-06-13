---
title: multi_match 查询
toc: true
date: 2021-07-06 20:37:26
tags: elasticsearch
categories: [Elasticsearch, Search in Depth]
---

multi_match 查询为能在多个字段上反复执行相同查询提供了一种便捷方式。

📒 📒 📒
> multi_match 多匹配查询的类型有多种，其中的三种恰巧与 了解我们的数据 中介绍的三个场景对应，即：`best_fields` 、 `most_fields` 和 `cross_fields` （最佳字段、多数字段、跨字段）。

默认情况下，查询的类型是 `best_fields` ，这表示它会为每个字段生成一个 `match` 查询，然后将它们组合到 `dis_max` 查询的内部，如下：

```json
{
  "dis_max": {
    "queries":  [
      {
        "match": {
          "title": {
            "query": "Quick brown fox",
            "minimum_should_match": "30%"
          }
        }
      },
      {
        "match": {
          "body": {
            "query": "Quick brown fox",
            "minimum_should_match": "30%"
          }
        }
      },
    ],
    "tie_breaker": 0.3
  }
}
```
上面这个查询用 multi_match 重写成更简洁的形式：

```json
{
    "multi_match": {
        "query":                "Quick brown fox",
        "type":                 "best_fields", 
        "fields":               [ "title", "body" ],
        "tie_breaker":          0.3,
        "minimum_should_match": "30%" 
    }
}
```
⚠️ ⚠️ ⚠️
- best_fields 类型是默认值，可以不指定。
- 如 minimum_should_match 或 operator 这样的参数会被传递到生成的 match 查询中。


## 查询字段名称的模糊匹配
字段名称可以用 `模糊匹配` 的方式给出：任何与模糊模式正则匹配的字段都会被包括在搜索条件中，例如可以使用以下方式同时匹配 `book_title` 、 `chapter_title` 和 `section_title` （书名、章名、节名）这三个字段：

```json
{
    "multi_match": {
        "query":  "Quick brown fox",
        "fields": "*_title"
    }
}
```

## 提升单个字段的权重

可以使用 ^ 字符语法为单个字段提升权重，在字段名称的末尾添加 ^boost ，其中 boost 是一个浮点数：

```json
{
    "multi_match": {
        "query":  "Quick brown fox",
        "fields": [ "*_title", "chapter_title^2" ] 
    }
}
```
	
chapter_title 这个字段的 boost 值为 2 ，而其他两个字段 book_title 和 section_title 字段的默认 boost 值为 1 。

## 参考资料
> - [Elasticsearch: 权威指南 » 深入搜索 » 多字段搜索 » multi_match 查询](https://www.elastic.co/guide/cn/elasticsearch/guide/current/multi-match-query.html)
> - [Elasticsearch Guide [7.x] » Query DSL » Full text queries » Multi-match query](https://www.elastic.co/guide/en/elasticsearch/reference/7.x/query-dsl-multi-match-query.html)
