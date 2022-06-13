---
title: Java-传统的BIO
toc: true
date: 2021-07-28 17:50:21
tags:
- Java
- IO
categories: [Develop Lan,Java,IO]
---




# 传统的BIO

## Socket 和 ServerSocket

```java
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

```

> new ServerSocket(9090);

这个java程序创建new ServerSocket(9090);会调用操作系统内核，也就是系统调用，
比如linux操作系统，应用进程也就是我们的java进程，会调用linux的内核方法，创建一个socket，在linux系统中就是一个文件描述符fd，最终对得到：
```language
socket() = XXfd
bind(XXfd,9090)
listen(XXfd)
```

![image.png](https://sjwx.easydoc.xyz/47754217/files/kn1ecf3d.png)




- socket 的read方法 ，读取客户端发送的数据，如果没有，则一直阻塞
- serverSocket的accept方法，等待客户端的链接，如果没有链接，则一直阻塞等待
- serverSocket 一次只能处理一个客户端请求

## BIO程序有哪些弊端？

- 服务端一次处理一个请求，并发非常低
- 没有客户端请求，服务端一直阻塞，占用资源


## 如果在bio的基础上，利用多线程处理客户端请求？
>d C10K问题 

<font color=red>来一个链接，服务端创建一个线程</font> ，去处理请求，服务端继续监听客户端，是不是可以增加并发？
有什么问题？

- 线程消耗内存资源

如果一下子过来10万个请求呢？  
服务器要创建10万个线程，内存就崩了。

如果搞一个线程池呢？ 并发度最大为最大线程数？ 并发度已经定死了？
