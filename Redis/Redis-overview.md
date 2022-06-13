---
title: Redis-overview
toc: true
date: 2021-07-28 16:49:42
tags: Redis
categories: [DataBase, Redis]
---


# Redis （Remote Dictionary Server）

## Redis简介 
Redis 本质上是一个 Key-Value 类型的内存数据库，很像 memcached，整个数据库统统加载在内存当中
进行操作，定期通过异步操作把数据库数据 flush 到硬盘上进行保存。  
因为是纯内存操作，Redis的性能非常出色，每秒可以处理超过 10 万次读写操作，是已知性能最快的 Key-Value DB。
Redis 的出色之处不仅仅是性能，Redis 最大的魅力是支持保存多种数据结构，此外单个 value 的最大限
制是 1GB，不像 memcached 只能保存 1MB 的数据，因此 Redis 可以用来实现很多有用的功能。
比方说用他的 List 来做 FIFO 双向链表，实现一个轻量级的高性 能消息队列服务，用他的 Set 可以做高
性能的 tag 系统等等。
另外 Redis 也可以对存入的 Key-Value 设置 expire 时间，因此也可以被当作一 个功能加强版的 memcached 来用。 
Redis 的主要缺点是数据库容量受到物理内存的限制，不能用作海量数据的高性能读写，因
此 Redis 适合的场景主要局限在较小数据量的高性能操作和运算上。

## Redis 如何设置密码及验证密码？
设置密码：config set requirepass 123456  
授权密码：auth 123456  

## Redis 有哪几种数据淘汰策略？
noeviction:返回错误当内存限制达到并且客户端尝试执行会让更多内存被使用的命令（大部分的写入指令，但 DEL 和几个例外）  
allkeys-lru: 尝试回收最少使用的键（LRU），使得新添加的数据有空间存放。  
volatile-lru: 尝试回收最少使用的键（LRU），但仅限于在过期集合的键,使得新添加的数据有空间存放。  
allkeys-random: 回收随机的键使得新添加的数据有空间存放。  
volatile-random: 回收随机的键使得新添加的数据有空间存放，但仅限于在过期集合的键。  
volatile-ttl: 回收在过期集合的键，并且优先回收存活时间（TTL）较短的键,使得新添加的数据有空间存放。  



## Redis 有哪些适合的场景？

### （1）会话缓存（Session Cache）
最常用的一种使用 Redis 的情景是会话缓存（session cache）。用 Redis 缓存会话比其他存储（如 Mem
cached）的优势在于：Redis 提供持久化。当维护一个不是严格要求一致性的缓存时，如果用户的购物车
信息全部丢失，大部分人都会不高兴的，现在，他们还会这样吗？ 幸运的是，随着 Redis 这些年的改进，很容易找到怎么恰当的使用 Redis 来缓存会话的文档。甚至广为
人知的商业平台 Magento 也提供 Redis 的插件。
### （2）全页缓存（FPC）
除基本的会话 token 之外，Redis 还提供很简便的 FPC 平台。回到一致性问题，即使重启了 Redis 实
例，因为有磁盘的持久化，用户也不会看到页面加载速度的下降，这是一个极大改进，类似 PHP 本地 FPC。
再次以 Magento 为例，Magento 提供一个插件来使用 Redis 作为全页缓存后端。
此外，对 WordPress 的用户来说，Pantheon 有一个非常好的插件 wp-redis，这个插件能帮助你以最快
速度加载你曾浏览过的页面。  
### （3）队列
Redis 在内存存储引擎领域的一大优点是提供 list 和 set 操作，这使得 Redis 能作为一个很好的消息队
列平台来使用。Redis 作为队列使用的操作，就类似于本地程序语言（如 Python）对 list 的 push/pop
操作。
如果你快速的在 Google 中搜索“Redis queues”，你马上就能找到大量的开源项目，这些项目的目的就是
利用 Redis 创建非常好的后端工具，以满足各种队列需求。例如，Celery 有一个后台就是使用 Redis 作
为 broker，你可以从这里去查看。
### （4）排行榜/计数器
Redis 在内存中对数字进行递增或递减的操作实现的非常好。集合（Set）和有序集合（Sorted Set）也
使得我们在执行这些操作的时候变的非常简单，Redis 只是正好提供了这两种数据结构。
所以，我们要从排序集合中获取到排名最靠前的 10 个用户–我们称之为“user_scores”，我们只需要像下
面一样执行即可：
当然，这是假定你是根据你用户的分数做递增的排序。如果你想返回用户及用户的分数，你需要这样执
行：
ZRANGE user_scores 0 10 WITHSCORESAgora Games 就是一个很好的例子，用 Ruby 实现的，它的排行榜就是使用 Redis 来存储数据的，你可
以在这里看到。
###（5）发布/订阅
最后（但肯定不是最不重要的）是 Redis 的发布/订阅功能。发布/订阅的使用场景确实非常多。我已看见
人们在社交网络连接中使用，还可作为基于发布/订阅的脚本触发器，甚至用 Redis 的发布/订阅功能来建
立聊天系统！


### Redis 常见的性能问题和解决方案
1、master 最好不要做持久化工作，如 RDB 内存快照和 AOF 日志文件  
2、如果数据比较重要，某个 slave 开启 AOF 备份，策略设置成每秒同步一次  
3、为了主从复制的速度和连接的稳定性，master 和 Slave 最好在一个局域网内  
4、尽量避免在压力大得主库上增加从库  
5、主从复制不要采用网状结构，尽量是线性结构，Master<--Slave1<----Slave2 ....  



### Redis为什么这么快

https://mp.weixin.qq.com/s/EjDeypra_d9Tfsn-WkJZdw


### 比较好的学习资源 📒📒📒
https://redisbook.readthedocs.io/en/latest/index.html
![redis设计与指南](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/20210923140319.png)  

### redis的数据结构

#### quick list
https://www.cnblogs.com/hunternet/p/12624691.html

#### ziplist
https://segmentfault.com/a/1190000017328042





















