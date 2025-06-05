<template>
  <Layout>
    <div class="system-settings">
    <el-card>
      <template #header>
        <div class="page-header">
          <div class="header-title">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="card" class="settings-tabs">
        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <div class="tab-content">
            <el-form :model="basicSettings" label-width="120px">
              <el-form-item label="系统名称">
                <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
              </el-form-item>
              
              <el-form-item label="系统描述">
                <el-input 
                  v-model="basicSettings.systemDescription" 
                  type="textarea" 
                  :rows="3"
                  placeholder="请输入系统描述"
                />
              </el-form-item>
              
              <el-form-item label="系统Logo">
                <el-upload
                  :before-upload="beforeLogoUpload"
                  :show-file-list="false"
                  action=""
                  accept="image/*"
                >
                  <el-button type="primary" :icon="Plus">上传Logo</el-button>
                  <template #tip>
                    <div class="el-upload__tip">
                      只能上传jpg/png文件，且不超过2MB
                    </div>
                  </template>
                </el-upload>
              </el-form-item>
              
              <el-form-item label="联系邮箱">
                <el-input v-model="basicSettings.contactEmail" placeholder="请输入联系邮箱" />
              </el-form-item>
              
              <el-form-item label="联系电话">
                <el-input v-model="basicSettings.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
              
              <el-form-item label="版权信息">
                <el-input v-model="basicSettings.copyright" placeholder="请输入版权信息" />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
                <el-button @click="resetBasicSettings">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <div class="tab-content">
            <el-form :model="securitySettings" label-width="120px">
              
              <!-- 密码策略 -->
              <div class="setting-section">
                <h4>密码策略</h4>
                <el-form-item label="包含大写字母">
                  <el-switch v-model="securitySettings.passwordPolicy.requireUppercase" />
                </el-form-item>
                
                <el-form-item label="包含小写字母">
                  <el-switch v-model="securitySettings.passwordPolicy.requireLowercase" />
                </el-form-item>
                
                <el-form-item label="包含数字">
                  <el-switch v-model="securitySettings.passwordPolicy.requireNumbers" />
                </el-form-item>
                
                <el-form-item label="包含特殊字符">
                  <el-switch v-model="securitySettings.passwordPolicy.requireSpecialChars" />
                </el-form-item>
                
                <el-form-item label="最小长度：">
                  <el-input-number 
                    v-model="securitySettings.passwordPolicy.minLength" 
                    :min="6" 
                    :max="32" 
                  />
                </el-form-item>
              </div>

              <!-- 登录限制 -->
              <div class="setting-section">
                <h4>登录限制</h4>
                <el-form-item label="最大登录失败次数：">
                  <el-input-number 
                    v-model="securitySettings.loginPolicy.maxFailAttempts" 
                    :min="3" 
                    :max="10" 
                  />
                </el-form-item>
                
                <el-form-item label="锁定时间（分钟）：">
                  <el-input-number 
                    v-model="securitySettings.loginPolicy.lockoutDuration" 
                    :min="5" 
                    :max="1440" 
                  />
                </el-form-item>
                
                <el-form-item label="Session超时（分钟）：">
                  <el-input-number 
                    v-model="securitySettings.loginPolicy.sessionTimeout" 
                    :min="30" 
                    :max="1440" 
                  />
                </el-form-item>
              </div>

              <!-- IP访问控制 -->
              <div class="setting-section">
                <h4>IP访问控制</h4>
                <el-form-item label="启用IP访问控制">
                  <el-switch v-model="securitySettings.ipControl.enabled" />
                </el-form-item>
              </div>
              
              <el-form-item>
                <el-button type="primary" @click="saveSecuritySettings">保存设置</el-button>
                <el-button @click="resetSecuritySettings">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 邮件设置 -->
        <el-tab-pane label="邮件设置" name="email">
          <div class="tab-content">
            <el-form :model="emailSettings" label-width="120px">
              <el-form-item label="启用邮件服务">
                <el-switch v-model="emailSettings.enabled" />
              </el-form-item>
              
              <template v-if="emailSettings.enabled">
                <div class="test-email-section">
                  <el-input
                    v-model="testEmail"
                    placeholder="输入测试邮箱地址"
                    style="width: 250px; margin-right: 10px;"
                  />
                  <el-button 
                    type="primary" 
                    :loading="sendingTest"
                    @click="sendTestEmail"
                  >
                    发送测试邮件
                  </el-button>
                </div>
              </template>
              
              <el-form-item>
                <el-button type="primary" @click="saveEmailSettings">保存设置</el-button>
                <el-button @click="resetEmailSettings">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 存储设置 -->
        <el-tab-pane label="存储设置" name="storage">
          <div class="tab-content">
            <el-form :model="storageSettings" label-width="120px">
              <el-form-item label="文件存储方式">
                <el-radio-group v-model="storageSettings.type">
                                  <el-radio value="local">本地存储</el-radio>
                <el-radio value="oss">阿里云OSS</el-radio>
                <el-radio value="cos">腾讯云COS</el-radio>
                <el-radio value="qiniu">七牛云</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="存储路径">
                <el-input v-model="storageSettings.localPath" placeholder="请输入存储路径" />
              </el-form-item>
              
              <el-form-item label="文件大小限制">
                <el-input-number 
                  v-model="storageSettings.maxFileSize" 
                  :min="1" 
                  :max="100"
                />
                <span style="margin-left: 8px;">MB</span>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveStorageSettings">保存设置</el-button>
                <el-button @click="resetStorageSettings">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { Setting, Plus } from '@element-plus/icons-vue'

const activeTab = ref('basic')
const sendingTest = ref(false)
const testEmail = ref('')

// 基础设置
const basicSettings = reactive({
  systemName: 'FiveBear企业管理系统',
  systemDescription: '一个强大的企业级管理系统，提供用户管理、权限控制、数据分析等功能。',
  logoUrl: '',
  contactEmail: 'admin@fivebear.com',
  contactPhone: '400-123-4567',
  copyright: '© 2024 FiveBear Technology. All rights reserved.'
})

// 安全设置
const securitySettings = reactive({
  passwordPolicy: {
    requireUppercase: true,
    requireLowercase: true,
    requireNumbers: true,
    requireSpecialChars: false,
    minLength: 8
  },
  loginPolicy: {
    maxFailAttempts: 5,
    lockoutDuration: 30,
    sessionTimeout: 120
  },
  ipControl: {
    enabled: false,
    mode: 'whitelist',
    ipList: ''
  }
})

// 邮件设置
const emailSettings = reactive({
  enabled: false,
  smtpHost: '',
  smtpPort: 587,
  encryption: 'tls',
  fromEmail: '',
  password: '',
  fromName: 'FiveBear系统'
})

// 存储设置
const storageSettings = reactive({
  type: 'local',
  localPath: '/uploads',
  accessKey: '',
  secretKey: '',
  bucket: '',
  domain: '',
  maxFileSize: 10
})

// Logo上传前验证
const beforeLogoUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 发送测试邮件
const sendTestEmail = async () => {
  if (!testEmail.value) {
    ElMessage.warning('请输入测试邮箱地址')
    return
  }

  sendingTest.value = true
  try {
    // TODO: 调用API发送测试邮件
    await new Promise(resolve => setTimeout(resolve, 2000)) // 模拟延迟
    ElMessage.success('测试邮件发送成功，请检查邮箱')
  } catch (error) {
    ElMessage.error('测试邮件发送失败')
  } finally {
    sendingTest.value = false
  }
}

// 保存基础设置
const saveBasicSettings = async () => {
  try {
    // TODO: 调用API保存基础设置
    ElMessage.success('基础设置保存成功')
  } catch (error) {
    ElMessage.error('基础设置保存失败')
  }
}

// 重置基础设置
const resetBasicSettings = () => {
  Object.assign(basicSettings, {
    systemName: 'FiveBear企业管理系统',
    systemDescription: '一个强大的企业级管理系统，提供用户管理、权限控制、数据分析等功能。',
    logoUrl: '',
    contactEmail: 'admin@fivebear.com',
    contactPhone: '400-123-4567',
    copyright: '© 2024 FiveBear Technology. All rights reserved.'
  })
}

// 保存安全设置
const saveSecuritySettings = async () => {
  try {
    // TODO: 调用API保存安全设置
    ElMessage.success('安全设置保存成功')
  } catch (error) {
    ElMessage.error('安全设置保存失败')
  }
}

// 重置安全设置
const resetSecuritySettings = () => {
  Object.assign(securitySettings, {
    passwordPolicy: {
      requireUppercase: true,
      requireLowercase: true,
      requireNumbers: true,
      requireSpecialChars: false,
      minLength: 8
    },
    loginPolicy: {
      maxFailAttempts: 5,
      lockoutDuration: 30,
      sessionTimeout: 120
    },
    ipControl: {
      enabled: false,
      mode: 'whitelist',
      ipList: ''
    }
  })
}

// 保存邮件设置
const saveEmailSettings = async () => {
  try {
    // TODO: 调用API保存邮件设置
    ElMessage.success('邮件设置保存成功')
  } catch (error) {
    ElMessage.error('邮件设置保存失败')
  }
}

// 重置邮件设置
const resetEmailSettings = () => {
  Object.assign(emailSettings, {
    enabled: false,
    smtpHost: '',
    smtpPort: 587,
    encryption: 'tls',
    fromEmail: '',
    password: '',
    fromName: 'FiveBear系统'
  })
}

// 保存存储设置
const saveStorageSettings = async () => {
  try {
    // TODO: 调用API保存存储设置
    ElMessage.success('存储设置保存成功')
  } catch (error) {
    ElMessage.error('存储设置保存失败')
  }
}

// 重置存储设置
const resetStorageSettings = () => {
  Object.assign(storageSettings, {
    type: 'local',
    localPath: '/uploads',
    accessKey: '',
    secretKey: '',
    bucket: '',
    domain: '',
    maxFileSize: 10
  })
}
</script>

<style scoped>
.system-settings {
  padding: 0;
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

.settings-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 20px 0;
}

.setting-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 6px;
}

.setting-section h4 {
  margin: 0 0 20px 0;
  color: #606266;
  font-size: 16px;
  font-weight: 600;
}

.test-email-section {
  margin: 20px 0;
  padding: 15px;
  background: #f0f9ff;
  border: 1px solid #409eff;
  border-radius: 6px;
  display: flex;
  align-items: center;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-input-number {
  width: 150px;
}

.el-radio-group .el-radio {
  margin-right: 20px;
  margin-bottom: 10px;
}

:deep(.el-tabs__header) {
  margin-bottom: 0;
}

:deep(.el-tabs__content) {
  padding-top: 0;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style> 