---
title: elasticsearch统计每年每小时访问量
toc: true
date: 2021-07-19 22:53:39
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---

> 需求背景，要统计文章在一年的时间内，每个小时的访问情况，按照0点举例子，每个文章，一年内每一天0点的访问次数累加起来；



## Elasticsearch索引如下
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
        },
        "c" : {
          "type" : "keyword"
        },
        "ip" : {
          "type" : "keyword"
        },
        "p" : {
          "type" : "keyword"
        },
        "ua" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "visitTime" : {
          "type" : "date"
        }
      }
    }
  }
}

```




## Java RestHighLevelClient写法

```java
public void getDateDist() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("yj_visit_data2");
        TermsAggregationBuilder termsAggregation = AggregationBuilders.terms("article")
                .field("article.keyword").size(2200)
                .subAggregation(AggregationBuilders.dateHistogram("visitTime")
                        .field("visitTime")
                        .calendarInterval(DateHistogramInterval.HOUR));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.aggregation(termsAggregation);
        sourceBuilder.query(QueryBuilders.rangeQuery("visitTime").gt("1609430400000").lte("1625068799000"));
        sourceBuilder.timeout(new TimeValue(900000));
        SearchRequest request = new SearchRequest();
        request.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = search.getAggregations();

        log.info("agg -> {}", aggregations.asList().size());
        List<? extends Terms.Bucket> buckets = ((ParsedStringTerms) aggregations.asList().get(0)).getBuckets();
        List<ArticleHourData> hourDataList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            List<? extends Histogram.Bucket> innerBuckets = ((ParsedDateHistogram) bucket.getAggregations().asList().get(0)).getBuckets();
            hourDataList.add(calcBucket(innerBuckets, bucket.getKeyAsString()));
        }
        log.info("result ----> {}", JSONObject.toJSONString(hourDataList));
    }

```
### 聚合分析
```java
 public ArticleHourData calcBucket(List<? extends Histogram.Bucket> innerBuckets, String article) {
        log.info("innerBuckets get(0) ---> {}", JSON.toJSONString(innerBuckets.get(0)));
        Map<String, ? extends List<? extends Histogram.Bucket>> hourMap = innerBuckets.stream()
                .collect(Collectors.groupingBy(bucket -> getHour(bucket.getKeyAsString())));
        log.info("collect  ======> {} ", JSONObject.toJSONString(hourMap.keySet()));

        ArticleHourData hourData = ArticleHourData.builder().article(article).build();
        if (hourMap.isEmpty()) {
            return hourData;
        }
        HashMap<String, Long> hashMap = new HashMap<>();
        for (String hour : hourMap.keySet()) {
            List<? extends Histogram.Bucket> list = hourMap.get(hour);
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            hashMap.put(hour, list.stream().mapToLong(Histogram.Bucket::getDocCount).sum());
        }
        hourData.setCountMap(hashMap);
        return hourData;
    }
```
### 获取时间的小时

```java
  private String getHour(String date) {
        date = date.replace("Z", " UTC");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        Date d = null;
        try {
            d = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return String.valueOf(DateUtil.asLocalDateTime(d).getHour());
    }
```


## Python写法

```python

from json.decoder import JSONDecoder
from elasticsearch import Elasticsearch
import logging,json
from datetime import datetime

es = Elasticsearch([{'host':'39.107.117.232','port':9200}], http_auth=('elastic', 'elastic'), timeout = 90000)


sqs = {
   "size" : 0,
   "aggs": {
      "art": {
         "terms": {
            "field": "article.keyword",
            "size": 5
         },
         "aggs": {
            "art_total": { 
               "value_count": {
                  "field": "article.keyword"
               }
            },
            "_time": { 
                "date_histogram": {
                    "field": "visitTime",
                    "calendar_interval": "hour"
                }
            }
         }
      }
   }
}

_search_result = es.search(index="yj_visit_data2" , body=sqs)
_result_json = json.dumps(_search_result,sort_keys=True, indent=4, separators=(', ', ': '), ensure_ascii=False)

aggregations = _search_result['aggregations']
art = aggregations['art']
buckets = art['buckets']
#print(type(buckets)) ; print(buckets)


def getHour(time):
    return (int)(time[11:13])


# 计算每个小时的点击数
def countByMonth(dataList , hourTar):
    _count = 0
    for data in dataList:
        timestamp = data['key_as_string']
        hour = getHour(timestamp)
        if hour == hourTar: 
            _count = (int)(data['doc_count']) + _count   
    return _count        
            
   
final_list = []
# 循环计算每一个文章
for outBucket in buckets: 
   simple_result = {}
   _time = outBucket['_time']
   innerBuckets = _time['buckets']
   print("time inner bucker size" , len(innerBuckets))
   simple_list = []
   for num in range(0,24):
      simple_list.append(countByMonth(innerBuckets,num))
   simple_result[0] = outBucket['key']
   simple_result[1] = simple_list
   final_list.append(simple_result)
print("final result ----> ",final_list)   

```