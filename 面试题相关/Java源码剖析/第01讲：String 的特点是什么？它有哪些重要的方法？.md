
<p>本课时的问题是：String&nbsp;是如何实现的？它有哪些重要的方法？</p>
<h3>典型回答</h3>
<p>以主流的&nbsp;JDK&nbsp;版本 1.8&nbsp;来说，String&nbsp;内部实际存储结构为&nbsp;char&nbsp;数组，源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">String</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">java</span>.<span class="hljs-title">io</span>.<span class="hljs-title">Serializable</span>,&nbsp;<span class="hljs-title">Comparable</span>&lt;<span class="hljs-title">String</span>&gt;,&nbsp;<span class="hljs-title">CharSequence</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;用于存储字符串的值</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-keyword">char</span>&nbsp;value[];
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;缓存字符串的&nbsp;hash&nbsp;code</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;hash;&nbsp;<span class="hljs-comment">//&nbsp;Default&nbsp;to&nbsp;0</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;......其他内容</span>
}
</code></pre>
<p>String&nbsp;源码中包含下面几个重要的方法。</p>
<h4>1. 多构造方法</h4>
<p>String&nbsp;字符串有以下 4 个重要的构造方法：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-comment">//&nbsp;String&nbsp;为参数的构造方法</span>
<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">String</span><span class="hljs-params">(String&nbsp;original)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.value&nbsp;=&nbsp;original.value;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.hash&nbsp;=&nbsp;original.hash;
}
<span class="hljs-comment">//&nbsp;char[]&nbsp;为参数构造方法</span>
<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">String</span><span class="hljs-params">(<span class="hljs-keyword">char</span>&nbsp;value[])</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.value&nbsp;=&nbsp;Arrays.copyOf(value,&nbsp;value.length);
}
<span class="hljs-comment">//&nbsp;StringBuffer&nbsp;为参数的构造方法</span>
<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">String</span><span class="hljs-params">(StringBuffer&nbsp;buffer)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">synchronized</span>(buffer)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.value&nbsp;=&nbsp;Arrays.copyOf(buffer.getValue(),&nbsp;buffer.length());
&nbsp;&nbsp;&nbsp;&nbsp;}
}
<span class="hljs-comment">//&nbsp;StringBuilder&nbsp;为参数的构造方法</span>
<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">String</span><span class="hljs-params">(StringBuilder&nbsp;builder)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.value&nbsp;=&nbsp;Arrays.copyOf(builder.getValue(),&nbsp;builder.length());
}
</code></pre>
<p>其中，比较容易被我们忽略的是以&nbsp;StringBuffer 和 StringBuilder 为参数的构造函数，因为这三种数据类型，我们通常都是单独使用的，所以这个小细节我们需要特别留意一下。</p>
<h4>2. equals() 比较两个字符串是否相等</h4>
<p>源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;<span class="hljs-title">equals</span><span class="hljs-params">(Object&nbsp;anObject)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;对象引用相同直接返回&nbsp;true</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(<span class="hljs-keyword">this</span>&nbsp;==&nbsp;anObject)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">true</span>;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;判断需要对比的值是否为&nbsp;String&nbsp;类型，如果不是则直接返回&nbsp;false</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(anObject&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;String)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String&nbsp;anotherString&nbsp;=&nbsp;(String)anObject;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;n&nbsp;=&nbsp;value.length;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(n&nbsp;==&nbsp;anotherString.value.length)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;把两个字符串都转换为&nbsp;char&nbsp;数组对比</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">char</span>&nbsp;v1[]&nbsp;=&nbsp;value;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">char</span>&nbsp;v2[]&nbsp;=&nbsp;anotherString.value;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;i&nbsp;=&nbsp;<span class="hljs-number">0</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;循环比对两个字符串的每一个字符</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">while</span>&nbsp;(n--&nbsp;!=&nbsp;<span class="hljs-number">0</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果其中有一个字符不相等就&nbsp;true&nbsp;false，否则继续对比</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(v1[i]&nbsp;!=&nbsp;v2[i])
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">false</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i++;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">true</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">false</span>;
}
</code></pre>
<p>String&nbsp;类型重写了&nbsp;Object&nbsp;中的&nbsp;equals()&nbsp;方法，equals()&nbsp;方法需要传递一个&nbsp;Object&nbsp;类型的参数值，在比较时会先通过 instanceof&nbsp;判断是否为&nbsp;String&nbsp;类型，如果不是则会直接返回&nbsp;false，instanceof&nbsp;的使用如下：</p>
<pre><code data-language="java" class="lang-java">Object&nbsp;oString&nbsp;=&nbsp;<span class="hljs-string">"123"</span>;
Object&nbsp;oInt&nbsp;=&nbsp;<span class="hljs-number">123</span>;
System.out.println(oString&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;String);&nbsp;<span class="hljs-comment">//&nbsp;返回&nbsp;true</span>
System.out.println(oInt&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;String);&nbsp;<span class="hljs-comment">//&nbsp;返回&nbsp;false</span>
</code></pre>
<p>当判断参数为 String&nbsp;类型之后，会循环对比两个字符串中的每一个字符，当所有字符都相等时返回&nbsp;true，否则则返回 false。</p>
<p>还有一个和&nbsp;equals()&nbsp;比较类似的方法 equalsIgnoreCase()，它是用于忽略字符串的大小写之后进行字符串对比。</p>
<h4>3. compareTo() 比较两个字符串</h4>
<p>compareTo()&nbsp;方法用于比较两个字符串，返回的结果为&nbsp;int&nbsp;类型的值，源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;<span class="hljs-title">compareTo</span><span class="hljs-params">(String&nbsp;anotherString)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;len1&nbsp;=&nbsp;value.length;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;len2&nbsp;=&nbsp;anotherString.value.length;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;获取到两个字符串长度最短的那个&nbsp;int&nbsp;值</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;lim&nbsp;=&nbsp;Math.min(len1,&nbsp;len2);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">char</span>&nbsp;v1[]&nbsp;=&nbsp;value;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">char</span>&nbsp;v2[]&nbsp;=&nbsp;anotherString.value;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;k&nbsp;=&nbsp;<span class="hljs-number">0</span>;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;对比每一个字符</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">while</span>&nbsp;(k&nbsp;&lt;&nbsp;lim)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">char</span>&nbsp;c1&nbsp;=&nbsp;v1[k];
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">char</span>&nbsp;c2&nbsp;=&nbsp;v2[k];
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(c1&nbsp;!=&nbsp;c2)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;有字符不相等就返回差值</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;c1&nbsp;-&nbsp;c2;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;k++;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;len1&nbsp;-&nbsp;len2;
}
</code></pre>
<p>从源码中可以看出，compareTo()&nbsp;方法会循环对比所有的字符，当两个字符串中有任意一个字符不相同时，则 return&nbsp;char1-char2。比如，两个字符串分别存储的是 1&nbsp;和 2，返回的值是 -1；如果存储的是 1&nbsp;和 1，则返回的值是 0 ，如果存储的是 2&nbsp;和 1，则返回的值是 1。</p>
<p>还有一个和&nbsp;compareTo()&nbsp;比较类似的方法 compareToIgnoreCase()，用于忽略大小写后比较两个字符串。</p>
<p>可以看出&nbsp;compareTo()&nbsp;方法和&nbsp;equals()&nbsp;方法都是用于比较两个字符串的，但它们有两点不同：</p>
<ul>
<li>equals()&nbsp;可以接收一个 Object&nbsp;类型的参数，而 compareTo()&nbsp;只能接收一个&nbsp;String&nbsp;类型的参数；</li>
<li>equals()&nbsp;返回值为&nbsp;Boolean，而&nbsp;compareTo()&nbsp;的返回值则为&nbsp;int。</li>
</ul>
<p>它们都可以用于两个字符串的比较，当&nbsp;equals()&nbsp;方法返回&nbsp;true&nbsp;时，或者是&nbsp;compareTo()&nbsp;方法返回 0&nbsp;时，则表示两个字符串完全相同。</p>
<h4>4.&nbsp;其他重要方法</h4>
<ul>
<li>indexOf()：查询字符串首次出现的下标位置</li>
<li>lastIndexOf()：查询字符串最后出现的下标位置</li>
<li>contains()：查询字符串中是否包含另一个字符串</li>
<li>toLowerCase()：把字符串全部转换成小写</li>
<li>toUpperCase()：把字符串全部转换成大写</li>
<li>length()：查询字符串的长度</li>
<li>trim()：去掉字符串首尾空格</li>
<li>replace()：替换字符串中的某些字符</li>
<li>split()：把字符串分割并返回字符串数组</li>
<li>join()：把字符串数组转为字符串</li>
</ul>
<h3>考点分析</h3>
<p>这道题目考察的重点是，你对&nbsp;Java&nbsp;源码的理解，这也从侧面反应了你是否热爱和喜欢专研程序，而这正是一个优秀程序员所必备的特质。</p>
<p>String&nbsp;源码属于所有源码中最基础、最简单的一个，对&nbsp;String&nbsp;源码的理解也反应了你的&nbsp;Java&nbsp;基础功底。</p>
<p>String&nbsp;问题如果再延伸一下，会问到一些更多的知识细节，这也是大厂一贯使用的面试策略，从一个知识点入手然后扩充更多的知识细节，对于 String 也不例外，通常还会关联的询问以下问题：</p>
<ul>
<li>为什么&nbsp;String&nbsp;类型要用&nbsp;final&nbsp;修饰？</li>
<li>== 和 equals&nbsp;的区别是什么？</li>
<li>String 和&nbsp;StringBuilder、StringBuffer&nbsp;有什么区别？</li>
<li>String&nbsp;的 intern()&nbsp;方法有什么含义？</li>
<li>String&nbsp;类型在&nbsp;JVM（Java&nbsp;虚拟机）中是如何存储的？编译器对&nbsp;String&nbsp;做了哪些优化？</li>
</ul>
<p>接下来我们一起来看这些问题的答案。</p>
<h3>知识扩展</h3>
<h4>1.&nbsp;== 和&nbsp;equals&nbsp;的区别</h4>
<p>==&nbsp;对于基本数据类型来说，是用于比较 “值”是否相等的；而对于引用类型来说，是用于比较引用地址是否相同的。</p>
<p>查看源码我们可以知道 Object 中也有&nbsp;equals()&nbsp; 方法，源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;<span class="hljs-title">equals</span><span class="hljs-params">(Object&nbsp;obj)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;(<span class="hljs-keyword">this</span>&nbsp;==&nbsp;obj);
}
</code></pre>
<p>可以看出，Object&nbsp;中的&nbsp;equals()&nbsp;方法其实就是 ==，而&nbsp;String&nbsp;重写了 equals() 方法把它修改成比较两个字符串的值是否相等。</p>
<p>源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;<span class="hljs-title">equals</span><span class="hljs-params">(Object&nbsp;anObject)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;对象引用相同直接返回&nbsp;true</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(<span class="hljs-keyword">this</span>&nbsp;==&nbsp;anObject)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">true</span>;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;判断需要对比的值是否为&nbsp;String&nbsp;类型，如果不是则直接返回&nbsp;false</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(anObject&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;String)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String&nbsp;anotherString&nbsp;=&nbsp;(String)anObject;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;n&nbsp;=&nbsp;value.length;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(n&nbsp;==&nbsp;anotherString.value.length)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;把两个字符串都转换为&nbsp;char&nbsp;数组对比</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">char</span>&nbsp;v1[]&nbsp;=&nbsp;value;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">char</span>&nbsp;v2[]&nbsp;=&nbsp;anotherString.value;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">int</span>&nbsp;i&nbsp;=&nbsp;<span class="hljs-number">0</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;循环比对两个字符串的每一个字符</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">while</span>&nbsp;(n--&nbsp;!=&nbsp;<span class="hljs-number">0</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果其中有一个字符不相等就&nbsp;true&nbsp;false，否则继续对比</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(v1[i]&nbsp;!=&nbsp;v2[i])
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">false</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;i++;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">true</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">false</span>;
}
</code></pre>
<h4>2.&nbsp;final&nbsp;修饰的好处</h4>
<p>从&nbsp;String&nbsp;类的源码我们可以看出&nbsp;String&nbsp;是被&nbsp;final&nbsp;修饰的不可继承类，源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">final</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">String</span>&nbsp;
	<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">java</span>.<span class="hljs-title">io</span>.<span class="hljs-title">Serializable</span>,&nbsp;<span class="hljs-title">Comparable</span>&lt;<span class="hljs-title">String</span>&gt;,&nbsp;<span class="hljs-title">CharSequence</span>&nbsp;</span>{&nbsp;<span class="hljs-comment">//......&nbsp;}</span>
</code></pre>
<p>那这样设计有什么好处呢？</p>
<p>Java 语言之父 James Gosling 的回答是，他会更倾向于使用 final，因为它能够缓存结果，当你在传参时不需要考虑谁会修改它的值；如果是可变类的话，则有可能需要重新拷贝出来一个新值进行传参，这样在性能上就会有一定的损失。</p>
<p>James Gosling 还说迫使 String 类设计成不可变的另一个原因是<strong>安全</strong>，当你在调用其他方法时，比如调用一些系统级操作指令之前，可能会有一系列校验，如果是可变类的话，可能在你校验过后，它的内部的值又被改变了，这样有可能会引起严重的系统崩溃问题，这是迫使 String 类设计成不可变类的一个重要原因。</p>
<p>总结来说，使用&nbsp;final&nbsp;修饰的第一个好处是<strong>安全</strong>；第二个好处是<strong>高效</strong>，以&nbsp;JVM&nbsp;中的字符串常量池来举例，如下两个变量：</p>
<pre><code data-language="java" class="lang-java">String&nbsp;s1&nbsp;=&nbsp;<span class="hljs-string">"java"</span>;
String&nbsp;s2&nbsp;=&nbsp;<span class="hljs-string">"java"</span>;
</code></pre>
<p>只有字符串是不可变时，我们才能实现字符串常量池，字符串常量池可以为我们缓存字符串，提高程序的运行效率，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image3/M01/72/87/Cgq2xl5ofeSAGmDSAAAxub0kSB4592.png" alt=""></p>
<p>试想一下如果&nbsp;String&nbsp;是可变的，那当&nbsp;s1&nbsp;的值修改之后，s2&nbsp;的值也跟着改变了，这样就和我们预期的结果不相符了，因此也就没有办法实现字符串常量池的功能了。</p>
<h4>3.&nbsp;String 和&nbsp;StringBuilder、StringBuffer&nbsp;的区别</h4>
<p>因为&nbsp;String&nbsp;类型是不可变的，所以在字符串拼接的时候如果使用&nbsp;String&nbsp;的话性能会很低，因此我们就需要使用另一个数据类型&nbsp;StringBuffer，它提供了&nbsp;append 和 insert 方法可用于字符串的拼接，它使用 synchronized 来保证线程安全，如下源码所示：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Override</span>
<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">synchronized</span>&nbsp;StringBuffer&nbsp;<span class="hljs-title">append</span><span class="hljs-params">(Object&nbsp;obj)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;toStringCache&nbsp;=&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">super</span>.append(String.valueOf(obj));
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">this</span>;
}

<span class="hljs-meta">@Override</span>
<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">synchronized</span>&nbsp;StringBuffer&nbsp;<span class="hljs-title">append</span><span class="hljs-params">(String&nbsp;str)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;toStringCache&nbsp;=&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">super</span>.append(str);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">this</span>;
}
</code></pre>
<p>因为它使用了 synchronized 来保证线程安全，所以性能不是很高，于是在 JDK 1.5&nbsp;就有了&nbsp;StringBuilder，它同样提供了&nbsp;append&nbsp;和&nbsp;insert&nbsp;的拼接方法，但它没有使用 synchronized 来修饰，因此在性能上要优于 StringBuffer，所以在非并发操作的环境下可使用 StringBuilder 来进行字符串拼接。</p>
<h4>4.&nbsp;String&nbsp;和&nbsp;JVM</h4>
<p>String 常见的创建方式有两种，new String() 的方式和直接赋值的方式，直接赋值的方式会先去字符串常量池中查找是否已经有此值，如果有则把引用地址直接指向此值，否则会先在常量池中创建，然后再把引用指向此值；而 new String() 的方式一定会先在堆上创建一个字符串对象，然后再去常量池中查询此字符串的值是否已经存在，如果不存在会先在常量池中创建此字符串，然后把引用的值指向此字符串，如下代码所示：</p>
<pre><code data-language="java" class="lang-java">String&nbsp;s1&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;String(<span class="hljs-string">"Java"</span>);
String&nbsp;s2&nbsp;=&nbsp;s1.intern();
String&nbsp;s3&nbsp;=&nbsp;<span class="hljs-string">"Java"</span>;
System.out.println(s1&nbsp;==&nbsp;s2);&nbsp;<span class="hljs-comment">//&nbsp;false</span>
System.out.println(s2&nbsp;==&nbsp;s3);&nbsp;<span class="hljs-comment">//&nbsp;true</span>
</code></pre>
<p>它们在 JVM 存储的位置，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image3/M01/0D/BE/Ciqah16RQbaAZ3QkAACUHPvF6fE928.png" alt=""></p>
<blockquote>
<p>小贴士：JDK 1.7&nbsp;之后把永生代换成的元空间，把字符串常量池从方法区移到了&nbsp;Java&nbsp;堆上。</p>
</blockquote>
<p>除此之外编译器还会对&nbsp;String&nbsp;字符串做一些优化，例如以下代码：</p>
<pre><code data-language="java" class="lang-java">String&nbsp;s1&nbsp;=&nbsp;<span class="hljs-string">"Ja"</span>&nbsp;+&nbsp;<span class="hljs-string">"va"</span>;
String&nbsp;s2&nbsp;=&nbsp;<span class="hljs-string">"Java"</span>;
System.out.println(s1&nbsp;==&nbsp;s2);
</code></pre>
<p>虽然&nbsp;s1&nbsp;拼接了多个字符串，但对比的结果却是&nbsp;true，我们使用反编译工具，看到的结果如下：</p>
<pre><code data-language="java" class="lang-java">Compiled&nbsp;from&nbsp;"StringExample.java"
public&nbsp;class&nbsp;com.lagou.interview.StringExample&nbsp;{
&nbsp;&nbsp;public&nbsp;com.lagou.interview.StringExample();
&nbsp;&nbsp;&nbsp;&nbsp;Code:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0:&nbsp;aload_0
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1:&nbsp;invokespecial&nbsp;#1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//&nbsp;Method&nbsp;java/lang/Object."&lt;init&gt;":()V
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;4:&nbsp;return
&nbsp;&nbsp;&nbsp;&nbsp;LineNumberTable:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;line&nbsp;3:&nbsp;0

&nbsp;&nbsp;public&nbsp;static&nbsp;void&nbsp;main(java.lang.String[]);
&nbsp;&nbsp;&nbsp;&nbsp;Code:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0:&nbsp;ldc&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;#2&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//&nbsp;String&nbsp;Java
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2:&nbsp;astore_1
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3:&nbsp;ldc&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;#2&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//&nbsp;String&nbsp;Java
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;5:&nbsp;astore_2
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;6:&nbsp;getstatic&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;#3&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//&nbsp;Field&nbsp;java/lang/System.out:Ljava/io/PrintStream;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;9:&nbsp;aload_1
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;10:&nbsp;aload_2
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;11:&nbsp;if_acmpne&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;18
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;14:&nbsp;iconst_1
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;15:&nbsp;goto&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;19
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;18:&nbsp;iconst_0
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;19:&nbsp;invokevirtual&nbsp;#4&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;//&nbsp;Method&nbsp;java/io/PrintStream.println:(Z)V
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;22:&nbsp;return
&nbsp;&nbsp;&nbsp;&nbsp;LineNumberTable:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;line&nbsp;5:&nbsp;0
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;line&nbsp;6:&nbsp;3
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;line&nbsp;7:&nbsp;6
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;line&nbsp;8:&nbsp;22
}
</code></pre>
<p>从编译代码 #2&nbsp;可以看出，代码 "Ja"+"va"&nbsp;被直接编译成了 "Java"&nbsp;，因此 s1==s2 的结果才是 true，这就是编译器对字符串优化的结果。</p>
<h3>小结</h3>
<p>本课时从&nbsp;String&nbsp;的源码入手，重点讲了&nbsp;String&nbsp;的构造方法、equals() 方法和&nbsp;compareTo()&nbsp;方法，其中&nbsp;equals()&nbsp;重写了&nbsp;Object&nbsp;的 equals()&nbsp;方法，把引用对比改成了字符串值对比，也介绍了&nbsp;final&nbsp;修饰&nbsp;String&nbsp;的好处，可以提高效率和增强安全性，同时我们还介绍了&nbsp;String&nbsp;和 JVM 的一些执行细节。</p>

---

### 精选评论

##### **科：
> 更新有点慢，更新完面试季都过了！！！！！！！！😂

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师哈，目前该课程每周三、五更新，一周更新两课时。

##### *鑫：
> <div>留言不能复制...不能撸代码...格式换下的就上万字不准提交，你们这个能不能优化下...要不然还搞什么留言？</div><div>1.<span style="font-size: 0.427rem;">String s1 = "Ja" + "va";</span><span style="font-size: 16.0125px;">编译期 被jvm优化编译为java 常量池不存在就创建。</span></div><div><span style="font-size: 16.0125px;">2.</span><span style="font-size: 0.427rem;">String s2 = "Java";</span><span style="font-size: 16.0125px;">编译期 常量池不存在就创建。</span></div><div><span style="font-size: 16.0125px;">3.</span><span style="font-size: 16.0125px;">String s3 = new String("java");</span><span style="font-size: 16.0125px;">常量池的"java"在编译期确认，类加载的时候创建(常量池不存在时)；</span><span style="font-size: 0.427rem;">堆中的"java"是在运行时确定，在new时创建。</span></div><div><span style="font-size: 0.427rem;">4.</span><span style="font-size: 0.427rem;">String s4 = "Ja";&nbsp;</span><span style="font-size: 0.427rem;">String s5 = "va";</span><span style="font-size: 16.0125px;">String s6 = s4 + s5;s6</span><span style="font-size: 0.427rem;">反编译为(new StringBuilder()).append(s4).append(s5).toString()，</span><span style="font-size: 0.427rem;">s1在编译期为常量,s6被编译为StringBuilder及append，</span><span style="font-size: 0.427rem;">s6常量池只有Ja和va，这是s4和s5编译的，</span><span style="font-size: 0.427rem;">字符串拼接中，有一个参数是变量的话，整个拼接操作会被编译成StringBuilder.append，</span><span style="font-size: 0.427rem;">这种情况编译器是无法知道其确定值的,只有在运行期才能确定。</span></div><div><span style="font-size: 0.427rem;">5.</span><span style="font-size: 16.0125px;">String s7 = (s4 + s5).intern();</span><span style="font-size: 0.427rem;">把字符串字面量放入常量池(不存在的话)，</span><span style="font-size: 0.427rem;">返回这个常量的引用。</span></div><div><span style="font-size: 0.427rem;">6.</span><span style="font-size: 0.427rem;">System.out.println(s1 == s2); // true。</span><span style="font-size: 0.427rem;">System.out.println(s2 == s3); // false。</span><span style="font-size: 0.427rem;">System.out.println(s1 == s6); // false。</span><span style="font-size: 0.427rem;">System.out.println(s1 == s7); // true</span></div>

##### **涛：
> 更新好慢哟！！！能不能更新快一点呢

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已将意见反馈给讲师，后期会加快更新频率

##### **淋：
> 讲得不错 但确实更新太慢了😂<div><br></div>

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### **明：
> 第一次接触拉勾的文章。这篇文章只值一块钱。真的太值了。可以说对于string讲的太好了。深入底层。逻辑清晰。讲的太好了。

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 感谢认可，要坚持来听课哦～

##### **斌：
> 今天看了一眼源码，高版string用的byte数组存储，网上搜了一下，没找到相关说明，老师有了解么

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; Java 9 之后 String 的存储就从 char 数组转成了 byte 数组，这样做的好处是存储变的更紧凑，占用的内存更少，操作性能更高了。

##### **思：
> 什么时候可以更新完？

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 现在是每周三、五更新，一周更新两个课时哈

##### **4387：
> <div>String s1 = new String("Java");</div><div>String s2 = s1.intern();</div><div>String s3 = "Java";</div><div>System.out.println(s1 == s2); // false</div><div>System.out.println(s2 == s3); // true</div><div>这里的结果注释是错了吧？</div>

##### **福：
> 还不错，不光是知识，还有掌握技术细节的研究思路、方法，那些个奇葩面试题有的连博客和书里都找不到，就需要平时多想一步为什么，做下试验

##### thd：
> 比较清楚 还是很基础的

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 把基础打牢，后续的学习才能如鱼得水～

##### *阵：
> 有助于加强理解，如果有更多的图示会更好

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会注意～

##### *匪：
> String char[] value数组怎么初始化的讲讲啊，

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这个已经超出了字符串的知识点了

##### **0354：
> 讲的很好

##### **纲：
> 没有看视频，只看了文字，感觉很不错。

##### Ff：
> <div>James Gosling，请问这个人说的final修饰的好处能具体讲下么？不懂啊</div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 文章有讲到，一个是提高了安全性，另一个是提高了效率。

##### *鑫：
> 更新的太慢了，等更新完都过了面试的时候了、

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### *航：
> 讲的挺好，比较细致，适合巩固基础掌握重点！

##### **栓：
> 永久代和元空间不都是方法区的实现方式么，JDK1.8将永久代移除，然后加上了元空间，元空间是直接共享使用的机器物理内存，应该是独立于jvm堆内存的呀，文中谈到的字符串常量池移到了堆中，具体是堆中的哪个区域？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 元空间确实在本地内存中，这个没有异议，但本地直接内存中主要存储的是类型信息，而字符串常量池已经在 JDK 7 时被移除方法区，放到堆中了。你可以将 -Xmx 设置小一些，然后 while 循环调用 String.valueOf(i++).intern() 看 OOM 异常信息 Java heap space 就可以看出字符串常量池已经被移到堆中了，或者是看周志明的《深入理解Java虚拟机》2.4 实战：OutOfMemoryError 里面有写。

##### **新：
> 老师讲的很好，学完就跳槽

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 哈哈小编祝你学业有成，跳槽成功。

##### **2115：
> 整理的很不错，学习了

##### **彬：
> 我能一天学十章，快更新快更新啊

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已经把大家高涨的学习热情反馈给讲师啦，后期会加快更新节奏

##### **用户7139：
> 求多更！讲得很好

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 感谢认可！我们已反馈给讲师，后期会加快更新节凑～

##### **帅：
> <div>你好，个人感觉在String 和 StringBuilder、StringBuffer 的区别与jvm的讲解还是有些浅，能再讲的深一些吗，谢谢</div>

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期课程的内容会逐步加深

##### *翼：
> 赞讲的不错谢谢老师

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 谢谢认可，记得按时来听课哦

##### *奇：
> 够基础，带着你过源码

##### **娇：
> “当你在调用其他方法时，比如调用一些系统级操作指令之前，可能会有一系列校验，如果是可变类的话，可能在你校验过后，它的内部的值又被改变了”中“可能在你教验过后，它的内部值又被改变了”被谁改变？难道String a=“1”被设置为a=“2”就不算改变吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 文中想要表达含义是：如果不是“不可变”的，那么就会有被篡改的风险，那么他就可能会产生问题。这里的修改并不是显示的常规代码级别的修改（这种修改是业务代码的需求，是被允许的安全性修改），这种的修改指的是使用非常规手段注入和篡改所带来的运行和预期不相符的非安全性修改哦。

##### **洲：
> 永生代换成的元空间到底是 1.8 还是 1.7 ？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; jdk 1.8 。有空的时候可以看看《深入理解Java虚拟机》第三版 123 页有写哈。

##### Null：
> 有两个问题请教：1为何string="xxx"，这种方式会直接在常量池中创建，要知道string的内部是char/byte数组，总得有个地方去存储它们吧，堆空间还是应该开辟才对。2.final提升性能是如何做到的？即便不是final也一样可以提升性能啊，

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 问题 1 也可以这样理解，不同的虚拟机实现有所区别，以 Hotspot 来说，在 JDK 1.7 之前常量池（运行时常量池和字符串常量池）都在方法区，而《Java虚拟机规范》对于方法区的描述为堆的一个逻辑部分。至于被 final 修饰主要的优点是安全，还有就是不可变更（只读）、线程安全，因此效率也能略高一些。

##### *炜：
> 跟着视频，看了String的源码，课程很nice!物超所值！

##### **2436：
> 分析equals的代码里，突然出现一个value，这个value从哪里来的呀？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; value 是存储值的容器

##### **泽：
> 刚学，讲得不错

##### **鸣：
> 非常感谢老师，在老师的带领下，今天上午自己把String里的equals()、equalsIgnoreCase()、compareTo()、compareToIgnoreCase()这四个方法的源代码给看了一遍，关键是自己理解了每行代码的意思，感觉到了源代码也不是很难。非常感谢，之后跟着老师的脚步，学好每一篇文章，平时也养成看源代码的习惯。🙏

##### **鸣：
> 刚刚说到的字符串new的创建方式，不管有没有肯定先创建这个字符串，然后如果堆中字符串常量池没有该值则直接放入，是不是少介绍了一种堆中字符串常量池有该值的情况，那么如何处理？是替换原有的？还是舍去新创建的保留原有的？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 字符串常量池的作用就是使用已有的字符串来提高程序的执行效率，是复用哦。

##### **威：
> Java 语言之父 James Gosling 的回答是，他会更倾向于使用 final，因为它能够缓存结果，当你在传参时不需要考虑谁会修改它的值；如果是可变类的话，则有可能需要重新拷贝出来一个新值进行传参，这样在性能上就会有一定的损失。<div>老师，对于这句话我不太理解 final 类在传参的作用，例如传参的不是 String，而是一个普通的 Object 类型，它不是 final，传参的时候是不需要拷贝一个新对象出来的吧</div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是的可以这样理解，或者只需要记它的 3 重点优势：1、可以利用不可变性实现字符串常量池；2、非常适合做 HashMap 的 key（因为不变）；3、天生线程安全。

##### *杰：
> 第一次感受到介绍的这么好，拿着小本子写下了😀

##### **铭：
> <span style="font-size: 16.7811px;">String 的 intern() 方法有什么含义？</span><div><span style="font-size: 16.7811px;">老师，貌似这个问题没有解答吧？</span></div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; String#intern 是一个 native 方法，注释写的很详细，“如果常量池中存在当前字符串, 就会直接返回当前字符串. 如果常量池中没有此字符串, 会将此字符串放入常量池中后, 再返回”

##### **宇：
> 有点疑惑，string类是final的与传参值不变有什么关系啊

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 意思是，如果是这样 final 的参数 private static void upString(final String str) { 就不能修改了，而非 fianl 是可以修改的。

##### *辉：
> <div>// StringBuffer 为参数的构造方法</div><div>public String(StringBuffer buffer) {</div><div>&nbsp; &nbsp; synchronized(buffer) {</div><div>&nbsp; &nbsp; &nbsp; &nbsp; this.value = Arrays.copyOf(buffer.getValue(), buffer.length());</div><div>&nbsp; &nbsp; }</div><div>}</div><div>这里加同步代码块的原因是StringBuffer是线程安全的原因吗</div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这是 JDK 的源码，原因大概是为了保证线程安全。

##### **东：
> new String() 如果常量池中没有此字符串的值，那么会在常量池中创建。<div><br></div><div>首先new的时候会在堆中创建对象，如果常量池中没有这个值，不是直接把刚刚创建的对象的值搬到堆中然后把引用指向它就可以了吗，为什么还要在常量池中创建一个？</div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; new String() 是先去判断的，还没有创建对象呢。

##### **新：
> 希望能快点更新

##### *罗：
> 讲的清晰透彻！

##### **敏：
> 老师讲的很好很清晰，比极客时间那个核心精讲好多了，看那个差点没气死；<div>不过能不能每天更新啊这样太慢了</div>

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 谢谢认可！坚持学下去，相信你会get更多课程的精髓～关于更新频次我们已经反馈给讲师，后续会加快更新节奏的～

##### **龙：
> 有个地方不清楚&nbsp;<div><span style="font-size: 16.7811px;"><br></span></div><div><div><span style="font-size: 16.7811px;">String s1 = "Ja" + "va";</span></div><div><span style="font-size: 16.7811px;">String s2 = "Java";</span></div><div><span style="font-size: 16.7811px;">System.out.println(s1 == s2);</span></div><div>如果这样的话，意思是常量池中不存在"Ja"和"va"吗？</div></div><div><br></div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 常量池不会有 “ja”、“va”，代码在编译器阶段被优化成了"Java"

##### **阳：
> 更新太慢了

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### **国：
> 跟着大佬长知识！

##### *杰：
> 求快点更新

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，

##### **萍：
> 求更新的快一点，等不及看下一课的

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 好的，您的意见已反馈给讲师，后期会加快更新节凑

##### *伟：
> 为啥还没更新😭

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 该课程每周三、周五更新哈

##### *健：
> 讲的很好，就是更新太慢了。

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 您的意见我们已将反馈给讲师，后续会加快更新节奏。目前该课程每周三、周五更新哈

##### **旭：
> 为何只能看第一讲

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 该课程每周三、周五更新哈，每周更新两个课时

##### **炎：
> 更新好慢的，具体更新时间能精确到几点嘛😀

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 具体的更新时间还是要看排版的时间 不过更新完成之后会有短信提醒

##### *新：
> 赞。。还是说的有一些不知道的知识点，比如string 从堆 转移到 常量池的方法inter 。就是更新可以快些。不然 面试都过了😀😀😀

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 嗯嗯，您的意见我们已经反馈给讲师，后续会加快更新节奏

##### *涛：
> 课程不错 就是更新有点慢

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### **峰：
> 讲的很仔细又有源码，很棒，但是更新太慢了啊，34月是关键月份错过就难找了啊，跪求更新快点

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 好哒，我们已经把大家的需求反馈给讲师，后期会加快更新节凑～

##### **锋：
> 大佬，感觉<span style="font-size: 16.0125px;">&nbsp;String 和 StringBuilder、StringBuffer 的区别这个是高频面试题，可以讲深一点，文中这样讲面试过不去的</span>😂

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 感谢反馈，建议配合 Java 源码查看，源码不是很大，读完就能做到心中有数了。

##### *胜：
> 非常好，建议更新快一些，谢谢

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 已反馈给讲师，后期会加快更新节凑

##### *仁：
> 王老师你好，这一张有个问题，在类上面是用final是表示这个类是最终类，不可被继承。跟实际的内容变化应该没关系，如果希望string这个变量不可变应该是声明变量的前面加final。不知道我的理解有没有误，希望王老师抽空可以解答一下

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 对的，你的理解没问题，final 语义是这样的。

##### **鹏：
> 第二讲更新还要多久？觉得讲的很详细

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 每周三、五更新，记得按时来听课哦

##### **可：
> 讲的挺好的

##### **2021：
> 讲的确实很全，决定学下去

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 加油哦，后面的课程更精彩～

