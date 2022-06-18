---
title: SpringMVC-核心讲解
toc: true
date: 2021-09-03 17:10:00
tags: Spring
categories:
---

# Spring MVC的工作流程

- SpringMVC属于Spring那个模块
- SpringMVC替我们做了哪些工作



## SpringMVC如何简化工作的

1、请求参数不需要在手动平装到对象上了。可以直接使用@RequestBody @RequestHeader

2、SpringMVC增强了对文件的处理 `MultipartFile`



## SpringMVC工作流程


![](https://oscimg.oschina.net/oscnet/up-daf79a9b206b871056b99140133e625e8f0.png)

- **用户发送请求**
- **请求交由核心控制器处理**
- **核心控制器找到映射器，映射器看看请求路径是什么**
- **核心控制器再找到适配器，看看有哪些类实现了Controller接口或者对应的bean对象**
- **将带过来的数据进行转换，格式化等等操作**
- **找到我们的控制器Action，处理完业务之后返回一个ModelAndView对象**
- **最后通过视图解析器来对ModelAndView进行解析**
- **跳转到对应的JSP/html页面**



## Spring MVC如何进行参数绑定？


### 接受参数可以是基本类型

![](https://oscimg.oschina.net/oscnet/up-80f52aca51905e09b53f875995dd1d8a781.png)

> 要求：  控制器方法的型参必须和表单元素的name属性一致，如上所示 

如果不一致，可以在 `@RequestParam('name1')` 这样来接受，那表单中对应的就是name1

如下所示 👇  

![](https://oscimg.oschina.net/oscnet/up-b112c1ef9573bb3b0eca07cb508c15c4e66.png)


### 实体类类型

控制器方法的形参是实体类型，表单元素的name属性取值必须和实体类中的属性一致。

![](https://oscimg.oschina.net/oscnet/up-984f9a15b39c92c9314fb4d9e3005fbbccf.png)

如果实体类中还有实体类：

![](https://oscimg.oschina.net/oscnet/up-c5e8af6291a7702f1e344b247442e38fbf3.png)

表单提交如下：

![](https://oscimg.oschina.net/oscnet/up-958629cbf06322965f7289da13aba49b063.png)


### list集合

![](https://oscimg.oschina.net/oscnet/up-a4e1d08bc878f82ec5994da504246f50470.png)

这块注意一下是有 **下标** 的



## Spring MVC 获取表单数据的方式

- 通过HttpServlerRequest.getParameter()
- 通过控制器方法的型参，也就是我们写的controller方法的型参数，配合@RequestParam注解
- 如果需要接受json，可以配合使用@RequestBody注解接收，这块主要实体名称要和form表单中的一致




## 参考资料
> - [三歪肝出了期待已久的SpringMVC](https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247487665&idx=1&sn=cffb9d634a8b770562557b72a833e591&chksm=ebd751b0dca0d8a604d89b1e3c5d1728f35b752368f5a878311066cbc09ff8a5905ad05690c7&token=1936697047&lang=zh_CN#rd)
> - []()

