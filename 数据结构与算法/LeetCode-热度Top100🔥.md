---
title: "LeetCode-热度Top100\U0001F525"
toc: true
date: 2021-08-31 14:06:04
tags:
- LeetCode
- 算法
categories:
---

# 挑战LeetCode热度Top100 👇

https://leetcode-cn.com/problem-list/2cktkvj/

### 1. 两数之和

https://leetcode-cn.com/problems/two-sum/submissions/

<img src="/Users/gaolei/Library/Application Support/typora-user-images/image-20210831143918937.png" alt="image-20210831143918937" style="zoom:50%;" />

```java
class Solution {
    public int[] twoSum(int[] nums, int target) {
      if(nums == null || nums.length == 0){
        return res;
	    }
        int [] arr = new int[2];
        for(int i = 0; i<nums.length -1 ; i++){
            int temp = target - nums[i];
            for(int j = i+1;j< nums.length; j++){
                if(temp == nums[j]){
                    arr[0] = i;
                    arr[1] = j;
                    return arr;
                }
            }
        }
        arr[0] = 0;
        arr[1] = 0;
        return arr;
    }
}
```

**还有一种方式：**

```java
public int[] twoSum(int[] nums, int target) {
    int[] res = new int[2];
    if(nums == null || nums.length == 0){
        return res;
    }
    Map<Integer, Integer> map = new HashMap<>();
    for(int i = 0; i < nums.length; i++){
        int temp = target - nums[i];
        if(map.containsKey(temp)){
            res[1] = i;
            res[0] = map.get(temp);
        }
        map.put(nums[i], i);
    }
    return res;
}
```

### [2. 两数相加](https://leetcode-cn.com/problems/add-two-numbers/)

<img src="/Users/gaolei/Library/Application Support/typora-user-images/image-20210831143837463.png" alt="image-20210831143837463" style="zoom:50%;" />

```java
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode result = new ListNode(0);
        ListNode index = result;
        if(l1 == null && l2 == null){
            return result;
        }
    
        int pre = 0;
        while(l1 != null || l2 != null || pre != 0){
            int a = l1 != null ? l1.val : 0;
            int b = l2 != null ? l2.val : 0;
            int c = a + b + pre;
            if(c > 9){
                c = c - 10;
                pre = 1;
            }else{
                pre = 0;
            }
            result.next = new ListNode(c);
            result = result.next;
            l1 = l1 == null ? l1 : l1.next;
            l2 = l2 == null ? l2 : l2.next;            
        }
        return index.next;
    }
}
```

> index指向result的头节点，result不断的往next添加值，最终返回index.next.

⚠️  注意临界条件

` while(l1 != null || l2 != null || pre != 0)`

`pre != 0` 是会出现最高位是1，但是l1和l2都是null的情况，所以最后需要判断一下pre的位置有没有值。

### [3. 无重复字符的最长子串](https://leetcode-cn.com/problems/longest-substring-without-repeating-characters/)



### [4. 寻找两个正序数组的中位数](https://leetcode-cn.com/problems/median-of-two-sorted-arrays/)

<img src="/Users/gaolei/Library/Application Support/typora-user-images/image-20210831164326354.png" alt="image-20210831164326354" style="zoom:50%;" />

```java
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
    
        
        int [] arr = combineArr(nums1,nums2);
        return getResult(arr);
    }

    double getResult(int [] nums){
        int c = 0;
        int d = 0;
        if(nums.length % 2 == 0){
            c = nums.length / 2;
            d = c - 1;
        }else{
            c = (nums.length - 1) / 2;
            d = c;
        }

        return (double)(nums[c] + nums[d] ) / 2;
    }


    int [] combineArr(int [] nums1, int [] nums2){
       int [] target = new int[nums1.length + nums2.length];
        int i = 0,j = 0;
        int offset = 0;
        while(i < nums1.length && j < nums2.length){
            if(nums1[i] <= nums2[j]){
                target[offset] = nums1[i];
                i++;
            }else{
                target[offset] = nums2[j];
                j++;
            }
            offset++;

        }

        if(i != nums1.length){
            for(int x = i; x < nums1.length;x++){
                target[offset] = nums1[x];
                offset++;
            }
        }else{
            for(int x = j; x < nums2.length;x++){
                target[offset] = nums2[x];
                offset++;
            }
        }
        return target;
    }

}
```

上面的思路比较清晰，合并两个有序数组，然后取中位数。

### [5. 最长回文子串](https://leetcode-cn.com/problems/longest-palindromic-substring/)

<img src="/Users/gaolei/Library/Application Support/typora-user-images/image-20210831233346789.png" alt="image-20210831233346789" style="zoom:50%;" />

```java
public static String longestPalindrome(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }

        int left = 0;
        int right = 0;
        int maxLength = 0;
        int i = 0;

        while (i < s.length()) {
            int step = 1;
            while ((i - step) >= 0 && (i + step) <= (s.length() - 1)) {
                if (s.charAt(i - step) == s.charAt(i + step)) {
                    if (step >= maxLength && (i - step) != (i + step)) {
                        left = Math.max((i - step), 0);
                        right = (i + step);
                    }
                    step++;
                    maxLength = Math.max(maxLength, step);
                    continue;
                }
                if (s.charAt(i) == s.charAt(i + step)) {
                    if (step >= maxLength  && step != 0) {
                        left = Math.max((i), 0);
                        right = (i + step);
                    }
                    step++;
                    maxLength = Math.max(maxLength, step);
                    continue;
                }
                step++;
            }
            i++;
        }

        return s.substring(left, right + 1);
    }
```

**❌ 上面这种写法在 `bb` `bbbb` `bbbbbb`这些用例下不兼容 有没有人帮忙调整一下！**

```java
public String longestPalindrome(String s) {
        int[] arr = new int[2];
        char[] chars = s.toCharArray();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            int high = i, low = i;
            while (high < n - 1 && chars[i] == chars[high + 1]) {
                high++;
            }
            while (low - 1 >= 0 && high + 1 < n && chars[low - 1] == chars[high + 1]) {
                high++;
                low--;
            }
            if (high - low > arr[1] - arr[0]) {
                arr[0] = low;
                arr[1] = high;
            }
        }
        return s.substring(arr[0], arr[1] + 1);
    }
```

这种写法和上面👆第一种写法思想类似。

### [7. 整数反转](https://leetcode-cn.com/problems/reverse-integer/)

```java
class Solution {
    public int reverse(int x) {
        long n = 0;
        while(x != 0) {
            n = n*10 + x%10;
            x = x/10;
        }
        return (int)n==n ? (int)n:0;
    }
}
```

还有一种简单的方法，字符串反转。如果出现Integer.parseInt()异常，就返回0；

### [15. 三数之和](https://leetcode-cn.com/problems/3sum/)

最先想到的肯定是暴力求解法，先对数组排个序，三层循环，判断重复组合，简单易懂。

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        if(nums == null || nums.length < 3){
            return new ArrayList();
        }
        Arrays.sort(nums);
        List list = new ArrayList();

        HashSet set = new HashSet();

        for(int i = 0;i<nums.length - 2;i++){
            for(int j = i + 1; j < nums.length - 1; j++){
                for(int t = j + 1; t < nums.length; t++){
                    if(nums[i] + nums[j] + nums[t] == 0){
                        List temp = new ArrayList();
                        temp.add(nums[i]);
                        temp.add(nums[j]);
                        temp.add(nums[t]);
                        String flag = nums[i] + "" + nums[j] + "" +nums[t] ;
                        if(set.contains(flag)){
                            continue;
                        }
                        set.add(flag);
                        list.add(temp);
                    }
                }
            }
        }

        return list;
    }
}
```

但是这种方式肯定是比较差的，leetcode上也是直接执行超时了。

优化 👇： 

<img src="/Users/gaolei/Library/Application Support/typora-user-images/image-20210902154107965.png" alt="image-20210902154107965" style="zoom:50%;" />

[**解题方案**](https://leetcode-cn.com/problems/3sum/solution/hua-jie-suan-fa-15-san-shu-zhi-he-by-guanpengchn/)

首先对数组进行排序，排序后固定一个数 nums[i]nums[i]，再使用左右指针指向 nums[i]nums[i]后面的两端，数字分别为 nums[L]nums[L] 和 nums[R]nums[R]，计算三个数的和 sumsum 判断是否满足为 00，满足则添加进结果集
如果 nums[i]nums[i]大于 00，则三数之和必然无法等于 00，结束循环
如果 nums[i]nums[i] == nums[i-1]nums[i−1]，则说明该数字重复，会导致结果重复，所以应该跳过
当 sumsum == 00 时，nums[L]nums[L] == nums[L+1]nums[L+1] 则会导致结果重复，应该跳过，L++L++
当 sumsum == 00 时，nums[R]nums[R] == nums[R-1]nums[R−1] 则会导致结果重复，应该跳过，R--R−−
时间复杂度：O(n^2)O(n 2 )，nn 为数组长度

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> result = new ArrayList();
        if(nums == null || nums.length < 3){
            return new ArrayList();
        }

        Arrays.sort(nums);
        // -4,-1,-1,0,1,2
        for(int i=0; i< nums.length; i++) {
            int l = i + 1;
            int r = nums.length - 1;
            if(nums[i] > 0) {
                continue;
            }
            
            if(i > 0 && nums[i] == nums[i-1] ){
                continue;
            }

            while(l < r){
                int count = nums[i] + nums[l] + nums[r];
                if (count == 0) {
                    
                    List list = new ArrayList();
                    list.add(nums[i]);
                    list.add(nums[l]);
                    list.add(nums[r]);
                    result.add(list);
                    while(l < r && nums[l] == nums[l+1]){
                        l++;
                    }
                    while(l < r && nums[r] == nums[r-1]){
                        r--;
                    }
                    l++;
                    r--;
                    continue;
                }
                if(count > 0){
                    r--;
                }              
                if(count < 0){
                    l++;
                }
            } 
        }

        return result;
    }
}
```

### [19. 删除链表的倒数第 N 个结点](https://leetcode-cn.com/problems/remove-nth-node-from-end-of-list/)

<img src="/Users/gaolei/Library/Application Support/typora-user-images/image-20210902160727578.png" alt="image-20210902160727578" style="zoom:67%;" />

思路： 快慢指针先找到倒数第K个节点，然后把这个节点的前节点设置成这个节点的后节点。

https://leetcode-cn.com/problems/lian-biao-zhong-dao-shu-di-kge-jie-dian-lcof/

![image-20210902161032803](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210902161032803.png)

```java
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public ListNode removeNthFromEnd(ListNode head, int n) {

        ListNode l = head;
        ListNode r = head;
        ListNode result = new ListNode();
        ListNode node = result;

        while(r != null && n > 0){
            r = r.next;
            n--;
        }

        while(r != null){
            r = r.next;
            result.next = l;
            l = l.next;
            result = result.next;
        }
        
        result.next = l.next;
        return node.next;
    }
}
```

### [24. 两两交换链表中的节点](https://leetcode-cn.com/problems/swap-nodes-in-pairs/)

给定一个链表，两两交换其中相邻的节点，并返回交换后的链表。

**你不能只是单纯的改变节点内部的值**，而是需要实际的进行节点交换。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210911204758796.png" alt="image-20210911204758796" style="zoom:50%;" />

**解题思路：**

[【猿来绘（逻辑清晰，简单易懂）- 24. 两两交换链表中的节点】](https://leetcode-cn.com/problems/swap-nodes-in-pairs/solution/yuan-lai-hui-luo-ji-qing-xi-jian-dan-yi-8t93h/)

**非递归：**

```java
class Solution {
     public static ListNode swapPairs(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode result = new ListNode();
        result.next = head;
        ListNode curr = result;
        while(curr.next != null && curr.next.next != null){
           ListNode temp = curr;
           ListNode first = curr.next;
           ListNode second = first.next;
           temp.next = second;
           first.next = second.next;
           second.next = first;
            curr = curr.next.next;   
        }
        return result.next;
    }
}
```

**递归：**

```java
class Solution {
    public ListNode swapPairs(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode next = head.next;
        head.next = swapPairs(next.next);
        next.next = head;
        return next;
    }
}

作者：guanpengchn
链接：https://leetcode-cn.com/problems/swap-nodes-in-pairs/solution/hua-jie-suan-fa-24-liang-liang-jiao-huan-lian-biao/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```

### [61. 旋转链表](https://leetcode-cn.com/problems/rotate-list/)

给你一个链表的头节点 `head` ，旋转链表，将链表每个节点向右移动 `k` 个位置。

解题思路参见：👇

https://leetcode-cn.com/problems/rotate-list/solution/dong-hua-yan-shi-kuai-man-zhi-zhen-61-xu-7bp0/

> 向右移动k个位置就相当于倒数第k个节点做头节点，把前面的部分拼后后面就完事了，思路很简单，就这一句话。
>
> 这种思路最核心的点在于寻找倒数第k个点，这个和上面19题差不多。
>
> 快慢指针寻找倒数第k个点，快的走两步，慢的走一步，步数走完或者节点为空了，慢节点就是倒数第k个点

```java
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public ListNode rotateRight(ListNode head, int k) {
        if(k == 0 || head == null || head.next == null){
            return head;
        }

        int length = 0;
        ListNode node = head;
        while(node != null){
            length++;
            node = node.next;
        }

        int target = k % length;
        if(target == 0){
            return head;
        }

        ListNode origin = head;
        ListNode first = head;
        ListNode second = head;
        int num = target + 1;
        while(first != null && num > 0){
            first = first.next;
            num--;
        }

        while(first != null){
            first = first.next;
            second = second.next;
        }

        ListNode newNode = second.next;
        second.next = null;

        ListNode tail = newNode;
        while(tail.next != null){
            tail = tail.next;
        }

        tail.next = origin;
        return newNode;
    }
}
```

