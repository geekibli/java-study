---
title: MySQL-主从同步原理以及实现
toc: true
date: 2021-08-27 16:53:05
tags: MySQL
categories:
---



## 主从复制的原理

我们在平时工作中，使用最多的数据库就是 `MySQL` 了，随着业务的增加，如果单单靠一台服务器的话，负载过重，就容易造成**宕机**。

这样我们保存在 MySQL 数据库的数据就会丢失，那么该怎么解决呢？

其实在 MySQL 本身就自带有一个主从复制的功能，可以帮助我们实现**负载均衡和读写分离**。

对于主服务器（Master）来说，主要负责写，从服务器（Slave）主要负责读，这样的话，就会大大减轻压力，从而提高效率。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210827173121932.png" alt="image-20210827173121932" style="zoom:33%;" />



MySQL 的主从复制工作过程大致如下：

1. 从库生成**两个线程**，一个 I/O 线程，一个 SQL 线程；
2. I/O 线程去请求主库的 binlog，并将得到的 binlog 日志**写到** relay log(中继日志) 文件中；
3. 主库会**生成**一个 log dump 线程，用来给从库 I/O 线程传 binlog；
4. SQL 线程会读取 relay log 文件中的日志，并**解析**成具体操作，来实现主从的操作一致，而最终数据一致；

#### 请求流程

MySQL 建立请求的主从的详细流程如下：

1. 当从服务器连接主服务器时，主服务器会创建一个 log dump 线程，用于发送 binlog 的内容。在读取 binlog 的内容的操作中，会对象主节点上的 binlog **加锁**，当读取完成并发送给从服务器后解锁。
2. 当从节点上执行 `start slave` 命令之后，从节点会创建一个 IO 线程用来连接主节点，请求主库中**更新**binlog。IO 线程接收主节点 binlog dump 进程发来的更新之后，保存到 relay-log 中。
3. 从节点 SQL 线程负责读取 realy-log 中的内容，**解析**成具体的操作执行，最终保证主从数据的一致性。





## 主从同步实现

我这里实现了一主一从。数据库版本都是用的MySQL5.8。

### 设置主库

#### 设置配置文件

```shell
log-bin=mysql-bin #添加这一行就ok
binlog-format=ROW #选择row模式
server_id=1 #配置mysql replaction需要定义
```

#### 创建同步用户

```sql
# 创建用户
create user 'repl'@'*' identified by 'repl';

# 授权，只授予复制和客户端访问权限
grant replication slave on *.* to 'repl'@'*';#分配权限
```

#### 查看binlog状态

`SHOW MASTER STATUS;`

![image-20210827170105244](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210827170105244.png)

Position一开始同步之前是在 2729 位置。

### 配置从库Slave

#### 修改配置文件

```shell
log-bin=mysql-bin #添加这一行就ok
binlog-format=ROW #选择row模式
server_id=2 
```

#### 设置同步信息

```sql
CHANGE MASTER TO MASTER_HOST='123.56.77.177', 
        MASTER_USER='repl', 
        MASTER_PASSWORD='repl',
        MASTER_LOG_FILE='binlog.000001', 
        MASTER_LOG_POS=2729, 
        Master_Port = 3306;
```

执行这个SQL必须是在 `slave not running` 状态下可以运行，可以 `stop slave`命令来停止slave。

#### 启动slave

`mysql>start slave;`

#### 查看slave状态

`mysql> show slave status\G`

![image-20210827170515304](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210827170515304.png)



⚠️ 只有当 `Slave_IO_Running: Yes` 和 `Slave_SQL_Running: Yes` 同时是 yes的时候，才可以进行同步。

#### Slave_IO_Running: No 可能的原因

1、端口不同

`ping ip` 如果是docker启动的mysql的话，需要退出docker来测试端口。

`telnet ip port`

2、没有权限

`grant replication slave, replication client on *.* to  'repl'@'%';`

3、binlog文件写错

可以查看 ` Last_IO_Error` 的错误提示，我这里遇到一个错误，主库binlog文件是binlog.000001 ，但是我直接粘贴别人博客的时候，没有注意，博客上面写的是mysql-bin.000001。



## 同步效果测试

#### 分三个步骤测试

**1、测试数据库**

**2、测试表**

**3、测试表数据**

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210827171817248.png" alt="image-20210827171817248" style="zoom:50%;" />



## 参考资料

主从同步的类型 （异步 同步 半同步 延迟）

https://juejin.cn/post/6967224081410162696
