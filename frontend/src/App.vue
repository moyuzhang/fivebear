<template>
  <div id="app">
    <el-config-provider :locale="zhCn">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
      <WebSocketStatus />
    </el-config-provider>
  </div>
</template>

<script setup lang="ts">
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import WebSocketStatus from '@/components/WebSocketStatus.vue'
</script>

<style lang="scss">
@import '@/styles/global.scss';

#app {
  height: 100%;
  min-height: 100vh;
  background-color: $bg-color;
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>