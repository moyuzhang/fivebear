import React, { useEffect, useState } from 'react';
import { Card, Table, Tag, Space, Button, message } from 'antd';
import axios from 'axios';

interface IPPoolInfo {
  ip: string;
  status: 'active' | 'inactive';
  lastUsed: string;
  requestCount: number;
  responseTime: number;
}

const IPPoolStatus: React.FC = () => {
  const [ipPoolData, setIpPoolData] = useState<IPPoolInfo[]>([]);
  const [loading, setLoading] = useState(false);

  const columns = [
    {
      title: 'IP地址',
      dataIndex: 'ip',
      key: 'ip',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'active' ? 'green' : 'red'}>
          {status === 'active' ? '活跃' : '不活跃'}
        </Tag>
      ),
    },
    {
      title: '最后使用时间',
      dataIndex: 'lastUsed',
      key: 'lastUsed',
    },
    {
      title: '请求次数',
      dataIndex: 'requestCount',
      key: 'requestCount',
    },
    {
      title: '平均响应时间(ms)',
      dataIndex: 'responseTime',
      key: 'responseTime',
    },
  ];

  const fetchIPPoolStatus = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/ip-pool/status');
      setIpPoolData(response.data);
    } catch (error) {
      message.error('获取IP池状态失败');
      console.error('Error fetching IP pool status:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIPPoolStatus();
    // 每60秒自动刷新一次
    const interval = setInterval(fetchIPPoolStatus, 60000);
    return () => clearInterval(interval);
  }, []);

  return (
    <Card 
      title="IP池状态监控" 
      extra={
        <Button type="primary" onClick={fetchIPPoolStatus} loading={loading}>
          刷新
        </Button>
      }
    >
      <Table 
        dataSource={ipPoolData} 
        columns={columns} 
        rowKey="ip"
        loading={loading}
        pagination={false}
      />
    </Card>
  );
};

export default IPPoolStatus; 