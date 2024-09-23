
# 题目

https://leetcode.cn/problems/find-first-and-last-position-of-element-in-sorted-array/


# 解法

```java
class Solution {

    // 写两遍二分查询，第一个找开始坐标，第二个找结束坐标

    public int[] searchRange(int[] nums, int target) {
        if (nums == null || nums.length == 0) {
            int[] result = new int[2];
            result[0] = -1;
            result[1] = -1;
            return result;
        }

        int left1 = 0;
        int right1 = nums.length - 1;
        int first = -1;
        // 先找开始位置
        while (left1 <= right1) {
            int middle = (right1 - left1) / 2 + left1;
            if (nums[middle] == target) {
                first = middle;
                right1 = middle -1;
            } else if (nums[middle] < target) {
                // 当时这里想的是可以提前终止，但是思路是错的，无法提前终止
                 left1 = middle + 1;
            } else if (nums[middle] > target) {
                right1 = middle - 1;
            }
        }

        int left2 = 0;
        int right2 = nums.length - 1;
        int second = -1;
        // 找最大值
        while (left2 <= right2) {
            int middle = (right2 - left2) / 2 + left2;
            if (nums[middle] == target) {
                // second = second < middle ? middle : second;
                second = middle;
                left2 = middle + 1;
            }
            else if (nums[middle] > target) {
                right2 = middle - 1;
            }
            else if (nums[middle] < target)  {
                left2 = middle + 1;
            }
        }


        int[] res = new int[2];
        res[0] = first;
        res[1] = second;
        return res;

    }
}

```