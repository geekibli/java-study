# mybatis中占位符$和#的区别


- 使用#{}传入参数时，sql语句解析借助log4j输出日志看到的sql语句是

`select * from table_name where columns_name = ？`

- 使用${}传入参数是，借助log4j打印出来的是字符串拼接，所以，#{}传参可以防止 **sql注入**

这个可以参考log4j之前的几次重大漏洞。
