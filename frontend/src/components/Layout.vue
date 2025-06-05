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
          <!-- 用户信息展示 -->
          <div class="user-display">
            <div class="user-welcome">
              <span class="welcome-text">欢迎回来</span>
              <span class="user-name">{{ userStore.userInfo?.username || '用户' }}</span>
            </div>
          </div>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ userStore.userInfo?.username?.charAt(0).toUpperCase() || 'U' }}
              </el-avatar>
              <el-icon class="dropdown-icon">
                <arrow-down />
              </el-icon>
            </div>
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
  background: white;
  box-shadow: var(--shadow-sm);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--space-6);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--gray-200);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.sidebar-toggle-btn {
  color: var(--gray-500);
  font-size: 18px;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.sidebar-toggle-btn:hover {
  color: var(--primary-600);
  background-color: var(--gray-100);
}

.header-left h2 {
  margin: 0;
  color: var(--primary-600);
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

/* 用户信息展示区域 */
.user-display {
  text-align: right;
  padding-right: var(--space-3);
  border-right: 1px solid var(--gray-200);
}

.user-welcome {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-1);
}

.welcome-text {
  font-size: var(--text-xs);
  color: var(--gray-500);
  font-weight: var(--font-weight-medium);
}

.user-name {
  font-size: var(--text-lg);
  color: var(--gray-900);
  font-weight: var(--font-weight-bold);
}



/* 用户头像下拉菜单 */
.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.user-info:hover {
  background-color: var(--gray-100);
}

.user-avatar {
  background: var(--primary-600);
  color: white;
  font-weight: var(--font-weight-bold);
  box-shadow: var(--shadow-sm);
}

.dropdown-icon {
  color: var(--gray-500);
  font-size: 14px;
  transition: transform var(--transition-base);
}

.user-info:hover .dropdown-icon {
  color: var(--primary-600);
  transform: rotate(180deg);
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