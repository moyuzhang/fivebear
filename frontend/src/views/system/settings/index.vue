<template>
  <div class="settings-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
        </div>
      </template>
      
      <el-form :model="settings" label-width="120px">
        <el-form-item label="系统名称">
          <el-input v-model="settings.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        
        <el-form-item label="系统Logo">
          <el-upload
            class="logo-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleLogoSuccess"
            :before-upload="beforeLogoUpload"
          >
            <img v-if="settings.logo" :src="settings.logo" class="logo" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="系统主题">
          <el-radio-group v-model="settings.theme">
            <el-radio label="light">浅色</el-radio>
            <el-radio label="dark">深色</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="导航模式">
          <el-radio-group v-model="settings.navMode">
            <el-radio label="side">侧边菜单</el-radio>
            <el-radio label="top">顶部菜单</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="系统公告">
          <el-input
            v-model="settings.announcement"
            type="textarea"
            :rows="3"
            placeholder="请输入系统公告"
          />
        </el-form-item>
        
        <el-form-item label="开启注册">
          <el-switch v-model="settings.enableRegister" />
        </el-form-item>
        
        <el-form-item label="开启验证码">
          <el-switch v-model="settings.enableCaptcha" />
        </el-form-item>
        
        <el-form-item label="会话超时">
          <el-input-number
            v-model="settings.sessionTimeout"
            :min="30"
            :max="1440"
            :step="30"
          />
          <span class="form-text">分钟</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 模拟数据
const settings = ref({
  systemName: 'MQTT管理系统',
  logo: '',
  theme: 'light',
  navMode: 'side',
  announcement: '欢迎使用MQTT管理系统！',
  enableRegister: false,
  enableCaptcha: true,
  sessionTimeout: 120
})

const handleLogoSuccess = (response: any) => {
  settings.value.logo = response.url
  ElMessage.success('Logo上传成功')
}

const beforeLogoUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

const handleSave = () => {
  ElMessage.success('设置保存成功')
}
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.logo-uploader:hover {
  border-color: #409EFF;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.logo {
  width: 178px;
  height: 178px;
  display: block;
}

.form-text {
  margin-left: 10px;
  color: #909399;
}
</style> 