<template>
  <router-view />
</template>

<script setup lang="ts">
// 这是一个路由容器组件，用于渲染子路由
</script>

<style scoped>
.system-container {
  height: 100%;
  width: 100%;
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