---
title: 2021年后端大厂-Spring
toc: true
date: 2021-01-09 14:24:05
tags: 面试题
categories:
---



### spring支持几种bean scope？

**Spring bean 支持 5 种 scope：** 

Singleton - 每个 Spring IoC 容器仅有一个单实例。

Prototype - 每次请求都会产生一个新的实例。

Request - 每一次 HTTP 请求都会产生一个新的实例，并且该 bean 仅在当前 HTTP 请求内有效。

Session - 每一次 HTTP 请求都会产生一个新的 bean，同时该 bean 仅在当前 HTTP session 内有效。 

Global-session - 类似于标准的 HTTP Session 作用域，不过它仅仅在基于portlet 的 web 应用中才有意义。

Portlet 规范定义了全局 Session 的概念，它被所有构成某个 portlet web 应用的各种不同的 portlet 所共享。在 globalsession 作用域中定义的 bean 被限定于全局 portlet Session 的生命周期范围内。如果你在 web 中使用 global session 作用域来标识 bean，那么 web会自动当成 session 类型来使用。 仅当用户使用支持 Web 的 ApplicationContext 时，最后三个才可用。



### Spring bean的生命周期是怎样的

spring bean 容器的生命周期流程如下： 

（1）Spring 容器根据配置中的 bean 定义中实例化 bean。 
（2）Spring 使用依赖注入填充所有属性，如 bean 中所定义的配置。 
（3）如果 bean 实现BeanNameAware 接口，则工厂通过传递 bean 的 ID 来调用setBeanName()。 
（4）如果 bean 实现 BeanFactoryAware 接口，工厂通过传递自身的实例来调用 setBeanFactory()。 
（5）如果存在与 bean 关联的任何BeanPostProcessors，则调用preProcessBeforeInitialization() 方法。
（6）如果为 bean 指定了 init 方法（ <bean> 的 init-method 属性），那么将调 用它。
（7）最后，如果存在与 bean 关联的任何 BeanPostProcessors，则将调用 postProcessAfterInitialization() 方法。
（8）如果 bean 实现DisposableBean 接口，当 spring 容器关闭时，会调用 destory()。
（9）如果为bean 指定了 destroy 方法（ <bean> 的 destroy-method 属性），那么将 调用它。



### 什么是Spring的装配

当 bean 在 Spring 容器中组合在一起时，它被称为装配或 bean 装配。Spring容器需要知道需要什么 bean 以及容器应该如何使用依赖注入来将 bean 绑定在一起，同时装配 bean。

### 自动装配有哪些方式

在spring中，对象无需自己查找或创建与其关联的其他对象，由容器负责把需要相互协作的对象引用赋予各个对象，使用autowire来配置自动装载模式。

在Spring框架xml配置中共有5种自动装配： 

（1）no：默认的方式是不进行自动装配的，通过手工设置ref属性来进行装配bean。 

（2）byName：通过bean的名称进行自动装配，如果一个bean的 property 与另一bean 的name 相同，就进行自动装配。 

（3）byType：通过参数的数据类型进行自动装配。 

（4）constructor：利用构造函数进行装配，并且构造函数的参数通过byType进行装配。 

（5）autodetect：自动探测，如果有构造方法，通过 construct的方式自动装配，否则使用 byType的方式自动装配。

### 描述一下 DispatcherServlet 的工作流程

（1）向服务器发送 HTTP 请求，请求被前端控制器 DispatcherServlet 捕获。 

（2） DispatcherServlet 根据 -servlet.xml 中的配置对请求的 URL 进行解析，得到请求资源标识符（URI）。然后根据该 URI，调用 HandlerMapping获得该 Handler 配置的所有相关的对象（包括 Handler 对象以及 Handler 对象对应的拦截器），最后以HandlerExecutionChain 对象的形式返回。

（3） DispatcherServlet 根据获得的 Handler，选择一个合适的HandlerAdapter。（附注：如果成功获得 HandlerAdapter 后，此时将开始执行拦截器的 preHandler(...)方法）。

（4）提取 Request 中的模型数据，填充 Handler 入参，开始执行 Handler（ Controller)。在填充 Handler 的入参过程中，根据你的配置，Spring 将帮你做一些额外的工作： · HttpMessageConveter：将请求消息（如 Json、xml 等数据）转换成一个对象，将对象转换为指定的响应信息。 · 数据转换：对请求消息进行数据转换。如 String 转换成 Integer、Double 等。 · 数据根式化：对请求消息进行数据格式化。如将字符串转换成格式化数字或格式化日期等。 · 数据验证：验证数据的有效性（长度、格式等），验证结果存储到BindingResult 或 Error 中。 

（5）Handler(Controller)执行完成后，向 DispatcherServlet 返回一个ModelAndView 对象； 

（6）根据返回的 ModelAndView，选择一个适合的 ViewResolver（必须是已经注册到 Spring 容器中的 ViewResolver)返回给 DispatcherServlet。 

（7） ViewResolver 结合 Model 和 View，来渲染视图。

（8）视图负责将渲染结果返回给客户端。

### 面试请不要再问我Spring Cloud底层原理

https://juejin.cn/post/6844903705553174541

### 静态代理/动态代理

狂神说 https://mp.weixin.qq.com/s/McxiyucxAQYPSOaJSUCCRQ 

### 什么是Spring IOC

[Spring入门这一篇就够了](https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247483942&idx=1&sn=f71e1adeeaea3430dd989ef47cf9a0b3&chksm=ebd74327dca0ca3141c8636e95d41629843d2623d82be799cf72701fb02a665763140b480aec&scene=21#wechat_redirect)

https://www.tianmaying.com/tutorial/spring-ioc

### Spring依赖注入

[Spring【依赖注入】就是这么简单](https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247483946&idx=1&sn=bb21dfd83cf51214b2789c9ae214410f&chksm=ebd7432bdca0ca3ded6ad9b50128d29267f1204bf5722e5a0501a1d38af995c1ee8e37ae27e7&scene=21#wechat_redirect)    3y

[java后端开发三年！你还不了解Spring 依赖注入，凭什么给你涨薪](https://juejin.cn/post/6850418118721404936)

### 对象创建循环依赖问题

[如何解决循环依赖问题](https://juejin.cn/post/6895753832815394824#heading-0)

#### 一级缓存：Map<String, Object> singletonObjects

第一级缓存的作用？

- 用于存储单例模式下创建的Bean实例（已经创建完毕）。
- 该缓存是对外使用的，指的就是使用Spring框架的程序员。

存储什么数据？

- K：bean的名称
- V：bean的实例对象（有代理对象则指的是代理对象，已经创建完毕）

#### 第二级缓存：Map<String, Object> earlySingletonObjects

第二级缓存的作用？

- 用于存储单例模式下创建的Bean实例（该Bean被提前暴露的引用,该Bean还在创建中）。
- 该缓存是对内使用的，指的就是Spring框架内部逻辑使用该缓存。
- 为了解决第一个classA引用最终如何替换为代理对象的问题（如果有代理对象）请爬楼参考演示案例

存储什么数据？

- K：bean的名称
- V：bean的实例对象（有代理对象则指的是代理对象，该Bean还在创建中）

#### 第三级缓存：Map<String, ObjectFactory<?>> singletonFactories

第三级缓存的作用？

- 通过ObjectFactory对象来存储单例模式下提前暴露的Bean实例的引用（正在创建中）。
- 该缓存是对内使用的，指的就是Spring框架内部逻辑使用该缓存。
- 此缓存是解决循环依赖最大的功臣

存储什么数据？

- K：bean的名称
- V：ObjectFactory，该对象持有提前暴露的bean的引用













