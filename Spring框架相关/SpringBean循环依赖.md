---
title: SpringBean循环依赖
toc: true
date: 2021-07-28 17:34:20
tags: Spring
categories: [Spring Family , Spring Framework]
---

# Spring Bean 循环依赖


## 为什么会存在循环依赖
<img src="https://oscimg.oschina.net/oscnet/up-4ac2f1ac005d007a18a0823719edb29abf7.png" width=300 height=168>

如上图👆所示，A对象的一个属性是B,B对象的一个属性是A,而Spring中的bean默认情况下都是单例的，所以这两个Bean就产生了循环依赖的问题！
> 那么循环依赖的问题出现在什么情况呢

想一下属性赋值的方式有几种呢？
- 构造器赋值
> 这种形式循环依赖问题无法解决
- GET/SET方法
> 调用SET方法进行赋值的时候，可以通过三级缓存的策略来解决循环依赖的问题

所以，三级缓存的策略是针对于使用SET方法对属性赋值的场景下的！


## 循环依赖如何解决

<img src="https://oscimg.oschina.net/oscnet/up-eabac6749e665ea36856dce17c2119658a7.png">

在实例化的过程中，将处于半成品的对象全部放到缓存中，方便后续来进行调用；
只要有了当前对象的引用地址，那么后续来进行赋值即可；

>d 能不能将创建好的对象也放到缓存中呢？

不能，如果放在一起将无法区分对象是成品对象还是半成品对象了
所以再次引出多级缓存的概念，可以创建两个缓存对象，一个用来存放已经实例化的半成品对象，另一个存放完成实例化并且完成初始化的成品对象，这个应该比较好理解吧！

> 思考一下以上的设计有没有问题呢？


### 为什么需要三级缓存？
Spring在解决对象Bean循环依赖的问题的解决方案是使用了「三级缓存」；
为什么需要三级缓存，也就是三个Map对象；

> org.springframework.beans.factory.support.DefaultSingletonBeanRegistry

```java
// 一级缓存
private final Map<String, Object> singletonObjects = new ConcurrentHashMap(256);
// 二级缓存
private final Map<String, Object> earlySingletonObjects = new HashMap(16);
// 三级缓存
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap(16);
```







> 三级缓存中分别保存的是什么内容
- 一级缓存： 成品对象
- 二级缓存： 半成品对象
- 三级缓存； lambda表达式

> 如果只有二级缓存可不可行

在Spring源码中，只有addSingleton方法和doCreateBean方法中向三级缓存中添加东西的；

org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#addSingletonFactory
```java
 protected void addSingleton(String beanName, Object singletonObject) {
        synchronized(this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

```


org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#getSingleton(java.lang.String, boolean)
```java
 @Nullable
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null && this.isSingletonCurrentlyInCreation(beanName)) {
            synchronized(this.singletonObjects) {
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null && allowEarlyReference) {
                    ObjectFactory<?> singletonFactory = (ObjectFactory)this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }

        return singletonObject;
    }

```


```java
 public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "Bean name must not be null");
        synchronized(this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                if (this.singletonsCurrentlyInDestruction) {
                    throw new BeanCreationNotAllowedException(beanName, "Singleton bean creation not allowed while singletons of this factory are in destruction (Do not request a bean from a BeanFactory in a destroy method implementation!)");
                }

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }

                this.beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                boolean recordSuppressedExceptions = this.suppressedExceptions == null;
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = new LinkedHashSet();
                }

                try {
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                } catch (IllegalStateException var16) {
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        throw var16;
                    }
                } catch (BeanCreationException var17) {
                    BeanCreationException ex = var17;
                    if (recordSuppressedExceptions) {
                        Iterator var8 = this.suppressedExceptions.iterator();

                        while(var8.hasNext()) {
                            Exception suppressedException = (Exception)var8.next();
                            ex.addRelatedCause(suppressedException);
                        }
                    }

                    throw ex;
                } finally {
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = null;
                    }

                    this.afterSingletonCreation(beanName);
                }

                if (newSingleton) {
                    this.addSingleton(beanName, singletonObject);
                }
            }

            return singletonObject;
        }
    }

```
