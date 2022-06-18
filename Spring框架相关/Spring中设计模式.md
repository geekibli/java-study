# Spring中的设计模式

## 工厂模式

在各种BeanFactory以及ApplicationContext创建中都用到了

## 模版模式

在各种BeanFactory以及ApplicationContext实现中都有用到了

## 代理模式

在AOP实现中用到了JDK的动态代理

## 单例模式
实例bean的创建默认都是单例模式创建

## 策略模式

在Spring中，我们可以使用JdbcTemplate实现对数据库的CRUD操作，而在查询时我们可能会用到RowMapper接口以及spring提供的一个BeanPropertyRowMapper的实现类，RowMapper接口就是规范，而我们根据实际业务需求编写的每个实现类，都是一个达成目标的策略

## 观察者模式

Spring在java ee应用创建的WebApplicationContext时，是通过一个ContextLoaderListener监听器实现的，监听器就是观察者模式的具体实现

## 适配器模式

在springframework中提供了spring mvc的开发包，我们在用spring mvc中，它实现控制器方式有很多，例如我们常用的使用@Controller注解，或者实现Controller接口或者实现HttpRequestHandler接口等等。
而且 DispatcherServlet中如何处理这三种不同的控制器呢，它用到了适配器，用于对不同的实现方式适配。
