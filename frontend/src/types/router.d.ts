import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    hideInMenu?: boolean
    requiresAuth?: boolean  // 是否需要认证
    requiresAdmin?: boolean // 是否需要管理员权限
    guest?: boolean         // 是否为访客页面（已登录用户不能访问）
    roles?: number[]        // 允许访问的角色ID列表
  }
} 