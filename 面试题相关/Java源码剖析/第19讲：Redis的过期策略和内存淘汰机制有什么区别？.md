<p>Redis 和 MySQL 是面试绕不过的两座大山，他们一个是关系型数据库的代表（MySQL），一个是键值数据库以及缓存中间件的一哥。尤其 Redis 几乎是所有互联网公司都在用的技术，比如国内的 BATJ、新浪、360、小米等公司；国外的微软、Twitter、Stack Overflow、GitHub、暴雪等公司。我从业了十几年，就职过 4、5 家公司，有的公司用 MySQL、有的用 SQL Server、甚至还有的用 Oracle 和 DB2，但缓存无一例外使用的都是 Redis，从某种程度上来讲 Redis 是普及率最高的技术，没有之一。</p>
<p>我们本课时的面试题是，Redis 是如何处理过期数据的？当内存不够用时 Redis 又是如何处理的？</p>
<h3>典型回答</h3>
<p>我们在新增 Redis 缓存时可以设置缓存的过期时间，该时间保证了数据在规定的时间内失效，可以借助这个特性来实现很多功能。比如，存储一定天数的用户（登录）会话信息，这样在一定范围内用户不用重复登录了，但为了安全性，需要在一定时间之后重新验证用户的信息。因此，我们可以使用 Redis 设置过期时间来存储用户的会话信息。</p>
<p>对于已经过期的数据，Redis 将使用两种策略来删除这些过期键，它们分别是<strong>惰性删除</strong>和<strong>定期删除</strong>。</p>
<p><strong>惰性删除</strong>是指 Redis 服务器不主动删除过期的键值，而是当访问键值时，再检查当前的键值是否过期，如果过期则执行删除并返回 null 给客户端；如果没过期则正常返回值信息给客户端。</p>
<p>它的优点是不会浪费太多的系统资源，只是在每次访问时才检查键值是否过期。缺点是删除过期键不及时，造成了一定的空间浪费。</p>
<p>惰性删除的源码位于 src/db.c 文件的 expireIfNeeded 方法中，如下所示：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">int</span> <span class="hljs-title">expireIfNeeded</span><span class="hljs-params">(redisDb *db, robj *key)</span> </span>{
    <span class="hljs-comment">// 判断键是否过期</span>
    <span class="hljs-keyword">if</span> (!keyIsExpired(db,key)) <span class="hljs-keyword">return</span> <span class="hljs-number">0</span>;
    <span class="hljs-keyword">if</span> (server.masterhost != NULL) <span class="hljs-keyword">return</span> <span class="hljs-number">1</span>;
    <span class="hljs-comment">/* 删除过期键 */</span>
    <span class="hljs-comment">// 增加过期键个数</span>
    server.stat_expiredkeys++;
    <span class="hljs-comment">// 传播键过期的消息</span>
    propagateExpire(db,key,server.lazyfree_lazy_expire);
    notifyKeyspaceEvent(NOTIFY_EXPIRED,
        <span class="hljs-string">"expired"</span>,key,db-&gt;id);
    <span class="hljs-comment">// server.lazyfree_lazy_expire 为 1 表示异步删除，否则则为同步删除</span>
    <span class="hljs-keyword">return</span> server.lazyfree_lazy_expire ? dbAsyncDelete(db,key) :
                                         dbSyncDelete(db,key);
}
<span class="hljs-comment">// 判断键是否过期</span>
<span class="hljs-function"><span class="hljs-keyword">int</span> <span class="hljs-title">keyIsExpired</span><span class="hljs-params">(redisDb *db, robj *key)</span> </span>{
    mstime_t when = getExpire(db,key);
    <span class="hljs-keyword">if</span> (when &lt; <span class="hljs-number">0</span>) <span class="hljs-keyword">return</span> <span class="hljs-number">0</span>; 
    <span class="hljs-keyword">if</span> (server.loading) <span class="hljs-keyword">return</span> <span class="hljs-number">0</span>;
    mstime_t now = server.lua_caller ? server.lua_time_start : mstime();
    <span class="hljs-keyword">return</span> now &gt; when;
}
<span class="hljs-comment">// 获取键的过期时间</span>
<span class="hljs-function"><span class="hljs-keyword">long</span> <span class="hljs-keyword">long</span> <span class="hljs-title">getExpire</span><span class="hljs-params">(redisDb *db, robj *key)</span> </span>{
    dictEntry *de;
    <span class="hljs-keyword">if</span> (dictSize(db-&gt;expires) == <span class="hljs-number">0</span> ||
       (de = dictFind(db-&gt;expires,key-&gt;ptr)) == NULL) <span class="hljs-keyword">return</span> -<span class="hljs-number">1</span>;
    serverAssertWithInfo(NULL,key,dictFind(db-&gt;dict,key-&gt;ptr) != NULL);
    <span class="hljs-keyword">return</span> dictGetSignedIntegerVal(de);
}
</code></pre>
<p>惰性删除的执行流程如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/08/B3/CgqCHl67XjqAPZOAAAA2gxqwspY599.png" alt="图片1.png"></p>
<p>除了惰性删除之外，Redis 还提供了定期删除功能以弥补惰性删除的不足。</p>
<p>定期删除是指 Redis 服务器每隔一段时间会检查一下数据库，看看是否有过期键可以被清除。</p>
<p>默认情况下 Redis 定期检查的频率是每秒扫描 10 次，用于定期清除过期键。当然此值还可以通过配置文件进行设置，在 redis.conf 中修改配置“hz”即可，默认的值为“hz 10”。</p>
<blockquote>
<p>小贴士：定期删除的扫描并不是遍历所有的键值对，这样的话比较费时且太消耗系统资源。Redis 服务器采用的是随机抽取形式，每次从过期字典中，取出 20 个键进行过期检测，过期字典中存储的是所有设置了过期时间的键值对。如果这批随机检查的数据中有 25% 的比例过期，那么会再抽取 20 个随机键值进行检测和删除，并且会循环执行这个流程，直到抽取的这批数据中过期键值小于 25%，此次检测才算完成。</p>
</blockquote>
<p>定期删除的源码在 expire.c 文件的 activeExpireCycle 方法中，如下所示：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">activeExpireCycle</span><span class="hljs-params">(<span class="hljs-keyword">int</span> type)</span> </span>{
    <span class="hljs-keyword">static</span> unsigned <span class="hljs-keyword">int</span> current_db = <span class="hljs-number">0</span>; <span class="hljs-comment">/* 上次定期删除遍历到的数据库ID */</span>
    <span class="hljs-keyword">static</span> <span class="hljs-keyword">int</span> timelimit_exit = <span class="hljs-number">0</span>;      
    <span class="hljs-keyword">static</span> <span class="hljs-keyword">long</span> <span class="hljs-keyword">long</span> last_fast_cycle = <span class="hljs-number">0</span>; <span class="hljs-comment">/* 上次执行定期删除的时间点 */</span>
    <span class="hljs-keyword">int</span> j, iteration = <span class="hljs-number">0</span>;
    <span class="hljs-keyword">int</span> dbs_per_call = CRON_DBS_PER_CALL; <span class="hljs-comment">// 需要遍历数据库的数量</span>
    <span class="hljs-keyword">long</span> <span class="hljs-keyword">long</span> start = ustime(), timelimit, elapsed;
    <span class="hljs-keyword">if</span> (clientsArePaused()) <span class="hljs-keyword">return</span>;
    <span class="hljs-keyword">if</span> (type == ACTIVE_EXPIRE_CYCLE_FAST) {
        <span class="hljs-keyword">if</span> (!timelimit_exit) <span class="hljs-keyword">return</span>;
        <span class="hljs-comment">// ACTIVE_EXPIRE_CYCLE_FAST_DURATION 快速定期删除的执行时长</span>
        <span class="hljs-keyword">if</span> (start &lt; last_fast_cycle + ACTIVE_EXPIRE_CYCLE_FAST_DURATION*<span class="hljs-number">2</span>) <span class="hljs-keyword">return</span>;
        last_fast_cycle = start;
    }
    <span class="hljs-keyword">if</span> (dbs_per_call &gt; server.dbnum || timelimit_exit)
        dbs_per_call = server.dbnum;
    <span class="hljs-comment">// 慢速定期删除的执行时长</span>
    timelimit = <span class="hljs-number">1000000</span>*ACTIVE_EXPIRE_CYCLE_SLOW_TIME_PERC/server.hz/<span class="hljs-number">100</span>;
    timelimit_exit = <span class="hljs-number">0</span>;
    <span class="hljs-keyword">if</span> (timelimit &lt;= <span class="hljs-number">0</span>) timelimit = <span class="hljs-number">1</span>;
    <span class="hljs-keyword">if</span> (type == ACTIVE_EXPIRE_CYCLE_FAST)
        timelimit = ACTIVE_EXPIRE_CYCLE_FAST_DURATION; <span class="hljs-comment">/* 删除操作花费的时间 */</span>
    <span class="hljs-keyword">long</span> total_sampled = <span class="hljs-number">0</span>;
    <span class="hljs-keyword">long</span> total_expired = <span class="hljs-number">0</span>;
    <span class="hljs-keyword">for</span> (j = <span class="hljs-number">0</span>; j &lt; dbs_per_call &amp;&amp; timelimit_exit == <span class="hljs-number">0</span>; j++) {
        <span class="hljs-keyword">int</span> expired;
        redisDb *db = server.db+(current_db % server.dbnum);
        current_db++;
        <span class="hljs-keyword">do</span> {
            <span class="hljs-comment">// .......</span>
            expired = <span class="hljs-number">0</span>;
            ttl_sum = <span class="hljs-number">0</span>;
            ttl_samples = <span class="hljs-number">0</span>;
            <span class="hljs-comment">// 每个数据库中检查的键的数量</span>
            <span class="hljs-keyword">if</span> (num &gt; ACTIVE_EXPIRE_CYCLE_LOOKUPS_PER_LOOP)
                num = ACTIVE_EXPIRE_CYCLE_LOOKUPS_PER_LOOP;
            <span class="hljs-comment">// 从数据库中随机选取 num 个键进行检查</span>
            <span class="hljs-keyword">while</span> (num--) {
                dictEntry *de;
                <span class="hljs-keyword">long</span> <span class="hljs-keyword">long</span> ttl;
                <span class="hljs-keyword">if</span> ((de = dictGetRandomKey(db-&gt;expires)) == NULL) <span class="hljs-keyword">break</span>;
                ttl = dictGetSignedInteger
                <span class="hljs-comment">// 过期检查，并对过期键进行删除</span>
                <span class="hljs-keyword">if</span> (activeExpireCycleTryExpire(db,de,now)) expired++;
                <span class="hljs-keyword">if</span> (ttl &gt; <span class="hljs-number">0</span>) {
                    ttl_sum += ttl;
                    ttl_samples++;
                }
                total_sampled++;
            }
            total_expired += expired;
            <span class="hljs-keyword">if</span> (ttl_samples) {
                <span class="hljs-keyword">long</span> <span class="hljs-keyword">long</span> avg_ttl = ttl_sum/ttl_samples;
                <span class="hljs-keyword">if</span> (db-&gt;avg_ttl == <span class="hljs-number">0</span>) db-&gt;avg_ttl = avg_ttl;
                db-&gt;avg_ttl = (db-&gt;avg_ttl/<span class="hljs-number">50</span>)*<span class="hljs-number">49</span> + (avg_ttl/<span class="hljs-number">50</span>);
            }
            <span class="hljs-keyword">if</span> ((iteration &amp; <span class="hljs-number">0xf</span>) == <span class="hljs-number">0</span>) { <span class="hljs-comment">/* check once every 16 iterations. */</span>
                elapsed = ustime()-start;
                <span class="hljs-keyword">if</span> (elapsed &gt; timelimit) {
                    timelimit_exit = <span class="hljs-number">1</span>;
                    server.stat_expired_time_cap_reached_count++;
                    <span class="hljs-keyword">break</span>;
                }
            }
            <span class="hljs-comment">/* 判断过期键删除数量是否超过 25% */</span>
        } <span class="hljs-keyword">while</span> (expired &gt; ACTIVE_EXPIRE_CYCLE_LOOKUPS_PER_LOOP/<span class="hljs-number">4</span>);
    }
    <span class="hljs-comment">// .......</span>
}
</code></pre>
<p>定期删除的执行流程，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/08/B3/Ciqc1F67Xl6ARsvYAADOQ1M6viA643.png" alt="图片2.png"></p>
<blockquote>
<p>小贴士：Redis 服务器为了保证过期删除策略不会导致线程卡死，会给过期扫描增加了最大执行时间为 25ms。</p>
</blockquote>
<p>以上是 Redis 服务器对待过期键的处理方案，当 Redis 的内存超过最大允许的内存之后，Redis 会触发内存淘汰策略，这和过期策略是完全不同的两个概念，经常有人把二者搞混，这两者一个是在正常情况下清除过期键，一个是在非正常情况下为了保证 Redis 顺利运行的保护策略。</p>
<p>当 Redis 内存不够用时，Redis 服务器会根据服务器设置的淘汰策略，删除一些不常用的数据，以保证 Redis 服务器的顺利运行。</p>
<h3>考点分析</h3>
<p>本课时的面试题并非 Redis 的入门级面试题，需要面试者对 Redis 有一定的了解才能对答如流，并且 Redis 的过期淘汰策略和内存淘汰策略的概念比较类似，都是用于淘汰数据的。因此很多人会把二者当成一回事，但其实并不是，这个面试者特别注意一下，和此知识点相关的面试题还有以下这些：</p>
<ul>
<li>Redis 内存淘汰策略有哪些？</li>
<li>Redis 有哪些内存淘汰算法？</li>
</ul>
<h3>知识扩展</h3>
<h4>Redis 内存淘汰策略</h4>
<p>我们可以使用 config get maxmemory-policy 命令，来查看当前 Redis 的内存淘汰策略，示例代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-number">127.0</span><span class="hljs-number">.0</span><span class="hljs-number">.1</span>:<span class="hljs-number">6379</span>&gt; config get maxmemory-policy
<span class="hljs-number">1</span>) <span class="hljs-string">"maxmemory-policy"</span>
<span class="hljs-number">2</span>) <span class="hljs-string">"noeviction"</span>
</code></pre>
<p>从上面的结果可以看出，当前 Redis 服务器设置的是“noeviction”类型的内存淘汰策略，那么这表示什么含义呢？Redis 又有几种内存淘汰策略呢？</p>
<p>在 4.0 版本之前 Redis 的内存淘汰策略有以下 6 种。</p>
<ul>
<li>noeviction：不淘汰任何数据，当内存不足时，执行缓存新增操作会报错，它是 Redis 默认内存淘汰策略。</li>
<li>allkeys-lru：淘汰整个键值中最久未使用的键值。</li>
<li>allkeys-random：随机淘汰任意键值。</li>
<li>volatile-lru：淘汰所有设置了过期时间的键值中最久未使用的键值。</li>
<li>volatile-random：随机淘汰设置了过期时间的任意键值。</li>
<li>volatile-ttl：优先淘汰更早过期的键值。</li>
</ul>
<p>可以看出我们上面示例使用的是 Redis 默认的内存淘汰策略“noeviction”。</p>
<p>而在 Redis 4.0 版本中又新增了 2 种淘汰策略：</p>
<ul>
<li>volatile-lfu，淘汰所有设置了过期时间的键值中最少使用的键值；</li>
<li>allkeys-lfu，淘汰整个键值中最少使用的键值。</li>
</ul>
<blockquote>
<p>小贴士：从以上内存淘汰策略中可以看出，allkeys-xxx 表示从所有的键值中淘汰数据，而 volatile-xxx 表示从设置了过期键的键值中淘汰数据。</p>
</blockquote>
<p>这个内存淘汰策略我们可以通过配置文件来修改，redis.conf 对应的配置项是“maxmemory-policy noeviction”，只需要把它修改成我们需要设置的类型即可。</p>
<p>需要注意的是，如果使用修改 redis.conf 的方式，当设置完成之后需要重启 Redis 服务器才能生效。</p>
<p>还有另一种简单的修改内存淘汰策略的方式，我们可以使用命令行工具输入“config set maxmemory-policy noeviction”来修改内存淘汰的策略，这种修改方式的好处是执行成功之后就会生效，无需重启 Redis 服务器。但它的坏处是不能持久化内存淘汰策略，每次重启 Redis 服务器之后设置的内存淘汰策略就会丢失。</p>
<h4>Redis 内存淘汰算法</h4>
<p>内存淘汰算法主要包含两种：LRU 淘汰算法和 LFU 淘汰算法。</p>
<p><strong>LRU</strong>（ Least Recently Used，最近最少使用）<strong>淘汰算法</strong>：是一种常用的页面置换算法，也就是说最久没有使用的缓存将会被淘汰。</p>
<p>LRU 是基于链表结构实现的，链表中的元素按照操作顺序从前往后排列，最新操作的键会被移动到表头，当需要进行内存淘汰时，只需要删除链表尾部的元素即可。</p>
<p>Redis 使用的是一种近似 LRU 算法，目的是为了更好的节约内存，它的实现方式是给现有的数据结构添加一个额外的字段，用于记录此键值的最后一次访问时间。Redis 内存淘汰时，会使用随机采样的方式来淘汰数据，它是随机取 5 个值 (此值可配置) ，然后淘汰最久没有使用的数据。</p>
<p><strong>LFU</strong>（Least Frequently Used，最不常用的）<strong>淘汰算法</strong>：最不常用的算法是根据总访问次数来淘汰数据的，它的核心思想是“如果数据过去被访问多次，那么将来被访问的频率也更高”。</p>
<p>LFU 相对来说比 LRU 更“智能”，因为它解决了使用频率很低的缓存，只是最近被访问了一次就不会被删除的问题。如果是使用 LRU 类似这种情况数据是不会被删除的，而使用 LFU 的话，这个数据就会被删除。</p>
<p>Redis 内存淘汰策略使用了 LFU 和近 LRU 的淘汰算法，具体使用哪种淘汰算法，要看服务器是如何设置内存淘汰策略的，也就是要看“maxmemory-policy”的值是如何设置的。</p>
<h3>小结</h3>
<p>本课时我们讲了 Redis 的过期删除策略：惰性删除 + 定期删除；还讲了 Redis 的内存淘汰策略，它和过期策略是完全不同的两个概念，内存淘汰策略是当内存不够用时才会触发的一种机制，它在 Redis 4.0 之后提供了 8 种内存淘汰策略，这些淘汰策略主要使用了近 LRU 淘汰算法和 LFU 淘汰算法。</p>

---

### 精选评论

##### **冬：
> 过期数据的删除：1、惰性删除，当访问的时候，判断是否过期2、定期随机删除。全量删除，会占用大量资源。内存不足时，淘汰策略：1、volatile-lru2、volatile-random3、allkeys-lru。一般使用volatile-lru，按照LRU算法逐出数据，但仅逐出设置了过期时间的数据

##### **雄：
> LFU算法的思想是：如果一个数据在最近一段时间很少被访问到，那么可以认为在将来它被访问的可能性也很小。因此，当空间满时，最小频率访问的数据最先被淘汰。LRU算法的思想是：如果一个数据在最近一段时间没有被访问到，那么可以认为在将来它被访问的可能性也很小。因此，当空间满时，最久没有访问的数据最先被置换（淘汰）。

##### **台：
> 请教下问题，第一个问题，淘汰策略是内存不足的触发，有具体的阈值定义吗，不太可能等没有了再淘汰吧，这样会阻塞主线程。第二个问题，停止淘汰有具体的标准和相关源码分析吗

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 当内存的使用超过配置的最大内存就会触发内存淘汰，可使用 config get maxmemory 查看，也可以通过修改配置文件或者使用 config set maxmemory 进行配置。Redis 源码可以参考 https://blog.csdn.net/weixin_43705457/article/details/105087813

