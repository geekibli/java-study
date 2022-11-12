# 使用系统命令查看上下文切换

vmstat 是一款指定采样周期和次数的功能性监测工具，我们可以使用它监控进程上下文切换的情况。

![](https://oscimg.oschina.net/oscnet/up-4dc013bd95a1b7b323c98f88822841e5d96.png)

> vmstat 1 3 命令行代表每秒收集一次性能指标，总共获取 3 次

- procs
  - r：等待运行的进程数
  - b：处于非中断睡眠状态的进程数
- memory
  - swpd：虚拟内存使用情况
  - free：空闲的内存
  - buff：用来作为缓冲的内存数
  - cache：缓存大小
- swap
  - si：从磁盘交换到内存的交换页数量
  - so：从内存交换到磁盘的交换页数量
- io
  - bi：发送到快设备的块数
  - bo：从块设备接收到的块数
- system
  - in：每秒中断数
  - cs：每秒上下文切换次数
- cpu
  - us：用户 CPU 使用事件
  - sy：内核 CPU 系统使用时间
  - id：空闲时间
  - wa：等待 I/O 时间
  - st：运行虚拟机窃取的时间