---
title: Spring-事务隔离
toc: true
date: 2021-08-18 11:10:21
tags: Spring
categories:
---



如何使用Spring来对程序进行事务控制和管理？

事务控制一般是在哪一层（controller? Service? Dao?） service

事务控制有几种方式？



## 事务控制分类

### 编程式事务

**自己手动控制事务，就叫做编程式事务控制。**

- Jdbc代码：

- - Conn.setAutoCommite(false);  // 设置手动控制事务

- Hibernate代码：

- - Session.beginTransaction();   // 开启一个事务

- **【细粒度的事务控制： 可以对指定的方法、指定的方法的某几行添加事务控制】**

- **(比较灵活，但开发起来比较繁琐： 每次都要开启、提交、回滚.)**

- 人为控制容易产生难以控制的问题

### 声明式事务



### **TransactionDefinition**事务属性

#### 事务传播类型

##### 1、**TransactionDefinition.PROPAGATION_REQUIRED**

使用的最多的一个事务传播行为，我们平时经常使用的`@Transactional`注解默认使用就是这个事务传播行为。<font color=blue>如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。</font>也就是说：

1. 如果外部方法没有开启事务的话，`Propagation.REQUIRED`修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。
2. 如果外部方法开启事务并且被`Propagation.REQUIRED`的话，所有`Propagation.REQUIRED`修饰的内部方法和外部方法均属于同一事务 ，只要一个方法回滚，整个事务均回滚。

> 如果内部方法的事务也开启`Propagation.REQUIRED`, 并且抛出异常，不管外层方法是否catch异常，整个外层方法都会回滚



##### **2、TransactionDefinition.PROPAGATION_REQUIRES_NEW**

创建一个新的事务，如果当前存在事务，则把当前事务挂起。也就是说不管外部方法是否开启事务，`Propagation.REQUIRES_NEW`修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。



##### 3、**TransactionDefinition.PROPAGATION_NESTED**:

如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于`TransactionDefinition.PROPAGATION_REQUIRED`。也就是说：

1. 在外部方法未开启事务的情况下`Propagation.NESTED`和`Propagation.REQUIRED`作用相同，修饰的内部方法都会新开启自己的事务，且开启的事务相互独立，互不干扰。
2. 如果外部方法开启事务的话，`Propagation.NESTED`修饰的内部方法属于外部事务的子事务，外部主事务回滚的话，子事务也会回滚，而内部子事务可以单独回滚而不影响外部主事务和其他子事务。



##### 4、**TransactionDefinition.PROPAGATION_MANDATORY**

如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。（mandatory：强制性）

**若是错误的配置以下 3 种事务传播行为，事务将不会发生回滚，这里不对照案例讲解了，使用的很少。**



##### **5、TransactionDefinition.PROPAGATION_SUPPORTS

如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。



##### 6、TransactionDefinition.PROPAGATION_NOT_SUPPORTED

以非事务方式运行，如果当前存在事务，则把当前事务挂起。



##### 7、TransactionDefinition.PROPAGATION_NEVER

以非事务方式运行，如果当前存在事务，则抛出异常。



#### Isolation事务隔离级别

```java
public enum Isolation {

 DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),

 READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),

 READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),

 REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),

 SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);

 private final int value;

 Isolation(int value) {
  this.value = value;
 }

 public int value() {
  return this.value;
 }
}
```



**`TransactionDefinition.ISOLATION_DEFAULT`** :使用后端数据库默认的隔离级别，MySQL 默认采用的 `REPEATABLE_READ` 隔离级别 Oracle 默认采用的 `READ_COMMITTED` 隔离级别.

**`TransactionDefinition.ISOLATION_READ_UNCOMMITTED`** :最低的隔离级别，使用这个隔离级别很少，因为它允许读取尚未提交的数据变更，**可能会导致脏读、幻读或不可重复读**

**`TransactionDefinition.ISOLATION_READ_COMMITTED`** : 允许读取并发事务已经提交的数据，**可以阻止脏读，但是幻读或不可重复读仍有可能发生**

**`TransactionDefinition.ISOLATION_REPEATABLE_READ`** : 对同一字段的多次读取结果都是一致的，除非数据是被本身事务自己所修改，**可以阻止脏读和不可重复读，但幻读仍有可能发生。**

**`TransactionDefinition.ISOLATION_SERIALIZABLE`** : 最高的隔离级别，完全服从 ACID 的隔离级别。所有的事务依次逐个执行，这样事务之间就完全不可能产生干扰，也就是说，**该级别可以防止脏读、不可重复读以及幻读**。但是这将严重影响程序的性能。通常情况下也不会用到该级别。



#### 事务超时时间

所谓事务超时，就是指一个事务所允许执行的最长时间，如果超过该时间限制但事务还没有完成，则自动回滚事务。在 `TransactionDefinition` 中以 int 的值来表示超时时间，其单位是秒，默认值为-1。



#### 事务只读属性

```java
package org.springframework.transaction;

import org.springframework.lang.Nullable;

public interface TransactionDefinition {
    ......
    // 返回是否为只读事务，默认值为 false
    boolean isReadOnly();

}
```

对于只有读取数据查询的事务，可以指定事务类型为 readonly，即只读事务。只读事务不涉及数据的修改，数据库会提供一些优化手段，适合用在有多条数据库查询操作的方法中。

很多人就会疑问了，为什么我一个数据查询操作还要启用事务支持呢？

拿 MySQL 的 innodb 举例子，根据官网 [dev.mysql.com/doc/refman/…](https://link.juejin.cn?target=https%3A%2F%2Fdev.mysql.com%2Fdoc%2Frefman%2F5.7%2Fen%2Finnodb-autocommit-commit-rollback.html) 描述：

> “
>
> MySQL 默认对每一个新建立的连接都启用了`autocommit`模式。在该模式下，每一个发送到 MySQL 服务器的`sql`语句都会在一个单独的事务中进行处理，执行结束后会自动提交事务，并开启一个新的事务。

但是，如果你给方法加上了`Transactional`注解的话，这个方法执行的所有`sql`会被放在一个事务中。如果声明了只读事务的话，数据库就会去优化它的执行，并不会带来其他的什么收益。

如果不加`Transactional`，每条`sql`会开启一个单独的事务，中间被其它事务改了数据，都会实时读取到最新值。

分享一下关于事务只读属性，其他人的解答：

1. 如果你一次执行单条查询语句，则没有必要启用事务支持，数据库默认支持 SQL 执行期间的读一致性；
2. 如果你一次执行多条查询语句，例如统计查询，报表查询，在这种场景下，多条查询 SQL 必须保证整体的读一致性，否则，在前条 SQL 查询之后，后条 SQL 查询之前，数据被其他用户改变，则该次整体的统计查询将会出现读数据不一致的状态，此时，应该启用事务支持



#### rollback事务回滚规则

`@Transactional(rollbackFor= MyException.class)` 发生如下异常时，触发事务回滚。





## 参考资料

[Spring DAO模块知识](https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247483965&idx=1&sn=2cd6c1530e3f81ca5ad35335755ed287&chksm=ebd7433cdca0ca2a70cb8419306eb9b3ccaa45b524ddc5ea549bf88cf017d6e5c63c45f62c6e&scene=21###wechat_redirect)

https://mp.weixin.qq.com/s/IglQITCkmx7Lpz60QOW7HA

https://juejin.cn/post/6844904160005996552

