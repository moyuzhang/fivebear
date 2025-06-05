<template>
  <Layout>
  
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="page-header">
          <div class="header-title">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="showAddDialog = true">
              <el-icon><Plus /></el-icon>
              新增用户
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              @keyup.enter="handleSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select v-model="searchForm.status" placeholder="用户状态" clearable>
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="searchForm.roleId" placeholder="用户角色" clearable>
              <el-option 
                v-for="role in roles" 
                :key="role.id" 
                :label="role.name" 
                :value="role.id" 
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 用户表格 -->
      <div class="table-section">
        <el-table
          :data="userList"
          v-loading="loading"
          stripe
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column prop="roleName" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.roleId)">
                {{ row.roleName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastLoginTime" label="最后登录" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.lastLoginTime) }}
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
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                :type="row.status === 1 ? 'warning' : 'success'" 
                link 
                @click="handleToggleStatus(row)"
              >
                <el-icon><Switch /></el-icon>
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editMode ? '编辑用户' : '新增用户'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="userForm.username" 
                placeholder="请输入用户名"
                :disabled="editMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleId">
              <el-select v-model="userForm.roleId" placeholder="请选择角色" style="width: 100%">
                <el-option 
                  v-for="role in roles" 
                  :key="role.id" 
                  :label="role.name" 
                  :value="role.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="!editMode">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="userForm.password" 
                type="password" 
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="userForm.confirmPassword" 
                type="password" 
                placeholder="请确认密码"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="userForm.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大会话数" prop="maxConcurrentSessions">
              <el-input-number 
                v-model="userForm.maxConcurrentSessions" 
                :min="1" 
                :max="10" 
                placeholder="最大同时登录数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import {
  User,
  Plus,
  Search,
  Refresh,
  Edit,
  Delete,
  Switch
} from '@element-plus/icons-vue'

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const editMode = ref(false)
const userFormRef = ref()

// 搜索表单
const searchForm = reactive({
  username: '',
  status: null,
  roleId: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 用户列表
const userList = ref([])
const selectedUsers = ref([])

// 角色列表
const roles = ref([
  { id: 1, name: '管理员' },
  { id: 2, name: '普通用户' },
  { id: 3, name: '访客' }
])

// 用户表单
const userForm = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  roleId: null,
  status: 1,
  maxConcurrentSessions: 1
})

// 表单验证规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在 6 到 32 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取角色标签类型
const getRoleTagType = (roleId: number) => {
  switch (roleId) {
    case 1: return 'danger'  // 管理员
    case 2: return 'primary' // 普通用户
    case 3: return 'info'    // 访客
    default: return ''
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取用户列表
    // 模拟数据
    const mockData = [
      {
        id: 1,
        username: 'admin',
        nickname: '系统管理员',
        email: 'admin@fivebear.com',
        roleId: 1,
        roleName: '管理员',
        status: 1,
        lastLoginTime: '2024-12-05 12:00:00',
        createTime: '2024-12-01 10:00:00'
      },
      {
        id: 2,
        username: 'user1',
        nickname: '普通用户1',
        email: 'user1@fivebear.com',
        roleId: 2,
        roleName: '普通用户',
        status: 1,
        lastLoginTime: '2024-12-05 11:30:00',
        createTime: '2024-12-02 14:30:00'
      }
    ]
    
    userList.value = mockData
    pagination.total = mockData.length
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    status: null,
    roleId: null
  })
  handleSearch()
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedUsers.value = selection
}

// 处理编辑
const handleEdit = (user: any) => {
  editMode.value = true
  Object.assign(userForm, {
    ...user,
    password: '',
    confirmPassword: ''
  })
  showAddDialog.value = true
}

// 处理状态切换
const handleToggleStatus = async (user: any) => {
  try {
    await ElMessageBox.confirm(
      `确认${user.status === 1 ? '禁用' : '启用'}用户 ${user.nickname} 吗？`,
      '状态变更确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API切换用户状态
    user.status = user.status === 1 ? 0 : 1
    ElMessage.success(`用户状态已${user.status === 1 ? '启用' : '禁用'}`)
  } catch {
    // 用户取消
  }
}

// 处理删除
const handleDelete = async (user: any) => {
  try {
    await ElMessageBox.confirm(
      `确认删除用户 ${user.nickname} 吗？此操作不可逆！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API删除用户
    ElMessage.success('用户删除成功')
    loadUserList()
  } catch {
    // 用户取消
  }
}

// 处理提交
const handleSubmit = async () => {
  try {
    await userFormRef.value.validate()
    
    submitting.value = true
    
    // TODO: 调用API保存用户
    if (editMode.value) {
      ElMessage.success('用户更新成功')
    } else {
      ElMessage.success('用户创建成功')
    }
    
    showAddDialog.value = false
    loadUserList()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  editMode.value = false
  Object.assign(userForm, {
    id: null,
    username: '',
    nickname: '',
    email: '',
    password: '',
    confirmPassword: '',
    roleId: null,
    status: 1,
    maxConcurrentSessions: 1
  })
  userFormRef.value?.resetFields()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadUserList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadUserList()
}

// 初始化
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-management {
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