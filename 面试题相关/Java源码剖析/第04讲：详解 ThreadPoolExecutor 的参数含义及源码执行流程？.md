<p data-nodeid="837" class="">线程池是为了避免线程频繁的创建和销毁带来的性能消耗，而建立的一种池化技术，它是把已创建的线程放入“池”中，当有任务来临时就可以重用已有的线程，无需等待创建的过程，这样就可以有效提高程序的响应速度。但如果要说线程池的话一定离不开 ThreadPoolExecutor ，在阿里巴巴的《Java 开发手册》中是这样规定线程池的：</p>
<blockquote data-nodeid="838">
<p data-nodeid="839">线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式，这样的处理方式让写的读者更加明确线程池的运行规则，规避资源耗尽的风险。</p>
</blockquote>
<p data-nodeid="840">说明：Executors 返回的线程池对象的弊端如下：</p>
<ul data-nodeid="841">
<li data-nodeid="842">
<p data-nodeid="843">FixedThreadPool 和 SingleThreadPool：允许的请求队列长度为 Integer.MAX_VALUE，可能会堆积大量的请求，从而导致 OOM。</p>
</li>
<li data-nodeid="844">
<p data-nodeid="845">CachedThreadPool 和 ScheduledThreadPool：允许的创建线程数量为 Integer.MAX_VALUE，可能会创建大量的线程，从而导致 OOM。</p>
</li>
</ul>
<p data-nodeid="846">其实当我们去看 Executors 的源码会发现，Executors.newFixedThreadPool()、Executors.newSingleThreadExecutor() 和 Executors.newCachedThreadPool() 等方法的底层都是通过 ThreadPoolExecutor 实现的，所以本课时我们就重点来了解一下 ThreadPoolExecutor 的相关知识，比如它有哪些核心的参数？它是如何工作的？</p>
<h3 data-nodeid="847">典型回答</h3>
<p data-nodeid="848">ThreadPoolExecutor 的核心参数指的是它在构建时需要传递的参数，其构造方法如下所示：</p>
<pre class="lang-java" data-nodeid="849"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">ThreadPoolExecutor</span><span class="hljs-params">(<span class="hljs-keyword">int</span>&nbsp;corePoolSize,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;maximumPoolSize,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">long</span>&nbsp;keepAliveTime,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TimeUnit&nbsp;unit,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;BlockingQueue&lt;Runnable&gt;&nbsp;workQueue,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ThreadFactory&nbsp;threadFactory,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;RejectedExecutionHandler&nbsp;handler)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(corePoolSize&nbsp;&lt;&nbsp;<span class="hljs-number">0</span>&nbsp;||
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;maximumPoolSize&nbsp;必须大于&nbsp;0，且必须大于&nbsp;corePoolSize</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;maximumPoolSize&nbsp;&lt;=&nbsp;<span class="hljs-number">0</span>&nbsp;||
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;maximumPoolSize&nbsp;&lt;&nbsp;corePoolSize&nbsp;||
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;keepAliveTime&nbsp;&lt;&nbsp;<span class="hljs-number">0</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;IllegalArgumentException();
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(workQueue&nbsp;==&nbsp;<span class="hljs-keyword">null</span>&nbsp;||&nbsp;threadFactory&nbsp;==&nbsp;<span class="hljs-keyword">null</span>&nbsp;||&nbsp;handler&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;NullPointerException();
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.acc&nbsp;=&nbsp;System.getSecurityManager()&nbsp;==&nbsp;<span class="hljs-keyword">null</span>&nbsp;?
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">null</span>&nbsp;:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;AccessController.getContext();
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.corePoolSize&nbsp;=&nbsp;corePoolSize;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.maximumPoolSize&nbsp;=&nbsp;maximumPoolSize;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.workQueue&nbsp;=&nbsp;workQueue;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.keepAliveTime&nbsp;=&nbsp;unit.toNanos(keepAliveTime);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.threadFactory&nbsp;=&nbsp;threadFactory;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.handler&nbsp;=&nbsp;handler;
}
</code></pre>
<p data-nodeid="850">第 1 个参数：<strong data-nodeid="938">corePoolSize</strong> 表示线程池的常驻核心线程数。如果设置为 0，则表示在没有任何任务时，销毁线程池；如果大于 0，即使没有任务时也会保证线程池的线程数量等于此值。但需要注意，此值如果设置的比较小，则会频繁的创建和销毁线程（创建和销毁的原因会在本课时的下半部分讲到）；如果设置的比较大，则会浪费系统资源，所以开发者需要根据自己的实际业务来调整此值。</p>
<p data-nodeid="851">第 2 个参数：<strong data-nodeid="944">maximumPoolSize</strong> 表示线程池在任务最多时，最大可以创建的线程数。官方规定此值必须大于 0，也必须大于等于 corePoolSize，此值只有在任务比较多，且不能存放在任务队列时，才会用到。</p>
<p data-nodeid="852">第 3 个参数：<strong data-nodeid="950">keepAliveTime</strong> 表示线程的存活时间，当线程池空闲时并且超过了此时间，多余的线程就会销毁，直到线程池中的线程数量销毁的等于 corePoolSize 为止，如果 maximumPoolSize 等于 corePoolSize，那么线程池在空闲的时候也不会销毁任何线程。</p>
<p data-nodeid="853">第 4 个参数：<strong data-nodeid="956">unit</strong> 表示存活时间的单位，它是配合 keepAliveTime 参数共同使用的。</p>
<p data-nodeid="854">第 5 个参数：<strong data-nodeid="962">workQueue</strong> 表示线程池执行的任务队列，当线程池的所有线程都在处理任务时，如果来了新任务就会缓存到此任务队列中排队等待执行。</p>
<p data-nodeid="855">第 6 个参数：<strong data-nodeid="968">threadFactory</strong> 表示线程的创建工厂，此参数一般用的比较少，我们通常在创建线程池时不指定此参数，它会使用默认的线程创建工厂的方法来创建线程，源代码如下：</p>
<pre class="lang-java" data-nodeid="856"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">ThreadPoolExecutor</span><span class="hljs-params">(<span class="hljs-keyword">int</span>&nbsp;corePoolSize,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;maximumPoolSize,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">long</span>&nbsp;keepAliveTime,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TimeUnit&nbsp;unit,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;BlockingQueue&lt;Runnable&gt;&nbsp;workQueue)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;Executors.defaultThreadFactory()&nbsp;为默认的线程创建工厂</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>(corePoolSize,&nbsp;maximumPoolSize,&nbsp;keepAliveTime,&nbsp;unit,&nbsp;workQueue,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Executors.defaultThreadFactory(),&nbsp;defaultHandler);
}
<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">static</span>&nbsp;ThreadFactory&nbsp;<span class="hljs-title">defaultThreadFactory</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;DefaultThreadFactory();
}
<span class="hljs-comment">//&nbsp;默认的线程创建工厂，需要实现&nbsp;ThreadFactory&nbsp;接口</span>
<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">DefaultThreadFactory</span>&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">ThreadFactory</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;AtomicInteger&nbsp;poolNumber&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;AtomicInteger(<span class="hljs-number">1</span>);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;ThreadGroup&nbsp;group;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;AtomicInteger&nbsp;threadNumber&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;AtomicInteger(<span class="hljs-number">1</span>);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;String&nbsp;namePrefix;

&nbsp;&nbsp;&nbsp;&nbsp;DefaultThreadFactory()&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SecurityManager&nbsp;s&nbsp;=&nbsp;System.getSecurityManager();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;group&nbsp;=&nbsp;(s&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;?&nbsp;s.getThreadGroup()&nbsp;:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Thread.currentThread().getThreadGroup();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;namePrefix&nbsp;=&nbsp;<span class="hljs-string">"pool-"</span>&nbsp;+
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;poolNumber.getAndIncrement()&nbsp;+
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"-thread-"</span>;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;创建线程</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Thread&nbsp;<span class="hljs-title">newThread</span><span class="hljs-params">(Runnable&nbsp;r)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Thread&nbsp;t&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;Thread(group,&nbsp;r,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;namePrefix&nbsp;+&nbsp;threadNumber.getAndIncrement(),
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-number">0</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(t.isDaemon())&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;t.setDaemon(<span class="hljs-keyword">false</span>);&nbsp;<span class="hljs-comment">//&nbsp;创建一个非守护线程</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(t.getPriority()&nbsp;!=&nbsp;Thread.NORM_PRIORITY)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;t.setPriority(Thread.NORM_PRIORITY);&nbsp;<span class="hljs-comment">//&nbsp;线程优先级设置为默认值</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;t;
&nbsp;&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p data-nodeid="857">我们也可以自定义一个线程工厂，通过实现 ThreadFactory 接口来完成，这样就可以自定义线程的名称或线程执行的优先级了。</p>
<p data-nodeid="858">第 7 个参数：<strong data-nodeid="975">RejectedExecutionHandler</strong> 表示指定线程池的拒绝策略，当线程池的任务已经在缓存队列 workQueue 中存储满了之后，并且不能创建新的线程来执行此任务时，就会用到此拒绝策略，它属于一种限流保护的机制。</p>
<p data-nodeid="859">线程池的工作流程要从它的执行方法 <strong data-nodeid="981">execute()</strong> 说起，源码如下：</p>
<pre class="lang-java te-preview-highlight" data-nodeid="8588"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">execute</span><span class="hljs-params">(Runnable&nbsp;command)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(command&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;NullPointerException();
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;c&nbsp;=&nbsp;ctl.get();
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;当前工作的线程数小于核心线程数</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(workerCountOf(c)&nbsp;&lt;&nbsp;corePoolSize)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;创建新的线程执行此任务</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(addWorker(command,&nbsp;<span class="hljs-keyword">true</span>))
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;c&nbsp;=&nbsp;ctl.get();
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;检查线程池是否处于运行状态，如果是则把任务添加到队列</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(isRunning(c)&nbsp;&amp;&amp;&nbsp;workQueue.offer(command))&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;recheck&nbsp;=&nbsp;ctl.get();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;再次检查线程池是否处于运行状态，防止在第一次校验通过后线程池关闭</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果是非运行状态，则将刚加入队列的任务移除</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(!&nbsp;isRunning(recheck)&nbsp;&amp;&amp;&nbsp;remove(command))
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;reject(command);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果线程池的线程数为&nbsp;0&nbsp;时（当&nbsp;corePoolSize&nbsp;设置为&nbsp;0&nbsp;时会发生）</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;<span class="hljs-keyword">if</span>&nbsp;(workerCountOf(recheck)&nbsp;==&nbsp;<span class="hljs-number">0</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addWorker(<span class="hljs-keyword">null</span>,&nbsp;<span class="hljs-keyword">false</span>);&nbsp;<span class="hljs-comment">//&nbsp;新建线程执行任务</span>
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;核心线程都在忙且队列都已爆满，尝试新启动一个线程执行失败</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;<span class="hljs-keyword">if</span>&nbsp;(!addWorker(command,&nbsp;<span class="hljs-keyword">false</span>))&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;执行拒绝策略</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;reject(command);
}
</code></pre>



















<p data-nodeid="861">其中 addWorker(Runnable firstTask, boolean core) 方法的参数说明如下：</p>
<ul data-nodeid="862">
<li data-nodeid="863">
<p data-nodeid="864"><strong data-nodeid="987">firstTask</strong>，线程应首先运行的任务，如果没有则可以设置为 null；</p>
</li>
<li data-nodeid="865">
<p data-nodeid="866"><strong data-nodeid="992">core</strong>，判断是否可以创建线程的阀值（最大值），如果等于 true 则表示使用 corePoolSize 作为阀值，false 则表示使用 maximumPoolSize 作为阀值。</p>
</li>
</ul>
<h3 data-nodeid="867">考点分析</h3>
<p data-nodeid="868">本课时的这道面试题考察的是你对于线程池和 ThreadPoolExecutor 的掌握程度，也属于 Java 的基础知识，几乎所有的面试都会被问到，其中线程池任务执行的主要流程，可以参考以下流程图：</p>
<p data-nodeid="869"><img src="https://s0.lgstatic.com/i/image3/M01/78/50/Cgq2xl5zjxGAXOA-AABF0Dv8GMI518.png" alt="" data-nodeid="996"></p>
<p data-nodeid="870">与 ThreadPoolExecutor 相关的面试题还有以下几个：</p>
<ul data-nodeid="871">
<li data-nodeid="872">
<p data-nodeid="873">ThreadPoolExecutor 的执行方法有几种？它们有什么区别？</p>
</li>
<li data-nodeid="874">
<p data-nodeid="875">什么是线程的拒绝策略？</p>
</li>
<li data-nodeid="876">
<p data-nodeid="877">拒绝策略的分类有哪些？</p>
</li>
<li data-nodeid="878">
<p data-nodeid="879">如何自定义拒绝策略？</p>
</li>
<li data-nodeid="880">
<p data-nodeid="881">ThreadPoolExecutor 能不能实现扩展？如何实现扩展？</p>
</li>
</ul>
<h3 data-nodeid="882">知识扩展</h3>
<h4 data-nodeid="883">execute() VS submit()</h4>
<p data-nodeid="884">execute() 和 submit() 都是用来执行线程池任务的，它们最主要的区别是，submit() 方法可以接收线程池执行的返回值，而 execute() 不能接收返回值。</p>
<p data-nodeid="885">来看两个方法的具体使用：</p>
<pre class="lang-java" data-nodeid="886"><code data-language="java">ThreadPoolExecutor&nbsp;executor&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;ThreadPoolExecutor(<span class="hljs-number">2</span>,&nbsp;<span class="hljs-number">10</span>,&nbsp;<span class="hljs-number">10L</span>,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TimeUnit.SECONDS,&nbsp;<span class="hljs-keyword">new</span>&nbsp;LinkedBlockingQueue(<span class="hljs-number">20</span>));
<span class="hljs-comment">//&nbsp;execute&nbsp;使用</span>
executor.execute(<span class="hljs-keyword">new</span>&nbsp;Runnable()&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">run</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"Hello,&nbsp;execute."</span>);
&nbsp;&nbsp;&nbsp;&nbsp;}
});
<span class="hljs-comment">//&nbsp;submit&nbsp;使用</span>
Future&lt;String&gt;&nbsp;future&nbsp;=&nbsp;executor.submit(<span class="hljs-keyword">new</span>&nbsp;Callable&lt;String&gt;()&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;String&nbsp;<span class="hljs-title">call</span><span class="hljs-params">()</span>&nbsp;<span class="hljs-keyword">throws</span>&nbsp;Exception&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"Hello,&nbsp;submit."</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-string">"Success"</span>;
&nbsp;&nbsp;&nbsp;&nbsp;}
});
System.out.println(future.get());
</code></pre>
<p data-nodeid="887">以上程序执行结果如下：</p>
<pre class="lang-java" data-nodeid="888"><code data-language="java">Hello,&nbsp;submit.
Hello,&nbsp;execute.
Success
</code></pre>
<p data-nodeid="889">从以上结果可以看出 submit() 方法可以配合 Futrue 来接收线程执行的返回值。它们的另一个区别是 execute() 方法属于 Executor 接口的方法，而 submit() 方法则是属于 ExecutorService 接口的方法，它们的继承关系如下图所示：</p>
<p data-nodeid="890"><img src="https://s0.lgstatic.com/i/image3/M01/78/50/CgpOIF5zjxGAGu4zAAAsFgFaNvI005.png" alt="" data-nodeid="1010"></p>
<h4 data-nodeid="891">线程池的拒绝策略</h4>
<p data-nodeid="892">当线程池中的任务队列已经被存满，再有任务添加时会先判断当前线程池中的线程数是否大于等于线程池的最大值，如果是，则会触发线程池的拒绝策略。</p>
<p data-nodeid="893">Java 自带的拒绝策略有 4 种：</p>
<ul data-nodeid="894">
<li data-nodeid="895">
<p data-nodeid="896"><strong data-nodeid="1018">AbortPolicy</strong>，终止策略，线程池会抛出异常并终止执行，它是默认的拒绝策略；</p>
</li>
<li data-nodeid="897">
<p data-nodeid="898"><strong data-nodeid="1023">CallerRunsPolicy</strong>，把任务交给当前线程来执行；</p>
</li>
<li data-nodeid="899">
<p data-nodeid="900"><strong data-nodeid="1028">DiscardPolicy</strong>，忽略此任务（最新的任务）；</p>
</li>
<li data-nodeid="901">
<p data-nodeid="902"><strong data-nodeid="1033">DiscardOldestPolicy</strong>，忽略最早的任务（最先加入队列的任务）。</p>
</li>
</ul>
<p data-nodeid="903">例如，我们来演示一个 AbortPolicy 的拒绝策略，代码如下：</p>
<pre class="lang-java" data-nodeid="904"><code data-language="java">ThreadPoolExecutor&nbsp;executor&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;ThreadPoolExecutor(<span class="hljs-number">1</span>,&nbsp;<span class="hljs-number">3</span>,&nbsp;<span class="hljs-number">10</span>,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TimeUnit.SECONDS,&nbsp;<span class="hljs-keyword">new</span>&nbsp;LinkedBlockingQueue&lt;&gt;(<span class="hljs-number">2</span>),
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">new</span>&nbsp;ThreadPoolExecutor.AbortPolicy());&nbsp;<span class="hljs-comment">//&nbsp;添加&nbsp;AbortPolicy&nbsp;拒绝策略</span>
<span class="hljs-keyword">for</span>&nbsp;(<span class="hljs-keyword">int</span>&nbsp;i&nbsp;=&nbsp;<span class="hljs-number">0</span>;&nbsp;i&nbsp;&lt;&nbsp;<span class="hljs-number">6</span>;&nbsp;i++)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;executor.execute(()&nbsp;-&gt;&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(Thread.currentThread().getName());
&nbsp;&nbsp;&nbsp;&nbsp;});
}
</code></pre>
<p data-nodeid="905">以上程序的执行结果：</p>
<pre class="lang-java" data-nodeid="906"><code data-language="java">pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">3</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">2</span>
Exception&nbsp;in&nbsp;thread&nbsp;<span class="hljs-string">"main"</span>&nbsp;java.util.concurrent.RejectedExecutionException:&nbsp;Task&nbsp;com.lagou.interview.ThreadPoolExample$$Lambda$<span class="hljs-number">1</span>/<span class="hljs-number">1096979270</span>@<span class="hljs-number">448139f</span>0&nbsp;rejected&nbsp;from&nbsp;java.util.concurrent.ThreadPoolExecutor@<span class="hljs-number">7</span>cca494b[Running,&nbsp;pool&nbsp;size&nbsp;=&nbsp;<span class="hljs-number">3</span>,&nbsp;active&nbsp;threads&nbsp;=&nbsp;<span class="hljs-number">3</span>,&nbsp;queued&nbsp;tasks&nbsp;=&nbsp;<span class="hljs-number">2</span>,&nbsp;completed&nbsp;tasks&nbsp;=&nbsp;<span class="hljs-number">0</span>]
&nbsp;at&nbsp;java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:<span class="hljs-number">2063</span>)
&nbsp;at&nbsp;java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:<span class="hljs-number">830</span>)
&nbsp;at&nbsp;java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:<span class="hljs-number">1379</span>)
&nbsp;at&nbsp;com.lagou.interview.ThreadPoolExample.rejected(ThreadPoolExample.java:<span class="hljs-number">35</span>)
&nbsp;at&nbsp;com.lagou.interview.ThreadPoolExample.main(ThreadPoolExample.java:<span class="hljs-number">26</span>)
</code></pre>
<p data-nodeid="907">可以看出当第 6 个任务来的时候，线程池则执行了 AbortPolicy &nbsp;拒绝策略，抛出了异常。因为队列最多存储 2 个任务，最大可以创建 3 个线程来执行任务（2+3=5），所以当第 6 个任务来的时候，此线程池就“忙”不过来了。</p>
<h4 data-nodeid="908">自定义拒绝策略</h4>
<p data-nodeid="909">自定义拒绝策略只需要新建一个 RejectedExecutionHandler 对象，然后重写它的 rejectedExecution() 方法即可，如下代码所示：</p>
<pre class="lang-java" data-nodeid="910"><code data-language="java">ThreadPoolExecutor&nbsp;executor&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;ThreadPoolExecutor(<span class="hljs-number">1</span>,&nbsp;<span class="hljs-number">3</span>,&nbsp;<span class="hljs-number">10</span>,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TimeUnit.SECONDS,&nbsp;<span class="hljs-keyword">new</span>&nbsp;LinkedBlockingQueue&lt;&gt;(<span class="hljs-number">2</span>),
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">new</span>&nbsp;RejectedExecutionHandler()&nbsp;{&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;添加自定义拒绝策略</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">rejectedExecution</span><span class="hljs-params">(Runnable&nbsp;r,&nbsp;ThreadPoolExecutor&nbsp;executor)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;业务处理方法</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"执行自定义拒绝策略"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;});
<span class="hljs-keyword">for</span>&nbsp;(<span class="hljs-keyword">int</span>&nbsp;i&nbsp;=&nbsp;<span class="hljs-number">0</span>;&nbsp;i&nbsp;&lt;&nbsp;<span class="hljs-number">6</span>;&nbsp;i++)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;executor.execute(()&nbsp;-&gt;&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(Thread.currentThread().getName());
&nbsp;&nbsp;&nbsp;&nbsp;});
}
</code></pre>
<p data-nodeid="911">以上代码执行的结果如下：</p>
<pre class="lang-java" data-nodeid="912"><code data-language="java">执行自定义拒绝策略
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">2</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">3</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">2</span>
</code></pre>
<p data-nodeid="913">可以看出线程池执行了自定义的拒绝策略，我们可以在 rejectedExecution 中添加自己业务处理的代码。</p>
<h4 data-nodeid="914">ThreadPoolExecutor 扩展</h4>
<p data-nodeid="915">ThreadPoolExecutor 的扩展主要是通过重写它的 beforeExecute() 和 afterExecute() 方法实现的，我们可以在扩展方法中添加日志或者实现数据统计，比如统计线程的执行时间，如下代码所示：</p>
<pre class="lang-java" data-nodeid="916"><code data-language="java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">ThreadPoolExtend</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">main</span><span class="hljs-params">(String[]&nbsp;args)</span>&nbsp;<span class="hljs-keyword">throws</span>&nbsp;ExecutionException,&nbsp;InterruptedException&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;线程池扩展调用</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MyThreadPoolExecutor&nbsp;executor&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;MyThreadPoolExecutor(<span class="hljs-number">2</span>,&nbsp;<span class="hljs-number">4</span>,&nbsp;<span class="hljs-number">10</span>,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TimeUnit.SECONDS,&nbsp;<span class="hljs-keyword">new</span>&nbsp;LinkedBlockingQueue());
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">for</span>&nbsp;(<span class="hljs-keyword">int</span>&nbsp;i&nbsp;=&nbsp;<span class="hljs-number">0</span>;&nbsp;i&nbsp;&lt;&nbsp;<span class="hljs-number">3</span>;&nbsp;i++)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;executor.execute(()&nbsp;-&gt;&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Thread.currentThread().getName();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;});
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;<span class="hljs-comment">/**
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;线程池扩展
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">MyThreadPoolExecutor</span>&nbsp;<span class="hljs-keyword">extends</span>&nbsp;<span class="hljs-title">ThreadPoolExecutor</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;保存线程执行开始时间</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;ThreadLocal&lt;Long&gt;&nbsp;localTime&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;ThreadLocal&lt;&gt;();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">MyThreadPoolExecutor</span><span class="hljs-params">(<span class="hljs-keyword">int</span>&nbsp;corePoolSize,&nbsp;<span class="hljs-keyword">int</span>&nbsp;maximumPoolSize,&nbsp;<span class="hljs-keyword">long</span>&nbsp;keepAliveTime,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TimeUnit&nbsp;unit,&nbsp;BlockingQueue&lt;Runnable&gt;&nbsp;workQueue)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">super</span>(corePoolSize,&nbsp;maximumPoolSize,&nbsp;keepAliveTime,&nbsp;unit,&nbsp;workQueue);
}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">/**
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;开始执行之前
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@param</span>&nbsp;t&nbsp;线程
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@param</span>&nbsp;r&nbsp;任务
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">protected</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">beforeExecute</span><span class="hljs-params">(Thread&nbsp;t,&nbsp;Runnable&nbsp;r)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Long&nbsp;sTime&nbsp;=&nbsp;System.nanoTime();&nbsp;<span class="hljs-comment">//&nbsp;开始时间&nbsp;(单位：纳秒)</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;localTime.set(sTime);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(String.format(<span class="hljs-string">"%s&nbsp;|&nbsp;before&nbsp;|&nbsp;time=%s"</span>,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;t.getName(),&nbsp;sTime));
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">super</span>.beforeExecute(t,&nbsp;r);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">/**
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;执行完成之后
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@param</span>&nbsp;r&nbsp;任务
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@param</span>&nbsp;t&nbsp;抛出的异常
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">protected</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">afterExecute</span><span class="hljs-params">(Runnable&nbsp;r,&nbsp;Throwable&nbsp;t)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Long&nbsp;eTime&nbsp;=&nbsp;System.nanoTime();&nbsp;<span class="hljs-comment">//&nbsp;结束时间&nbsp;(单位：纳秒)</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Long&nbsp;totalTime&nbsp;=&nbsp;eTime&nbsp;-&nbsp;localTime.get();&nbsp;<span class="hljs-comment">//&nbsp;执行总时间</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(String.format(<span class="hljs-string">"%s&nbsp;|&nbsp;after&nbsp;|&nbsp;time=%s&nbsp;|&nbsp;耗时：%s&nbsp;毫秒"</span>,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Thread.currentThread().getName(),&nbsp;eTime,&nbsp;(totalTime&nbsp;/&nbsp;<span class="hljs-number">1000000.0</span>)));
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">super</span>.afterExecute(r,&nbsp;t);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p data-nodeid="917">以上程序的执行结果如下所示：</p>
<pre class="lang-java" data-nodeid="918"><code data-language="java">pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>&nbsp;|&nbsp;before&nbsp;|&nbsp;time=<span class="hljs-number">4570298843700</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">2</span>&nbsp;|&nbsp;before&nbsp;|&nbsp;time=<span class="hljs-number">4570298840000</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>&nbsp;|&nbsp;after&nbsp;|&nbsp;time=<span class="hljs-number">4570327059500</span>&nbsp;|&nbsp;耗时：<span class="hljs-number">28.2158</span>&nbsp;毫秒
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">2</span>&nbsp;|&nbsp;after&nbsp;|&nbsp;time=<span class="hljs-number">4570327138100</span>&nbsp;|&nbsp;耗时：<span class="hljs-number">28.2981</span>&nbsp;毫秒
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>&nbsp;|&nbsp;before&nbsp;|&nbsp;time=<span class="hljs-number">4570328467800</span>
pool-<span class="hljs-number">1</span>-thread-<span class="hljs-number">1</span>&nbsp;|&nbsp;after&nbsp;|&nbsp;time=<span class="hljs-number">4570328636800</span>&nbsp;|&nbsp;耗时：<span class="hljs-number">0.169</span>&nbsp;毫秒
</code></pre>
<h3 data-nodeid="919">小结</h3>
<p data-nodeid="920" class="">最后我们总结一下：线程池的使用必须要通过 ThreadPoolExecutor 的方式来创建，这样才可以更加明确线程池的运行规则，规避资源耗尽的风险。同时，也介绍了 ThreadPoolExecutor 的七大核心参数，包括核心线程数和最大线程数之间的区别，当线程池的任务队列没有可用空间且线程池的线程数量已经达到了最大线程数时，则会执行拒绝策略，Java 自动的拒绝策略有 4 种，用户也可以通过重写 rejectedExecution() 来自定义拒绝策略，我们还可以通过重写 beforeExecute() 和 afterExecute() 来实现 ThreadPoolExecutor 的扩展功能。</p>

---

### 精选评论

##### **靖：
> 精品课程，这波羊毛褥的值了😀😀

##### **福：
> 哇，很棒，纠正了我的一个认知错误，coresize = 3, 队列长度2，默认拒绝策略，我以为要3＊2 = 6个任务后才会抛异常。。。

##### **鹏：
> 核心线程数设置的过小会频繁创建和销毁线程，说会在下半部分解答，没有发现呀

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 就是参数说明部分，可以看下哦。

##### **琼：
> 第五个参数队列有疑问，到底是所有的线程数都满了才放队列，还是核心数满了才放队列？有的人说核心数满了放队列？

##### *镇：
> 线程池核心线程池设置为0时，表示在没有任何任务时，销毁线程池；这个销毁线程池的操作在源码那个地方啊

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 可以在 ThreadPoolExecutor 源码中搜索 shutdown 关键字来查看哈。

##### **5761：
> 老师你好，我想问一下线程池创建的线程执行的任务如果出了异常该如何进行处理？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 和其他异常一样的哈，根据实际情况决定 catch 如何处理即可。

##### **1438：
> 老师，你好，我想问下，AbortPolicy，终止策略，异常之后，如果执行完当前这在执行的线程，会恢复正常吗，后面又来了新的任务，会执行吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 终止策略的执行对象是以单条任务为单元的，所以当一条任务触发了终止策略之后，后面的任务是可以正常执行的。

##### **伟：
> 当终止条件满足触发终止策略后，是整个线程池都会终止吗？包括正在执行的任务的线程都终止吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 正在执行的任务不会终止，当拒绝策略使用默认的 AbortPolicy 时，当有任务执行不过来时，新增的这个任务会终止执行，其他的任务还会继续执行。

##### *炜：
> 引用：core，判断是否可以创建线程的阀值（最大值），如果等于 true 则表示使用 corePoolSize 作为阀值，false 则表示使用 maximumPoolSize 作为阀值问题：maximunPoolSize 表示最大线程数，且该值大于等于corePoolSize，那为什么true时使用corePoolSize，false时使用maximumPoolSize作为阈值？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; maximumPoolSize 是大于等于 corePoolSize，corePoolSize 只有当工作队列满了之后，线程池的线程数才会大于 corePoolSize；maximumPoolSize 的作用是如果队列中任务已满，并且当前线程个数小于maximumPoolSize，那么会创建新的线程来执行任务。

##### **泽：
> 值了讲的挺好。

##### **8335：
> 写的好棒 给个赞

##### *露：
> AbortPolicy，终止策略，线程池会抛出异常并终止执行，它是默认的拒绝策略；<div><br></div><div>是整个线程池都终止吗？包括正常执行的线程？</div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 不是的，文中有写，只有达到线程的终止条件之后才会触发线程池的终止策略。

##### *林：
> 想向您请教一下非核心线程的销毁方式，这块不是很明白，是通过中断回收的吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 非核心线程会在线程池空闲的时候回收

##### **明：
> <div><span style="font-size: 16.0125px;">表示线程池的常驻核心线程数。如果设置为 0，则表示在没有任何任务时，销毁线程池。<br>销毁线程池是什么意思？</span></div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 就是注销所有的线程

##### **生：
> 非常受用

