---
title: JVM-自定义类加载器
toc: true
date: 2021-07-28 21:02:32
tags: JVM
categories: [Develop Lan,Java,JVM]
---

# 如何自定义类加载器

为什么要自定义加载器

原因：
1、存放在自定义路径上的类，需要通过自定义类加载器去加载。【注意：AppClassLoader加载classpath下的类】
2、类不一定从文件中加载，也可能从网络中的流中加载，这就需要自定义加载器去实现加密解密。
3、可以定义类的实现机制，实现类的热部署,
如OSGi中的bundle模块就是通过实现自己的ClassLoader实现的，
如tomcat实现的自定义类加载模型。

如何实现自定义加载器

> 实现自定义类加载有以下两步：
1、继承ClassLoader
2、重写findClass，在findClass里获取类的字节码，并调用ClassLoader中的defineClass方法来加载类，获取class对象。
注意：如果要打破双亲委派机制，需要重写loadClass方法。
如下：是一个自定义 的类加载器

```java
public static class MyClassLoader  extends  ClassLoader{
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] data=null;
            try {
                 data= loadByte(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this.defineClass(data,0,data.length);
        }
        private byte[] loadByte(String name) throws IOException {
            File file = new File("/Users/admin/test/"+name);
            FileInputStream fi = new FileInputStream(file);
            int len = fi.available();
            byte[] b = new byte[len];
            fi.read(b);
            return b;
        }
    }
```


下面是要加载的类：

```java
public class Demo{
public void say(){
System.out.println("hello");
}
}
```

该类编译后的class 文件放置在/Users/admin/test/下,然后执行如下代码去加载：

```java
MyClassLoader classLoader = new MyClassLoader();
        Class clazz = classLoader.loadClass("Demo.class");
        Object o=clazz.newInstance();
        Method method = clazz.getMethod("say");
        method.invoke(o);

输出:hello
```

能不能自己写一个java.lang.String

1、代码书写后可以编译不会报错
2、在另一个类中加载java.lang.String，通过反射调用自己写的String类里的方法，得到结果NoSuchMethod，说明加载的还是原来的String，因为通过双亲委派机制，会把java.lang.String一直提交给启动类加载器去加载，通过他加载，加载到的永远是/lib下面的java.lang.String
3、在这个自己写的类中写上main方法
public static void main(String[] args)
执行main方法报错，因为这个String并不是系统的java.lang.String，所以JVM找不到main方法的签名




## 参考资料
[JVM:如何实现一个自定义类加载器？](https://blog.csdn.net/qq_28605513/article/details/85014451)
原文链接：https://blog.csdn.net/qq_28605513/article/details/85014451