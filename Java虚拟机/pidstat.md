# Linux 命令行工具之 pidstat 命令


pidstat 命令就可以帮助我们监测到具体线程的上下文切换。pidstat 是 Sysstat 中一个组件，也是一款功能强大的性能监测工具。我们可以通过命令 yum install sysstat 安装该监控组件。

通过 pidstat -help 命令，我们可以查看到有以下几个常用参数可以监测线程的性能：

```
用法: pidstat [ 选项 ] [ <时间间隔> [ <次数> ] ]
Options are:
[ -d ] [ -h ] [ -I ] [ -l ] [ -r ] [ -s ] [ -t ] [ -U [ <username> ] ] [ -u ]
[ -V ] [ -w ] [ -C <command> ] [ -p { <pid> [,...] | SELF | ALL } ]
[ -T { TASK | CHILD | ALL } ]
```


**常用参数：**

- u：默认参数，显示各个进程的 cpu 使用情况；
- r：显示各个进程的内存使用情况；
- d：显示各个进程的 I/O 使用情况；
- w：显示每个进程的上下文切换情况；
- p：指定进程号；
- t：显示进程中线程的统计信息

首先，通过 pidstat -w -p pid 命令行，我们可以查看到进程的上下文切换：

```
Linux 3.10.0-1160.25.1.el7.x86_64 (iZ2ze54kq062ml51c2doloZ) 	2022年08月25日 	_x86_64_	(4 CPU)

15时53分53秒   UID       PID   cswch/s nvcswch/s  Command
15时53分53秒     0     16166      0.00      0.00  java
```

- **cswch/s**：每秒主动任务上下文切换数量
- **nvcswch/s**：每秒被动任务上下文切换数量

之后，通过 pidstat -w -p pid -t 命令行，我们可以查看到具体线程的上下文切换：

```
Linux 3.10.0-1160.25.1.el7.x86_64 (iZ2ze54kq062ml51c2doloZ) 	2022年08月25日 	_x86_64_	(4 CPU)

15时55分06秒   UID      TGID       TID   cswch/s nvcswch/s  Command
15时55分06秒     0     16166         -      0.00      0.00  java
15时55分06秒     0         -     16166      0.00      0.00  |__java
15时55分06秒     0         -     16167      0.00      0.00  |__java
15时55分06秒     0         -     16168      0.00      0.00  |__java
15时55分06秒     0         -     16169      0.00      0.00  |__java
15时55分06秒     0         -     16170      0.00      0.00  |__java
15时55分06秒     0         -     16171      0.00      0.00  |__java
15时55分06秒     0         -     16172      0.00      0.00  |__VM Thread
15时55分06秒     0         -     16173      0.00      0.00  |__Reference Handl
15时55分06秒     0         -     16174      0.00      0.00  |__Finalizer
15时55分06秒     0         -     16175      0.00      0.00  |__Signal Dispatch
15时55分06秒     0         -     16176      0.00      0.00  |__C2 CompilerThre
15时55分06秒     0         -     16177      0.00      0.00  |__C2 CompilerThre
15时55分06秒     0         -     16178      0.00      0.00  |__C1 CompilerThre
15时55分06秒     0         -     16179      0.00      0.00  |__Service Thread
15时55分06秒     0         -     16180      0.01      0.00  |__VM Periodic Tas
15时55分06秒     0         -     16189      0.00      0.00  |__logback-appende
15时55分06秒     0         -     16760      0.00      0.00  |__pool-1-thread-1
15时55分06秒     0         -     16761      0.00      0.00  |__I/O dispatcher 
15时55分06秒     0         -     16762      0.00      0.00  |__I/O dispatcher 
15时55分06秒     0         -     16763      0.00      0.00  |__I/O dispatcher 
15时55分06秒     0         -     16764      0.00      0.00  |__I/O dispatcher 
15时55分06秒     0         -     16765      0.01      0.00  |__Abandoned conne
15时55分06秒     0         -     16766      0.00      0.00  |__lettuce-eventEx
15时55分06秒     0         -     16767      0.00      0.00  |__lettuce-eventEx
15时55分06秒     0         -     16768      0.00      0.00  |__Catalina-utilit
15时55分06秒     0         -     16769      0.00      0.00  |__Catalina-utilit
```