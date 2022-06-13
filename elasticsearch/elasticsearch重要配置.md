---
title: elasticsearch重要配置
toc: true
date: 2021-07-05 21:22:32
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---


虽然Elasticsearch仅需要很少的配置，但有许多设置需要手动配置，并且在进入生产之前绝对必须进行配置。

`path.data` 和 `path.logs`  
`cluster.name`  
`node.name`  
`bootstrap.memory_lock`  
`network.host`  
`discovery.zen.ping.unicast.hosts`  
`discovery.zen.minimum_master_nodes`   
`path.data` 和 `path.logs`  
如果使用.zip或.tar.gz归档，则数据和日志目录是$ES_HOME的子文件夹。 如果这些重要的文件夹保留在其默认位置，则存在将Elasticsearch升级到新版本时被删除的高风险。  

在生产使用中，肯定得更改数据和日志文件夹的位置：
```yaml
path:
  logs: /var/log/elasticsearch
  data: /var/data/elasticsearch
```
RPM和Debian发行版已经使用数据和日志的自定义路径。  

`path.data` 设置可以设置为多个路径，在这种情况下，所有路径将用于存储数据（属于单个分片的文件将全部存储在同一数据路径上）：
```yaml
path:
  data:
    - /mnt/elasticsearch_1
    - /mnt/elasticsearch_2
    - /mnt/elasticsearch_3
```
## cluster.name
节点只能在群集与群集中的所有其他节点共享其cluster.name时才能加入群集。 默认名称为elasticsearch，但您应将其更改为描述集群用途的适当名称。  
`cluster.name: logging-prod`  
确保不要在不同的环境中重复使用相同的集群名称，否则可能会导致加入错误集群的节点。  

## node.name
默认情况下，Elasticsearch将使用随机生成的uuid的第一个字符作为节点id。 请注意，节点ID是持久化的，并且在节点重新启动时不会更改，因此默认节点名称也不会更改。  
配置一个更有意义的名称是值得的，这是重启节点后也能一直保持的优势：  
`node.name: prod-data-2`  
node.name也可以设置为服务器的HOSTNAME，如下所示：  
```yaml
node.name: ${HOSTNAME}
bootstrap.memory_lock
```
没有JVM被交换到磁盘上这事对于节点的健康来说是至关重要的。一种实现方法是将bootstrap.memory_lock设置为true。  
要使此设置生效，需要首先配置其他系统设置。 有关如何正确设置内存锁定的更多详细信息，请参阅启用[bootstrap.memory_lock](https://www.elastic.co/guide/en/elasticsearch/reference/current/setup-configuration-memory.html#mlockall)。  

## network.host
默认情况下，Elasticsearch仅仅绑定在本地回路地址——如：127.0.0.1与[::1]。这在一台服务器上跑一个开发节点是足够的。
提示

事实上，多个节点可以在单个节点上相同的$ES_HOME位置一同运行。这可以用于测试Elasticsearch形成集群的能力,但这种配置方式不推荐用于生产环境。

为了将其它服务器上的节点形成一个可以相互通讯的集群，你的节点将不能绑定在一个回路地址上。 这里有更多的网路配置，通常你只需要配置network.host：  
`network.host: 192.168.1.10`
network.host也可以配置成一些能识别的特殊的值，譬如：_local_、_site、_global_，它们可以结合指定:ip4与ip6来使用。更多相信信息请参见：[网路配置](https://aqlu.gitbooks.io/elasticsearch-reference/content/Modules/Network_Settings.html)

重要 👇

一旦你自定义了network.host的配置，Elasticsearch将假设你已经从开发模式转到了生产模式，并将升级系统检测的警告信息为异常信息。更多信息请参见：开发模式vs生产模式

## discovery.zen.ping.unicast.hosts（单播发现）
开箱即用，无需任何网络配置，Elasticsearch将绑定到可用的回路地址，并扫描9300年到9305的端口去连接同一机器上的其他节点,试图连接到相同的服务器上运行的其他节点。它提供了不需要任何配置就能自动组建集群的体验。  
当与其它机器上的节点要形成一个集群时，你需要提供一个在线且可访问的节点列表。像如下来配置：  
```
discovery.zen.ping.unicast.hosts:
   - 192.168.1.10:9300
   - 192.168.1.11 #①
   - seeds.mydomain.com #②
```
① 未指定端口时，将使用默认的`transport.profiles.default.port`值，如果此值也为设置则使用`transport.tcp.port`

② 主机名将被尝试解析成能解析的多个IP

## discovery.zen.minimum_master_nodes
为防止数据丢失，配置discovery-zen-minimum_master_nodes将非常重要，他规定了必须至少要有多少个master节点才能形成一个集群。  
没有此设置时，一个集群在发生网络问题是可能会分裂成多个集群——脑裂——这将导致数据丢失。更多详细信息请参见：通过minimum_master_nodes避免脑裂
为避免脑裂，你需要根据master节点数来设置法定人数：
`(master_eligible_nodes / 2) + 1`
换句话说，如果你有三个master节点，最小的主节点数因被设置为(3/2)+1或者是2
`discovery.zen.minimum_master_nodes: 2`


## 参考资料
> - [elastic 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/setup-configuration-memory.html#mlockall)
> - [codingdict.com](https://www.codingdict.com/document)
