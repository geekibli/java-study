---
title: Redis-list底层实现
toc: true
date: 2021-07-28 16:45:33
tags: Redis
categories: [DataBase, Redis]
---

# Redis List 底层实现

![](https://oscimg.oschina.net/oscnet/up-c8da12582edb1a4320e17096efc9b9103a2.png)   

## 关键字
> 连锁更新问题 | quicklist | ziplist | linkedlist 

## List底层数据结构 
在 3.0 版本的 Redis 中，List 类型有两种实现方式：
数据结构底层采用压缩列表ziplist或linkedlist两种数据结构进行存储，首先以ziplist进行存储，在不满足ziplist的存储要求后转换为linkedlist列表。
当列表对象同时满足以下两个条件时，列表对象使用ziplist进行存储，否则用linkedlist存储。

### ziplist转换成linkedlist的条件
**1、触发一下任意一条即进行转换：**
- 列表对象保存的所有字符串元素的长度小于64字节  
- 列表对象保存的元素数量小于512个。

**2、redis.conf配置文件**
```text
list-max-ziplist-value 64 
list-max-ziplist-entries 512 
```
**3、ziplist和linkedlist底层实现**  
[1、使用压缩列表（ziplist）实现的列表对象。]()  
[2、使用双端链表（linkedlist）实现的列表对象。]()  

在 3.2 版本后新增了 quicklist 数据结构实现了 list，现在就来分析下 quicklist 的结构。    

## quicklist
![](https://oscimg.oschina.net/oscnet/up-558f859ac71d40fd122bae2ebdb0a9eb055.png)  
ziplist会引入频繁的内存申请和释放，而linkedlist由于指针也会造成内存的浪费，而且每个节点是单独存在的，会造成很多内存碎片，
所以结合两个结构的特点，设计了quickList。
quickList 是一个 ziplist 组成的双向链表。每个节点使用 ziplist 来保存数据。本质上来说， quicklist 里面保存着一个一个小的 ziplist。

### quicklist表头结构
```c++
typedef struct quicklist {
    //指向头部(最左边)quicklist节点的指针
    quicklistNode *head;
    
    //指向尾部(最右边)quicklist节点的指针
    quicklistNode *tail;

    //ziplist中的entry节点计数器
    unsigned long count;        /* total count of all entries in all ziplists */

    //quicklist的quicklistNode节点计数器
    unsigned int len;           /* number of quicklistNodes */

    //保存ziplist的大小，配置文件设定，占16bits
    int fill : 16;              /* fill factor for individual nodes */

    //保存压缩程度值，配置文件设定，占16bits，0表示不压缩
    unsigned int compress : 16; /* depth of end nodes not to compress;0=off */
} quicklist;
```

- head 和 tail 分别指向这个双端链表的表头和表尾, quicklist 存储的节点是一个叫做 quicklistNode 的结构, 如果这个 quicklist 是空的, 
那么 head 和 tail 会同时成为空指针, 如果这个双端链表的大小为 1, 那么 head 和 tail 会同时指向一个相同的节点
- count 是一个计数器, 表示当前这个 list 结构一共存储了多少个元素, 它的类型是 unsigned long, 所以一个 list 能存储的最多的元素在 字长为 64 bit 的机器上是 (1 << 64) - 1, 字长为 32 bit 的机器上是 (1 << 32) - 1
- len 表示了这个双端链表的长度(quicklistNodes 的数量)
- fill 表示了单个节点(quicklistNode)的负载比例(fill factor), 这是什么意思呢 
> Lists 结构使用了一种特殊的编码方式来节省空间, Lists 中每一个节点所能存储的东西可以通过最大长度或者一个最大存储的空间大小来限制, 
> 对于想限制每个节点最大存储空间的用户, 用 -5 到 -1 来表示这个限制值  
-5: 最大存储空间: 64 Kb <-- 通常情况下不要设置这个值    
-4: 最大存储空间: 32 Kb <-- 非常不推荐
-3: 最大存储空间: 16 Kb <-- 不推荐
-2: 最大存储空间: 8 Kb <-- 推荐
-1: 最大存储空间: 4 Kb <-- 推荐    

对于正整数则表示最多能存储到你设置的那个值, 当前的节点就装满了
通常在 -2 (8 Kb size) 或 -1 (4 Kb size) 时, 性能表现最好
但是如果你的使用场景非常独特的话, 调整到适合你的场景的值！！！！

**redis.conf, 其中有一个可配置的参数叫做 list-max-ziplist-size, 默认值为 -2, 它控制了 quicklist 中的 fill 字段的值, 负数限制 quicklistNode 中的 ziplist 的字节长度, 正数限制 quicklistNode 中的 ziplist 的最大长度**  

- compress 则表示 quicklist 中的节点 quicklistNode, 除开最两端的 compress 个节点之后, 中间的节点都会被压缩
>Lists 在某些情况下是会被压缩的, 压缩深度是表示除开 list 两侧的这么多个节点不会被压缩, 剩下的节点都会被尝试进行压缩, 头尾两个节点一定不会被进行压缩, 
> 因为要保证 push/pop 操作的性能, 有以下的值可以设置:  
0: 关闭压缩功能

 1: 深度 1 表示至少在 1 个节点以后才会开始尝试压缩, 方向为从头到尾或者从尾到头
```text
[head]->node->node->…->node->[tail]
[head], [tail] 永远都是不会被压缩的状态; 中间的节点则会被压缩
```
2 不会尝试压缩 head 或者 head->next 或者 tail->prev 或者 tail 但是会压缩这中间的所有节点
```text
[head]->[next]->node->node->…->node->[prev]->[tail]
```
3: 以此类推，最大为2的16次方。  


### quicklistNode 节点
```c++
typedef struct quicklistNode {
    struct quicklistNode *prev;     //前驱节点指针
    struct quicklistNode *next;     //后继节点指针

    //不设置压缩数据参数recompress时指向一个ziplist结构
    //设置压缩数据参数recompress指向quicklistLZF结构
    unsigned char *zl;

    //压缩列表ziplist的总长度
    unsigned int sz;                  /* ziplist size in bytes */

    //ziplist中包的节点数，占16 bits长度
    unsigned int count : 16;          /* count of items in ziplist */

    //表示是否采用了LZF压缩算法压缩quicklist节点，1表示压缩过，2表示没压缩，占2 bits长度
    unsigned int encoding : 2;        /* RAW==1 or LZF==2 */

    //表示一个quicklistNode节点是否采用ziplist结构保存数据，2表示压缩了，1表示没压缩，默认是2，占2bits长度
    unsigned int container : 2;       /* NONE==1 or ZIPLIST==2 */

    //标记quicklist节点的ziplist之前是否被解压缩过，占1bit长度
    //如果recompress为1，则等待被再次压缩
    unsigned int recompress : 1; /* was this node previous compressed? */

    //测试时使用
    unsigned int attempted_compress : 1; /* node can't compress; too small */

    //额外扩展位，占10bits长度
    unsigned int extra : 10; /* more bits to steal for future usage */
} quicklistNode;
```
- prev 和 next 分别指向当前 quicklistNode 的前一个和后一个节点
- zl 指向实际的 ziplist
- sz 存储了当前这个 ziplist 的占用空间的大小, 单位是字节
- count 表示当前有多少个元素存储在这个节点的 ziplist 中, 它是一个 16 bit 大小的字段, 所以一个 quicklistNode 最多也只能存储 65536 个元素
- encoding 表示当前节点中的 ziplist 的编码方式, 1(RAW) 表示默认的方式存储, 2(LZF) 表示用 LZF 算法压缩后进行的存储 
- container 表示 quicklistNode 当前使用哪种数据结构进行存储的, 目前支持的也是默认的值为 2(ZIPLIST), 未来也许会引入更多其他的结构
- recompress 是一个 1 bit 大小的布尔值, 它表示当前的 quicklistNode 是不是已经被解压出来作临时使用
- attempted_compress 只在测试的时候使用
- extra 是剩下多出来的 bit, 可以留作未来使用


### quicklistLZF 结构定义
```text
Copytypedef struct quicklistLZF {
    unsigned int sz;  //压缩后的ziplist大小
    char compressed[];//柔性数组，存放压缩后的ziplist字节数组
} quicklistLZF;
```
当指定使用lzf压缩算法压缩ziplist的entry节点时，quicklistNode结构的zl成员指向quicklistLZF结构;   




## 参考资料
1、[Redis列表list 底层原理](https://zhuanlan.zhihu.com/p/102422311)  
2、[Redis中string、list的底层数据结构原理](https://cloud.tencent.com/developer/article/1710612)  
3、[《闲扯Redis五》List数据类型底层之quicklist](https://cloud.tencent.com/developer/article/1619920)  
4、[Redis源码剖析和注释（七）--- 快速列表(quicklist)](https://blog.csdn.net/men_wen/article/details/70229375)  
5、[redis 列表结构 底层实现(quicklist)](https://blog.csdn.net/qq_31720329/article/details/99938219)  