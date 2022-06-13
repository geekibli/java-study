---
title: kibana添加用户及控制权限
toc: true
date: 2021-07-15 23:53:44
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---

> 操作步骤： 【修改elasticsearch配置文件】 -> 【重启elasticsearch】 -> 【初始化账号&密码】 -> 【修改kibana配置文件】 -> 【重启kibana】 -> 【初始账号登录kibana】 -> 【创建、配置角色】 -> 【创建新用户】

## 配置elasticsearch
开启自带的xpack的验证功能
`xpack.security.enabled: true`

配置单节点模式
`discovery.type: single-node`


### 坑1: 集群节点配置报错
`cluster.initial_master_nodes`

### 坑2: 本次存储节点配置报错
maybe these locations are not writable or multiple nodes were started without increasing [node.max_local_storage_nodes] (was [1])?
`node.max_local_storage_nodes: 2`



## 为内置账号设置密码
在elasticsearch的bin下执行以下命令：
`./elasticsearch-setup-passwords interactive`

<img src="https://oscimg.oschina.net/oscnet/up-2073ba41d73f8d4b16cd39e420ee017f62d.png" width="800" height='480'>


## 配置kibana
```
#使用初始用户kibana
elasticsearch.username: "kibana_system"
elasticsearch.password: "密码"
```

重启kibana之后使用初始账号 `elastic` 登录

## 创建、配置角色
<img src='https://oscimg.oschina.net/oscnet/up-be2569d2151c190b3f49f2faebae05dd19a.png' width=840 height=750>


## 创建新用户
<img src='https://oscimg.oschina.net/oscnet/up-b662c15522750b55863cf006e9c8a9d63c4.png' width=840 height=750>


## 查看新用户访问界面
角色里面配置了kibana的访问权限，只开通了discover和dashboard两个入口
<img src='https://oscimg.oschina.net/oscnet/up-3371b430006a3b65cffc8a41f04cb02f423.png' width=800 height=450>

索引的话，有好几个索引，但是只配置了一个索引的权限
<img src='https://oscimg.oschina.net/oscnet/up-10b5d2b6e179b6c200c691631dd43fc1713.png' width=800 height=450>





## 参考资料
> - [kibana7.2添加登录及权限](https://blog.csdn.net/QiaoRui_/article/details/97375237)
> - [Elasticsearch 安装](https://www.cnblogs.com/tq1226112215/p/8435127.html)
