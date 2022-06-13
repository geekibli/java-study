---
title: SpringMVC-核心讲解
toc: true
date: 2021-09-03 17:10:00
tags: Spring
categories:
---



SpringMVC属于Spring那个模块

SpringMVC替我们做了哪些工作







## SpringMVC如何简化工作的

1、请求参数不需要在手动平装到对象上了。可以直接使用@RequestBody @RequestHeader

2、SpringMVC增强了对文件的处理 `MultipartFile`



## SpringMVC工作流程



- **用户发送请求**
- **请求交由核心控制器处理**
- **核心控制器找到映射器，映射器看看请求路径是什么**
- **核心控制器再找到适配器，看看有哪些类实现了Controller接口或者对应的bean对象**
- **将带过来的数据进行转换，格式化等等操作**
- **找到我们的控制器Action，处理完业务之后返回一个ModelAndView对象**
- **最后通过视图解析器来对ModelAndView进行解析**
- **跳转到对应的JSP/html页面**









## 参考资料
> - [三歪肝出了期待已久的SpringMVC](https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247487665&idx=1&sn=cffb9d634a8b770562557b72a833e591&chksm=ebd751b0dca0d8a604d89b1e3c5d1728f35b752368f5a878311066cbc09ff8a5905ad05690c7&token=1936697047&lang=zh_CN#rd)
> - []()

