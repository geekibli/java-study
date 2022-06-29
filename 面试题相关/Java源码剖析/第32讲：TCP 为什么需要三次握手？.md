<p data-nodeid="1816" class="">TCP 协议是我们每天都在使用的一个网络通讯协议，因为绝大部分的网络连接都是建立在 TCP 协议上的，比如你此刻正在看的这篇文章是建立在 HTTP（Hypertext Transfer Protocol，超文本传送协议） 应用层协议的基础上的，而 HTTP 协议的“底层”则是建立在 TCP 的传输层协议上的。因此可以理解为，你之所以能看到本篇文章就是得益于 TCP 协议的功劳。</p>
<p data-nodeid="1817">我们本课时的面试题是，说一下 TCP 三次握手的执行流程，以及为什么需要三次握手？</p>
<h3 data-nodeid="1818">典型回答</h3>
<p data-nodeid="1819">在回答这个问题之前，首先我们需要搞清楚两个概念，第一，什么是 TCP？第二，什么是 TCP 连接？只有搞明白了这两个问题，我们才能彻底搞懂为什么 TCP 需要三次握手？</p>
<h4 data-nodeid="1820">什么是 TCP？</h4>
<p data-nodeid="1821">首先来说 TCP（Transmission Control Protocol，传输控制协议）是一个面向连接的、可靠的、基于字节流的传输层协议。从它的概念中我们可以看出 TCP 的三个特点：<strong data-nodeid="1921">面向连接、可靠性和面向字节流</strong>。</p>
<p data-nodeid="1822"><img src="https://s0.lgstatic.com/i/image/M00/26/68/CgqCHl7x4AWAIebKAABegWUqA1U920.png" alt="image (1).png" data-nodeid="1924"><br>
TCP 的特点</p>
<p data-nodeid="1823"><strong data-nodeid="1931">面向连接</strong>：是指 TCP 是面向客户端和服务器端连接的通讯协议，使用它可以将客户端和服务器端进行连接。</p>
<p data-nodeid="1824"><strong data-nodeid="1936">可靠性</strong>：是指无论网络环境多差，TCP 都可以保证信息一定能够传递到接收端。</p>
<p data-nodeid="1825">TCP 之所以可以保证可靠性主要得益于两个方面，一个是“状态性”，另一个是“可控制性”。所谓状态性是指 TCP 会记录信息的发送状态，例如，哪些数据收到了、哪些数据没收到等状态信息都会被记录；可控制性是指 TCP 会根据状态情况控制自己的行为，比如当 TCP 意识到丢包了就会控制重发此包，这样就实现了 TCP 的可靠性。</p>
<p data-nodeid="1826"><strong data-nodeid="1942">面向字节流</strong>：是指 TCP 是以字节流的方式进行数据传输的。</p>
<p data-nodeid="1827">RFC 793 对 TCP 连接的定义如下：</p>
<blockquote data-nodeid="1828">
<p data-nodeid="1829">Connections:<br>
The reliability and flow control mechanisms described above require that TCPs initialize and maintain certain status information for each data stream.<br>
The combination of this information, including sockets, sequence numbers, and window sizes, is called a connection.<br>
小贴士：TCP 之所以被广泛应用，首先是因为它是一个标准化的协议，TCP 的标准协议就是由 RFC 793 定义的，它已经有了 30 多年的历史，并且已经被多次更新。RFC（Request For Comments）是 IETF（Internet Engineering Task Force）的正式文档。IETF 是一家制定互联网标准的组织，它制定了 Internet（互联网）的整体协议体系，凡是经过 IETF 评审认可的标准都会被发布为带编号的 RFC 的文档。</p>
</blockquote>
<p data-nodeid="1830">TCP 定义的大致意思是，用于保证可靠性和流控制机制的信息，包括 Socket、序列号及窗口大小被称为连接。</p>
<p data-nodeid="1831">其中，Socket 是由 IP 地址加端口号组成的，序列号是用来解决乱序问题的，而窗口大小则是用来做流量控制的。</p>
<p data-nodeid="3232">接下来我们来看 TCP 三次握手的执行流程，如下图所示：</p>
<p data-nodeid="3233" class=""><img src="https://s0.lgstatic.com/i/image/M00/27/2D/CgqCHl70ccOALHS1AADhgTvLn9Q814.png" alt="22.png" data-nodeid="3237"><br>
TCP 三次握手的执行流程图</p>



<p data-nodeid="1834">关键字说明：</p>
<ul data-nodeid="1835">
<li data-nodeid="1836">
<p data-nodeid="1837">SYN（Synchronize Sequence Numbers），同步序列编号；</p>
</li>
<li data-nodeid="1838">
<p data-nodeid="1839">ACK（Acknowledge Character），确认字符；</p>
</li>
<li data-nodeid="1840">
<p data-nodeid="1841">SEQ（Sequence Number），序列号。</p>
</li>
</ul>
<p data-nodeid="1842">TCP 的执行流程如下：</p>
<ul data-nodeid="1843">
<li data-nodeid="1844">
<p data-nodeid="1845">最开始时客户端和服务端都处于 CLOSED 状态，然后服务端先主动监听某个端口，此时服务器端就变成了 LISTEN（监听）状态；</p>
</li>
<li data-nodeid="1846">
<p data-nodeid="1847">然后客户端主动发起连接，发送 SYN（同步序列编号），此时客户端就变成了 SYN-SENT 状态；</p>
</li>
<li data-nodeid="1848">
<p data-nodeid="1849">服务端接收到信息之后返回 SYN 和 ACK 至客户端，此时服务器端就变成了 SYN-REVD 状态；</p>
</li>
<li data-nodeid="1850">
<p data-nodeid="1851">客户端接收到消息之后，再发送 ACK 至服务器端，此时客户端就变成了 ESTABLISHED（已确认）状态，服务端收到 ACK 之后，也变成了 ESTABLISHED 状态，此时连接工作就执行完了。</p>
</li>
</ul>
<h4 data-nodeid="1852">为什么 TCP 需要三次握手？</h4>
<p data-nodeid="1853">了解了以上 TCP 的基础概念之后，我们再来看一下 TCP 为什么需要三次握手？</p>
<p data-nodeid="1854"><strong data-nodeid="1973">原因一：防止重复连接</strong></p>
<p data-nodeid="1855">首先来说 RFC 793 - Transmission Control Protocol 其实就指出了三次握手的主要原因，它的描述如下：</p>
<blockquote data-nodeid="1856">
<p data-nodeid="1857">The principle reason for the three-way handshake is to prevent old duplicate connection initiations from causing confusion.</p>
</blockquote>
<p data-nodeid="1858">翻译为中文的意思是，三次握手的主要原因是为了防止旧的重复连接引起连接混乱问题。</p>
<p data-nodeid="1859">比如在网络状况比较复杂或者网络状况比较差的情况下，发送方可能会连续发送多次建立连接的请求。如果 TCP 握手的次数只有两次，那么接收方只能选择接受请求或者拒绝接受请求，但它并不清楚这次的请求是正常的请求，还是由于网络环境问题而导致的过期请求，如果是过期请求的话就会造成错误的连接。</p>
<p data-nodeid="1860">所以如果 TCP 是三次握手的话，那么客户端在接收到服务器端 SEQ+1 的消息之后，就可以判断当前的连接是否为历史连接，如果判断为历史连接的话就会发送终止报文（RST）给服务器端终止连接；如果判断当前连接不是历史连接的话就会发送指令给服务器端来建立连接。</p>
<p data-nodeid="1861"><strong data-nodeid="1982">原因二：同步初始化序列化</strong></p>
<p data-nodeid="1862">通过上面的概念我们知道 TCP 的一个重要特征就是可靠性，而 TCP 为了保证在不稳定的网络环境中构建一个稳定的数据连接，它就需要一个“序列号”字段来保证自己的稳定性，而这个序列号的作用就是防止数据包重复发送，以及有效的解决数据包接收时顺序颠倒的问题。</p>
<p data-nodeid="1863">那么在建立 TCP 连接时就需要同步初始化一个序列号来保证 TCP 的稳定性，因此它需要执行以下过程：</p>
<ul data-nodeid="1864">
<li data-nodeid="1865">
<p data-nodeid="1866">首先客户端发送一个携带了初始序列号的 SYN 报文给服务器端；</p>
</li>
<li data-nodeid="1867">
<p data-nodeid="1868">服务端接收到消息之后会回复一个 ACK 的应答报文，表示客户端的 SYN 报文已被服务端成功接收了；</p>
</li>
<li data-nodeid="1869">
<p data-nodeid="1870">而客户端收到消息之后也会发送一个 ACK 给服务端，服务器端拿到这个消息之后，我们就可以得到一个可靠的初始化序列号了。</p>
</li>
</ul>
<p data-nodeid="1871">而如果是两次握手的话，就无法进行序列号的确认工作了，因此也就无法得到一个可靠的序列号了，所以 TCP 连接至少需要三次握手。</p>
<p data-nodeid="1872">以上两种原因就是 TCP 连接为什么需要三次握手的主要原因，当然 TCP 连接还可以四次握手，甚至是五次握手，也能实现 TCP 连接的稳定性，但三次握手是最节省资源的连接方式，因此 TCP 连接应该为三次握手。</p>
<h3 data-nodeid="1873">考点分析</h3>
<p data-nodeid="1874">TCP 知识是计算机编程基础，也是面试中常见的面试问题，因为我们现在所使用的大部分连接都是建立在 TCP 基础上的。因此对 TCP 的掌握可以让我们更清楚地理解技术的实现过程，也能帮我们写出更加优秀的代码，以及排查一些和网络相关的问题。</p>
<p data-nodeid="1875">和此知识点相关的面试题还有以下这些：</p>
<ul data-nodeid="1876">
<li data-nodeid="1877">
<p data-nodeid="1878">什么是 UDP？</p>
</li>
<li data-nodeid="1879">
<p data-nodeid="1880">TCP 和 UDP 有什么区别？</p>
</li>
</ul>
<h3 data-nodeid="1881">知识扩展</h3>
<h4 data-nodeid="1882">UDP 介绍</h4>
<p data-nodeid="1883">UDP（User Data Protocol，用户数据报协议）是无连接的、简单的、面向数据报的传输层协议。也就是 UDP 在发送数据之前，无须建立客户端与服务端的连接，直接发送消息即可。</p>
<p data-nodeid="4182">UDP 的协议头有 8 个字节（64 位），如下图所示：</p>
<p data-nodeid="4183" class="te-preview-highlight"><img src="https://s0.lgstatic.com/i/image/M00/27/2D/CgqCHl70cdGAPLl8AABHUQhxFtY478.png" alt="23.png" data-nodeid="4187"><br>
UDP 的协议头</p>



<p data-nodeid="1886">其中源端口和目标端口是指记录发送方和接收方端口；UDP 包长度是指 UDP 头部加上 UDP 数据的总长度；UDP 校验和用于效验 UDP 的内容是否可靠。</p>
<p data-nodeid="1887">UDP 常见的使用场景有：语音、视频等多媒体通信、DNS（域名转化）、TFTP 等。</p>
<h4 data-nodeid="1888">TCP VS UDP</h4>
<p data-nodeid="1889">TCP 和 UDP 的区别主要体现在以下 7 个方面：</p>
<ul data-nodeid="1890">
<li data-nodeid="1891">
<p data-nodeid="1892"><strong data-nodeid="2012">可靠性</strong>，TCP 有“状态性”和“可控制性”可以保证消息不重复、按顺序、不丢失的发送和接收，而 UDP 则不能保证消息的可靠性；</p>
</li>
<li data-nodeid="1893">
<p data-nodeid="1894"><strong data-nodeid="2017">连接</strong>，TCP 是面向连接的传输层协议，传输数据前先要建立连接，而 UDP 发送数据之前无需建立连接；</p>
</li>
<li data-nodeid="1895">
<p data-nodeid="1896"><strong data-nodeid="2022">服务对象</strong>，TCP 服务的对象为一对一的双端应用，而 UDP 可以应用于一对一、一对多和多对多的通信场景；</p>
</li>
<li data-nodeid="1897">
<p data-nodeid="1898"><strong data-nodeid="2027">效率</strong>，TCP 的传输效率较低，而 UDP 的传输效率较高；</p>
</li>
<li data-nodeid="1899">
<p data-nodeid="1900"><strong data-nodeid="2032">流量控制</strong>，TCP 有滑动窗口可以用来控制流量，而 UDP 则不具备流量控制的能力；</p>
</li>
<li data-nodeid="1901">
<p data-nodeid="1902"><strong data-nodeid="2037">报文</strong>，TCP 是面向字节流的传输层协议，而 UDP 是面向报文的传输层协议；</p>
</li>
<li data-nodeid="1903">
<p data-nodeid="1904"><strong data-nodeid="2042">应用场景</strong>，TCP 的应用场景是对消息准确性和顺序要求较高的场景，而 UDP 则是应用于对通信效率较高、准确性要求相对较低的场景。</p>
</li>
</ul>
<p data-nodeid="1905">TCP 和 UDP 的使用场景如下图所示：</p>
<p data-nodeid="1906"><img src="https://s0.lgstatic.com/i/image/M00/26/69/CgqCHl7x4EKAW86xAACoPgxtPLM601.png" alt="image (4).png" data-nodeid="2046"><br>
TCP 和 UDP 的使用场景</p>
<h3 data-nodeid="1907">小结</h3>
<p data-nodeid="1908">本课时我们介绍了 TCP 三个特点：面向连接、可靠性和面向字节流，其中可靠性主要是依赖它的状态记录和根据实际情况调整自身的行为方式。例如，当 TCP 意识到丢包时就会重发此包，这样就保证了通信的可靠性。</p>
<p data-nodeid="1909">TCP 之所以需要三次握手的主要原因是为了防止在网络环境比较差的情况下不会进行无效的连接，同时三次握手可以实现 TCP 初始化序列号的确认工作，TCP 需要初始化一个序列号来保证消息的顺序。如果是两次握手则不能确认序列号是否正常，如果是四次握手的话会浪费系统的资源，因此 TCP 三次握手是最优的解决方案，所以 TCP 连接需要三次握手。</p>
<p data-nodeid="1910">最后我们讲了 UDP 的概念，以及 UDP 和 TCP 的区别，在传输效率要求比较高且对可靠性要求不高的情况下可以使用 UDP，反之则应该使用 TCP。</p>

---

### 精选评论

##### **润：
> 面试一般问你为啥建立连接需要三次握手而断开连接却需要四次挥手😁

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 了解了3次握手再理解4次挥手就简单一些了，4次挥手可以理解为 A 向 B 发送：“我没消息了，要端口连接了哦”，然后 B 回复：“好的，收到了，等我处理完手头的事，再通知你断开”，第三次 B 发送：”ok，搞定了，我要断开了“，第四次 A 说：”OK，那就断开吧“。

##### **辉：
> 老师，四次挥手也是常问的？😀

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是的，四次挥手主要是因为 TCP 是全双工通信的，可配合其他资料理解一下哦。

##### **台：
> 流和包的区别，是怎么理解的呢

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 流可以理解为向水流一样，它默认情况下是没有边界的，需要人为的控制，比如 TCP 使用中的粘包和半包问题，本质上是没有控制好流的边界，详情可以参考：https://mp.weixin.qq.com/s/ODxGlLrohCveH-2m-BSDWQ 而包的概念通常指的一次消息发送的单位，它是有具体的边界的。

##### **青：
> 讲解的恰到好处

