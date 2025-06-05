# FiveBear 热重载开发环境配置

## 🚀 功能特性

### 后端热重载
- ✅ Spring Boot DevTools 自动重启
- ✅ 代码变更自动检测
- ✅ 配置文件热更新
- ✅ LiveReload 支持

### 前端热重载
- ✅ Vite HMR (Hot Module Replacement)
- ✅ 组件级热更新
- ✅ 样式实时更新
- ✅ 自动代理后端API

## 🛠️ 启动方式

### 方式一：一键启动脚本

**Windows:**
```bash
# 运行启动脚本
./start-dev.bat
```

**Linux/Mac:**
```bash
# 添加执行权限
chmod +x start-dev.sh

# 运行启动脚本
./start-dev.sh
```

### 方式二：手动启动

1. **启动Redis服务**
   ```bash
   # Windows
   redis-server --service-start
   
   # macOS
   brew services start redis
   
   # Linux
   sudo systemctl start redis
   ```

2. **启动后端服务**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **启动前端服务**
   ```bash
   cd frontend
   npm run dev
   ```

## 📱 访问地址

- **前端应用**: http://localhost:3000
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html
- **数据库监控**: http://localhost:8080/druid (admin/admin)

## 🔧 开发特性

### 后端热重载特性
- 修改Java代码后自动重启应用
- 配置文件变更实时生效
- 静态资源热更新
- 开发工具端点暴露

### 前端热重载特性
- Vue组件修改实时更新
- CSS样式实时预览
- 错误覆盖层显示
- 自动打开浏览器

## 🔍 调试功能

### 后端调试
- 详细的日志输出
- SQL语句打印
- 安全调试信息
- Actuator端点监控

### 前端调试
- 代理请求日志
- 错误提示覆盖层
- 热更新状态提示
- TypeScript类型检查

## ⚙️ 配置说明

### 后端配置
- `application-dev.yml`: 开发环境专用配置
- `spring.devtools.*`: 热重载相关配置
- `management.endpoints.*`: 监控端点配置

### 前端配置
- `vite.config.ts`: Vite开发服务器配置
- `package.json`: 开发脚本配置
- 代理配置自动转发API请求到后端

## 🚨 注意事项

1. **端口占用**: 确保3000和8080端口未被占用
2. **Redis依赖**: 登录安全功能需要Redis服务
3. **Java版本**: 需要Java 21或更高版本
4. **Node版本**: 建议使用Node 18或更高版本

## 🐛 常见问题

### 后端热重载不生效
1. 检查DevTools依赖是否正确添加
2. 确认IDE支持自动编译
3. 检查`spring.devtools.restart.enabled=true`

### 前端热更新失败
1. 清除node_modules重新安装
2. 检查Vite配置文件语法
3. 确认浏览器支持WebSocket

### 代理请求失败
1. 确认后端服务已启动
2. 检查代理配置路径
3. 查看控制台网络请求

## 📋 开发工作流

1. 运行启动脚本或手动启动服务
2. 浏览器自动打开前端应用
3. 修改代码，观察实时更新
4. 使用浏览器开发者工具调试
5. 查看后端日志和API文档

## 🎯 性能优化

- 后端启用条件重启减少重启次数
- 前端HMR只更新修改的模块
- 开发环境禁用不必要的缓存
- 代理请求包含详细日志便于调试 