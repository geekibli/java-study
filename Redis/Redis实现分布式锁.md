---
title: Redis实现分布式锁
toc: true
date: 2021-09-15 11:21:41
tags: 
- Redis
- 分布式
categories:
---

# Redis实现分布式锁的种种细节



**1、redis分布式锁直接使用 `setNx` 获取锁🔒，`del key` 释放锁**

会造成 「 <font color=blue>**死锁**</font> 」的问题，获取锁的线程没有释放锁，进程死掉了，其他进程永远无法获取锁



**2、给锁对应的key添加过期时间不就可以解决死锁的问题了吗？**

`127.0.0.1:6379> SETNX lock 1    // 加锁(integer) `

`127.0.0.1:6379> EXPIRE lock 10  // 10s后自动过期(integer) `

<font color=blue> **上面两个命令有什么问题吗**？</font>

不是原子操作，可能 `expire`没有执行！使用如下复合命令 👇

`127.0.0.1:6379> SET lock 1 EX 10 NX`



**3、这样还会存在一个问题，进程2释放的是进程1的锁**

进程1操作时间太久，还没有主动释放锁，锁就过期了，然后进程2获取锁，然后执行，进程2还没有执行完成，进程1执行完了，释放

锁，但是释放的是进程2的锁。「 <font color=blue>**释放他人锁**</font>」和 「 <font color=blue>**锁过期时间问题**</font>」

1. 加锁：`SET lock_key $unique_id EX $expire_time NX`
2. 操作共享资源
3. 释放锁：Lua 脚本，先 GET 判断锁是否归属自己，再 DEL 释放锁

```lua
// 判断锁是自己的，才释放
if redis.call("GET",KEYS[1]) == ARGV[1]
then
    return redis.call("DEL",KEYS[1])
else
    return 0
end
```

**4、锁过期时间不好评估怎么办？**

<font color=blue>**假设一个方案：**</font>

**加锁时，先设置一个过期时间，然后我们开启一个「守护线程」，定时去检测这个锁的失效时间，如果锁快要过期了，操作共享资源还未完成，那么就自动对锁进行「续期」，重新设置过期时间。**

如果你是 Java 技术栈，幸运的是，已经有一个库把这些工作都封装好了：<font color=blue>**Redisson**</font>。

Redisson 是一个 Java 语言实现的 Redis SDK 客户端，在使用分布式锁时，它就采用了「自动续期」的方案来避免锁过期，这个守护线程我们一般也把它叫做「看门狗」线程。

**<font color=red>以上都是基于单机redis的角度思考的redis分布式锁的问题，主要有三点 👇</font>**

1、死锁问题 （加过期时间解决）

2、释放他人锁 （添加线程标志）

3、锁过期时间问题 （守护线程自动续期）

**<font color=red>如果是redis集群模式下会有哪些问题呢 👇</font>**

在redis主从模式下，如果master节点突然宕机了，锁还没有同步到从节点是，是不是分布式锁就丢了？？？

Redis 作者提出的 Redlock 方案，是如何解决主从切换后，锁失效问题的。如何解决这个问题呢 ？ 「 **<font color=blue>RedLock</font>**」

**Redlock 的方案基于 2 个前提：**

1. 不再需要部署**从库**和**哨兵**实例，只部署**主库**
2. 但主库要部署多个，官方推荐至少 5 个实例

也就是说，想用使用 Redlock，你至少要部署 5 个 Redis 实例，而且都是主库，它们之间没有任何关系，都是一个个孤立的实例。

> **注意：不是部署 Redis Cluster，就是部署 5 个简单的 Redis 实例。**




## 参考资料
> - [深度剖析：Redis 分布式锁到底安全吗？看完这篇文章彻底懂了！](https://mp.weixin.qq.com/s/ybiN5Q89wI0CnLURGUz4vw)

