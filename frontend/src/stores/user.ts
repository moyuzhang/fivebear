import { defineStore } from 'pinia'
import { ref } from 'vue'
import Cookies from 'js-cookie'
import { request } from '@/utils/request'
import type { LoginParams, LoginResult, UserInfo } from '@/types'
import { initGlobalWebSocket, destroyGlobalWebSocket } from '@/utils/unifiedWebSocket'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 存储key
const USER_INFO_KEY = 'fivebear-user-info'
const TOKEN_KEY = 'fivebear-token'

export const useUserStore = defineStore('user', () => {
  // 从localStorage恢复用户信息
  const getStoredUserInfo = (): UserInfo | null => {
    try {
      const stored = localStorage.getItem(USER_INFO_KEY)
      return stored ? JSON.parse(stored) : null
    } catch (error) {
      console.error('读取用户信息失败:', error)
      localStorage.removeItem(USER_INFO_KEY)
      return null
    }
  }

  // 保存用户信息到localStorage
  const saveUserInfo = (userInfo: UserInfo | null) => {
    try {
      if (userInfo) {
        localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
      } else {
        localStorage.removeItem(USER_INFO_KEY)
      }
    } catch (error) {
      console.error('保存用户信息失败:', error)
    }
  }

  // 状态
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<UserInfo | null>(getStoredUserInfo())
  const isLoggedIn = ref<boolean>(!!token.value && !!userInfo.value)
  const isLoading = ref<boolean>(false)

  // 强制登出处理器标记，防止重复绑定和重复处理
  let forceLogoutHandlerRegistered = false
  let forceLogoutInProgress = false
  let webSocketConnected = false  // 新增：WebSocket连接状态标记

  // 登录
  const login = async (loginParams: LoginParams): Promise<LoginResult> => {
    isLoading.value = true

    try {
      const response = await request.post<LoginResult>('/auth/login', loginParams)
      
      if (response.code === 200 && response.data) {
        const { token: accessToken, userInfo: user } = response.data
        
        // 保存token到localStorage
        token.value = accessToken
        localStorage.setItem(TOKEN_KEY, accessToken)
        
        // 保存用户信息到localStorage和状态
        userInfo.value = user
        saveUserInfo(user)
        isLoggedIn.value = true
        
        // 建立统一WebSocket连接
        setupWebSocketConnection()
        
        console.log('✅ 登录成功，用户信息:', user)
        return response.data
      } else {
        console.error('登录失败:', response.message)
        return Promise.reject(new Error(response.message || '登录失败'))
      }
    } catch (error: any) {
      console.error('登录请求失败:', error)
      return Promise.reject(error)
    } finally {
      isLoading.value = false
    }
  }

  // 登出
  const logout = async (silent: boolean = false, isForceLogout: boolean = false) => {
    try {
      if (token.value && !silent) {
        await request.post('/auth/logout')
      }
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 清除所有本地数据
      clearUserData(isForceLogout)
      console.log('✅ 已登出，清除本地数据')
    }
  }

  // 清除用户数据
  const clearUserData = (isForceLogout: boolean = false) => {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem(TOKEN_KEY)
    saveUserInfo(null)
    
    // 断开统一WebSocket连接
    destroyGlobalWebSocket()
    
    // 重置标记，但在强制登出过程中保持forceLogoutInProgress状态
    forceLogoutHandlerRegistered = false
    webSocketConnected = false
    
    // 只有在非强制登出时才重置forceLogoutInProgress
    if (!isForceLogout) {
      forceLogoutInProgress = false
    }
  }

  // 设置WebSocket连接（防止重复绑定事件和重复连接）
  const setupWebSocketConnection = () => {
    // 如果已经连接，不重复连接
    if (webSocketConnected) {
      console.log('🔌 WebSocket已连接，跳过重复连接')
      return
    }

    const webSocket = initGlobalWebSocket(token.value)
    
    // 监听强制登出（只注册一次）
    if (!forceLogoutHandlerRegistered) {
      webSocket.onForceLogout((message) => {
        console.warn('📥 User Store: 收到强制登出通知:', message.message)
        handleForceLogout(message)
      })
      forceLogoutHandlerRegistered = true
      console.log('🔌 WebSocket连接已建立并绑定强制登出事件监听器')
    } else {
      console.log('🔌 WebSocket连接已建立（强制登出监听器已存在）')
    }
    
    webSocketConnected = true
  }

  // 处理强制登出
  const handleForceLogout = async (message: any) => {
    // 防止重复处理
    if (forceLogoutInProgress) {
      console.log('⚠️ User Store: 强制登出正在进行中，忽略重复调用')
      return
    }
    
    forceLogoutInProgress = true
    console.log('🚨 User Store: 开始处理强制登出', message)
    
    try {
      // 显示用户友好的通知
      ElMessage({
        message: '🔒 检测到您的账户在其他设备登录，为了安全已自动退出当前会话',
        type: 'warning',
        duration: 6000,
        showClose: true
      })
      
      console.log('📤 User Store: 已显示强制登出提示')
      
      // 执行登出，标记为强制登出
      await logout(true, true)
      
      console.log('🔄 User Store: 强制登出完成，准备跳转到登录页')
      
      // 跳转到登录页面
      router.push('/login')
    } finally {
      // 重置强制登出标记
      forceLogoutInProgress = false
    }
  }

  // 获取用户信息
  const getUserInfo = async (): Promise<UserInfo | null> => {
    if (!token.value) {
      return null
    }

    try {
      const response = await request.get<UserInfo>('/auth/user-info')
      
      if (response.code === 200 && response.data) {
        userInfo.value = response.data
        saveUserInfo(response.data)
        isLoggedIn.value = true
        
        // 只有在没有WebSocket连接时才建立连接
        if (!webSocketConnected) {
          setupWebSocketConnection()
        }
        
        return response.data
      } else {
        // Token可能已过期，静默登出
        await logout(true)
        return null
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      await logout(true)
      return null
    }
  }

  // 验证Token有效性
  const validateToken = async (): Promise<boolean> => {
    if (!token.value) {
      return false
    }

    try {
      // 如果有用户信息，先尝试获取最新的用户信息来验证token
      const userInfoResult = await getUserInfo()
      return !!userInfoResult
    } catch (error) {
      console.error('Token验证失败:', error)
      await logout(true)
      return false
    }
  }

  // 初始化用户状态
  const initUser = async (): Promise<boolean> => {
    console.log('🔄 初始化用户状态...')
    
    // 如果没有token，直接返回
    if (!token.value) {
      console.log('❌ 未找到token，跳过初始化')
      clearUserData()
      return false
    }

    // 如果有token但没有用户信息，尝试获取
    if (!userInfo.value) {
      console.log('🔄 有token但无用户信息，尝试获取...')
      const result = await getUserInfo()
      // getUserInfo内部会在成功时建立WebSocket连接，这里不需要重复建立
      return !!result
    }

    // 如果都有，验证token有效性
    console.log('🔄 验证现有token有效性...')
    const isValid = await validateToken()
    
    if (isValid) {
      // Token有效，只有在没有WebSocket连接时才建立连接
      if (!webSocketConnected) {
        setupWebSocketConnection()
      }
      console.log('✅ 用户状态初始化成功')
    } else {
      console.log('❌ Token无效，用户状态初始化失败')
    }
    
    return isValid
  }

  // 更新用户信息
  const updateUserInfo = (newUserInfo: Partial<UserInfo>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...newUserInfo }
      saveUserInfo(userInfo.value)
    }
  }

  // 强制刷新用户信息
  const refreshUserInfo = async (): Promise<UserInfo | null> => {
    console.log('🔄 刷新用户信息...')
    return await getUserInfo()
  }

  // 检查是否为管理员
  const isAdmin = (): boolean => {
    return userInfo.value?.roleId === 1
  }

  // 检查是否为特定角色
  const hasRole = (roleId: number): boolean => {
    return userInfo.value?.roleId === roleId
  }

  // 检查用户状态是否有效
  const isUserActive = (): boolean => {
    return userInfo.value?.status === 1
  }

  return {
    // 状态
    token,
    userInfo,
    isLoggedIn,
    isLoading,
    
    // 方法
    login,
    logout,
    clearUserData,
    getUserInfo,
    validateToken,
    initUser,
    updateUserInfo,
    refreshUserInfo,
    isAdmin,
    hasRole,
    isUserActive
  }
}) 