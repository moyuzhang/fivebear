import { request } from '@/utils/request'
import type { Result } from '@/types'

// 报表数据类型定义
export interface ReportData {
  date: string
  value: number
  label: string
  category?: string
}

export interface SalesReportParams {
  startDate: string
  endDate: string
  type: 'daily' | 'weekly' | 'monthly'
}

export interface RevenueReportParams {
  startDate: string
  endDate: string
}

export interface ProductRankingParams {
  startDate: string
  endDate: string
  limit: number
}

export interface ExportReportParams {
  reportType: string
  startDate: string
  endDate: string
  format: 'excel' | 'pdf' | 'csv'
}

// 报表API接口
export const reportApi = {
  // 获取销售报表
  getSalesReport(params: SalesReportParams): Promise<Result<ReportData[]>> {
    return request.get('/report/sales', params)
  },

  // 获取收入报表
  getRevenueReport(params: RevenueReportParams): Promise<Result<Record<string, any>>> {
    return request.get('/report/revenue', params)
  },

  // 获取产品销量排行
  getProductRanking(params: ProductRankingParams): Promise<Result<Record<string, any>[]>> {
    return request.get('/report/product-ranking', params)
  },

  // 获取客户分析报表
  getCustomerAnalysis(params: RevenueReportParams): Promise<Result<Record<string, any>>> {
    return request.get('/report/customer-analysis', params)
  },

  // 获取库存报表
  getInventoryReport(): Promise<Result<Record<string, any>[]>> {
    return request.get('/report/inventory')
  },

  // 获取财务概览
  getFinancialOverview(year: number, month?: number): Promise<Result<Record<string, any>>> {
    const params: any = { year }
    if (month) params.month = month
    return request.get('/report/financial-overview', params)
  },

  // 导出报表
  exportReport(params: ExportReportParams): Promise<Result<string>> {
    return request.post('/report/export', params)
  },

  // 获取实时统计
  getRealtimeStats(): Promise<Result<Record<string, any>>> {
    return request.get('/report/realtime-stats')
  },

  // 获取趋势分析
  getTrendAnalysis(analysisType: string, days: number = 30): Promise<Result<Record<string, any>>> {
    return request.get('/report/trend-analysis', { analysisType, days })
  },

  // 获取仪表盘数据
  getDashboardData(): Promise<Result<Record<string, any>>> {
    return request.get('/report/dashboard')
  },

  // 获取图表配置
  getChartConfig(chartType: string): Promise<Result<Record<string, any>>> {
    return request.get(`/report/chart-config/${chartType}`)
  }
} 