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
          {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="user-details">
          <div class="username">{{ userStore.userInfo?.nickname }}</div>
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
  background: #ffffff;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 999;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5e7eb;
}

.sidebar-navigation.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #f3f4f6;
  background: #ffffff;
}

.logo {
  display: flex;
  align-items: center;
  color: #1f2937;
  font-size: 18px;
  font-weight: bold;
}

.logo .el-icon {
  font-size: 24px;
  margin-right: 10px;
  color: #6366f1;
}

.logo-text {
  color: #1f2937;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.collapse-btn {
  color: #6b7280;
  border: none;
  border-radius: 6px;
  transition: all 0.2s ease;
  width: 32px;
  height: 32px;
}

.collapse-btn:hover {
  background-color: #f3f4f6;
  color: #374151;
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: #ffffff;
  overflow-y: auto;
  padding: 12px 0;
}

.sidebar-menu .el-menu-item {
  color: #6b7280 !important;
  height: 44px;
  line-height: 44px;
  font-size: 14px !important;
  font-weight: 500 !important;
  margin: 2px 16px;
  border-radius: 6px;
  transition: all 0.2s ease;
  background: transparent;
}

.sidebar-menu .el-menu-item span {
  color: #6b7280 !important;
}

.sidebar-menu .el-menu-item .el-icon {
  color: #9ca3af !important;
  font-size: 16px !important;
  margin-right: 12px;
}

.sidebar-menu .el-menu-item:hover {
  background: #f9fafb !important;
  color: #374151 !important;
}

.sidebar-menu .el-menu-item:hover span {
  color: #374151 !important;
}

.sidebar-menu .el-menu-item:hover .el-icon {
  color: #6366f1 !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: #eef2ff !important;
  color: #6366f1 !important;
  border-left: 3px solid #6366f1;
  margin-left: 16px;
  padding-left: 13px;
}

.sidebar-menu .el-menu-item.is-active span {
  color: #6366f1 !important;
  font-weight: 600 !important;
}

.sidebar-menu .el-menu-item.is-active .el-icon {
  color: #6366f1 !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title {
  color: #6b7280 !important;
  height: 44px;
  line-height: 44px;
  font-size: 14px !important;
  font-weight: 500 !important;
  margin: 2px 16px;
  border-radius: 6px;
  transition: all 0.2s ease;
  background: transparent;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title span {
  color: #6b7280 !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title .el-icon {
  color: #9ca3af !important;
  font-size: 16px !important;
  margin-right: 12px;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover {
  background: #f9fafb !important;
  color: #374151 !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover span {
  color: #374151 !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover .el-icon {
  color: #6366f1 !important;
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title {
  color: #6366f1 !important;
  background: #eef2ff !important;
  font-weight: 600 !important;
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title span {
  color: #6366f1 !important;
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title .el-icon {
  color: #6366f1 !important;
}

.sidebar-menu .el-sub-menu .el-menu {
  background: #f8fafc;
  margin: 4px 16px;
  border-radius: 6px;
  padding: 4px 0;
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item {
  background: transparent;
  margin: 1px 8px;
  border-radius: 4px;
  color: #64748b !important;
  font-size: 13px !important;
  height: 36px;
  line-height: 36px;
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item:hover {
  background: #e2e8f0 !important;
  color: #475569 !important;
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item.is-active {
  background: #ddd6fe !important;
  color: #7c3aed !important;
  font-weight: 600 !important;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #f3f4f6;
  background: #ffffff;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  background: #6366f1;
  color: #ffffff;
  font-weight: 600;
  margin-right: 12px;
}

.user-details {
  flex: 1;
  color: #1f2937;
}

.username {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 2px;
  color: #1f2937;
}

.role {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

/* 调整主内容区域的左边距 */
.sidebar-navigation + * {
  margin-left: 250px;
  transition: margin-left 0.3s ease;
}

.sidebar-navigation.collapsed + * {
  margin-left: 64px;
}
</style> 