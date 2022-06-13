---
title: Arthas-Java诊断神器
toc: true
date: 2021-08-20 16:28:32
tags: Linux
categories:
---

## Arthas-Java诊断神器

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820172620815.png" alt="image-20210820172620815" style="zoom:50%;"/>

官方文档地址 👉    https://arthas.aliyun.com/doc/index.html#

### 1. 安装arthas

`wget https://arthas.aliyun.com/arthas-boot.jar `

`java -jar arthas-boot.jar --target-ip 0.0.0.0`

如果你的机器没有任何java进程在运行，会提示如下错误 👇

![image-20210820165717792](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820165717792.png)

**正常启动如下：**

⚠️ 由于我们仅仅启动了一个java进程，所有这里就只有一个。输入1回车即可。

![image-20210820165953801](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820165953801.png)

**42423就是我们的java进程号**

![image-20210820170112574](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820170112574.png)



### 2. 查看JVM信息

#### 2.1 sysprop

`sysprop` 可以打印所有的System Properties信息。

也可以指定单个key： `sysprop java.version`

也可以通过`grep`来过滤： `sysprop | grep user`

可以设置新的value： `sysprop testKey testValue`

#### 2.2 sysenv

`sysenv` 命令可以获取到环境变量。和`sysprop`命令类似。

#### 2.3 jvm

`jvm` 命令会打印出`JVM`的各种详细信息。

#### 2.4 dashboard

`dashboard` 命令可以查看当前系统的实时数据面板。

输入 `Q` 或者 `Ctrl+C` 可以退出dashboard命令。

### 3. 查看线程相关

#### 3.1 查看线程列表

`thread` 

![image-20210820163125277](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820163125277.png)

#### 3.2 **查看线程栈信息**

`thread 18` 

![image-20210820163518891](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820163518891.png)

#### 3.3 查看5秒内的CPU使用率top n线程栈

`thread -n 3 -i 5000 `

#### 3.4 查找线程是否有阻塞

`thread -b`



### 4. sc/sm 查看已加载的类

下面介绍Arthas里查找已加载类的命令。

### 4.1 sc 查找到**所有JVM已经加载到的类**

如果搜索的是接口，还会搜索所有的实现类。比如查看所有的`Filter`实现类：

`sc javax.servlet.Filter`

通过`-d`参数，可以打印出类加载的具体信息，很方便查找类加载问题。

`sc -d javax.servlet.Filter`

`sc`支持通配，比如搜索所有的`StringUtils`：

`sc *StringUtils`

### 4.2 sm 查找类的**具体函数**

`sm java.math.RoundingMode`

通过`-d`参数可以打印函数的具体属性：

`sm -d java.math.RoundingMode`

也可以查找特定的函数，比如查找构造函数：

`sm java.math.RoundingMode <init>`

### 5. Jad反编译

可以通过 `jad` 命令来反编译代码：

`jad com.example.demo.arthas.user.UserController`

通过`--source-only`参数可以只打印出在反编译的源代码：

`jad --source-only com.example.demo.arthas.user.UserController`



### 6. Ognl动态代码

在Arthas里，有一个单独的`ognl`命令，可以动态执行代码。

#### 6.1 调用static函数

`ognl '@java.lang.System@out.println("hello ognl")'`

可以检查`Terminal`里的进程输出，可以发现打印出了`hello ognl`。

#### 6.2 查找UserController的ClassLoader

```shell
sc -d com.example.demo.arthas.user.UserController | grep classLoaderHash
$ sc -d com.example.demo.arthas.user.UserController | grep classLoaderHash
 classLoaderHash   1be6f5c3
```

注意hashcode是变化的，需要先查看当前的ClassLoader信息，提取对应ClassLoader的hashcode。

如果你使用`-c`，你需要手动输入hashcode：`-c <hashcode>`

`$ ognl -c 1be6f5c3 @com.example.demo.arthas.user.UserController@logger`

对于只有唯一实例的ClassLoader可以通过`--classLoaderClass`指定class name，使用起来更加方便：

```bash
$ ognl --classLoaderClass org.springframework.boot.loader.LaunchedURLClassLoader  @org.springframework.boot.SpringApplication@logger
@Slf4jLocationAwareLog[
    FQCN=@String[org.apache.commons.logging.LogAdapter$Slf4jLocationAwareLog],
    name=@String[org.springframework.boot.SpringApplication],
    logger=@Logger[Logger[org.springframework.boot.SpringApplication]],
]
```

`--classLoaderClass` 的值是ClassLoader的类名，只有匹配到唯一的ClassLoader实例时才能工作，目的是方便输入通用命令，而`-c <hashcode>`是动态变化的。

#### 6.3 获取静态类的静态字段

获取`UserController`类里的`logger`字段：

```
ognl --classLoaderClass org.springframework.boot.loader.LaunchedURLClassLoader @com.example.demo.arthas.user.UserController@logger
```

还可以通过`-x`参数控制返回值的展开层数。比如：

```
ognl --classLoaderClass org.springframework.boot.loader.LaunchedURLClassLoader -x 2 @com.example.demo.arthas.user.UserController@logger
```

#### 6.4 执行多行表达式，赋值给临时变量，返回一个List

```
ognl '#value1=@System@getProperty("java.home"), #value2=@System@getProperty("java.runtime.name"), {#value1, #value2}'
$ ognl '#value1=@System@getProperty("java.home"), #value2=@System@getProperty("java.runtime.name"), {#value1, #value2}'
@ArrayList[
    @String[/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/jre],
    @String[Java(TM) SE Runtime Environment],
]
```

#### 6.5 更多

在Arthas里`ognl`表达式是很重要的功能，在很多命令里都可以使用`ognl`表达式。

一些更复杂的用法，可以参考：

- OGNL特殊用法请参考：https://github.com/alibaba/arthas/issues/71
- OGNL表达式官方指南：https://commons.apache.org/proper/commons-ognl/language-guide.html



### 7. Watch查看命令

#### 7.1 如何使用

`watch com.example.demo.arthas.user.UserController * '{params, throwExp}'`

执行完之后，会阻塞，此时如果有请求进来，发生一场的话，就会看到异常信息。

![image-20210820171343673](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820171343673.png)

如果想把获取到的结果展开，可以用`-x`参数：

`watch com.example.demo.arthas.user.UserController * '{params, throwExp}' -x 2`

#### 7.2 返回值表达式

在上面的例子里，第三个参数是`返回值表达式`，它实际上是一个`ognl`表达式，它支持一些内置对象：

- loader
- clazz
- method
- target
- params
- returnObj
- throwExp
- isBefore
- isThrow
- isReturn

你可以利用这些内置对象来组成不同的表达式。比如返回一个数组：

```
watch com.example.demo.arthas.user.UserController * '{params[0], target, returnObj}'
```

更多参考： https://arthas.aliyun.com/doc/advice-class.html

#### 7.3 条件表达式

`watch`命令支持在第4个参数里写条件表达式，比如：

`watch com.example.demo.arthas.user.UserController * returnObj 'params[0] > 100'`

当访问 `localhost:80/user/1`时，`watch`命令没有输出

当访问`localhost:80/user/101`时，`watch`会打印出结果。

#### 7.4 当异常时捕获

`watch`命令支持`-e`选项，表示只捕获抛出异常时的请求：

`watch com.example.demo.arthas.user.UserController * "{params[0],throwExp}" -e`

#### 7.5 按照耗时进行过滤

watch命令支持按请求耗时进行过滤，比如：

`watch com.example.demo.arthas.user.UserController * '{params, returnObj}' '#cost>200'`



### 8. 热更新代码

下面介绍通过`jad`/`mc`/`redefine` 命令实现动态更新代码的功能。

目前，访问 http://localhost/user/0 ，会返回500异常：

```java
curl http://localhost/user/0
{"timestamp":1550223186170,"status":500,"error":"Internal Server Error","exception":"java.lang.IllegalArgumentException","message":"id < 1","path":"/user/0"}
```

下面通过热更新代码，修改这个逻辑。

#### 8.1 jad反编译UserController

**在arthas中执行jad命令 👇**

`jad --source-only com.example.demo.arthas.user.UserController > /tmp/UserController.java`

jad反编译的结果保存在 `/tmp/UserController.java`文件里了。

在【 机器 】上然后用vim来编辑`/tmp/UserController.java`：

`vim /tmp/UserController.java`

比如当 user id 小于1时，也正常返回，不抛出异常：

```java
    @GetMapping(value={"/user/{id}"})
    public User findUserById(@PathVariable Integer id) {
        logger.info("id: {}", (Object)id);
        if (id != null && id < 1) {
            return new User(id, "name" + id);
            // throw new IllegalArgumentException("id < 1");
        }
        return new User(id.intValue(), "name" + id);
    }
```

#### 8.2 sc查找加载UserController的ClassLoader

```
sc -d *UserController | grep classLoaderHash
$ sc -d *UserController | grep classLoaderHash
 classLoaderHash   1be6f5c3
```

可以发现是 `springbootLaunchedURLClassLoader@1be6f5c3` 加载的。

记下classLoaderHash，后面需要使用它。在这里，它是 `1be6f5c3`。

#### 8.3 mc编译java文件

编译java文件，类似于javac。

保存好`/tmp/UserController.java`之后，使用`mc`(Memory Compiler)命令来编译，并且通过`-c`或者`--classLoaderClass`参数指定ClassLoader：

```
mc --classLoaderClass org.springframework.boot.loader.LaunchedURLClassLoader /tmp/UserController.java -d /tmp
$ mc --classLoaderClass org.springframework.boot.loader.LaunchedURLClassLoader /tmp/UserController.java -d /tmp
Memory compiler output:
/tmp/com/example/demo/arthas/user/UserController.class
Affect(row-cnt:1) cost in 346 ms
```

也可以通过`mc -c <classLoaderHash> /tmp/UserController.java -d /tmp`，使用`-c`参数指定ClassLoaderHash:

```bash
$ mc -c 1be6f5c3 /tmp/UserController.java -d /tmp
```

![image-20210820172201737](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820172201737.png)

#### 8.4 redefine加载class文件

再使用`redefine`命令重新加载新编译好的`UserController.class`：

```
redefine /tmp/com/example/demo/arthas/user/UserController.class
$ redefine /tmp/com/example/demo/arthas/user/UserController.class
redefine success, size: 1
```

#### 8.5 热修改代码结果

`redefine`成功之后，再次访问 `localhost:80/user/0` ，结果是：

```
{
  "id": 0,
  "name": "name0"
}
```



### 9. Exit/Stop

#### 9.1 reset

Arthas在 watch/trace 等命令时，实际上是修改了应用的字节码，插入增强的代码。显式执行 `reset` 命令，可以清除掉这些增强代码。

#### 9.2 退出Arthas

用 `exit` 或者 `quit` 命令可以退出Arthas。

退出Arthas之后，还可以再次用 `java -jar arthas-boot.jar` 来连接。

#### 9.3 彻底退出Arthas

`exit/quit`命令只是退出当前session，arthas server还在目标进程中运行。

想完全退出Arthas，可以执行 `stop` 命令。



### 10. arthas-boot支持的参数

`arthas-boot.jar` 支持很多参数，可以执行 `java -jar arthas-boot.jar -h` 来查看。

#### 10.1 允许外部访问

默认情况下， arthas server侦听的是 `127.0.0.1` 这个IP，如果希望远程可以访问，可以使用`--target-ip`的参数。

`java -jar arthas-boot.jar --target-ip`

#### 10.2 列出所有的版本

`java -jar arthas-boot.jar --versions`

使用指定版本：

`java -jar arthas-boot.jar --use-version 3.1.0`

#### 10.3 只侦听Telnet端口，不侦听HTTP端口

`java -jar arthas-boot.jar --telnet-port 9999 --http-port -1`

#### 10.4 打印运行的详情

`java -jar arthas-boot.jar -v`

![image-20210820164916567](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820164916567.png)



### 11. Web Console

![image-20210820173816512](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210820173816512.png)

