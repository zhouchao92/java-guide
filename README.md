# 概述
- 面试八股文
- 阅读笔记（[深入理解 Java 虚拟机：JVM 高级特性与最佳实践（第3版）周志明](/doc/深入Java虚拟机.md)）
- 笔记源码

## Java核心知识
### 基础
- [java 基本语法](/doc/java/基本语法.md)
- [集合](/doc/java/集合.md) 
	- [HashMap](/doc/java/HashMap.md)
	- [ConcurrentHashMap](/doc/java/ConcurrentHashMap.md)
- [io 模型](/doc/java/io模型.md)
- [Netty](/doc/java/Netty.md)
- [jdk8 新特性](/doc/java/jdk8%20新特性.md)

### juc
- [进程与线程](/doc/juc/进程与线程.md)
- [线程的生命周期](/doc/juc/线程的生命周期.md)
- [线程池](/doc/juc/线程池.md)
- [AQS抽象队列同步器](/doc/juc/AQS%20抽象队列同步器.md)
- [ReentrantLock原理](/doc/juc/ReentrantLock原理.md)
- [Synchronized原理](/doc/juc/Synchronized原理.md)
- [Volatile原理](/doc/juc/Volatile原理.md)
- [ThreadLocal原理](/doc/juc/ThreadLocal原理.md)

### jvm
- [内存模型](/doc/jvm/内存模型.md)
- [类加载](/doc/jvm/类加载.md)
- [双亲委派机制](/doc/jvm/双亲委派机制.md)
- [对象头 Markword](/doc/jvm/对象头%20Markword.md)
- [对象的生命周期](/doc/jvm/对象的生命周期.md)
- [GC怎么判断垃圾是否能被回收](/doc/jvm/GC怎么判断垃圾是否能被回收.md)
- [垃圾回收器](/doc/jvm/垃圾回收器.md)

## 数据库
### MySQL
- [SQL执行过程](/doc/mysql/SQL%20执行过程.md)
- [锁的划分](/doc/mysql/锁的划分.md)
- [索引](/doc/mysql/索引.md)
- [事务](/doc/mysql/事务.md)
- [MVCC原理](/doc/mysql/MVCC原理.md)
- [innodb是如何实现事务的](/doc/mysql/innodb是如何实现事务的.md)
- [表分区](/doc/mysql/表分区.md)
- [主从复制](/doc/mysql/主从复制.md)
- [单表优化](/doc/mysql/单表优化.md)

### Redis
- [布隆过滤器](/doc/redis/布隆过滤器.md)
- [Redis核心知识](/doc/redis/Redis%20核心知识.md)
- [9大数据结构](/doc/redis/9大数据结构.md)
- [持久化策略 rdb 与 aof](/doc/redis/持久化策略%20rdb%20与%20aof.md)
- [缓存一致性问题](/doc/redis/缓存一致性问题.md)
- [缓存穿透、缓存击穿、缓存雪崩](/doc/redis/缓存穿透、缓存击穿、缓存雪崩.md)
- [分布式锁原理](/doc/redis/Redis%20分布式锁原理.md)
- [锁丢失问题（分布式锁）](/doc/redis/锁丢失问题（分布式锁）.md)
- [高可用方案](/doc/redis/高可用方案.md)

### ElasticSearch
- [ElasticSearch 核心知识](/doc/elasticsearch/Elasticsearch%20核心知识.md)
- [倒排索引](/doc/elasticsearch/倒排索引.md)

## 框架
### Spring
- [Spring 核心知识点](/doc/spring/Spring%20核心知识点.md)
- [Spring 容器启动过程](/doc/spring/Spring%20容器启动过程.md)
- [Spring 循环依赖](/doc/spring/Spring%20循环依赖.md)
- [Spring Bean 的生命周期](/doc/spring/Spring%20Bean%20的生命周期.md)
- [Spring 事务传播机制](/doc/spring/Spring%20事务传播机制.md)
- [BeanFactory 和 FactoryBean 的区别](/doc/spring/BeanFactory%20和%20FactoryBean%20的区别.md)

### Spring MVC
- [Spring MVC核心知识点](/doc/spring%20mvc/Spring%20MVC%20核心知识点.md)

### Spring Boot
- [Spring Boot 的启动过程](/doc/spring%20boot/Spring%20Boot%20的启动过程.md)
- [Spring Boot 的自动装配原理](/doc/spring%20boot/Spring%20Boot%20的自动装配原理.md)

### Spring Cloud
- [nacos](/doc/spring%20cloud/nacos.md)
- [Ribbon 懒加载](/doc/spring%20cloud/Ribbon懒加载.md)
- [SpringSessison核心原理(装饰者模式)](/doc/spring%20cloud/SpringSessison核心原理(装饰者模式).md)

### MyBatis
- [查询实现分页](/doc/mybatis/查询实现分页.md)
- [缓存技术](/doc/mybatis/缓存技术.md)
- [源码分析](/doc/mybatis/源码分析.md)

## 消息队列
### kafka
- [ZAB协议](doc/kafka/ZAB协议.md) 
- [kafka 核心知识](/doc/kafka/Kafka%20核心知识.md)
- [kafka 基础概念](/doc/kafka/kafka%20基础概念.md)
- [kafka 技术架构](/doc/kafka/kafka%20技术架构.md)
- [kafka 高性能原因](/doc/kafka/kafka%20高性能原因.md)
- [kafka 可靠性传输](/doc/kafka/kafka%20可靠性传输.md)
- [kafka 消费模式](/doc/kafka/kafka%20消费模式.md)
- [kafka 顺序消费](/doc/kafka/kafka%20顺序消费.md)
- [kafka 副本同步](/doc/kafka/kafka%20副本同步.md)
- [kafka rebanlance 机制](/doc/kafka/kafka%20rebalance%20机制.md)

### rocketmq
- [rocketmq 核心知识](/doc/rocketmq/RocketMQ%20核心知识.md)

### rabbitmq
- [rabbitmq 核心知识](/doc/rabbitmq/RabbitMQ%20核心知识.md)

## 其他
- [RPC](/doc/other/什么是%20rpc.md)
- [微服务熔断降级](/doc/other/微服务熔断降级.md)
- [接口幂等性](/doc/other/接口幂等性.md) 
- [本地事务与分布式事务](/doc/other/本地事务与分布式事务.md)
- [DDD领域驱动设计](/doc/other/DDD领域驱动设计.md)
- [分布式方案](/doc/other/分布式方案.md) 
- [高并发架构](/doc/other/高并发架构.md)
- [Promethus](/doc/other/Prometheus.md)

