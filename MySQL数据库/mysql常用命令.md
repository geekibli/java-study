---
title: mysql常用命令
toc: true
date: 2021-07-05 11:06:36
tags: MySQL
categories: [DataBase,MySQL]
---

## binlog相关命令
`mysql> show variables like 'binlog_format';`

```
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| binlog_format | ROW   |
+---------------+-------+
```
`show variables like 'log_bin';`

```
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_bin       | ON    |
+---------------+-------+
```



## 用户&权限
### 创建用户并授权
root用户登录： mysql -u root -p 然后输入密码  
创建用户： `create user 'yjuser'@'%' identified by 'u-bx.com';`  
授权用户只读权限： `grant SELECT on mirror.* to 'yjuser'@'%' IDENTIFIED by 'u-bx.com';`  
刷新权限：`flush privileges;`

### 查看当前用户
`select User();`



### Mac下配置文件

首先，查看mysql读取配置文件的默认顺序

`mysqld --help --verbose | more `
查看帮助，下翻，会看到表示配置文件默认读取顺序，如下：

Default options are read from the following files in the given order:
`/etc/my.cnf /etc/mysql/my.cnf /usr/local/etc/my.cnf ~/.my.cnf`
通常，这些位置上没有配置文件，所以需要创建文件，用下面的命令查找一个样例文件

`ls $(brew --prefix mysql)/support-files/my-* `
找到后，将这个配置文件拷贝到第一个默认读取目录下，用如下命令：

`cp /usr/local/opt/mysql/support-files/my-default.cnf /etc/my.cnf` 或者

`cp /usr/local/mysql/support-files/my-default.cnf /etc/my.cnf` 看你mysql的位置

然后，按需修改 `my.cnf` 文件。

修改完成后，需要重新启动mysql服务

brew services start mysql (启动)
brew services stop mysql (停止)  
brew services restart mysql(重启)
