# hashcode方法和equals方法


## 作用

hashCode方法和equals方法都是Object类中的方法。

**如果不重写此方法**

- hashcode返回该对象的地址值
- equals方法比较两个对象地址值是否相等


**如果重写此方法**

- hashcode 返回的是根据对象的成员变量，计算出一个整数
- equals 比较两个对象中的成员信息是否相等


![](https://oscimg.oschina.net/oscnet/up-3daaf9d200aec2991e96b3d8d15c1c4634a.png)


## 类中重写hashcode方法和equals方法比较两个对象是否相等

两个对象通过equals比较相等，那个hashcode一定相等  
但是如果两个对象的hashcode相等，那个equals做比较不一定相等，也即是hashcode不是绝对可靠的


## 通过hashcode 和equals 搭载使用比较对象是否相等，是如何提高效率的？

**问题** 对于一个对象中有大量的成员变量，用equals比较会降低效率。

**解决** 可以先通过hashcode进行比较，如果不想等那么对象一定不相等，如果相同，再通过equals方法进行比较，这样既可以判断对象是否相等，也可以提升效率



## 在HashSet集合中，通过hashcode()和equals（）,保证元素的唯一性

**hashset如何保证元素的唯一性？**

![](https://oscimg.oschina.net/oscnet/up-a59e9a41cb21408518f207819b86b89c299.png)



![](https://oscimg.oschina.net/oscnet/up-c09bc7a47de63785a734d8e4a59b9cfaeb8.png)