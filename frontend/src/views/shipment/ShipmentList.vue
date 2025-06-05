<template>
  <!-- 导航栏 -->
  <Navigation />
  
  <div class="shipment-list">
    <el-card>
      <template #header>
        <span>📦 出货管理</span>
      </template>
      <div style="text-align: center; padding: 50px;">
        <h3>🚚 出货模块开发中...</h3>
        <p>该模块功能正在开发中，敬请期待</p>
        <el-button @click="$router.push('/dashboard')">返回首页</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import Navigation from '@/components/Navigation.vue'
// 出货列表组件
</script>

<style scoped>
.shipment-list {
  padding: 120px 20px 20px 20px; /* 增加顶部padding为导航栏留空间 */
}
</style> 