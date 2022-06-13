---
title: elasticsearch操作索引
toc: true
date: 2021-07-05 21:11:01
tags: elasticsearch
categories: [Elasticsearch, Getting Started]
---

## 创建索引
```
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

## 查看索引的mapping
`GET yj_visit_data/_mapping`
```json
{
  "yj_visit_data" : {
    "mappings" : {
      "properties" : {
        "_class" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "article" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        }
      }
    }
  }
}
```

## 查询所有
`GET yj_visit_data/_search`
```json
{
  "query": {
    "match_all": {}
  }
}
```


## 删除所有
`POST yj_visit_data/_delete_by_query`
```json
{
  "query": { 
    "match_all": {
    }
  }
}
```


## 通过文章删除
`POST yj_visit_data/_delete_by_query`
```json
{
  "query": {
    "match": {
      "article.keyword": "2019/01/3"
    }
  }
}
```

## 根据文章查询
`GET yj_visit_data/_search`
```json
{
  "query": {
    "match": {
      "article.keyword": "2019/01/3"
    }
  }
}

```


## 修改索引
```json
POST customer/_doc/1
{
  "name":"2"
}
```




