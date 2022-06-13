---
title: elasticsearch 查询值前缀不包含某个字符串
toc: true
date: 2021-07-20 21:48:31
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---

> 需求 查询IP不是以11.开头的所有文档，然后获取文档访问量前100条

`curl -X GET "localhost:9200/yj_visit_data2,yj_visit_data3/_search?pretty" -u elastic:elastic -H 'Content-Type: application/json' -d'`
```json
{
  "query": {
    "bool": {
      "must_not": [
        {
          "bool": {
            "should": [
              {
                "prefix": {
                  "ip": {
                    "value": "11."
                  }
                }
              },
              {
                "prefix": {
                  "ip": {
                    "value": "1."
                  }
                }
              }
            ]
          }
        }
      ],
      "must": [
        {
          "range": {
            "visitTime": {
              "gte": 1577808000000,
              "lte": 1609430399000
            }
          }
        }
      ]
    }
  },
  "aggs": {
    "term_article": {
      "terms": {
        "field": "ip",
        "min_doc_count": 20,
        "size": 10000
      }
    }
  }
}
'
```

