<p>高可用是通过设计，减少系统不能提供服务的时间，是分布式系统的基础也是保障系统可靠性的重要手段。而 Redis 作为一款普及率最高的内存型中间件，它的高可用技术也非常的成熟。</p>
<p>我们本课时的面试题是，Redis 是如何保证系统高可用的？它的实现方式有哪些？</p>
<h3>典型回答</h3>
<p>Redis 高可用的手段主要有以下四种：</p>
<ul>
<li>数据持久化</li>
<li>主从数据同步（主从复制）</li>
<li>Redis 哨兵模式（Sentinel）</li>
<li>Redis 集群（Cluster）</li>
</ul>
<p>其中数据持久化保证了系统在发生宕机或者重启之后数据不会丢失，增加了系统的可靠性和减少了系统不可用的时间（省去了手动恢复数据的过程）；而主从数据同步可以将数据存储至多台服务器，这样当遇到一台服务器宕机之后，可以很快地切换至另一台服务器以继续提供服务；哨兵模式用于发生故障之后自动切换服务器；而 Redis 集群提供了多主多从的 Redis 分布式集群环境，用于提供性能更好的 Redis 服务，并且它自身拥有故障自动切换的能力。</p>
<h3>考点分析</h3>
<p>高可用的问题属于 Redis 中比较大的面试题了，因为很多知识点都和这个面试题有关，同时也属于比较难的面试题了。因为涉及了分布式集群，而分布式集群属于 Redis 中比较难懂的一个知识点。和此问题相关的面试题还有以下几个：</p>
<ul>
<li>数据持久化有几种方式？</li>
<li>Redis 主从同步有几种模式？</li>
<li>什么是 Redis 哨兵模式？它解决了什么问题？</li>
<li>Redis 集群的优势是什么？</li>
</ul>
<h3>知识扩展</h3>
<h4>1.数据持久化</h4>
<p>持久化功能是 Redis 和 Memcached 的主要区别之一，因为只有 Redis 提供了此功能。</p>
<p>在 Redis 4.0 之前数据持久化方式有两种：AOF 方式和 RDB 方式。</p>
<ul>
<li>RDB（Redis DataBase，快照方式）是将某一个时刻的内存数据，以二进制的方式写入磁盘。</li>
<li>AOF（Append Only File，文件追加方式）是指将所有的操作命令，以文本的形式追加到文件中。</li>
</ul>
<p>RDB 默认的保存文件为 dump.rdb，优点是以二进制存储的，因此占用的空间更小、数据存储更紧凑，并且与 AOF 相比，RDB 具备更快的重启恢复能力。</p>
<p>AOF 默认的保存文件为 appendonly.aof，它的优点是存储频率更高，因此丢失数据的风险就越低，并且 AOF 并不是以二进制存储的，所以它的存储信息更易懂。缺点是占用空间大，重启之后的数据恢复速度比较慢。</p>
<p>可以看出 RDB 和 AOF 各有利弊，RDB 具备更快速的数据重启恢复能力，并且占用更小的磁盘空间，但有数据丢失的风险；而 AOF 文件的可读性更高，但却占用了更大的空间，且重启之后的恢复速度更慢，于是在 Redis 4.0 就推出了混合持久化的功能。</p>
<p>混合持久化的功能指的是 Redis 可以使用 RDB + AOF 两种格式来进行数据持久化，这样就可以做到扬长避短物尽其用了，混合持久化的存储示意图如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0F/40/CgqCHl7HRL-ARaj7AABVIFnJgfE685.png" alt="image (1).png"></p>
<p>我们可以使用“config get aof-use-rdb-preamble”的命令来查询 Redis 混合持久化的功能是否开启，执行示例如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-number">127.0</span><span class="hljs-number">.0</span><span class="hljs-number">.1</span>:<span class="hljs-number">6379</span>&gt; config get aof-use-rdb-preamble
<span class="hljs-number">1</span>) <span class="hljs-string">"aof-use-rdb-preamble"</span>
<span class="hljs-number">2</span>) <span class="hljs-string">"yes"</span>
</code></pre>
<p>如果执行结果为“no”则表示混合持久化功能关闭，不过我们可以使用“config set aof-use-rdb-preamble yes”的命令打开此功能。<br>
Redis 混合持久化的存储模式是，开始的数据以 RDB 的格式进行存储，因此只会占用少量的空间，并且之后的命令会以 AOF 的方式进行数据追加，这样就可以减低数据丢失的风险，同时可以提高数据恢复的速度。</p>
<h4>2.Redis 主从同步</h4>
<p>主从同步是 Redis 多机运行中最基础的功能，它是把多个 Redis 节点组成一个 Redis 集群，在这个集群当中有一个主节点用来进行数据的操作，其他从节点用于同步主节点的内容，并且提供给客户端进行数据查询。</p>
<p>Redis 主从同步分为：主从模式和从从模式。<strong>主从模式</strong>就是一个主节点和多个一级从节点，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0F/40/CgqCHl7HRNaAUEFMAADdgcS-e7A625.png" alt="image (3).png"></p>
<p>而<strong>从从模式</strong>是指一级从节点下面还可以拥有更多的从节点，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0F/40/CgqCHl7HRN-APxPIAAFbO6pdGEk455.png" alt="image (4).png"></p>
<p>主从模式可以提高 Redis 的整体运行速度，因为使用主从模式就可以实现数据的读写分离，把写操作的请求分发到主节点上，把其他的读操作请求分发到从节点上，这样就减轻了 Redis 主节点的运行压力，并且提高了 Redis 的整体运行速度。</p>
<p>不但如此使用主从模式还实现了 Redis 的高可用，当主服务器宕机之后，可以很迅速的把从节点提升为主节点，为 Redis 服务器的宕机恢复节省了宝贵的时间。</p>
<p>并且主从复制还降低了数据丢失的风险，因为数据是完整拷贝在多台服务器上的，当一个服务器磁盘坏掉之后，可以从其他服务器拿到完整的备份数据。</p>
<h4>3.Redis 哨兵模式</h4>
<p>Redis 主从复制模式有那么多的优点，但是有一个致命的缺点，就是当 Redis 的主节点宕机之后，必须人工介入手动恢复，那么到特殊时间段，比如公司组织全体团建或者半夜突然发生主节点宕机的问题，此时如果等待人工去处理就会很慢，这个时间是我们不允许的，并且我们还需要招聘专职的人来负责数据恢复的事，同时招聘的人还需要懂得相关的技术才能胜任这份工作。既然如此的麻烦，那有没有简单一点的解决方案，这个时候我们就需要用到 Redis 的哨兵模式了。</p>
<p>Redis 哨兵模式就是用来监视 Redis 主从服务器的，当 Redis 的主从服务器发生故障之后，Redis 哨兵提供了自动容灾修复的功能，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0F/40/CgqCHl7HRPGAOotiAAEnlC_LOmI256.png" alt="image (5).png"></p>
<p>Redis 哨兵模块存储在 Redis 的 src/redis-sentinel 目录下，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0F/35/Ciqc1F7HRPiAT6ITAAEMYbbe7uE121.png" alt="image (6).png"></p>
<p>我们可以使用命令“./src/redis-sentinel sentinel.conf”来启动哨兵功能。</p>
<p>有了哨兵功能之后，就再也不怕 Redis 主从服务器宕机了。哨兵的工作原理是每个哨兵会以每秒钟 1 次的频率，向已知的主服务器和从服务器，发送一个 PING 命令。如果最后一次有效回复 PING 命令的时间，超过了配置的最大下线时间（Down-After-Milliseconds）时，默认是 30s，那么这个实例会被哨兵标记为主观下线。</p>
<p>如果一个主服务器被标记为主观下线，那么正在监视这个主服务器的所有哨兵节点，要以每秒 1 次的频率确认主服务器是否进入了主观下线的状态。如果有足够数量（quorum 配置值）的哨兵证实该主服务器为主观下线，那么这个主服务器被标记为客观下线。此时所有的哨兵会按照规则（协商）自动选出新的主节点服务器，并自动完成主服务器的自动切换功能，而整个过程都是无须人工干预的。</p>
<h4>4.Redis 集群</h4>
<p>Redis 集群也就是 Redis Cluster，它是 Redis 3.0 版本推出的 Redis 集群方案，将数据分布在不同的主服务器上，以此来降低系统对单主节点的依赖，并且可以大大提高 Redis 服务的读写性能。Redis 集群除了拥有主从模式 + 哨兵模式的所有功能之外，还提供了多个主从节点的集群功能，实现了真正意义上的分布式集群服务，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0F/40/CgqCHl7HRQiASHI6AAEIfzYWTpo237.png" alt="image (7).png"></p>
<p>Redis 集群可以实现数据分片服务，也就是说在 Redis 集群中有 16384 个槽位用来存储所有的数据，当我们有 N 个主节点时，可以把 16384 个槽位平均分配到 N 台主服务器上。当有键值存储时，Redis 会使用 crc16 算法进行 hash 得到一个整数值，然后用这个整数值对 16384 进行取模来得到具体槽位，再把此键值存储在对应的服务器上，读取操作也是同样的道理，这样我们就实现了数据分片的功能。</p>
<h3>小结</h3>
<p>本课时我们讲了保障 Redis 高可用的 4 种手段：数据持久化保证了数据不丢失；Redis 主从让 Redis 从单机变成了多机。它有两种模式：主从模式和从从模式，但当主节点出现问题时，需要人工手动恢复系统；Redis 哨兵模式用来监控 Redis 主从模式，并提供了自动容灾恢复的功能。最后是 Redis 集群，除了可以提供主从和哨兵的功能之外，还提供了多个主从节点的集群功能，这样就可以把数据均匀的存储各个主机主节点上，实现了系统的横向扩展，大大提高了 Redis 的并发处理能力。</p>

---

### 精选评论

##### *镇：
> 很有收货

##### **婷：
> <div>1.开始的数据以 RDB 的格式进行存储，并且之后的命令会以 AOF 的方式进行数据追加。啥是开始的数据，有没有一个具体的时间节点？</div>2.每个redis集群都有16384个槽位吗？为啥这么设置，槽位长啥样

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这分为几种情况，最常见的就是使用 save 和 bgsave 时会生成 rdb 文件，其他操作时会以 aof 方式存储，可以使用 info Persistence 命令查看 rdb 最后一次生成时间。关于槽位的问题，可以看 redis 作者的回复 https://github.com/antirez/redis/issues/2576

##### *明：
> 数据持久化应该是为了数据可靠性的吧

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是的~

##### **江：
> 老师数据持久化也算高可用吗？每次回答都回答了集群，主从和哨兵，总感觉面试官还是想问持久化

