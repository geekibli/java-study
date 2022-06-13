---
title: docker整体架构图
toc: true
date: 2021-07-28 16:20:03
tags: docker
categories: [Develop Tools , docker]
---
# Docker的总架构图 
<img src='https://cdn.zsite.com/data/upload/d/docker/202104/f_e2031050ce83ec39ca6574875e047811.png' width=500 height=600>

docker是一个C/S模式的架构，后端是一个松耦合架构，模块各司其职。

1、用户是使用Docker Client与Docker Daemon建立通信，并发送请求给后者。
2、Docker Daemon作为Docker架构中的主体部分，首先提供Server的功能使其可以接受Docker Client的请求；
3、Engine执行Docker内部的一系列工作，每一项工作都是以一个Job的形式的存在。
4、Job的运行过程中，当需要容器镜像时，则从Docker Registry中下载镜像，并通过镜像管理驱动graphdriver将下载镜像以Graph的形式存储；
5、当需要为Docker创建网络环境时，通过网络管理驱动networkdriver创建并配置Docker容器网络环境；
6、当需要限制Docker容器运行资源或执行用户指令等操作时，则通过execdriver来完成。
7、libcontainer是一项独立的容器管理包，networkdriver以及execdriver都是通过libcontainer来实现具体对容器进行的操作。
