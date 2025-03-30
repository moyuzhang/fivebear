<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { IpPoolSettings } from '@/types/ipPool';
import { getPoolSettings, updatePoolSettings } from '@/api/ipPool';

const loading = ref(false);
const settings = ref<IpPoolSettings>({
  autoFetch: true,
  fetchInterval: 30,
  maxIpCount: 100,
  minSpeed: 1000,
  testUrl: 'https://www.baidu.com'
});

// 获取IP池设置
const fetchSettings = async () => {
  loading.value = true;
  try {
    const data = await getPoolSettings();
    settings.value = data;
  } catch (error: any) {
    ElMessage.error(error.message || '获取IP池设置失败');
  } finally {
    loading.value = false;
  }
};

// 保存设置
const handleSave = async () => {
  loading.value = true;
  try {
    await updatePoolSettings(settings.value);
    ElMessage.success('设置保存成功');
  } catch (error: any) {
    ElMessage.error(error.message || '设置保存失败');
  } finally {
    loading.value = false;
  }
};

// 重置设置
const handleReset = () => {
  fetchSettings();
};

onMounted(() => {
  fetchSettings();
});
</script>

<template>
  <div class="ip-pool-settings">
    <div class="header">
      <h2>IP池设置</h2>
      <div class="header-buttons">
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" :loading="loading" @click="handleSave">保存设置</el-button>
      </div>
    </div>

    <el-card v-loading="loading" class="settings-card">
      <el-form :model="settings" label-width="120px">
        <el-form-item label="自动获取代理">
          <el-switch v-model="settings.autoFetch" />
          <div class="form-tip">启用后系统会自动从代理源获取新的代理IP</div>
        </el-form-item>

        <el-form-item label="获取间隔(分钟)">
          <el-input-number 
            v-model="settings.fetchInterval" 
            :min="1" 
            :max="1440"
            :disabled="!settings.autoFetch"
            style="width: 180px"
          />
          <div class="form-tip">自动获取代理的时间间隔，范围1-1440分钟</div>
        </el-form-item>

        <el-form-item label="最大IP数量">
          <el-input-number 
            v-model="settings.maxIpCount" 
            :min="1" 
            :max="1000"
            style="width: 180px"
          />
          <div class="form-tip">IP池中允许存储的最大IP数量，范围1-1000</div>
        </el-form-item>

        <el-form-item label="最小响应时间(ms)">
          <el-input-number 
            v-model="settings.minSpeed" 
            :min="100" 
            :max="10000"
            :step="100"
            style="width: 180px"
          />
          <div class="form-tip">IP响应时间的最小阈值，范围100-10000毫秒</div>
        </el-form-item>

        <el-form-item label="测试URL">
          <el-input 
            v-model="settings.testUrl" 
            placeholder="请输入测试URL"
            style="width: 400px"
          />
          <div class="form-tip">用于验证代理IP可用性的测试网址</div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.ip-pool-settings {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.settings-card {
  max-width: 800px;
  margin: 0 auto;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-form-item) {
  margin-bottom: 25px;
}

:deep(.el-input-number) {
  width: 180px;
}
</style> 