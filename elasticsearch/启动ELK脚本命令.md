---
title: 启动ELK脚本命令
toc: true
date: 2021-07-14 23:37:17
tags: elasticsearch
categories: [Elasticsearch, Administration and Deployment]
---



## esuser 授权
`chown -R esuser /usr/local/elasticsearch/*`

## elastcisearch 启动脚本
`nohup ./elasticsearch-7.10.0/bin/elasticsearch >> ./elasticsearch-7.10.0/nohup.out 2>&1 &`

## kibana 启动脚本
`nohup ./bin/kibana >> ./nohup.out 2>&1 &`

## logstash 启动脚本
`nohup /usr/local/logstash/logstash-7.10.0/bin/logstash -f /usr/local/logstash/logstash-7.10.0/config/redtom-logstash.conf`

`nohup /usr/local/logstash/logstash-7.10.0/bin/logstash -f /usr/local/logstash/logstash-7.10.0/config/redtom-logstash.conf >> /usr/local/logstash/logstash-7.10.0/nohup.out 2>&1 &`