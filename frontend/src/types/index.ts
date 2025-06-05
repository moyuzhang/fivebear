// 通用响应接口
export interface Result<T = any> {
  code: number
  message: string
  data?: T
  timestamp: number
}

// 分页参数
export interface PageParams {
  page: number
  size: number
  keyword?: string
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  status: number
  roleId: number
  roleName: string
}

// 登录参数
export interface LoginParams {
  username: string
  password: string
  captcha?: string
}

// 登录响应
export interface LoginResult {
  token: string
  userInfo: UserInfo
}

// 菜单项
export interface MenuItem {
  id: number
  name: string
  path: string
  icon?: string
  parentId: number
  children?: MenuItem[]
}

// Vite环境变量类型已由vite-env.d.ts提供 