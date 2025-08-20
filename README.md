#  比价系统

## 项目简介

这是一个基于Spring Cloud微服务架构的比价系统本系统集成jd,淘宝,拼多多.


## 前端地址 
https://github.com/kkky1/comprice_mall_front.git


### 微服务组件
- **Nacos**: 服务注册与配置中心
- **Gateway**: 统一网关路由
- **OpenFeign**: 服务间通信
- **Sentinel**: 流量控制与熔断降级
- **RabbitMQ**: 消息队列
- **Redis**: 缓存存储
- **MySQL**: 数据持久化

## 系统模块

### 1. 网关服务 (Gateway)

### 2. 用户登录服务 (Login)

### 3. 商品服务 (Goods)

### 4. 论坛服务 (Forum)

### 5. 支付服务 (Payment)

### 6. 公共模块 (Common)

### 7. 异常处理模块 (Exception)

## 主要功能

### 商品价格监控系统
- **价格爬取**: 通过爬虫API获取商品最新价格
- **趋势分析**: 记录商品价格变化历史
- **智能提醒**: 当商品价格下降时自动通知订阅用户
- **订阅管理**: 用户可以订阅感兴趣的商品价格监控

### 用户认证系统
- **JWT令牌**: 基于JWT的无状态认证
- **Redis缓存**: 用户会话信息缓存
- **OpenFeign**: 服务间用户信息传递

### 微服务通信
- **服务发现**: 通过Nacos自动发现服务
- **负载均衡**: 支持多实例负载均衡
- **熔断降级**: Sentinel保护服务稳定性



## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- Nacos 2.0+

### 启动步骤

1. **启动基础服务**
   ```bash
   # 启动Nacos
   # 启动Redis
   # 启动RabbitMQ
   # 启动MySQL
   ```

2. **启动Sentinel Dashboard**
   ```bash
   # 下载并启动Sentinel Dashboard
   java -jar sentinel-dashboard.jar --server.port=8858
   ```



## 配置说明

### 数据库配置
- 数据库名: `compare_price_mall`
- 字符集: UTF-8
- 时区: Asia/Shanghai

### Redis配置
- 默认端口: 6379
- 序列化方式: StringRedisSerializer + GenericJackson2JsonRedisSerializer

### RabbitMQ配置
- 默认端口: 5672
- 虚拟主机: /
- 用户名: admin
- 密码: admin

### Sentinel配置
- Dashboard地址: 127.0.0.1:8858
- 客户端端口: 8719

## 监控与运维

### Sentinel监控
- 实时监控接口调用情况
- 流量控制规则配置
- 熔断降级策略
- 热点参数限流

### 日志配置
- 商品服务: DEBUG级别
- Sentinel: DEBUG级别
- 统一日志格式

## 开发说明

### 代码结构
```
cloud-demo/
├── common/          # 公共模块
├── exception/       # 异常处理
├── gateway/         # 网关服务
├── login/           # 用户服务
├── goods/           # 商品服务
├── forum/           # 论坛服务
├── payment/         # 支付服务
└── pom.xml          # 父POM
```

### 开发规范
- 使用Lombok减少样板代码
- 统一异常处理
- 统一响应格式
- 完善的日志记录

## 注意事项

1. **端口冲突**: 确保各服务端口不冲突
2. **依赖顺序**: 先启动Nacos等基础服务
3. **配置检查**: 确保各服务配置文件正确
4. **网络连通**: 确保服务间网络连通性

## 联系方式

如有问题，请查看日志或联系开发团队。

---

*本项目仅供学习和演示使用*
