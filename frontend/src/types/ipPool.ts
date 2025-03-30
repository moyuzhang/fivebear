export interface IpPool {
  id: number;
  ip: string;
  port: number;
  username?: string;
  password?: string;
  location?: string;
  status: 'active' | 'inactive';
  lastChecked?: string;
  createdAt: string;
  updatedAt: string;
}

export interface IpPoolQuery {
  page: number;
  pageSize: number;
  status?: string;
  location?: string;
}

export interface IpPoolResponse {
  total: number;
  items: IpPool[];
}

export interface IpInfo {
  ip: string;
  port: number;
  speed?: number;
  isValid: boolean;
  lastChecked: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
}

export interface IpPoolStatus {
  totalCount: number;
  validCount: number;
  averageSpeed: number;
  lastUpdateTime: string;
}

export interface IpPoolSettings {
  autoFetch: boolean;
  fetchInterval: number; // 分钟
  maxIpCount: number;
  minSpeed: number; // ms
  testUrl: string;
  urls?: Array<{
    id: string;
    url: string;
    description: string;
  }>;
  apiKey?: string;
  apiInterval?: number;
  apiConcurrency?: number;
  testTimeout?: number;
}

export interface IpPoolInfo {
  status: IpPoolStatus;
  settings: IpPoolSettings;
  ipList: IpInfo[];
  total: number;
} 