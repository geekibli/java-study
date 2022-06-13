---
title: Spring梳理启动脉络
toc: true
date: 2021-07-28 17:33:20
tags: Spring
categories: [Spring Family , Spring Framework]
---


# Spring是如何启动的

Spring最大的核心就是Bean容器；
容器： 从对象创建，使用和销毁全部由容器帮我们控制，用户仅仅使用就可以。

## 两大核心
- IOC 控制反转
- AOP 面向切面编程

思考：我们是如何使用Spring的呢？

>  加入从配置文件中加载bean 我们猜想一下大致流程是怎样的

```xml
<bean id=getPerson class=com.ibli.Person>
<property name=id value=1>
<property name=age value=20>
```

配置文件如上👆，这里是伪代码！

先猜想大致流程：

<img src="https://oscimg.oschina.net/oscnet/up-c5313f104a205f008fde4f4b729507c80e7.png">


通过上面猜想创建的对象流程，创建出对象，对象已经好了，就是使用了，那么如何使用呢？

一般情况下我们会可以这样使用，写一下伪代码吧👇：

```java
创建一个ApplicationContext对象
Object obj = applicationContext.getBean("bean name); 
```

思考，创建的对象如何存储？ 或者容器到底是什么呢？ 应该可以猜到是Map结构，具体是什么Map,先不管；

<img src="https://oscimg.oschina.net/oscnet/up-baea159953c9590bba80022b251df95a255.png" >

> 1、首先容器是创建好的，容器创建好之后，才可以加载配置文件

也就是我们猜想的Map

> 2、加载配置文件

配置文件可能会有多种方式，比如XML格式，property格式，yaml格式，注解格式，这个格式各不相同，又是如何加载的呢？
Spring提供了一个接口，BeanDefinitionReader,它有一个抽象实现类AbstractBeanDefinitionReader，不同配置文件的Reader来继承这个抽象类，实现它们自己的逻辑；
```java
public class PropertiesBeanDefinitionReader extends AbstractBeanDefinitionReader
public class GroovyBeanDefinitionReader extends AbstractBeanDefinitionReader implements GroovyObject
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader
```

> 3、读取的配置文件会转换成Spring定义的格式，也就是BeanDefinition；

BeanDefinition定义了类的所有相关的数据； 此时得到的BeanDefinition的属性值只是「符号类型」,并不是真正的属性值；

我们可能会见过这中加载数据源的方式👇
```xml
<bean id=dataSource class=com.alibab.durid.pool.DruidDataSource>
<property name=url value=${jdbc.url}>
<property name=username value=${jdbc.username}>
<property name=password value=${jdbc.password}>
</bean> 
```
数据源的具体配置是放在配置文件中的，当通过XmlBeanDefinitionReader读取并解析到的BeanDefinition，仅仅是将Xml中的文件数据存放到BeanDefinition中，属性的值是${jdbc.url}而不是真正的我们数据源的地址；

> 4、得到所有的BeanDefinition之后，通过BeanFactoryPostProcessor来处理上一步骤中，属性value不是真实数据的问题

比如PlaceHolderConfigurerSupport(占位符处理) 经过工厂后置处理器处理之后，BeanDefinition的属性值就是真实需要的数据了；

> 5、BeanDefinition数据准备完成之后，由BeanFactory来完成Bean的创建

- 实例化 对象中分配堆内存等操作
> 反射调用无参构造函数 创建对象 但是属性是空的
- 初始化

> 6、初始化之前需要准备的工作 

1、准备BeanPostProcessors
2、观察者模式，准备监听器 事件 广播器

> 7、初始化环节有很多步骤

- 对象的填充
> 其实就是调用get/set方法对属性赋值
- 调用aware方法
> 如果我们的对象中的属性是BeanFactory 我们不用自己去完成setBeanFactory方法，只需要当前类实现BeanFactoryAware方法即可
```java
public interface BeanFactoryAware extends Aware {
    void setBeanFactory(BeanFactory var1) throws BeansException;
}
```

- 处理before操作
- 调用init方法
- 执行after方法
> before和after此处是调用的BeanPostProcessor的方法
```language
// 前置方法
postProcessBeforeInitialization
// 后置方法
postProcessAfterInitialization
```


> 8、执行到此，完成对象的创建，得到一个可以使用的对象

