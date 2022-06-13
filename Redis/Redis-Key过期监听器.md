---
title: Redis-Key过期监听器
toc: true
date: 2021-08-25 11:40:30
tags: Redis
categories:
---



## 背景

有一个需求，我在某平台发布了一片文章，需要判断这片文章在发布之后，10min，30min，1h，3h，1d，3d时间点的点赞数量和关注数量，但是呢，平台没有提供信息统计的功能，那么只能我定期去查看。

那么如何实现这个功能或者需求呢？

当时首先想到的是定时任务轮训，这种方式其实比较简单，就是搂数据库，判断时间就完事了，同时记录这片文章定时任务执行了多少次，超过一定次数之后，设置标志位，那么下次就不需要筛选这些文章了。

但是这种方式的缺点很明显，首先定时任务执行的频率改如何设置呢，应该是最小时间10min。也就是每10min搂一次库，查出来的数据，再去执行业务逻辑。当数据量很大的时候，这个定时任务就会显得比较重了。

于是我想到了基于事件触发的方式去解决这个问题，比如延时队列，redis过期策略啊等等，应该有很多。

这里说到延时队列，为什么我没有用JDK自带的DelayQueue呢，毕竟这些数据都是放在内存中，还是解决不了内存的问题。

还有通过redis的sort set数据结果来做的方式，score存的是时间戳，这种方式其实要比直接搂数据库要好的多。

最后我选择使用redis过期监听策略来实现这个需求，各位大佬们有什么别的方案呢？

## redis过期监听

### 首先设置一下redis的通知事件

需要设置redis配置文件 `notify-keyspace-events Ex`

```shell
# For instance if keyspace events notification is enabled, and a client
# performs a DEL operation on key "foo" stored in the Database 0, two
# messages will be published via Pub/Sub:
#
# PUBLISH __keyspace@0__:foo del
# PUBLISH __keyevent@0__:del foo
#  K     Keyspace events, published with __keyspace@<db>__ prefix.
#  E     Keyevent events, published with __keyevent@<db>__ prefix.
#  g     Generic commands (non-type specific) like DEL, EXPIRE, RENAME, ...
#  $     String commands
#  l     List commands
#  s     Set commands
#  h     Hash commands
#  z     Sorted set commands
#  x     Expired events (events generated every time a key expires)
#  e     Evicted events (events generated when a key is evicted for maxmemory)
#  t     Stream commands
#  d     Module key type events
#  m     Key-miss events (Note: It is not included in the 'A' class)
#  A     Alias for g$lshzxetd, so that the "AKE" string means all the events
#        (Except key-miss events which are excluded from 'A' due to their
#         unique nature).
```

或者使用命令 `CONFIG set notify-keyspace-events Ex`

### Springboot 集成redis

```java
@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public <T> RedisTemplate<String, T> getRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericFastJsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericFastJsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericFastJsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericFastJsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    public RedisScript<Boolean> hitMaxScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/hitmax.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.setTaskExecutor(executor());
        Topic topic = new PatternTopic(RedisKeyExpirationListener.LISTENER_PATTERN);
        container.addMessageListener(new RedisKeyExpirationListener(), topic);
        return container;
    }

    @Bean
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("V-Thread");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

### 自定义监听器重写onMessage方法

```java
@Component
public class RedisKeyExpirationListener implements MessageListener {
    public static final String LISTENER_PATTERN = "__key*@*__:*";
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.err.println("触发监听器。。。。。。");
        String body = new String(message.getBody());
        String channel = new String(message.getChannel());
        System.out.println("onMessage >> "+String.format("channel: %s, body: %s, bytes: %s",channel,body,new String(pattern)));
    }
}
```

## 测试

![image-20210825120806260](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210825120806260.png)

**项目控制台：**

![image-20210825120841859](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210825120841859.png)



⚠️ 监听key过期时间是不能获取key的value的，因为这个时间是key过期才触发的，所以我们把关键信息放到key上就行了

