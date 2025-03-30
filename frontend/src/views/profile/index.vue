<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人设置</span>
          <el-button type="primary" @click="saveProfile" :loading="loading">
            保存修改
          </el-button>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" disabled />
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="全名" prop="fullName">
          <el-input v-model="form.fullName" placeholder="请输入全名" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-divider>修改密码</el-divider>
        
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input 
            v-model="form.oldPassword" 
            type="password" 
            placeholder="请输入旧密码" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="form.newPassword" 
            type="password" 
            placeholder="请输入新密码" 
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            show-password
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'
import md5 from 'crypto-js/md5'
import request from '@/utils/request'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  account: '',
  username: '',
  fullName: '',
  email: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  newPassword: [
    { min: 6, message: '密码长度不能小于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const initForm = () => {
  if (userStore.userInfo) {
    form.account = userStore.userInfo.account || ''
    form.username = userStore.userInfo.username || ''
    form.fullName = userStore.userInfo.fullName || ''
    form.email = userStore.userInfo.email || ''
  }
}

const saveProfile = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const profileData: any = {
      username: form.username,
      fullName: form.fullName,
      email: form.email
    }
    
    // 如果填写了密码，添加密码字段
    if (form.oldPassword && form.newPassword) {
      profileData.oldPassword = md5(form.oldPassword).toString().toLowerCase()
      profileData.newPassword = md5(form.newPassword).toString().toLowerCase()
    }
    
    const response = await request.put('/user/profile', profileData)
    
    // 更新本地用户信息
    userStore.setUserInfo({
      ...userStore.userInfo!,
      username: form.username,
      fullName: form.fullName,
      email: form.email
    })
    
    ElMessage.success('个人信息更新成功')
    
    // 清除密码字段
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
  } catch (error: any) {
    console.error('更新个人信息失败:', error)
    ElMessage.error(error.message || '更新个人信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initForm()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 