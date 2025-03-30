<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import type { IpPool } from '@/types/ipPool';
import { addIp } from '@/api/ipPool';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const formRef = ref<FormInstance>();

// 表单数据
const form = ref<Partial<IpPool>>({
  ip: '',
  port: undefined,
  username: '',
  password: '',
  location: '',
  status: 'active'
});

// 表单验证规则
const rules = {
  ip: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: '请输入正确的IP地址格式', trigger: 'blur' }
  ],
  port: [
    { required: true, message: '请输入端口号', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口号范围为1-65535', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// 判断是否为编辑模式
const isEdit = route.params.id !== undefined;

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    
    if (isEdit) {
      // 由于后端API不支持更新单个IP，我们暂时只支持添加
      ElMessage.warning('暂不支持更新IP，请先删除后重新添加');
    } else {
      await addIp({
        ip: form.value.ip || '',
        port: form.value.port || 0
      });
      ElMessage.success('添加成功');
    }
    router.push('/ip-pool/list');
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message);
    } else {
      ElMessage.error(isEdit ? '更新失败' : '添加失败');
    }
  } finally {
    loading.value = false;
  }
};

// 返回列表页
const handleCancel = () => {
  router.push('/ip-pool');
};

// 如果是编辑模式，获取IP详情
onMounted(async () => {
  if (isEdit) {
    loading.value = true;
    try {
      const response = await fetch(`/api/ip-pool/${route.params.id}`);
      const data = await response.json();
      form.value = data;
    } catch (error) {
      ElMessage.error('获取IP详情失败');
      router.push('/ip-pool');
    } finally {
      loading.value = false;
    }
  }
});
</script>

<template>
  <div class="ip-pool-form">
    <div class="header">
      <h2>{{ isEdit ? '编辑IP' : '添加IP' }}</h2>
    </div>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        :disabled="loading"
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

        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名（可选）" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（可选）"
            show-password
          />
        </el-form-item>

        <el-form-item label="地区" prop="location">
          <el-input v-model="form.location" placeholder="请输入地区（可选）" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="活跃" value="active" />
            <el-option label="不活跃" value="inactive" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.ip-pool-form {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}
</style> 