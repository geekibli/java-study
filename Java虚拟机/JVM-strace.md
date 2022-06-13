---
title: JVM-strace
toc: true
date: 2021-07-28 17:52:23
tags: JVM
categories: [Develop Lan,Java,JVM]
---



# strace 命令查看操作系统日志



`strace -ff -o out java ***.class`

-ff : 跟踪进程下所有线程用到的系统命令
-o : 将跟踪的操作系统日志输出

> 下面查看JDK1.8下，BIO模式都有哪些系统命令的执行
```java
package com.ibli.javaBase.io.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author gaolei
 * @Date 2021/4/3 2:55 下午
 * @Version 1.0
 */
public class SockerIo {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(9090);
        // 阻塞
        Socket client = serverSocket.accept();

        InputStream inputStream = client.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        // 读阻塞
        System.err.println(bufferedReader.readLine());

        while (true){

        }
    }
}
```

> 服务端

1、`javac SockerIo.java` 得到SockerIo.class
然后，使用strace启动java程序👇：
2、`strace -ff -0 out java SockerIo`
得到如下日志：
<img src="https://oscimg.oschina.net/oscnet/up-924f13a6df8c2ce97e13019329008fb4a84.png">


> 客户端使用nc连接9090端口，然后请求数据

`nc 127.0.0.1 9090`  发送如下数据

<img src="https://oscimg.oschina.net/oscnet/up-4b7b364f021786bd9bdf4f40135da3b4a8d.png">

> strace查看日志

查看主线程日志：
<img src="https://oscimg.oschina.net/oscnet/up-291f9a1018a2d288b88a5c2e43666d5d47e.png"> 
如上图，👆文件最大的是主线程日志：


<img src="https://oscimg.oschina.net/oscnet/up-c306b9f94e3d0a3582b049d3c4769b4f5ec.png">

根据上面👆strace命令跟踪的日志可以看到，JDK1.8下的BIO的多路复用器是使用的「poll」






