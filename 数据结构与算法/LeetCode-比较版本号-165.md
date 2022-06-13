---
title: LeetCode-比较版本号(165)
toc: true
date: 2021-09-02 14:08:13
tags:
- LeetCode
- 数组
- 中等
categories:
---

https://leetcode-cn.com/problems/compare-version-numbers/

![image-20210902141257542](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210902141257542.png)

```java
class Solution {
    public int compareVersion(String version1, String version2) {
        
        String [] sp1 = version1.split("\\.");
        String [] sp2 = version2.split("\\.");
        int a = 0,b = 0;
        while(a < sp1.length && b < sp2.length){
            if (Integer.valueOf(sp1[a]) > Integer.valueOf(sp2[b])){
                return 1;
            }
            if (Integer.valueOf(sp1[a]) < Integer.valueOf(sp2[b])){
                return -1;
            }
            a++;
            b++;
        }
        if(a == sp1.length){
            for(int x = b; x < sp2.length;x++){
                if(Integer.valueOf(sp2[x])  > 0){
                    return -1;
                }
            }
        }

        if(b == sp2.length){
            for(int y = a; y < sp1.length; y++){
                if(Integer.valueOf(sp1[y]) > 0){
                    return 1;
                }
            }
        }
        return 0;
    }
}
```

注意点⚠️

1、` a++; `,`b++;`

while里面是两次条件判断，所以 `if (Integer.valueOf(sp1[a]) > Integer.valueOf(sp2[b]))`不能写成	`if (Integer.valueOf(sp1[a++]) > Integer.valueOf(sp2[b++]))`

2、先判断相同长度部分，如果相同长度部分能分出结果就直接返回，如果不能，就比较长度比较长的版本剩余的部分是否比0大就完事了。

