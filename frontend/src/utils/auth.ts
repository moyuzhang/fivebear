import { ElMessage } from 'element-plus'
import router from '../router'
import { userApi } from '../api/user'

export const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
}

export const handleLogout = async () => {
  try {
    const response = await userApi.logout()
    if (response.code === 200) {
      clearAuth()
      ElMessage.success('退出登录成功')
      router.push('/login')
    } else {
      ElMessage.error(response.message || '退出登录失败')
    }
  } catch (error: any) {
    console.error('退出登录错误:', error)
    // 即使请求失败，也清除本地存储并跳转
    clearAuth()
    ElMessage.error('退出登录失败，请重试')
    router.push('/login')
  }
} 