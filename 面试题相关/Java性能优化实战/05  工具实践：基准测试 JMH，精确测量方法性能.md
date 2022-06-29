<p data-nodeid="1693" class="">上一课时，我们了解到一些外部工具可以获取系统的性能数据。</p>
<p data-nodeid="1694">但有时候，我们想要测量某段具体代码的性能情况，这时经常会写一些统计执行时间的代码，这些代码穿插在我们的逻辑中，进行一些简单的计时运算。比如下面这几行：</p>
<pre class="lang-java" data-nodeid="1695"><code data-language="java"><span class="hljs-keyword">long</span> start = System.currentTimeMillis(); 
<span class="hljs-comment">//logic </span>
<span class="hljs-keyword">long</span> cost = System.currentTimeMillis() - start; 
System.out.println(<span class="hljs-string">"Logic cost : "</span> + cost);
</code></pre>
<p data-nodeid="1696">可惜的是，这段代码的统计结果，并不一定准确。举个例子来说，JVM 在执行时，会对一些代码块，或者一些频繁执行的逻辑，进行 JIT 编译和内联优化，在得到一个稳定的测试结果之前，需要先循环上万次进行预热。预热前和预热后的性能差别非常大。</p>
<p data-nodeid="1697">另外，从 01 课时我们就知道，评估性能，有很多的指标，如果这些指标数据，每次都要手工去算的话，那肯定是枯燥乏味且低效的。</p>
<h3 data-nodeid="1698">JMH—基准测试工具</h3>
<p data-nodeid="1699"><strong data-nodeid="1861">JMH（the Java Microbenchmark Harness）就是这样一个能做基准测试的工具</strong>。如果你通过 04 课时介绍的一系列外部工具，定位到了热点代码，要测试它的性能数据，评估改善情况，就可以交给 JMH。它的<strong data-nodeid="1862">测量精度非常高，可达纳秒级别。</strong></p>
<p data-nodeid="1700">JMH 已经在 JDK 12中被包含，其他版本的需要自行引入 maven，坐标如下：</p>
<pre class="lang-js" data-nodeid="1701"><code data-language="js">&lt;dependencies&gt; 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.openjdk.jmh<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>jmh-core<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">version</span>&gt;</span>1.23<span class="hljs-tag">&lt;/<span class="hljs-name">version</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span></span> 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.openjdk.jmh<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>jmh-generator-annprocess<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">version</span>&gt;</span>1.23<span class="hljs-tag">&lt;/<span class="hljs-name">version</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">scope</span>&gt;</span>provided<span class="hljs-tag">&lt;/<span class="hljs-name">scope</span>&gt;</span> 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span></span> 
&lt;/dependencies&gt;
</code></pre>
<p data-nodeid="1702">下面，我们介绍一下这个工具的使用。</p>
<p data-nodeid="1703">JMH 是一个 jar 包，它和单元测试框架 JUnit 非常像，可以通过注解进行一些基础配置。这部分配置有很多是可以通过 main 方法的 OptionsBuilder 进行设置的。</p>
<p data-nodeid="1704"><img src="https://s0.lgstatic.com/i/image/M00/38/FA/Ciqc1F8epk6ALUNZAABpIyGz37g324.png" alt="1.png" data-nodeid="1868"></p>
<p data-nodeid="1705">上图是一个典型的 JMH 程序执行的内容。通过开启多个进程，多个线程，先执行预热，然后执行迭代，最后汇总所有的测试数据进行分析。在执行前后，还可以根据粒度处理一些前置和后置操作。</p>
<p data-nodeid="1706">一段简单的 JMH 代码如下所示：</p>
<pre class="lang-java" data-nodeid="1707"><code data-language="java"><span class="hljs-meta">@BenchmarkMode(Mode.Throughput)</span> 
<span class="hljs-meta">@OutputTimeUnit(TimeUnit.MILLISECONDS)</span> 
<span class="hljs-meta">@State(Scope.Thread)</span> 
<span class="hljs-meta">@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)</span> 
<span class="hljs-meta">@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)</span> 
<span class="hljs-meta">@Fork(1)</span> 
<span class="hljs-meta">@Threads(2)</span> 
<span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">BenchmarkTest</span> </span>{ 
 &nbsp; &nbsp;<span class="hljs-meta">@Benchmark</span> 
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">long</span> <span class="hljs-title">shift</span><span class="hljs-params">()</span> </span>{ 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">long</span> t = <span class="hljs-number">455565655225562L</span>; 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">long</span> a = <span class="hljs-number">0</span>; 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">for</span> (<span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>; i &lt; <span class="hljs-number">1000</span>; i++) { 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;a = t &gt;&gt; <span class="hljs-number">30</span>; 
 &nbsp; &nbsp; &nbsp;  } 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> a; 
 &nbsp;  } 
 
 &nbsp; &nbsp;<span class="hljs-meta">@Benchmark</span> 
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">long</span> <span class="hljs-title">div</span><span class="hljs-params">()</span> </span>{ 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">long</span> t = <span class="hljs-number">455565655225562L</span>; 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">long</span> a = <span class="hljs-number">0</span>; 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">for</span> (<span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>; i &lt; <span class="hljs-number">1000</span>; i++) { 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;a = t / <span class="hljs-number">1024</span> / <span class="hljs-number">1024</span> / <span class="hljs-number">1024</span>; 
 &nbsp; &nbsp; &nbsp;  } 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> a; 
 &nbsp;  } 
 
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> <span class="hljs-keyword">throws</span> Exception </span>{ 
 &nbsp; &nbsp; &nbsp; &nbsp;Options opts = <span class="hljs-keyword">new</span> OptionsBuilder() 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .include(BenchmarkTest.class.getSimpleName()) 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .resultFormat(ResultFormatType.JSON) 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .build(); 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">new</span> Runner(opts).run(); 
 &nbsp;  } 
}
</code></pre>
<p data-nodeid="1708">下面，我们逐一介绍一下比较关键的注解和参数。</p>
<h3 data-nodeid="1709">关键注解</h3>
<h4 data-nodeid="1710">1. @Warmup</h4>
<p data-nodeid="1711">样例如下：</p>
<pre class="lang-java" data-nodeid="1712"><code data-language="java"><span class="hljs-meta">@Warmup( 
iterations = 5, 
time = 1, 
timeUnit = TimeUnit.SECONDS)</span>
</code></pre>
<p data-nodeid="2964" class="">我们不止一次提到预热 warmup 这个注解，可以用在类或者方法上，进行预热配置。可以看到，它有几个配置参数：</p>


<ul data-nodeid="8107">
<li data-nodeid="8108">
<p data-nodeid="8109">timeUnit：时间的单位，默认的单位是秒；</p>
</li>
<li data-nodeid="8110">
<p data-nodeid="8111">iterations：预热阶段的迭代数；</p>
</li>
<li data-nodeid="8112">
<p data-nodeid="8113" class="">time：每次预热的时间；</p>
</li>
<li data-nodeid="8114">
<p data-nodeid="8115">batchSize：批处理大小，指定了每次操作调用几次方法。</p>
</li>
</ul>






<p data-nodeid="1723">上面的注解，意思是对代码预热总计 5 秒（迭代 5 次，每次一秒）。预热过程的测试数据，是不记录测量结果的。</p>
<p data-nodeid="1724">我们可以看一下它执行的效果：</p>
<pre class="lang-dart" data-nodeid="1725"><code data-language="dart"># Warmup: <span class="hljs-number">3</span> iterations, <span class="hljs-number">1</span> s each 
# Warmup Iteration &nbsp; <span class="hljs-number">1</span>: <span class="hljs-number">0.281</span> ops/ns 
# Warmup Iteration &nbsp; <span class="hljs-number">2</span>: <span class="hljs-number">0.376</span> ops/ns 
# Warmup Iteration &nbsp; <span class="hljs-number">3</span>: <span class="hljs-number">0.483</span> ops/ns
</code></pre>
<p data-nodeid="1726">一般来说，基准测试都是针对比较小的、执行速度相对较快的代码块，这些代码有很大的可能性被 JIT 编译、内联，所以在编码时保持方法的精简，是一个好的习惯。具体优化过程，我们将在 18 课时介绍。</p>
<p data-nodeid="1727">说到预热，就不得不提一下在分布式环境下的服务预热。在对服务节点进行发布的时候，通常也会有预热过程，逐步放量到相应的服务节点，直到服务达到最优状态。如下图所示，负载均衡负责这个放量过程，一般是根据百分比进行放量。</p>
<p data-nodeid="1728"><img src="https://s0.lgstatic.com/i/image/M00/39/06/CgqCHl8epmWAWw_3AABS3CbQ8AE949.png" alt="2.png" data-nodeid="1888"></p>
<h4 data-nodeid="1729">2. @Measurement</h4>
<p data-nodeid="1730">样例如下：</p>
<pre class="lang-java" data-nodeid="1731"><code data-language="java"><span class="hljs-meta">@Measurement( 
iterations = 5,
time = 1,
timeUnit = TimeUnit.SECONDS)</span>
</code></pre>
<p data-nodeid="1732">Measurement 和 Warmup 的参数是一样的，不同于预热，它指的是真正的迭代次数。</p>
<p data-nodeid="1733">我们能够从日志中看到这个执行过程：</p>
<pre class="lang-dart" data-nodeid="1734"><code data-language="dart"># Measurement: <span class="hljs-number">5</span> iterations, <span class="hljs-number">1</span> s each 
Iteration &nbsp; <span class="hljs-number">1</span>: <span class="hljs-number">1646.000</span> ns/op 
Iteration &nbsp; <span class="hljs-number">2</span>: <span class="hljs-number">1243.000</span> ns/op 
Iteration &nbsp; <span class="hljs-number">3</span>: <span class="hljs-number">1273.000</span> ns/op 
Iteration &nbsp; <span class="hljs-number">4</span>: <span class="hljs-number">1395.000</span> ns/op 
Iteration &nbsp; <span class="hljs-number">5</span>: <span class="hljs-number">1423.000</span> ns/op
</code></pre>
<p data-nodeid="1735">虽然经过预热之后，代码都能表现出它的最优状态，但一般和实际应用场景还是有些出入。如果你的测试机器性能很高，或者你的测试机资源利用已经达到了极限，都会影响测试结果的数值。</p>
<p data-nodeid="1736">所以，通常情况下，我都会在测试时，给机器充足的资源，保持一个稳定的环境。在分析结果时，也会更加关注不同代码实现方式下的<strong data-nodeid="1901">性能差异</strong> ，而不是测试数据本身。</p>
<h4 data-nodeid="1737">3. @BenchmarkMode</h4>
<p data-nodeid="8966" class="">此注解用来指定基准测试类型，对应 Mode 选项，用来修饰类和方法都可以。这里的 value，是一个数组，可以配置多个统计维度。比如：</p>

<p data-nodeid="1739">@BenchmarkMode({Throughput,Mode.AverageTime})，统计的就是吞吐量和平均执行时间两个指标。</p>
<p data-nodeid="1740">所谓的模式，其实就是我们第 01 课时里说的一些指标，在 JMH 中，可以分为以下几种：</p>
<ul data-nodeid="1741">
<li data-nodeid="1742">
<p data-nodeid="1743"><strong data-nodeid="1912">Throughput：</strong> 整体吞吐量，比如 QPS，单位时间内的调用量等；</p>
</li>
<li data-nodeid="1744">
<p data-nodeid="1745"><strong data-nodeid="1917">AverageTime：</strong> 平均耗时，指的是每次执行的平均时间。如果这个值很小不好辨认，可以把统计的单位时间调小一点；</p>
</li>
<li data-nodeid="1746">
<p data-nodeid="1747"><strong data-nodeid="1922">SampleTime：</strong> 随机取样，这和我们在第一课时里聊到的 TP 值是一个概念；</p>
</li>
<li data-nodeid="1748">
<p data-nodeid="1749"><strong data-nodeid="1927">SingleShotTime：</strong> 如果你想要测试仅仅一次的性能，比如第一次初始化花了多长时间，就可以使用这个参数，其实和传统的 main 方法没有什么区别；</p>
</li>
<li data-nodeid="1750">
<p data-nodeid="1751"><strong data-nodeid="1932">All：</strong> 所有的指标，都算一遍，你可以设置成这个参数看下效果。</p>
</li>
</ul>
<p data-nodeid="1752">我们拿平均时间，看一下一个大体的执行结果：</p>
<pre class="lang-java" data-nodeid="1753"><code data-language="java">Result <span class="hljs-string">"com.github.xjjdog.tuning.BenchmarkTest.shift"</span>: 
 &nbsp;<span class="hljs-number">2.068</span> ±(<span class="hljs-number">99.9</span>%) <span class="hljs-number">0.038</span> ns/op [Average] 
  (min, avg, max) = (<span class="hljs-number">2.059</span>, <span class="hljs-number">2.068</span>, <span class="hljs-number">2.083</span>), stdev = <span class="hljs-number">0.010</span> 
 &nbsp;CI (<span class="hljs-number">99.9</span>%): [<span class="hljs-number">2.030</span>, <span class="hljs-number">2.106</span>] (assumes normal distribution)
</code></pre>
<p data-nodeid="1754">由于我们声明的时间单位是纳秒，本次 shift 方法的平均响应时间就是 2.068 纳秒。</p>
<p data-nodeid="1755">我们也可以看下最终的耗时时间：</p>
<pre class="lang-java" data-nodeid="1756"><code data-language="java">Benchmark &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Mode &nbsp;Cnt &nbsp;Score &nbsp; Error &nbsp;Units 
BenchmarkTest.div &nbsp; &nbsp;avgt &nbsp; &nbsp;<span class="hljs-number">5</span> &nbsp;<span class="hljs-number">2.072</span> ± <span class="hljs-number">0.053</span> &nbsp;ns/op 
BenchmarkTest.shift &nbsp;avgt &nbsp; &nbsp;<span class="hljs-number">5</span> &nbsp;<span class="hljs-number">2.068</span> ± <span class="hljs-number">0.038</span> &nbsp;ns/op
</code></pre>
<p data-nodeid="1757">由于是平均数，这里的 Error 值的是误差（或者波动）的意思。</p>
<p data-nodeid="1758">可以看到，在衡量这些指标的时候，都有一个时间维度，它就是通过 <strong data-nodeid="1942">@OutputTimeUnit</strong> 注解进行配置的。</p>
<p data-nodeid="1759">这个就比较简单了，它指明了基准测试结果的时间类型。可用于类或者方法上，一般选择秒、毫秒、微秒，纳秒那是针对的速度非常快的方法。</p>
<p data-nodeid="1760">举个例子，@BenchmarkMode(Mode.Throughput) 和 @OutputTimeUnit(TimeUnit.MILLISECONDS) 进行组合，代表的就是每毫秒的吞吐量。</p>
<p data-nodeid="1761">如下面的关于吞吐量的结果，就是以毫秒计算的：</p>
<pre class="lang-java" data-nodeid="1762"><code data-language="java">Benchmark &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Mode &nbsp;Cnt &nbsp; &nbsp; &nbsp; Score &nbsp; &nbsp; &nbsp; Error &nbsp; Units 
BenchmarkTest.div &nbsp; &nbsp;thrpt &nbsp; &nbsp;<span class="hljs-number">5</span> &nbsp;<span class="hljs-number">482999.685</span> ± &nbsp;<span class="hljs-number">6415.832</span> &nbsp;ops/ms 
BenchmarkTest.shift &nbsp;thrpt &nbsp; &nbsp;<span class="hljs-number">5</span> &nbsp;<span class="hljs-number">480599.263</span> ± <span class="hljs-number">20752.609</span> &nbsp;ops/ms
</code></pre>
<p data-nodeid="1763">OutputTimeUnit 注解同样可以修饰类或者方法，通过更改时间级别，可以获取更加易读的结果。</p>
<h4 data-nodeid="1764">4. @Fork</h4>
<p data-nodeid="1765">fork 的值一般设置成 1，表示只使用一个进程进行测试；如果这个数字大于 1，表示会启用新的进程进行测试；但如果设置成 0，程序依然会运行，不过是这样是在用户的 JVM 进程上运行的，可以看下下面的提示，但不推荐这么做。</p>
<pre class="lang-dart" data-nodeid="1766"><code data-language="dart"># Fork: N/A, test runs <span class="hljs-keyword">in</span> the host VM 
# *** WARNING: Non-forked runs may silently omit JVM options, mess up profilers, disable compiler hints, etc. *** 
# *** WARNING: Use non-forked runs only <span class="hljs-keyword">for</span> debugging purposes, not <span class="hljs-keyword">for</span> actual performance runs. ***
</code></pre>
<p data-nodeid="1767">那么 fork 到底是在进程还是线程环境里运行呢？</p>
<p data-nodeid="1768">我们追踪一下 JMH 的源码，发现每个 fork 进程是单独运行在 Proccess 进程里的，这样就可以做完全的环境隔离，避免交叉影响。</p>
<p data-nodeid="1769">它的输入输出流，通过 Socket 连接的模式，发送到我们的执行终端。</p>
<p data-nodeid="1770"><img src="https://s0.lgstatic.com/i/image/M00/38/FA/Ciqc1F8epneAFThuAABRpqRrEUw322.png" alt="3.png" data-nodeid="1956"></p>
<p data-nodeid="1771">在这里分享一个小技巧。其实 fork 注解有一个参数叫作 jvmArgsAppend，我们可以通过它传递一些 JVM 的参数。</p>
<pre class="lang-java" data-nodeid="1772"><code data-language="java"><span class="hljs-meta">@Fork(value = 3, jvmArgsAppend = {"-Xmx2048m", "-server", "-XX:+AggressiveOpts"})</span>
</code></pre>
<p data-nodeid="1773">在平常的测试中，也可以适当增加 fork 数，来减少测试的误差。</p>
<h4 data-nodeid="1774">5. @Threads</h4>
<p data-nodeid="1775">fork 是面向进程的，而 Threads 是面向线程的。指定了这个注解以后，将会开启并行测试。如果配置了 Threads.MAX，则使用和处理机器核数相同的线程数。</p>
<p data-nodeid="1776">这个和我们平常编码中的习惯也是相同的，并不是说开的线程越多越好。线程多了，操作系统就需要耗费更多的时间在上下文切换上，造成了整体性能的下降。</p>
<h4 data-nodeid="1777">6. @Group</h4>
<p data-nodeid="1778">@Group 注解只能加在方法上，用来把测试方法进行归类。如果你单个测试文件中方法比较多，或者需要将其归类，则可以使用这个注解。</p>
<p data-nodeid="1779">与之关联的 @GroupThreads 注解，会在这个归类的基础上，再进行一些线程方面的设置。这两个注解都很少使用，除非是非常大的性能测试案例。</p>
<h4 data-nodeid="1780">7. @State</h4>
<p data-nodeid="1781">@State 指定了在类中变量的作用范围，用于声明某个类是一个“状态”，可以用 Scope 参数用来表示该状态的共享范围。这个注解必须加在类上，否则提示无法运行。</p>
<p data-nodeid="1782">Scope 有如下三种值。</p>
<ul data-nodeid="1783">
<li data-nodeid="1784">
<p data-nodeid="1785"><strong data-nodeid="1978">Benchmark</strong> ：表示变量的作用范围是某个基准测试类。</p>
</li>
<li data-nodeid="1786">
<p data-nodeid="1787"><strong data-nodeid="1983">Thread</strong> ：每个线程一份副本，如果配置了 Threads 注解，则每个 Thread 都拥有一份变量，它们互不影响。</p>
</li>
<li data-nodeid="1788">
<p data-nodeid="1789"><strong data-nodeid="1988">Group</strong> ：联系上面的 @Group 注解，在同一个 Group 里，将会共享同一个变量实例。</p>
</li>
</ul>
<p data-nodeid="1790">在 JMHSample04DefaultState 测试文件中，演示了变量 x 的默认作用范围是 Thread，关键代码如下：</p>
<pre class="lang-java" data-nodeid="1791"><code data-language="java"><span class="hljs-meta">@State(Scope.Thread)</span> 
<span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">JMHSample_04_DefaultState</span> </span>{ 
 &nbsp; &nbsp;<span class="hljs-keyword">double</span> x = Math.PI; 
 &nbsp; &nbsp;<span class="hljs-meta">@Benchmark</span> 
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">measure</span><span class="hljs-params">()</span> </span>{ 
 &nbsp; &nbsp; &nbsp; &nbsp;x++; 
 &nbsp;  } 
}
</code></pre>
<h4 data-nodeid="1792">8. @Setup 和 @TearDown</h4>
<p data-nodeid="9814" class="">和单元测试框架 JUnit 类似，@Setup 用于基准测试前的初始化动作，@TearDown 用于基准测试后的动作，来做一些全局的配置。</p>

<p data-nodeid="1794">这两个注解，同样有一个 Level 值，标明了方法运行的时机，它有三个取值。</p>
<ul data-nodeid="1795">
<li data-nodeid="1796">
<p data-nodeid="1797"><strong data-nodeid="1999">Trial</strong> ：默认的级别，也就是 Benchmark 级别。</p>
</li>
<li data-nodeid="1798">
<p data-nodeid="1799"><strong data-nodeid="2004">Iteration</strong> ：每次迭代都会运行。</p>
</li>
<li data-nodeid="1800">
<p data-nodeid="1801"><strong data-nodeid="2009">Invocation</strong> ：每次方法调用都会运行，这个是粒度最细的。</p>
</li>
</ul>
<p data-nodeid="1802">如果你的初始化操作，是和方法相关的，那最好使用 Invocation 级别。但大多数场景是一些全局的资源，比如一个 Spring 的 DAO，那么就使用默认的 Trial，只初始化一次就可以。</p>
<h4 data-nodeid="1803">9. @Param</h4>
<p data-nodeid="1804">@Param 注解只能修饰字段，用来测试不同的参数，对程序性能的影响。配合 @State注解，可以同时制定这些参数的执行范围。</p>
<p data-nodeid="1805">代码样例如下：</p>
<pre class="lang-js" data-nodeid="1806"><code data-language="js">@BenchmarkMode(Mode.AverageTime) 
@OutputTimeUnit(TimeUnit.NANOSECONDS) 
@Warmup(iterations = <span class="hljs-number">5</span>, time = <span class="hljs-number">1</span>, timeUnit = TimeUnit.SECONDS) 
@Measurement(iterations = <span class="hljs-number">5</span>, time = <span class="hljs-number">1</span>, timeUnit = TimeUnit.SECONDS) 
@Fork(<span class="hljs-number">1</span>) 
@State(Scope.Benchmark) 
public <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">JMHSample_27_Params</span> </span>{ 
 &nbsp; &nbsp;@Param({<span class="hljs-string">"1"</span>, <span class="hljs-string">"31"</span>, <span class="hljs-string">"65"</span>, <span class="hljs-string">"101"</span>, <span class="hljs-string">"103"</span>}) 
 &nbsp; &nbsp;public int arg; 
 &nbsp; &nbsp;@Param({<span class="hljs-string">"0"</span>, <span class="hljs-string">"1"</span>, <span class="hljs-string">"2"</span>, <span class="hljs-string">"4"</span>, <span class="hljs-string">"8"</span>, <span class="hljs-string">"16"</span>, <span class="hljs-string">"32"</span>}) 
 &nbsp; &nbsp;public int certainty; 
 &nbsp; &nbsp;@Benchmark 
 &nbsp; &nbsp;public boolean bench() { 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> BigInteger.valueOf(arg).isProbablePrime(certainty); 
 &nbsp;  } 
 &nbsp; &nbsp;public <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> main(<span class="hljs-built_in">String</span>[] args) throws RunnerException { 
 &nbsp; &nbsp; &nbsp; &nbsp;Options opt = <span class="hljs-keyword">new</span> OptionsBuilder() 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .include(JMHSample_27_Params.class.getSimpleName()) 
<span class="hljs-comment">// &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .param("arg", "41", "42") // Use this to selectively constrain/override parameters </span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .build(); 
 
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">new</span> Runner(opt).run(); 
 &nbsp;  } 
}
</code></pre>
<p data-nodeid="1807">值得注意的是，如果你设置了非常多的参数，这些参数将执行多次，通常会运行很长时间。比如参数 1 M 个，参数 2 N 个，那么总共要执行 M*N 次。</p>
<p data-nodeid="1808">下面是一个执行结果的截图：</p>
<p data-nodeid="1809"><img src="https://s0.lgstatic.com/i/image/M00/38/CD/CgqCHl8ebZaAPtXOAAPe5vpFf_c784.png" alt="Drawing 3.png" data-nodeid="2022"></p>
<h4 data-nodeid="1810">10. @CompilerControl</h4>
<p data-nodeid="1811">这可以说是一个非常有用的功能了。</p>
<p data-nodeid="1812">Java 中方法调用的开销是比较大的，尤其是在调用量非常大的情况下。拿简单的getter/setter 方法来说，这种方法在 Java 代码中大量存在。我们在访问的时候，就需要创建相应的栈帧，访问到需要的字段后，再弹出栈帧，恢复原程序的执行。</p>
<p data-nodeid="13206" class="">如果能够把这些对象的访问和操作，纳入目标方法的调用范围之内，就少了一次方法调用，速度就能得到提升，这就是方法内联的概念。如下图所示，代码经过 JIT 编译之后，效率会有大的提升。</p>




<p data-nodeid="1814"><img src="https://s0.lgstatic.com/i/image/M00/38/FA/Ciqc1F8epoqAI9u2AAB4h_ABJWE362.png" alt="4.png" data-nodeid="2031"></p>
<p data-nodeid="1815">这个注解可以用在类或者方法上，能够控制方法的编译行为，常用的有 3 种模式：</p>
<p data-nodeid="1816">强制使用内联（INLINE），禁止使用内联（DONT_INLINE），甚至是禁止方法编译（EXCLUDE）等。</p>
<h3 data-nodeid="1817">将结果图形化</h3>
<p data-nodeid="1818">使用 JMH 测试的结果，可以二次加工，进行图形化展示。结合图表数据，更加直观。通过运行时，指定输出的格式文件，即可获得相应格式的性能测试结果。</p>
<p data-nodeid="1819">比如下面这行代码，就是指定输出 JSON 格式的数据：</p>
<pre class="lang-java" data-nodeid="1820"><code data-language="java">Options opt = <span class="hljs-keyword">new</span> OptionsBuilder() 
 &nbsp;  .resultFormat(ResultFormatType.JSON) 
 &nbsp;  .build();
</code></pre>
<h4 data-nodeid="1821">1. JMH 支持 5 种格式结果</h4>
<ul data-nodeid="1822">
<li data-nodeid="1823">
<p data-nodeid="1824"><strong data-nodeid="2046">TEXT</strong> 导出文本文件。</p>
</li>
<li data-nodeid="1825">
<p data-nodeid="1826"><strong data-nodeid="2051">CSV</strong> 导出 csv 格式文件。</p>
</li>
<li data-nodeid="1827">
<p data-nodeid="1828"><strong data-nodeid="2056">SCSV</strong> 导出 scsv 等格式的文件。</p>
</li>
<li data-nodeid="1829">
<p data-nodeid="1830"><strong data-nodeid="2061">JSON</strong> 导出成 json 文件。</p>
</li>
<li data-nodeid="1831">
<p data-nodeid="1832"><strong data-nodeid="2066">LATEX</strong> 导出到 latex，一种基于 ΤΕΧ 的排版系统。</p>
</li>
</ul>
<p data-nodeid="1833">一般来说，我们导出成 CSV 文件，直接在 Excel 中操作，生成如下相应的图形就可以了。</p>
<p data-nodeid="1834"><img src="https://s0.lgstatic.com/i/image/M00/38/C3/Ciqc1F8ebi2AdAAbAALlvsHgcKk925.png" alt="Drawing 5.png" data-nodeid="2070"></p>
<h4 data-nodeid="1835">2. 结果图形化制图工具</h4>
<p data-nodeid="1836"><strong data-nodeid="2077">JMH Visualizer</strong></p>
<p data-nodeid="1837">这里有一个开源的项目，通过导出 json 文件，上传至 <a href="https://jmh.morethan.io/" data-nodeid="2081">JMH Visualizer</a>（点击链接跳转），可得到简单的统计结果。由于很多操作需要鼠标悬浮在上面进行操作，所以个人认为它的展示方式并不是很好。</p>
<p data-nodeid="1838"><strong data-nodeid="2086">JMH Visual Chart</strong></p>
<p data-nodeid="1839">相比较而言， <a href="http://deepoove.com/jmh-visual-chart" data-nodeid="2090">JMH Visual Chart</a>（点击链接跳转）这个工具，就相对直观一些。</p>
<p data-nodeid="1840"><img src="https://s0.lgstatic.com/i/image/M00/38/CE/CgqCHl8ebkmAbujsAAHK-g94ooM905.png" alt="Drawing 6.png" data-nodeid="2094"></p>
<p data-nodeid="1841"><strong data-nodeid="2098">meta-chart</strong></p>
<p data-nodeid="1842">一个通用的 <a href="https://www.meta-chart.com/" data-nodeid="2102">在线图表生成器</a>（点击链接跳转），导出 CSV 文件后，做适当处理，即可导出精美图像。</p>
<p data-nodeid="1843"><img src="https://s0.lgstatic.com/i/image/M00/38/CE/CgqCHl8eboKAHRe8AAGSfMVOXxw934.png" alt="Drawing 7.png" data-nodeid="2106"></p>
<p data-nodeid="1844">像 Jenkins 等一些持续集成工具，也提供了相应的插件，用来直接显示这些测试结果。</p>
<h3 data-nodeid="1845">小结</h3>
<p data-nodeid="1846">本课时主要介绍了 基准测试工具— JMH，官方的 JMH 有非常丰富的示例，比如伪共享（FalseSharing）的影响等高级话题。我已经把它放在了 <a href="https://gitee.com/xjjdog/tuning-lagou-res" data-nodeid="2112">Gitee</a>（点击链接跳转）上，你可以将其导入至 Idea 编辑器进行测试。</p>
<p data-nodeid="1847">JMH 这个工具非常好用，它可以使用确切的测试数据，来支持我们的分析结果。一般情况下，如果定位到热点代码，就需要使用基准测试工具进行专项优化，直到性能有了显著的提升。</p>
<p data-nodeid="1848" class="">接下来的课程，将涉及对一些性能问题细节的验证，也会使用 JMH 进行进一步的分析。</p>

---

### 精选评论

##### **7540：
> 有源码不

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; https://gitee.com/xjjdog/tuning-lagou-res

##### **方：
> 使用JMH的前提是定位到热点代码，如何定位热点代码？热点代码的特点有哪些？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 并不仅仅是热点代码，执行的非常慢的接口也可以进行着重优化。这个问题非常好，这就是公司内监控平台和APM平台的重要性。目前比较主流的做法有类似Skywalking的探针（拉钩有课），也有大公司使用Spark Stream做实时的流式分析，实时输出方法调用的耗时和性能指标。如果没有这些工具，就只能靠业务感觉了。

##### **俊：
> 我将JMH集成到公司项目，启动测试，fork线程启动失败，显示文件名或扩展名过长

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 专门做了一个512字符长的类试了一下，没有发现这个问题

##### *苏：
> 老师,有没有别的方法可以测试接口响应速度的,或者方法

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 对于http，单个请求可以使用curl工具。
”time curl $url>/dev/null“
统计类的可以用wrk或者jmeter

对于java调用，arthas或者skywalking之类的探针apm都可去做。

##### **7718：
> 期待实战课程

