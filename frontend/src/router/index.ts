import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 路由配置
const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: {
      title: '仪表盘',
      icon: 'dashboard',
      requiresAuth: true  // 需要认证
    }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminList.vue'),
    meta: {
      title: '管理员管理',
      icon: 'user',
      requiresAuth: true,  // 需要认证
      requiresAdmin: true  // 需要管理员权限
    }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: {
      title: '用户管理',
      icon: 'user',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/roles',
    name: 'RoleManagement',
    component: () => import('@/views/admin/RoleManagement.vue'),
    meta: {
      title: '角色管理',
      icon: 'key',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/settings',
    name: 'SystemSettings',
    component: () => import('@/views/admin/SystemSettings.vue'),
    meta: {
      title: '系统设置',
      icon: 'setting',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/monitoring',
    name: 'SystemMonitoring',
    component: () => import('@/views/admin/SystemMonitoring.vue'),
    meta: {
      title: '系统监控',
      icon: 'monitor',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/logs',
    name: 'LogManagement',
    component: () => import('@/views/admin/LogManagement.vue'),
    meta: {
      title: '日志管理',
      icon: 'files',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/security',
    name: 'SecurityCenter',
    component: () => import('@/views/admin/SecurityCenter.vue'),
    meta: {
      title: '安全中心',
      icon: 'lock',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/report',
    name: 'Report',
    component: () => import('@/views/report/ReportDashboard.vue'),
    meta: {
      title: '报表管理',
      icon: 'chart',
      requiresAuth: true  // 需要认证
    }
  },
  {
    path: '/shipment',
    name: 'Shipment',
    component: () => import('@/views/shipment/ShipmentList.vue'),
    meta: {
      title: '出货管理',
      icon: 'box',
      requiresAuth: true  // 需要认证
    }
  },
  {
    path: '/sites',
    name: 'Sites',
    component: () => import('@/views/admin/Sites.vue'),
    meta: {
      title: '站点管理',
      icon: 'monitor',
      requiresAuth: true  // 需要认证
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      hideInMenu: true,
      guest: true  // 访客页面（已登录用户访问时跳转到首页）
    }
  },
  {
    path: '/init-admin',
    name: 'InitAdmin',
    component: () => import('@/views/InitAdmin.vue'),
    meta: {
      title: '初始化管理员',
      hideInMenu: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: {
      title: '页面不存在',
      hideInMenu: true
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 白名单路由 - 不需要认证的页面
const whiteList = ['/login', '/404', '/init-admin']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  console.log(`🛣️ 路由跳转: ${from.path} -> ${to.path}`)
  
  // 设置页面标题
  if (to.meta?.title && typeof to.meta.title === 'string') {
    document.title = `${to.meta.title} - FiveBear企业管理系统`
  }

  const userStore = useUserStore()
  
  // 如果是白名单路由，直接通过
  if (whiteList.includes(to.path)) {
    // 如果是访客页面且用户已登录，跳转到首页
    if (to.meta?.guest && userStore.isLoggedIn) {
      console.log('👤 已登录用户访问登录页，跳转到首页')
      next('/dashboard')
      return
    }
    next()
    return
  }

  // 检查是否需要认证
  if (to.meta?.requiresAuth) {
    // 如果没有登录状态，尝试初始化
    if (!userStore.isLoggedIn) {
      console.log('🔒 页面需要认证，初始化用户状态...')
      
      const initSuccess = await userStore.initUser()
      
      if (!initSuccess) {
        console.log('❌ 用户未登录，跳转到登录页')
        ElMessage.warning('请先登录')
        next({
          path: '/login',
          query: { redirect: to.fullPath } // 保存重定向地址
        })
        return
      }
    }

    // 检查用户状态是否有效
    if (!userStore.isUserActive()) {
      console.log('❌ 用户账户已被禁用')
      ElMessage.error('账户已被禁用，请联系管理员')
      await userStore.logout(true)
      next('/login')
      return
    }

    // 检查管理员权限
    if (to.meta?.requiresAdmin && !userStore.isAdmin()) {
      console.log('❌ 无管理员权限')
      ElMessage.error('无权限访问该页面')
      next('/dashboard') // 跳转到首页
      return
    }

    console.log('✅ 认证通过，允许访问')
  }

  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('🚨 路由错误:', error)
  ElMessage.error('页面加载失败')
})

export default router