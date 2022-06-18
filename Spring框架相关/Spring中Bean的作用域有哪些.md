# Spring中Bean的作用域有哪些？

Spring IOC容器在根据配置（可以是xml，也可以是注解）创建一个bean对象实例时，可以为Bean制定实例的作用域。

## 作用域

- singleton 单例模式
- prototype 原型模式
- request http请求
- session 会话
- global-session 全局会话 在spring5.x版本中已经移除了


## 五种作用范围说明

## singleton

IOC容器进创建一个Bean实例，IOC容器每次返回的都是同一个Bean实例

## prototype

IOC容器可以创建多个实例，每次返回的都是一个新的实例

## request

该属性仅对http请求起作用，每次http请求都会创建一个新的bean，适用于WebApplicationContext环境。


## session

改属性仅用于HttpSession，同一个Session共享一个Bean实例，不同的Session使用不同的实例。

## global-session

该属性仅用于Http Session，所有的Session共享一个Bean实例。
