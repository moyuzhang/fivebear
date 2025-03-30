import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

// 配置 NProgress
NProgress.configure({ 
  showSpinner: false,
  easing: 'ease',
  speed: 500
})

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { 
      title: '登录',
      requiresAuth: false 
    }
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/home/index.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '仪表盘', requiresAuth: true }
      },
      {
        path: 'mqtt',
        name: 'MQTT',
        component: () => import('../views/mqtt/index.vue'),
        meta: { title: 'MQTT管理', requiresAuth: true },
        children: [
          {
            path: '',
            redirect: '/mqtt/connections'
          },
          {
            path: 'connections',
            name: 'Connections',
            component: () => import('../views/mqtt/connections/index.vue'),
            meta: { title: '连接管理', requiresAuth: true }
          },
          {
            path: 'devices',
            name: 'Devices',
            component: () => import('../views/mqtt/devices/index.vue'),
            meta: { title: '设备管理', requiresAuth: true }
          },
          {
            path: 'messages',
            name: 'Messages',
            component: () => import('../views/mqtt/messages/index.vue'),
            meta: { title: '消息管理', requiresAuth: true }
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('../views/system/index.vue'),
        meta: { title: '系统管理', requiresAuth: true },
        children: [
          {
            path: '',
            redirect: '/system/users'
          },
          {
            path: 'users',
            name: 'Users',
            component: () => import('../views/system/users/index.vue'),
            meta: { title: '用户管理', requiresAuth: true }
          },
          {
            path: 'roles',
            name: 'Roles',
            component: () => import('../views/system/roles/index.vue'),
            meta: { title: '角色管理', requiresAuth: true }
          },
          {
            path: 'settings',
            name: 'Settings',
            component: () => import('../views/system/settings/index.vue'),
            meta: { title: '系统设置', requiresAuth: true }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 开始加载进度条
  NProgress.start()

  // 设置页面标题
  const title = to.meta.title
  if (title) {
    document.title = `${title} - MQTT管理系统`
  }

  const userStore = useUserStore()
  const isAuthenticated = userStore.isAuthenticated
  
  // 判断该路由是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      NProgress.done()
    } else {
      next()
    }
  } else {
    // 不需要登录权限的页面
    if (isAuthenticated && to.path === '/login') {
      // 已登录且要跳转登录页，重定向到首页
      next({ path: '/home' })
      NProgress.done()
    } else {
      next()
    }
  }
})

router.afterEach(() => {
  // 结束加载进度条
  NProgress.done()
})

export default router 