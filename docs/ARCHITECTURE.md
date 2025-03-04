# FiveBear 系统架构文档

## 1. 系统概述
```mermaid
graph TD
    A[用户终端] --> B[API网关]
    B -->|路由| C[认证服务]
    B -->|业务请求| D[业务处理集群]
    D -->|持久化| E[(核心数据库)]
    D -->|缓存读写| F[缓存集群]
    F -->|数据存储| G[(Redis)]
    style A fill:#90EE90,stroke:#333
    style B fill:#87CEEB,stroke:#333
    style C fill:#FFB6C1,stroke:#333
    style D fill:#D3D3D3,stroke:#333
    style E fill:#FFD700,stroke:#333
    style F fill:#FFA07A,stroke:#333
    style G fill:#98FB98,stroke:#333
```

## 2. 技术栈
### 后端架构
| 组件         | 技术选型           | 版本   | 架构决策依据                  |
|--------------|--------------------|--------|------------------------------|
| 开发语言     | Java               | 17     | 企业级应用生态支持            |
| Web框架      | Spring Boot        | 3.2.0  | 微服务全家桶整合              |
| 数据库       | PostgreSQL         | 15     | 事务一致性与GIS支持           |
| 缓存         | Redis              | 7.0    | 分布式会话管理                |
| 消息队列     | Apache Kafka       | 3.5    | 事件驱动架构基础              |
| 配置中心     | Nacos              | 2.2.3  | 动态配置管理                  |
| 容器化       | Docker             | 24.0   | 环境一致性保障                |

### 架构演进记录
```mermaid
timeline
    title 架构演进路线
    2023 Q3 : 单体架构 → 模块化拆分
    2024 Q1 : Spring Cloud Alibaba → 微服务治理
    2024 Q3 : 引入Service Mesh → 可观测性提升
    2025 Q1 : 云原生转型 → 弹性计算
```

### 前端架构
```mermaid
pie
    title 前端技术分布
    "React": 45
    "TypeScript": 30
    "Webpack": 15
    "其他": 10
```

## 3. 核心组件说明
### 认证服务架构
```mermaid
sequenceDiagram
    用户->>+认证服务: 登录请求
    认证服务->>LDAP: 身份验证
    LDAP-->>认证服务: 验证结果
    认证服务->>JWT: 生成令牌
    认证服务-->>用户: 返回访问令牌
```

## 4. 部署架构
```mermaid
flowchart TB
    subgraph 公有云
        A[CDN] -->|静态资源| B[负载均衡]
        B -->|流量分发| C[API集群]
        C -->|读写分离| D[数据库代理]
        D -->|主库写入| E[(PostgreSQL主库)]
        D -->|从库读取| F[(PostgreSQL只读副本)]
    end
    subgraph 私有云
        G[Prometheus] -->|指标采集| H[API集群]
        I[Elastic Agent] -->|日志收集| H
        J[Jaeger] -->|链路追踪| H
        H -->|监控数据| K[Grafana]
        style K fill:#F7941D,stroke:#333
    end
    style A fill:#90EE90,stroke:#333
    style B fill:#87CEEB,stroke:#333
    style C fill:#D3D3D3,stroke:#333
    style D fill:#FFB6C1,stroke:#333
    style E fill:#FFD700,stroke:#333
    style F fill:#98FB98,stroke:#333
    style G fill:#FFA07A,stroke:#333
    style I fill:#9370DB,stroke:#333
    style J fill:#00BFFF,stroke:#333
```

## 5. 数据流向
```mermaid
journey
    title 订单处理流程
    section 创建订单
        用户输入: 5: 用户
        验证库存: 3: 库存服务
        生成订单号: 4: 订单服务
    section 支付
        调用支付网关: 5: 支付服务
        更新订单状态: 2: 数据库
```

## 6. 监控体系
| 监控类型       | 工具               | 采集频率 |
|----------------|--------------------|----------|
| 应用性能       | Prometheus         | 15s      |
| 日志分析       | ELK Stack          | 实时     |
| 链路追踪       | Jaeger             | 按需     |
