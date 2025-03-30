<template>
  <div class="api-doc-container">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-if="error" class="error">{{ error }}</div>
    <iframe
      v-show="!loading && !error"
      :src="apiDocUrl"
      frameborder="0"
      class="api-doc-frame"
      title="API Documentation"
      @load="handleLoad"
      @error="handleError"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const loading = ref(true)
const error = ref('')

const apiDocUrl = computed(() => {
  // 使用相对路径，让 Vite 代理处理请求
  return '/api/doc.html'
})

const handleLoad = () => {
  loading.value = false
  error.value = ''
}

const handleError = (e: Event) => {
  loading.value = false
  error.value = '加载文档失败，请检查网络连接或刷新页面重试'
  console.error('iframe load error:', e)
}

onMounted(() => {
  console.log('Component mounted, URL:', apiDocUrl.value)
})
</script>

<style scoped>
.api-doc-container {
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  position: relative;
}

.api-doc-frame {
  width: 100%;
  height: calc(100vh - 100px);
  border: none;
}

.loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
  color: #606266;
}

.error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #f56c6c;
  text-align: center;
}
</style> 