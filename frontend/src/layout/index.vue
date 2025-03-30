<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapse': isCollapse }">
      <div class="logo-container">
        <el-icon class="logo-icon"><Monitor /></el-icon>
        <span v-show="!isCollapse">MQTT 管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleSelect"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu index="/mqtt">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>MQTT 管理</span>
          </template>
          <el-menu-item index="/mqtt/connections">连接管理</el-menu-item>
          <el-menu-item index="/mqtt/devices">设备管理</el-menu-item>
          <el-menu-item index="/mqtt/messages">消息管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">用户管理</el-menu-item>
          <el-menu-item index="/system/roles">角色管理</el-menu-item>
          <el-menu-item index="/system/settings">系统设置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- 主容器 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="left">
          <el-icon 
            class="collapse-btn"
            @click="toggleSidebar"
          >
            <Fold v-if="!isCollapse"/>
            <Expand v-else/>
          </el-icon>
          <breadcrumb />
        </div>
        <div class="right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">Admin</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 标签页区域 -->
      <div class="tabs-container">
        <el-tabs
          v-model="activeTab"
          type="card"
          closable
          @tab-remove="removeTab"
          @tab-click="handleTabClick"
        >
          <el-tab-pane
            v-for="tab in visitedTabs"
            :key="tab.path"
            :label="tab.title"
            :name="tab.path"
            :closable="tab.path !== '/home'"
            @close="removeTab(tab.path)"
          >
            <router-view v-slot="{ Component }">
              <transition name="fade-transform" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  HomeFilled,
  Monitor,
  Setting,
  Fold,
  Expand
} from '@element-plus/icons-vue'
import Breadcrumb from './components/Breadcrumb.vue'

const router = useRouter()
const route = useRoute()

// 侧边栏折叠状态
const isCollapse = ref(false)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 标签页相关
const activeTab = ref('/home')
const visitedTabs = ref([
  { title: '首页', path: '/home' }
])

// 处理菜单选择
const handleSelect = (path: string) => {
  if (path === activeTab.value) return
  
  // 检查标签是否已存在
  const existingTab = visitedTabs.value.find(tab => tab.path === path)
  if (!existingTab) {
    // 获取路由元信息中的标题
    const route = router.resolve(path)
    const title = route.meta?.title || '未命名页面'
    visitedTabs.value.push({ title, path })
  }
  activeTab.value = path
  router.push(path)
}

// 处理标签点击
const handleTabClick = (tab: any) => {
  router.push(tab.props.name)
}

// 移除标签
const removeTab = (path: string) => {
  const tabs = visitedTabs.value
  let activePath = activeTab.value
  
  if (activePath === path) {
    tabs.forEach((tab, index) => {
      if (tab.path === path) {
        const nextTab = tabs[index + 1] || tabs[index - 1]
        if (nextTab) {
          activePath = nextTab.path
        }
      }
    })
  }
  
  visitedTabs.value = tabs.filter(tab => tab.path !== path)
  if (activePath === path) {
    activeTab.value = activePath
    router.push(activePath)
  }
}

// 处理用户下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      // 处理登出逻辑
      break
  }
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.sidebar-container.is-collapse {
  width: 64px;
}

.logo-container {
  height: 50px;
  padding: 10px;
  display: flex;
  align-items: center;
  background: #2b2f3a;
}

.logo-icon {
  font-size: 24px;
  color: #409EFF;
  margin-right: 12px;
}

.logo-container span {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.el-menu-vertical {
  border-right: none;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 210px;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
}

.left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px;
}

.right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

.tabs-container {
  flex: 1;
  padding: 10px;
  background: #f0f2f5;
  overflow: auto;
}

:deep(.el-tabs__header) {
  margin-bottom: 10px;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

:deep(.el-tabs__item) {
  height: 40px;
  line-height: 40px;
}

:deep(.el-tabs__content) {
  height: calc(100% - 50px);
  overflow: auto;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style> 