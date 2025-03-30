import request from '@/api/request'

export interface DashboardData {
  date: string
  name: string
  value: string
}

export const getDashboardData = () => {
  return request.get<DashboardData[]>('/api/dashboard/data')
} 