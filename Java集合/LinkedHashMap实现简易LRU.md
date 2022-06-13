---
title: LinkedHashMap实现简易LRU
toc: true
date: 2021-09-15 10:07:58
tags: LRU
categories:
---

## 题目 [#](https://hadyang.com/interview/docs/leetcode/LRUCache/#题目)

运用你所掌握的数据结构，设计和实现一个 LRU (最近最少使用) 缓存机制。它应该支持以下操作： 获取数据 get 和 写入数据 put 。

获取数据 get(key) - 如果密钥 (key) 存在于缓存中，则获取密钥的值（总是正数），否则返回 -1。 写入数据 put(key, value) - 如果密钥不存在，则写入其数据值。当缓存容量达到上限时，它应该在写入新数据之前删除最近最少使用的数据值，从而为新的数据值留出空间。

进阶:

你是否可以在 O(1) 时间复杂度内完成这两种操作？

```java
public class LruCache extends LinkedHashMap<Integer, Integer> {

    private final int capacity;

    public LruCache(int capacity) {
        super(capacity , 0.75f, true);
        this.capacity = capacity;
    }

    public int get(int key) {
        Integer integer = super.get(key);

        return integer == null ? -1 : integer;
    }

    public void put(int key, int value) {

        super.put(key, value);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > this.capacity;
    }


    public static void main(String[] args) {

        LruCache cache = new LruCache(2);
        cache.put(1,1);
        cache.put(2,2);
        System.err.println("get1 -> " + cache.get(1));
        System.err.println("get2 -> " + cache.get(2));

        System.err.println("----------------------------");


        cache.put(3,3);
        System.err.println("get1 -> " + cache.get(1));
        System.err.println("get2 -> " + cache.get(2));
        System.err.println("get3 -> " + cache.get(3));

        System.err.println("----------------------------");


        cache.put(4,4);
        System.err.println("get1 -> " + cache.get(1));
        System.err.println("get2 -> " + cache.get(2));
        System.err.println("get3 -> " + cache.get(3));
        System.err.println("get4 -> " + cache.get(4));

    }
}
```
