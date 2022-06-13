---
title: LeetCode-存在重复元素
toc: true
date: 2021-08-19 20:57:35
tags: 
- 数组
- 哈希表
- 排序
- 简单
categories:
---

# 存在重复元素

https://leetcode-cn.com/leetbook/read/top-interview-questions-easy/x248f5/

给定一个整数数组，判断是否存在重复元素。

如果存在一值在数组中出现至少两次，函数返回 true 。如果数组中每个元素都不相同，则返回 false 。

示例 1:

输入: [1,2,3,1]
输出: true
示例 2:

输入: [1,2,3,4]
输出: false
示例 3:

输入: [1,1,1,3,3,4,3,2,4,2]
输出: true

## 代码

**1、双重循环是很容易想到，但是会超出时间限制**

```java
class Solution {
    public boolean containsDuplicate(int[] nums) {
        if(nums == null || nums.length == 0){
            return false;
        }
        for(int i = 0; i < nums.length; i++){
            for(int j = i + 1; j < nums.length ; j++){
                if(nums[i] == nums[j]){
                    return true;
                }
            }
        }
        return false;
    }
}
```

**2、先排序，然后判断相邻两个元素是否相等**

```java
class Solution {
    public boolean containsDuplicate(int[] nums) {
    Arrays.sort(nums);
    for(int i = 1; i < nums.length; i++) {
        if (nums[i] == nums[i - 1]) {
            return true;
        }
    }
    return false;
    }
}
```
