---
title: elasticsearch分页查询
toc: true
date: 2021-07-05 22:08:23
tags: elasticsearch
categories: [Elasticsearch, Getting Started]
---

和 SQL 使用 LIMIT 关键字返回单个 page 结果的方法相同，Elasticsearch 接受 from 和 size 参数：

`size`
显示应该返回的结果数量，默认是 10
`from`
显示应该跳过的初始结果数量，默认是 0
如果每页展示 5 条结果，可以用下面方式请求得到 1 到 3 页的结果：

`GET /_search?size=5`  
`GET /_search?size=5&from=5`  
`GET /_search?size=5&from=10`  

⚠️ ⚠️ ⚠️
考虑到分页过深以及一次请求太多结果的情况，结果集在返回之前先进行排序。 但请记住一个请求经常跨越多个分片，每个分片都产生自己的排序结果，这些结果需要进行集中排序以保证整体顺序是正确的。  

在分布式系统中深度分页

> 理解为什么深度分页是有问题的，我们可以假设在一个有 5 个主分片的索引中搜索。 当我们请求结果的第一页（结果从 1 到 10 ），每一个分片产生前 10 的结果，并且返回给 协调节点 ，协调节点对 50 个结果排序得到全部结果的前 10 个。

现在假设我们请求第 1000 页—​结果从 10001 到 10010 。所有都以相同的方式工作除了每个分片不得不产生前10010个结果以外。 然后协调节点对全部 50050 个结果排序最后丢弃掉这些结果中的 50040 个结果。

可以看到，在分布式系统中，对结果排序的成本随分页的深度成指数上升。这就是 web 搜索引擎对任何查询都不要返回超过 1000 个结果的原因




## 参考资料
 - [elasticsearch权威指南](https://www.elastic.co/guide/cn/elasticsearch/guide/current/pagination.html)  
 - [干货 | 全方位深度解读 Elasticsearch 分页查询](https://blog.csdn.net/laoyang360/article/details/116472697?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522162549431316780269873046%2522%252C%2522scm%2522%253A%252220140713.130102334.pc%255Fblog.%2522%257D&request_id=162549431316780269873046&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~blog~first_rank_v2~rank_v29-2-116472697.pc_v2_rank_blog_default&utm_term=%E5%88%86%E9%A1%B5&spm=1018.2226.3001.4450)  
 - [Paginate search results](https://www.elastic.co/guide/en/elasticsearch/reference/7.x/paginate-search-results.html)
 