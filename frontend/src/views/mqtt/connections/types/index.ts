export interface Connection {
  id: string
  name: string
  host: string
  port: number
  username: string
  password: string
  clientId: string
  autoReconnect: boolean
  status: 'connected' | 'disconnected'
  createdAt: string
  updatedAt: string
}

export interface ConnectionForm {
  name: string
  host: string
  port: number
  username: string
  password: string
  clientId: string
  autoReconnect: boolean
}

export interface ConnectionResponse {
  code: number
  message: string
  data: Connection | Connection[]
}

export interface ConnectionQuery {
  page: number
  pageSize: number
  keyword?: string
  status?: 'connected' | 'disconnected'
} 