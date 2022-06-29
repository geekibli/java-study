<p data-nodeid="1489" class="">在上一课时，我们以 Guava 的 LoadingCache 为例，介绍了堆内缓存的特点以及一些注意事项。同时，还了解了缓存使用的场景，这对分布式缓存来说，同样适用。</p>
<p data-nodeid="1490">那什么叫<strong data-nodeid="1630">分布式缓存</strong>呢？它其实是一种<strong data-nodeid="1631">集中管理</strong>的思想。如果我们的服务有多个节点，堆内缓存在每个节点上都会有一份；而分布式缓存，所有的节点，共用一份缓存，既节约了空间，又减少了管理成本。</p>
<p data-nodeid="1491">在分布式缓存领域，使用最多的就是 Redis。<strong data-nodeid="1637">Redis</strong> 支持非常丰富的数据类型，包括字符串（string）、列表（list）、集合（set）、有序集合（zset）、哈希表（hash）等常用的数据结构。当然，它也支持一些其他的比如位图（bitmap）一类的数据结构。</p>
<p data-nodeid="1492">说到 Redis，就不得不提一下另外一个分布式缓存 <strong data-nodeid="1647">Memcached</strong>（以下简称 MC）。MC 现在已经很少用了，但<strong data-nodeid="1648">面试的时候经常会问到它们之间的区别</strong>，这里简单罗列一下：</p>
<p data-nodeid="1493"><img src="https://s0.lgstatic.com/i/image/M00/3D/AF/CgqCHl8qaxiATTH1AAB10CrXXk8295.png" alt="Drawing 0.png" data-nodeid="1651"></p>
<p data-nodeid="1494">Redis 在互联网中，几乎是标配。我们接下来，先简单看一下 Redis 在 Spring 中是如何使用的，然后，再介绍一下在秒杀业务中，Redis是如何帮助我们承接瞬时流量的。</p>
<h3 data-nodeid="1495">SpringBoot 如何使用 Redis</h3>
<p data-nodeid="1496">使用 SpringBoot 可以很容易地对 Redis 进行操作（<a href="https://gitee.com/xjjdog/tuning-lagou-res/tree/master/tuning-008/cache-redis" data-nodeid="1657">完整代码见仓库</a>）。Java 的 Redis的客户端，常用的有三个：jedis、redisson 和 lettuce，Spring 默认使用的是 lettuce。</p>
<p data-nodeid="1497"><strong data-nodeid="1667">lettuce</strong> 是使用 netty 开发的，操作是异步的，性能比常用的 jedis 要高；<strong data-nodeid="1668">redisson</strong> 也是异步的，但它对常用的业务操作进行了封装，适合书写有业务含义的代码。</p>
<p data-nodeid="1498">通过加入下面的 jar 包即可方便地使用 Redis。</p>
<pre class="lang-xml" data-nodeid="1499"><code data-language="xml"><span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span> 
 &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.springframework.boot<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span> 
 &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>spring-boot-starter-data-redis<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span> 
<span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span>
</code></pre>
<p data-nodeid="1500">上面这种方式，我们主要是使用 RedisTemplate 这个类。它针对不同的数据类型，抽象了相应的方法组。</p>
<p data-nodeid="1501"><img src="https://s0.lgstatic.com/i/image/M00/3D/AF/CgqCHl8qa0CAF6eCAAHpRwXu93w738.png" alt="Drawing 1.png" data-nodeid="1673"></p>
<p data-nodeid="1502">另外一种方式，就是使用 Spring 抽象的缓存包 spring-cache。它使用注解，采用 AOP的方式，对 Cache 层进行了抽象，可以在各种堆内缓存框架和分布式框架之间进行切换。这是它的 maven 坐标：</p>
<pre class="lang-xml" data-nodeid="1503"><code data-language="xml"><span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span> 
 &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.springframework.boot<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span> 
 &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>spring-boot-starter-cache<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span> 
<span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span>
</code></pre>
<blockquote data-nodeid="1504">
<p data-nodeid="1505">与 spring-cache 类似的，还有阿里的 jetcache，都是比较好用的。</p>
</blockquote>
<p data-nodeid="1506">使用 spring-cache 有三个步骤：</p>
<ul data-nodeid="1507">
<li data-nodeid="1508">
<p data-nodeid="1509">在启动类上加入 @EnableCaching 注解；</p>
</li>
<li data-nodeid="1510">
<p data-nodeid="1511">使用 CacheManager 初始化要使用的缓存框架，使用 @CacheConfig 注解注入要使用的资源；</p>
</li>
<li data-nodeid="1512">
<p data-nodeid="1513">使用 @Cacheable 等注解对资源进行缓存。</p>
</li>
</ul>
<p data-nodeid="1514">我们这里使用的是 RedisCacheManager，由于现在只有这一个初始化实例，第二个步骤是可以省略的。</p>
<p data-nodeid="1515">针对缓存操作的注解，有三个：</p>
<ul data-nodeid="1516">
<li data-nodeid="1517">
<p data-nodeid="1518"><strong data-nodeid="1686">@Cacheable</strong> 表示如果缓存系统里没有这个数值，就将方法的返回值缓存起来；</p>
</li>
<li data-nodeid="1519">
<p data-nodeid="1520"><strong data-nodeid="1691">@CachePut</strong> 表示每次执行该方法，都把返回值缓存起来；</p>
</li>
<li data-nodeid="1521">
<p data-nodeid="1522"><strong data-nodeid="1696">@CacheEvict</strong> 表示执行方法的时候，清除某些缓存值。</p>
</li>
</ul>
<p data-nodeid="1523">对于秒杀系统来说，仅仅使用这三个注解是有局限性的，需要使用更加底层的 API，比如 RedisTemplate，来完成逻辑开发，下面就来介绍一些比较重要的功能。</p>
<h3 data-nodeid="1524">秒杀业务介绍</h3>
<p data-nodeid="1525">秒杀，是对正常业务流程的考验。因为它会产生突发流量，平常一天的请求，可能就集中在几秒内就要完成。比如，京东的某些抢购，可能库存就几百个，但是瞬时进入的流量可能是几十上百万。</p>
<p data-nodeid="1526"><img src="https://s0.lgstatic.com/i/image/M00/3D/AF/CgqCHl8qa06AXwgiAABlE7P5SV4914.png" alt="Drawing 2.png" data-nodeid="1702"></p>
<p data-nodeid="1527">如果参与秒杀的人，等待很长时间，体验就非常差，想象一下拥堵的高速公路收费站，就能理解秒杀者的心情。同时，被秒杀的资源会成为热点，发生并发争抢的后果。比如 12306 的抢票，如果单纯使用数据库来接受这些请求，就会产生严重的锁冲突，这也是秒杀业务难的地方。</p>
<p data-nodeid="1528">大家可以回忆一下上一课时的内容，此时，秒杀前端需求与数据库之间的速度是严重不匹配的，而且秒杀的资源是热点资源。这种场景下，采用缓存是非常合适的。</p>
<p data-nodeid="1529"><strong data-nodeid="1708">处理秒杀业务有三个绝招：</strong></p>
<ul data-nodeid="1530">
<li data-nodeid="1531">
<p data-nodeid="1532">第一，选择速度最快的内存作为数据写入；</p>
</li>
<li data-nodeid="1533">
<p data-nodeid="1534">第二，使用异步处理代替同步请求；</p>
</li>
<li data-nodeid="1535">
<p data-nodeid="1536">第三，使用分布式横向扩展。</p>
</li>
</ul>
<p data-nodeid="1537">下面，我们就来看一下 Redis 是如何助力秒杀的。</p>
<h3 data-nodeid="1538">Lua 脚本完成秒杀</h3>
<p data-nodeid="1539">一个秒杀系统是非常复杂的，一般来说，秒杀可以分为一下三个阶段：</p>
<ul data-nodeid="1540">
<li data-nodeid="1541">
<p data-nodeid="1542"><strong data-nodeid="1719">准备阶段</strong>，会提前载入一些必需的数据到缓存中，并提前预热业务数据，用户会不断刷新页面，来查看秒杀是否开始；</p>
</li>
<li data-nodeid="1543">
<p data-nodeid="1544"><strong data-nodeid="1724">抢购阶段</strong>，就是我们通常说的秒杀，会产生瞬时的高并发流量，对资源进行集中操作；</p>
</li>
<li data-nodeid="1545">
<p data-nodeid="1546"><strong data-nodeid="1729">结束清算</strong>，主要完成数据的一致性，处理一些异常情况和回仓操作。</p>
</li>
</ul>
<p data-nodeid="1547"><img src="https://s0.lgstatic.com/i/image/M00/3D/A4/Ciqc1F8qa1eAfW9ZAADONsLsuh4160.png" alt="Drawing 4.png" data-nodeid="1732"></p>
<p data-nodeid="1548">下面，我将介绍一下最重要的秒杀阶段。</p>
<p data-nodeid="1549">我们可以设计一个 Hash 数据结构，来支持库存的扣减。</p>
<pre class="lang-java" data-nodeid="1550"><code data-language="java">seckill:goods:${goodsId}{ 
 &nbsp; &nbsp;total: <span class="hljs-number">100</span>, 
 &nbsp; &nbsp;start: <span class="hljs-number">0</span>, 
 &nbsp; &nbsp;alloc:<span class="hljs-number">0</span> 
}
</code></pre>
<p data-nodeid="1551">在这个 Hash 数据结构中，有以下三个重要部分：</p>
<ul data-nodeid="1552">
<li data-nodeid="1553">
<p data-nodeid="1554"><strong data-nodeid="1740">total</strong> 是一个静态值，表示要秒杀商品的数量，在秒杀开始前，会将这个数值载入到缓存中。</p>
</li>
<li data-nodeid="1555">
<p data-nodeid="1556"><strong data-nodeid="1745">start</strong> 是一个布尔值。秒杀开始前的值为 0；通过后台或者定时，将这个值改为 1，则表示秒杀开始。</p>
</li>
<li data-nodeid="1557">
<p data-nodeid="1558">此时，<strong data-nodeid="1751">alloc</strong> 将会记录已经被秒杀的商品数量，直到它的值达到 total 的上限。</p>
</li>
</ul>
<pre class="lang-java" data-nodeid="1559"><code data-language="java"><span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> String goodsId = <span class="hljs-string">"seckill:goods:%s"</span>; 
 
<span class="hljs-function">String <span class="hljs-title">getKey</span><span class="hljs-params">(String id)</span> </span>{ 
 &nbsp; &nbsp;<span class="hljs-keyword">return</span> String.format(goodsId, id); 
} 
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">prepare</span><span class="hljs-params">(String id, <span class="hljs-keyword">int</span> total)</span> </span>{ 
 &nbsp; &nbsp;String key = getKey(id); 
 &nbsp; &nbsp;Map&lt;String, Integer&gt; goods = <span class="hljs-keyword">new</span> HashMap&lt;&gt;(); 
 &nbsp; &nbsp;goods.put(<span class="hljs-string">"total"</span>, total); 
 &nbsp; &nbsp;goods.put(<span class="hljs-string">"start"</span>, <span class="hljs-number">0</span>); 
 &nbsp; &nbsp;goods.put(<span class="hljs-string">"alloc"</span>, <span class="hljs-number">0</span>); 
 &nbsp; &nbsp;redisTemplate.opsForHash().putAll(key, goods); 
 }
</code></pre>
<p data-nodeid="1560">秒杀的时候，首先需要判断库存，才能够对库存进行锁定。这两步动作并不是原子的，在分布式环境下，多台机器同时对 Redis 进行操作，就会发生同步问题。</p>
<p data-nodeid="1561">为了<strong data-nodeid="1758">解决同步问题</strong>，一种方式就是使用 Lua 脚本，把这些操作封装起来，这样就能保证原子性；另外一种方式就是使用分布式锁，分布式锁我们将在 13、14 课时介绍。</p>
<p data-nodeid="1562">下面是一个调试好的 Lua 脚本，可以看到一些关键的比较动作，和 HINCRBY 命令，能够成为一个原子操作。</p>
<pre class="lang-lua" data-nodeid="1563"><code data-language="lua"><span class="hljs-keyword">local</span> falseRet = <span class="hljs-string">"0"</span> 
<span class="hljs-keyword">local</span> n = <span class="hljs-built_in">tonumber</span>(ARGV[<span class="hljs-number">1</span>]) 
<span class="hljs-keyword">local</span> key = KEYS[<span class="hljs-number">1</span>] 
<span class="hljs-keyword">local</span> goodsInfo = redis.call(<span class="hljs-string">"HMGET"</span>,key,<span class="hljs-string">"total"</span>,<span class="hljs-string">"alloc"</span>) 
<span class="hljs-keyword">local</span> total = <span class="hljs-built_in">tonumber</span>(goodsInfo[<span class="hljs-number">1</span>]) 
<span class="hljs-keyword">local</span> alloc = <span class="hljs-built_in">tonumber</span>(goodsInfo[<span class="hljs-number">2</span>]) 
<span class="hljs-keyword">if</span> <span class="hljs-keyword">not</span> total <span class="hljs-keyword">then</span> 
 &nbsp; &nbsp;<span class="hljs-keyword">return</span> falseRet 
<span class="hljs-keyword">end</span> 
<span class="hljs-keyword">if</span> total &gt;= alloc + n &nbsp;<span class="hljs-keyword">then</span> 
 &nbsp; &nbsp;<span class="hljs-keyword">local</span> ret = redis.call(<span class="hljs-string">"HINCRBY"</span>,key,<span class="hljs-string">"alloc"</span>,n) 
 &nbsp; &nbsp;<span class="hljs-keyword">return</span> <span class="hljs-built_in">tostring</span>(ret) 
<span class="hljs-keyword">end</span> 
<span class="hljs-keyword">return</span> falseRet
</code></pre>
<p data-nodeid="1564">对应的秒杀代码如下，由于我们使用的是 String 的序列化方式，所以会把库存的扣减数量先转化为字符串，然后再调用 Lua 脚本。</p>
<pre class="lang-java" data-nodeid="1565"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">int</span> <span class="hljs-title">secKill</span><span class="hljs-params">(String id, <span class="hljs-keyword">int</span> number)</span> </span>{ 
 &nbsp; &nbsp;String key = getKey(id); 
 &nbsp; &nbsp;Object alloc = &nbsp;redisTemplate.execute(script, Arrays.asList(key), String.valueOf(number)); 
 &nbsp; &nbsp;<span class="hljs-keyword">return</span> Integer.valueOf(alloc.toString()); 
}
</code></pre>
<p data-nodeid="1566">执行仓库里的 testSeckill 方法。启动 1000 个线程对 100 个资源进行模拟秒杀，可以看到生成了 100 条记录，同时其他的线程返回的是 0，表示没有秒杀到。</p>
<p data-nodeid="1567"><img src="https://s0.lgstatic.com/i/image/M00/3D/B0/CgqCHl8qa3KAHrjzAACfVjTuQ9c533.png" alt="Drawing 6.png" data-nodeid="1764"></p>
<h3 data-nodeid="1568">缓存穿透、击穿和雪崩</h3>
<p data-nodeid="1569">抛开秒杀场景，我们再来看一下分布式缓存系统会存在的三大问题： 缓存穿透、缓存击穿和缓存雪崩 。</p>
<h4 data-nodeid="1570">1.缓存穿透</h4>
<p data-nodeid="1571">第一个比较大的问题就是缓存穿透。这个概念比较好理解，和我们上一课时提到的命中率有关。如果命中率很低，那么压力就会集中在数据库持久层。</p>
<p data-nodeid="1572">假如能找到相关数据，我们就可以把它缓存起来。但问题是，<strong data-nodeid="1773">本次请求，在缓存和持久层都没有命中，这种情况就叫缓存的穿透。</strong></p>
<p data-nodeid="1573"><img src="https://s0.lgstatic.com/i/image/M00/3D/B0/CgqCHl8qa32AXy2GAACsgw1i8As520.png" alt="Drawing 7.png" data-nodeid="1776"></p>
<p data-nodeid="1574">举个例子，如上图，在一个登录系统中，有外部攻击，一直尝试使用不存在的用户进行登录，这些用户都是虚拟的，不能有效地被缓存起来，每次都会到数据库中查询一次，最后就会造成服务的性能故障。</p>
<p data-nodeid="1575">解决这个问题有多种方案，我们来简单介绍一下。</p>
<p data-nodeid="1576"><strong data-nodeid="1783">第一种</strong>就是把空对象缓存起来。不是持久层查不到数据吗？那么我们就可以把本次请求的结果设置为 null，然后放入到缓存中。通过设置合理的过期时间，就可以保证后端数据库的安全。</p>
<p data-nodeid="1577">缓存空对象会占用额外的缓存空间，还会有数据不一致的时间窗口，所以<strong data-nodeid="1789">第二种</strong>方法就是针对大数据量的、有规律的键值，使用布隆过滤器进行处理。</p>
<p data-nodeid="1578">一条记录存在与不存在，是一个 Bool 值，只需要使用 1 比特就可存储。<strong data-nodeid="1795">布隆过滤器</strong>就可以把这种是、否操作，压缩到一个数据结构中。比如手机号，用户性别这种数据，就非常适合使用布隆过滤器。</p>
<h4 data-nodeid="1579">2.缓存击穿</h4>
<p data-nodeid="1580">缓存击穿，指的也是用户请求落在数据库上的情况，大多数情况，是由于缓存时间批量过期引起的。</p>
<p data-nodeid="1581">我们一般会对缓存中的数据，设置一个过期时间。如果在某个时刻从数据库获取了大量数据，并设置了同样的过期时间，它们将会在同一时刻失效，造成和缓存的击穿。</p>
<p data-nodeid="1582">对于比较热点的数据，我们就可以设置它不过期；或者在访问的时候，更新它的过期时间；批量入库的缓存项，也尽量分配一个比较平均的过期时间，避免同一时间失效。</p>
<h4 data-nodeid="1583">3.缓存雪崩</h4>
<p data-nodeid="1584">雪崩这个词看着可怕，实际情况也确实比较严重。缓存是用来对系统加速的，后端的数据库只是数据的备份，而不是作为高可用的备选方案。</p>
<p data-nodeid="1585">当缓存系统出现故障，流量会瞬间转移到后端的数据库。过不了多久，数据库将会被大流量压垮挂掉，这种级联式的服务故障，可以形象地称为雪崩。</p>
<p data-nodeid="1586"><img src="https://s0.lgstatic.com/i/image/M00/3D/B0/CgqCHl8qa5CAd61nAAG3-zdlhRw552.png" alt="Drawing 9.png" data-nodeid="1805"></p>
<p data-nodeid="1587">缓存的高可用建设是非常重要的。Redis 提供了主从和 Cluster 的模式，其中 Cluster 模式使用简单，每个分片也能单独做主从，可以保证极高的可用性。</p>
<p data-nodeid="1588">另外，我们对数据库的性能瓶颈有一个大体的评估。如果缓存系统当掉，那么流向数据库的请求，就可以使用限流组件，将请求拦截在外面。</p>
<h3 data-nodeid="1589">缓存一致性</h3>
<p data-nodeid="1590">引入缓存组件后，另外一个老大难的问题，就是缓存的一致性。</p>
<p data-nodeid="1591">我们首先来看问题是怎么发生的。对于一个缓存项来说，常用的操作有四个：写入、更新、读取、删除。</p>
<ul data-nodeid="1592">
<li data-nodeid="1593">
<p data-nodeid="1594"><strong data-nodeid="1815">写入</strong>：缓存和数据库是两个不同的组件，只要涉及双写，就存在只有一个写成功的可能性，造成数据不一致。</p>
</li>
<li data-nodeid="1595">
<p data-nodeid="1596"><strong data-nodeid="1820">更新</strong>：更新的情况类似，需要更新两个不同的组件。</p>
</li>
<li data-nodeid="1597">
<p data-nodeid="1598"><strong data-nodeid="1825">读取</strong>：读取要保证从缓存中读到的信息是最新的，是和数据库中的是一致的。</p>
</li>
<li data-nodeid="1599">
<p data-nodeid="1600"><strong data-nodeid="1830">删除</strong>：当删除数据库记录的时候，如何把缓存中的数据也删掉？</p>
</li>
</ul>
<p data-nodeid="1601">由于业务逻辑大多数情况下，是比较复杂的。其中的更新操作，就非常昂贵，比如一个用户的余额，就是通过计算一系列的资产算出来的一个数。如果这些关联的资产，每个地方改动的时候，都去刷新缓存，那代码结构就会非常混乱，以至于无法维护。</p>
<p data-nodeid="1602">我推荐使用<strong data-nodeid="1837">触发式的缓存一致性方式</strong>，使用懒加载的方式，可以让缓存的同步变得非常简单：</p>
<ul data-nodeid="1603">
<li data-nodeid="1604">
<p data-nodeid="1605">当读取缓存的时候，如果缓存里没有相关数据，则执行相关的业务逻辑，构造缓存数据存入到缓存系统；</p>
</li>
<li data-nodeid="1606">
<p data-nodeid="1607">当与缓存项相关的资源有变动，则先删除相应的缓存项，然后再对资源进行更新，这个时候，即使是资源更新失败，也是没有问题的。</p>
</li>
</ul>
<p data-nodeid="1608">这种操作，除了编程模型简单，有一个明显的好处。我只有在用到这个缓存的时候，才把它加载到缓存系统中。如果每次修改 都创建、更新资源，那缓存系统中就会存在非常多的冷数据。</p>
<p data-nodeid="1609">但这样还是有问题。<strong data-nodeid="1845">接下来介绍的场景，也是面试中经常提及的问题。</strong></p>
<p data-nodeid="1610">我们上面提到的缓存删除动作，和数据库的更新动作，明显是不在一个事务里的。如果一个请求删除了缓存，同时有另外一个请求到来，此时发现没有相关的缓存项，就从数据库里加载了一份到缓存系统。接下来，数据库的更新操作也完成了，此时数据库的内容和缓存里的内容，就产生了不一致。</p>
<p data-nodeid="1611">下面这张图，直观地解释了这种不一致的情况，此时，缓存读取 B 操作以及之后的读取操作，都会读到错误的缓存值。</p>
<p data-nodeid="1612"><img src="https://s0.lgstatic.com/i/image/M00/3D/B0/CgqCHl8qa5-AWDbqAACK1Itu_Wc954.png" alt="Drawing 10.png" data-nodeid="1850"></p>
<p data-nodeid="1613"><strong data-nodeid="1854">在面试中，只要你把这个问题给点出来，面试官都会跷起大拇指。</strong></p>
<p data-nodeid="1614">可以使用分布式锁来解决这个问题，将缓存操作和数据库删除操作，与其他的缓存读操作，使用锁进行资源隔离即可。一般来说，读操作是不需要加锁的，它会在遇到锁的时候，重试等待，直到超时。</p>
<h3 data-nodeid="1615">小结</h3>
<p data-nodeid="1616">本课时的内容有点多，但是非常重要，如果你参加过大型互联网公司的面试，会发现本课时有很多高频面试点，值得你反复揣摩。</p>
<p data-nodeid="1617">本课时和上一课时，都是围绕着缓存展开的，它们之间有很多知识点也比较相似。对于分布式缓存来说，Redis 是现在使用最广泛的。我们先简单介绍了一下它和 Memcached 的一些区别，介绍了 SpringBoot 项目中 Redis 的使用方式，然后以秒杀场景为主，学习了库存扣减这一个核心功能的 Lua 代码。这段代码主要是把条件判断和扣减命令做成了原子性操作。</p>
<p data-nodeid="2609">Redis 的 API 使用非常简单，速度也很快，但同时它也引入了很多问题。如果不能解决这些异常场景，那么 Redis 的价值就大打折扣，所以我们主要谈到了缓存的穿透、击穿以及雪崩的场景，并着重介绍了一下缓存的一致性和解决的思路。</p>
<p data-nodeid="3361" class=""><strong data-nodeid="3365">课后题：上面提到的缓存一致性，有更好的方式去解决，你知道该怎么做么（提示：Cache Aside Pattern）？</strong></p>




<p data-nodeid="1619" class="">下一小节，我将介绍一个与缓存非常类似的优化方法——对象的池化，用复用来增加运行效率，我们下节课见。</p>

---

### 精选评论

##### Albert：
> 最后老师说更新保证缓存的一致性，说到了可以用分布式锁，还有别的方法吗？比如双删，用分布式锁高并发怎么办，老师能给普及下吗？这个找工作确实老问

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 可以这样思考：所谓的一致性，就是到最后总要有个收口的地方。双删操作明显不能收口。Redis的分布式锁性能特别高，能应付大多数高并发场景，这个要到14课时详细介绍。如果redis单机有瓶颈，可以根据锁的内容（比如用户ID）再近一步hash到多台机器上，采用分段的思想解决。这个分段思想不仅仅可以用在锁上，还能用在资源上。比如，把一个红包，先拆成100份，然后每一批人分别对其中的一份进行秒杀。

##### **翔：
> 缓存更新：先更新db，再把缓存失效不能实现吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 赞一个，这是风险和改动最小的，也就是常用的`Cache Aside Pattern`，这个竟然忘了提了。

##### *锋：
> 可以使用分布式锁来解决这个问题，将缓存操作和数据库删除操作，与其他的缓存读操作，使用锁进行资源隔离即可。一般来说，读操作是不需要加锁的，它会在遇到锁的时候，重试等待，直到超时。==========================这样每个牵涉到读db数据写入到redis时，都需要分布式锁，感觉方法有点重。

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; API封装好的话使用还是比较优雅的。比如14课时提到的redlock，就可以像使用JUC中的lock一样使用。
RLock lock = redisson.getLock(resourceKey);
lock.lock(5, TimeUnit.SECONDS);

##### **忠：
> 拉勾的课程怎么缓存，天天地铁上看，流量伤不起

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 视频可以下载的哦～点开“视频”便能看到“缓存课程”按钮

##### **3631：
> 老师，数据一致性，可以先删缓存后更新么？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 可以的，这叫做cache aside pattern，是推荐的做法

##### **4948：
> 分布式锁怎么能解决缓存一致性问题呢？不能解决！！！分布式锁只能解决同一时刻只有一个请求的写操作，写完后释放写锁，这时候读请求来读到db新值，假设读请求卡了，后面又来一个写请求，写请求操作完了。刚才的读请求反映过来了，把上一个写请求的旧值写到缓存了。分布式锁可以应用在缓存击穿时，大量请求同时请求数据库时对db造成压力的情况。

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 1.“同一时刻“：在时间概念上可以无限延伸，一般是锁超时时间；2.分布式锁不是写锁，而是读写锁，写锁是排他的，需要等所有读请求（锁）释放才会加锁进行后续操作。所以不存在读到旧值的场景。

##### **生：
> 先更新db，再把缓存失效。这个会不会有问题，a进行先执行更新db，然后缓存失效，但是还没有提交事务。这个时候b正在查询的时候，没有缓存，就去查db，查询出的是旧数据，并写入了缓存

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 可以把缓存失效放在finally块里，或者放在事务之外。回滚概率是很小的，合理安排生命周期即可

##### **涛：
> 1.缓存更新：先更新db，再把缓存失效不能实现吗？讲师回复： 赞一个，这是风险和改动最小的，也就是常用的`Cache Aside Pattern`，这个竟然忘了提了。2.老师，数据一致性，可以先删缓存后更新么？讲师回复： 可以的，这叫做cache aside pattern，是推荐的做法先删除缓存后更新db 和 先更新db后删除缓存 都叫"cache aside pattern"???本来明白了,看了老师的回复直接懵逼了!

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 来，看一下这篇文章：https://mp.weixin.qq.com/s/A3hcQNoJ3nKKeiINwVYasw，应该能解除你的疑惑

##### **涛：
> 布隆过滤器hash相同的话会预判，不适合记录null值缓存这个场景吧（本来正常请求，db有值但未缓存，被算法错误判定为null而直接过滤，无法访问db拿值，这种情况是不可容忍的）

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 可以这么做的。只不过要多一步操作：在更新值的时候，删掉相应bit位得值。践行cache aside pattern，先更新，再删除，就能保持一致。

##### **城：
> 数据更新时，Cache Aside Pattern 到底是建议先操作缓存还是先操作数据库呢？网上两种观点都有，迷惑。

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 1. 先删除缓存，再更新数据库。 2. 读取的时候，首先读取缓存，读取不到缓存再读取数据库

##### **生：
> 先更新db并提交成功，删除redis失败怎么办？后面就不一致了吧

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 参考其他留言。一般做法都是先删缓存，再更新数据库，也就是cache aside模式。

##### **龙：
> 老师你好，我想问下为什么redis 可以使用lua，lua运行没有特定环境吗？lua怎么和redis配合起来的，这个调用是怎么回事？还有一些网管啥的也用她lua，这到底是怎么做的呢？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; lua是一门非常简洁的语言，非常轻量级，应用非常广泛。可以很容易的嵌入到各种程序里。比如一些游戏中，就用lua做配置脚本用

##### **铭：
> local n = tonumber(ARGV[1])还有这句是啥意思

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; ARGV参数列表，KEYS操作目标列表. Java API的目的就是把这些信息传进来

##### **铭：
> local n ＝ KEYS1是什么意思呀，脚本执行第一句就是这个

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这是lua脚本的语法。像KEYS、ARGV、redis等变量，都是redis做好的预设变量，拿来直接用就行了。配合下面Java代码的exec函数，就可以直接通过这些变量拿到对应key值和参数

##### **林：
> 请问老师redis cluster集群版可以用lua脚本么

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; lua脚本在任何集群模式下都是可用的。可参照redlock的实现

##### Zorrrrrro：
> 老师，请问canal 数据有好的工具类直接还原成sql 吗？看了官方的工具类会还原成预编译的sql 再加上一系列 value 值，但有些场景我想直接还原成简单的 sql ，能直接拿去数据库工具 取datagrip 执行的记录。

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 可以试试binlog2sql这个工具，略过canal，可以将binlog直接转化为dml

##### **安：
> 置顶留言在哪看呀

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 在开篇词的留言区顶部哦

##### **安：
> 打卡redis

##### **用户0528：
> 可以使用分布式锁来解决这个问题，将缓存操作和数据库删除操作，与其他的缓存读操作，使用锁进行资源隔离即可。一般来说，读操作是不需要加锁的，它会在遇到锁的时候，重试等待，直到超时————这是指在删除更新和查询更新，需要操作缓存时，用分布式锁来锁定么

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是的，适合读多写少场景。读操作只需要判断有没有写锁，有的话就等待或者重试。写操作需要等待读写操作都完成才能够获取锁，所以更加复杂一些。自己实现的话太繁杂，org.redisson.api.RReadWriteLock可以去做这些事情。可以参考Cache Aside Pattern的做法，先操作数据再删除缓存。

##### **伟：
> 缓存更新问题 可以考虑订阅数据库binlog更新缓存 就避开了上面这些问题.

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; binlog可以使用canal组件，但它只能针对mysql数据库，能够适应大部分应用场景。binlog也有问题，比如时效性、时效性和消息顺序问题。很多项目型公司为了部署方便，也会尽量减少依赖组件的添加。
许多应用使用了多级缓存，比如堆内缓存->分布式缓存->DB，这种场景就要非常小心。
具体方案还要根据业务实际情况去制定。

##### **辉：
> 最新版的redis已经支持多线程了吧

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这个是Redis 6.0之后加入的，太新了。
redis使用的i/o多路复用，所以速度已经足够快。它的多线程主要是做下面的工作：
1.异步做一些比较耗时的操作，比如删除大key。
2.异步做网络读写

但是。redis的读写操作虽然做成异步了，但是它的命令执行操作依然是单线程的（避免了顺序和并发问题）

所以redis的异步化是相对片面的，并不是全部多线程。我们6.0以前的redis规范应验依然适用。

##### **用户0370：
> 文章中提供的redis项目代码，在gitee上没有开放下载呢

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 地址参见置顶留言，代码是public权限，任何人都可以访问的。

