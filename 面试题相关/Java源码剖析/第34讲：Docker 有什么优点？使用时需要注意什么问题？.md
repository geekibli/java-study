<p data-nodeid="3">Docker 从 2013 年发展到现在，它的普及率已经可以和最常用的 MySQL 和 Redis 并驾齐驱了，从最初偶尔出现在面试中，到现在几乎成为面试中必问的问题之一。如果再不了解 Docker 相关的知识点，可能就会与自己心仪的职位擦肩而过。所以本课时将会带领你对 Docker 相关的知识做一个全面的认识。</p>
<p data-nodeid="4">我们本课时的面试题是，Docker  是什么？它有什么优点？</p>
<h3 data-nodeid="5">典型回答</h3>
<p data-nodeid="6">Docker 是一个开源（开放源代码）的应用容器引擎，可以方便地对容器进行管理。可通过 Docker 打包各种环境应用配置，比如安装 JDK 环境、发布自己的 Java 程序等，然后再把它发布到任意 Linux 机器上。</p>
<p data-nodeid="7">Docker 中有三个重要的概念，具体如下。</p>
<ul data-nodeid="1447">
<li data-nodeid="1448">
<p data-nodeid="1449"><strong data-nodeid="1459">镜像（Image）</strong>：一个特殊的文件操作系统，除了提供容器运行时所需的程序、库、资源、配置等文件外，还包含了一些为运行时准备的配置参数（如匿名卷、环境变量、用户等）， 镜像不包含任何动态数据，其内容在构建之后也不会被改变。</p>
</li>
<li data-nodeid="1450">
<p data-nodeid="1451"><strong data-nodeid="1464">容器（Container）</strong>：它是用来运行镜像的。例如，我们拉取了一个 MySQL 镜像之后，只有通过创建并启动 MySQL 容器才能正常的运行 MySQL，容器可以进行创建、启动、停止、删除、暂停等操作。</p>
</li>
<li data-nodeid="1452">
<p data-nodeid="1453"><strong data-nodeid="1473">仓库（Repository）</strong>：用来存放镜像文件的地方，我们可以把自己制作的镜像上传到仓库中，Docker 官方维护了一个公共仓库 Docker Hub，<a href="https://hub.docker.com" data-nodeid="1471">你也可以点击这里查询并下载所有的公共镜像</a>。</p>
</li>
</ul>
<p data-nodeid="1454" class=""><img src="https://s0.lgstatic.com/i/image/M00/2B/9F/CgqCHl7-xpKAFgHdAAEShADSk60188.png" alt="Drawing 0.png" data-nodeid="1476"></p>


<p data-nodeid="16">在 Docker 出现之前，我们如果要发布自己的 Java 程序，就需要在服务器上安装 JDK（或者 JRE）、Tomcat 容器，然后配置 Tomcat 参数，对 JVM 参数进行调优等操作。然而如果要在多台服务器上运行 Java 程序，则需要将同样繁杂的步骤在每台服务器都重复执行一遍，这样显然比较耗时且笨拙的。</p>
<p data-nodeid="17">后来有了虚拟机的技术，我们就可以将配置环境打包到一个虚拟机镜像中，然后在需要的服务器上装载这些虚拟机，从而实现了运行环境的复制，但虚拟机会占用很多的系统资源，比如内存资源和硬盘资源等，并且虚拟机的运行需要加载整个操作系统，这样就会浪费掉好几百兆的内存资源，最重要的是因为它需要加载整个操作系统所以它的运行速度就很慢，并且还包含了一些我们用不到的冗余功能。</p>
<p data-nodeid="18">因为虚拟机的这些缺点，所以在后来就有了 Linux 容器（Linux Containers，LXC），它是一种进程级别的 Linux 容器，用它可以模拟一个完整的操作系统。相比于虚拟机来说，Linux 容器所占用的系统资源更少，启动速度也更快，因为它本质上是一个进程而非真实的操作系统，因此它的启动速度就比较快。</p>
<p data-nodeid="2293">而 Docker 则是对 Linux 容器的一种封装，并提供了更加方便地使用接口，所以 Docker 一经推出就迅速流行起来。Docker 和虚拟机（VM）区别如下图所示：</p>
<p data-nodeid="2294" class=""><img src="https://s0.lgstatic.com/i/image/M00/2B/9F/CgqCHl7-xqKAdySsAARl-ihH0Cc421.png" alt="Drawing 1.png" data-nodeid="2298"></p>


<p data-nodeid="21">Docker 具备以下 6 个优点。</p>
<ul data-nodeid="22">
<li data-nodeid="23">
<p data-nodeid="24"><strong data-nodeid="133">轻量级</strong>：Docker 容器主要利用并共享主机内核，它并不是完整的操作系统，因此它更加轻量化。</p>
</li>
<li data-nodeid="25">
<p data-nodeid="26"><strong data-nodeid="138">灵活</strong>：它可以将复杂的应用程序容器化，因此它非常灵活和方便。</p>
</li>
<li data-nodeid="27">
<p data-nodeid="28"><strong data-nodeid="143">可移植</strong>：可以在本地构建 Docker 容器，并把它部署到云服务器或任何地方进行使用。</p>
</li>
<li data-nodeid="29">
<p data-nodeid="30"><strong data-nodeid="148">相互隔离，方便升级</strong>：容器是高度自给自足并相互隔离的容器，这样就可以在不影响其他容器的情况下更换或升级你的 Docker 容器了。</p>
</li>
<li data-nodeid="31">
<p data-nodeid="32"><strong data-nodeid="153">可扩展</strong>：可以在数据中心内增加并自动分发容器副本。</p>
</li>
<li data-nodeid="33">
<p data-nodeid="34"><strong data-nodeid="158">安全</strong>：Docker 容器可以很好地约束和隔离应用程序，并且无须用户做任何配置。</p>
</li>
</ul>
<h3 data-nodeid="35">考点分析</h3>
<p data-nodeid="36">通过此面试题可以考察出面试者是否真的使用或了解过 Docker 技术，然而对于面试官来说，最关注的是面试者是否了解 Docker 和 Java 程序结合时会出现的一些问题，因此这部分的内容需要读者特别留意一下。</p>
<p data-nodeid="37">和此知识点相关的面试题还有以下这些：</p>
<ul data-nodeid="38">
<li data-nodeid="39">
<p data-nodeid="40">Docker 的常用命令有哪些？</p>
</li>
<li data-nodeid="41">
<p data-nodeid="42">在 Docker 中运行 Java 程序可能会存在什么问题？</p>
</li>
</ul>
<h3 data-nodeid="43">知识扩展</h3>
<h4 data-nodeid="44">Docker 常用命令</h4>
<p data-nodeid="45">我们在安装了 Docker Disktop（客户端）就可以用 docker --version 命令来查看 Docker 的版本号，使用示例如下：</p>
<pre class="lang-java" data-nodeid="46"><code data-language="java">$ docker --version
Docker version <span class="hljs-number">19.03</span>.<span class="hljs-number">8</span>, build afacb8b
</code></pre>
<p data-nodeid="3939">然后可以到 Docker Hub 上查找我们需要的镜像，比如 Redis 镜像，如下图所示：</p>
<p data-nodeid="3940" class=""><img src="https://s0.lgstatic.com/i/image/M00/2B/9F/CgqCHl7-xsCAC__xAATWzSiVbKU657.png" alt="Drawing 2.png" data-nodeid="3944"></p>




<p data-nodeid="4761">接着我们选择并点击最多人下载的镜像，如下图所示：</p>
<p data-nodeid="4762" class=""><img src="https://s0.lgstatic.com/i/image/M00/2B/93/Ciqc1F7-xseAVVs3AANxQpDC9RM481.png" alt="Drawing 3.png" data-nodeid="4766"></p>


<p data-nodeid="50">从描述中找到我们需要装载 Redis 的版本，然后使用 docker pull redis@ 版本号来拉取相关的镜像，或者使用 docker pull redis 直接拉取最新（版本）的 Redis 镜像，如下所示：</p>
<pre class="lang-powershell" data-nodeid="51"><code data-language="powershell"><span class="hljs-variable">$</span> docker pull redis
<span class="hljs-keyword">Using</span> default tag: latest
latest: Pulling from library/redis
Digest: sha256:<span class="hljs-number">800</span>f2587bf3376cb01e6307afe599ddce9439deafbd4fb8562829da96085c9c5
Status: Image is up to date <span class="hljs-keyword">for</span> redis:latest
docker.io/library/redis:latest
</code></pre>
<p data-nodeid="52">紧接着就可以使用 docker images 命令来查看所有下载的镜像，如下所示：</p>
<pre class="lang-powershell" data-nodeid="53"><code data-language="powershell"><span class="hljs-variable">$</span> docker images
REPOSITORY TAG IMAGE ID CREATED SIZE
redis latest <span class="hljs-number">235592615444</span> <span class="hljs-number">13</span> days ago <span class="hljs-number">104</span>MB
</code></pre>
<p data-nodeid="54">有了镜像之后我们就可以使用 docker run 来<strong data-nodeid="183">创建并运行容器</strong>了，使用命令如下：</p>
<pre class="lang-powershell" data-nodeid="55"><code data-language="powershell"><span class="hljs-variable">$</span> docker run -<span class="hljs-literal">-name</span> myredis <span class="hljs-literal">-d</span> redis
<span class="hljs-number">22</span>f560251e68b5afb5b7b52e202dcb3d47327f2136700d5a17bca7e37fc486bf
</code></pre>
<p data-nodeid="56">查看运行中的容器，命令如下：</p>
<pre class="lang-powershell" data-nodeid="57"><code data-language="powershell">￥ docker <span class="hljs-built_in">ps</span>
CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES
<span class="hljs-number">22</span>f560251e68 redis <span class="hljs-string">"docker-entrypoint.s…"</span> About a minute ago Up About a minute <span class="hljs-number">6379</span>/tcp myredis
</code></pre>
<p data-nodeid="58">其中，“myredis”为容器的名称，“6379/tcp”为 Redis 的端口号，容器的 ID 为“22f560251e68”。<br>
最后我们使用如下命令来连接 Redis：</p>
<pre class="lang-powershell" data-nodeid="59"><code data-language="powershell"><span class="hljs-variable">$</span> docker&nbsp; exec <span class="hljs-literal">-it</span> myredis&nbsp; redis<span class="hljs-literal">-cli</span>
<span class="hljs-number">127.0</span>.<span class="hljs-number">0.1</span>:<span class="hljs-number">6379</span>&gt;&nbsp;
</code></pre>
<p data-nodeid="60">其他常用命令如下：</p>
<ul data-nodeid="61">
<li data-nodeid="62">
<p data-nodeid="63">容器停止：docker stop 容器名称</p>
</li>
<li data-nodeid="64">
<p data-nodeid="65">启动容器：docker start 容器名称</p>
</li>
<li data-nodeid="66">
<p data-nodeid="67">删除容器：docker rm 容器名称</p>
</li>
<li data-nodeid="68">
<p data-nodeid="69">删除镜像：docker rmi 镜像名称</p>
</li>
<li data-nodeid="70">
<p data-nodeid="71">查看运行的所有容器：docker ps</p>
</li>
<li data-nodeid="72">
<p data-nodeid="73">查看所有容器：docker ps -a</p>
</li>
<li data-nodeid="74">
<p data-nodeid="75">容器复制文件到物理机：docker cp 容器名称:容器目录 物理机目录</p>
</li>
<li data-nodeid="76">
<p data-nodeid="77">物理机复制文件到容器：docker cp 物理机目录 容器名称:容器目录</p>
</li>
</ul>
<h4 data-nodeid="78">Docker 可能存在的问题</h4>
<p data-nodeid="79">Java 相对于 Docker 来说显然具有更悠久的历史，因此在早期的 Java 版本中（JDK 8u131）因为不能很好地识别 Docker 相关的配置信息，从而导致可能会出现 Java 程序意外被终止的情况或者是过度创建线程数而导致并发性能下降的问题。</p>
<p data-nodeid="80">Java 程序意外终止的主要原因是因为，在 Docker 中运行的 Java 程序因为没有明确指定 JVM 堆和直接内存等参数，而 Java 程序也不能很好地识别 Docker 的相关容量配置，导致 Java 程序试图获取了超过 Docker 本身的容量，而被 Docker 容器强制结束进程的情况（这是 Docker 自身的防御保护机制）。</p>
<p data-nodeid="81">过度创建线程是因为早期的 Java 版本并不能很好地识别 Docker 容器的 CPU 资源，因此会错误地识别和创建过多的线程数。比如 ParallelStreams 和 ForkJoinPool 等，它们默认就是根据当前系统的 CPU 核心数来创建对应的线程数的，但因为在 Docker 中的 Java 程序并不能很好地识别 CPU 核心数，就会导致创建的线程数量大于 CPU 的核心数量，从而导致并发效率降低的情况。</p>
<p data-nodeid="82">ParallelStreams 的基本用法如下：</p>
<pre class="lang-java" data-nodeid="83"><code data-language="java">List&lt;Integer&gt; numbers = Arrays.asList(<span class="hljs-number">1</span>, <span class="hljs-number">2</span>, <span class="hljs-number">3</span>, <span class="hljs-number">4</span>, <span class="hljs-number">5</span>, <span class="hljs-number">6</span>, <span class="hljs-number">7</span>, <span class="hljs-number">8</span>, <span class="hljs-number">9</span>);
numbers.parallelStream().forEach(count -&gt; {
    System.out.println(<span class="hljs-string">"val:"</span> + count);
});
</code></pre>
<p data-nodeid="84">ParallelStreams 是将任务提交给 ForkJoinPool 来实现的，ForkJoinPool 获取本地 CPU 核心数的源码如下：</p>
<pre class="lang-java" data-nodeid="85"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> ForkJoinPool <span class="hljs-title">makeCommonPool</span><span class="hljs-params">()</span> </span>{
    <span class="hljs-comment">// 忽略其他代码</span>
    <span class="hljs-keyword">if</span> (parallelism &lt; <span class="hljs-number">0</span> &amp;&amp; <span class="hljs-comment">// default 1 less than #cores</span>
        (parallelism = Runtime.getRuntime().availableProcessors() - <span class="hljs-number">1</span>) &lt;= <span class="hljs-number">0</span>)
        parallelism = <span class="hljs-number">1</span>;
    <span class="hljs-keyword">if</span> (parallelism &gt; MAX_CAP)
        parallelism = MAX_CAP;
    <span class="hljs-keyword">return</span> <span class="hljs-keyword">new</span> ForkJoinPool(parallelism, factory, handler, LIFO_QUEUE,
                            <span class="hljs-string">"ForkJoinPool.commonPool-worker-"</span>);
}
</code></pre>
<p data-nodeid="5389">其中，“Runtime.getRuntime().availableProcessors()”是用来获取本地线程数量的。</p>
<p data-nodeid="5390">要解决以上这些问题的方法，最简单的解决方案就是升级 Java 版本，比如 Java 10 就可以很好地识别 Docker 容器的这些限制。但如果使用的是老版本的 Java，那么需要在启动 JVM 的时候合理的配置堆、元数据区等内存区域大小，并指定 ForkJoinPool 的最大线程数，如下所示：</p>



<pre class="lang-java" data-nodeid="15924"><code data-language="java">-Djava.util.concurrent.ForkJoinPool.common.parallelism=<span class="hljs-number">8</span>
</code></pre>


























<h3 data-nodeid="88">小结</h3>
<p data-nodeid="89">本课时我们介绍了 Docker 的概念以及 Docker 中最重要的三个组件：镜像、容器和仓库，并且介绍了 Docker 的 6 大特点：轻量级、灵活、可移植、相互隔离、可扩展和安全等特点；同时还介绍了 Docker 的常见使用命令；最后介绍了 Docker 可能在老版本（JDK 8u131 之前）的 Java 中可能会存在意外停止和线程创建过多的问题以及解决方案。</p>
<p data-nodeid="90">通过以上内容的学习相信你对 Docker 已经有了一个系统的认识，需要特别注意是 Docker 在 Java 老版本中可能出现的问题以及解决方案，这一点在面试中经常会被问到。</p>
<p data-nodeid="91" class="te-preview-highlight">OK，这一课时就讲到这里啦，恭喜你已经学习完了关于本系列的所有课程。如果你觉得课程不错，从中有所收获的话，不要忘了推荐给身边的朋友哦，最后希望大家都有所提高、不断成长，谢谢~</p>

---

### 精选评论

##### *剑：
> 可以多给一些java在docker中运行需要注意的点和需要避开的坑

##### **润：
> 拿个沙发吧，都没人到这里评论

##### **江：
> 冒个泡

