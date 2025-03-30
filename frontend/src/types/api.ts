import axios, { AxiosInstance, AxiosResponse, AxiosInterceptorManager } from 'axios'

export interface UserInfo {
  id: number;
  account: string;
  username: string;
  parentId: number;
  level: number;
  createTime: string;
  role: string | null;
  isActive: boolean;
  lastLoginTime: string;
  fullName: string | null;
  email: string | null;
  passwordChanged: boolean;
  authorities: Array<{ authority: string }>;
  enabled: boolean;
  accountNonExpired: boolean;
  credentialsNonExpired: boolean;
  accountNonLocked: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  userInfo: UserInfo;
  token: string;
}

export interface LogoutResponse {
  success: boolean;
  message: string;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  error: string | null;
  code: number;
}

export interface SystemStatus {
  cpuUsage: number;
  memoryUsage: number;
  diskUsage: number;
  onlineUsers: number;
  todayVisits: number;
  systemMessages: number;
  pendingTasks: number;
}

export interface SystemConfig {
  siteName: string;
  siteDescription: string;
  logo: string;
  theme: string;
  language: string;
}

export interface LogRecord {
  id: number;
  type: string;
  content: string;
  operator: string;
  ip: string;
  createTime: string;
}

export interface PageParams {
  page: number;
  size: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

export interface PageResponse<T> {
  total: number;
  items: T[];
}

export interface CustomAxiosInstance extends Omit<AxiosInstance, 'get' | 'post' | 'put' | 'delete' | 'interceptors'> {
  <T = any>(config: any): Promise<ApiResponse<T>>;
  get<T = any>(url: string, config?: any): Promise<ApiResponse<T>>;
  post<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>>;
  put<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>>;
  delete<T = any>(url: string, config?: any): Promise<ApiResponse<T>>;
  interceptors: {
    request: AxiosInterceptorManager<any>;
    response: AxiosInterceptorManager<AxiosResponse<ApiResponse>>;
  };
}

export type ApiRequest = ReturnType<typeof axios.create>; 