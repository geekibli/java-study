---
title: field name is null or empty
toc: true
date: 2021-07-06 12:53:44
tags: elasticsearch
categories: [Elasticsearch, Other Question]
---


## canal adapter 报错信息

```log
2021-07-06 12:46:31.959 [http-nio-8081-exec-2] INFO  o.a.catalina.core.ContainerBase.[Tomcat].[localhost].[/] - Initializing Spring FrameworkServlet 'dispatcherServlet'
2021-07-06 12:46:31.959 [http-nio-8081-exec-2] INFO  org.springframework.web.servlet.DispatcherServlet - FrameworkServlet 'dispatcherServlet': initialization started
2021-07-06 12:46:31.968 [http-nio-8081-exec-2] INFO  org.springframework.web.servlet.DispatcherServlet - FrameworkServlet 'dispatcherServlet': initialization completed in 9 ms
2021-07-06 12:46:31.995 [http-nio-8081-exec-2] INFO  c.a.otter.canal.client.adapter.es7x.etl.ESEtlService - start etl to import data to index: rd_member
2021-07-06 12:46:32.027 [http-nio-8081-exec-2] ERROR c.a.otter.canal.client.adapter.es7x.etl.ESEtlService - field name is null or empty
java.lang.IllegalArgumentException: field name is null or empty
	at org.elasticsearch.index.query.BaseTermQueryBuilder.<init>(BaseTermQueryBuilder.java:113) ~[na:na]
	at org.elasticsearch.index.query.TermQueryBuilder.<init>(TermQueryBuilder.java:75) ~[na:na]
	at org.elasticsearch.index.query.QueryBuilders.termQuery(QueryBuilders.java:202) ~[na:na]
	at com.alibaba.otter.canal.client.adapter.es7x.etl.ESEtlService.lambda$executeSqlImport$1(ESEtlService.java:141) ~[na:na]
	at com.alibaba.otter.canal.client.adapter.support.Util.sqlRS(Util.java:60) ~[client-adapter.common-1.1.5-SNAPSHOT.jar:na]
	at com.alibaba.otter.canal.client.adapter.es7x.etl.ESEtlService.executeSqlImport(ESEtlService.java:64) ~[na:na]
	at com.alibaba.otter.canal.client.adapter.support.AbstractEtlService.importData(AbstractEtlService.java:105) ~[client-adapter.common-1.1.5-SNAPSHOT.jar:na]
	at com.alibaba.otter.canal.client.adapter.es7x.etl.ESEtlService.importData(ESEtlService.java:56) ~[na:na]
	at com.alibaba.otter.canal.client.adapter.es7x.ES7xAdapter.etl(ES7xAdapter.java:79) ~[na:na]
	at com.alibaba.otter.canal.adapter.launcher.rest.CommonRest.etl(CommonRest.java:100) ~[client-adapter.launcher-1.1.5-SNAPSHOT.jar:na]
	at com.alibaba.otter.canal.adapter.launcher.rest.CommonRest.etl(CommonRest.java:123) ~[client-adapter.launcher-1.1.5-SNAPSHOT.jar:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_292]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_292]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_292]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_292]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke
```


## 问题排查

操作是向数据库中插入一条数据，通过canal-adapter同步到elasticsearch中，接口发生以上错误！  
现象是canal-adapter检测到和mysql的数据变化，但是同步到es的时候发生了错误；  
猜想大概是某个为空导致存到es的时候发生异常；

然后查看es7下的mapping配置：

<img src="https://oscimg.oschina.net/oscnet/up-93dc9d1f9074d9d44946f2d5c3780dd7e13.png" width=400 height=300>

发现我的sql查id的时候写错了，别名应该写成_id,对应elasticsearch的_id


