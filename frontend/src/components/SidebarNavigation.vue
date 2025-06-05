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
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  position: fixed;
  left: 0;
  top: 0;
  z-index: 999;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.sidebar-navigation.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
}

.logo {
  display: flex;
  align-items: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.logo .el-icon {
  font-size: 24px;
  margin-right: 8px;
  color: #3498db;
}

.logo-text {
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.collapse-btn {
  color: #ffffff;
  border: none;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background-color: #3498db;
  transform: scale(1.05);
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent;
  overflow-y: auto;
  padding: 8px 0;
}

.sidebar-menu .el-menu-item {
  color: #ffffff !important;
  height: 48px;
  line-height: 48px;
  font-size: 15px !important;
  font-weight: 500 !important;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: transparent;
}

.sidebar-menu .el-menu-item span {
  color: #ffffff !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.sidebar-menu .el-menu-item .el-icon {
  color: #3498db !important;
  font-size: 18px !important;
}

.sidebar-menu .el-menu-item:hover {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%) !important;
  color: #ffffff !important;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.sidebar-menu .el-menu-item:hover span {
  color: #ffffff !important;
}

.sidebar-menu .el-menu-item:hover .el-icon {
  color: #ffffff !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%) !important;
  color: #ffffff !important;
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

.sidebar-menu .el-menu-item.is-active span {
  color: #ffffff !important;
}

.sidebar-menu .el-menu-item.is-active .el-icon {
  color: #ffffff !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title {
  color: #ffffff !important;
  height: 48px;
  line-height: 48px;
  font-size: 15px !important;
  font-weight: 500 !important;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: transparent;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title span {
  color: #ffffff !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.sidebar-menu .el-sub-menu .el-sub-menu__title .el-icon {
  color: #3498db !important;
  font-size: 18px !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%) !important;
  color: #ffffff !important;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover span {
  color: #ffffff !important;
}

.sidebar-menu .el-sub-menu .el-sub-menu__title:hover .el-icon {
  color: #ffffff !important;
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title {
  color: #ffffff !important;
  background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%) !important;
  box-shadow: 0 4px 12px rgba(155, 89, 182, 0.3);
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title span {
  color: #ffffff !important;
}

.sidebar-menu .el-sub-menu.is-active .el-sub-menu__title .el-icon {
  color: #ffffff !important;
}

.sidebar-menu .el-sub-menu .el-menu {
  background: rgba(0, 0, 0, 0.2);
  margin: 0 8px;
  border-radius: 8px;
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item {
  background: transparent;
  margin: 2px 8px;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.9) !important;
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item:hover {
  background: rgba(52, 152, 219, 0.8) !important;
  color: #ffffff !important;
  transform: translateX(2px);
}

.sidebar-menu .el-sub-menu .el-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%) !important;
  color: #ffffff !important;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: #ffffff;
  font-weight: bold;
  margin-right: 12px;
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.3);
}

.user-details {
  flex: 1;
  color: #ffffff;
}

.username {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 2px;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.role {
  font-size: 12px;
  color: #bdc3c7;
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