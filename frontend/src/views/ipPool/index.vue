<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import type { IpInfo, IpPoolStatus, IpPoolSettings } from '@/types/ipPool';
import { 
  getIpList, 
  deleteIp, 
  verifyIp, 
  addIp, 
  fetchProxies, 
  getPoolStatus,
  getPoolSettings,
  updatePoolSettings,
  getIpPoolInfo
} from '@/api/ipPool';
import { formatTime } from '@/utils/time';

const loading = ref(false);
const addLoading = ref(false);
const verifyLoading = ref<string>(''); // 存储正在验证的IP
const ipList = ref<IpInfo[]>([]);
const total = ref(0);
const poolStatus = ref<IpPoolStatus | null>(null);
const dialogVisible = ref(false);

// IP池设置
const settings = ref<IpPoolSettings>({
  autoFetch: true,
  fetchInterval: 30,
  maxIpCount: 100,
  minSpeed: 1000,
  testUrl: 'https://www.baidu.com'
});

const queryParams = ref({
  page: 1,
  pageSize: 10
});

// IP地址验证规则
const ipPattern = /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;

const formRef = ref<FormInstance>();
const form = ref({
  ip: '',
  port: 1
});

const rules: FormRules = {
  ip: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { pattern: ipPattern, message: '请输入有效的IP地址', trigger: 'blur' }
  ],
  port: [
    { required: true, message: '请输入端口号', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口号范围为1-65535', trigger: 'blur' }
  ]
};

// 获取IP池综合信息
const fetchIpPoolInfo = async () => {
  loading.value = true;
  try {
    const data = await getIpPoolInfo();
    ipList.value = data.ipList;
    total.value = data.total;
    poolStatus.value = data.status;
    settings.value = data.settings;
  } catch (error: any) {
    ElMessage.error(error.message || '获取IP池信息失败');
  } finally {
    loading.value = false;
  }
};

// 删除IP
const handleDelete = async (row: IpInfo) => {
  try {
    await ElMessageBox.confirm(`确认删除IP ${row.ip}:${row.port}？`, '提示', {
      type: 'warning'
    });
    await deleteIp({ ip: row.ip, port: row.port });
    ElMessage.success('删除成功');
    await fetchIpPoolInfo();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};

// 验证IP
const handleVerify = async (row: IpInfo) => {
  const key = `${row.ip}:${row.port}`;
  verifyLoading.value = key;
  try {
    const result = await verifyIp({
      ip: row.ip,
      port: row.port,
      testUrl: settings.value.testUrl
    });
    ElMessage[result ? 'success' : 'warning'](`验证${result ? '成功' : '失败'}`);
    await fetchIpPoolInfo();
  } catch (error: any) {
    ElMessage.error(error.message || '验证失败');
  } finally {
    verifyLoading.value = '';
  }
};

// 添加IP
const handleAdd = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      addLoading.value = true;
      try {
        await addIp(form.value);
        ElMessage.success('添加成功');
        dialogVisible.value = false;
        await fetchIpPoolInfo();
      } catch (error: any) {
        ElMessage.error(error.message || '添加失败');
      } finally {
        addLoading.value = false;
      }
    }
  });
};

// 获取新代理
const handleFetchProxies = async () => {
  try {
    await fetchProxies();
    ElMessage.success('获取新代理成功');
    await fetchIpPoolInfo();
  } catch (error: any) {
    ElMessage.error(error.message || '获取新代理失败');
  }
};

// 保存设置
const handleSaveSettings = async () => {
  try {
    await updatePoolSettings(settings.value);
    ElMessage.success('设置保存成功');
  } catch (error: any) {
    ElMessage.error(error.message || '设置保存失败');
  }
};

// 打开添加对话框
const handleAddDialogOpen = () => {
  dialogVisible.value = true;
  form.value = {
    ip: '',
    port: 1
  };
};

// 关闭对话框时重置表单
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 分页
const handlePageChange = (page: number) => {
  queryParams.value.page = page;
  fetchIpPoolInfo();
};

const handleSizeChange = (size: number) => {
  queryParams.value.pageSize = size;
  queryParams.value.page = 1;
  fetchIpPoolInfo();
};

// 定时刷新
let refreshTimer: number;
const startAutoRefresh = () => {
  refreshTimer = window.setInterval(async () => {
    await fetchIpPoolInfo();
  }, 30000); // 每30秒刷新一次
};

onMounted(() => {
  fetchIpPoolInfo();
  startAutoRefresh();
});

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
  }
});
</script>

<template>
  <div class="ip-pool-container">
    <!-- 状态卡片 -->
    <el-row :gutter="20" class="status-cards">
      <el-col :span="6">
        <el-card>
          <template #header>总IP数</template>
          <div class="status-value">{{ poolStatus?.totalCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>有效IP数</template>
          <div class="status-value">{{ poolStatus?.validCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>平均响应时间</template>
          <div class="status-value">{{ poolStatus?.averageSpeed || 0 }}ms</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>最后更新时间</template>
          <div class="status-value">{{ formatTime(poolStatus?.lastUpdateTime) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- IP池设置 -->
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>IP池设置</span>
          <el-button type="primary" @click="handleSaveSettings">保存设置</el-button>
        </div>
      </template>
      <el-form :model="settings" label-width="120px">
        <el-form-item label="自动获取代理">
          <el-switch v-model="settings.autoFetch" />
        </el-form-item>
        <el-form-item label="获取间隔(分钟)">
          <el-input-number 
            v-model="settings.fetchInterval" 
            :min="1" 
            :max="1440"
            :disabled="!settings.autoFetch"
          />
        </el-form-item>
        <el-form-item label="最大IP数量">
          <el-input-number 
            v-model="settings.maxIpCount" 
            :min="1" 
            :max="1000"
          />
        </el-form-item>
        <el-form-item label="最小响应时间(ms)">
          <el-input-number 
            v-model="settings.minSpeed" 
            :min="100" 
            :max="10000"
            :step="100"
          />
        </el-form-item>
        <el-form-item label="测试URL">
          <el-input v-model="settings.testUrl" placeholder="请输入测试URL" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- IP列表 -->
    <el-card class="ip-list">
      <template #header>
        <div class="card-header">
          <span>IP列表</span>
          <div class="header-buttons">
            <el-button type="primary" @click="handleAddDialogOpen">添加IP</el-button>
            <el-button type="success" @click="handleFetchProxies">获取代理</el-button>
            <el-button @click="fetchIpPoolInfo">刷新</el-button>
          </div>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="ipList" border style="width: 100%">
        <el-table-column prop="ip" label="IP地址" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="speed" label="响应时间(ms)" width="120">
          <template #default="{ row }">
            <span :class="{ 'text-success': row.speed < settings.minSpeed }">
              {{ row.speed || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="isValid" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isValid ? 'success' : 'danger'">
              {{ row.isValid ? '有效' : '无效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastChecked" label="最后检查时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastChecked) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              :loading="verifyLoading === `${row.ip}:${row.port}`"
              @click="handleVerify(row)"
            >
              验证
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 添加IP对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加IP"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="form.ip" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number
            v-model="form.port"
            :min="1"
            :max="65535"
            placeholder="请输入端口号"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="addLoading"
            @click="handleAdd"
          >
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.ip-pool-container {
  padding: 20px;
}

.status-cards {
  margin-bottom: 20px;
}

.settings-card {
  margin-bottom: 20px;
}

.status-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.ip-list {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-success {
  color: #67C23A;
}
</style> 