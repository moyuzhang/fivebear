<template>
  <a-card title="IP池状态监控" :loading="loading">
    <template #extra>
      <a-button type="primary" :loading="loading" @click="fetchIPPoolStatus">
        刷新
      </a-button>
    </template>
    <a-table
      :dataSource="ipPoolData"
      :columns="columns"
      :pagination="false"
      rowKey="ip"
    >
      <template #bodyCell="{ column, text }">
        <template v-if="column.key === 'status'">
          <a-tag :color="text === 'active' ? 'green' : 'red'">
            {{ text === 'active' ? '活跃' : '不活跃' }}
          </a-tag>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import axios from 'axios';

interface IPPoolInfo {
  ip: string;
  status: 'active' | 'inactive';
  lastUsed: string;
  requestCount: number;
  responseTime: number;
}

export default defineComponent({
  name: 'IPPoolStatus',
  setup() {
    const loading = ref(false);
    const ipPoolData = ref<IPPoolInfo[]>([]);
    let refreshInterval: number;

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
        loading.value = true;
        const response = await axios.get('/api/ip-pool/status');
        ipPoolData.value = response.data;
      } catch (error) {
        message.error('获取IP池状态失败');
        console.error('Error fetching IP pool status:', error);
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchIPPoolStatus();
      // 每60秒自动刷新一次
      refreshInterval = setInterval(fetchIPPoolStatus, 60000);
    });

    onUnmounted(() => {
      if (refreshInterval) {
        clearInterval(refreshInterval);
      }
    });

    return {
      loading,
      ipPoolData,
      columns,
      fetchIPPoolStatus,
    };
  },
});
</script>

<style scoped>
.ant-card {
  margin: 24px;
}
</style> 