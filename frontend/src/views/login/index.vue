<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <div class="brand">
          <h1>FiveBear</h1>
          <p>欢迎使用 FiveBear 管理系统</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <el-icon><Monitor /></el-icon>
            <span>现代化的界面设计</span>
          </div>
          <div class="feature-item">
            <el-icon><Lock /></el-icon>
            <span>安全可靠的系统架构</span>
          </div>
          <div class="feature-item">
            <el-icon><Setting /></el-icon>
            <span>灵活的系统配置</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-container">
          <h2>账号登录</h2>
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleLogin" 
                :loading="loading" 
                class="login-button"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Monitor, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        router.push('/home')
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1f4037 0%, #99f2c8 100%);
}

.login-box {
  width: 1000px;
  height: 600px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
  animation: fadeIn 0.5s ease-out;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1f4037 0%, #99f2c8 100%);
  padding: 40px;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand {
  text-align: center;
  margin-top: 60px;
}

.brand h1 {
  font-size: 36px;
  margin: 0;
  font-weight: 600;
  letter-spacing: 2px;
}

.brand p {
  font-size: 16px;
  margin: 10px 0 0;
  opacity: 0.9;
}

.features {
  margin-bottom: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin: 20px 0;
  font-size: 16px;
}

.feature-item .el-icon {
  font-size: 24px;
  margin-right: 12px;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-form-container {
  width: 100%;
  max-width: 400px;
}

.login-form-container h2 {
  text-align: center;
  color: #303133;
  margin-bottom: 30px;
  font-size: 28px;
}

.login-form {
  margin-top: 20px;
}

.login-form :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #409EFF;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 22px;
  background: linear-gradient(135deg, #1f4037 0%, #99f2c8 100%);
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(31, 64, 55, 0.2);
}

.login-button:active {
  transform: translateY(0);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media screen and (max-width: 768px) {
  .login-box {
    width: 90%;
    height: auto;
    flex-direction: column;
  }

  .login-left {
    padding: 30px;
  }

  .brand h1 {
    font-size: 28px;
  }

  .features {
    display: none;
  }

  .login-right {
    padding: 30px;
  }
}
</style> 