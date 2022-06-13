---
title: docker-compost安装mongodb
toc: false
date: 2021-07-28 15:24:01
tags: mongodb
categories: [DataBase,MongoDB]
---

> mongo 配置文件 -> https://www.cnblogs.com/xibuhaohao/p/12580331.html

## docker-compose  配置文件
```yml
mongo:
    image: mongo:4.4.7 #根据需要选择自己的镜像
    ports:
     - 27017:27017 #对外暴露停供服务的端口，正式生产的时候理论不用暴露。
    volumes:
     - ./mongodb/data/db:/data/db # 挂载数据目录
     - ./mongodb/data/log:/var/log/mongodb  # 挂载日志目录
     - ./mongodb/data/config:/etc/mongo  # 挂载配置目录
    # command: --config /docker/mongodb/mongod.conf # 配置文件
```

## 按照上面👆配置文件设置目录
`/data/db/mongodb/data`
`ls -l`
`config  db  log`


## mongo 配置文件
```yml
# Where and how to store data.
storage:
  dbPath: /data/db/mongodb/data/db
  journal:
    enabled: true
#  engine:
#  mmapv1:
#  wiredTiger:

# where to write logging data.
systemLog:
  destination: file
  logAppend: true
  path: /data/db/mongodb/data/log

# network interfaces
net:
  port: 27017
  bindIp: 0.0.0.0


# how the process runs
processManagement:
  timeZoneInfo: /usr/share/zoneinfo

#security:

#operationProfiling:

#replication:

#sharding:

## Enterprise-Only Options:

#auditLog:

#snmp:
```

`bindIp: 0.0.0.0` 允许远程访问

## docker-compose启动mongo
`docker-compose up -d`
`docker ps`


## 进入docker
`docker ps`
`docker exec -it xxxxxxxxxx bash`


## mongo创建数据库
### 登录
`mongo`

### 查看数据库
`show dbs`

### 创建数据库
`use wechat_spider`  然后 `db` 查看

### 创建用户
```sql
db.createUser(
     {
       user:"wechat",
       pwd:"123456",
       roles:[{role:"readWrite",db:"wechat_spider"}]
     }
  )
```



## Java客户端链接

### 配置mvn
```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>pring-boot-starter-data-mongodb</artifactId>
    </dependency>

    <dependency>
        <groupId>org.mongodb</groupId>
        <artifactId>mongo-java-driver</artifactId>
        <version>3.0.2</version>
    </dependency>
```


### 配置文件
```yml
spring:
  data:
    mongodb:
      username: 'wechat'
      password: '123456'
#      port: 3333
      port: 27017
      database: wechat_spider
#      host: 123.56.77.177
      host: 39.107.117.232
      repositories:
        type: auto

```


### domain
```java
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @Author gaolei
 * @Date 2021/7/28 上午10:02
 * @Version 1.0
 */
@Document(collection = "passenger")
public class Passenger {

    private String name;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

### controller
```java
@RestController
public class TestContoller {

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/insert")
    public String insert() {
        Passenger passenger = new Passenger();
        passenger.setName("hello");
        passenger.setPassword("world1");
        passenger = mongoTemplate.insert(passenger);
        if (passenger != null) {
            return "success";
        } else {
            return "false";
        }
    }
}
```













