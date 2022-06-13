---
title: Java-为什么禁止把SimpleDateFormat定义成static变量?
toc: true
date: 2021-07-21 20:06:46
tags: Java
categories: [Develop Lan,Java]
---

> 本文参照 《Java技术灵魂15问》


## 简介
在日常开发中，我们经常会用到时间，我们有很多办法在 Java 代码中获取时 间。但是不同的方法获取到的时间的格式都不尽相同，这时候就需要一种格式化工 具，把时间显示成我们需要的格式。

最常用的方法就是使用 SimpleDateFormat 类。这是一个看上去功能比较简单 的类，但是，一旦使用不当也有可能导致很大的问题。
在 Java 开发手册中，有如下明确规定:

<img src="https://oscimg.oschina.net/oscnet/up-8b016ecefbdfc6ea675aaf7f2a0511bbc6a.png">

那么，本文就围绕 SimpleDateFormat 的用法、原理等来深入分析下如何以正 确的姿势使用它。

SimpleDateFormat 是 Java 提供的一个格式化和解析日期的工具类。它允许进 行格式化(日期 -> 文本)、解析(文本 -> 日期)和规范化。SimpleDateFormat 使 得可以选择任何用户定义的日期 - 时间格式的模式。

在 Java 中，可以使用 SimpleDateFormat 的 format 方法，将一个 Date 类型 转化成 String 类型，并且可以指定输出格式。

## SimpleDateFormat 用法

```java
 // Date转String
Date data = new Date();
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String dataStr = sdf.format(data);
System.out.println(dataStr);
```

以上代码，转换的结果是:2018-11-25 13:00:00，日期和时间格式由”日期 和时间模式”字符串指定。如果你想要转换成其他格式，只要指定不同的时间模式就 行了。

在 Java 中，可以使用 SimpleDateFormat 的 parse 方法，将一个 String 类型 转化成 Date 类型。

```java
// String转Data 
System.out.println(sdf.parse(dataStr));
```


## 日期和时间模式表达方法

在使用 SimpleDateFormat 的时候，需要通过字母来描述时间元素，并组装成 想要的日期和时间模式。常用的时间元素和字母的对应表如下:

<img src='https://oscimg.oschina.net/oscnet/up-80bd4dd81e9c6fb2f0fe9c3b5eae1cef2b8.png'>

模式字母通常是重复的，其数量确定其精确表示。如下表是常用的输出格式的表 示方法。

<img src="https://oscimg.oschina.net/oscnet/up-0c95fadb15ae4ca6aedb2e8cb68ce7ec50b.png">


## 输出不同时区的时间

时区是地球上的区域使用同一个时间定义。以前，人们通过观察太阳的位置(时 角)决定时间，这就使得不同经度的地方的时间有所不同(地方时)。1863 年，首次 使用时区的概念。时区通过设立一个区域的标准时间部分地解决了这个问题。

世界各个国家位于地球不同位置上，因此不同国家，特别是东西跨度大的国家日 出、日落时间必定有所偏差。这些偏差就是所谓的时差。

现今全球共分为 24 个时区。由于实用上常常 1 个国家，或 1 个省份同时跨着 2 个或更多时区，为了照顾到行政上的方便，常将 1 个国家或 1 个省份划在一起。所以 时区并不严格按南北直线来划分，而是按自然条件来划分。例如，中国幅员宽广，差 不多跨 5 个时区，但为了使用方便简单，实际上在只用东八时区的标准时即北京时间 为准。

由于不同的时区的时间是不一样的，甚至同一个国家的不同城市时间都可能不一 样，所以，在 Java 中想要获取时间的时候，要重点关注一下时区问题。
默认情况下，如果不指明，在创建日期的时候，会使用当前计算机所在的时区作为默认时区，这也是为什么我们通过只要使用new Date()就可以获取中国的当前 时间的原因。

那么，如何在 Java 代码中获取不同时区的时间呢? SimpleDateFormat 可以 实现这个功能。

```java
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles")); 
System.out.println(sdf.format(Calendar.getInstance().getTime()));
```

以上代码，转换的结果是:2018-11-24 21:00:00 。既中国的时间是 11 月 25 日的 13 点，而美国洛杉矶时间比中国北京时间慢了 16 个小时(这还和冬夏令时有关 系，就不详细展开了)。

如果你感兴趣，你还可以尝试打印一下美国纽约时间(America/New_York)。 纽约时间是 2018-11-25 00:00:00。纽约时间比中国北京时间早了 13 个小时。

当然，这不是显示其他时区的唯一方法，不过本文主要为了介绍 SimpleDate-Format，其他方法暂不介绍了。


## SimpleDateFormat 线程安全性

由于 SimpleDateFormat 比较常用，而且在一般情况下，一个应用中的时间显 示模式都是一样的，所以很多人愿意使用如下方式定义 SimpleDateFormat:

```java
public class Main {
        private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public static void main(String[] args) {
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            System.out.println(simpleDateFormat.format(Calendar.getInstance().
                    getTime()));
        }
    }
```

⚠️ 这种定义方式，存在很大的安全隐患。


我们来看一段代码，以下代码使用线程池来执行时间输出。

```java
public class Main {
        /**
         * 定义一个全局的SimpleDateFormat
         */
        private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /**
         * 使用ThreadFactoryBuilder定义一个线程池
         */
        private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
        private static ExecutorService pool = new ThreadPoolExecutor(5, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new
                ThreadPoolExecutor.AbortPolicy());
        /**
         * 定义一个CountDownLatch，保证所有子线程执行完之后主线程再执行
         */
        private static CountDownLatch countDownLatch = new CountDownLatch(100);

        public static void main(String[] args) {
        // 定义一个线程安全的 HashSet
            Set<String> dates = Collections.synchronizedSet(new HashSet<String>());
            for (int i = 0; i < 100; i++) {
                // 获取当前时间
                Calendar calendar = Calendar.getInstance();
                int finalI = i;
                pool.execute(() -> {
        // 时间增加
                    calendar.add(Calendar.DATE, finalI);
        // 通过 simpleDateFormat 把时间转换成字符串
                    String dateString = simpleDateFormat.format(calendar.
                            getTime());
                    // 把字符串放入 Set 中
                    dates.add(dateString); 
                    //countDown countDownLatch.countDown();
                });
            }
            // 阻塞，直到 countDown 数量为 0 countDownLatch.await();
        // 输出去重后的时间个数 System.out.println(dates.size());
        }
    }
```


以上代码，其实比较简单，很容易理解。就是循环一百次，每次循环的时候都在 当前时间基础上增加一个天数(这个天数随着循环次数而变化)，然后把所有日期放入 一个线程安全的、带有去重功能的 Set 中，然后输出 Set 中元素个数。

正常情况下，以上代码输出结果应该是 100。但是实际执行结果是一个小于 100 的数字。

原因就是因为 SimpleDateFormat 作为一个非线程安全的类，被当做了共享变 量在多个线程中进行使用，这就出现了线程安全问题。

## 线程不安全原因

通过以上代码，我们发现了在并发场景中使用 SimpleDateFormat 会有线程安 全问题。其实，JDK 文档中已经明确表明了 SimpleDateFormat 不应该用在多线程 场景中:

> Date formats are not synchronized. It is recommended to create separate format instances for each thread. If multiple threads access a format concurrently, it must be synchronized externally.

那么接下来分析下为什么会出现这种问题，SimpleDateFormat 底层到底是怎 么实现的?
我们跟一下 SimpleDateFormat 类中 format 方法的实现其实就能发现端倪。

```java
// Called from Format after creating a FieldDelegate
    private StringBuffer format(Date date, StringBuffer toAppendTo,
                                FieldDelegate delegate) {
        // Convert input date to time field list
        calendar.setTime(date);

        boolean useDateFormatSymbols = useDateFormatSymbols();

        for (int i = 0; i < compiledPattern.length; ) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i++] & 0xff;
            if (count == 255) {
                count = compiledPattern[i++] << 16;
                count |= compiledPattern[i++];
            }

            switch (tag) {
            case TAG_QUOTE_ASCII_CHAR:
                toAppendTo.append((char)count);
                break;

            case TAG_QUOTE_CHARS:
                toAppendTo.append(compiledPattern, i, count);
                i += count;
                break;

            default:
                subFormat(tag, count, delegate, toAppendTo, useDateFormatSymbols);
                break;
            }
        }
        return toAppendTo;
    }
```

SimpleDateFormat 中的 format 方法在执行过程中，会使用一个成员变量 calendar 来保存时间。这其实就是问题的关键。

由于我们在声明 SimpleDateFormat 的时候，使用的是 static 定义的。那么 这 个 SimpleDateFormat就是一个共享变量， 随 之，SimpleDateFormat 中 的 calendar 也就可以被多个线程访问到。

假设线程 1 刚刚执行完 calendar.setTime 把时间设置成 2018-11-11，还 没等执行完，线程 2 又执行了 calendar.setTime 把时间改成了 2018-12-12。 这时候线程 1 继续往下执行，拿到的 calendar.getTime 得到的时间就是线程 2 改 过之后的。

除了 format 方法以外，SimpleDateFormat 的 parse 方法也有同样的问题。 所以，不要把 SimpleDateFormat 作为一个共享变量使用。


## 如何解决线程安全问题

- 使用局部变量
   不要使用static
- 加同步锁
```java
 for (int i = 0; i < 100; i++) { // 获取当前时间
            Calendar calendar = Calendar.getInstance();
            int finalI = i;
            pool.execute(() -> {
                // 加锁
                synchronized (simpleDateFormat) {
                    // 时间增加
                    calendar.add(Calendar.DATE, finalI);
                    // 通过 simpleDateFormat 把时间转换成字符串
                    String dateString = simpleDateFormat.format(calendar.getTime()); // 把字符串放入 Set 中
                    dates.add(dateString);
                    //countDown
                    countDownLatch.countDown();
                }
            });
        }
```

其实以上代码还有可以改进的地方，就是可以把锁的粒度再设置的小一点，可以 只对 simpleDateFormat.format 这一行加锁，这样效率更高一些。
- 使用 ThreadLocal
   第三种方式，就是使用 ThreadLocal。 ThreadLocal 可以确保每个线程都可以 得到单独的一个 SimpleDateFormat 的对象，那么自然也就不存在竞争问题了。

```java
 /**
         * 使用ThreadLocal定义一个全局的SimpleDateFormat */
        private static ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
        };
        // 用法
        String dateString = simpleDateFormatThreadLocal.get().format(calendar.getTime());
```

用 ThreadLocal 来实现其实是有点类似于缓存的思路，每个线程都有一个独享 的对象，避免了频繁创建对象，也避免了多线程的竞争。

当然，以上代码也有改进空间，就是，其实 SimpleDateFormat 的创建过程可 以改为延迟加载。这里就不详细介绍了。


## 使用 DateTimeFormatter

如果是 Java8 应用，可以使用 DateTimeFormatter 代替 SimpleDateFormat， 这是一个线程安全的格式化工具类。就像官方文档中说的，这个类 simple beautiful strong immutable thread-safe。

```java
    // 解析日期
    String dateStr = "2016年10月25日";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    LocalDate date = LocalDate.parse(dateStr, formatter);
    // 日期转换为字符串
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy年MM月dd日 hh:mm a");
    String nowStr = now.format(format);
    System.out.println(nowStr);
```


## 总结
本 文 介 绍 了 SimpleDateFormat 的 用 法，SimpleDateFormat 主 要 可 以 在 String 和 Date 之间做转换，还可以将时间转换成不同时区输出。同时提到在并发场 景中 SimpleDateFormat 是不能保证线程安全的，需要开发者自己来保证其安全性。

主要的几个手段有改为局部变量、使用 synchronized 加锁、使用 Threadlocal 为每一个线程单独创建一个等。












