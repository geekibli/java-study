---
title: LeetCode-只出现一次的数字
toc: true
date: 2021-08-19 21:12:54
tags:
- 数组
- 位运算
- 简单
categories:
---

# 只出现一次的数字

https://leetcode-cn.com/leetbook/read/top-interview-questions-easy/x21ib6/

给定一个非空整数数组，除了某个元素只出现一次以外，其余每个元素均出现两次。找出那个只出现了一次的元素。

说明：

你的算法应该具有线性时间复杂度。 你可以不使用额外空间来实现吗？

示例 1:

输入: [2,2,1]
输出: 1
示例 2:

输入: [4,1,2,1,2]
输出: 4
相关标签
位运算



## 分析

仅仅出现一个，很显然，这个数和前后都不一样，然后特殊判断一下头部和尾部就可以了。

## 代码

```java
class Solution {
    public int singleNumber(int[] nums) {
        if(nums == null || nums.length == 0){
            return 0;
        }
        if(nums.length == 1){
            return nums[0];
        }
        Arrays.sort(nums);
        for(int i = 2; i < nums.length; i++){
            if(nums[i - 2] != nums[i - 1] && nums[i - 1] != nums[i]){
                return nums[i - 1];
            }
            if(i == nums.length - 1 && nums[i] != nums[i - 1]){
                return nums[i];
            }
            if(i == 2 && nums[i - 2] != nums[i - 1]){
                return nums[i - 2];
            }
        }
        return 0;
    }
}
```





## 分析二

使用异或运算，将所有值进行异或
异或运算，相异为真，相同为假，所以` a^a = 0 ;0^a = a`
因为异或运算 满足交换律 `a^b^a = a^a^b = b `所以数组经过异或运算，单独的值就剩下了

https://leetcode-cn.com/leetbook/read/top-interview-questions-easy/x21ib6/?discussion=Mo9fKT

## 代码二

```java
class Solution {
    public int singleNumber(int[] nums) {
        int reduce = 0;
        for (int num : nums) {
            reduce =  reduce ^ num;
        }
        return reduce;
    }
}
```

