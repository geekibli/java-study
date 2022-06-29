<p>细心的你可能发现了，本系列课程中竟然出现了三个课时都是在说消息队列，第 10 课时讲了程序级别的消息队列以及延迟消息队列的实现，而第 15 课时讲了常见的消息队列中间件 RabbitMQ、Kafka 等，由此可见消息队列在整个 Java 技术体系中的重要程度。本课时我们将重点来看一下 Redis 是如何实现消息队列的。</p>
<p>我们本课时的面试题是，在 Redis 中实现消息队列的方式有几种？</p>
<h3>典型回答</h3>
<p>早在 Redis 2.0 版本之前使用 Redis 实现消息队列的方式有两种：</p>
<ul>
<li>使用 List 类型实现</li>
<li>使用 ZSet 类型实现</li>
</ul>
<p>其中使用** List 类型实现的方式最为简单和直接**，它主要是通过 lpush、rpop 存入和读取实现消息队列的，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0E/10/CgqCHl7E8CiAPUYfAAA-J0r2Mgc997.png" alt="image.png"></p>
<p>lpush 可以把最新的消息存储到消息队列（List 集合）的首部，而 rpop 可以读取消息队列的尾部，这样就实现了先进先出，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0E/04/Ciqc1F7E8C6AJKWJAABCSLt5htE783.png" alt="image (1).png"></p>
<p>命令行的实现命令如下：</p>
<pre><code data-language="powershell" class="lang-powershell"><span class="hljs-number">127.0</span>.<span class="hljs-number">0.1</span>:<span class="hljs-number">6379</span>&gt; lpush mq <span class="hljs-string">"java"</span> <span class="hljs-comment">#推送消息 java</span>
(integer) <span class="hljs-number">1</span>
<span class="hljs-number">127.0</span>.<span class="hljs-number">0.1</span>:<span class="hljs-number">6379</span>&gt; lpush mq <span class="hljs-string">"msg"</span> <span class="hljs-comment">#推送消息 msg</span>
(integer) <span class="hljs-number">2</span>
<span class="hljs-number">127.0</span>.<span class="hljs-number">0.1</span>:<span class="hljs-number">6379</span>&gt; rpop mq <span class="hljs-comment">#接收到消息 java</span>
<span class="hljs-string">"java"</span>
<span class="hljs-number">127.0</span>.<span class="hljs-number">0.1</span>:<span class="hljs-number">6379</span>&gt; rpop mq <span class="hljs-comment">#接收到消息 msg</span>
<span class="hljs-string">"mq"</span>
</code></pre>
<p>其中，mq 相当于消息队列的名称，而 lpush 用于生产并添加消息，而 rpop 用于拉取并消费消息。<br>
使用 List 实现消息队列的优点是消息可以被持久化，List 可以借助 Redis 本身的持久化功能，AOF 或者是 RDB 或混合持久化的方式，用于把数据保存至磁盘，这样当 Redis 重启之后，消息不会丢失。</p>
<p>但使用 List 同样存在一定的问题，比如消息不支持重复消费、没有按照主题订阅的功能、不支持消费消息确认等。</p>
<p>ZSet 实现消息队列的方式和 List 类似，它是利用 zadd 和 zrangebyscore 来实现存入和读取消息的，这里就不重复叙述了。但 ZSet 的实现方式更为复杂一些，因为 ZSet 多了一个分值（score）属性，我们可以使用它来实现更多的功能，比如用它来存储时间戳，以此来实现延迟消息队列等。</p>
<p>ZSet 同样具备持久化的功能，List 存在的问题它也同样存在，不但如此，使用 ZSet 还不能存储相同元素的值。因为它是有序集合，有序集合的存储元素值是不能重复的，但分值可以重复，也就是说当消息值重复时，只能存储一条信息在 ZSet 中。</p>
<p>在 Redis 2.0 之后 Redis 就新增了专门的发布和订阅的类型，Publisher（发布者）和 Subscriber（订阅者）来实现消息队列了，它们对应的执行命令如下：</p>
<ul>
<li>发布消息，publish channel "message"</li>
<li>订阅消息，subscribe channel</li>
</ul>
<p>使用发布和订阅的类型，我们可以实现主题订阅的功能，也就是 Pattern Subscribe 的功能。因此我们可以使用一个消费者“queue_*”来订阅所有以“queue_”开头的消息队列，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0E/10/CgqCHl7E8D2AUKjhAABVTe21uBg498.png" alt="image (2).png"></p>
<p>发布订阅模式的优点很明显，但同样存在以下 3 个问题：</p>
<ul>
<li>无法持久化保存消息，如果 Redis 服务器宕机或重启，那么所有的消息将会丢失；</li>
<li>发布订阅模式是“发后既忘”的工作模式，如果有订阅者离线重连之后就不能消费之前的历史消息；</li>
<li>不支持消费者确认机制，稳定性不能得到保证，例如当消费者获取到消息之后，还没来得及执行就宕机了。因为没有消费者确认机制，Redis 就会误以为消费者已经执行了，因此就不会重复发送未被正常消费的消息了，这样整体的 Redis 稳定性就被没有办法得到保障了。</li>
</ul>
<p>然而在 Redis 5.0 之后新增了 Stream 类型，我们就可以使用 Stream 的 xadd 和 xrange 来实现消息的存入和读取了，并且 Stream 提供了 xack 手动确认消息消费的命令，用它我们就可以实现消费者确认的功能了，使用命令如下：</p>
<pre><code data-language="powershell" class="lang-powershell"><span class="hljs-number">127.0</span>.<span class="hljs-number">0.1</span>:<span class="hljs-number">6379</span>&gt; xack mq group1 <span class="hljs-number">1580959593553</span><span class="hljs-literal">-0</span>
(integer) <span class="hljs-number">1</span>
</code></pre>
<p>相关语法如下：</p>
<pre><code data-language="java" class="lang-java">xack key group-key ID [ID ...]&nbsp;
</code></pre>
<p>消费确认增加了消息的可靠性，一般在业务处理完成之后，需要执行 ack 确认消息已经被消费完成，整个流程的执行如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/0E/10/CgqCHl7E8GGAbuSbAADG34Yr_oI363.png" alt="image (3).png"></p>
<p>其中“Group”为群组，消费者也就是接收者需要订阅到群组才能正常获取到消息。</p>
<p>以上就 Redis 实现消息队列的四种方式，他们分别是：</p>
<ul>
<li>使用 List 实现消息队列；</li>
<li>使用 ZSet 实现消息队列；</li>
<li>使用发布订阅者模式实现消息队列；</li>
<li>使用  Stream 实现消息队列。</li>
</ul>
<h3>考点分析</h3>
<p>本课时的题目比较全面的考察了面试者对于 Redis 整体知识框架和新版本特性的理解和领悟。早期版本中比较常用的实现消息队列的方式是 List、ZSet 和发布订阅者模式，使用 Stream 来实现消息队列是近两年才流行起来的方案，并且很多企业也没有使用到 Redis 5.0 这么新的版本。因此只需回答出前三种就算及格了，而 Stream 方式实现消息队列属于附加题，如果面试中能回答上来的话就更好了，它体现了你对新技术的敏感度与对技术的热爱程度，属于面试中的加分项。</p>
<p>和此知识点相关的面试题还有以下几个：</p>
<ul>
<li>在 Java 代码中使用 List 实现消息队列会有什么问题？应该如何解决？</li>
<li>在程序中如何使用 Stream 来实现消息队列？</li>
</ul>
<h3>知识扩展</h3>
<h4>使用 List 实现消息队列</h4>
<p>在 Java 程序中我们需要使用 Redis 客户端框架来辅助程序操作 Redis，比如 Jedis 框架。</p>
<p>使用 Jedis 框架首先需要在 pom.xml 文件中添加 Jedis 依赖，配置如下：</p>
<pre><code data-language="xml" class="lang-xml"><span class="hljs-comment">&lt;!-- https://mvnrepository.com/artifact/redis.clients/jedis --&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span>
&nbsp; <span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>redis.clients<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span>
&nbsp; <span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>jedis<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span>
&nbsp; <span class="hljs-tag">&lt;<span class="hljs-name">version</span>&gt;</span>${version}<span class="hljs-tag">&lt;/<span class="hljs-name">version</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span>
</code></pre>
<p>List 实现消息队列的完整代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">import</span> redis.clients.jedis.Jedis;
publicclass ListMQTest {
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span></span>{
        <span class="hljs-comment">// 启动一个线程作为消费者</span>
        <span class="hljs-keyword">new</span> Thread(() -&gt; consumer()).start();
        <span class="hljs-comment">// 生产者</span>
        producer();
    }
    <span class="hljs-comment">/**
     * 生产者
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">producer</span><span class="hljs-params">()</span> </span>{
        Jedis jedis = <span class="hljs-keyword">new</span> Jedis(<span class="hljs-string">"127.0.0.1"</span>, <span class="hljs-number">6379</span>);
        <span class="hljs-comment">// 推送消息</span>
        jedis.lpush(<span class="hljs-string">"mq"</span>, <span class="hljs-string">"Hello, List."</span>);
    }
    <span class="hljs-comment">/**
     * 消费者
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">consumer</span><span class="hljs-params">()</span> </span>{
        Jedis jedis = <span class="hljs-keyword">new</span> Jedis(<span class="hljs-string">"127.0.0.1"</span>, <span class="hljs-number">6379</span>);
        <span class="hljs-comment">// 消费消息</span>
        <span class="hljs-keyword">while</span> (<span class="hljs-keyword">true</span>) {
            <span class="hljs-comment">// 获取消息</span>
            String msg = jedis.rpop(<span class="hljs-string">"mq"</span>);
            <span class="hljs-keyword">if</span> (msg != <span class="hljs-keyword">null</span>) {
                <span class="hljs-comment">// 接收到了消息</span>
                System.out.println(<span class="hljs-string">"接收到消息："</span> + msg);
            }
        }
    }
}
</code></pre>
<p>以上程序的运行结果是：</p>
<pre><code data-language="java" class="lang-java">接收到消息：Hello, Java.
</code></pre>
<p>但是以上的代码存在一个问题，可以看出以上消费者的实现是通过 while 无限循环来获取消息，但如果消息的空闲时间比较长，一直没有新任务，而 while 循环不会因此停止，<strong>它会一直执行循环的动作，这样就会白白浪费了系统的资源</strong>。</p>
<p>此时我们可以借助 Redis 中的<strong>阻塞读</strong>来替代 rpop 的方法就可以解决此问题，具体实现代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">import</span> redis.clients.jedis.Jedis;
<span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">ListMQExample</span> </span>{
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> <span class="hljs-keyword">throws</span> InterruptedException </span>{
        <span class="hljs-comment">// 消费者</span>
        <span class="hljs-keyword">new</span> Thread(() -&gt; bConsumer()).start();
        <span class="hljs-comment">// 生产者</span>
        producer();
    }
    <span class="hljs-comment">/**
     * 生产者
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">producer</span><span class="hljs-params">()</span> <span class="hljs-keyword">throws</span> InterruptedException </span>{
        Jedis jedis = <span class="hljs-keyword">new</span> Jedis(<span class="hljs-string">"127.0.0.1"</span>, <span class="hljs-number">6379</span>);
        <span class="hljs-comment">// 推送消息</span>
        jedis.lpush(<span class="hljs-string">"mq"</span>, <span class="hljs-string">"Hello, Java."</span>);
        Thread.sleep(<span class="hljs-number">1000</span>);
        jedis.lpush(<span class="hljs-string">"mq"</span>, <span class="hljs-string">"message 2."</span>);
        Thread.sleep(<span class="hljs-number">2000</span>);
        jedis.lpush(<span class="hljs-string">"mq"</span>, <span class="hljs-string">"message 3."</span>);
    }
    <span class="hljs-comment">/**
     * 消费者（阻塞版）
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">bConsumer</span><span class="hljs-params">()</span> </span>{
        Jedis jedis = <span class="hljs-keyword">new</span> Jedis(<span class="hljs-string">"127.0.0.1"</span>, <span class="hljs-number">6379</span>);
        <span class="hljs-keyword">while</span> (<span class="hljs-keyword">true</span>) {
            <span class="hljs-comment">// 阻塞读</span>
            <span class="hljs-keyword">for</span> (String item : jedis.brpop(<span class="hljs-number">0</span>,<span class="hljs-string">"mq"</span>)) {
                <span class="hljs-comment">// 读取到相关数据，进行业务处理</span>
                System.out.println(item);
            }
        }
    }
}
</code></pre>
<p>以上程序的运行结果是：</p>
<pre><code data-language="java" class="lang-java">接收到消息：Hello, Java.
</code></pre>
<p>以上代码是经过改良的，我们使用 brpop 替代 rpop 来读取最后一条消息，就可以解决 while 循环在没有数据的情况下，一直循环消耗系统资源的情况了。brpop 中的 b 是 blocking 的意思，表示阻塞读，也就是当队列没有数据时，它会进入休眠状态，当有数据进入队列之后，它才会“苏醒”过来执行读取任务，这样就可以解决 while 循环一直执行消耗系统资源的问题了。</p>
<h4>使用 Stream 实现消息队列</h4>
<p>在开始实现消息队列之前，我们必须先创建分组才行，因为消费者需要关联分组信息才能正常运行，具体实现代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">import</span> com.google.gson.Gson;
<span class="hljs-keyword">import</span> redis.clients.jedis.Jedis;
<span class="hljs-keyword">import</span> redis.clients.jedis.StreamEntry;
<span class="hljs-keyword">import</span> redis.clients.jedis.StreamEntryID;
<span class="hljs-keyword">import</span> utils.JedisUtils;
<span class="hljs-keyword">import</span> java.util.AbstractMap;
<span class="hljs-keyword">import</span> java.util.HashMap;
<span class="hljs-keyword">import</span> java.util.List;
<span class="hljs-keyword">import</span> java.util.Map;
<span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">StreamGroupExample</span> </span>{
    <span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> String _STREAM_KEY = <span class="hljs-string">"mq"</span>; <span class="hljs-comment">// 流 key</span>
    <span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> String _GROUP_NAME = <span class="hljs-string">"g1"</span>; <span class="hljs-comment">// 分组名称</span>
    <span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> String _CONSUMER_NAME = <span class="hljs-string">"c1"</span>; <span class="hljs-comment">// 消费者 1 的名称</span>
    <span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> String _CONSUMER2_NAME = <span class="hljs-string">"c2"</span>; <span class="hljs-comment">// 消费者 2 的名称</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> </span>{
        <span class="hljs-comment">// 生产者</span>
        producer();
        <span class="hljs-comment">// 创建消费组</span>
        createGroup(_STREAM_KEY, _GROUP_NAME);
        <span class="hljs-comment">// 消费者 1</span>
        <span class="hljs-keyword">new</span> Thread(() -&gt; consumer()).start();
        <span class="hljs-comment">// 消费者 2</span>
        <span class="hljs-keyword">new</span> Thread(() -&gt; consumer2()).start();
    }
    <span class="hljs-comment">/**
     * 创建消费分组
     * <span class="hljs-doctag">@param</span> stream    流 key
     * <span class="hljs-doctag">@param</span> groupName 分组名称
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">createGroup</span><span class="hljs-params">(String stream, String groupName)</span> </span>{
        Jedis jedis = JedisUtils.getJedis();
        jedis.xgroupCreate(stream, groupName, <span class="hljs-keyword">new</span> StreamEntryID(), <span class="hljs-keyword">true</span>);
    }
    <span class="hljs-comment">/**
     * 生产者
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">producer</span><span class="hljs-params">()</span> </span>{
        Jedis jedis = JedisUtils.getJedis();
        <span class="hljs-comment">// 添加消息 1</span>
        Map&lt;String, String&gt; map = <span class="hljs-keyword">new</span> HashMap&lt;&gt;();
        map.put(<span class="hljs-string">"data"</span>, <span class="hljs-string">"redis"</span>);
        StreamEntryID id = jedis.xadd(_STREAM_KEY, <span class="hljs-keyword">null</span>, map);
        System.out.println(<span class="hljs-string">"消息添加成功 ID："</span> + id);
        <span class="hljs-comment">// 添加消息 2</span>
        Map&lt;String, String&gt; map2 = <span class="hljs-keyword">new</span> HashMap&lt;&gt;();
        map2.put(<span class="hljs-string">"data"</span>, <span class="hljs-string">"java"</span>);
        StreamEntryID id2 = jedis.xadd(_STREAM_KEY, <span class="hljs-keyword">null</span>, map2);
        System.out.println(<span class="hljs-string">"消息添加成功 ID："</span> + id2);
    }
    <span class="hljs-comment">/**
     * 消费者 1
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">consumer</span><span class="hljs-params">()</span> </span>{
        Jedis jedis = JedisUtils.getJedis();
        <span class="hljs-comment">// 消费消息</span>
        <span class="hljs-keyword">while</span> (<span class="hljs-keyword">true</span>) {
            <span class="hljs-comment">// 读取消息</span>
            Map.Entry&lt;String, StreamEntryID&gt; entry = <span class="hljs-keyword">new</span> AbstractMap.SimpleImmutableEntry&lt;&gt;(_STREAM_KEY,
                    <span class="hljs-keyword">new</span> StreamEntryID().UNRECEIVED_ENTRY);
            <span class="hljs-comment">// 阻塞读取一条消息（最大阻塞时间120s）</span>
            List&lt;Map.Entry&lt;String, List&lt;StreamEntry&gt;&gt;&gt; list = jedis.xreadGroup(_GROUP_NAME, _CONSUMER_NAME, <span class="hljs-number">1</span>,
                    <span class="hljs-number">120</span> * <span class="hljs-number">1000</span>, <span class="hljs-keyword">true</span>, entry);
            <span class="hljs-keyword">if</span> (list != <span class="hljs-keyword">null</span> &amp;&amp; list.size() == <span class="hljs-number">1</span>) {
                <span class="hljs-comment">// 读取到消息</span>
                Map&lt;String, String&gt; content = list.get(<span class="hljs-number">0</span>).getValue().get(<span class="hljs-number">0</span>).getFields(); <span class="hljs-comment">// 消息内容</span>
                System.out.println(<span class="hljs-string">"Consumer 1 读取到消息 ID："</span> + list.get(<span class="hljs-number">0</span>).getValue().get(<span class="hljs-number">0</span>).getID() +
                        <span class="hljs-string">" 内容："</span> + <span class="hljs-keyword">new</span> Gson().toJson(content));
            }
        }
    }
    <span class="hljs-comment">/**
     * 消费者 2
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">consumer2</span><span class="hljs-params">()</span> </span>{
        Jedis jedis = JedisUtils.getJedis();
        <span class="hljs-comment">// 消费消息</span>
        <span class="hljs-keyword">while</span> (<span class="hljs-keyword">true</span>) {
            <span class="hljs-comment">// 读取消息</span>
            Map.Entry&lt;String, StreamEntryID&gt; entry = <span class="hljs-keyword">new</span> AbstractMap.SimpleImmutableEntry&lt;&gt;(_STREAM_KEY,
                    <span class="hljs-keyword">new</span> StreamEntryID().UNRECEIVED_ENTRY);
            <span class="hljs-comment">// 阻塞读取一条消息（最大阻塞时间120s）</span>
            List&lt;Map.Entry&lt;String, List&lt;StreamEntry&gt;&gt;&gt; list = jedis.xreadGroup(_GROUP_NAME, _CONSUMER2_NAME, <span class="hljs-number">1</span>,
                    <span class="hljs-number">120</span> * <span class="hljs-number">1000</span>, <span class="hljs-keyword">true</span>, entry);
            <span class="hljs-keyword">if</span> (list != <span class="hljs-keyword">null</span> &amp;&amp; list.size() == <span class="hljs-number">1</span>) {
                <span class="hljs-comment">// 读取到消息</span>
                Map&lt;String, String&gt; content = list.get(<span class="hljs-number">0</span>).getValue().get(<span class="hljs-number">0</span>).getFields(); <span class="hljs-comment">// 消息内容</span>
                System.out.println(<span class="hljs-string">"Consumer 2 读取到消息 ID："</span> + list.get(<span class="hljs-number">0</span>).getValue().get(<span class="hljs-number">0</span>).getID() +
                        <span class="hljs-string">" 内容："</span> + <span class="hljs-keyword">new</span> Gson().toJson(content));
            }
        }
    }
}
</code></pre>
<p>以上代码运行结果如下：</p>
<pre><code data-language="java" class="lang-java">消息添加成功 ID：<span class="hljs-number">1580971482344</span>-<span class="hljs-number">0</span>
消息添加成功 ID：<span class="hljs-number">1580971482415</span>-<span class="hljs-number">0</span>
Consumer <span class="hljs-number">1</span> 读取到消息 ID：<span class="hljs-number">1580971482344</span>-<span class="hljs-number">0</span> 内容：{<span class="hljs-string">"data"</span>:<span class="hljs-string">"redis"</span>}
Consumer <span class="hljs-number">2</span> 读取到消息 ID：<span class="hljs-number">1580971482415</span>-<span class="hljs-number">0</span> 内容：{<span class="hljs-string">"data"</span>:<span class="hljs-string">"java"</span>}
</code></pre>
<p>其中，jedis.xreadGroup() 方法的第五个参数 noAck 表示是否自动确认消息，如果设置 true 收到消息会自动确认 (ack) 消息，否则需要手动确认。</p>
<p>可以看出，同一个分组内的多个 consumer 会读取到不同消息，不同的 consumer 不会读取到分组内的同一条消息。</p>
<blockquote>
<p>小贴士：Jedis 框架要使用最新版，低版本 block 设置大于 0 时，会出现 bug，抛连接超时异常。</p>
</blockquote>
<h3>小结</h3>
<p>本课时我们讲了 Redis 中消息队列的四种实现方式：List 方式、ZSet 方式、发布订阅者模式、Stream 方式，其中发布订阅者模式不支持消息持久化、而其他三种方式支持持久化，并且 Stream 方式支持消费者确认。我们还使用 Jedis 框架完成了 List 和 Stream 的消息队列功能，需要注意的是在 List 中需要使用 brpop 来读取消息，而不是 rpop，这样可以解决没有任务时 ，while 一直循环浪费系统资源的问题。</p>

---

### 精选评论

##### **驰：
> Stream 如何手动确认消息呢

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; Streams 有 xack key group-key ID 可以用来确认消息。

##### **台：
> Stream流，会在消费者离线的时侯，消费端能记住消费到哪里了吗，还是服务端记录。他的原理是复制到多份数据，还是消费端记住消费进度呢

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 像这类功能都只可能是服务器端存储，不可能放在客户端存储的，它有一个消费确认编号，记录了未消费的信息。

