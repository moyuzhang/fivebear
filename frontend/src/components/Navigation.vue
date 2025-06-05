<template>
  <div class="navigation">
    <el-header class="header">
      <div class="header-left">
        <!-- 空白区域，为侧边栏留空间 -->
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
    </el-header>
    
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentRouteName">
          {{ currentRouteTitle }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, User, Setting, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前路由信息
const currentRouteName = computed(() => route.name)
const currentRouteTitle = computed(() => route.meta?.title || '')

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中...')
      break
      
    case 'settings':
      ElMessage.info('系统设置功能开发中...')
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
    
    // 显示退出中的提示
    const loadingMessage = ElMessage({
      message: '退出中...',
      type: 'info',
      duration: 0
    })
    
    try {
      // 执行登出
      await userStore.logout()
      
      loadingMessage.close()
      ElMessage.success('已成功退出登录')
      
      console.log('✅ 退出登录成功，跳转到登录页')
      
      // 跳转到登录页
      await router.push('/login')
      
    } catch (error) {
      loadingMessage.close()
      console.error('❌ 退出登录失败:', error)
      ElMessage.error('退出登录失败，请重试')
    }
    
  } catch {
    // 用户取消
    console.log('❌ 用户取消退出登录')
  }
}
</script>

<style scoped>
.navigation {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background: #fff;
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

/* 确保页面内容不被导航栏遮挡 */
.navigation + * {
  margin-top: 100px;
}
</style> 