# Java字符串

1. String属于什么数据类型
2. String常用的方法
3. Java中的内存分配
4. String创建对象有什么特点



```
String s = new String("xyz");
```

创建了几个StringObject,是否可以继承String类？



## 1. 属于什么数据类型

String是一个final修饰的类。 不可以继承String类。
String不是8中数据类型中的一种。


```java
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {}
```


## 常用的方法

- equals 比较内容是否相等


![](https://oscimg.oschina.net/oscnet/up-c71b630d4c8c934c9f85650bf646fb460e0.png)



## 内存分配


![](https://oscimg.oschina.net/oscnet/up-60460ab199beea9b2958f83ec1a89863b18.png)



![](https://oscimg.oschina.net/oscnet/up-53aa2434e4e184e8374ea5721599e694ff3.png)



![](https://oscimg.oschina.net/oscnet/up-c7d44ffa5621819652a9d04dac1c7479774.png)



![](https://oscimg.oschina.net/oscnet/up-adfe2a7025b8648551226e6fd04a3f5272c.png)