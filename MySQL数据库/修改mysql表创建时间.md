---
title: 修改mysql表创建时间
toc: true
date: 2021-07-14 21:42:53
tags: MySQL
categories: [DataBase,MySQL]
---


- 修改服务器时间
`date -s "2021-07-14 21:22:10"`

- 执行DDL
`alter table mirror_user comment '用户表';`

- 服务器时间修正
`ntpdate ntp1.aliyun.com`

