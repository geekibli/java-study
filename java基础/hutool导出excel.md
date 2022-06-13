---
title: hutool导出excel
toc: true
date: 2021-07-14 21:01:13
tags: Java
categories: [Develop Lan,Java]
---

> 如果你仅需一个Java导出excel的工具，👇就可以满足你的临时需求，当然代码下面这么写肯定是不规范的，可以稍后完善！

## 添加依赖
```xml
<!-- https://mvnrepository.com/artifact/cn.hutool/hutool-all -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.7.4</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.0.0</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.0.0</version>
        </dependency>
```

## 数据类
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IPData {
    private String ip;
}
```

## Export方法示例
```java
 public void export(List<IPData> rows) throws FileNotFoundException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.renameSheet("所有数据");     //甚至sheet的名称
        writer.addHeaderAlias("ip", "IP");
        writer.write(rows, true);
        writer.setOnlyAlias(true);
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/gaolei/Desktop/IP1.xlsx");
        writer.flush(fileOutputStream);
        writer.close();
    }
```




