<template>
  <div class="layout">
    <!-- 侧边栏导航 -->
    <SidebarNavigation />
    
    <!-- 主内容区域 -->
    <div class="main-content" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <!-- 顶部导航栏 -->
      <div class="top-header">
        <div class="header-left">
          <el-button 
            class="sidebar-toggle-btn" 
            :icon="sidebarCollapsed ? Expand : Fold" 
            @click="toggleSidebar"
            text
            size="large"
          />
          <h2>🐻 FiveBear企业管理系统</h2>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="35" class="user-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <el-icon class="el-icon--right">
                <arrow-down />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 面包屑导航 -->
      <div class="breadcrumb-container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentRouteName">
            {{ currentRouteTitle }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 页面内容 -->
      <div class="page-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, User, Setting, SwitchButton, Expand, Fold } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import SidebarNavigation from './SidebarNavigation.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态
const sidebarCollapsed = ref(localStorage.getItem('sidebar-collapsed') === 'true')

// 当前路由信息
const currentRouteName = computed(() => route.name)
const currentRouteTitle = computed(() => route.meta?.title || '')

// 监听侧边栏折叠事件
const handleSidebarToggle = (event: CustomEvent) => {
  sidebarCollapsed.value = event.detail.collapsed
}

onMounted(() => {
  window.addEventListener('sidebar-toggle', handleSidebarToggle as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('sidebar-toggle', handleSidebarToggle as EventListener)
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('sidebar-collapsed', sidebarCollapsed.value.toString())
  
  // 触发自定义事件，通知侧边栏组件
  window.dispatchEvent(new CustomEvent('sidebar-toggle', { 
    detail: { collapsed: sidebarCollapsed.value } 
  }))
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
      
    case 'settings':
      router.push('/profile/settings')
      break
      
    case 'logout':
      await handleLogout()
      break
  }
}

// 处理登出
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确认要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    console.log('🚪 用户确认退出登录')
    
    const loadingMessage = ElMessage({
      message: '退出中...',
      type: 'info',
      duration: 0
    })
    
    try {
      await userStore.logout()
      loadingMessage.close()
      ElMessage.success('已成功退出登录')
      console.log('✅ 退出登录成功，跳转到登录页')
      await router.push('/login')
    } catch (error) {
      loadingMessage.close()
      console.error('❌ 退出登录失败:', error)
      ElMessage.error('退出登录失败，请重试')
    }
  } catch {
    console.log('❌ 用户取消退出登录')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
}

.main-content {
  flex: 1;
  margin-left: 250px;
  transition: margin-left 0.3s ease;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.main-content.sidebar-collapsed {
  margin-left: 64px;
}

.top-header {
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-toggle-btn {
  color: #606266;
  font-size: 18px;
}

.sidebar-toggle-btn:hover {
  color: #409eff;
  background-color: #f0f2f5;
}

.header-left h2 {
  margin: 0;
  color: #409eff;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.user-avatar {
  margin-right: 8px;
  background-color: #409eff;
  color: white;
  font-weight: bold;
}

.username {
  margin-right: 8px;
  font-size: 14px;
  color: #606266;
}

.breadcrumb-container {
  padding: 12px 20px;
  background: #f5f5f5;
  border-bottom: 1px solid #e4e7ed;
}

.page-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style> 