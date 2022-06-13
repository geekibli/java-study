---
title: Docker安装MySQL
toc: true
date: 2021-08-27 14:16:47
tags: 
- docker
- mysql
categories:
---

## linux安装docker
`yum update`
`yum install docker`

结果启动docker时报错了 👇
```
Emulate Docker CLI using podman. Create /etc/containers/nodocker to quiet msg.
Error: open /proc/self/uid_map: no such file or directory
```

**解决办法**
1，卸载podman软件（可以使用rpm -qa|grep docker）
`yum remove docker`
2,下载docker-ce源
`curl https://download.docker.com/linux/centos/docker-ce.repo -o /etc/yum.repos.d/docker-ce.repo`
3，安装docker-ce
`yum install docker-ce -y`

**问题原因分析：**
Centos 8使用yum install docker -y时，默认安装的是podman-docker软件

### 查看docker状态
<img src = 'https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/20210827142038.png'>

### 启动docker
`systemctl start docker`
<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/20210827142309.png">

## 安装MySQl
`docker search mysql`

### **选择你要安装的版本**
`docker pull centos/mysql-57-centos7`

### **查看安装的镜像**
`docker images`
<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/20210827142710.png">

### **启动mysql**
`docker run --name mysqlserver -v /etc/mysql/my.conf:/etc/my.conf -e MYSQL_ROOT_PASSWORD=123456 -d -i -p 3306:3306 centos/mysql-57-centos7`
<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/20210827143045.png">

