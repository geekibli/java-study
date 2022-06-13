---
title: Mysql-MVCC多版本并发控制
toc: true
date: 2021-07-28 18:12:28
tags:  MySQL
categories: [DataBase,MySQL]
---

# MVCC多版本并发控制


MVCC，全称Multi-Version Concurrency Control，即多版本并发控制。MVCC是一种并发控制的方法，一般在数据库管理系统中，实现对数据库的并发访问，在编程语言中实现事务内存。

MVCC在MySQL InnoDB中的实现主要是为了 「<font color=red> 提高数据库并发性能，用更好的方式去处理读-写冲突，做到即使有读写冲突时，也能做到不加锁，非阻塞并发读 </font>」

## 当前读和快照读


> 当前读

像select lock in share mode(共享锁), select for update ; update, insert ,delete(排他锁)这些操作都是一种当前读，为什么叫当前读？就是它读取的是记录的最新版本，读取时还要保证其他并发事务不能修改当前记录，会对读取的记录进行加锁。

> 快照读

像不加锁的select操作就是快照读，即不加锁的非阻塞读；快照读的前提是隔离级别不是串行级别，串行级别下的快照读会退化成当前读；之所以出现快照读的情况，是基于提高并发性能的考虑，快照读的实现是基于多版本并发控制，即MVCC,可以认为MVCC是行锁的一个变种，但它在很多情况下，避免了加锁操作，降低了开销；既然是基于多版本，即快照读可能读到的并不一定是数据的最新版本，而有可能是之前的历史版本

说白了MVCC就是为了实现读-写冲突不加锁，而这个读指的就是快照读, 而非当前读，当前读实际上是一种加锁的操作，是悲观锁的实现

<font color=red >MVCC模型在MySQL中的具体实现则是由 3个隐式字段，undo日志 ，Read View 等去完成的，具体可以看下面的MVCC实现原理</font>

## MVCC有什么好处，解决了什么问题
多版本并发控制（MVCC）是一种用来「 <font color=blue>解决读-写冲突的无锁并发控制</font> 」，也就是为事务分配单向增长的时间戳，为每个修改保存一个版本，版本与事务时间戳关联，读操作只读该事务开始前的数据库的快照。 所以MVCC可以为数据库解决以下问题

- 在并发读写数据库时，可以做到在读操作时不用阻塞写操作，写操作也不用阻塞读操作，提高了数据库并发读写的性能
- 同时还可以解决脏读，幻读，不可重复读等事务隔离问题，但不能解决更新丢失问题


## MVCC的实现原理

MVCC的目的就是多版本并发控制，在数据库中的实现，就是为了解决读写冲突，它的实现原理主要是依赖记录中的  <font color=red>3个隐式字段</font>，<font color=red>undo日志</font> ，<font color=red>Read View</font> 来实现的。所以我们先来看看这个三个point的概念

### 隐式字段

每行记录除了我们自定义的字段外，还有数据库隐式定义的DB_TRX_ID,DB_ROLL_PTR,DB_ROW_ID等字段

> DB_TRX_ID

6byte，最近修改(修改/插入)事务ID：记录创建这条记录/最后一次修改该记录的事务ID

> DB_ROLL_PTR

7byte，回滚指针，指向这条记录的上一个版本（存储于rollback segment里）

> DB_ROW_ID

6byte，隐含的自增ID（隐藏主键），如果数据表没有主键，InnoDB会自动以DB_ROW_ID产生一个聚簇索引
实际还有一个删除flag隐藏字段, 既记录被更新或删除并不代表真的删除，而是删除flag变了


### undo日志
undo log主要分为两种：

> insert undo log

代表事务在insert新记录时产生的undo log, 只在事务回滚时需要，并且在事务提交后可以被立即丢弃

> update undo log

事务在进行update或delete时产生的undo log; 不仅在事务回滚时需要，在快照读时也需要；所以不能随便删除，只有在快速读或事务回滚不涉及该日志时，对应的日志才会被purge线程统一清除

### Read View(读视图)


什么是Read View，说白了Read View就是<font color=blue>事务进行快照读操作的时候</font>生产的读视图(Read View)，在该事务执行的快照读的那一刻，会生成数据库系统当前的一个快照，记录并维护系统当前活跃事务的ID(当每个事务开启时，都会被分配一个ID, 这个ID是递增的，所以最新的事务，ID值越大)

所以我们知道 Read View主要是用来做可见性判断的, 即当我们某个事务执行快照读的时候，对该记录创建一个Read View读视图，把它比作条件用来判断当前事务能够看到哪个版本的数据，既可能是当前最新的数据，也有可能是该行记录的undo log里面的某个版本的数据。

Read View遵循一个可见性算法，<font color=red>主要是将要被修改的数据的最新记录中的DB_TRX_ID（即当前事务ID）取出来，与系统当前其他活跃事务的ID去对比（由Read View维护）</font>，如果DB_TRX_ID跟Read View的属性做了某些比较，不符合可见性，那就通过DB_ROLL_PTR回滚指针去取出Undo Log中的DB_TRX_ID再比较，即遍历链表的DB_TRX_ID（从链首到链尾，即从最近的一次修改查起），直到找到满足特定条件的DB_TRX_ID, 那么这个DB_TRX_ID所在的旧记录就是当前事务能看见的最新老版本;


### 总结
所谓的MVCC（Multi-Version Concurrency Control ，多版本并发控制）指的就是在使用<font color=blue>读已提交（READ COMMITTD）</font>、<font color=blue>可重复读（REPEATABLE READ）</font>这两种隔离级别的事务在执行普通的SELECT操作时访问记录的版本链的过程，这样子可以使不同事务的读-写、写-读操作并发执行，从而提升系统性能。

这两个隔离级别的一个很大不同就是：**生成ReadView的时机不同，READ COMMITTD在每一次进行普通SELECT操作前都会生成一个ReadView，而REPEATABLE READ只在第一次进行普通SELECT操作前生成一个ReadView，数据的可重复读其实就是ReadView的重复使用**。

这样去解释这些技术，主要是希望大家对现象背后的本质多点思考，不然你去背出这几种隔离级别，以及各种数据现象是没有任何意义的，实际开发过程中真的出现了问题，你不懂本质以及过程，你去排查也会很难受的，到头来还是要看书，看资料。

## 参考资料
[1、MVCC多版本并发控制](https://www.jianshu.com/p/8845ddca3b23)
[2、MVCC浅析](https://blog.csdn.net/chosen0ne/article/details/18093187)
[3、乐观锁、悲观锁和MVCC，今天让你一次搞懂](https://database.51cto.com/art/202010/629317.htm)
[4、面试官：谈谈你对Mysql的MVCC的理解？](https://baijiahao.baidu.com/s?id=1629409989970483292&wfr=spider&for=pc)
[5、Mysql中MVCC的使用及原理详解](https://www.cnblogs.com/shujiying/p/11347632.html)
[6、[Innodb中的事务隔离级别和锁的关系]](https://tech.meituan.com/2014/08/20/innodb-lock.html)