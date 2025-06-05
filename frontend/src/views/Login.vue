<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="background-animation">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
      <div class="shape shape-5"></div>
    </div>
    
    <!-- 登录表单 -->
    <div class="login-container">
      <div class="login-card glass-effect">
        <!-- Logo和标题 -->
        <div class="login-header">
          <div class="logo-wrapper">
            <span class="logo-icon">🐻</span>
          </div>
          <h1 class="app-title">FiveBear</h1>
          <p class="app-subtitle">企业管理系统</p>
        </div>
        
        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <!-- 记住我和忘记密码 -->
          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loginLoading"
              class="login-btn"
              @click="handleLogin"
            >
              <span v-if="!loginLoading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 其他登录方式 -->
        <div class="other-login">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="social-login">
            <el-button circle class="social-btn">
              <el-icon><Message /></el-icon>
            </el-button>
            <el-button circle class="social-btn">
              <el-icon><Share /></el-icon>
            </el-button>
            <el-button circle class="social-btn">
              <el-icon><Phone /></el-icon>
            </el-button>
          </div>
        </div>
        
        <!-- 页脚 -->
        <div class="login-footer">
          <p>© 2024 FiveBear System</p>
          <p>技术支持：FiveBear Tech</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { User, Lock, Message, Share, Phone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import type { LoginParams } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref<InstanceType<typeof ElForm>>()

// 登录表单数据
const loginForm = reactive<LoginParams>({
  username: '',
  password: ''
})

// 记住我
const rememberMe = ref(false)

// 表单验证规则
const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
})

// 登录状态
const loginLoading = ref(false)

// 处理登录
const handleLogin = async () => {
  // 表单验证
  if (!loginFormRef.value) {
    return
  }
  
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  // 检查锁定状态
  if (loginForm.username.trim()) {
    try {
      const lockStatus = await authApi.checkLockStatus(loginForm.username.trim())
      if (lockStatus.data?.isLocked) {
        const remainingMinutes = lockStatus.data.remainingMinutes || 0
        ElMessage.warning(`账户已被锁定，请${remainingMinutes}分钟后再试`)
        return
      }
    } catch (error) {
      console.error('检查锁定状态失败:', error)
    }
  }

  loginLoading.value = true

  try {
    console.log('🔐 开始登录...')
    
    // 执行登录
    await userStore.login(loginForm)
    
    // 登录成功
    console.log('✅ 登录成功')
    ElMessage.success('登录成功')
    
    // 获取重定向地址
    const redirect = (route.query.redirect as string) || '/dashboard'
    console.log('🔀 跳转到:', redirect)
    
    // 跳转到目标页面
    await router.push(redirect)
    
  } catch (error: any) {
    console.error('❌ 登录失败:', error)
    // 错误消息已在请求拦截器中处理，这里不再重复显示
  } finally {
    loginLoading.value = false
  }
}

// 组件挂载时检查登录状态
onMounted(async () => {
  console.log('📄 登录页面挂载')
  
  // 检查是否是强制登出
  const message = route.query.message as string
  if (message === 'forced_logout') {
    ElMessage.warning('您的账户在其他地方登录，已自动退出')
  }
  
  // 如果已经登录，直接跳转
  if (userStore.isLoggedIn && userStore.userInfo) {
    console.log('👤 用户已登录，准备跳转')
    
    const redirect = (route.query.redirect as string) || '/dashboard'
    console.log('🔀 跳转到:', redirect)
    
    await router.push(redirect)
    return
  }
  
  // 如果有token但没有用户信息，尝试初始化
  if (userStore.token && !userStore.userInfo) {
    console.log('🔄 检测到token，尝试恢复登录状态...')
    
    const initSuccess = await userStore.initUser()
    if (initSuccess) {
      const redirect = (route.query.redirect as string) || '/dashboard'
      console.log('🔀 状态恢复成功，跳转到:', redirect)
      await router.push(redirect)
    }
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 动态背景
.background-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 300px;
  height: 300px;
  background: white;
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.shape-2 {
  width: 200px;
  height: 200px;
  background: white;
  top: 50%;
  right: -100px;
  animation-delay: 2s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  background: white;
  bottom: -75px;
  left: 30%;
  animation-delay: 4s;
}

.shape-4 {
  width: 250px;
  height: 250px;
  background: white;
  top: 20%;
  left: 50%;
  animation-delay: 6s;
}

.shape-5 {
  width: 180px;
  height: 180px;
  background: white;
  bottom: 20%;
  right: 20%;
  animation-delay: 8s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  33% {
    transform: translateY(-100px) rotate(120deg);
  }
  66% {
    transform: translateY(100px) rotate(240deg);
  }
}

// 登录容器
.login-container {
  position: relative;
  z-index: 1;
  padding: $spacing-lg;
  animation: fadeInUp 0.8s ease;
}

.login-card {
  width: 420px;
  padding: $spacing-xl $spacing-xxl;
  border-radius: $radius-xl;
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 25px 45px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

// Logo和标题
.login-header {
  text-align: center;
  margin-bottom: $spacing-xxl;
}

.logo-wrapper {
  display: inline-block;
  margin-bottom: $spacing-md;
  animation: bounce 2s ease-in-out infinite;
}

.logo-icon {
  font-size: 64px;
  filter: drop-shadow(2px 4px 6px rgba(0, 0, 0, 0.1));
}

.app-title {
  font-size: $font-size-xxl;
  font-weight: bold;
  margin: 0 0 $spacing-xs 0;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.app-subtitle {
  font-size: $font-size-md;
  color: $text-secondary;
  margin: 0;
}

// 表单样式
.login-form {
  margin-bottom: $spacing-lg;
  
  :deep(.el-form-item) {
    margin-bottom: $spacing-lg;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: $radius-md;
    height: 48px;
    background: rgba(0, 0, 0, 0.03);
    box-shadow: none;
    border: 1px solid transparent;
    transition: all $duration-base;
    
    &:hover {
      background: rgba(0, 0, 0, 0.05);
    }
    
    &.is-focus {
      background: white;
      border-color: $primary-color;
      box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
    }
  }
  
  :deep(.el-input__inner) {
    font-size: $font-size-md;
  }
}

.input-icon {
  font-size: 18px;
  color: $text-secondary;
}

// 登录选项
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  
  .el-checkbox {
    color: $text-regular;
  }
  
  .el-link {
    font-size: $font-size-sm;
  }
}

// 登录按钮
.login-btn {
  width: 100%;
  height: 48px;
  font-size: $font-size-md;
  font-weight: 600;
  border-radius: $radius-md;
  background: linear-gradient(135deg, $primary-light 0%, $primary-color 100%);
  border: none;
  transition: all $duration-base;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(24, 144, 255, 0.3);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
  }
}

// 其他登录方式
.other-login {
  margin-top: $spacing-xl;
}

.divider {
  text-align: center;
  margin: $spacing-lg 0;
  position: relative;
  
  span {
    display: inline-block;
    padding: 0 $spacing-md;
    background: rgba(255, 255, 255, 0.95);
    color: $text-secondary;
    font-size: $font-size-sm;
    position: relative;
    z-index: 1;
  }
  
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: $divider-color;
  }
}

.social-login {
  display: flex;
  justify-content: center;
  gap: $spacing-md;
}

.social-btn {
  width: 48px;
  height: 48px;
  border: 1px solid $border-color;
  background: white;
  transition: all $duration-base;
  
  &:hover {
    border-color: $primary-color;
    color: $primary-color;
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}

// 页脚
.login-footer {
  text-align: center;
  margin-top: $spacing-xl;
  
  p {
    margin: $spacing-xs 0;
    font-size: $font-size-xs;
    color: $text-secondary;
  }
}

// 动画
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-card {
    width: 100%;
    max-width: 360px;
    padding: $spacing-lg;
  }
  
  .app-title {
    font-size: $font-size-xl;
  }
  
  .shape {
    display: none;
  }
}
</style>