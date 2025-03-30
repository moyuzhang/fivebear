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

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { 
        title: '登录',
        requiresAuth: false 
      }
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('@/views/home/index.vue'),
          meta: { title: '首页', requiresAuth: true }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/404.vue'),
      meta: { 
        title: '404',
        requiresAuth: false 
      }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 开始加载进度条
  NProgress.start()

  // 设置页面标题
  const title = to.meta.title
  if (title) {
    document.title = `${title} - FiveBear`
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