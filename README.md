# FiveBear 管理系统

FiveBear 是一个现代化的企业管理系统，采用前后端分离架构，提供安全可靠、功能丰富的管理平台。

## 技术栈

### 后端
- Spring Boot 3.2.3 - 快速构建企业级应用
- Spring Security - 安全认证和授权
- Spring Data JPA - 数据持久化
- MySQL 8.0 - 关系型数据库
- Redis - 缓存和会话管理
- JWT 认证 - 无状态身份验证
- Knife4j (Swagger/OpenAPI) - API 文档生成
- Maven - 项目构建和依赖管理

### 前端
- Vue 3 - 渐进式 JavaScript 框架
- TypeScript - 类型安全的 JavaScript 超集
- Element Plus - Vue 3 组件库
- Vite - 现代前端构建工具
- Pinia - Vue 3 状态管理
- Vue Router - 路由管理
- Axios - HTTP 客户端

## 功能特性

- 🔐 安全认证
  - JWT 基于令牌的认证
  - 密码加密存储
  - 登录状态管理
  - 角色权限控制

- 🎨 现代化界面
  - 响应式设计
  - 优雅的动画效果
  - 用户友好的交互体验
  - 深色/浅色主题切换

- 🛠 系统配置
  - 灵活的系统参数配置
  - 用户权限管理
  - 系统监控
  - 日志管理

## 快速开始

### 环境要求
- JDK 17+ - Java 开发环境
- Node.js 16+ - 前端开发环境
- MySQL 8.0+ - 数据库服务
- Redis 6.0+ - 缓存服务
- Maven 3.8+ - 项目构建工具

### 后端启动
1. 克隆项目
```bash
git clone [项目地址]
```

2. 配置数据库
```bash
# 执行数据库初始化脚本
mysql -u root -p < init_db.sql
```

3. 修改配置
- 编辑 `application.properties` 文件，配置数据库连接信息
- 配置 Redis 连接信息
- 配置 JWT 密钥和其他系统参数

4. 启动项目
```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或使用批处理脚本
run.bat
```

### 前端启动
1. 进入前端目录
```bash
cd frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

## 项目结构

### 前端目录结构
```
frontend/
├── src/                    # 源代码目录
│   ├── api/               # API 接口定义
│   │   ├── user.ts        # 用户相关接口
│   │   ├── auth.ts        # 认证相关接口
│   │   └── system.ts      # 系统相关接口
│   ├── assets/            # 静态资源文件
│   │   ├── images/        # 图片资源
│   │   └── icons/         # 图标资源
│   ├── components/        # 公共组件
│   │   ├── common/        # 通用组件
│   │   └── business/      # 业务组件
│   ├── config/            # 配置文件
│   │   ├── constants.ts   # 常量配置
│   │   └── settings.ts    # 系统设置
│   ├── layout/            # 布局组件
│   │   ├── components/    # 布局相关组件
│   │   └── index.vue      # 主布局文件
│   ├── router/            # 路由配置
│   │   ├── index.ts       # 路由入口
│   │   └── modules/       # 路由模块
│   ├── stores/            # Pinia 状态管理
│   │   ├── user.ts        # 用户状态
│   │   └── app.ts         # 应用状态
│   ├── styles/            # 全局样式文件
│   │   ├── variables.scss # 样式变量
│   │   └── mixins.scss    # 样式混入
│   ├── types/             # TypeScript 类型定义
│   │   ├── api.d.ts       # API 类型
│   │   └── store.d.ts     # 状态类型
│   ├── utils/             # 工具函数
│   │   ├── request.ts     # 请求封装
│   │   └── auth.ts        # 认证工具
│   ├── views/             # 页面视图组件
│   │   ├── login/         # 登录相关
│   │   ├── dashboard/     # 仪表盘
│   │   └── system/        # 系统管理
│   ├── App.vue            # 根组件
│   ├── main.ts            # 应用入口文件
│   └── env.d.ts           # 环境变量类型声明
├── public/                # 公共静态资源
├── index.html             # HTML 模板
├── package.json           # 项目依赖配置
├── tsconfig.json          # TypeScript 配置
├── vite.config.ts         # Vite 配置
└── .env                   # 环境变量配置
```

### 后端目录结构
```
src/
├── main/
│   ├── java/              # Java 源代码
│   │   └── com/fivebear/  # 项目包路径
│   │       ├── config/    # 配置类
│   │       │   ├── SecurityConfig.java    # 安全配置
│   │       │   ├── RedisConfig.java       # Redis配置
│   │       │   └── SwaggerConfig.java     # Swagger配置
│   │       ├── controller/# 控制器
│   │       │   ├── AuthController.java    # 认证控制器
│   │       │   ├── UserController.java    # 用户控制器
│   │       │   └── SystemController.java  # 系统控制器
│   │       ├── model/     # 数据模型
│   │       │   ├── entity/    # 实体类
│   │       │   ├── dto/       # 数据传输对象
│   │       │   └── vo/        # 视图对象
│   │       ├── repository/# 数据访问层
│   │       │   ├── UserRepository.java    # 用户数据访问
│   │       │   └── RoleRepository.java    # 角色数据访问
│   │       ├── service/   # 业务逻辑层
│   │       │   ├── impl/      # 接口实现
│   │       │   └── interfaces/# 接口定义
│   │       └── util/      # 工具类
│   │           ├── JwtUtil.java       # JWT工具
│   │           └── SecurityUtil.java  # 安全工具
│   └── resources/         # 资源文件
│       ├── application.properties  # 应用配置
│       ├── application-dev.properties  # 开发环境配置
│       ├── application-prod.properties # 生产环境配置
│       └── static/        # 静态资源
└── test/                  # 测试代码
    ├── java/             # 测试源代码
    └── resources/        # 测试资源
```

## 开发指南

### 代码规范
- 遵循阿里巴巴 Java 开发手册
- 使用 ESLint 进行前端代码规范检查
- 使用 Prettier 进行代码格式化
- 遵循 Vue 3 组合式 API 风格指南
- 使用 TypeScript 严格模式

### 提交规范
- feat: 新功能
- fix: 修复问题
- docs: 文档修改
- style: 代码格式修改
- refactor: 代码重构
- test: 测试用例修改
- chore: 其他修改

## 部署

### Docker 部署
1. 构建镜像
```bash
docker build -t fivebear-system .
```

2. 运行容器
```bash
docker run -d -p 8080:8080 fivebear-system
```

### 环境配置
- 开发环境：`.env.development`
- 生产环境：`.env.production`
- 测试环境：`.env.test`

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交代码
4. 创建 Pull Request

## 许可证

[MIT License](LICENSE)

## 联系方式

如有问题或建议，请提交 Issue 或联系项目维护者。 