<template>
  <div class="sidebar-navigation" :class="{ collapsed: isCollapsed }">
    <!-- 侧边栏头部 -->
    <div class="sidebar-header">
      <div class="logo" v-if="!isCollapsed">
        <el-icon>🐻</el-icon>
        <span class="logo-text">FiveBear</span>
      </div>
      <el-button 
        class="collapse-btn" 
        :icon="isCollapsed ? Expand : Fold" 
        @click="toggleCollapse"
        text
      />
    </div>

    <!-- 导航菜单 -->
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      :collapse="isCollapsed"
      :unique-opened="true"
      router
    >
      <!-- 仪表盘 -->
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>仪表盘</template>
      </el-menu-item>

      <!-- 管理员功能 -->
      <el-sub-menu index="admin" v-if="userStore.userInfo?.roleName === '管理员'">
        <template #title>
          <el-icon><UserFilled /></el-icon>
          <span>管理员功能</span>
        </template>
        
        <el-menu-item index="/admin">
          <el-icon><Grid /></el-icon>
          <template #title>管理控制台</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/roles">
          <el-icon><Key /></el-icon>
          <template #title>角色管理</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/monitoring">
          <el-icon><Monitor /></el-icon>
          <template #title>系统监控</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/logs">
          <el-icon><Files /></el-icon>
          <template #title>日志管理</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/security">
          <el-icon><Lock /></el-icon>
          <template #title>安全中心</template>
        </el-menu-item>
      </el-sub-menu>

      <!-- 报表分析 -->
      <el-sub-menu index="report">
        <template #title>
          <el-icon><TrendCharts /></el-icon>
          <span>报表分析</span>
        </template>
        
        <el-menu-item index="/report">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>
        
        <el-menu-item index="/report/sales">
          <el-icon><TrendCharts /></el-icon>
          <template #title>销售报表</template>
        </el-menu-item>
        
        <el-menu-item index="/report/finance">
          <el-icon><Money /></el-icon>
          <template #title>财务报表</template>
        </el-menu-item>
      </el-sub-menu>

      <!-- 出货管理 -->
      <el-sub-menu index="shipment">
        <template #title>
          <el-icon><Box /></el-icon>
          <span>出货管理</span>
        </template>
        
        <el-menu-item index="/shipment">
          <el-icon><List /></el-icon>
          <template #title>出货列表</template>
        </el-menu-item>
        
        <el-menu-item index="/shipment/create">
          <el-icon><Plus /></el-icon>
          <template #title>创建出货单</template>
        </el-menu-item>
        
        <el-menu-item index="/shipment/tracking">
          <el-icon><Position /></el-icon>
          <template #title>物流跟踪</template>
        </el-menu-item>
      </el-sub-menu>

      <!-- 个人中心 -->
      <el-sub-menu index="profile">
        <template #title>
          <el-icon><Avatar /></el-icon>
          <span>个人中心</span>
        </template>
        
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title>个人信息</template>
        </el-menu-item>
        
        <el-menu-item index="/profile/settings">
          <el-icon><Tools /></el-icon>
          <template #title>个人设置</template>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>

    <!-- 侧边栏底部 -->
    <div class="sidebar-footer" v-if="!isCollapsed">
      <div class="user-info">
        <el-avatar :size="32" class="user-avatar">
          {{ userStore.userInfo?.username?.charAt(0).toUpperCase() || 'U' }}
        </el-avatar>
        <div class="user-details">
          <div class="username">{{ userStore.userInfo?.username }}</div>
          <div class="role">{{ userStore.userInfo?.roleName }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { 
  Expand, Fold, Odometer, UserFilled, Grid, User, Key, Setting, 
  Monitor, Files, Lock, TrendCharts, DataAnalysis, Money, Box, List, 
  Plus, Position, Avatar, Tools
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态 - 使用localStorage持久化
const isCollapsed = ref(localStorage.getItem('sidebar-collapsed') === 'true')

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('sidebar-collapsed', isCollapsed.value.toString())
  
  // 触发自定义事件，通知布局组件
  window.dispatchEvent(new CustomEvent('sidebar-toggle', { 
    detail: { collapsed: isCollapsed.value } 
  }))
}

// 监听折叠状态变化，更新CSS变量
watch(isCollapsed, (newVal) => {
  document.documentElement.style.setProperty('--sidebar-width', newVal ? '64px' : '250px')
}, { immediate: true })

// 监听来自Layout组件的折叠事件
const handleExternalToggle = (event: CustomEvent) => {
  isCollapsed.value = event.detail.collapsed
}

onMounted(() => {
  window.addEventListener('sidebar-toggle', handleExternalToggle as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('sidebar-toggle', handleExternalToggle as EventListener)
})

// 暴露折叠状态给父组件
defineExpose({ isCollapsed })
</script>

<style scoped>
.sidebar-navigation {
  width: 250px;
  height: 100vh;
  background: var(--gray-50);
  position: fixed;
  left: 0;
  top: 0;
  z-index: 999;
  transition: all var(--transition-slow);
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--gray-200);
  box-shadow: var(--shadow-lg);
}

.sidebar-navigation.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
  background: var(--primary-600);
  border-bottom: 1px solid var(--primary-700);
  position: relative;
}

.logo {
  display: flex;
  align-items: center;
  color: white;
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
}

.logo .el-icon {
  font-size: 24px;
  margin-right: var(--space-3);
  color: white;
}

.logo-text {
  color: white;
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
  letter-spacing: -0.5px;
}

.collapse-btn {
  color: white;
  border: none;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: var(--gray-50);
  overflow-y: auto;
  padding: var(--space-4) 0;
}

.sidebar-menu .el-menu-item {
  color: var(--gray-700) !important;
  height: 44px;
  line-height: 44px;
  font-size: var(--text-sm) !important;
  font-weight: var(--font-weight-medium) !important;
  margin: var(--space-1) var(--space-4);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
  background: white;
  border: 1px solid transparent;
}

.sidebar-menu .el-menu-item span {
  color: var(--gray-700) !important;
}

.sidebar-menu .el-menu-item .el-icon {
  color: var(--gray-500) !important;
  font-size: 16px !important;
  margin-right: var(--space-3);
}

.sidebar-menu .el-menu-item:hover {
  background: var(--primary-50) !important;
  color: var(--primary-700) !important;
  border-color: var(--primary-200);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.sidebar-menu .el-menu-item:hover span {
  color: var(--primary-700) !important;
}

.sidebar-menu .el-menu-item:hover .el-icon {
  color: var(--primary-600) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: var(--primary-600) !important;
  color: white !important;
  border-color: var(--primary-600);
  box-shadow: var(--shadow-md);
}

.sidebar-menu .el-menu-item.is-active span {
  color: white !important;
  font-weight: var(--font-weight-semibold) !important;
}

.sidebar-menu .el-menu-item.is-active .el-icon {
  color: white !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title {
  color: var(--gray-700) !important;
  height: 44px;
  line-height: 44px;
  font-size: var(--text-sm) !important;
  font-weight: var(--font-weight-medium) !important;
  margin: var(--space-1) var(--space-4);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
  background: white;
  border: 1px solid transparent;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title span {
  color: var(--gray-700) !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title .el-icon {
  color: var(--gray-500) !important;
  font-size: 16px !important;
  margin-right: var(--space-3);
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover {
  background: var(--primary-50) !important;
  color: var(--primary-700) !important;
  border-color: var(--primary-200);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover span {
  color: var(--primary-700) !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover .el-icon {
  color: var(--primary-600) !important;
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title {
  color: var(--primary-700) !important;
  background: var(--primary-100) !important;
  font-weight: var(--font-weight-semibold) !important;
  border-color: var(--primary-300);
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title span {
  color: var(--primary-700) !important;
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title .el-icon {
  color: var(--primary-600) !important;
}

.sidebar-menu .el-sub-menu .el-menu {
  background: var(--gray-100);
  margin: var(--space-2) var(--space-4);
  border-radius: var(--radius-lg);
  padding: var(--space-2) 0;
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item {
  background: var(--gray-50);
  margin: var(--space-1) var(--space-3);
  border-radius: var(--radius-md);
  color: var(--gray-600) !important;
  font-size: var(--text-xs) !important;
  height: 36px;
  line-height: 36px;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item:hover {
  background: var(--primary-50) !important;
  color: var(--primary-700) !important;
  border-color: var(--primary-200);
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item.is-active {
  background: var(--primary-600) !important;
  color: white !important;
  font-weight: var(--font-weight-semibold) !important;
  border-color: var(--primary-600);
}

.sidebar-footer {
  padding: var(--space-4) var(--space-6);
  background: white;
  border-top: 1px solid var(--gray-200);
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  background: var(--primary-600);
  color: white;
  font-weight: var(--font-weight-bold);
  margin-right: var(--space-3);
}

.user-details {
  flex: 1;
  color: var(--gray-700);
}

.username {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-1);
  color: var(--gray-800);
}

.role {
  font-size: var(--text-xs);
  color: var(--gray-500);
  font-weight: var(--font-weight-medium);
}

/* 调整主内容区域的左边距 */
.sidebar-navigation + * {
  margin-left: 250px;
  transition: margin-left 0.3s ease;
}

.sidebar-navigation.collapsed + * {
  margin-left: 64px;
}

.nav-menu-item {
  padding: var(--space-3) var(--space-4);
  margin: var(--space-2) var(--space-3);
  border-radius: var(--radius-lg);
  color: var(--gray-700);
  text-decoration: none;
  transition: all var(--transition-base);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: white;
  border: 1px solid var(--gray-200);
  box-shadow: var(--shadow-xs);
  min-height: 48px;
}

.logo-area {
  padding: var(--space-6) var(--space-4);
  text-align: center;
  border-bottom: 1px solid var(--gray-200);
  margin-bottom: var(--space-6);
  background: var(--primary-50);
}

.sidebar {
  width: 250px;
  background: var(--gray-50);
  border-right: 1px solid var(--gray-200);
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  transition: all var(--transition-base);
  display: flex;
  flex-direction: column;
  z-index: 200;
  box-shadow: var(--shadow-md);
}

.nav-menu {
  flex: 1;
  padding: 0 0 var(--space-6) 0;
}

.submenu-item {
  padding: var(--space-2) var(--space-4) var(--space-2) var(--space-12);
  margin: var(--space-1) var(--space-3);
  border-radius: var(--radius-md);
  color: var(--gray-600);
  background: var(--gray-50);
  border: 1px solid var(--gray-200);
  transition: all var(--transition-base);
  display: flex;
  align-items: center;
  text-decoration: none;
  font-size: var(--text-sm);
  min-height: 36px;
}
</style> 