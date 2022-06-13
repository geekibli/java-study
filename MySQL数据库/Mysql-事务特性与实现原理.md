---
title: Mysql-事务特性与实现原理
toc: true
date: 2021-07-28 18:13:07
tags: MySQL
categories: [DataBase,MySQL]
---

# MySQL事务特性与实现原理

## 1. 事务特性
> 原子性(Atomicity)

事务中的所有操作作为一个整体像原子一样不可分割，要么全部成功,要么全部失败。

> 一致性(Consistency)

事务的执行结果必须使数据库从一个一致性状态到另一个一致性状态。一致性状态是指:1.系统的状态满足数据的完整性约束(主码,参照完整性,check约束等) 2.系统的状态反应数据库本应描述的现实世界的真实状态,比如转账前后两个账户的金额总和应该保持不变。

> 隔离性(Isolation)

并发执行的事务不会相互影响,其对数据库的影响和它们串行执行时一样。比如多个用户同时往一个账户转账,最后账户的结果应该和他们按先后次序转账的结果一样。

> 持久性(Durability)

事务一旦提交,其对数据库的更新就是持久的。任何事务或系统故障都不会导致数据丢失。

在事务的ACID特性中,C即一致性是事务的根本追求,而对数据一致性的破坏主要来自两个方面
1.事务的并发执行
2.事务故障或系统故障


## 2. 事务实现原理

<img src="https://oscimg.oschina.net/oscnet/up-7b54f7847cee22930ec53a4058179a2b531.png" width=460 height=300>

- 并发控制技术保证了事务的隔离性,使数据库的一致性状态不会因为并发执行的操作被破坏。
- 日志恢复技术保证了事务的原子性,使一致性状态不会因事务或系统故障被破坏。同时使已提交的对数据库的修改不会因系统崩溃而丢失,保证了事务的持久性。


### 2.1 回滚日志（undo）
undo log属于 「 逻辑日志 」，它记录的是sql执行相关的信息。当发生回滚时，InnoDB会根据undo log的内容做与之前相反的工作：对于每个insert，回滚时会执行delete；对于每个delete，回滚时会执行insert；对于每个update，回滚时会执行一个相反的update，把数据改回去。

undo log用于存放数据被修改前的值，如果修改出现异常，可以使用undo日志来实现回滚操作，保证事务的一致性。另外InnoDB MVCC事务特性也是基于undo日志实现的。

因此，undo log有两个作用：提供回滚和多个行版本控制(MVCC)。

### 2.2 重做日志（redo）
redo log重做日志记录的是新数据的备份，属于物理日志。在事务提交前，只要将redo log持久化即可，不需要将数据持久化。当系统崩溃时，虽然数据没有持久化，但是redo log已经持久化。系统可以根据redo log的内容，将所有数据恢复到最新的状态。

redo log包括两部分：一是内存中的日志缓冲(redo log buffer)，该部分日志是易失性的；二是磁盘上的重做日志文件(redo log file)，该部分日志是持久的。

MySQL中redo log刷新规则采用一种称为Checkpoint的机制（利用LSN实现），为了确保安全性，又引入double write机制。



## 事务基本操作
开启事务：start transaction
回滚事务：rollback
提交事务：commit

## 数据库隔离级别

SQL标准定义了4类隔离级别，包括了一些具体规则，用来限定事务内外的哪些改变是可见的，哪些是不可见的。低级别的隔离级一般支持更高的并发处理，并拥有更低的系统开销。另外，这篇分布式事务不理解？一次给你讲清楚！推荐大家阅读。

### Read Uncommitted（读取未提交内容）

在该隔离级别，所有事务都可以看到其他未提交事务的执行结果。本隔离级别很少用于实际应用，因为它的性能也不比其他级别好多少。读取未提交的数据，也被称之为脏读（Dirty Read）。

### Read Committed（读取提交内容）

这是大多数数据库系统的默认隔离级别（但不是MySQL默认的）。它满足了隔离的简单定义：一个事务只能看见已经提交事务所做的改变。这种隔离级别 也支持所谓的不可重复读（Nonrepeatable Read），因为同一事务的其他实例在该实例处理其间可能会有新的commit，所以同一select可能返回不同结果。

### Repeatable Read（可重读）

这是MySQL的默认事务隔离级别，它确保同一事务的多个实例在并发读取数据时，会看到同样的数据行。不过理论上，这会导致另一个棘手的问题：幻读 （Phantom Read）。简单的说，幻读指当用户读取某一范围的数据行时，另一个事务又在该范围内插入了新行，当用户再读取该范围的数据行时，会发现有新的“幻影” 行。InnoDB和Falcon存储引擎通过多版本并发控制（MVCC，Multiversion Concurrency Control）机制解决了该问题。

### Serializable（可串行化）

这是最高的隔离级别，它通过强制事务排序，使之不可能相互冲突，从而解决幻读问题。简言之，它是在每个读的数据行上加上共享锁。在这个级别，可能导致大量的超时现象和锁竞争。

### 事务隔离级别产生的问题

<img src="https://oscimg.oschina.net/oscnet/up-ee3cb778a32220ff81103f9163d22f774b2.png"> 

#### 脏读(Drity Read)

某个事务已更新一份数据，另一个事务在此时读取了同一份数据，由于某些原因，前一个RollBack了操作，则后一个事务所读取的数据就会是不正确的。

```
事务A第一次读取到price=100
同时事务B更新update price=120，但是此时的事务B还未commit
事务A读取的price=120
事务B->rollback操作
事务A读取到的是脏数据
```

#### 不可重复读(Non-repeatable read)

在一个事务的两次查询之中数据不一致，这可能是两次查询过程中间插入了一个事务更新的原有的数据。

```
事务A第一次读取到price=100
同时事务B更新update price=120，并commit
事务A读取的price=120
事务A多次读取的结果不一致
```

#### 幻读(Phantom Read) 

<font color=red >幻读和不可重复读的区别在于，幻读主要表现在数据的删除和插入，而不可重复读表现在数据的更新。</font>

```language
事务A第一次读取到price=100
同时事务B更新delete price=100 这条记录，并commit
事务A读取的price=100
price这条记录已经不存在，但是事务A还是可以读取到
```

1、在可重复读隔离级别下，普通查询是快照读，是不会看到别的事务插入的数据的，幻读只在当前读下才会出现。 
2、幻读专指新插入的行，读到原本存在行的更新结果不算。因为当前读的作用就是能读到所有已经提交记录的最新值。 

## 参考资料
[详细分析MySQL事务日志(redo log和undo log)](https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html)
[数据库事务的概念及其实现原理](https://www.cnblogs.com/takumicx/p/9998844.html)
[数据库事务实现原理](https://zhuanlan.zhihu.com/p/281927963)
[mysql数据库的隔离级别](https://blog.csdn.net/xiewenfeng520/article/details/99407038)
[MYSQL数据库的四种隔离级别](https://blog.csdn.net/sinat_15805929/article/details/91127491)
[MySQL幻读](https://www.jianshu.com/p/c53c8ab650b5)
[MySQL 事务&&锁机制&&MVCC](https://mp.weixin.qq.com/s?__biz=MzU4NzA3MTc5Mg==&mid=2247484480&idx=1&sn=3571b89575e8c37c114c9f290b953a1c&chksm=fdf0ec1fca87650913e6673a453d0ba1614341433aa67dd9977fef7231a3d825f7da4e4a132a&token=1651214636&lang=zh_CN&scene=21#wechat_redirect)
[Innodb中的事务隔离级别和锁的关系 - 美团技术团队](https://tech.meituan.com/2014/08/20/innodb-lock.html)

