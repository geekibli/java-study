<p data-nodeid="1345" class="">Netty 的高性能架构，是基于一个网络编程设计模式 Reactor 进行设计的。现在，大多数与 I/O 相关的组件，都会使用 Reactor 模型，比如 Tomcat、Redis、Nginx 等，可见 Reactor 应用的广泛性。</p>
<p data-nodeid="1346">Reactor 是 NIO 的基础。为什么 NIO 的性能就能够比传统的阻塞 I/O 性能高呢？我们首先来看一下传统阻塞式 I/O 的一些特点。</p>
<h3 data-nodeid="1347">阻塞 I/O 模型</h3>
<p data-nodeid="1348"><img src="https://s0.lgstatic.com/i/image/M00/48/9C/CgqCHl9MynKADFW4AAB9PAD7ZA0902.png" alt="Drawing 1.png" data-nodeid="1483"></p>
<p data-nodeid="1349">如上图，是典型的<strong data-nodeid="1489">BIO 模型</strong>，每当有一个连接到来，经过协调器的处理，就开启一个对应的线程进行接管。如果连接有 1000 条，那就需要 1000 个线程。</p>
<p data-nodeid="1350">线程资源是非常昂贵的，除了占用大量的内存，还会占用非常多的 CPU 调度时间，所以 BIO 在连接非常多的情况下，效率会变得非常低。</p>
<p data-nodeid="1351">下面的代码是使用 ServerSocket 实现的一个简单 Socket 服务器，监听在 8888 端口。</p>
<pre class="lang-java" data-nodeid="1352"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">BIO</span> </span>{
 &nbsp; &nbsp;<span class="hljs-keyword">static</span> <span class="hljs-keyword">boolean</span> stop = <span class="hljs-keyword">false</span>;

 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> <span class="hljs-keyword">throws</span> Exception </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> connectionNum = <span class="hljs-number">0</span>;
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> port = <span class="hljs-number">8888</span>;
 &nbsp; &nbsp; &nbsp; &nbsp;ExecutorService service = Executors.newCachedThreadPool();
 &nbsp; &nbsp; &nbsp; &nbsp;ServerSocket serverSocket = <span class="hljs-keyword">new</span> ServerSocket(port);
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">while</span> (!stop) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">if</span> (<span class="hljs-number">10</span> == connectionNum) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;stop = <span class="hljs-keyword">true</span>;
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Socket socket = serverSocket.accept();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;service.execute(() -&gt; {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">try</span> {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Scanner scanner = <span class="hljs-keyword">new</span> Scanner(socket.getInputStream());
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;PrintStream printStream = <span class="hljs-keyword">new</span> PrintStream(socket.getOutputStream());
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">while</span> (!stop) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;String s = scanner.next().trim();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;printStream.println(<span class="hljs-string">"PONG:"</span> + s);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  } <span class="hljs-keyword">catch</span> (Exception ex) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;ex.printStackTrace();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  });
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;connectionNum++;
 &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp;service.shutdown();
 &nbsp; &nbsp; &nbsp; &nbsp;serverSocket.close();
 &nbsp;  }
}
</code></pre>
<p data-nodeid="1353">启动之后，使用 nc 命令进行连接测试，结果如下。</p>
<pre class="lang-java" data-nodeid="1354"><code data-language="java">$ nc -v localhost <span class="hljs-number">8888</span>
Connection to localhost port <span class="hljs-number">8888</span> [tcp/ddi-tcp-<span class="hljs-number">1</span>] succeeded!
hello
PONG:hello
nice
PONG:nice
</code></pre>
<p data-nodeid="1355">使用 “04 | 工具实践：如何获取代码性能数据？”提到的 JMC 工具，在录制期间发起多个连接，能够发现有多个线程在运行，和连接数是一一对应的。</p>
<p data-nodeid="1356"><img src="https://s0.lgstatic.com/i/image/M00/48/9C/CgqCHl9MyoiAGgY5AAGbD3wkqUs988.png" alt="Drawing 2.png" data-nodeid="1498"></p>
<p data-nodeid="1357">可以看到，BIO 的读写操作是阻塞的，线程的整个生命周期和连接的生命周期是一样的，而且不能够被复用。</p>
<p data-nodeid="1358">就单个阻塞 I/O 来说，它的效率并不比 NIO 慢。但是当服务的连接增多，考虑到整个服务器的资源调度和资源利用率等因素，NIO 就有了显著的效果，NIO 非常适合高并发场景。</p>
<h3 data-nodeid="1359">非阻塞 I/O 模型</h3>
<p data-nodeid="1360">其实，在处理 I/O 动作时，有大部分时间是在等待。比如，socket 连接要花费很长时间进行连接操作，在完成连接的这段时间内，它并没有占用额外的系统资源，但它只能阻塞等待在线程中。这种情况下，系统资源并不能被合理利用。</p>
<p data-nodeid="1361">Java 的 NIO，在 Linux 上底层是使用 epoll 实现的。epoll 是一个高性能的多路复用 I/O 工具，改进了 select 和 poll 等工具的一些功能。<strong data-nodeid="1507">在网络编程中，对 epoll 概念的一些理解，几乎是面试中必问的问题。</strong></p>
<p data-nodeid="1362">epoll 的数据结构是直接在内核上进行支持的，通过 epoll_create 和 epoll_ctl 等函数的操作，可以构造描述符（fd）相关的事件组合（event）。</p>
<p data-nodeid="1363">这里有两个比较重要的概念：</p>
<ul data-nodeid="1364">
<li data-nodeid="1365">
<p data-nodeid="1366">fd 每条连接、每个文件，都对应着一个描述符，比如端口号。内核在定位到这些连接的时候，就是通过 fd 进行寻址的。</p>
</li>
<li data-nodeid="1367">
<p data-nodeid="1368">event 当 fd 对应的资源，有状态或者数据变动，就会更新 epoll_item 结构。在没有事件变更的时候，epoll 就阻塞等待，也不会占用系统资源；一旦有新的事件到来，epoll 就会被激活，将事件通知到应用方。</p>
</li>
</ul>
<p data-nodeid="1369"><strong data-nodeid="1521">关于 epoll 还会有一个面试题，相对于 select，epoll 有哪些改进？</strong></p>
<p data-nodeid="1370">你可以这样回答：</p>
<ul data-nodeid="1371">
<li data-nodeid="1372">
<p data-nodeid="1373">epoll 不再需要像 select 一样对 fd 集合进行轮询，也不需要在调用时将 fd 集合在用户态和内核态进行交换；</p>
</li>
<li data-nodeid="1374">
<p data-nodeid="1375">应用程序获得就绪 fd 的事件复杂度，epoll 是 O(1)，select 是 O(n)；</p>
</li>
<li data-nodeid="1376">
<p data-nodeid="1377">select 最大支持约 1024 个 fd，epoll 支持 65535个；</p>
</li>
<li data-nodeid="1378">
<p data-nodeid="1379">select 使用轮询模式检测就绪事件，epoll 采用通知方式，更加高效。</p>
</li>
</ul>
<p data-nodeid="1380">我们还是以 Java 中的 NIO 代码为例，来看一下 NIO 的具体概念。</p>
<pre class="lang-java" data-nodeid="1381"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">NIO</span> </span>{
 &nbsp; &nbsp;<span class="hljs-keyword">static</span> <span class="hljs-keyword">boolean</span> stop = <span class="hljs-keyword">false</span>;

 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> <span class="hljs-keyword">throws</span> Exception </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> connectionNum = <span class="hljs-number">0</span>;
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> port = <span class="hljs-number">8888</span>;
 &nbsp; &nbsp; &nbsp; &nbsp;ExecutorService service = Executors.newCachedThreadPool();

 &nbsp; &nbsp; &nbsp; &nbsp;ServerSocketChannel ssc = ServerSocketChannel.open();
 &nbsp; &nbsp; &nbsp; &nbsp;ssc.configureBlocking(<span class="hljs-keyword">false</span>);
 &nbsp; &nbsp; &nbsp; &nbsp;ssc.socket().bind(<span class="hljs-keyword">new</span> InetSocketAddress(<span class="hljs-string">"localhost"</span>, port));

 &nbsp; &nbsp; &nbsp; &nbsp;Selector selector = Selector.open();
 &nbsp; &nbsp; &nbsp; &nbsp;ssc.register(selector, ssc.validOps());


 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">while</span> (!stop) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">if</span> (<span class="hljs-number">10</span> == connectionNum) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;stop = <span class="hljs-keyword">true</span>;
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> num = selector.select();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">if</span> (num == <span class="hljs-number">0</span>) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">continue</span>;
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Iterator&lt;SelectionKey&gt; events = selector.selectedKeys().iterator();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">while</span> (events.hasNext()) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;SelectionKey event = events.next();

 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">if</span> (event.isAcceptable()) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;SocketChannel sc = ssc.accept();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;sc.configureBlocking(<span class="hljs-keyword">false</span>);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;sc.register(selector, SelectionKey.OP_READ);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;connectionNum++;
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  } <span class="hljs-keyword">else</span> <span class="hljs-keyword">if</span> (event.isReadable()) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">try</span> {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;SocketChannel sc = (SocketChannel) event.channel();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;ByteBuffer buf = ByteBuffer.allocate(<span class="hljs-number">1024</span>);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> size = sc.read(buf);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">if</span>(-<span class="hljs-number">1</span>==size){
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;sc.close();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;String result = <span class="hljs-keyword">new</span> String(buf.array()).trim();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;ByteBuffer wrap = ByteBuffer.wrap((<span class="hljs-string">"PONG:"</span> + result).getBytes());
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;sc.write(wrap);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  } <span class="hljs-keyword">catch</span> (Exception ex) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;ex.printStackTrace();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  } <span class="hljs-keyword">else</span> <span class="hljs-keyword">if</span> (event.isWritable()) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;SocketChannel sc = (SocketChannel) event.channel();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }

 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;events.remove();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp;service.shutdown();
 &nbsp; &nbsp; &nbsp; &nbsp;ssc.close();
 &nbsp;  }
}
</code></pre>
<p data-nodeid="1382">上面这段代码比较长，是使用 NIO 实现的和 BIO 相同的功能。从它的 API 设计上，我们就能够看到 epoll 的一些影子。</p>
<p data-nodeid="1383">首先，我们创建了一个服务端 ssc，并开启一个新的事件选择器，监听它的 OP_ACCEPT 事件。</p>
<pre class="lang-yaml" data-nodeid="1384"><code data-language="yaml"><span class="hljs-string">ServerSocketChannel</span> <span class="hljs-string">ssc</span> <span class="hljs-string">=</span> <span class="hljs-string">ServerSocketChannel.open();</span>
<span class="hljs-string">Selector</span> <span class="hljs-string">selector</span> <span class="hljs-string">=</span> <span class="hljs-string">Selector.open();</span>
<span class="hljs-string">ssc.register(selector,</span> <span class="hljs-string">ssc.validOps());</span>
</code></pre>
<p data-nodeid="1385">共有 4 种事件类型，分别是：</p>
<ul data-nodeid="1386">
<li data-nodeid="1387">
<p data-nodeid="1388">新连接事件（OP_ACCEPT）；</p>
</li>
<li data-nodeid="1389">
<p data-nodeid="1390">连接就绪事件（OP_CONNECT）；</p>
</li>
<li data-nodeid="1391">
<p data-nodeid="1392">读就绪事件（OP_READ）；</p>
</li>
<li data-nodeid="1393">
<p data-nodeid="1394">写就绪事件（OP_WRITE）。</p>
</li>
</ul>
<p data-nodeid="1395">任何网络和文件操作，都可以抽象成这四个事件。</p>
<p data-nodeid="1396"><img src="https://s0.lgstatic.com/i/image/M00/48/91/Ciqc1F9MyqmAdmlrAAMSNPAj_F4698.png" alt="Drawing 3.png" data-nodeid="1548"></p>
<p data-nodeid="1397">接下来，在 while 循环里，使用 select 函数，阻塞在主线程里。所谓<strong data-nodeid="1554">阻塞</strong>，就是操作系统不再分配 CPU 时间片到当前线程中，所以 select 函数是几乎不占用任何系统资源的。</p>
<pre class="lang-java" data-nodeid="1398"><code data-language="java"><span class="hljs-keyword">int</span> num = selector.select();
</code></pre>
<p data-nodeid="1399">一旦有新的事件到达，比如有新的连接到来，主线程就能够被调度到，程序就能够向下执行。这时候，就能够根据订阅的事件通知，持续获取订阅的事件。<br>
由于注册到 selector 的连接和事件可能会有多个，所以这些事件也会有多个。我们使用安全的迭代器循环进行处理，在处理完毕之后，将它删除。</p>
<blockquote data-nodeid="1400">
<p data-nodeid="1401">这里留一个思考题：如果事件不删除的话，或者漏掉了某个事件的处理，会有什么后果？</p>
</blockquote>
<pre class="lang-java" data-nodeid="1402"><code data-language="java">Iterator&lt;SelectionKey&gt; events = selector.selectedKeys().iterator();
 &nbsp; &nbsp;<span class="hljs-keyword">while</span> (events.hasNext()) {
 &nbsp; &nbsp; &nbsp; &nbsp;SelectionKey event = events.next();
 &nbsp; &nbsp; &nbsp;  ...
 &nbsp; &nbsp; &nbsp; &nbsp;events.remove();
 &nbsp;  }
}
</code></pre>
<p data-nodeid="1403">有新的连接到达时，我们订阅了更多的事件。对于我们的数据读取来说，对应的事件就是 OP_READ。和 BIO 编程面向流的方式不同，NIO 操作的对象是抽象的概念 Channel，通过缓冲区进行数据交换。</p>
<pre class="lang-java" data-nodeid="1404"><code data-language="java">SocketChannel sc = ssc.accept();
sc.configureBlocking(<span class="hljs-keyword">false</span>);
sc.register(selector, SelectionKey.OP_READ);
</code></pre>
<p data-nodeid="1405">值得注意的是：服务端和客户端的实现方式，可以是不同的。比如，服务端是 NIO，客户端可以是 BIO，它们并没有什么强制要求。</p>
<p data-nodeid="1406"><strong data-nodeid="1571">另外一个面试时候经常问到的事件就是 OP_WRITE</strong>。我们上面提到过，这个事件是表示写就绪的，当底层的缓冲区有空闲，这个事件就会一直发生，浪费占用 CPU 资源。所以，我们一般是不注册 OP_WRITE 的。</p>
<p data-nodeid="1407">这里还有一个细节，在读取数据的时候，并没有像 BIO 的方式一样使用循环来获取数据。</p>
<p data-nodeid="1408">如下面的代码，我们创建了一个 1024 字节的缓冲区，用于数据的读取。如果连接中的数据，大于 1024 字节怎么办？</p>
<pre class="lang-java" data-nodeid="1409"><code data-language="java">SocketChannel sc = (SocketChannel) event.channel();
ByteBuffer buf = ByteBuffer.allocate(<span class="hljs-number">1024</span>);
<span class="hljs-keyword">int</span> size = sc.read(buf);
</code></pre>
<p data-nodeid="1410">这涉及两种事件的通知机制：</p>
<ul data-nodeid="1411">
<li data-nodeid="1412">
<p data-nodeid="1413"><strong data-nodeid="1579">水平触发</strong>(level-triggered) 称作 LT 模式。只要缓冲区有数据，事件就会一直发生</p>
</li>
<li data-nodeid="1414">
<p data-nodeid="1415"><strong data-nodeid="1584">边缘触发</strong>(edge-triggered) 称作 ET 模式。缓冲区有数据，仅会触发一次。事件想要再次触发，必须先将 fd 中的数据读完才行</p>
</li>
</ul>
<p data-nodeid="1416">可以看到，Java 的 NIO 采用的就是水平触发的方式。LT 模式频繁环唤醒线程，效率相比较ET模式低，所以 Netty 使用 JNI 的方式，实现了 ET 模式，效率上更高一些。</p>
<h3 data-nodeid="1417">Reactor 模式</h3>
<p data-nodeid="1418">了解了 BIO 和 NIO 的一些使用方式，Reactor 模式就呼之欲出了。</p>
<p data-nodeid="1419">NIO 是基于事件机制的，有一个叫作 Selector 的选择器，阻塞获取关注的事件列表。获取到事件列表后，可以通过分发器，进行真正的数据操作。</p>
<p data-nodeid="1420"><img src="https://s0.lgstatic.com/i/image/M00/48/91/Ciqc1F9MysaAZw9aAADxUNI1q_I139.png" alt="Drawing 5.png" data-nodeid="1591"></p>
<blockquote data-nodeid="1421">
<p data-nodeid="1422">该图来自 Doug Lea 的<a href="http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf" data-nodeid="1595">《</a><a href="http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf" data-nodeid="1598">Scalable IO in Java》</a>，该图指明了最简单的 Reactor 模型的基本元素。</p>
</blockquote>
<p data-nodeid="1423">你可以回看下我在上文举例的 “Java 中的 NIO 代码”，对比分析一下，你会发现 Reactor</p>
<p data-nodeid="1424">模型 里面有四个主要元素：</p>
<ul data-nodeid="1425">
<li data-nodeid="1426">
<p data-nodeid="1427"><strong data-nodeid="1606">Acceptor</strong>处理 client 的连接，并绑定具体的事件处理器；</p>
</li>
<li data-nodeid="1428">
<p data-nodeid="1429"><strong data-nodeid="1611">Event</strong>具体发生的事件，比如图中s的read、send等；</p>
</li>
<li data-nodeid="1430">
<p data-nodeid="1431"><strong data-nodeid="1616">Handler</strong>执行具体事件的处理者，比如处理读写事件的具体逻辑；</p>
</li>
<li data-nodeid="1432">
<p data-nodeid="1433"><strong data-nodeid="1621">Reactor</strong>将具体的事件分配（dispatch）给 Handler。</p>
</li>
</ul>
<p data-nodeid="1434">我们可以对上面的模型进行进一步细化，如下图所示，将 Reactor 分为 mainReactor 和 subReactor 两部分。</p>
<p data-nodeid="1435"><img src="https://s0.lgstatic.com/i/image/M00/48/91/Ciqc1F9MytSAebMfAAFlMTAoyJQ496.png" alt="Drawing 7.png" data-nodeid="1625"></p>
<blockquote data-nodeid="1436">
<p data-nodeid="1437">该图来自 Doug Lea 的 <a href="http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf" data-nodeid="1629">《Scalable IO in Java》</a></p>
</blockquote>
<ul data-nodeid="1438">
<li data-nodeid="1439">
<p data-nodeid="1440"><strong data-nodeid="1634">mainReactor</strong>负责监听处理新的连接，然后将后续的事件处理交给 subReactor；</p>
</li>
<li data-nodeid="1441">
<p data-nodeid="1442"><strong data-nodeid="1639">subReactor</strong>对事件处理的方式，也由阻塞模式变成了多线程处理，引入了任务队列的模式。</p>
</li>
</ul>
<p data-nodeid="1443">熟悉 Netty 的同学可以看到，这个 Reactor 模型就是 Netty 设计的基础。在 Netty 中，Boss 线程对应着对连接的处理和分派，相当于 mainReactor；Worker 线程对应着 subReactor，使用多线程负责读写事件的分发和处理。</p>
<p data-nodeid="1444">这种模式将每个组件的职责分得更细，耦合度也更低，能有效解决 C10k 问题。</p>
<h3 data-nodeid="1445">AIO</h3>
<p data-nodeid="1446">关于 NIO 的概念，误解还是比较多的。</p>
<p data-nodeid="1447"><strong data-nodeid="1647">面试官可能会问你：为什么我在使用 NIO 时，使用 Channel 进行读写，socket 的操作依然是阻塞的？NIO 的作用主要体现在哪里？</strong></p>
<pre class="lang-java" data-nodeid="1448"><code data-language="java"><span class="hljs-comment">//这行代码是阻塞的</span>
<span class="hljs-keyword">int</span> size = sc.read(buf);
</code></pre>
<p data-nodeid="1449">这时你可以回答：NIO 只负责对发生在 fd 描述符上的事件进行通知。事件的获取和通知部分是非阻塞的，但收到通知之后的操作，却是阻塞的，即使使用多线程去处理这些事件，它依然是阻塞的。</p>
<p data-nodeid="1450">AIO 更近一步，将这些对事件的操作也变成非阻塞的。下面是一段典型的 AIO 代码，它通过注册 CompletionHandler&nbsp;回调函数进行事件处理。这里的事件是隐藏的，比如 read 函数，它不仅仅代表 Channel 可读了，而且会把数据自动的读取到 ByteBuffer 中。等完成了读取，就会通过回调函数通知你，进行后续的操作。</p>
<pre class="lang-java" data-nodeid="1451"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">AIO</span> </span>{
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> <span class="hljs-keyword">throws</span> Exception </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> port = <span class="hljs-number">8888</span>;
 &nbsp; &nbsp; &nbsp; &nbsp;AsynchronousServerSocketChannel ssc = AsynchronousServerSocketChannel.open();
 &nbsp; &nbsp; &nbsp; &nbsp;ssc.bind(<span class="hljs-keyword">new</span> InetSocketAddress(<span class="hljs-string">"localhost"</span>, port));
 &nbsp; &nbsp; &nbsp; &nbsp;ssc.accept(<span class="hljs-keyword">null</span>, <span class="hljs-keyword">new</span> CompletionHandler&lt;AsynchronousSocketChannel, Object&gt;() {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">job</span><span class="hljs-params">(<span class="hljs-keyword">final</span> AsynchronousSocketChannel sc)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;ByteBuffer buffer = ByteBuffer.allocate(<span class="hljs-number">1024</span>);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;sc.read(buffer, buffer, <span class="hljs-keyword">new</span> CompletionHandler&lt;Integer, ByteBuffer&gt;() {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-meta">@Override</span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">completed</span><span class="hljs-params">(Integer result, ByteBuffer attachment)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;String str = <span class="hljs-keyword">new</span> String(attachment.array()).trim();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;ByteBuffer wrap = ByteBuffer.wrap((<span class="hljs-string">"PONG:"</span> + str).getBytes());
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;sc.write(wrap, <span class="hljs-keyword">null</span>, <span class="hljs-keyword">new</span> CompletionHandler&lt;Integer, Object&gt;() {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-meta">@Override</span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">completed</span><span class="hljs-params">(Integer result, Object attachment)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;job(sc);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-meta">@Override</span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">failed</span><span class="hljs-params">(Throwable exc, Object attachment)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(<span class="hljs-string">"error"</span>);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  });
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-meta">@Override</span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">failed</span><span class="hljs-params">(Throwable exc, ByteBuffer attachment)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(<span class="hljs-string">"error"</span>);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  });
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-meta">@Override</span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">completed</span><span class="hljs-params">(AsynchronousSocketChannel sc, Object attachment)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;ssc.accept(<span class="hljs-keyword">null</span>, <span class="hljs-keyword">this</span>);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;job(sc);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-meta">@Override</span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">failed</span><span class="hljs-params">(Throwable exc, Object attachment)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;exc.printStackTrace();
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(<span class="hljs-string">"error"</span>);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp;  });
 &nbsp; &nbsp; &nbsp; &nbsp;Thread.sleep(Integer.MAX_VALUE);
 &nbsp;  }
}
</code></pre>
<p data-nodeid="1452">AIO 是 Java 1.7 加入的，理论上性能会有提升，但实际测试并不理想。这是因为，AIO主要处理对数据的自动读写操作。这些操作的具体逻辑，假如不放在框架中，也要放在内核中，并没有节省操作步骤，对性能的影响有限。而 Netty 的 NIO 模型加上多线程处理，在这方面已经做得很好，编程模式也比AIO简单。</p>
<p data-nodeid="1453">所以，市面上对 AIO 的实践并不多，在采用技术选型的时候，一定要谨慎。</p>
<h3 data-nodeid="1454">响应式编程</h3>
<p data-nodeid="1455">你可能听说过 Spring 5.0 的 WebFlux，WebFlux 是可以替代 Spring MVC 的一套解决方案，可以编写响应式的应用，两者之间的关系如下图所示：</p>
<p data-nodeid="1456"><img src="https://s0.lgstatic.com/i/image/M00/48/92/Ciqc1F9My2WAeCGbAACrOS4gYGA066.png" alt="image.png" data-nodeid="1656"></p>
<p data-nodeid="1457">Spring WebFlux 的底层使用的是 Netty，所以操作是异步非阻塞的，类似的组件还有 vert.x、akka、rxjava 等。</p>
<p data-nodeid="1458">WebFlux 是运行在 project reactor 之上的一个封装，其根本特性是后者提供的，至于再底层的非阻塞模型，就是由 Netty 保证的了。</p>
<p data-nodeid="1459">非阻塞的特性我们可以理解，那响应式又是什么概念呢？</p>
<p data-nodeid="1460"><strong data-nodeid="1664">响应式编程</strong>是一种面向数据流和变化传播的编程范式。这意味着可以在编程语言中很方便地表达静态或动态的数据流，而相关的计算模型会自动将变化的值，通过数据流进行传播。</p>
<p data-nodeid="1461">这段话很晦涩，在编程方面，它表达的意思就是：<strong data-nodeid="1669">把生产者消费者模式，使用简单的API 表示出来，并自动处理背压（Backpressure）问题。</strong></p>
<p data-nodeid="1462">背压，指的是生产者与消费者之间的流量控制，通过将操作全面异步化，来减少无效的等待和资源消耗。</p>
<p data-nodeid="1463">Java 的 Lambda 表达式可以让编程模型变得非常简单，Java 9 更是引入了响应式流（Reactive Stream），方便了我们的操作。</p>
<p data-nodeid="1464">比如，下面是 Spring Cloud GateWay 的 Fluent API 写法，响应式编程的 API 都是类似的。</p>
<pre class="lang-java" data-nodeid="1465"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> RouteLocator <span class="hljs-title">customerRouteLocator</span><span class="hljs-params">(RouteLocatorBuilder builder)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> builder.routes()
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .route(r -&gt; r.path(<span class="hljs-string">"/market/**"</span>)
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .filters(f -&gt; f.filter(<span class="hljs-keyword">new</span> RequestTimeFilter())
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .addResponseHeader(<span class="hljs-string">"X-Response-Default-Foo"</span>, <span class="hljs-string">"Default-Bar"</span>))
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .uri(<span class="hljs-string">"http://localhost:8080/market/list"</span>)
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .order(<span class="hljs-number">0</span>)
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .id(<span class="hljs-string">"customer_filter_router"</span>)
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  )
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  .build();
 &nbsp;  }
</code></pre>
<p data-nodeid="1466">从传统的开发模式过渡到 Reactor 的开发模式，是有一定成本的，不过它确实能够提高我们应用程序的性能，至于是否采用，这取决于你在编程难度和性能之间的取舍。</p>
<h3 data-nodeid="1467">小结</h3>
<p data-nodeid="1468">本课时，我们系统地学习了 BIO、NIO、AIO 等概念和基本的编程模型 Reactor，我们了解到：</p>
<ul data-nodeid="1681">
<li data-nodeid="1682">
<p data-nodeid="1683">BIO 的线程模型是一个连接对应一个线程的，非常浪费资源；</p>
</li>
<li data-nodeid="1684">
<p data-nodeid="1685" class="">NIO通过对关键事件的监听，通过主动通知的方式完成非阻塞操作，但它对事件本身的处理依然是阻塞的；</p>
</li>
<li data-nodeid="1686">
<p data-nodeid="1687">AIO 完全是异步非阻塞的，但现实中使用很少。</p>
</li>
</ul>

<p data-nodeid="1476">使用 Netty 的多 Acceptor 模式和多线程模式，我们能够方便地完成类似 AIO 这样的操作。Netty 的事件触发机制使用了高效的 ET 模式，使得支持的连接更多，性能更高。</p>
<p data-nodeid="1477" class="">使用 Netty，能够构建响应式编程的基础，加上类似 Lambda 表达式这样的书写风格，能够完成类似 WebFlux 这样的响应式框架。响应式编程是一个趋势，现在有越来越多的框架和底层的数据库支持响应式编程，我们的应用响应也会更加迅速。</p>

---

### 精选评论

##### **威：
> 这篇有点没跟上节奏

##### *锋：
> 如果事件不删除的话，或者漏掉了某个事件的处理，会有什么后果？==========会不会导致死循环

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 会的。由于每次判断都会有事件，就会造成select线程的频繁唤醒，进而造成CPU的使用飙升。

##### **生：
> 这得学多少东西 才能总结出这么牛的章节

##### **0161：
> 老师 对于这章节 有什么比较好的 的资源吗 入门到能回答上面试题

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 给你准备了一份脑图，可以按照这个去准备：http://mind.xjjdog.cn/mind/java-nio

##### **青：
> 被说中，“这章没跟上节奏”。或许这就使差距，有难度就跟不上，读着读着就神游了

##### *波：
> 精彩，佩服

##### **4060：
> 醍醐灌顶

