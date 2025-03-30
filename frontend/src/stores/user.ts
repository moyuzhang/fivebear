import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

interface UserInfo {
  id: number
  username: string
  token: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref<UserInfo | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  const login = async (loginForm: { username: string; password: string }) => {
    try {
      // 这里应该调用实际的登录API
      // 模拟登录成功
      const mockUserInfo = {
        id: 1,
        username: loginForm.username,
        token: 'mock-token'
      }
      
      token.value = mockUserInfo.token
      userInfo.value = mockUserInfo
      
      // 存储到本地
      localStorage.setItem('token', mockUserInfo.token)
      localStorage.setItem('userInfo', JSON.stringify(mockUserInfo))
      
      return mockUserInfo
    } catch (error: any) {
      throw new Error(error.message || '登录失败')
    }
  }

  const logout = async () => {
    try {
      // 这里应该调用实际的登出API
      token.value = ''
      userInfo.value = null
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    } catch (error: any) {
      throw new Error(error.message || '退出失败')
    }
  }

  const initializeFromStorage = () => {
    const storedToken = localStorage.getItem('token')
    const storedUserInfo = localStorage.getItem('userInfo')
    
    if (storedToken && storedUserInfo) {
      token.value = storedToken
      userInfo.value = JSON.parse(storedUserInfo)
    }
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    login,
    logout,
    initializeFromStorage
  }
}) 