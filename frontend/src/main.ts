import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/user'

// 创建应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 应用初始化
const initApp = async () => {
  console.log('🚀 FiveBear 系统启动中...')
  
  // 初始化用户状态
  const userStore = useUserStore()
  
  try {
    await userStore.initUser()
    console.log('✅ 用户状态初始化完成')
  } catch (error) {
    console.error('❌ 用户状态初始化失败:', error)
  }

  console.log('✅ FiveBear 系统启动完成')
}

// 启动应用
initApp().catch(error => {
  console.error('🚨 应用启动失败:', error)
}).finally(() => {
  // 无论初始化是否成功，都要挂载应用
  app.mount('#app')
}) 