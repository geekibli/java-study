---
title: 'Parameter index out of range (1 > number of parameters, which is 0).'
toc: true
date: 2021-07-06 13:03:16
tags: elasticsearch
categories: [Elasticsearch,Other Question]
---


## 问题记录

```log
2021-07-06 12:39:31.179 [http-nio-8081-exec-2] INFO  c.a.otter.canal.client.adapter.es7x.etl.ESEtlService - start etl to import data to index: rd_member
2021-07-06 12:39:31.186 [http-nio-8081-exec-2] ERROR com.alibaba.otter.canal.client.adapter.support.Util - sqlRs has error, sql: SELECT COUNT(1) FROM ( select t.redtom_id as id, t.username, t.nickname, t.avatar, t.status, t.mobile, t.mobile_region_no, t.email, t.gender, t.password,t.salt,t.birthday,t.introduce,t.country,t.region,t.level,t.is_vip,t.follows    ,t.fans,t.likes_num, t.collects_num, t.instagram_account, t.youtube_account, t.facebook_account, t.twitter_account,t.create_ip, t.create_time,t.update_time from rd_member r where t.create_time>='{0}') _CNT
2021-07-06 12:39:31.188 [http-nio-8081-exec-2] ERROR c.a.otter.canal.client.adapter.es7x.etl.ESEtlService - java.sql.SQLException: Parameter index out of range (1 > number of parameters, which is 0).
java.lang.RuntimeException: java.sql.SQLException: Parameter index out of range (1 > number of parameters, which is 0).
	at com.alibaba.otter.canal.client.adapter.support.Util.sqlRS(Util.java:65) ~[client-adapter.common-1.1.5-SNAPSHOT.jar:na]
	at com.alibaba.otter.canal.client.adapter.support.AbstractEtlService.importData(AbstractEtlService.java:62) ~[client-adapter.common-1.1.5-SNAPSHOT.jar:na]
	at com.alibaba.otter.canal.client.adapter.es7x.etl.ESEtlService.importData(ESEtlService.java:56) [client-adapter.es7x-1.1.5-SNAPSHOT-jar-with-dependencies.jar:na]
	at com.alibaba.otter.canal.client.adapter.es7x.ES7xAdapter.etl(ES7xAdapter.java:79) [client-adapter.es7x-1.1.5-SNAPSHOT-jar-with-dependencies.jar:na]
	at com.alibaba.otter.canal.adapter.launcher.rest.CommonRest.etl(CommonRest.java:100) [client-adapter.launcher-1.1.5-SNAPSHOT.jar:na]
	at com.alibaba.otter.canal.adapter.launcher.rest.CommonRest.etl(CommonRest.java:123) [client-adapter.launcher-1.1.5-SNAPSHOT.jar:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_292]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_292]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_292]
```

## 如何解决

我执行的操作如下：👇
`curl http://127.0.0.1:8081/etl/es7/customer.yml -X POST -d "params=2019-08-31 00:00:00"`

但是我的 es7/rd_member.yml的配置文件如下：

`etlCondition:"where a.c_time>='{0}'" # etl 的条件参数`

应该改成：
`etlCondition:"where a.c_time>={}" # etl 的条件参数`
