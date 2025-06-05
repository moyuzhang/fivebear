<template>
  <aside class="sidebar" :class="{ collapsed: isCollapsed }">
    <!-- 侧边栏头部 -->
    <div class="sidebar-header">
      <div class="logo-wrapper" v-if="!isCollapsed">
        <span class="logo-icon">🐻</span>
        <span class="logo-text">FiveBear</span>
      </div>
      <el-button 
        v-else
        class="logo-collapsed"
        text
        @click="toggleCollapse"
      >
        🐻
      </el-button>
    </div>

    <!-- 导航菜单 -->
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      :collapse="isCollapsed"
      :unique-opened="true"
      :collapse-transition="false"
      router
    >
      <!-- 仪表盘 -->
      <el-menu-item index="/dashboard" class="menu-item-custom">
        <el-icon><Odometer /></el-icon>
        <template #title>仪表盘</template>
      </el-menu-item>

      <!-- 站点管理 -->
      <el-menu-item index="/sites" class="menu-item-custom">
        <el-icon><Monitor /></el-icon>
        <template #title>站点管理</template>
      </el-menu-item>

      <!-- 管理员功能 -->
      <el-sub-menu index="admin" v-if="userStore.userInfo?.roleName === '管理员'" class="submenu-custom">
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
      <el-sub-menu index="report" class="submenu-custom">
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
      <el-sub-menu index="shipment" class="submenu-custom">
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
    </el-menu>

    <!-- 侧边栏底部 -->
    <div class="sidebar-footer">
      <el-button 
        class="collapse-btn" 
        :icon="isCollapsed ? Expand : Fold" 
        @click="toggleCollapse"
        circle
        size="small"
      />
      <div class="user-card" v-if="!isCollapsed">
        <el-avatar :size="32" class="user-avatar gradient-primary">
          {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="user-details">
          <div class="username">{{ userStore.userInfo?.nickname }}</div>
          <div class="role">{{ userStore.userInfo?.roleName }}</div>
        </div>
      </div>
    </div>
  </aside>
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
  document.documentElement.style.setProperty('--sidebar-width', newVal ? '64px' : '260px')
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

<style scoped lang="scss">
@import '@/styles/variables.scss';

.sidebar {
  width: $sidebar-width;
  height: 100vh;
  background: linear-gradient(180deg, #1e3c72 0%, #2a5298 100%);
  position: fixed;
  left: 0;
  top: 0;
  z-index: $z-index-fixed;
  transition: all $duration-base cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.1);
  
  &.collapsed {
    width: $sidebar-collapsed-width;
    
    .sidebar-header {
      padding: $spacing-md $spacing-sm;
    }
  }
}

// 头部样式
.sidebar-header {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  animation: slideInLeft 0.5s ease;
}

.logo-icon {
  font-size: 32px;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.2));
  animation: bounce 2s ease-in-out infinite;
}

.logo-text {
  font-size: $font-size-xl;
  font-weight: bold;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.logo-collapsed {
  font-size: 28px;
  width: 40px;
  height: 40px;
  padding: 0;
  border: none;
  background: transparent;
  
  &:hover {
    transform: scale(1.1);
    filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
  }
}

// 菜单样式
.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent;
  overflow-y: auto;
  overflow-x: hidden;
  padding: $spacing-md 0;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.3);
    border-radius: 2px;
  }
  
  // 菜单项样式
  :deep(.el-menu-item) {
    color: rgba(255, 255, 255, 0.85);
    margin: 0 $spacing-sm;
    border-radius: $radius-md;
    transition: all $duration-fast;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
      color: white;
    }
    
    &.is-active {
      background: rgba(255, 255, 255, 0.2);
      color: white;
      font-weight: 600;
      position: relative;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 70%;
        background: white;
        border-radius: 0 3px 3px 0;
      }
    }
    
    .el-icon {
      font-size: 18px;
    }
  }
  
  // 子菜单样式
  :deep(.el-sub-menu) {
    .el-sub-menu__title {
      color: rgba(255, 255, 255, 0.85);
      margin: 0 $spacing-sm;
      border-radius: $radius-md;
      transition: all $duration-fast;
      
      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: white;
      }
      
      .el-icon {
        font-size: 18px;
      }
    }
    
    &.is-opened > .el-sub-menu__title {
      background: rgba(255, 255, 255, 0.1);
      color: white;
    }
    
    .el-menu {
      background: rgba(0, 0, 0, 0.1);
      
      .el-menu-item {
        padding-left: 50px !important;
        font-size: $font-size-sm;
        min-height: 40px;
      }
    }
  }
}

// 底部样式
.sidebar-footer {
  padding: $spacing-md;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.collapse-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  transition: all $duration-fast;
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: rotate(180deg);
  }
}

.user-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border-radius: $radius-md;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.user-avatar {
  flex-shrink: 0;
  font-weight: bold;
  color: white;
  box-shadow: $shadow-md;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.username {
  font-size: $font-size-sm;
  font-weight: 600;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

// 动画
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    
    &.mobile-open {
      transform: translateX(0);
    }
  }
}
</style> 