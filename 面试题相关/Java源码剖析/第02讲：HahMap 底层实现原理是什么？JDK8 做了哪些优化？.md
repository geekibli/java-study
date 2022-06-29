<p data-nodeid="2765" class="">HashMap 是使用频率最高的类型之一，同时也是面试经常被问到的问题之一，这是因为 HashMap 的知识点有很多，同时它又属于 Java 基础知识的一部分，因此在面试中经常被问到。</p>
<p data-nodeid="2766">本课时的面试题是，HashMap 底层是如何实现的？在 JDK 1.8 中它都做了哪些优化？</p>
<h3 data-nodeid="2767">典型回答</h3>
<p data-nodeid="2768">在 JDK 1.7 中 HashMap 是以数组加链表的形式组成的，JDK 1.8 之后新增了红黑树的组成结构，当链表大于 8 并且容量大于 64 时，链表结构会转换成红黑树结构，它的组成结构如下图所示：<br>
<img src="https://s0.lgstatic.com/i/image3/M01/73/D9/Cgq2xl5rDYmAM-0hAABv6sMsyOQ867.png" alt="" data-nodeid="2843"></p>
<p data-nodeid="2769">数组中的元素我们称之为哈希桶，它的定义如下：</p>
<pre class="lang-java" data-nodeid="2770"><code data-language="java"><span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Node</span>&lt;<span class="hljs-title">K</span>,<span class="hljs-title">V</span>&gt;&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">Map</span>.<span class="hljs-title">Entry</span>&lt;<span class="hljs-title">K</span>,<span class="hljs-title">V</span>&gt;&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;hash;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">final</span>&nbsp;K&nbsp;key;
&nbsp;&nbsp;&nbsp;&nbsp;V&nbsp;value;
&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;&nbsp;next;

&nbsp;&nbsp;&nbsp;&nbsp;Node(<span class="hljs-keyword">int</span>&nbsp;hash,&nbsp;K&nbsp;key,&nbsp;V&nbsp;value,&nbsp;Node&lt;K,V&gt;&nbsp;next)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.hash&nbsp;=&nbsp;hash;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.key&nbsp;=&nbsp;key;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.value&nbsp;=&nbsp;value;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.next&nbsp;=&nbsp;next;
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;K&nbsp;<span class="hljs-title">getKey</span><span class="hljs-params">()</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>{&nbsp;<span class="hljs-keyword">return</span>&nbsp;key;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;V&nbsp;<span class="hljs-title">getValue</span><span class="hljs-params">()</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>{&nbsp;<span class="hljs-keyword">return</span>&nbsp;value;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;String&nbsp;<span class="hljs-title">toString</span><span class="hljs-params">()</span>&nbsp;</span>{&nbsp;<span class="hljs-keyword">return</span>&nbsp;key&nbsp;+&nbsp;<span class="hljs-string">"="</span>&nbsp;+&nbsp;value;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;<span class="hljs-title">hashCode</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;Objects.hashCode(key)&nbsp;^&nbsp;Objects.hashCode(value);
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;V&nbsp;<span class="hljs-title">setValue</span><span class="hljs-params">(V&nbsp;newValue)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;V&nbsp;oldValue&nbsp;=&nbsp;value;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value&nbsp;=&nbsp;newValue;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;oldValue;
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;<span class="hljs-title">equals</span><span class="hljs-params">(Object&nbsp;o)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(o&nbsp;==&nbsp;<span class="hljs-keyword">this</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">true</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(o&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;Map.Entry)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Map.Entry&lt;?,?&gt;&nbsp;e&nbsp;=&nbsp;(Map.Entry&lt;?,?&gt;)o;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(Objects.equals(key,&nbsp;e.getKey())&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Objects.equals(value,&nbsp;e.getValue()))
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">true</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">false</span>;
&nbsp;&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p data-nodeid="2771">可以看出每个哈希桶中包含了四个字段：hash、key、value、next，其中 next 表示链表的下一个节点。</p>
<p data-nodeid="2772">JDK 1.8 之所以添加红黑树是因为一旦链表过长，会严重影响 HashMap 的性能，而红黑树具有快速增删改查的特点，这样就可以有效的解决链表过长时操作比较慢的问题。</p>
<h3 data-nodeid="2773">考点分析</h3>
<p data-nodeid="2774">上面大体介绍了 HashMap 的组成结构，但面试官想要知道的远远不止这些，和 HashMap 相关的面试题还有以下几个：</p>
<ul data-nodeid="2775">
<li data-nodeid="2776">
<p data-nodeid="2777">JDK 1.8 HashMap 扩容时做了哪些优化？</p>
</li>
<li data-nodeid="2778">
<p data-nodeid="2779">加载因子为什么是 0.75？</p>
</li>
<li data-nodeid="2780">
<p data-nodeid="2781">当有哈希冲突时，HashMap 是如何查找并确认元素的？</p>
</li>
<li data-nodeid="2782">
<p data-nodeid="2783">HashMap 源码中有哪些重要的方法？</p>
</li>
<li data-nodeid="2784">
<p data-nodeid="2785">HashMap 是如何导致死循环的？</p>
</li>
</ul>
<h3 data-nodeid="2786">知识扩展</h3>
<h4 data-nodeid="2787">1.HashMap 源码分析</h4>
<blockquote data-nodeid="2788">
<p data-nodeid="2789">声明：本系列课程在未做特殊说明的情况下，都是以目前主流的 JDK 版本 1.8 为例来进行源码分析的。</p>
</blockquote>
<p data-nodeid="2790">HashMap 源码中包含了以下几个属性：</p>
<pre class="lang-java" data-nodeid="2791"><code data-language="java"><span class="hljs-comment">//&nbsp;HashMap&nbsp;初始化长度</span>
<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;DEFAULT_INITIAL_CAPACITY&nbsp;=&nbsp;<span class="hljs-number">1</span>&nbsp;&lt;&lt;&nbsp;<span class="hljs-number">4</span>;&nbsp;<span class="hljs-comment">//&nbsp;aka&nbsp;16</span>

<span class="hljs-comment">//&nbsp;HashMap&nbsp;最大长度</span>
<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;MAXIMUM_CAPACITY&nbsp;=&nbsp;<span class="hljs-number">1</span>&nbsp;&lt;&lt;&nbsp;<span class="hljs-number">30</span>;&nbsp;<span class="hljs-comment">//&nbsp;1073741824</span>

<span class="hljs-comment">//&nbsp;默认的加载因子&nbsp;(扩容因子)</span>
<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">float</span>&nbsp;DEFAULT_LOAD_FACTOR&nbsp;=&nbsp;<span class="hljs-number">0.75f</span>;

<span class="hljs-comment">//&nbsp;当链表长度大于此值且容量大于&nbsp;64&nbsp;时</span>
<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;TREEIFY_THRESHOLD&nbsp;=&nbsp;<span class="hljs-number">8</span>;

<span class="hljs-comment">//&nbsp;转换链表的临界值，当元素小于此值时，会将红黑树结构转换成链表结构</span>
<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;UNTREEIFY_THRESHOLD&nbsp;=&nbsp;<span class="hljs-number">6</span>;

<span class="hljs-comment">//&nbsp;最小树容量</span>
<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;MIN_TREEIFY_CAPACITY&nbsp;=
</code></pre>
<p data-nodeid="2792"><strong data-nodeid="2861">什么是加载因子？加载因子为什么是 0.75？</strong></p>
<p data-nodeid="2793">加载因子也叫扩容因子或负载因子，用来判断什么时候进行扩容的，假如加载因子是 0.5，HashMap 的初始化容量是 16，那么当 HashMap 中有 16*0.5=8 个元素时，HashMap 就会进行扩容。</p>
<p data-nodeid="2794">那加载因子为什么是 0.75 而不是 0.5 或者 1.0 呢？</p>
<p data-nodeid="2795">这其实是出于容量和性能之间平衡的结果：</p>
<ul data-nodeid="2796">
<li data-nodeid="2797">
<p data-nodeid="2798">当加载因子设置比较大的时候，扩容的门槛就被提高了，扩容发生的频率比较低，占用的空间会比较小，但此时发生 Hash 冲突的几率就会提升，因此需要更复杂的数据结构来存储元素，这样对元素的操作时间就会增加，运行效率也会因此降低；</p>
</li>
<li data-nodeid="2799">
<p data-nodeid="2800">而当加载因子值比较小的时候，扩容的门槛会比较低，因此会占用更多的空间，此时元素的存储就比较稀疏，发生哈希冲突的可能性就比较小，因此操作性能会比较高。</p>
</li>
</ul>
<p data-nodeid="2801">所以综合了以上情况就取了一个 0.5 到 1.0 的平均数 0.75 作为加载因子。</p>
<p data-nodeid="2802">HashMap 源码中三个重要方法：<strong data-nodeid="2879">查询、新增</strong>和<strong data-nodeid="2880">数据扩容</strong>。</p>
<p data-nodeid="2803">先来看<strong data-nodeid="2886">查询</strong>源码：</p>
<pre class="lang-java" data-nodeid="2804"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;V&nbsp;<span class="hljs-title">get</span><span class="hljs-params">(Object&nbsp;key)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;对&nbsp;key&nbsp;进行哈希操作</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;(e&nbsp;=&nbsp;getNode(hash(key),&nbsp;key))&nbsp;==&nbsp;<span class="hljs-keyword">null</span>&nbsp;?&nbsp;<span class="hljs-keyword">null</span>&nbsp;:&nbsp;e.value;
}
<span class="hljs-function"><span class="hljs-keyword">final</span>&nbsp;Node&lt;K,V&gt;&nbsp;<span class="hljs-title">getNode</span><span class="hljs-params">(<span class="hljs-keyword">int</span>&nbsp;hash,&nbsp;Object&nbsp;key)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;[]&nbsp;tab;&nbsp;Node&lt;K,V&gt;&nbsp;first,&nbsp;e;&nbsp;<span class="hljs-keyword">int</span>&nbsp;n;&nbsp;K&nbsp;k;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;非空判断</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;((tab&nbsp;=&nbsp;table)&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>&nbsp;&amp;&amp;&nbsp;(n&nbsp;=&nbsp;tab.length)&nbsp;&gt;&nbsp;<span class="hljs-number">0</span>&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(first&nbsp;=&nbsp;tab[(n&nbsp;-&nbsp;<span class="hljs-number">1</span>)&nbsp;&amp;&nbsp;hash])&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;判断第一个元素是否是要查询的元素</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(first.hash&nbsp;==&nbsp;hash&nbsp;&amp;&amp;&nbsp;<span class="hljs-comment">//&nbsp;always&nbsp;check&nbsp;first&nbsp;node</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((k&nbsp;=&nbsp;first.key)&nbsp;==&nbsp;key&nbsp;||&nbsp;(key&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>&nbsp;&amp;&amp;&nbsp;key.equals(k))))
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;first;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;下一个节点非空判断</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;((e&nbsp;=&nbsp;first.next)&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果第一节点是树结构，则使用&nbsp;getTreeNode&nbsp;直接获取相应的数据</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(first&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;TreeNode)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;((TreeNode&lt;K,V&gt;)first).getTreeNode(hash,&nbsp;key);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">do</span>&nbsp;{&nbsp;<span class="hljs-comment">//&nbsp;非树结构，循环节点判断</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;hash&nbsp;相等并且&nbsp;key&nbsp;相同，则返回此节点</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(e.hash&nbsp;==&nbsp;hash&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((k&nbsp;=&nbsp;e.key)&nbsp;==&nbsp;key&nbsp;||&nbsp;(key&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>&nbsp;&amp;&amp;&nbsp;key.equals(k))))
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">while</span>&nbsp;((e&nbsp;=&nbsp;e.next)&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">null</span>;
}
</code></pre>
<p data-nodeid="2805">从以上源码可以看出，当哈希冲突时我们需要通过判断 key 值是否相等，才能确认此元素是不是我们想要的元素。</p>
<p data-nodeid="2806">HashMap 第二个重要方法：<strong data-nodeid="2893">新增方法</strong>，源码如下：</p>
<pre class="lang-java" data-nodeid="2807"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;V&nbsp;<span class="hljs-title">put</span><span class="hljs-params">(K&nbsp;key,&nbsp;V&nbsp;value)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;对&nbsp;key&nbsp;进行哈希操作</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;putVal(hash(key),&nbsp;key,&nbsp;value,&nbsp;<span class="hljs-keyword">false</span>,&nbsp;<span class="hljs-keyword">true</span>);
}
<span class="hljs-function"><span class="hljs-keyword">final</span>&nbsp;V&nbsp;<span class="hljs-title">putVal</span><span class="hljs-params">(<span class="hljs-keyword">int</span>&nbsp;hash,&nbsp;K&nbsp;key,&nbsp;V&nbsp;value,&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;onlyIfAbsent,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;evict)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;[]&nbsp;tab;&nbsp;Node&lt;K,V&gt;&nbsp;p;&nbsp;<span class="hljs-keyword">int</span>&nbsp;n,&nbsp;i;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;哈希表为空则创建表</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;((tab&nbsp;=&nbsp;table)&nbsp;==&nbsp;<span class="hljs-keyword">null</span>&nbsp;||&nbsp;(n&nbsp;=&nbsp;tab.length)&nbsp;==&nbsp;<span class="hljs-number">0</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;n&nbsp;=&nbsp;(tab&nbsp;=&nbsp;resize()).length;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;根据&nbsp;key&nbsp;的哈希值计算出要插入的数组索引&nbsp;i</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;((p&nbsp;=&nbsp;tab[i&nbsp;=&nbsp;(n&nbsp;-&nbsp;<span class="hljs-number">1</span>)&nbsp;&amp;&nbsp;hash])&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果&nbsp;table[i]&nbsp;等于&nbsp;null，则直接插入</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tab[i]&nbsp;=&nbsp;newNode(hash,&nbsp;key,&nbsp;value,&nbsp;<span class="hljs-keyword">null</span>);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;&nbsp;e;&nbsp;K&nbsp;k;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果&nbsp;key&nbsp;已经存在了，直接覆盖&nbsp;value</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(p.hash&nbsp;==&nbsp;hash&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((k&nbsp;=&nbsp;p.key)&nbsp;==&nbsp;key&nbsp;||&nbsp;(key&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>&nbsp;&amp;&amp;&nbsp;key.equals(k))))
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e&nbsp;=&nbsp;p;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果&nbsp;key&nbsp;不存在，判断是否为红黑树</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;<span class="hljs-keyword">if</span>&nbsp;(p&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;TreeNode)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;红黑树直接插入键值对</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e&nbsp;=&nbsp;((TreeNode&lt;K,V&gt;)p).putTreeVal(<span class="hljs-keyword">this</span>,&nbsp;tab,&nbsp;hash,&nbsp;key,&nbsp;value);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;为链表结构，循环准备插入</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">for</span>&nbsp;(<span class="hljs-keyword">int</span>&nbsp;binCount&nbsp;=&nbsp;<span class="hljs-number">0</span>;&nbsp;;&nbsp;++binCount)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;下一个元素为空时</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;((e&nbsp;=&nbsp;p.next)&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;p.next&nbsp;=&nbsp;newNode(hash,&nbsp;key,&nbsp;value,&nbsp;<span class="hljs-keyword">null</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;转换为红黑树进行处理</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(binCount&nbsp;&gt;=&nbsp;TREEIFY_THRESHOLD&nbsp;-&nbsp;<span class="hljs-number">1</span>)&nbsp;<span class="hljs-comment">//&nbsp;-1&nbsp;for&nbsp;1st</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;treeifyBin(tab,&nbsp;hash);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">break</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;&nbsp;key&nbsp;已经存在直接覆盖&nbsp;value</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(e.hash&nbsp;==&nbsp;hash&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((k&nbsp;=&nbsp;e.key)&nbsp;==&nbsp;key&nbsp;||&nbsp;(key&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>&nbsp;&amp;&amp;&nbsp;key.equals(k))))
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">break</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;p&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(e&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{&nbsp;<span class="hljs-comment">//&nbsp;existing&nbsp;mapping&nbsp;for&nbsp;key</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;V&nbsp;oldValue&nbsp;=&nbsp;e.value;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(!onlyIfAbsent&nbsp;||&nbsp;oldValue&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e.value&nbsp;=&nbsp;value;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;afterNodeAccess(e);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;oldValue;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;++modCount;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;超过最大容量，扩容</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(++size&nbsp;&gt;&nbsp;threshold)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;resize();
&nbsp;&nbsp;&nbsp;&nbsp;afterNodeInsertion(evict);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">null</span>;
}
</code></pre>
<p data-nodeid="2808">新增方法的执行流程，如下图所示：</p>
<p data-nodeid="2809"><img src="https://s0.lgstatic.com/i/image3/M01/73/D9/CgpOIF5rDYmATP43AAB3coc0R64799.png" alt="" data-nodeid="2896"></p>
<p data-nodeid="2810">HashMap 第三个重要的方法是<strong data-nodeid="2902">扩容方法</strong>，源码如下：</p>
<pre class="lang-java" data-nodeid="2811"><code data-language="java"><span class="hljs-keyword">final</span>&nbsp;Node&lt;K,V&gt;[]&nbsp;resize()&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;扩容前的数组</span>
&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;[]&nbsp;oldTab&nbsp;=&nbsp;table;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;扩容前的数组的大小和阈值</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;oldCap&nbsp;=&nbsp;(oldTab&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)&nbsp;?&nbsp;<span class="hljs-number">0</span>&nbsp;:&nbsp;oldTab.length;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;oldThr&nbsp;=&nbsp;threshold;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;预定义新数组的大小和阈值</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;newCap,&nbsp;newThr&nbsp;=&nbsp;<span class="hljs-number">0</span>;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(oldCap&nbsp;&gt;&nbsp;<span class="hljs-number">0</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;超过最大值就不再扩容了</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(oldCap&nbsp;&gt;=&nbsp;MAXIMUM_CAPACITY)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;threshold&nbsp;=&nbsp;Integer.MAX_VALUE;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;oldTab;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;扩大容量为当前容量的两倍，但不能超过&nbsp;MAXIMUM_CAPACITY</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;<span class="hljs-keyword">if</span>&nbsp;((newCap&nbsp;=&nbsp;oldCap&nbsp;&lt;&lt;&nbsp;<span class="hljs-number">1</span>)&nbsp;&lt;&nbsp;MAXIMUM_CAPACITY&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;oldCap&nbsp;&gt;=&nbsp;DEFAULT_INITIAL_CAPACITY)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newThr&nbsp;=&nbsp;oldThr&nbsp;&lt;&lt;&nbsp;<span class="hljs-number">1</span>;&nbsp;<span class="hljs-comment">//&nbsp;double&nbsp;threshold</span>
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;当前数组没有数据，使用初始化的值</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;<span class="hljs-keyword">if</span>&nbsp;(oldThr&nbsp;&gt;&nbsp;<span class="hljs-number">0</span>)&nbsp;<span class="hljs-comment">//&nbsp;initial&nbsp;capacity&nbsp;was&nbsp;placed&nbsp;in&nbsp;threshold</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newCap&nbsp;=&nbsp;oldThr;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;{&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;zero&nbsp;initial&nbsp;threshold&nbsp;signifies&nbsp;using&nbsp;defaults</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果初始化的值为&nbsp;0，则使用默认的初始化容量</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newCap&nbsp;=&nbsp;DEFAULT_INITIAL_CAPACITY;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newThr&nbsp;=&nbsp;(<span class="hljs-keyword">int</span>)(DEFAULT_LOAD_FACTOR&nbsp;*&nbsp;DEFAULT_INITIAL_CAPACITY);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果新的容量等于&nbsp;0</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(newThr&nbsp;==&nbsp;<span class="hljs-number">0</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">float</span>&nbsp;ft&nbsp;=&nbsp;(<span class="hljs-keyword">float</span>)newCap&nbsp;*&nbsp;loadFactor;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newThr&nbsp;=&nbsp;(newCap&nbsp;&lt;&nbsp;MAXIMUM_CAPACITY&nbsp;&amp;&amp;&nbsp;ft&nbsp;&lt;&nbsp;(<span class="hljs-keyword">float</span>)MAXIMUM_CAPACITY&nbsp;?
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(<span class="hljs-keyword">int</span>)ft&nbsp;:&nbsp;Integer.MAX_VALUE);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;threshold&nbsp;=&nbsp;newThr;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@SuppressWarnings({"rawtypes","unchecked"})</span>
&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;[]&nbsp;newTab&nbsp;=&nbsp;(Node&lt;K,V&gt;[])<span class="hljs-keyword">new</span>&nbsp;Node[newCap];
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;开始扩容，将新的容量赋值给&nbsp;table</span>
&nbsp;&nbsp;&nbsp;&nbsp;table&nbsp;=&nbsp;newTab;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;原数据不为空，将原数据复制到新&nbsp;table&nbsp;中</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(oldTab&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;根据容量循环数组，复制非空元素到新&nbsp;table</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">for</span>&nbsp;(<span class="hljs-keyword">int</span>&nbsp;j&nbsp;=&nbsp;<span class="hljs-number">0</span>;&nbsp;j&nbsp;&lt;&nbsp;oldCap;&nbsp;++j)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;((e&nbsp;=&nbsp;oldTab[j])&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;oldTab[j]&nbsp;=&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果链表只有一个，则进行直接赋值</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(e.next&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTab[e.hash&nbsp;&amp;&nbsp;(newCap&nbsp;-&nbsp;<span class="hljs-number">1</span>)]&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;<span class="hljs-keyword">if</span>&nbsp;(e&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;TreeNode)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;红黑树相关的操作</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((TreeNode&lt;K,V&gt;)e).split(<span class="hljs-keyword">this</span>,&nbsp;newTab,&nbsp;j,&nbsp;oldCap);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;{&nbsp;<span class="hljs-comment">//&nbsp;preserve&nbsp;order</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;链表复制，JDK&nbsp;1.8&nbsp;扩容优化部分</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;&nbsp;loHead&nbsp;=&nbsp;<span class="hljs-keyword">null</span>,&nbsp;loTail&nbsp;=&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;&nbsp;hiHead&nbsp;=&nbsp;<span class="hljs-keyword">null</span>,&nbsp;hiTail&nbsp;=&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Node&lt;K,V&gt;&nbsp;next;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">do</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;next&nbsp;=&nbsp;e.next;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;原索引</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;((e.hash&nbsp;&amp;&nbsp;oldCap)&nbsp;==&nbsp;<span class="hljs-number">0</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(loTail&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;loHead&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;loTail.next&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;loTail&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;原索引&nbsp;+&nbsp;oldCap</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(hiTail&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hiHead&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">else</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hiTail.next&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hiTail&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">while</span>&nbsp;((e&nbsp;=&nbsp;next)&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;将原索引放到哈希桶中</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(loTail&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;loTail.next&nbsp;=&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTab[j]&nbsp;=&nbsp;loHead;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;将原索引&nbsp;+&nbsp;oldCap&nbsp;放到哈希桶中</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(hiTail&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hiTail.next&nbsp;=&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTab[j&nbsp;+&nbsp;oldCap]&nbsp;=&nbsp;hiHead;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;newTab;
}
</code></pre>
<p data-nodeid="2812">从以上源码可以看出，JDK 1.8 在扩容时并没有像 JDK 1.7 那样，重新计算每个元素的哈希值，而是通过高位运算（e.hash &amp; oldCap）来确定元素是否需要移动，比如 key1 的信息如下：</p>
<ul data-nodeid="2813">
<li data-nodeid="2814">
<p data-nodeid="2815">key1.hash = 10 0000 1010</p>
</li>
<li data-nodeid="2816">
<p data-nodeid="2817">oldCap = 16 0001 0000</p>
</li>
</ul>
<p data-nodeid="2818">使用 e.hash &amp; oldCap 得到的结果，高一位为 0，当结果为 0 时表示元素在扩容时位置不会发生任何变化，而 key 2 信息如下：</p>
<ul data-nodeid="2819">
<li data-nodeid="2820">
<p data-nodeid="2821">key2.hash = 10 0001 0001</p>
</li>
<li data-nodeid="2822">
<p data-nodeid="2823">oldCap = 16 0001 0000</p>
</li>
</ul>
<p data-nodeid="2824">这时候得到的结果，高一位为 1，当结果为 1 时，表示元素在扩容时位置发生了变化，新的下标位置等于原下标位置 + 原数组长度，如下图所示：</p>
<p data-nodeid="2825"><img src="https://s0.lgstatic.com/i/image3/M01/73/D9/Cgq2xl5rDYmAXoWFAAArXO_oe8c713.png" alt="" data-nodeid="2915"></p>
<p data-nodeid="2826">其中红色的虚线图代表了扩容时元素移动的位置。</p>
<h4 data-nodeid="2827">2.HashMap 死循环分析</h4>
<p data-nodeid="2828">以 JDK 1.7 为例，假设 HashMap 默认大小为 2，原本 HashMap 中有一个元素 key(5)，我们再使用两个线程：t1 添加元素 key(3)，t2 添加元素 key(7)，当元素 key(3) 和 key(7) 都添加到 HashMap 中之后，线程 t1 在执行到 Entry&lt;K,V&gt; next = e.next; 时，交出了 CPU 的使用权，源码如下：</p>
<pre class="lang-java" data-nodeid="2829"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">transfer</span><span class="hljs-params">(Entry[]&nbsp;newTable,&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;rehash)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;newCapacity&nbsp;=&nbsp;newTable.length;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">for</span>&nbsp;(Entry&lt;K,V&gt;&nbsp;e&nbsp;:&nbsp;table)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">while</span>(<span class="hljs-keyword">null</span>&nbsp;!=&nbsp;e)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Entry&lt;K,V&gt;&nbsp;next&nbsp;=&nbsp;e.next;&nbsp;<span class="hljs-comment">//&nbsp;线程一执行此处</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(rehash)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e.hash&nbsp;=&nbsp;<span class="hljs-keyword">null</span>&nbsp;==&nbsp;e.key&nbsp;?&nbsp;<span class="hljs-number">0</span>&nbsp;:&nbsp;hash(e.key);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;i&nbsp;=&nbsp;indexFor(e.hash,&nbsp;newCapacity);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e.next&nbsp;=&nbsp;newTable[i];
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTable[i]&nbsp;=&nbsp;e;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;e&nbsp;=&nbsp;next;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p data-nodeid="2830">那么此时线程 t1 中的 e 指向了 key(3)，而 next 指向了 key(7) ；之后线程 t2 重新 rehash 之后链表的顺序被反转，链表的位置变成了 key(5) → key(7) → key(3)，其中 “→” 用来表示下一个元素。</p>
<p data-nodeid="2831">当 t1 重新获得执行权之后，先执行 newTalbe[i] = e 把 key(3) 的 next 设置为 key(7)，而下次循环时查询到 key(7) 的 next 元素为 key(3)，于是就形成了 key(3) 和 key(7) 的循环引用，因此就导致了死循环的发生，如下图所示：</p>
<p data-nodeid="2832"><img src="https://s0.lgstatic.com/i/image3/M01/73/D9/CgpOIF5rDYmAPR1lAABl-qSxBYs115.png" alt="" data-nodeid="2928"></p>
<p data-nodeid="2833">当然发生死循环的原因是 JDK 1.7 链表插入方式为首部倒序插入，这个问题在 JDK 1.8 得到了改善，变成了尾部正序插入。</p>
<p data-nodeid="2834">有人曾经把这个问题反馈给了 Sun 公司，但 Sun 公司认为这不是一个问题，因为 HashMap 本身就是非线程安全的，如果要在多线程下，建议使用 ConcurrentHashMap 替代，但这个问题在面试中被问到的几率依然很大，所以在这里需要特别说明一下。</p>
<h3 data-nodeid="2835">小结</h3>
<p data-nodeid="4791" class="te-preview-highlight">本课时介绍了 HashMap 的底层数据结构，在 JDK 1.7 时 HashMap 是由数组和链表组成的，而 JDK 1.8 则新增了红黑树结构，当链表长度达到 8 并且容器达到 64 时会转换为红黑树存储，以提升元素的操作性能。同时还介绍了 HashMap 的三个重要方法，查询、添加和扩容，以及 JDK 1.7 resize() &nbsp;在并发环境下导致死循环的原因。</p>

---

### 精选评论

##### **路：
> 好好看看源码，当链表长度到8需要转化红黑树是还有一个判断table的length大于<span style="font-size: 0.427rem; -webkit-text-size-adjust: 100%;">MIN_TREEIFY_CAPACITY，也就是64才会转化红黑树</span>

##### *松：
> hashMap负载因子选择0.75的原因，除了老师讲的两个原因，还有一点是不是因为，<strong style="color: rgb(51, 51, 51); font-family: &quot;Open Sans&quot;, &quot;Clear Sans&quot;, &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; orphans: 4; white-space: pre-wrap; box-sizing: border-box;"><span md-inline="highlight" style="box-sizing: border-box;"><mark style="box-sizing: border-box; background-image: initial; background-position: initial; background-size: initial; background-repeat: initial; background-attachment: initial; background-origin: initial; background-clip: initial;"><span md-inline="plain" class="md-plain" style="box-sizing: border-box;">为了提升扩容效率，HashMap的容量（capacity）有一个固定的要求，那就是一定是2的幂</span></mark></span><span md-inline="plain" class="md-plain" style="box-sizing: border-box;">。</span></strong><strong style="color: rgb(51, 51, 51); font-family: &quot;Open Sans&quot;, &quot;Clear Sans&quot;, &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; orphans: 4; white-space: pre-wrap; box-sizing: border-box;"><span md-inline="plain" class="md-plain" style="box-sizing: border-box;">所以，</span><span md-inline="highlight" style="box-sizing: border-box;"><mark style="box-sizing: border-box; background-image: initial; background-position: initial; background-size: initial; background-repeat: initial; background-attachment: initial; background-origin: initial; background-clip: initial;"><span md-inline="plain" class="md-plain" style="box-sizing: border-box;">如果负载因子是3/4的话，那么和capacity的乘积结果就可以是一个整数</span></mark></span></strong>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 感谢补充。

##### *鑫：
> 1.HashMap采用链地址法，低十六位和高十六位异或以及hash&amp;length-1来减少hash冲突<div>2.在1.7采用头插入，1.8优化为尾插入，因为头插入容易产生环形链表死循环问题</div><div>3.在1.7扩容位置为hash &amp; 新容量-1，1.8是如果只有首节点那么跟1.7一样，否则判断是否为红黑树或者链表，再通过hash&amp;原容量判断，为0放低位，否则高位，低位位置不变，高位位置=原位置+原容量</div><div>4.树化和退树化8和6的选择，红黑树平均查找为logn，长度为8时，查找长度为3，而链表平均为8除以2；当为6时，查找长度一样，而树化需要时间；然后中间就一个数防止频繁转换</div><div>5.容量设置为2的n次方主要是可以用位运算实现取模运算，位运算采用内存操作，且能解决负数问题；同时hash&amp;length-1时，length-1为奇数的二进制都为1，index的结果就等同与hashcode后几位，只要hashcode均匀，那么hash碰撞就少</div><div>6.负载因子.75主要是太大容易造成hash冲突，太小浪费空间</div><div>希望顺便说下红黑树，自己看的时候有很多东西不太理解😶</div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 红黑树的知识，后面的章节有专门讲哈。

##### **辉：
> 这几天面试都背问到HashMap，确实很重要

##### **康：
> 链表转为红黑树那里，少了一个判断条件：hash桶的长度大于等于64。两个条件都满足才会转红黑树，不满足后者会直接调用扩容方法

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是需要满足两个条件，源码 treeifyBin() 里面有关于 MIN_TREEIFY_CAPACITY (64) 的判断。

##### *凯：
> <div>JDK 1.8 在扩容时并没有像 JDK 1.7 那样，重新计算每个元素的哈希值，而是通过高位运算（e.hash &amp; oldCap）来确定元素是否需要移动</div><div><span style="font-size: 0.427rem;">key1.hash = 10 0000 1010</span></div><div>oldCap = 16 0001 0000</div><div><br></div><div>瞅了三次才明白这个&nbsp;16 0001 0000 什么含义。。。16 的二进制形式是&nbsp;0001 0000😥</div>

##### **飞：
> key1.hash = 10 0000 1010oldCap = 16 0001 0000这个简直看哭了我，一直在想最高位不是两个1吗，直到想到二进制只有01，才发现那是10和16

##### **磊：
> 说的不错，但是还是希望老师能够更新快点😁😁😁

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### **可待：
> 当 t1 重新获得执行权之后，先执行 newTalbe[i] = e 把 key(3) 的 next 设置为 key(7)，这是为什么

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这里不是因果逻辑关系，重点讲的是极端情况下“这样执行”会导致问题的产生。

##### **告：
> 为什么到8变成红黑树，到6才退化成单链

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 为了避免频发的触发类型转换带来的性能开销

##### **斌：
> 老师课很好理解，就是更新太慢了，只能干着急，哎

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### **聪：
> 另外能不能更新再快一点哦，辛苦啦～

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### **龙：
> JDK8，扩容后链表不会反转吧

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 不会的

##### *杰：
> 更新了第一名

##### *广：
> 当hashCode离散性很好的时候，树型bin用到的概率非常小，因为数据均匀分布在每个bin中，几乎不会有bin中链表长度会达到阈值（树华门槛）。但是在随机hashCode下，离散性可能会变差，然而JDK又不能阻止用户实现这种不好的hash算法，因此就可能导致不均匀的数据分布。不过理想情况下随机hashCode算法下所有bin中节点的分布频率会遵循泊松分布，我们可以看到，一个bin中链表长度达到8个元素的概率为0.00000006，几乎是不可能事件。所以，之所以选择8，不是拍拍屁股决定的，而是根据概率统计决定的。由此可见，发展30年的Java每一项改动和优化都是非常严谨和科学的。

##### christmad：
> 看到王磊老师在对第02讲评论回复时，有一段话“<span style="font-size: 16.0125px;">源码是这样判断的，可以理解为规定，0 不变，1变。就像井盖必须是圆的一样，那为什么不是方的一样，它总得有一个形状。”</span><div><span style="font-size: 16.0125px;"><br></span></div><div><span style="font-size: 16.0125px;">这段话是不恰当的。</span></div><div><span style="font-size: 16.0125px;"><br></span></div><div><span style="font-size: 16.0125px;">java hashmap 源码中扩容移动元素时，并不是随便定义 “0” 在原位，“1”要移动。</span></div><div><span style="font-size: 16.0125px;">而是因为，这是经过计算后得到的规律。试想你扩容后进行查找，</span><span style="font-size: 0.427rem;">对于某个元素 keyXXX来说扩容前和扩容后&nbsp;</span>get 代码并没有任何改变，对这个 keyXXX 进行桶下标计算，必须能计算到正确的下标值才能找到存放的数据。</div><div><br></div><div>所以，这是有精确计算的，有规律的结果。原因可能是因为 hashmap 计算 idx 用了数组长度-1 作为掩码 并且 数组长度始终是 2的幂次方 两者结合作用造成的吧。这导致扩容时元素移动只有两种规律，一种待在 idx, 一种是移动到 idx+oldCap......不会移动到 idx+1 ,idx+2 这些位置去......</div><div><br></div><div>至于，为什么写 hashmap 源码的人知道这个规律，也许是人家数据结构算法和编程经验起作用了呢？</div>

##### Hyolee：
> 课件可以下载吗？没更新的可以先发课件后补视频吗？

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 关注拉勾教育公众号 咨询小助手获取课件。课件与课程同步更新的，不能提前放哈

##### **用户7139：
> 在讲死链问题时，t2 rehash是不是写错了？应该是resize吧？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; java 1.7 是 rehash 没写错哈。

##### *俊：
> 查缺补漏

##### **明：
> 今天的文章涉及的底层太深了。有很多数据结构和算法的知识。不是很懂。多看看

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 加油哦，有不懂的地方可以留言提问哈

##### *胜：
> HashMap 为什么选择 红黑树了，咋不用 AVL 树了。

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这个原因和二者的特性强相关，AVL树对平衡性要求更严格，比较适合查找密集型任务，它在查询上的优秀表现是以牺牲添加和删除的速度为代价的，所以显然红黑树更"中庸"通用性更好一些，所以Java中就使用了红黑树作为HashMap的底层实现了。

##### **凯：
> if ((e.hash  oldCap) == 0) { if (loTail == null) else } // 原索引 + oldCap else { if (hiTail == null) else }一个链表中的node的hash不是相等吗？e.hash  oldCap处理得到的结果是一样吧？如果这样的话就不能在扩容之后移动元素。这个很困惑，麻烦老师给解答下🙏🙏

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这块是使用了一个比较巧妙的“算法”来确定数组在扩容之后要不要移动（0 表示不移动），其中 e.hash 表示旧数组中节点或元素 e 的 hash 值；而 oldCap 为旧数组的数组长度。

##### *坤：
> 加载因子过大，扩容发生频率较低，这个可以理解，但是为什么hash冲突的几率会增大呢？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 扩容发生频率低有限空间存储的元素就多了，发生 hash 冲突的几率也随之增大了。

##### **龙：
> 两个的结果，只有第三个部分，key1是0000、key2是0001不同，这个位置就叫"高一位"吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是从后往前算的，前面的是高位 1 0000， 0 00001，高位就是最前面的 1 和 0，可以参考美团的这篇文章，它的图画的更详细一些：https://tech.meituan.com/2016/06/24/java-hashmap.html

##### *飞：
> 在hashMap的putVal()方法里面最后有一个afterNodeInsertion(evict);方法，请问老师这个方法干嘛用的？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; afterNodeInsertion 方法是为了继承 HashMap 的 LinkedHashMap 类使用的，LinkedHashMap 中被覆盖的 afterNodeInsertion 方法，用来回调移除最早放入 Map 的对象。

##### *波：
> 1、加载因子越大，扩容频率减少，发生hash碰撞的机率增加；加载因子越小，扩容频率增加，发生hash碰撞的机率增加，这个结果是不是不太严谨呢，hash是跟容量挂钩的。2、扩容操作时，在1.8之后，只有当该索引处的元素的节点不为1时，才不会去重新计算元素的hash值；另外扩容操作是有new一个新的table出来的，当为链表时，只是将就原链表拆分为二，分别移动到新的table中，文中“通过高位运算(e.hasholdCap)来确定是否需要移动”这个说法是否存在不严谨的问题呢。望回复，谢谢～

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 问题一描述没问题，扩容的频率决定了可用容量，可用容量的大小和发生碰撞的几率是成反比的，可用容量越大发生 hash 碰撞的几率就越小，发生扩容的频率越高空闲的容量也就越大，所以这个观点是成立的。问题二的高位运算是指在发生扩容之后的链表行为，所以描述也是 ok 的哈。

##### *以：
> DEFAULT_INITIAL_CAPACITY这个是数组长度（哈希桶的默认大小值），还是说是数组加上链表的元素大小值

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这个是 HashMap 的数组默认长度，HashMap 是由数组 + 链表（或红黑树）组成的哈，你看文中的数据结构图，其中哈希桶是放在数组中的哈。

##### **洪：
> "通过高位运算（e.hash  newCap，那就不需要移动，这种理解对吧？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是的，通过高位运算结果判断是否要移动位置。

##### *浩：
> 新增方法流程图的左下角画错了吧，应该没有准备插入这一步，而是直接插入链表，然后判断大于8的话就转红黑树

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 因为只有确定了 key 是否相等，才能判断是修改操作还是添加操作，如果是修改操作的话那直接赋值就可以了，就不用再去判断并新增元素了。

##### **4707：
> <div>hashmap中一开始赋值的与或运算感觉还是蛮重要的，比如 hashmap初始值给了1w，然后插入1w个值，问hashmap会不会扩容（答案是不会），因为一开始赋初值的时候如果不是2的幂数，则向上找最近的一个二的幂数，而且jdk8中hashmap用到的时候才实例化</div><div><pre style="background-color:#303845;color:#abb2bf;font-family:'Consolas';font-size:10.5pt;"><span style="color:#c792ea;">public </span><span style="color:#62bffc;">HashMap</span><span style="color:#efaa8e;">() {<br></span><span style="color:#efaa8e;">    </span><span style="color:#c792ea;">this</span><span style="color:#a6b2c0;">.</span><span style="color:#de7c84;">loadFactor </span><span style="color:#a6b2c0;">= </span><span style="color:#d19a66;">DEFAULT_LOAD_FACTOR</span><span style="color:#a6b2c0;">; </span><span style="color:#59626f;font-style:italic;">// all other fields defaulted<br></span><span style="color:#efaa8e;">}</span></pre></div><div><br></div>

##### **波：
> 询问一下，负载因子选择0.75和泊松分布是否有关系

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 坦率的说，我觉得没啥关联关系。

##### **_Android：
> <div>当加载因子设置比较大的时候，扩容的门槛就被提高了，扩容发生的频率比较低</div><div><br></div><div>这句话的理解是不是说比如我把扩容因子设置为2 ，那么此时如果HashMap下一次扩容的时候需要的是2*16=32个长度的内存空间，那么老师解答一下这个这个内存分配机制，是否是这32个内存空间分配是否处于堆区分配，堆的优势就是动态分配内存大小，加载因子设置过大，会导致在堆区分配内存空间难度变大吗？如果是的话，扩容门槛变高，也就能够说得通了，继而扩容发生的概率降低也就是自然行得通。主要的问题还是没有明白扩容因子设置过大，导致的内存分配难度变大这个问题。麻烦解答一下，还有上诉猜想是否正确，比如是否在堆区分配扩容因子之后长度空间。也请解答一下，学习感觉还是要追求本质。</div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 可能表示的不是很清楚，“扩容因子设置过大，导致的内存分配难度变大这个问题”，这里的难度变大，不是指处理器的难度变大了，而是指业务场景的“难度变大了”，指的是扩容发生的门槛提高了，扩容的频率降低了。

##### **兵：
> 数组长度低于64的 即便是链表长度超过8也不会树化,而是扩容操作,数组超过64在链表大于等于8的时候才会进行树化吧?

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 需要同时满足两个条件才行

##### **山：
> 老师这个面试题，是针对工作者，还是找工作的学生呀

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 这些面试题在面试中经常会被考到，无论是工作者还是学生在面试前都应该吃透

##### **凯：
> <div>e.hash &amp; oldCap 得到的结果，高一位为 0，当结果为 0 时表示元素在扩容时位置不会发生任何变化，为什么高位为0，在扩容的时候，位置就不会再变了？</div><div><br></div><div><br></div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 源码是这样判断的，可以理解为规定，0 不变，1变。就像井盖必须是圆的一样，那为什么不是方的一样，它总得有一个形状。

##### **文：
> 在resize()函数中，对于链表数组的遍历里面的又套了一个循环不太理解这里为什么要这样做，看起来是毫无意义的，除了做链表在数组中的移动以外没有看到其他的含义，老师能够解答一下吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; for (int j = 0; j < oldCap; ++j) { 吗？这个扩容之后把旧数据赋值给新容器

##### yhb：
> 更新的太慢了老师们，可以一周五更吗

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### **盛：
> 最后transfer方法是在哪里执行的呢？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; JDK 1.7 resize(int newCapacity) 的时候会调用

##### **2132：
> 当加载因子设置比较大的时候，扩容的门槛就被提高了，扩容发生的频率比较低，占用的空间会比较小，但此时发生Hash冲突的几率就会提升，因此需要更复杂的数据结构来存储元素，这样对元素的操作时间就会增加。<div><br></div><div>老师您好，这一句我没懂，麻烦请解释下，谢谢。</div><div>1. 加载因子设置大，<span style="font-size: 0.427rem; -webkit-text-size-adjust: 100%;">发生Hash冲突的几率就会提升，是什么意思呢</span></div><div><span style="font-size: 0.427rem; -webkit-text-size-adjust: 100%;">2. 对于这种情况，需要更复杂的数据结构来存储，又如何理解</span></div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 加载因子设置的比较大，那么 Hash 扩容发生的频率就低了（门槛高了），数据存储的就更秘籍了，因此发生冲突的概率就高了。更复杂的数据结构指的是红黑树。

##### **杰：
> 个人认为只有当链表长度大于8而且哈希表的长度大于64时才会进行链表转化为红黑树的操作😀

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是这样，满足两个条件，还有 MIN_TREEIFY_CAPACITY (64) 的判断

##### **欧：
> 不错，能更新快点嘛

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### xuezhiw：
> 希望作者更新快点哦……

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### **芋：
> 老师您说的并发情况下是多线程访问同一个hashmap实例的情况吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是的。

##### **7698：
> 点赞，然后来催更

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 好哒，我们已反馈给讲师，后期会加快更新节凑

##### *华：
> ConcurrentHashp是怎么实现的？感觉没讲完

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 感谢反馈，篇幅有限，这篇重点讲解的是 HashMap。

##### **峰：
> 期待更优质的内容

##### **宏：
> 什么结果高一位，自己想的叫法吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 感谢反馈，中间少个逗号，“得到的结果，高一位为 0”

##### **聪：
> <div>不懂就问系列</div><div>2.HashMap 死循环分析</div><div>上面的那张图 绿色的方块指什么</div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 绿色表示高一位为 0，无需移动位置的元素。

##### **科：
> 总结的很到位

##### kyang：
> 扩容优化和死锁没有看懂。

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 具体有什么不懂的问题可以留言提问哈

##### **1623：
> 我记得以前看源码发现链表转换红黑树应该会先去判断数组长度！当数组长度超过16后才会去把超过8的链表转红黑树吧！难道是我看错了？？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 数组长度 64 转为红黑树

##### **乐：
> 更新太慢了吧😓

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 该课程每周三、五均有更新

