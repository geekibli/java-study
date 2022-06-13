---
title: python操作elasticsearch
toc: true
date: 2021-07-18 20:20:25
tags:  Python
categories: [Develop Lan,Python]
---

## 下载python对应的elasticsearch依赖包
`pip3 install elasticsearch==7.10.0`

## python操作elasticsearch代码
```python
from elasticsearch import Elasticsearch
print("init ...")
es = Elasticsearch([{'host':'XXXXXX','port':9200}], http_auth=('elastic', 'XXXXXX'))

# print(es.get(index='yj_ip_pool', doc_type='_doc', id='9256058'))
countRes = es.count(index='yj_ip_pool')
print(countRes)
```

## 查询效果
```text
gaolei:awesome-python3-webapp gaolei$ /usr/local/opt/python/bin/python3.7 /Users/gaolei/Documents/DemoProjects/awesome-python3-webapp/www/es_test.py
init ...
{'count': 20095400, '_shards': {'total': 1, 'successful': 1, 'skipped': 0, 'failed': 0}}
gaolei:awesome-python3-webapp gaolei$ 

```