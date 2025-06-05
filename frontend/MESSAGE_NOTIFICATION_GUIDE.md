# 消息通知系统使用指南

## 📋 概述

FiveBear企业管理系统集成了完整的消息通知系统，支持实时WebSocket消息推送、多种消息类型展示和权限控制。

## 🎯 功能特性

### ✨ **消息类型支持**
- **系统消息**: 系统信息、警告、错误
- **管理员专用**: 在线用户统计、系统状态、用户活动  
- **业务消息**: 业务信息、警告、错误
- **连接消息**: 连接成功、强制登出通知

### 🎨 **UI组件功能**
- **实时弹窗通知**: 右上角滑入式通知
- **消息中心**: 右下角消息指示器和抽屉
- **消息历史**: 分类查看和管理
- **权限控制**: 基于用户角色的消息过滤

### 🔐 **权限管理**
- **管理员用户**: 接收所有消息类型
- **普通用户**: 只接收业务相关消息
- **实时验证**: 基于数据库角色信息

## 🚀 快速开始

### 1. **组件集成**

消息通知组件已集成到主布局中：

```vue
<!-- Layout.vue -->
<template>
  <div class="layout">
    <!-- 其他布局内容 -->
    
    <!-- 消息通知组件 -->
    <MessageNotification ref="messageNotificationRef" />
  </div>
</template>
```

### 2. **WebSocket自动连接**

系统会在用户登录后自动建立WebSocket连接：

```typescript
// 在 user store 中自动调用
const setupWebSocketConnection = () => {
  if (!webSocketConnected) {
    const webSocket = initGlobalWebSocket(token.value)
    webSocket.onForceLogout(handleForceLogout)
    webSocketConnected = true
  }
}
```

### 3. **消息监听**

消息通知组件会自动监听所有WebSocket消息：

```typescript
// MessageNotification.vue
const handleWebSocketMessage = (message: any) => {
  const notification = createNotification(message)
  allMessages.value.unshift(notification)
  
  if (shouldShowPopupNotification(message)) {
    showPopupNotification(notification)
  }
}
```

## 🛠 使用方法

### **查看实时消息**

1. **弹窗通知**: 重要消息会在右上角显示弹窗
2. **消息指示器**: 点击右下角铃铛图标查看消息中心
3. **消息分类**: 在消息中心可按类型筛选消息

### **测试消息功能**

访问 `/test/messages` 页面（管理员专用）进行消息测试：

1. **预设消息测试**: 点击按钮发送各种类型的测试消息
2. **自定义消息**: 自定义消息类型、内容和数据
3. **连接状态监控**: 查看WebSocket连接状态和消息历史

### **消息中心操作**

- **分类查看**: 系统消息 / 用户活动 / 所有消息
- **标记已读**: 点击消息标记为已读
- **批量操作**: 全部已读 / 清空消息
- **详细信息**: 查看消息的完整数据

## 🎛 消息类型详解

### **系统消息 (SYSTEM_)**

```json
{
  "type": "SYSTEM_INFO",
  "message": "系统正常运行",
  "level": "info",
  "data": { "server": "prod-01", "uptime": "99.9%" }
}
```

- `SYSTEM_INFO`: 系统信息通知
- `SYSTEM_WARNING`: 系统警告（资源不足等）
- `SYSTEM_ERROR`: 系统错误（需要立即处理）

### **管理员专用消息 (ADMIN_)**

```json
{
  "type": "ADMIN_ONLINE_USER_COUNT",
  "message": "在线用户数量更新",
  "level": "info",
  "data": {
    "onlineUserCount": 15,
    "totalConnections": 23
  }
}
```

- `ADMIN_ONLINE_USER_COUNT`: 在线用户统计更新
- `ADMIN_SYSTEM_STATUS`: 系统状态信息
- `ADMIN_USER_ACTION`: 用户行为通知（登录、登出等）

### **业务消息 (BUSINESS_)**

```json
{
  "type": "BUSINESS_WARNING",
  "message": "订单处理异常",
  "level": "warning",
  "data": { "orderId": "12345", "error": "库存不足" }
}
```

- `BUSINESS_INFO`: 业务信息
- `BUSINESS_WARNING`: 业务警告
- `BUSINESS_ERROR`: 业务错误

### **连接消息**

```json
{
  "type": "FORCE_LOGOUT",
  "message": "检测到其他设备登录，您已被强制下线",
  "level": "error",
  "data": { "reason": "多地登录" }
}
```

- `CONNECTION_SUCCESS`: WebSocket连接成功
- `FORCE_LOGOUT`: 强制登出通知

## ⚙️ 配置选项

### **通知行为配置**

```typescript
// 自动关闭时间设置
const getNotificationDuration = (type: string): number => {
  const durationMap = {
    'FORCE_LOGOUT': 0,        // 不自动关闭
    'SYSTEM_ERROR': 0,        // 不自动关闭
    'SYSTEM_WARNING': 8000,   // 8秒
    'BUSINESS_ERROR': 6000,   // 6秒
    'ADMIN_USER_ACTION': 3000 // 3秒
  }
  return durationMap[type] || 4000
}
```

### **显示优先级配置**

```typescript
// 重要消息会显示弹窗
const shouldShowPopupNotification = (message: any): boolean => {
  const importantTypes = [
    'FORCE_LOGOUT',
    'SYSTEM_ERROR', 
    'SYSTEM_WARNING',
    'BUSINESS_ERROR',
    'BUSINESS_WARNING'
  ]
  return importantTypes.includes(message.type)
}
```

## 🎨 UI自定义

### **样式主题**

消息通知支持以下视觉样式：

- **Info**: 蓝色主题，信息类消息
- **Success**: 绿色主题，成功类消息  
- **Warning**: 橙色主题，警告类消息
- **Error**: 红色主题，错误类消息

### **响应式设计**

- **桌面端**: 右上角弹窗 + 右下角消息中心
- **移动端**: 全屏弹窗 + 底部消息中心
- **自适应**: 根据屏幕尺寸调整布局

## 🔧 开发集成

### **发送自定义消息**

```typescript
// 在任何组件中
import { getGlobalWebSocket } from '@/utils/unifiedWebSocket'

const webSocket = getGlobalWebSocket()
// 注意：通常消息由后端WebSocket推送，前端只接收
```

### **监听特定消息**

```typescript
// 监听强制登出消息
webSocket.onMessage('FORCE_LOGOUT', (message) => {
  console.log('收到强制登出:', message)
  // 处理登出逻辑
})

// 监听所有消息
webSocket.onMessage('*', (message) => {
  console.log('收到消息:', message)
})
```

### **移除消息监听**

```typescript
// 组件卸载时清理监听器
onUnmounted(() => {
  webSocket.offMessage('FORCE_LOGOUT', handler)
})
```

## 🚨 最佳实践

### **消息设计原则**

1. **简洁明了**: 消息内容应简洁易懂
2. **及时性**: 重要消息立即推送
3. **分类清晰**: 使用正确的消息类型
4. **数据完整**: 提供必要的上下文数据

### **性能优化**

1. **消息限制**: 历史消息限制在100条以内
2. **批量操作**: 支持批量标记已读和清空
3. **内存管理**: 自动清理过期连接和数据

### **用户体验**

1. **渐进提示**: 重要消息弹窗，普通消息仅指示器
2. **操作便捷**: 一键标记已读、清空历史
3. **视觉反馈**: 清晰的已读/未读状态

## 🐛 故障排除

### **常见问题**

1. **消息不显示**: 检查WebSocket连接状态
2. **权限错误**: 确认用户角色配置正确  
3. **连接断开**: 检查网络和服务器状态

### **调试信息**

在浏览器控制台查看调试日志：

```
📡 MessageNotification: WebSocket监听器已设置
📩 MessageNotification: 收到消息: {...}
🔔 MessageNotification: 触发强制登出处理器
```

## 📞 技术支持

如有问题或建议，请：

1. 查看浏览器控制台错误信息
2. 检查WebSocket连接状态  
3. 使用测试页面验证功能
4. 联系技术支持团队

---

**FiveBear企业管理系统** - 消息通知系统 v1.0 