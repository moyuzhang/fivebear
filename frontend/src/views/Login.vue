<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">🐻</div>
        <h2>FiveBear 系统登录</h2>
      </div>
      
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
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loginLoading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loginLoading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>© 2024 FiveBear System. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
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

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 48px;
  margin-bottom: 15px;
  display: inline-block;
}

.login-header h2 {
  color: #333;
  margin: 0;
  font-weight: 600;
}

.login-form {
  margin-bottom: 20px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  border-radius: 6px;
}

.login-footer {
  text-align: center;
  color: #888;
  font-size: 12px;
}

.login-footer p {
  margin: 0;
}
</style>