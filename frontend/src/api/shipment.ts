import { request } from '@/utils/request'
import type { Result, PageParams, PageResult } from '@/types'

// 出货单类型定义
export interface Shipment {
  id?: number
  orderNumber: string
  customerName: string
  customerAddress: string
  customerPhone: string
  products: ShipmentProduct[]
  totalAmount: number
  status: 'pending' | 'confirmed' | 'shipped' | 'delivered' | 'cancelled'
  shippingMethod: string
  trackingNumber?: string
  estimatedDelivery?: string
  actualDelivery?: string
  notes?: string
  createTime?: string
  updateTime?: string
}

export interface ShipmentProduct {
  productId: number
  productName: string
  productSku: string
  quantity: number
  unitPrice: number
  totalPrice: number
}

export interface ShipmentListParams extends PageParams {
  status?: string
  startDate?: string
  endDate?: string
}

export interface ShipmentCreateParams {
  customerName: string
  customerAddress: string
  customerPhone: string
  products: ShipmentProduct[]
  shippingMethod: string
  notes?: string
}

export interface LogisticsInfo {
  carrier: string
  trackingNumber: string
  estimatedDelivery: string
  notes?: string
}

export interface ShipmentStatisticsParams {
  startDate?: string
  endDate?: string
}

// 出货API接口
export const shipmentApi = {
  // 获取出货单列表
  getShipmentList(params: ShipmentListParams): Promise<Result<PageResult<Shipment>>> {
    return request.get('/shipment/list', params)
  },

  // 获取出货单详情
  getShipmentById(id: number): Promise<Result<Shipment>> {
    return request.get(`/shipment/${id}`)
  },

  // 创建出货单
  createShipment(data: ShipmentCreateParams): Promise<Result<Shipment>> {
    return request.post('/shipment', data)
  },

  // 更新出货单
  updateShipment(id: number, data: Partial<Shipment>): Promise<Result<Shipment>> {
    return request.put(`/shipment/${id}`, data)
  },

  // 删除出货单
  deleteShipment(id: number): Promise<Result<void>> {
    return request.delete(`/shipment/${id}`)
  },

  // 确认出货
  confirmShipment(id: number): Promise<Result<void>> {
    return request.post(`/shipment/${id}/confirm`)
  },

  // 取消出货
  cancelShipment(id: number, reason: string): Promise<Result<void>> {
    return request.post(`/shipment/${id}/cancel`, { reason })
  },

  // 更新物流信息
  updateLogistics(id: number, logisticsInfo: LogisticsInfo): Promise<Result<void>> {
    return request.post(`/shipment/${id}/logistics`, logisticsInfo)
  },

  // 获取物流跟踪
  getShipmentTracking(id: number): Promise<Result<Record<string, any>[]>> {
    return request.get(`/shipment/${id}/tracking`)
  },

  // 批量出货
  batchShipment(shipmentIds: number[]): Promise<Result<Record<string, any>>> {
    return request.post('/shipment/batch-ship', { shipmentIds })
  },

  // 生成出货标签
  generateShippingLabel(id: number): Promise<Result<string>> {
    return request.post(`/shipment/${id}/generate-label`)
  },

  // 获取出货统计
  getShipmentStatistics(params: ShipmentStatisticsParams): Promise<Result<Record<string, any>>> {
    return request.get('/shipment/statistics', params)
  },

  // 导出出货单
  exportShipments(params: Record<string, any>): Promise<Result<string>> {
    return request.post('/shipment/export', params)
  },

  // 获取可用的配送方式
  getShippingMethods(): Promise<Result<Record<string, any>[]>> {
    return request.get('/shipment/shipping-methods')
  },

  // 计算运费
  calculateShippingFee(params: {
    address: string
    weight: number
    shippingMethod: string
  }): Promise<Result<{ fee: number; estimatedDays: number }>> {
    return request.post('/shipment/calculate-fee', params)
  },

  // 批量更新状态
  batchUpdateStatus(ids: number[], status: string): Promise<Result<void>> {
    return request.post('/shipment/batch-update-status', { ids, status })
  }
} 