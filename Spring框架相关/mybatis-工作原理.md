---
title: mybatis-工作原理
toc: true
date: 2021-07-28 16:52:38
tags: mybatis
categories: [Spring Family,mybatis]
---


# Mybatis工作原理


## Mybatis整体框架
![](https://oscimg.oschina.net/oscnet/up-978b1c01a681073e4a020fdce7703887901.png)  

## 工作原理解析

![](https://oscimg.oschina.net/oscnet/up-0015501b933b36728b8f66f281d78358e97.png)  


1）读取MyBatis配置文件：mybatis-config.xml 为 MyBatis 的全局配置文件，配置了 MyBatis 的运行环境等信息，例如数据库连接信息。
> 读取配置文件将mybatis-config.xml转换为org.apache.ibatis.session.Configuration类，这里mybatis包含9个全局配置；

2）加载映射文件。映射文件即 SQL 映射文件，该文件中配置了操作数据库的 SQL 语句，需要在 MyBatis 配置文件 mybatis-config.xml 中加载。mybatis-config.xml 文件可以加载多个映射文件，每个文件对应数据库中的一张表。
> 扫描Mapping目录下的***Mapper.xml文件； 

3）构造会话工厂：通过 MyBatis 的环境等配置信息构建会话工厂 SqlSessionFactory。
```text
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(HikariDataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:com/****/mapping/**/*.xml"));
        return bean.getObject();
    }
```
生成工厂实例：
```java
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {
    private static final Log LOGGER = LogFactory.getLog(SqlSessionFactoryBean.class);
    private Resource configLocation;
    private Configuration configuration;
    private Resource[] mapperLocations;
    private DataSource dataSource;
    private TransactionFactory transactionFactory;
    private Properties configurationProperties;
    private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
    private SqlSessionFactory sqlSessionFactory;
    private String environment = SqlSessionFactoryBean.class.getSimpleName();
    private boolean failFast;
    private Interceptor[] plugins;
    private TypeHandler<?>[] typeHandlers;
    private String typeHandlersPackage;
    private Class<?>[] typeAliases;
    private String typeAliasesPackage;
    private Class<?> typeAliasesSuperType;
    private DatabaseIdProvider databaseIdProvider;
    private Class<? extends VFS> vfs;
    private Cache cache;
    private ObjectFactory objectFactory;
    private ObjectWrapperFactory objectWrapperFactory;

    public SqlSessionFactoryBean() {
    }
}
```
4）创建会话对象：由会话工厂创建 SqlSession 对象，该对象中包含了执行 SQL 语句的所有方法。
> SqlSession对象完成和数据库的交互： 

5）Executor 执行器：MyBatis 底层定义了一个 Executor 接口来操作数据库，它将根据 SqlSession 传递的参数动态地生成需要执行的 SQL 语句，同时负责查询缓存的维护。
> 3种执行期类型（Simple Pre Batch）  Executor接口有两个实现，一个是基本执行器、一个是缓存执行器。

6）MappedStatement 对象：在 Executor 接口的执行方法中有一个 MappedStatement 类型的参数，该参数是对映射信息的封装，用于存储要映射的 SQL 语句的 id、参数等信息。
> 借助MappedStatement中的结果映射关系，将返回结果转化成HashMap、JavaBean等存储结构并返回。 

7）输入参数映射：输入参数类型可以是 Map、List 等集合类型，也可以是基本数据类型和 POJO 类型。输入参数映射过程类似于 JDBC 对 preparedStatement 对象设置参数的过程。
8）输出结果映射：输出结果类型可以是 Map、 List 等集合类型，也可以是基本数据类型和 POJO 类型。输出结果映射过程类似于 JDBC 对结果集的解析过程。





## 参考资料
【1】[MyBatis的工作原理 C语言网](http://c.biancheng.net/view/4304.html)  
