---
title: canal同步es后部分字段为null
toc: true
date: 2021-07-06 16:11:15
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---


## 现象

<img src="https://oscimg.oschina.net/oscnet/up-54f2fa4a9dfb35acb02b77c5c6bc8c84c0f.png" width=800 height=400>


配置文件如下：
```
dataSourceKey: defaultDS        # 源数据源的key, 对应上面配置的srcDataSources中的值
destination: example            # cannal的instance或者MQ的topic
groupId: g1 # 对应MQ模式下的groupId, 只会同步对应groupId的数据
esMapping:
  _index: rd_member_fans_info           # es 的索引名称
  _type: _doc                   # es 的doc名称
  _id: _id                      # es 的_id, 如果不配置该项必须配置下面的pk项_id则会由es自动分配
#  pk: id                       # 如果不需要_id, 则需要指定一个属性为主键属性
#  # sql映射
  sql: 'SELECT t.id as _id , t.redtom_id ,t.fans_redtom_id,t.fans_username,t.fans_introduce,t.fans_avatar,t.is_each_following,t.follow_channel,t.create_time,t.update_time,t.`status` FROM rd_member_fans_info t'
#  objFields:
#    _labels: array:;           # 数组或者对象属性, array:; 代表以;字段里面是以;分隔的
#    _obj: object               # json对象
  etlCondition: "where t.update_time>={}" # etl 的条件参数
  commitBatch: 3000 # 提交批大小
```

⚠️ ⚠️
sql执行是没有问题的！

<img src="https://oscimg.oschina.net/oscnet/up-24a77ccd81ae7be4c6d4434f3d1d28edea0.png" width=800 height=400>


canal-adapter 获取binlog数据也没有问题，显示日志如下：

```log
2021-07-06 15:39:24.588 [pool-1-thread-1] DEBUG c.a.o.canal.client.adapter.es.core.service.ESSyncService - DML: {"data":[{"id":3,"redtom_id":1,"fans_redtom_id":1,"fans_username":"1","fans_introduce":"1","fans_avatar":"1","is_each_following":1,"follow_channel":1,"create_time":1625556851000,"update_time":1625556851000,"status":2}],"database":"redtom_dev","destination":"example","es":1625557164000,"groupId":"g1","isDdl":false,"old":null,"pkNames":["id"],"sql":"","table":"rd_member_fans_info","ts":1625557164587,"type":"INSERT"}
```


然后看一下我创建索引的mapping
<img src="https://oscimg.oschina.net/oscnet/up-8eac2e46423574ae04cd8694d2ac7389530.png" width=800 height=400>  



## 解决方法

调整sql如下： 

`SELECT t.id as _id , t.redtom_id ,t.fans_redtom_id,t.fans_username,t.fans_introduce,t.fans_avatar,t.is_each_following,t.follow_channel,t.`status` as is_deleted , t.create_time,t.update_time FROM rd_member_fans_info t`

调整了那些东西呢？     `status` 的顺序提前而已！


## 测试

执行一下命令：
`curl http://127.0.0.1:8081/etl/es7/rd_member_fans_info.yml -X POST`

canal-adapter 日志如下：
```log
2021-07-06 16:21:33.519 [http-nio-8081-exec-1] INFO  c.a.otter.canal.client.adapter.es7x.etl.ESEtlService - start etl to import data to index: rd_member_fans_info
2021-07-06 16:21:33.527 [http-nio-8081-exec-1] INFO  c.a.otter.canal.client.adapter.es7x.etl.ESEtlService - 数据全量导入完成, 一共导入 3 条数据, 耗时: 7
```

查看es数据：

<img src="https://oscimg.oschina.net/oscnet/up-69c4ac09e2b8fcd27a5b990e378664d6568.png" width=800 height=400>
