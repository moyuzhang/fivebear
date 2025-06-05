<template>
  <div class="app-layout">
    <!-- 侧边栏导航 -->
    <SidebarNavigation />
    
    <!-- 主内容区域 -->
    <div class="main-container" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <!-- 顶部导航栏 -->
      <header class="app-header glass-effect">
        <div class="header-left">
          <el-button 
            class="sidebar-toggle" 
            :icon="sidebarCollapsed ? Expand : Fold" 
            @click="toggleSidebar"
            circle
          />
          <div class="brand">
            <span class="brand-icon">🐻</span>
            <h1 class="brand-title">FiveBear 企业管理系统</h1>
          </div>
        </div>
        
        <div class="header-right">
          <div class="header-actions">
            <!-- 搜索框 -->
            <el-input
              v-model="searchQuery"
              class="header-search"
              placeholder="搜索功能..."
              prefix-icon="Search"
              clearable
            />
            
            <!-- 通知图标 -->
            <el-badge :value="3" class="notification-badge">
              <el-button icon="Bell" circle />
            </el-badge>
            
            <!-- 用户菜单 -->
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-menu">
                <el-avatar :size="36" class="user-avatar gradient-primary">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="user-info">
                  <div class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</div>
                  <div class="user-role">{{ userStore.userInfo?.roleName || '访客' }}</div>
                </div>
                <el-icon class="dropdown-arrow"><arrow-down /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    <span>账号设置</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>
      
      <!-- 面包屑导航 -->
      <div class="breadcrumb-wrapper">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentRouteName">
            {{ currentRouteTitle }}
          </el-breadcrumb-item>
        </el-breadcrumb>
        <div class="page-actions">
          <slot name="page-actions"></slot>
        </div>
      </div>
      
      <!-- 页面内容 -->
      <main class="page-container">
        <transition name="slide-fade" mode="out-in">
          <div class="page-content">
            <slot />
          </div>
        </transition>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowDown, User, Setting, SwitchButton, Expand, Fold, 
  Search, Bell, HomeFilled 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import SidebarNavigation from './SidebarNavigation.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 搜索查询
const searchQuery = ref('')

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

<style scoped lang="scss">
@import '@/styles/variables.scss';

.app-layout {
  display: flex;
  height: 100vh;
  background-color: $bg-color;
}

.main-container {
  flex: 1;
  margin-left: $sidebar-width;
  transition: margin-left $duration-base ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &.sidebar-collapsed {
    margin-left: $sidebar-collapsed-width;
  }
}

// 头部样式
.app-header {
  height: $header-height;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid $border-color;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 $spacing-lg;
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
  box-shadow: $shadow-sm;
}

.header-left {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.sidebar-toggle {
  transition: all $duration-fast;
  
  &:hover {
    transform: rotate(180deg);
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.brand-icon {
  font-size: 28px;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.1));
}

.brand-title {
  margin: 0;
  font-size: $font-size-lg;
  font-weight: 600;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.header-search {
  width: 240px;
  
  :deep(.el-input__wrapper) {
    border-radius: $radius-round;
    background-color: $bg-color;
  }
}

.notification-badge {
  :deep(.el-badge__content) {
    border-radius: $radius-round;
  }
}

.user-menu {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-xs $spacing-sm;
  border-radius: $radius-round;
  cursor: pointer;
  transition: all $duration-fast;

  &:hover {
    background-color: $bg-color;
  }
}

.user-avatar {
  box-shadow: $shadow-md;
  font-weight: bold;
  color: white;
}

.user-info {
  text-align: left;
}

.user-name {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.3;
}

.user-role {
  font-size: $font-size-xs;
  color: $text-secondary;
  line-height: 1.3;
}

.dropdown-arrow {
  color: $text-secondary;
  margin-left: $spacing-xs;
}

// 面包屑样式
.breadcrumb-wrapper {
  padding: $spacing-md $spacing-lg;
  background: white;
  border-bottom: 1px solid $divider-color;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-breadcrumb) {
  font-size: $font-size-sm;
  
  .el-breadcrumb__item {
    display: flex;
    align-items: center;
    
    .el-icon {
      margin-right: $spacing-xs;
    }
  }
}

// 页面内容样式
.page-container {
  flex: 1;
  overflow: hidden;
  padding: $spacing-lg;
}

.page-content {
  height: 100%;
  overflow-y: auto;
  
  // 为内容添加渐入动画
  animation: fadeInUp 0.5s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .header-search {
    display: none;
  }
  
  .brand-title {
    display: none;
  }
  
  .main-container {
    margin-left: 0;
  }
}
</style> 