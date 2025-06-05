<template>
  <Layout>
    <div class="role-management">
      <el-card>
        <template #header>
          <div class="page-header">
            <div class="header-title">
              <el-icon>
                <UserFilled />
              </el-icon>
              <span>角色管理</span>
            </div>
            <div class="header-actions">
              <el-button type="primary" @click="showAddDialog = true">
                <el-icon>
                  <Plus />
                </el-icon>
                新增角色
              </el-button>
            </div>
          </div>
        </template>

        <!-- 搜索筛选 -->
        <div class="search-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input v-model="searchForm.name" placeholder="请输入角色名称" @keyup.enter="handleSearch" clearable>
                <template #prefix>
                  <el-icon>
                    <Search />
                  </el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="6">
              <el-select v-model="searchForm.status" placeholder="角色状态" clearable>
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-button type="primary" @click="handleSearch">
                <el-icon>
                  <Search />
                </el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon>
                  <Refresh />
                </el-icon>
                重置
              </el-button>
            </el-col>
          </el-row>
        </div>

        <!-- 角色表格 -->
        <div class="table-section">
          <el-table :data="roleList" v-loading="loading" stripe border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="角色名称" min-width="120" />
            <el-table-column prop="description" label="角色描述" min-width="200" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)">
                  <el-icon>
                    <Edit />
                  </el-icon>
                  编辑
                </el-button>
                <el-button type="warning" link @click="handlePermissions(row)">
                  <el-icon>
                    <Key />
                  </el-icon>
                  权限
                </el-button>
                <el-button type="danger" link @click="handleDelete(row)">
                  <el-icon>
                    <Delete />
                  </el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-section">
            <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
              :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange" @current-change="handleCurrentChange" />
          </div>
        </div>
      </el-card>

      <!-- 新增/编辑角色对话框 -->
      <el-dialog v-model="showAddDialog" :title="editMode ? '编辑角色' : '新增角色'" width="500px" @close="resetForm">
        <el-form ref="roleFormRef" :model="roleForm" :rules="roleFormRules" label-width="100px">
          <el-form-item label="角色名称" prop="name">
            <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="角色描述" prop="description">
            <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="roleForm.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import {
  UserFilled,
  Plus,
  Search,
  Refresh,
  Edit,
  Delete,
  Key
} from '@element-plus/icons-vue'

// 角色接口定义
interface RoleItem {
  id: number
  name: string
  description: string
  status: number
  createTime: string
}

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const editMode = ref(false)
const roleFormRef = ref()

// 搜索表单
const searchForm = reactive({
  name: '',
  status: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 角色列表
const roleList = ref<RoleItem[]>([])

// 角色表单
const roleForm = reactive({
  id: null,
  name: '',
  description: '',
  status: 1
})

// 表单验证规则
const roleFormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入角色描述', trigger: 'blur' }
  ]
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 加载角色列表
const loadRoleList = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取角色列表
    // 模拟数据
    const mockData: RoleItem[] = [
      {
        id: 1,
        name: '管理员',
        description: '系统管理员，拥有所有权限',
        status: 1,
        createTime: '2024-12-01 10:00:00'
      },
      {
        id: 2,
        name: '普通用户',
        description: '普通用户角色',
        status: 1,
        createTime: '2024-12-01 10:30:00'
      },
      {
        id: 3,
        name: '访客',
        description: '访客角色，只有查看权限',
        status: 1,
        createTime: '2024-12-01 11:00:00'
      }
    ]

    roleList.value = mockData
    pagination.total = mockData.length
  } catch (error) {
    ElMessage.error('加载角色列表失败')
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadRoleList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: null
  })
  handleSearch()
}

// 处理编辑
const handleEdit = (role: RoleItem) => {
  editMode.value = true
  Object.assign(roleForm, role)
  showAddDialog.value = true
}

// 处理权限设置
const handlePermissions = (role: RoleItem) => {
  ElMessage.info(`权限设置功能开发中... 角色：${role.name}`)
}

// 处理删除
const handleDelete = async (role: RoleItem) => {
  try {
    await ElMessageBox.confirm(
      `确认删除角色 ${role.name} 吗？此操作不可逆！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API删除角色
    ElMessage.success('角色删除成功')
    loadRoleList()
  } catch {
    // 用户取消
  }
}

// 处理提交
const handleSubmit = async () => {
  try {
    await roleFormRef.value.validate()

    submitting.value = true

    // TODO: 调用API保存角色
    if (editMode.value) {
      ElMessage.success('角色更新成功')
    } else {
      ElMessage.success('角色创建成功')
    }

    showAddDialog.value = false
    loadRoleList()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  editMode.value = false
  Object.assign(roleForm, {
    id: null,
    name: '',
    description: '',
    status: 1
  })
  roleFormRef.value?.resetFields()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadRoleList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadRoleList()
}

// 初始化
onMounted(() => {
  loadRoleList()
})
</script>

<style scoped>
.role-management {
  padding: 120px 20px 20px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.header-title .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.table-section {
  margin-top: 20px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

:deep(.el-dialog__body) {
  padding: 20px;
}
</style> 