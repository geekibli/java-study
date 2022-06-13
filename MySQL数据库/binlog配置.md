---
title: mysql配置binlog
toc: true
date: 2021-07-05 11:06:36
tags: MySQL
categories: [DataBase,MySQL]
---


## 开启binlog
[mysqld]  
log-bin=mysql-bin #添加这一行就ok  
binlog-format=ROW #选择row模式  
server_id=1 #配置mysql replaction需要定义，不能和canal的slaveId重复  

## 查看binlog状态
mysql> show variables like 'binlog_format';
```
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| binlog_format | ROW   |
+---------------+-------+
```
show variables like 'log_bin';
```
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_bin       | ON    |
+---------------+-------+
```