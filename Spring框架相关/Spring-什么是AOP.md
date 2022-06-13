---
title: Spring-什么是AOP
toc: true
date: 2021-09-03 14:20:06
tags: Spring
categories:
---

## AOP概述

**Aop： aspect object programming  面向切面编程**

- **功能： 让关注点代码与业务代码分离！**
- 面向切面编程就是指： **对很多功能都有的重复的代码抽取，再在运行的时候往业务方法上动态植入“切面类代码”。**

举个例子，比如我们需要监控一个方法的执行时长，方法结束时间 - 进入方法的时间。

如果就几个方法，我们完全可以在这几个方法上实现，进入方法是获取当前时间，退出时时间戳做一下差就完事了。

但是如果很多方法或者接口需要监控，怎么办，AOP就体现出来了。

```java
@Aspect
@Order(1)
@Component
@Lazy(false)
public class TimeAspect {

    private Logger logger = LoggerFactory.getLogger(getClass());
		
  	// 切入点表达式主要就是来配置拦截哪些类的哪些方法
    @Pointcut("@annotation(com.yj.common.app.annotation.Time)")
    public void timePointCut() {
      // 好处是避免before，after等每个方法都写一遍，这样只写一遍就好了
    }
		
  	// 切入点的使用，
    @Around("timePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        Time time = method.getAnnotation(Time.class);
        long beginTime = System.currentTimeMillis();
        Object object = point.proceed();
        logger.info("{} : execute_totalTime: {} ms", time.value(), (System.currentTimeMillis() - beginTime));
        return object;
    }
}
```

这样，我们仅仅需要在需要监控的方法上添加@Time就可以了，当然还有一个参数，备注一些信息。



## AOP注解

- **@Aspect**               指定一个类为切面类
- **@Pointcut("execution(\* cn.itcast.e_aop_anno.\*.\*(..))")  指定切入点表达式**
- **@Before("pointCut_()")**         前置通知: 目标方法之前执行
- **@After("pointCut_()")**         **后置通知：目标方法之后执行（始终执行）**
- @AfterReturning("pointCut_()")       返回后通知： **执行方法结束前执行(异常不执行)**
- @AfterThrowing("pointCut_()")       异常通知:  出现异常时候执行
- @Around("pointCut_()")         环绕通知： 环绕目标方法执行



## 动态代理

静态代理**需要实现目标对象的相同接口，那么可能会导致代理类会非常非常多**，所以会产生动态代理。

JDK自带的动态代理JDK Proxy在实现的时候有一个限制**，代理的对象一定是要有接口的。**没有接口的话不能实现动态代理。

看一下JDK的API就明白了

```java
public static Object newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h)
```

所以JDK的动态代理就存在一定的局限性。

而cglib则比较灵活，cglib代理也叫子类代理，**从内存中构建出一个子类来扩展目标对象的功能！**

**CGLIB是一个强大的高性能的代码生成包，它可以在运行期扩展Java类与实现Java接口。它广泛的被许多AOP的框架使用，例如Spring AOP和dynaop，为他们提供方法的interception（拦截）。**



<font color=blue>在Spring的动态代理中，如果代理对象是有接口的，代理的实现是JDK的Proxy，如果代理对象不是接口的，代理的实现是通过cglib。</font>













[Spring AOP就是这么简单](https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247483954&idx=1&sn=b34e385ed716edf6f58998ec329f9867&chksm=ebd74333dca0ca257a77c02ab458300ef982adff3cf37eb6d8d2f985f11df5cc07ef17f659d4&scene=21###wechat_redirect)

[三歪红着眼睛总结了Spring知识点](https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247487013&idx=1&sn=f0d8c292738eb49bcd09cb2f6458dc69&chksm=ebd74f24dca0c632fa3ef8f205a2dd5c96531f78a68eae805e15b84de0b59774196a188aed14&token=306734573&lang=zh_CN#rd)

