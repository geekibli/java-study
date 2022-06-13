---
title: mybatis配置文件解析
toc: true
date: 2021-07-28 17:05:46
tags: mybatis
categories: [Spring Family]
--- 



# Mybatis配置文件
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
	<!-- 引入数据库属性文件 -->
	<properties  resource="database.properties">
		<!-- <property name="username" value="sa"></property> -->
	</properties>
	<!-- mybatis配置文件 -->
	<settings>
		<setting name="cacheEnabled" value="false"/>
		<setting name="autoMappingBehavior" value="PARTIAL"/>
	</settings>
	<!-- 别名的配置 -->
	<typeAliases>
		<!-- <typeAlias type="com.xit.pojo.User" alias="user"/> -->
		<package name="com.xit.pojo"/>
	</typeAliases>
	
  	<!-- 配置运行环境 -->
  <environments default="default">
    <environment id="default">
    	<!-- 配置事务管理器 -->
    	<!-- 由JDBC管理事务 -->
      <transactionManager type="JDBC"/>
      <!-- 配置数据源：连接池 -->
      <dataSource type="POOLED">
        <property name="driver" value="${driver}"/>
        <property name="url" value="${url}"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${password}"/>
      </dataSource>
    </environment>
  </environments>
  <!-- 引入Mapper映射文件 -->
  <mappers>
  	<mapper resource="com/xit/pojo/UserMapper.xml"/>
  	<!-- URL方式 -->
  	<!-- <mapper url="file:///C:/eclipse-workspace/mybatis-01/src/com/xit/pojo/UserMapper.xml"/> -->
  </mappers>
</configuration>


```




## Mybatis有几部分全局配置
properties=>ettings=>typeAliases=>typeHandlers=>objectFactory=>plugins=>environment=>databaseIdProvider=>mappers


## Mybatis 加载Mapper文件有几种方式？
   
![](https://oscimg.oschina.net/oscnet/up-418b861802897058c608593b4b159d5baa2.png)    
以上是Mybatis官方文档介绍的样例👆，[原文链接请点击这](https://mybatis.org/mybatis-3/zh/configuration.html#mappers)  
   
  **有4种方式；按照优先级从高到底依次是：**  
  - package
  - resource
  - url
  - class
  
下面是Mybatis加载mybatis-config.xml文件配置的源码，从代码中也可以看到加载的4中方式和优先级！👇
**org.apache.ibatis.builder.xml.XMLConfigBuilder#typeHandlerElement**
```java
private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            Iterator var2 = parent.getChildren().iterator();

            while(true) {
                while(var2.hasNext()) {
                    XNode child = (XNode)var2.next();
                    String resource;
                    if ("package".equals(child.getName())) {
                        resource = child.getStringAttribute("name");
                        this.configuration.addMappers(resource);
                    } else {
                        resource = child.getStringAttribute("resource");
                        String url = child.getStringAttribute("url");
                        String mapperClass = child.getStringAttribute("class");
                        XMLMapperBuilder mapperParser;
                        InputStream inputStream;
                        if (resource != null && url == null && mapperClass == null) {
                            ErrorContext.instance().resource(resource);
                            inputStream = Resources.getResourceAsStream(resource);
                            mapperParser = new XMLMapperBuilder(inputStream, this.configuration, resource, this.configuration.getSqlFragments());
                            mapperParser.parse();
                        } else if (resource == null && url != null && mapperClass == null) {
                            ErrorContext.instance().resource(url);
                            inputStream = Resources.getUrlAsStream(url);
                            mapperParser = new XMLMapperBuilder(inputStream, this.configuration, url, this.configuration.getSqlFragments());
                            mapperParser.parse();
                        } else {
                            if (resource != null || url != null || mapperClass == null) {
                                throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                            }

                            Class<?> mapperInterface = Resources.classForName(mapperClass);
                            this.configuration.addMapper(mapperInterface);
                        }
                    }
                }

                return;
            }
        }
    }
```      

## Mybatis有几种执行器
mybatis有3中执行器； 
```text
package org.apache.ibatis.session;

public enum ExecutorType {
    SIMPLE, // 默认
    REUSE,
    BATCH;

    private ExecutorType() {
    }
}
```
