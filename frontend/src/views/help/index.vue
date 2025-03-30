<template>
  <div class="help-container">
    <el-card class="help-card">
      <template #header>
        <div class="card-header">
          <span>使用帮助</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="开始使用" name="getting-started">
          <div class="help-content">
            <h2>欢迎使用 FiveBear 系统</h2>
            <p>FiveBear 是一个功能强大的 MQTT 连接管理系统，帮助您轻松管理和监控 MQTT 客户端连接。</p>
            
            <h3>快速入门</h3>
            <ol>
              <li>
                <strong>登录系统</strong>
                <p>使用管理员提供的账号密码登录系统。</p>
              </li>
              <li>
                <strong>仪表盘</strong>
                <p>登录后您将看到仪表盘页面，展示系统概览及常用功能入口。</p>
              </li>
              <li>
                <strong>MQTT 连接管理</strong>
                <p>在左侧菜单选择"MQTT 连接管理"，查看并管理所有 MQTT 客户端连接。</p>
              </li>
            </ol>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="MQTT 连接管理" name="mqtt-management">
          <div class="help-content">
            <h2>MQTT 连接管理</h2>
            <p>本系统提供全面的 MQTT 连接管理功能，包括查看连接状态、查看详情、断开连接等操作。</p>
            
            <h3>基本功能</h3>
            <ul>
              <li>
                <strong>查看连接列表</strong>
                <p>系统会显示所有 MQTT 客户端连接，包括客户端 ID、状态、连接时间和 IP 地址等信息。</p>
              </li>
              <li>
                <strong>刷新连接列表</strong>
                <p>点击顶部的"刷新"按钮可以获取最新的连接状态。</p>
              </li>
              <li>
                <strong>断开连接</strong>
                <p>点击连接行中的"断开连接"按钮可以强制断开指定客户端的连接。</p>
              </li>
              <li>
                <strong>查看详情</strong>
                <p>点击"查看详情"可以查看客户端的详细信息，包括订阅的主题等。</p>
              </li>
            </ul>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="系统设置" name="system-settings">
          <div class="help-content">
            <h2>系统设置指南</h2>
            <p>系统设置允许管理员配置系统的各种参数，包括基础设置、MQTT 设置和系统安全等。</p>
            
            <h3>设置分类</h3>
            <ul>
              <li>
                <strong>基础设置</strong>
                <p>配置系统名称、描述和 Logo 等基本信息。</p>
              </li>
              <li>
                <strong>MQTT 设置</strong>
                <p>配置 MQTT 服务器地址、端口、认证信息以及连接参数。</p>
              </li>
              <li>
                <strong>系统安全</strong>
                <p>设置登录尝试限制、会话超时时间、密码强度要求和 IP 白名单等安全参数。</p>
              </li>
            </ul>
            
            <div class="note">
              <strong>注意：</strong> 修改系统设置需要管理员权限，部分设置修改后可能需要重启系统才能生效。
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="常见问题" name="faq">
          <div class="help-content">
            <h2>常见问题解答</h2>
            
            <div class="faq-item">
              <h3>1. 如何修改个人信息？</h3>
              <p>点击顶部导航栏右侧的用户名，选择"个人设置"，即可修改个人信息和密码。</p>
            </div>
            
            <div class="faq-item">
              <h3>2. 无法连接到 MQTT 服务器怎么办？</h3>
              <p>请检查 MQTT 服务器的地址、端口是否正确，以及账号密码是否有效。如果问题持续，请联系系统管理员。</p>
            </div>
            
            <div class="faq-item">
              <h3>3. 为什么无法看到某些客户端连接？</h3>
              <p>这可能是因为您没有查看这些客户端的权限，或者这些客户端已经断开连接。请联系管理员获取更多帮助。</p>
            </div>
            
            <div class="faq-item">
              <h3>4. 如何批量管理客户端连接？</h3>
              <p>当前版本暂不支持批量操作功能，我们计划在未来版本中添加此功能。</p>
            </div>
            
            <div class="faq-item">
              <h3>5. 忘记密码怎么办？</h3>
              <p>请联系系统管理员重置您的密码。</p>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="联系我们" name="contact">
          <div class="help-content">
            <h2>联系我们</h2>
            <p>如果您在使用过程中遇到任何问题，或者有任何建议和反馈，欢迎通过以下方式联系我们：</p>
            
            <div class="contact-info">
              <p><strong>技术支持邮箱：</strong> support@fivebear.com</p>
              <p><strong>官方网站：</strong> <a href="https://www.fivebear.com" target="_blank">www.fivebear.com</a></p>
              <p><strong>服务热线：</strong> 400-123-4567</p>
              <p><strong>工作时间：</strong> 周一至周五 9:00-18:00</p>
            </div>
            
            <div class="version-info">
              <p><strong>当前版本：</strong> v1.0.0</p>
              <p><strong>发布日期：</strong> 2023-05-01</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('getting-started')
</script>

<style scoped>
.help-container {
  padding: 20px;
}

.help-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.help-content {
  padding: 20px 10px;
  line-height: 1.6;
}

h2 {
  font-size: 24px;
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

h3 {
  font-size: 18px;
  margin-top: 25px;
  margin-bottom: 15px;
  color: #303133;
}

p {
  margin: 10px 0;
  color: #606266;
}

ol, ul {
  padding-left: 20px;
  margin: 15px 0;
}

li {
  margin-bottom: 10px;
}

li strong {
  color: #303133;
}

.note {
  background-color: #f8f8f8;
  border-left: 4px solid #E6A23C;
  padding: 12px 15px;
  margin: 20px 0;
  border-radius: 4px;
}

.faq-item {
  margin-bottom: 25px;
}

.faq-item h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #409EFF;
}

.contact-info {
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
}

.contact-info p {
  margin: 10px 0;
}

.version-info {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  color: #909399;
  font-size: 14px;
}

a {
  color: #409EFF;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
</style> 