---
title: MySQL-索引
toc: true
date: 2021-08-25 14:48:18
tags: MySQL
categories:
---

# MySQL中支持的索引

之前问过存储引擎是数据库层面的还是数据表层面的？

那么现在也同样问一个问题，索引是存储引擎层面的还是服务器层面实现的？答案是存储引擎层面的。 

## 什么是索引
官方介绍索引是帮助MySQL高效获取数据的数据结构。更通俗的说，数据库索引好比是一本书前面的目录，能加快数据库的查询速度。

一般来说索引本身也很大，不可能全部存储在内存中，因此索引往往是存储在磁盘上的文件中的（可能存储在单独的索引文件中，也可能和数据一起存储在数据文件中）。

我们通常所说的索引，包括聚集索引、覆盖索引、组合索引、前缀索引、唯一索引等，没有特别说明，默认都是使用B+树结构组织（多路搜索树，并不一定是二叉的）的索引。

## 索引的优缺点
### 优势：
1、可以提高数据检索的效率，降低数据库的IO成本，类似于书的目录。

2、通过索引列对数据进行排序，降低数据排序的成本，降低了CPU的消耗。

3、被索引的列会自动进行排序，包括【单列索引】和【组合索引】，只是组合索引的排序要复杂一些。如果按照索引列的顺序进行排序，对应order by语句来说，效率就会提高很多。

### 劣势：

1、索引会占据磁盘空间

2、索引虽然会提高查询效率，但是会降低更新表的效率。比如每次对表进行增删改操作，MySQL不仅要保存数据，还有保存或者更新对应的索引文件。

<font color=blue>一个表最多可以创建多少个索引？</font> 16个

## 索引的类型
### 主键索引
索引列中的值必须是唯一的，不允许有空值。InnoDB下强烈推荐使用数值类型的自增主键。这个和b+数的排序有关。

### 普通索引
MySQL中基本索引类型，没有什么限制，允许在定义索引的列中插入重复值和空值。

### 唯一索引
索引列中的值必须是唯一的，但是允许为空值。

### 全文索引
只能在文本类型CHAR,VARCHAR,TEXT类型字段上创建全文索引。字段长度比较大时，如果创建普通索引，在进行like模糊查询时效率比较低，这时可以创建全文索引。MyISAM和InnoDB中都可以使用全文索引。

### 空间索引
MySQL在5.7之后的版本支持了空间索引，而且支持OpenGIS几何数据模型。MySQL在空间索引这方面遵循OpenGIS几何数据模型规则。

### 前缀索引
在文本类型如CHAR,VARCHAR,TEXT类列上创建索引时，可以指定索引列的长度，但是数值类型不能指定。

### 组合索引
组合索引的使用，需要遵循最左前缀匹配原则（最左匹配原则）。一般情况下在条件允许的情况下使用组合索引替代多个单列索引使用。

### 聚簇索引（聚集索引）

在MyISAM存储引擎中，主键索引上存储的是数据在磁盘上的地址。

在InnoDB存储引擎中，主键索引上存储的就是整行数据。

### 辅助索引

在MyISAM存储引擎中，辅助索引其实和主键索引一样，也是存储的数据的磁盘地址。区别在主键索引的键值必须唯一，而辅助索引的键值可以重复。

在InnoDB存储引擎中，辅助索引存储的是数据的主键。查到主键之后，在根据主键值查找主键索引上存储的数据。

这会发生一种现象叫**回表**，会增加性能消耗，解决这种问题的方法就是 **覆盖索引**，也就是把要查询的字段也添加到索引中。

### 跳跃索引

一般情况下，如果表users有复合索引idx_status_create_time，我们都知道，单独用create_time去查询，MySQL优化器是不走索引，所以还需要再创建一个单列索引idx_create_time。用过Oracle的同学都知道，是可以走索引跳跃扫描（Index Skip Scan），在MySQL 8.0也实现Oracle类似的索引跳跃扫描，在优化器选项也可以看到skip_scan=on。

```sql
| optimizer_switch             |use_invisible_indexes=off,skip_scan=on,hash_join=on |
```

**适合复合索引前导列唯一值少，后导列唯一值多的情况，如果前导列唯一值变多了，则MySQL CBO不会选择索引跳跃扫描，取决于索引列的数据分表情况。**

```sql
mysql> explain select id, user_id，status, phone from users where create_time >='2021-01-02 23:01:00' and create_time <= '2021-01-03 23:01:00';
+----+-------------+-------+------------+------+---------------+------+---------+------+--------+----------+----
| id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows   | filtered | Extra       |
+----+-------------+-------+------------+------+---------------+------+---------+------+--------+----------+----
|  1 | SIMPLE      | users | NULL       | range  | idx_status_create_time          | idx_status_create_time | NULL    | NULL | 15636 |    11.11 | Using where; Using index for skip scan|
```

也可以通过 `optimizer_switch='skip_scan=off'`来关闭索引跳跃扫描特性。

## B Tree

B树有什么特点呢？

1、所有的键值分布在整棵树上面

2、搜索有可能在非叶子节点结束，在关键字全集内做一次查找，性能接近于二分查找

3、每个节点最多拥有m个子树，根节点至少有2个子树，分支节点至少拥有m / 2个子树（除根节点和叶子节点都是分支节点）

4、所有叶子节点都在同一层，每个节点最多可以有m - 1个key，并且按照升序排序

![image-20210825145448549](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210825145448549.png)



B Tree 指的是 Balance Tree，也就是平衡树，平衡树是一颗查找树，并且所有叶子节点位于同一层。

### 图例说明

1、每个节点占据一个磁盘块，一个节点上有两个升序排序的关键字K和三个指向子树节点的指针P，指针P存储子节点所在的磁盘块的地址。

2、两个关键字K划分三个范围域对应三个指针指向子树的数据范围

### 查找关键字的过程

1、根据根节点查找磁盘块1，读入到内存（第一次磁盘IO）

2、比较关键字K22在（K25，K50）区间，找到磁盘块1的P2指针

3、根据磁盘块1的P2指针找到磁盘块3，读到内存（第二次磁盘IO）

4、继续比较K32,锁定在K30,K38之间，找到磁盘3的P2指针

5、根据磁盘块2的P2指针锁定磁盘块8，读到内存（第三次磁盘IO）

6、在磁盘块8的关键字列表中找到关键字K32

### B树的缺点

1、每个节点都存储key和数据，而数据一般又会占用相对较多的空间，这样导致每个节点锁存储的关键字数量较少，最终导致树的高度比较高

2、当数据量较大的时候，由于树的高度比较深，造成查询的性能极差

## B+Tree

**思考一下问题🤔**

为什么MySQL使用B+Tree这种数据结构来作为索引结构呢？

为什么不使用二叉树呢？为什么不使用B Tree呢？

### B+树的特点

**B+ Tree 是 B 树的一种变形，它是基于 B Tree 和叶子节点顺序访问指针进行实现，通常用于数据库和操作系统的文件系统中。**

1、B+ 树有两种类型的节点：内部节点（也称索引节点）和叶子节点，内部节点就是非叶子节点，内部节点不存储数据，只存储索引，相对于B树，每个节点可以存储更多的节点。这样可以大大降低树的高度，同时把数据范围数据都存在叶子节点。

2、内部节点中的 key 都按照从小到大的顺序排列，对于内部节点中的一个 key，左子树中的所有 key 都小于它，右子树中的 key 都大于等于它，叶子节点的记录也是按照从小到大排列的。

3、叶子节点两两指针互相链接（符合磁盘的预读特性），顺序查询性能更高。

![image-20210825150633224](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210825150633224.png)

### B+树的优点

- 因为不再需要进行全表扫描，只需要对树进行搜索即可，所以查找速度快很多。
- 因为 B+ Tree 的有序性，所以除了用于查找，还可以用于排序和分组。
- 可以指定多个列作为索引列，多个索引列共同组成键。
- 适用于全键值、键值范围和键前缀查找，其中键前缀查找只适用于最左前缀查找。如果不是按照索引列的顺序进行查找，则无法使用索引。



## 哈希索引

哈希索引能以 O(1) 时间进行查找，但是失去了有序性：

- 无法用于排序与分组；
- 只支持精确查找，无法用于部分查找和范围查找。

InnoDB 存储引擎有一个特殊的功能叫“自适应哈希索引”，当某个索引值被使用的非常频繁时，会在 B+Tree 索引之上再创建一个哈希索引，这样就让 B+Tree 索引具有哈希索引的一些优点，比如快速的哈希查找。

## 全文索引

MyISAM 存储引擎支持全文索引，用于查找文本中的关键词，而不是直接比较是否相等。

查找条件使用 MATCH AGAINST，而不是普通的 WHERE。

全文索引使用倒排索引实现，它记录着关键词到其所在文档的映射。

InnoDB 存储引擎在 MySQL 5.6.4 版本中也开始支持全文索引。



## 索引的优化

1、索引不支持表达式

2、在需要使用多个字段作为查询条件的时候，可以使用多列索引，注意最左匹配原则

3、索引列的顺序要注意，区分度大的列写在索引的前面，效率会比较高

4、前缀索引，对于Blog，Text之类的大字段，必须使用前缀索引，只索引开始的部分字符，前缀长度的选取需要根据索引选择性来确定。

5、覆盖索引，查询的字段尽量保存在索引，避免回表。



## 索引的优点

- 大大减少了服务器需要扫描的数据行数。
- 帮助服务器避免进行排序和分组，以及避免创建临时表（B+Tree 索引是有序的，可以用于 ORDER BY 和 GROUP BY 操作。临时表主要是在排序和分组过程中创建，不需要排序和分组，也就不需要创建临时表）。
- 将随机 I/O 变为顺序 I/O（B+Tree 索引是有序的，会将相邻的数据都存储在一起）。



## 索引的使用条件

- 对于非常小的表、大部分情况下简单的全表扫描比建立索引更高效；
- 对于中到大型的表，索引就非常有效；
- 但是对于特大型的表，建立和维护索引的代价将会随之增长。这种情况下，需要用到一种技术可以直接区分出需要查询的一组数据，而不是一条记录一条记录地匹配，例如可以使用分区技术。

**为什么对于非常小的表，大部分情况下简单的全表扫描比建立索引更高效？**

如果一个表比较小，那么显然直接遍历表比走索引要快（因为需要回表）。

⚠️注：首先，要注意这个答案隐含的条件是查询的数据不是索引的构成部分，否也不需要回表操作。其次，查询条件也不是主键，否则可以直接从聚簇索引中拿到数据。







https://mp.weixin.qq.com/s/faOaXRQM8p0kwseSHaMCbg

