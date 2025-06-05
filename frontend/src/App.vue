<template>
  <div id="app">
    <el-config-provider :locale="zhCn">
      <router-view />
      <WebSocketStatus />
    </el-config-provider>
  </div>
</template>

<script setup lang="ts">
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import WebSocketStatus from '@/components/WebSocketStatus.vue'
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
}

* {
  box-sizing: border-box;
}
</style>