---
title: Linux常用命令
toc: true
date: 2021-07-13 11:44:22
tags: linux
categories: [Linux System, Common commands]
---

## 用户｜权限

`chmod`：修改权限命令。一般用`+`号添加权限，`-`号删除权限，`x`代表执行权限，`r`代表读取权限，`w`代表写入权限，常见写法比如`chmod +x 文件名` 添加执行权限。

还有另外一种写法，使用数字来授权，因为`r`=4，`w`=2，`x`=1，平时执行命令`chmod 777 文件名`这就是最高权限了。

第一个数字7=4+2+1代表着所有者的权限，第二个数字7代表所属组的权限，第三个数字代表其他人的权限。

常见的权限数字还有644，所有者有读写权限，其他人只有只读权限，755代表其他人有只读和执行权限。

`chown`：用于修改文件和目录的所有者和所属组。一般用法`chown user 文件`用于修改文件所有者，`chown user:user 文件`修改文件所有者和组，冒号前面是所有者，后面是组。




## 文件相关

`touch`：用于创建文件。如果文件不存在，则创建一个新的文件，如果文件已存在，则会修改文件的时间戳。

`cat`：cat是英文`concatenate`的缩写，用于查看文件内容。使用`cat`查看文件的话，不管文件的内容有多少，都会一次性显示，所以他不适合查看太大的文件。

`more`：more和cat有点区别，more用于分屏显示文件内容。可以用`空格键`向下翻页，`b`键向上翻页

`less`：和more类似，less用于分行显示

`tail`：可能是平时用的最多的命令了，查看日志文件基本靠他了。一般用户`tail -fn 100 xx.log` 查看最后的100行内容

`gzip`：用于压缩.gz后缀文件，gzip命令不能打包目录。需要注意的是直接使用`gzip 文件名`源文件会消失，如果要保留源文件，可以使用 `gzip -c 文件名 > xx.gz`，解压缩直接使用`gzip -d xx.gz`

`tar`：tar常用几个选项，`-x`解打包，`-c`打包，`-f`指定压缩包文件名，`-v`显示打包文件过程，一般常用`tar -cvf xx.tar 文件`来打包，解压则使用`tar -xvf xx.tar`。

Linux的打包和压缩是分开的操作，如果要打包并且压缩的话，按照前面的做法必须先用tar打包，然后再用gzip压缩。当然，还有更好的做法就是`-z`命令，打包并且压缩。

使用命令`tar -zcvf xx.tar.gz 文件`来打包压缩，使用命令`tar -zxvf xx.tar.gz`来解压缩

## 日志相关
- linux 在文档中查找关键字个数
  `grep -o “关键字” 文档名 | wc -l `
  `grep -o “关键字” 文档名 | sort | uniq -c`

- 清除history记录
  `vim .bash_history`
  命令模式下（Esc之后输入:） 输入 set nu 每行数据前面显示行号
  `11,20d` 回车 11～20行的记录就被删除了
  然后命令模式下 wq 保存退出就可以了
  如果在此查看还是有记录，可以退出当前回话之后，再进去查看，就会不再显示删除的记录了



## 工具相关


### 查看yum下载的软件位置
rpm -qa | grep docker
rpm -ql podman-docker-3.2.3-0.10.module_el8.4.0+886+c9a8d9ad.noarch







## 问题排查

### 那如果CPU使用率达到100%呢？怎么排查？

1、通过`top`找到占用率高的进程。

2、通过`top -Hp pid`找到占用CPU高的线程ID。这里找到958的线程ID

3、再把线程ID转化为16进制，`printf "0x%x\n" 958`，得到线程ID`0x3be`

4、通过命令`jstack 163 | grep '0x3be' -C5 --color` 或者 `jstack 163|vim +/0x3be -` 找到有问题的代码













https://wizardforcel.gitbooks.io/vbird-linux-basic-4e/content/1.html

https://www.linuxidc.com/index.htm
