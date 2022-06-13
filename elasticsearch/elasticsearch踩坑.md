---
title: elasticsearch踩坑
toc: true
date: 2021-07-19 17:46:59
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---

## search.max_buckets
This limit can be set by changing the [search.max_buckets] cluster level setting.]];

```json
PUT _cluster/settings
{
  "persistent": {
    "search.max_buckets": 100000
  }
}
```


## Required one of fields [field, script]
[Elasticsearch exception [type=illegal_argument_exception, reason=Required one of fields [field, script], but none were specified. ]

```java
  @Test
    public void test1() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(QueryBuilders.matchAllQuery(), null);
        Script script = new Script("doc['article.keyword']");
        nativeSearchQuery.addAggregation(AggregationBuilders.terms("art")
                .field("article.keyword").size(10)
                .subAggregation(AggregationBuilders.dateHistogram("visitTime")
                        .field("visitTime") // 这一行没有写
                        .calendarInterval(DateHistogramInterval.HOUR)));
        SearchHits<YjVisitData> search = elasticsearchRestTemplate.search(nativeSearchQuery, YjVisitData.class);
        System.err.println("search.getAggregations().asList() {}" + search.getAggregations().asList().size());
    }
```



