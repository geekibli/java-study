---
title: MySQL-ShardingJDBC分库分表实战
toc: true
date: 2021-08-27 19:18:15
tags:
categories:
---

# 分库分表Demo
**项目地址** [https://github.com/geekibli/sharding-demos/](https://github.com/geekibli/sharding-demos/tree/main/simple-sharding-stratery)
## Springboot + Sharding jdbc

### maven依赖 
```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
            <version>4.0.0-RC1</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.0.0</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.47</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.1.16</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.47</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
```
### 数据源配置
```java
@Configuration
public class DuridConfig {

    @Bean
    public Filter statFilter() {
        StatFilter filter = new StatFilter();
        filter.setSlowSqlMillis(5000);
        filter.setLogSlowSql(true);
        filter.setMergeSql(true);
        return filter;
    }

    @Bean
    public ServletRegistrationBean statViewServlet() {
        //创建servlet注册实体
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        //设置ip白名单
        servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
        //设置ip黑名单，如果allow与deny共同存在时,deny优先于allow
        //servletRegistrationBean.addInitParameter("deny","192.168.0.19");
        //设置控制台管理用户
        servletRegistrationBean.addInitParameter("loginUsername", "root");
        servletRegistrationBean.addInitParameter("loginPassword", "123456");
        //是否可以重置数据
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        return servletRegistrationBean;
    }

}
```

### 配置文件
```properties
server.port=8071

# mybatis 配置
mybatis.mapper-locations=classpath:mapping/*.xml
mybatis.type-aliases-package=com.ibli.sharding.simple.domain

spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://localhost:3331/ds0?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&allowMultiQueries=true&useSSL=false
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=123456

spring.shardingsphere.datasource.ds1.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://localhost:3331/ds1?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&allowMultiQueries=true&useSSL=false
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=123456

# 分库配置 ， 根据member_id分库
spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column=member_id
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression=ds$->{member_id % 2}

# 分表配置，根据member_id分表
spring.shardingsphere.sharding.tables.member.actual-data-nodes=ds$->{0..1}.member
spring.shardingsphere.sharding.tables.member.table-strategy.inline.sharding-column=member_id
spring.shardingsphere.sharding.tables.member.table-strategy.inline.algorithm-expression=member$->{member_id % 2}
spring.shardingsphere.sharding.tables.member.key-generator.column=member_id
spring.shardingsphere.sharding.tables.member.key-generator.type=SNOWFLAKE

spring.shardingsphere.props.sql.show=true

```

### 插入数据
```java
@RequestMapping("/add")
    public Member add(){
        Member member = new Member();
        //不用手动设置主键id，新增时，sharding-jdbc会自动赋值，因为在配置文件中配置了该列使用SNOWFLAKE算法生成值
//        member.setMemberId(IdWorker.getLongId());
        member.setNickName("nickname");
        member.setAccountNo(new Date().hashCode());
        member.setPassword(UUID.randomUUID().toString());
        member.setAge(10);
        member.setDelFlag(UUID.randomUUID().toString());
        memberServiceImpl.insert(member);
        return member;
    }
```

### SQL文件
```sql
CREATE TABLE `member0` (
  `member_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nick_name` varchar(255) DEFAULT NULL,
  `account_no` int(11) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `birthday` varchar(255) DEFAULT NULL,
  `del_flag` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;


CREATE TABLE `member1` (
  `member_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nick_name` varchar(255) DEFAULT NULL,
  `account_no` int(11) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `birthday` varchar(255) DEFAULT NULL,
  `del_flag` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;
```

###  启动项目
#### 访问创建用户接口
`curl -X GET http://localhost:8071/member/add`

#### 查看数据库
<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/20210830141127.png">

## 参考资料

https://juejin.cn/post/6844903773383426061

https://juejin.cn/post/6890772387000762382

https://shardingsphere.apache.org/document/current/cn/quick-start/shardingsphere-proxy-quick-start/

https://www.pianshen.com/article/7996383507/

