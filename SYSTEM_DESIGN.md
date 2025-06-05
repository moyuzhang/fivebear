# FiveBear 企业管理系统 - 系统设计文档

## 📋 系统概述

FiveBear企业管理系统是一个基于Vue3 + Spring Boot的企业级管理系统，主要用于货物管理、站外站点管理、用户权限管理等业务功能。

## 🔐 角色与权限设计

### 系统用户角色

#### 1. 管理员 (ADMIN)
- **职责**: 管理整个系统
- **权限**: 
  - 系统用户管理
  - 所有业务功能的完整访问权限
  - 系统设置和配置
  - 功能权限分配（如超水检测权限）

#### 2. 总监 (SUPERVISOR) 
- **职责**: 业务层最高权限用户
- **权限**:
  - 出货/入货功能
  - 可添加所有类型的站外站点（管理、总监、会员）
  - 报表功能
  - 业务设置
  - 超水检测功能（需管理员授权）

#### 3. 大股东 (MAJOR_SHAREHOLDER)
- **职责**: 业务层受限权限用户
- **权限**:
  - 出货/入货功能
  - 可添加部分类型的站外站点（管理、会员，不能添加总监类型）
  - 报表功能
  - 业务设置
  - 超水检测功能（需管理员授权）

### 站外站点业务账户类型

基于数据库 `client_role` 表的设计：

| 业务账户类型 | ID | 代码 | 系统总监可添加 | 系统大股东可添加 | 说明 |
|-------------|----|----|--------------|----------------|-----|
| 会员 | 1 | MEMBER | ✅ | ✅ | 业务层普通会员 |
| 管理 | 2 | ADMIN | ✅ | ✅ | 业务层管理账户 |
| 总监 | 3 | DIRECTOR | ✅ | ❌ | 业务层总监账户 |

## 🎨 菜单结构设计

### 管理员菜单
```
📊 仪表板
├── 系统概览
└── 快速操作

📦 货物管理
├── 出货管理
├── 入货管理
└── 库存概览

🌐 站外站点管理
├── 站点列表（所有）
├── 添加站点
├── 站点统计
└── 站点配置

👥 系统用户管理
├── 用户列表
├── 角色权限管理
└── 功能权限配置
    └── 超水检测权限

📈 报表分析
├── 出货报表
├── 入货报表
├── 站点报表
└── 系统报表

⚙️ 系统设置
├── 基础配置
├── 安全设置
└── 日志管理

👤 个人中心
├── 个人信息
└── 密码修改
```

### 系统总监菜单
```
📊 仪表板
├── 业务概览
└── 我的数据

📦 货物管理
├── 出货管理
├── 入货管理
└── 我的操作

🌐 站外站点管理
├── 站点列表
├── 添加站点
    ├── 添加管理账户站点
    ├── 添加总监账户站点
    └── 添加会员账户站点
├── 我的站点
└── 站点统计

🔍 超水检测          // 需管理员授权
├── 检测记录
└── 检测设置

📈 报表分析
├── 出货报表
├── 入货报表
├── 站点报表
└── 我的报表

⚙️ 业务设置
└── 个人设置

👤 个人中心
├── 个人信息
└── 密码修改
```

### 系统大股东菜单
```
📊 仪表板
├── 业务概览
└── 我的数据

📦 货物管理
├── 出货管理
├── 入货管理
└── 我的操作

🌐 站外站点管理
├── 站点列表
├── 添加站点
    ├── 添加管理账户站点
    └── 添加会员账户站点
├── 我的站点
└── 站点统计

🔍 超水检测          // 需管理员授权
├── 检测记录
└── 检测设置

📈 报表分析
├── 出货报表
├── 入货报表
├── 站点报表
└── 我的报表

⚙️ 业务设置
└── 个人设置

👤 个人中心
├── 个人信息
└── 密码修改
```

## 🔒 权限系统设计

### 权限常量定义

```typescript
// 站外站点业务账户类型（对应client_role表）
export enum ExternalSiteClientRole {
  MEMBER = 1,    // 会员
  ADMIN = 2,     // 管理  
  DIRECTOR = 3   // 总监
}

export const PERMISSIONS = {
  // 系统层权限
  SYSTEM_MANAGE: 'system:manage',
  SYSTEM_USER_MANAGE: 'system:user:manage',
  FEATURE_PERMISSION_MANAGE: 'feature:permission:manage',
  
  // 站外站点管理权限
  EXTERNAL_SITE_MANAGE_ALL: 'external_site:manage:all',     // 管理员：所有站点
  EXTERNAL_SITE_MANAGE_FULL: 'external_site:manage:full',   // 系统总监：所有类型
  EXTERNAL_SITE_MANAGE_LIMITED: 'external_site:manage:limited', // 系统大股东：限制类型
  
  // 站外站点添加权限（对应client_role）
  EXTERNAL_SITE_ADD_ADMIN: 'external_site:add:admin',       // 添加管理类型站点
  EXTERNAL_SITE_ADD_DIRECTOR: 'external_site:add:director', // 添加总监类型站点
  EXTERNAL_SITE_ADD_MEMBER: 'external_site:add:member',     // 添加会员类型站点
  
  // 业务操作权限
  CARGO_OUT: 'cargo:out',
  CARGO_IN: 'cargo:in',
  
  // 特殊功能权限
  WATER_DETECTION: 'water:detection',
  
  // 报表权限
  REPORT_ALL: 'report:all',
  REPORT_SELF: 'report:self',
  
  // 设置权限
  SETTING_SYSTEM: 'setting:system',
  SETTING_BUSINESS: 'setting:business',
}
```

### 角色基础权限映射

```typescript
export const ROLE_BASE_PERMISSIONS = {
  ADMIN: [
    PERMISSIONS.SYSTEM_MANAGE,
    PERMISSIONS.SYSTEM_USER_MANAGE,
    PERMISSIONS.FEATURE_PERMISSION_MANAGE,
    PERMISSIONS.EXTERNAL_SITE_MANAGE_ALL,
    PERMISSIONS.EXTERNAL_SITE_ADD_ADMIN,
    PERMISSIONS.EXTERNAL_SITE_ADD_DIRECTOR,
    PERMISSIONS.EXTERNAL_SITE_ADD_MEMBER,
    PERMISSIONS.CARGO_OUT,
    PERMISSIONS.CARGO_IN,
    PERMISSIONS.WATER_DETECTION,
    PERMISSIONS.REPORT_ALL,
    PERMISSIONS.SETTING_SYSTEM,
  ],
  SUPERVISOR: [  // 系统总监 - 最高业务权限
    PERMISSIONS.EXTERNAL_SITE_MANAGE_FULL,
    PERMISSIONS.EXTERNAL_SITE_ADD_ADMIN,    // ✅ 可以添加
    PERMISSIONS.EXTERNAL_SITE_ADD_DIRECTOR, // ✅ 可以添加
    PERMISSIONS.EXTERNAL_SITE_ADD_MEMBER,   // ✅ 可以添加
    PERMISSIONS.CARGO_OUT,
    PERMISSIONS.CARGO_IN,
    PERMISSIONS.REPORT_SELF,
    PERMISSIONS.SETTING_BUSINESS,
    // PERMISSIONS.WATER_DETECTION, // 需管理员单独授权
  ],
  MAJOR_SHAREHOLDER: [  // 系统大股东 - 受限权限
    PERMISSIONS.EXTERNAL_SITE_MANAGE_LIMITED,
    PERMISSIONS.EXTERNAL_SITE_ADD_ADMIN,    // ✅ 可以添加
    // PERMISSIONS.EXTERNAL_SITE_ADD_DIRECTOR, // ❌ 不能添加
    PERMISSIONS.EXTERNAL_SITE_ADD_MEMBER,   // ✅ 可以添加
    PERMISSIONS.CARGO_OUT,
    PERMISSIONS.CARGO_IN,
    PERMISSIONS.REPORT_SELF,
    PERMISSIONS.SETTING_BUSINESS,
    // PERMISSIONS.WATER_DETECTION, // 需管理员单独授权
  ],
}
```

### 权限扩展机制

```typescript
// 用户扩展权限表
interface UserExtendedPermissions {
  userId: number
  basePermissions: string[]        // 角色基础权限
  grantedPermissions: string[]     // 管理员授权的额外权限
  revokedPermissions: string[]     // 被撤销的权限
}

// 最终权限计算
function calculateUserPermissions(user: User): string[] {
  const basePermissions = ROLE_BASE_PERMISSIONS[user.role] || []
  const extended = getUserExtendedPermissions(user.id)
  
  return [
    ...basePermissions,
    ...extended.grantedPermissions
  ].filter(p => !extended.revokedPermissions.includes(p))
}
```

## 🗃️ 数据库设计

### 核心表结构

#### 1. 系统用户表 (users)
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  username VARCHAR(50),
  password VARCHAR(255),
  email VARCHAR(100),
  role ENUM('ADMIN', 'SUPERVISOR', 'MAJOR_SHAREHOLDER'),
  status TINYINT DEFAULT 1,
  -- 其他字段...
);
```

#### 2. 站外站点表 (external_sites)
```sql
CREATE TABLE external_sites (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  url VARCHAR(500) NOT NULL UNIQUE,
  username VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  rebate_rate DOUBLE NOT NULL,
  weight INT NOT NULL,
  site_status INT NOT NULL,
  status VARCHAR(10) NOT NULL,
  site_lines TEXT,
  lottery_type INT NOT NULL,
  client_type INT NOT NULL,
  client_role INT NOT NULL,  -- 对应业务账户类型：1-会员，2-管理，3-总监
  create_by VARCHAR(100),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(100),
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  multiplier DOUBLE NOT NULL,
  domain_account VARCHAR(200) UNIQUE,
  remark TEXT
);
```

#### 3. 客户角色表 (client_role)
```sql
CREATE TABLE client_role (
  id INT PRIMARY KEY,
  code VARCHAR(50),           -- MEMBER, ADMIN, DIRECTOR
  name VARCHAR(50)            -- 会员, 管理, 总监
);
```

#### 4. 用户扩展权限表 (user_extended_permissions)
```sql
CREATE TABLE user_extended_permissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  permission_code VARCHAR(100) NOT NULL,
  permission_type ENUM('GRANTED', 'REVOKED') NOT NULL,
  granted_by BIGINT NOT NULL,
  granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark TEXT,
  INDEX idx_user_permission (user_id, permission_code)
);
```

## 💻 前端实现

### 权限控制示例

```typescript
// 站点添加权限检查
const canAddSiteType = (clientRole: ExternalSiteClientRole, userPermissions: string[]) => {
  const permissionMap = {
    [ExternalSiteClientRole.ADMIN]: PERMISSIONS.EXTERNAL_SITE_ADD_ADMIN,
    [ExternalSiteClientRole.DIRECTOR]: PERMISSIONS.EXTERNAL_SITE_ADD_DIRECTOR,
    [ExternalSiteClientRole.MEMBER]: PERMISSIONS.EXTERNAL_SITE_ADD_MEMBER,
  }
  
  return userPermissions.includes(permissionMap[clientRole])
}

// 站点列表权限过滤
const getAccessibleSites = (allSites: ExternalSite[], userRole: string, userId: number) => {
  if (userRole === 'ADMIN') {
    return allSites // 管理员看所有
  }
  
  if (userRole === 'SUPERVISOR') {
    return allSites // 系统总监看所有
  }
  
  if (userRole === 'MAJOR_SHAREHOLDER') {
    // 系统大股东只能看管理类型和会员类型的站点
    return allSites.filter(site => 
      site.client_role === ExternalSiteClientRole.ADMIN || 
      site.client_role === ExternalSiteClientRole.MEMBER
    )
  }
  
  return []
}
```

### 路由权限守卫

```typescript
// 路由配置示例
const routes = [
  {
    path: '/dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/system',
    component: SystemManage,
    meta: { 
      requiresAuth: true,
      permission: PERMISSIONS.SYSTEM_MANAGE
    }
  },
  {
    path: '/external-sites',
    component: ExternalSiteManage,
    meta: { 
      requiresAuth: true,
      permission: [
        PERMISSIONS.EXTERNAL_SITE_MANAGE_ALL, 
        PERMISSIONS.EXTERNAL_SITE_MANAGE_FULL,
        PERMISSIONS.EXTERNAL_SITE_MANAGE_LIMITED
      ]
    }
  }
]
```

## 🎯 开发优先级

### 第一阶段：基础框架
- [ ] 主布局组件开发
- [ ] 权限系统实现
- [ ] 路由配置和守卫
- [ ] 用户状态管理

### 第二阶段：核心功能
- [ ] 仪表板页面
- [ ] 站外站点管理功能
- [ ] 出货/入货管理
- [ ] 基础报表功能

### 第三阶段：高级功能
- [ ] 超水检测功能
- [ ] 详细报表分析
- [ ] 系统设置功能
- [ ] 移动端适配

## 📱 响应式设计

### 桌面端 (>1200px)
- 左侧固定侧边栏
- 主内容区自适应宽度
- 顶部固定导航栏

### 平板端 (768px - 1200px)
- 可折叠侧边栏
- 主内容区全宽显示
- 紧凑的卡片布局

### 移动端 (<768px)
- 底部导航栏
- 抽屉式菜单
- 堆叠式卡片布局

## 🔒 安全考虑

### 1. 认证安全
- JWT Token 验证
- WebSocket 连接验证
- 密码加密存储
- 会话管理

### 2. 权限安全
- 前端路由权限控制
- 后端接口权限验证
- 数据级权限过滤
- 操作日志记录

### 3. 数据安全
- 敏感数据加密
- API 请求验证
- CORS 跨域控制
- SQL 注入防护

---

**创建时间**: 2025-06-05  
**版本**: v1.0  
**维护**: FiveBear 开发团队 