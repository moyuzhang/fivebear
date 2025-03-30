<template>
  <div class="app-container">
    <!-- IP池状态卡片 -->
    <el-card class="status-card mb-20">
      <template #header>
        <div class="card-header">
          <span>IP池状态</span>
          <div class="header-actions">
            <el-button
              v-if="!ipPoolStatus"
              type="success"
              size="small"
              @click="handleStartIpPool"
            >
              启动
            </el-button>
            <el-button
              v-else
              type="danger"
              size="small"
              @click="handleStopIpPool"
            >
              停止
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleRefreshIpPool"
            >
              刷新
            </el-button>
          </div>
        </div>
      </template>
      <div class="status-info">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="status-item">
              <div class="status-label">IP池状态</div>
              <div class="status-value">
                <el-tag :type="ipPoolStatus ? 'success' : 'danger'">
                  {{ ipPoolStatus ? '运行中' : '已停止' }}
                </el-tag>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="status-item">
              <div class="status-label">总代理IP数量</div>
              <div class="status-value">{{ proxyCount }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="status-item">
              <div class="status-label">可用代理IP数量</div>
              <div class="status-value">
                <span class="valid-count">{{ validProxyCount }}</span> 
                <span class="count-percentage" v-if="proxyCount > 0">({{ Math.round(validProxyCount / proxyCount * 100) }}%)</span>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="status-item">
              <div class="status-label">平均响应时间</div>
              <div class="status-value">{{ avgResponseTime }}ms</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- IP池设置表单 -->
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <span>IP池设置</span>
          <div class="header-actions">
            <el-button
              type="primary"
              size="small"
              @click="handleSaveSettings"
            >
              保存设置
            </el-button>
          </div>
        </div>
      </template>
      <el-form
        ref="settingsFormRef"
        :model="ipPoolSettings"
        label-width="180px"
        :rules="{
          threadPoolSize: [{ required: true, message: '线程池大小不能为空', trigger: 'blur' }],
          schedulerPoolSize: [{ required: true, message: '调度池大小不能为空', trigger: 'blur' }],
          initialDelay: [{ required: true, message: '初始延迟不能为空', trigger: 'blur' }],
          period: [{ required: true, message: '周期不能为空', trigger: 'blur' }],
          expireTime: [{ required: true, message: '过期时间不能为空', trigger: 'blur' }],
          listKey: [{ required: true, message: 'List键不能为空', trigger: 'blur' }],
          setKey: [{ required: true, message: 'Set键不能为空', trigger: 'blur' }]
        }"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用IP池" prop="enabled">
              <el-switch v-model="ipPoolSettings.enabled" />
        </el-form-item>
            <el-form-item label="线程池大小" prop="threadPoolSize">
              <el-input-number v-model="ipPoolSettings.threadPoolSize" :min="1" :max="100" />
        </el-form-item>
            <el-form-item label="调度池大小" prop="schedulerPoolSize">
              <el-input-number v-model="ipPoolSettings.schedulerPoolSize" :min="1" :max="100" />
        </el-form-item>
            <el-form-item label="初始延迟(秒)" prop="initialDelay">
              <el-input-number v-model="ipPoolSettings.initialDelay" :min="0" :max="3600" />
        </el-form-item>
            <el-form-item label="周期(秒)" prop="period">
              <el-input-number v-model="ipPoolSettings.period" :min="1" :max="86400" />
            </el-form-item>
            <el-form-item label="IP过期时间(秒)" prop="expireTime">
              <el-input-number v-model="ipPoolSettings.expireTime" :min="1" :max="86400" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="List键名" prop="listKey">
              <el-input v-model="ipPoolSettings.listKey" />
            </el-form-item>
            <el-form-item label="Set键名" prop="setKey">
              <el-input v-model="ipPoolSettings.setKey" />
            </el-form-item>
            <el-form-item label="最大验证次数" prop="maxValidateCount">
              <el-input-number v-model="ipPoolSettings.maxValidateCount" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="最小可用率(%)" prop="minAvailableRate">
              <el-input-number v-model="ipPoolSettings.minAvailableRate" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="验证间隔(秒)" prop="validateInterval">
              <el-input-number v-model="ipPoolSettings.validateInterval" :min="1" :max="3600" />
            </el-form-item>
            <el-form-item label="超时时间(毫秒)" prop="timeout">
              <el-input-number v-model="ipPoolSettings.timeout" :min="100" :max="60000" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- URL管理 -->
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <span>代理URL管理</span>
          <div class="header-actions">
            <el-button
              type="primary"
              size="small"
              @click="handleAddUrl"
            >
              添加URL
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="urlList"
        border
        style="width: 100%"
        v-loading="tableLoading"
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />
        <el-table-column
          prop="url"
          label="URL"
          min-width="300"
        />
        <el-table-column
          prop="description"
          label="描述"
          min-width="200"
        />
        <el-table-column
          label="操作"
          width="150"
          align="center"
        >
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEditUrl(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteUrl(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- IP列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>代理IP列表</span>
          <div class="header-actions">
            <el-button
              type="primary"
              size="small"
              @click="handleGetIp"
            >
              获取新IP
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="fetchIpList"
            >
              刷新列表
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="handleAddProxyIp"
            >
              手动添加IP
            </el-button>
            <el-button
              type="info"
              size="small"
              :disabled="batchTesting || selectedProxies.length === 0"
              :loading="batchTesting"
              @click="handleBatchTestProxies"
            >
              {{ batchTesting ? '批量测试中' : '批量测试' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              :disabled="batchTesting"
              :loading="testingAll"
              @click="handleTestAllProxies"
            >
              {{ testingAll ? '正在测试全部...' : '测试全部' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              :disabled="batchTesting || removingDuplicates"
              :loading="removingDuplicates"
              @click="handleRemoveDuplicateIps"
            >
              {{ removingDuplicates ? '正在清理...' : '清理重复IP' }}
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="showIpManagementDialog"
            >
              IP管理
            </el-button>
          </div>
        </div>
      </template>
      <div class="mb-10 filter-row">
        <el-radio-group v-model="filterStatus" @change="handleFilterChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="valid">可用</el-radio-button>
          <el-radio-button label="invalid">不可用</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索IP地址"
          style="width: 200px; margin-left: 10px;"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-table
        :data="ipList"
        border
        style="width: 100%"
        v-loading="ipListLoading"
        @selection-change="handleSelectionChange"
        :row-class-name="getRowClassName"
        @sort-change="handleSortChange"
      >
        <el-table-column
          type="selection"
          width="55"
        />
        <el-table-column
          prop="ip"
          label="IP地址"
          min-width="150"
          sortable="custom"
        />
        <el-table-column
          prop="port"
          label="端口"
          width="100"
          sortable="custom"
        />
        <el-table-column
          prop="type"
          label="类型"
          width="100"
          sortable="custom"
        />
        <el-table-column
          prop="source"
          label="来源"
          min-width="150"
        />
        <el-table-column
          prop="lastValidTime"
          label="上次验证时间"
          min-width="180"
          sortable="custom"
        />
        <el-table-column
          label="有效性"
          width="120"
          align="center"
          sortable="custom"
          :sort-method="sortByValidity"
          prop="isValid"
        >
          <template #default="scope">
            <el-tag
              :type="scope.row.isValid ? 'success' : 'danger'"
              size="small"
            >
              {{ scope.row.isValid ? '有效' : '无效' }}
            </el-tag>
            <div v-if="scope.row.isValid && scope.row.responseTime" class="response-time">
              {{ scope.row.responseTime }}ms
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="150"
          align="center"
        >
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              :loading="testingProxyId === `${scope.row.ip}:${scope.row.port}`"
              @click="handleTestProxy(scope.row)"
            >
              {{ testingProxyId === `${scope.row.ip}:${scope.row.port}` ? '测试中' : '测试' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteProxy(scope.row)"
              :disabled="testingProxyId === `${scope.row.ip}:${scope.row.port}` || batchTesting"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- URL 对话框 -->
    <el-dialog
      :title="urlDialog.isEdit ? '编辑URL' : '添加URL'"
      v-model="urlDialog.visible"
      width="500px"
    >
      <el-form
        ref="urlFormRef"
        :model="urlDialog.form"
        label-width="80px"
        :rules="{
          url: [
            { required: true, message: 'URL不能为空', trigger: 'blur' },
            { type: 'url', message: 'URL格式不正确', trigger: 'blur' }
          ],
          description: [{ required: true, message: '描述不能为空', trigger: 'blur' }]
        }"
      >
        <el-form-item label="URL" prop="url">
          <el-input v-model="urlDialog.form.url" placeholder="请输入代理网站URL" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="urlDialog.form.description" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="urlDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveUrl">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- IP添加对话框 -->
    <el-dialog
      title="手动添加代理IP"
      v-model="proxyDialog.visible"
      width="500px"
    >
      <el-form
        ref="proxyFormRef"
        :model="proxyDialog.form"
        label-width="100px"
        :rules="{
          ip: [
            { required: true, message: 'IP不能为空', trigger: 'blur' },
            { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: 'IP格式不正确', trigger: 'blur' }
          ],
          port: [
            { required: true, message: '端口不能为空', trigger: 'blur' },
            { type: 'number', min: 1, max: 65535, message: '端口范围为1-65535', trigger: 'blur' }
          ],
          type: [{ required: true, message: '类型不能为空', trigger: 'change' }]
        }"
      >
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="proxyDialog.form.ip" placeholder="例如: 192.168.1.1" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="proxyDialog.form.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="proxyDialog.form.type" placeholder="请选择代理类型">
            <el-option label="HTTP" value="HTTP" />
            <el-option label="HTTPS" value="HTTPS" />
            <el-option label="SOCKS4" value="SOCKS4" />
            <el-option label="SOCKS5" value="SOCKS5" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-input v-model="proxyDialog.form.source" placeholder="例如: 手动添加" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="proxyDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProxyIp">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增：IP池详细状态仪表板 -->
    <el-card class="ip-dashboard-card">
      <template #header>
        <div class="card-header">
          <span>IP池仪表板</span>
          <div class="header-buttons">
            <el-button type="primary" @click="fetchDetailedStatus" :loading="dashboardLoading">刷新数据</el-button>
          </div>
        </div>
      </template>
      <div class="dashboard-content" v-loading="dashboardLoading">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="总代理IP数" :value="proxyCount">
              <template #suffix>
                <el-tooltip content="系统中的所有代理IP数量">
                  <el-icon><info-filled /></el-icon>
                </el-tooltip>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="有效代理数" :value="validProxyCount">
              <template #suffix>
                <el-tooltip content="测试可用的代理IP数量">
                  <el-icon><info-filled /></el-icon>
                </el-tooltip>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="平均响应时间" :value="avgResponseTime">
              <template #suffix>
                <span>毫秒</span>
                <el-tooltip content="所有有效代理的平均响应时间">
                  <el-icon><info-filled /></el-icon>
                </el-tooltip>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
        
        <div class="performance-chart">
          <div class="chart-header">
            <h3>响应时间分布</h3>
            <div class="chart-filters">
              <el-radio-group v-model="responseTimeFilter" @change="updateChartData" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="fast">快速 (<200ms)</el-radio-button>
                <el-radio-button label="medium">中等 (200-500ms)</el-radio-button>
                <el-radio-button label="slow">慢速 (>500ms)</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="chart-container" ref="chartContainer"></div>
        </div>
      </div>
    </el-card>

    <!-- 测试结果对话框 -->
    <el-dialog
      title="测试结果"
      v-model="testResultsDialog.visible"
      width="500px"
    >
      <div class="test-results-content">
        <div class="result-item">
          <span class="label">IP:</span>
          <span class="value">{{ testResultsData.ip }}</span>
        </div>
        <div class="result-item">
          <span class="label">端口:</span>
          <span class="value">{{ testResultsData.port }}</span>
        </div>
        <div class="result-item">
          <span class="label">类型:</span>
          <span class="value">{{ testResultsData.type }}</span>
        </div>
        <div class="result-item">
          <span class="label">有效性:</span>
          <span class="value">{{ testResultsData.isValid ? '有效' : '无效' }}</span>
        </div>
        <div class="result-item">
          <span class="label">响应时间:</span>
          <span class="value">{{ testResultsData.responseTime }}ms</span>
        </div>
        <div class="result-item">
          <span class="label">测试URL:</span>
          <span class="value">{{ testResultsData.testUrl }}</span>
        </div>
        <div class="result-item">
          <span class="label">测试时间:</span>
          <span class="value">{{ testResultsData.timestamp }}</span>
        </div>
        <div class="result-item">
          <span class="label">消息:</span>
          <span class="value">{{ testResultsData.message }}</span>
        </div>
        <div class="result-item">
          <span class="label">详情:</span>
          <pre class="details">{{ JSON.stringify(testResultsData.details, null, 2) }}</pre>
        </div>
      </div>
    </el-dialog>

    <!-- 批量测试结果对话框 -->
    <el-dialog
      title="批量测试结果"
      v-model="batchTestDialog.visible"
      width="800px"
    >
      <div class="batch-test-results-content">
        <div class="test-summary">
          <span class="summary-item">
            <span class="summary-label">总测试数:</span>
            <span class="summary-value">{{ batchTestResults.length }}</span>
          </span>
          <span class="summary-item">
            <span class="summary-label">可用数量:</span>
            <span class="summary-value success">{{ batchTestResults.filter(r => r.isValid).length }}</span>
          </span>
          <span class="summary-item">
            <span class="summary-label">不可用数量:</span>
            <span class="summary-value error">{{ batchTestResults.filter(r => !r.isValid).length }}</span>
          </span>
          <span class="summary-item" v-if="batchTestResults.filter(r => r.isValid).length > 0">
            <span class="summary-label">平均响应时间:</span>
            <span class="summary-value">{{ calculateAvgResponseTime() }}ms</span>
          </span>
        </div>
        
        <div class="filter-row">
          <el-select v-model="batchResultsFilter" placeholder="筛选状态" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="有效" value="valid" />
            <el-option label="无效" value="invalid" />
          </el-select>
          
          <el-select v-model="batchResultsSort" placeholder="排序方式" style="width: 160px;">
            <el-option label="默认排序" value="default" />
            <el-option label="响应时间 (升序)" value="response-asc" />
            <el-option label="响应时间 (降序)" value="response-desc" />
            <el-option label="IP地址 (升序)" value="ip-asc" />
            <el-option label="IP地址 (降序)" value="ip-desc" />
          </el-select>
          
          <el-input 
            v-model="batchResultsSearch" 
            placeholder="搜索IP" 
            clearable 
            style="width: 200px;"
          >
            <template #prefix>
              <el-icon class="el-input__icon"><Search /></el-icon>
            </template>
          </el-input>
          
          <el-tooltip 
            content="导出功能将只导出当前筛选后的结果" 
            placement="top"
          >
            <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        
        <el-table :data="filteredBatchResults" style="width: 100%" border stripe>
          <el-table-column prop="ip" label="IP" width="140" sortable />
          <el-table-column prop="port" label="端口" width="80" sortable />
          <el-table-column label="状态" width="80" sortable>
            <template #default="scope">
              <span class="status-tag" :class="scope.row.isValid ? 'success' : 'error'">
                {{ scope.row.isValid ? '有效' : '无效' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="响应时间/错误信息" sortable>
            <template #default="scope">
              <span v-if="scope.row.isValid">{{ scope.row.responseTime }}ms</span>
              <span v-else class="error-message">{{ scope.row.errorMessage }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="timestamp" label="测试时间" width="180" sortable />
        </el-table>
        
        <div v-if="filteredBatchResults.length === 0" class="empty-results">
          <el-empty description="没有匹配的测试结果" />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchTestDialog.visible = false">关闭</el-button>
          <el-button type="primary" @click="exportTestResults">
            导出结果 ({{ filteredBatchResults.length }}条)
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- IP管理对话框 -->
    <el-dialog
      title="IP管理"
      v-model="ipManagementDialog.visible"
      width="550px"
    >
      <div class="ip-management-content">
        <el-alert
          title="使用此功能可以清理某个特定IP地址的所有记录"
          type="info"
          :closable="false"
          class="mb-20"
        />
        
        <div class="ip-input-group">
          <el-input
            v-model="ipManagementDialog.targetIp"
            placeholder="输入要清理的IP地址，例如：8.219.97.248"
            class="ip-input"
          />
          <el-button
            type="danger"
            @click="handleDeleteIpByAddress(ipManagementDialog.targetIp)"
          >
            清理此IP
          </el-button>
          <el-button
            type="warning"
            @click="addToBlacklist(ipManagementDialog.targetIp)"
          >
            加入黑名单
          </el-button>
        </div>
        
        <div class="common-actions">
          <h4>常见问题处理</h4>
          <div class="action-buttons">
            <el-button @click="handleDeleteIpByAddress('8.219.97.248')">
              清理 8.219.97.248
            </el-button>
            <el-button @click="handleRemoveDuplicateIps">
              清理所有重复IP
            </el-button>
            <el-button @click="handleRefreshIpPool">
              重启IP池
            </el-button>
          </div>
        </div>
        
        <!-- 新增Redis导入功能 -->
        <div class="redis-import-section">
          <h4>Redis导入配置</h4>
          <div class="redis-form">
            <el-form :model="redisImportForm" label-width="100px" size="small">
              <el-form-item label="IP数量">
                <el-input-number v-model="redisImportForm.count" :min="1" :max="100" />
              </el-form-item>
              <el-form-item label="代理类型">
                <el-select v-model="redisImportForm.type" placeholder="请选择代理类型">
                  <el-option label="HTTP" value="HTTP" />
                  <el-option label="HTTPS" value="HTTPS" />
                  <el-option label="SOCKS4" value="SOCKS4" />
                  <el-option label="SOCKS5" value="SOCKS5" />
                </el-select>
              </el-form-item>
              <el-form-item label="来源标记">
                <el-input v-model="redisImportForm.source" placeholder="例如：手动导入" />
              </el-form-item>
              <el-form-item label="IP列表" class="ip-textarea-item">
                <el-input
                  type="textarea"
                  v-model="redisImportForm.ipList"
                  placeholder="每行一个IP，格式为IP:端口，例如：127.0.0.1:8080"
                  :rows="5"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleImportToRedis" :loading="importing">
                  {{ importing ? '导入中...' : '导入到Redis' }}
                </el-button>
                <el-button type="success" @click="handleAddRandomIps">
                  添加示例IP
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <div class="blacklist-section">
          <h4>IP黑名单</h4>
          <div class="blacklist-settings">
            <el-switch
              v-model="blacklistAutoCleaning"
              active-text="自动清理黑名单IP"
              inactive-text="手动清理模式"
              class="blacklist-switch"
              @change="saveBlacklistSettings"
            />
            <p class="blacklist-hint">
              {{ blacklistAutoCleaning ? '黑名单中的IP会在刷新列表时自动清理' : '黑名单IP仅做标记，不会自动清理' }}
            </p>
          </div>
          
          <div v-if="ipBlacklist.length === 0" class="empty-blacklist">
            黑名单为空
          </div>
          
          <el-tag
            v-for="ip in ipBlacklist"
            :key="ip"
            closable
            @close="removeFromBlacklist(ip)"
            class="blacklist-tag"
          >
            {{ ip }}
          </el-tag>
          
          <div class="blacklist-actions" v-if="ipBlacklist.length > 0">
            <el-button size="small" @click="cleanAllBlacklistedIps">
              立即清理所有黑名单IP
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import request from '@/api/request'
import type { FormInstance } from 'element-plus'
import * as ipPoolApi from '@/api/ipPool'
import { Search, InfoFilled, Delete } from '@element-plus/icons-vue'
import { testProxy, testProxyWithCorsProxy } from '@/utils/proxyTester'

// 定义接口类型
interface UrlItem {
  id: string
  url: string
  description: string
}

interface ProxyIp {
  ip: string
  port: number
  type: string
  source?: string
  lastValidTime?: string
  isValid?: boolean
  responseTime?: number  // 响应时间
  isBlacklisted?: boolean
}

interface IpPoolSettings {
  id: number
  enabled: boolean
  threadPoolSize: number
  schedulerPoolSize: number
  initialDelay: number
  period: number
  expireTime: number
  listKey: string
  setKey: string
  maxValidateCount: number
  minAvailableRate: number
  validateInterval: number
  timeout: number
  createTime: null | string
  updateTime: null | string
}

// IP池状态
const ipPoolStatus = ref(false)
const proxyCount = ref(0)
const validProxyCount = ref(0)
const avgResponseTime = ref(0)

// URL列表数据
const urlList = ref<UrlItem[]>([])
const tableLoading = ref(false)

// IP列表数据
const ipList = ref<ProxyIp[]>([])
const ipListLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const testingProxyId = ref<string>('')
const selectedProxies = ref<ProxyIp[]>([])
const batchTesting = ref(false)
const testingAll = ref(false)
const removingDuplicates = ref(false)

// 表单引用
const settingsFormRef = ref<FormInstance>()
const urlFormRef = ref<FormInstance>()
const proxyFormRef = ref<FormInstance>()

// URL对话框数据
const urlDialog = reactive({
  visible: false,
  isEdit: false,
  form: {
    id: '',
    url: '',
    description: ''
  } as UrlItem
})

// IP池设置表单
const ipPoolSettings = reactive<IpPoolSettings>({
  id: 1,
  enabled: true,
  threadPoolSize: 10,
  schedulerPoolSize: 3,
  initialDelay: 10,
  period: 600,
  expireTime: 3600,
  listKey: "proxy:ip:list",
  setKey: "proxy:ip:set",
  maxValidateCount: 10,
  minAvailableRate: 50,
  validateInterval: 300,
  timeout: 5000,
  createTime: null,
  updateTime: null
})

// IP添加对话框数据
const proxyDialog = reactive({
  visible: false,
  form: {
    ip: '',
    port: 0,
    type: '',
    source: ''
  } as ProxyIp
})

// 仪表板相关
const dashboardLoading = ref(false)
const responseTimeFilter = ref('all')
const chartContainer = ref<HTMLElement | null>(null)
let chart: any = null // 用于存储图表实例

// 测试结果对话框数据
interface TestResultsData {
  ip: string
  port: number
  type: string
  isValid: boolean
  responseTime: number
  testUrl: string
  message: string
  timestamp: string
  details: Record<string, any>
}

const testResultsDialog = reactive({
  visible: false
})

const testResultsData = ref<TestResultsData>({
  ip: '',
  port: 0,
  type: '',
  isValid: false,
  responseTime: 0,
  testUrl: '',
  message: '',
  timestamp: '',
  details: {}
})

// 批量测试结果数据
interface BatchTestResult {
  ip: string
  port: number
  isValid: boolean
  responseTime?: number
  errorMessage?: string
  timestamp: string
}

const batchTestDialog = reactive({
  visible: false
})

const batchTestResults = ref<BatchTestResult[]>([])

// 批量测试结果筛选
const batchResultsFilter = ref('all')
const batchResultsSort = ref('default')
const batchResultsSearch = ref('')

// 根据过滤条件筛选测试结果
const filteredBatchResults = computed(() => {
  let results = [...batchTestResults.value]
  
  // 按状态筛选
  if (batchResultsFilter.value !== 'all') {
    results = results.filter(r => {
      if (batchResultsFilter.value === 'valid') {
        return r.isValid
      } else {
        return !r.isValid
      }
    })
  }
  
  // 按IP搜索
  if (batchResultsSearch.value) {
    const keyword = batchResultsSearch.value.toLowerCase()
    results = results.filter(r => 
      r.ip.toLowerCase().includes(keyword) || 
      r.port.toString().includes(keyword)
    )
  }
  
  // 排序
  if (batchResultsSort.value !== 'default') {
    switch (batchResultsSort.value) {
      case 'response-asc':
        results.sort((a, b) => {
          // 无效的代理放在最后，有效的按响应时间升序
          if (a.isValid && b.isValid) {
            return (a.responseTime || 0) - (b.responseTime || 0)
          } else if (a.isValid) {
            return -1
          } else if (b.isValid) {
            return 1
          } else {
            return 0
          }
        })
        break
      case 'response-desc':
        results.sort((a, b) => {
          // 无效的代理放在最后，有效的按响应时间降序
          if (a.isValid && b.isValid) {
            return (b.responseTime || 0) - (a.responseTime || 0)
          } else if (a.isValid) {
            return -1
          } else if (b.isValid) {
            return 1
          } else {
            return 0
          }
        })
        break
      case 'ip-asc':
        results.sort((a, b) => {
          const ipCompare = a.ip.localeCompare(b.ip)
          if (ipCompare === 0) {
            return a.port - b.port
          }
          return ipCompare
        })
        break
      case 'ip-desc':
        results.sort((a, b) => {
          const ipCompare = b.ip.localeCompare(a.ip)
          if (ipCompare === 0) {
            return b.port - a.port
          }
          return ipCompare
        })
        break
    }
  }
  
  return results
})

// 获取IP池状态
const fetchIpPoolStatus = async () => {
  try {
    const response = await request.get('/api/ip-pool/status')
    // 处理不同的响应格式，支持直接返回布尔值或响应对象中的data字段
    if (response.data !== undefined) {
      if (typeof response.data === 'boolean') {
        ipPoolStatus.value = response.data
      } else if (response.data && typeof response.data === 'object') {
        // 支持 {success:true, data:true} 格式
        ipPoolStatus.value = response.data.data === true || response.data.status === true
      }
    }
    
    console.log('IP池状态更新:', ipPoolStatus.value, '原始响应:', response.data)
  } catch (error) {
    ElMessage.error('获取IP池状态失败')
    console.error('获取IP池状态失败:', error)
  }
}

// 启动IP池
const handleStartIpPool = async () => {
  try {
    const response = await request.post('/api/ip-pool/start')
    console.log('IP池启动响应:', response.data)
    
    if (response.data && (response.data.success === true || response.data === true || response.data.data === true)) {
      ElMessage.success('IP池启动成功')
      
      // 添加短暂延迟，确保后端状态已更新
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 更新状态
      await fetchIpPoolStatus()
    } else {
      ElMessage.error('IP池启动失败')
    }
  } catch (error) {
    ElMessage.error('IP池启动失败')
    console.error('IP池启动失败:', error)
  }
}

// 停止IP池
const handleStopIpPool = async () => {
  try {
    const response = await request.post('/api/ip-pool/stop')
    console.log('IP池停止响应:', response.data)
    
    if (response.data && (response.data.success === true || response.data === true || response.data.data === true)) {
      ElMessage.success('IP池停止成功')
      
      // 添加短暂延迟，确保后端状态已更新
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 更新状态
      await fetchIpPoolStatus()
    } else {
      ElMessage.error('IP池停止失败')
    }
  } catch (error) {
    ElMessage.error('IP池停止失败')
    console.error('IP池停止失败:', error)
  }
}

// 刷新IP池
const handleRefreshIpPool = async () => {
  try {
    // 显示加载消息
    ElMessage.info({
      message: 'IP池正在刷新中...',
      duration: 2000
    })
    
    // 先停止IP池
    await request.post('/api/ip-pool/stop')
    console.log('IP池已停止，准备重新启动')
    
    // 等待一秒后再启动
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 重新启动IP池
    const startResponse = await request.post('/api/ip-pool/start')
    console.log('IP池启动响应:', startResponse.data)
    
    // 确保状态更新
    await fetchIpPoolStatus()
    await fetchIpList()
    
    ElMessage.success('IP池刷新操作已完成')
  } catch (error) {
    ElMessage.error('IP池刷新失败')
    console.error('IP池刷新失败:', error)
    
    // 失败时也尝试更新状态，以确保UI正确
    fetchIpPoolStatus()
  }
}

// 获取所有设置
const fetchSettings = async () => {
  try {
    const response = await request.get('/api/IpSettings')
    if (!response.data || !response.data.data) {
      console.error('返回数据格式不正确:', response)
      ElMessage.error('返回数据格式不正确')
      return
    }
    
    const data = response.data.data
    console.log('获取到IP设置数据:', data)
    
    // 更新IP池设置
    Object.assign(ipPoolSettings, data)
    
    // 获取URL列表
    try {
      tableLoading.value = true
      const urlResponse = await request.get('/api/urls')
      console.log('获取到URL列表数据:', urlResponse.data)
      if (urlResponse.data && urlResponse.data.success && Array.isArray(urlResponse.data.data)) {
        urlList.value = urlResponse.data.data
      } else {
        urlList.value = []
        console.warn('URL列表数据格式不正确或为空')
      }
    } catch (urlError) {
      urlList.value = []
      console.error('获取URL列表失败:', urlError)
    } finally {
      tableLoading.value = false
    }
  } catch (error) {
    ElMessage.error('获取设置失败')
    console.error('获取设置失败:', error)
  }
}

// 保存IP池设置
const handleSaveSettings = async () => {
  if (!settingsFormRef.value) return
  
  try {
    const valid = await settingsFormRef.value.validate().catch(() => false)
    if (!valid) {
      ElMessage.warning('请检查表单填写是否正确')
      return
    }
    
    console.log('提交IP池设置:', ipPoolSettings)
    const response = await request.put('/api/IpSettings', ipPoolSettings)
    
    if (!response.data || !response.data.success) {
      ElMessage.error(response.data?.message || '保存失败')
      console.error('保存IP池设置失败:', response.data)
      return
    }
    
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
    console.error('保存失败:', error)
  }
}

// 添加URL
const handleAddUrl = () => {
  urlDialog.isEdit = false
  urlDialog.form = {
    id: '',
    url: '',
    description: ''
  }
  urlDialog.visible = true
}

// 编辑URL
const handleEditUrl = (row: UrlItem) => {
  urlDialog.isEdit = true
  urlDialog.form = { ...row }
  urlDialog.visible = true
}

// 删除URL
const handleDeleteUrl = async (id: string) => {
  if (!id) {
    ElMessage.error('URL ID不能为空')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除这个URL吗？', '提示', {
      type: 'warning'
    })
    
    const response = await request.delete(`/api/urls/${id}`)
    if (!response.data || !response.data.success) {
      ElMessage.error(response.data?.message || '删除失败')
      console.error('删除URL失败:', response.data)
      return
    }
    
    await fetchSettings()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除失败:', error)
    }
  }
}

// 保存URL
const handleSaveUrl = async () => {
  if (!urlFormRef.value) return
  
  try {
    const valid = await urlFormRef.value.validate().catch(() => false)
    if (!valid) {
      ElMessage.warning('请检查表单填写是否正确')
      return
    }
    
    // 验证URL格式
    if (!urlDialog.form.url.startsWith('http://') && !urlDialog.form.url.startsWith('https://')) {
      ElMessage.warning('URL必须以http://或https://开头')
      return
    }
    
    let response
    if (urlDialog.isEdit) {
      if (!urlDialog.form.id) {
        ElMessage.error('URL ID不能为空')
        return
      }
      response = await request.put(`/api/urls/${urlDialog.form.id}`, urlDialog.form)
    } else {
      response = await request.post('/api/urls', urlDialog.form)
    }
    
    if (!response.data || !response.data.success) {
      ElMessage.error(response.data?.message || '保存失败')
      console.error('保存URL失败:', response.data)
      return
    }
    
    await fetchSettings()
    urlDialog.visible = false
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
    console.error('保存失败:', error)
  }
}

// 统计代理可用性和平均响应时间
const calculateProxyStats = () => {
  const validProxies = ipList.value.filter(p => p.isValid)
  validProxyCount.value = validProxies.length
  
  // 计算平均响应时间
  if (validProxies.length > 0) {
    const totalResponseTime = validProxies.reduce((sum, proxy) => {
      return sum + (proxy.responseTime || 0)
    }, 0)
    avgResponseTime.value = Math.round(totalResponseTime / validProxies.length)
  } else {
    avgResponseTime.value = 0
  }
}

// 获取IP列表
const fetchIpList = async (skipBlacklistCleaning = false) => {
  ipListLoading.value = true
  try {
    const response = await request.get('/api/proxy/list', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        validStatus: filterStatus.value !== 'all' ? (filterStatus.value === 'valid') : undefined,
        keyword: searchKeyword.value || undefined
      }
    })
    
    // 直接使用响应数据
    if (Array.isArray(response.data)) {
      // 如果后端不支持过滤，在前端进行过滤
      let filteredList = response.data;
      
      // 如果没有在请求中应用过滤，手动在前端过滤
      if (filterStatus.value !== 'all') {
        filteredList = filteredList.filter(item => {
          if (filterStatus.value === 'valid') {
            return item.isValid === true;
          } else {
            return item.isValid === false;
          }
        });
      }
      
      // 如果没有在请求中应用搜索，手动在前端搜索
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        filteredList = filteredList.filter(item => 
          item.ip.toLowerCase().includes(keyword) || 
          (item.source && item.source.toLowerCase().includes(keyword))
        );
      }
      
      // 检查是否有黑名单IP并标记
      filteredList.forEach(ip => {
        if (ipBlacklist.value.includes(ip.ip)) {
          ip.isBlacklisted = true;
        }
      });
      
      ipList.value = filteredList;
      total.value = filteredList.length;
      
      // 总代理数量仍然是原始返回的数量
      proxyCount.value = response.data.length;
      
      // 计算统计数据
      calculateProxyStats();
      
      // 检查黑名单IP并根据设置决定是否自动清理
      // 仅在启用了自动清理功能、未跳过黑名单清理时执行
      if (blacklistAutoCleaning.value && !skipBlacklistCleaning) {
        const blacklistedItems = response.data.filter(ip => ipBlacklist.value.includes(ip.ip));
        if (blacklistedItems.length > 0) {
          console.log(`发现${blacklistedItems.length}个黑名单IP，将自动清理`);
          cleanBlacklistedIps();
        }
      } else if (ipBlacklist.value.length > 0) {
        // 如果禁用了自动清理但存在黑名单IP，仅显示提示
        const blacklistedCount = response.data.filter(ip => ipBlacklist.value.includes(ip.ip)).length;
        if (blacklistedCount > 0) {
          console.log(`发现${blacklistedCount}个黑名单IP，但自动清理功能已禁用`);
        }
      }
    } else {
      ipList.value = []
      total.value = 0
      proxyCount.value = 0
      validProxyCount.value = 0
      avgResponseTime.value = 0
    }
  } catch (error) {
    console.error('获取IP列表失败:', error)
    ElMessage.error('获取IP列表失败')
    ipList.value = []
    total.value = 0
    proxyCount.value = 0
    validProxyCount.value = 0
    avgResponseTime.value = 0
  } finally {
    ipListLoading.value = false
  }
}

// 清理黑名单中的IP
const cleanBlacklistedIps = async () => {
  try {
    let deletedCount = 0;
    for (const blacklistedIp of ipBlacklist.value) {
      // 判断列表中是否有此IP
      const hasBlacklistedIp = ipList.value.some(ip => ip.ip === blacklistedIp);
      if (hasBlacklistedIp) {
        await deleteIpByAddress(blacklistedIp);
        deletedCount++;
      }
    }
    
    if (deletedCount > 0) {
      console.log(`成功清理${deletedCount}个黑名单IP`);
      // 使用参数调用fetchIpList，跳过黑名单清理，避免死循环
      await fetchIpList(true);
    }
  } catch (error) {
    console.error('清理黑名单IP失败:', error);
  }
}

// 新增一个辅助函数用于从黑名单中删除IP但不触发刷新
const deleteIpByAddress = async (ip: string) => {
  try {
    // 获取所有IP
    const response = await request.get('/api/proxy/list', {
      params: {
        pageSize: 1000  // 获取足够多的IP
      }
    })
    
    if (!Array.isArray(response.data)) {
      console.error('获取IP列表失败');
      return 0;
    }
    
    // 筛选匹配的IP
    const matchingIps = response.data.filter((proxyIp: ProxyIp) => 
      proxyIp.ip === ip
    )
    
    if (matchingIps.length === 0) {
      return 0;
    }
    
    let deleteCount = 0;
    
    // 删除所有匹配的IP
    for (const proxyIp of matchingIps) {
      try {
        await request.delete('/api/proxy', {
          data: {
            ip: proxyIp.ip,
            port: proxyIp.port
          }
        })
        deleteCount++;
  } catch (error) {
        console.error(`删除IP失败: ${proxyIp.ip}:${proxyIp.port}`, error);
      }
    }
    
    return deleteCount;
  } catch (error) {
    console.error('删除IP失败:', error);
    return 0;
  }
}

// 修改handleDeleteIpByAddress函数来使用新的辅助函数
const handleDeleteIpByAddress = async (ip: string) => {
  if (!ip) {
    ElMessage.warning('请输入要清理的IP地址')
    return
  }
  
  try {
    ElMessage.info({
      message: `正在清理IP: ${ip}，请稍候...`,
      duration: 2000
    })
    
    const deleteCount = await deleteIpByAddress(ip);
    
    if (deleteCount === 0) {
      ElMessage.warning(`没有找到IP: ${ip}`);
      return;
    }
    
    ElMessage.success(`成功清理${deleteCount}个IP记录`);
    // 使用参数调用fetchIpList，跳过黑名单清理，避免死循环
    await fetchIpList(true);
    
    // 如果清理的是输入框中的IP，清空输入框
    if (ipManagementDialog.targetIp === ip) {
      ipManagementDialog.targetIp = '';
    }
    
  } catch (error) {
    console.error('清理IP失败:', error);
    ElMessage.error('清理IP失败');
  }
}

// 删除代理IP
const handleDeleteProxy = async (row: ProxyIp) => {
  try {
    await ElMessageBox.confirm('确定要删除这个代理IP吗？', '提示', {
      type: 'warning'
    })
    
    const response = await request.delete('/api/proxy', {
      data: {
        ip: row.ip,
        port: row.port
      }
    })
    
    ElMessage.success('删除成功')
    await fetchIpList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除代理IP失败:', error)
      ElMessage.error('删除代理IP失败')
    }
  }
}

// 测试代理IP
const handleTestProxy = async (row: ProxyIp) => {
  try {
    // 设置正在测试的代理ID (IP:端口)
    testingProxyId.value = `${row.ip}:${row.port}`
    
    ElMessage.info({
      message: `正在测试代理 ${row.ip}:${row.port}...`,
      duration: 2000
    })
    
    // 使用新的前端测试工具替代后端API
    const testData = await testProxy({
      ip: row.ip,
      port: row.port,
      type: row.type || 'HTTP'
    })
    
    // 更新测试结果数据以便在对话框中显示
    testResultsData.value = {
      ip: row.ip,
      port: row.port,
      type: row.type || 'HTTP',
      isValid: testData.success,
      responseTime: testData.responseTime || 0,
      testUrl: testData.testUrl || '',
      message: testData.message,
      timestamp: testData.timestamp,
      details: testData.details || {}
    }
    
    // 显示测试结果对话框
    testResultsDialog.visible = true
    
    if (testData.success) {
      // 更新本地数据，显示响应时间
      row.isValid = true;
      row.responseTime = testData.responseTime;
      row.lastValidTime = new Date().toLocaleString();
      
      ElMessage.success({
        message: `代理可用，响应时间: ${testData.responseTime}毫秒`,
        duration: 3000
      })
    } else {
      // 更新本地数据，标记为无效
      row.isValid = false;
      row.responseTime = undefined;
      
      ElMessage.error({
        message: `代理不可用: ${testData.message}`,
        duration: 3000
      })
    }
    
    // 刷新列表以获取最新状态
    await fetchIpList()
    
    return row;
  } catch (error) {
    console.error('测试代理IP失败:', error)
    ElMessage.error('测试代理IP失败，请检查网络连接')
    
    // 显示错误信息
    testResultsData.value = {
      ip: row.ip,
      port: row.port,
      type: row.type || 'HTTP',
      isValid: false,
      responseTime: 0,
      testUrl: '',
      message: error instanceof Error ? error.message : '网络错误',
      timestamp: new Date().toLocaleString(),
      details: {}
    }
    testResultsDialog.visible = true
    
    return {
      ...row,
      isValid: false,
      responseTime: undefined,
      lastValidTime: new Date().toLocaleString()
    };
  } finally {
    // 清除正在测试的代理ID
    testingProxyId.value = ''
  }
}

// 获取新IP
const handleGetIp = async () => {
  try {
    const response = await request.get('/api/proxy/fastest')
    if (response.data && response.data.ip) {
      ElMessage.success(`获取最快IP成功: ${response.data.ip}:${response.data.port}`)
      await fetchIpList()
    } else {
      ElMessage.warning('当前没有可用的IP')
    }
  } catch (error) {
    console.error('获取IP失败:', error)
    ElMessage.error('获取IP失败')
  }
}

// 添加代理IP
const handleAddProxyIp = () => {
  proxyDialog.visible = true
  proxyDialog.form = {
    ip: '',
    port: 0,
    type: '',
    source: ''
  }
}

// 保存代理IP
const handleSaveProxyIp = async () => {
  if (!proxyFormRef.value) return
  
  try {
    const valid = await proxyFormRef.value.validate().catch(() => false)
    if (!valid) {
      ElMessage.warning('请检查表单填写是否正确')
      return
    }

    // 如果没有填写来源，设置默认值
    if (!proxyDialog.form.source) {
      proxyDialog.form.source = '手动添加'
    }
    
    // 检查IP是否已存在
    const response = await request.get('/api/proxy/list')
    if (Array.isArray(response.data)) {
      const existingIp = response.data.find((ip: ProxyIp) => 
        ip.ip === proxyDialog.form.ip && ip.port === proxyDialog.form.port
      )
      
      if (existingIp) {
        ElMessage.warning(`IP ${proxyDialog.form.ip}:${proxyDialog.form.port} 已存在，不能重复添加`)
        return
      }
    }
    
    const addResponse = await request.post('/api/proxy', proxyDialog.form)
    if (!addResponse.data || !addResponse.data.success) {
      ElMessage.error(addResponse.data?.message || '保存失败')
      console.error('保存代理IP失败:', addResponse.data)
      return
    }
    
    ElMessage.success('代理IP保存成功')
    await fetchIpList()
    proxyDialog.visible = false
  } catch (error) {
    ElMessage.error('保存代理IP失败')
    console.error('保存代理IP失败:', error)
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchIpList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchIpList()
}

const handleSelectionChange = (rows: ProxyIp[]) => {
  selectedProxies.value = rows
}

const handleBatchTestProxies = async () => {
  if (selectedProxies.value.length === 0) {
    ElMessage.warning('请选择要测试的代理IP')
    return
  }
  
  batchTesting.value = true
  let successCount = 0;
  let failedCount = 0;
  
  // 清空之前的测试结果
  batchTestResults.value = []
  
  try {
    ElMessage.info(`开始批量测试${selectedProxies.value.length}个代理IP`)
    
    // 每次测试一个代理，避免并发请求太多
    for (const proxy of selectedProxies.value) {
      testingProxyId.value = `${proxy.ip}:${proxy.port}`
      
      try {
        // 使用前端测试工具
        const testData = await testProxy({
          ip: proxy.ip,
          port: proxy.port,
          type: proxy.type || 'HTTP'
        })
        
        if (testData.success) {
          // 更新当前代理状态
          proxy.isValid = true;
          proxy.responseTime = testData.responseTime;
          proxy.lastValidTime = new Date().toLocaleString();
          successCount++;
          
          // 添加到测试结果列表
          batchTestResults.value.push({
            ip: proxy.ip,
            port: proxy.port,
            isValid: true,
            responseTime: testData.responseTime,
            timestamp: testData.timestamp
          })
        } else {
          proxy.isValid = false;
          proxy.responseTime = undefined;
          failedCount++;
          
          // 添加到测试结果列表
          batchTestResults.value.push({
            ip: proxy.ip,
            port: proxy.port,
            isValid: false,
            errorMessage: testData.message || '连接失败',
            timestamp: testData.timestamp
          })
        }
      } catch (error) {
        console.error(`测试代理IP失败: ${proxy.ip}:${proxy.port}`, error)
        proxy.isValid = false;
        failedCount++;
        
        // 添加到测试结果列表
        batchTestResults.value.push({
          ip: proxy.ip,
          port: proxy.port,
          isValid: false,
          errorMessage: error instanceof Error ? error.message : '网络错误',
          timestamp: new Date().toLocaleString()
        })
      }
      
      // 短暂延迟，避免请求过快
      await new Promise(resolve => setTimeout(resolve, 200));
    }
    
    testingProxyId.value = '';
    
    // 刷新列表以获取最新状态
    await fetchIpList();
    
    // 显示批量测试结果对话框
    batchTestDialog.visible = true
    
    ElMessage.success(`批量测试完成: ${successCount}个可用, ${failedCount}个不可用`);
  } catch (error) {
    console.error('批量测试失败:', error)
    ElMessage.error('批量测试失败')
  } finally {
    batchTesting.value = false
    testingProxyId.value = ''
  }
}

// 测试所有代理
const handleTestAllProxies = async () => {
  if (ipList.value.length === 0) {
    ElMessage.warning('没有可测试的代理IP')
    return
  }
  
  testingAll.value = true
  let successCount = 0;
  let failedCount = 0;
  
  // 清空之前的测试结果
  batchTestResults.value = []
  
  try {
    ElMessage.info(`开始测试所有${ipList.value.length}个代理IP`)
    
    // 每次测试一个代理，避免并发请求太多
    for (const proxy of ipList.value) {
      testingProxyId.value = `${proxy.ip}:${proxy.port}`
      
      try {
        // 使用前端测试工具
        const testData = await testProxy({
          ip: proxy.ip,
          port: proxy.port,
          type: proxy.type || 'HTTP'
        })
        
        if (testData.success) {
          // 更新当前代理状态
          proxy.isValid = true;
          proxy.responseTime = testData.responseTime;
          proxy.lastValidTime = new Date().toLocaleString();
          successCount++;
          
          // 添加到测试结果列表
          batchTestResults.value.push({
            ip: proxy.ip,
            port: proxy.port,
            isValid: true,
            responseTime: testData.responseTime,
            timestamp: testData.timestamp
          })
        } else {
          proxy.isValid = false;
          proxy.responseTime = undefined;
          failedCount++;
          
          // 添加到测试结果列表
          batchTestResults.value.push({
            ip: proxy.ip,
            port: proxy.port,
            isValid: false,
            errorMessage: testData.message || '连接失败',
            timestamp: testData.timestamp
          })
        }
      } catch (error) {
        console.error(`测试代理IP失败: ${proxy.ip}:${proxy.port}`, error)
        proxy.isValid = false;
        failedCount++;
        
        // 添加到测试结果列表
        batchTestResults.value.push({
          ip: proxy.ip,
          port: proxy.port,
          isValid: false,
          errorMessage: error instanceof Error ? error.message : '网络错误',
          timestamp: new Date().toLocaleString()
        })
      }
      
      // 更新统计信息
      calculateProxyStats();
      
      // 短暂延迟，避免请求过快
      await new Promise(resolve => setTimeout(resolve, 200));
    }
    
    testingProxyId.value = '';
    
    // 刷新列表以获取最新状态
    await fetchIpList();
    
    // 显示批量测试结果对话框
    batchTestDialog.visible = true
    
    ElMessage.success(`全部测试完成: ${successCount}个可用, ${failedCount}个不可用`);
  } catch (error) {
    console.error('测试全部代理失败:', error)
    ElMessage.error('测试全部代理失败')
  } finally {
    testingAll.value = false
    testingProxyId.value = ''
  }
}

// 获取行的类名
const getRowClassName = ({ row }: { row: ProxyIp }) => {
  return testingProxyId.value === `${row.ip}:${row.port}` ? 'testing-row' : '';
}

// 组件挂载时获取设置和状态
onMounted(() => {
  // 加载黑名单
  const savedBlacklist = localStorage.getItem('ipBlacklist')
  if (savedBlacklist) {
    try {
      const parsed = JSON.parse(savedBlacklist)
      if (Array.isArray(parsed)) {
        ipBlacklist.value = parsed
      }
    } catch (e) {
      console.error('解析黑名单失败:', e)
    }
  }
  
  // 加载黑名单自动清理设置
  const savedAutoCleaning = localStorage.getItem('blacklistAutoCleaning')
  if (savedAutoCleaning) {
    blacklistAutoCleaning.value = savedAutoCleaning === 'true'
  }
  
  // 初始化时，禁用自动清理，以避免刚加载时就触发无限循环
  fetchSettings()
  fetchIpPoolStatus()
  fetchIpList(true) // 加载时跳过黑名单清理
})

// 新增过滤和搜索功能
const filterStatus = ref('all')
const searchKeyword = ref('')

const handleFilterChange = () => {
  fetchIpList()
}

const handleSearch = () => {
  fetchIpList()
}

// 定义排序状态
const sortOrder = ref({
  prop: '',
  order: ''
})

// 表格排序变化处理
const handleSortChange = (column: { prop: string; order: string }) => {
  sortOrder.value.prop = column.prop
  sortOrder.value.order = column.order
  
  // 对本地数据进行排序
  if (column.prop && column.order) {
    const isAsc = column.order === 'ascending'
    
    ipList.value.sort((a, b) => {
      // 响应时间排序（对有效性列的特殊处理）
      if (column.prop === 'isValid') {
        // 先按有效性排序
        if (a.isValid !== b.isValid) {
          return isAsc ? (a.isValid ? -1 : 1) : (a.isValid ? 1 : -1)
        }
        
        // 同为有效，按响应时间排序
        if (a.isValid && b.isValid) {
          const aTime = a.responseTime || Number.MAX_SAFE_INTEGER
          const bTime = b.responseTime || Number.MAX_SAFE_INTEGER
          return isAsc ? (aTime - bTime) : (bTime - aTime)
        }
        
        return 0
      }
      
      // 其他列的通用排序
      const aVal = a[column.prop as keyof ProxyIp]
      const bVal = b[column.prop as keyof ProxyIp]
      
      if (typeof aVal === 'string' && typeof bVal === 'string') {
        return isAsc ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal)
      } else if (typeof aVal === 'number' && typeof bVal === 'number') {
        return isAsc ? (aVal - bVal) : (bVal - aVal)
      }
      
      return 0
    })
  }
}

// 有效性排序方法
const sortByValidity = (a: ProxyIp, b: ProxyIp) => {
  // 此方法不会被调用，因为我们使用了自定义的handleSortChange
  return 0
}

// 获取详细状态信息
const fetchDetailedStatus = async () => {
  dashboardLoading.value = true
  try {
    // 获取IP池信息
    const response = await ipPoolApi.getIpPoolInfo()
    if (response && response.status) {
      // 更新统计数据
      proxyCount.value = response.status.totalCount
      validProxyCount.value = response.status.validCount
      avgResponseTime.value = response.status.averageSpeed
      
      // 获取IP列表
      await fetchIpList()
      
      // 更新图表
      nextTick(() => {
        updateChartData()
      })
    }
  } catch (error) {
    console.error('获取IP池详细状态失败:', error)
    ElMessage.error('获取IP池详细状态失败')
  } finally {
    dashboardLoading.value = false
  }
}

// 更新图表数据
const updateChartData = () => {
  if (!chartContainer.value) return
  
  // 根据筛选条件过滤数据
  let filteredProxies = [...ipList.value]
  if (responseTimeFilter.value === 'fast') {
    filteredProxies = filteredProxies.filter(p => p.isValid && p.responseTime && p.responseTime < 200)
  } else if (responseTimeFilter.value === 'medium') {
    filteredProxies = filteredProxies.filter(p => p.isValid && p.responseTime && p.responseTime >= 200 && p.responseTime <= 500)
  } else if (responseTimeFilter.value === 'slow') {
    filteredProxies = filteredProxies.filter(p => p.isValid && p.responseTime && p.responseTime > 500)
  }
  
  // 如果没有数据可视化库，可以简单显示统计信息
  // 这里可以根据项目中使用的图表库来实现图表展示
  // 例如，使用 ECharts 或其他图表库
  
  // 在实际项目中可以添加图表相关代码
  // 如果有图表库，在这里添加图表初始化和数据更新代码
}

// 获取所有数据
const fetchAllData = async () => {
  await fetchSettings()
  await fetchIpList()
  await fetchDetailedStatus()
}

// 计算测试可用代理的平均响应时间
const calculateAvgResponseTime = () => {
  const validResults = batchTestResults.value.filter(r => r.isValid && r.responseTime)
  if (validResults.length === 0) return 0
  
  const totalResponseTime = validResults.reduce((sum, result) => {
    return sum + (result.responseTime || 0)
  }, 0)
  
  return Math.round(totalResponseTime / validResults.length)
}

// 导出测试结果为CSV
const exportTestResults = () => {
  if (filteredBatchResults.value.length === 0) {
    ElMessage.warning('没有可导出的测试结果')
    return
  }
  
  // 创建CSV内容
  let csvContent = 'IP,端口,状态,响应时间/错误信息,测试时间\n'
  
  filteredBatchResults.value.forEach(result => {
    const status = result.isValid ? '有效' : '无效'
    const responseInfo = result.isValid ? `${result.responseTime}ms` : result.errorMessage
    csvContent += `${result.ip},${result.port},${status},"${responseInfo}",${result.timestamp}\n`
  })
  
  // 创建下载链接
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', `代理测试结果_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 添加清理重复IP的函数
const handleRemoveDuplicateIps = async () => {
  try {
    removingDuplicates.value = true
    
    ElMessage.info({
      message: '正在清理重复IP，请稍候...',
      duration: 2000
    })
    
    // 获取所有IP
    const allIps = await request.get('/api/proxy/list', {
      params: {
        pageSize: 1000  // 获取足够多的IP
      }
    })
    
    if (!Array.isArray(allIps.data)) {
      ElMessage.error('获取IP列表失败')
      return
    }
    
    // 根据IP地址分组，找出重复的IP
    const ipGroups: Record<string, ProxyIp[]> = {}
    allIps.data.forEach((ip: ProxyIp) => {
      if (!ipGroups[ip.ip]) {
        ipGroups[ip.ip] = []
      }
      ipGroups[ip.ip].push(ip)
    })
    
    // 找到有重复的IP
    const duplicateIps = Object.values(ipGroups).filter(group => group.length > 1)
    
    if (duplicateIps.length === 0) {
      ElMessage.success('没有发现重复的IP')
      return
    }
    
    console.log('发现的重复IP:', duplicateIps)
    
    // 保留每组中最好的一个（响应时间最短或最近验证的），删除其他的
    let deleteCount = 0
    
    for (const group of duplicateIps) {
      // 排序：优先保留可用的，其次是响应时间最短的，再次是最近验证的
      group.sort((a: ProxyIp, b: ProxyIp) => {
        // 优先保留可用的
        if (a.isValid !== b.isValid) {
          return a.isValid ? -1 : 1
        }
        
        // 对于都可用的，按响应时间排序
        if (a.isValid && a.responseTime && b.responseTime) {
          return a.responseTime - b.responseTime
        }
        
        // 按最后验证时间排序
        if (a.lastValidTime && b.lastValidTime) {
          return new Date(b.lastValidTime).getTime() - new Date(a.lastValidTime).getTime()
        }
        
        return 0
      })
      
      // 保留第一个（最好的），删除其他的
      for (let i = 1; i < group.length; i++) {
        const ip = group[i]
        try {
          await request.delete('/api/proxy', {
            data: {
              ip: ip.ip,
              port: ip.port
            }
          })
          deleteCount++
          console.log(`成功删除重复IP: ${ip.ip}:${ip.port}`)
        } catch (error) {
          console.error(`删除重复IP失败: ${ip.ip}:${ip.port}`, error)
        }
      }
    }
    
    ElMessage.success(`清理完成，已删除${deleteCount}个重复的IP`)
    
    // 刷新IP列表
    await fetchIpList()
    
  } catch (error) {
    console.error('清理重复IP失败:', error)
    ElMessage.error('清理重复IP失败')
  } finally {
    removingDuplicates.value = false
  }
}

// IP管理对话框数据
const ipManagementDialog = reactive({
  visible: false,
  targetIp: ''
})

// 显示IP管理对话框
const showIpManagementDialog = () => {
  ipManagementDialog.visible = true
  ipManagementDialog.targetIp = ''
}

// 在全局变量部分添加
// IP黑名单
const ipBlacklist = ref<string[]>(['8.219.97.248']) // 默认将用户提到的问题IP加入黑名单
const blacklistAutoCleaning = ref(false) // 默认关闭黑名单自动清理

// 添加管理黑名单的函数
const addToBlacklist = (ip: string) => {
  if (!ip || ipBlacklist.value.includes(ip)) return
  ipBlacklist.value.push(ip)
  ElMessage.success(`已将 ${ip} 加入黑名单`)
  
  // 保存到本地存储，持久化黑名单
  localStorage.setItem('ipBlacklist', JSON.stringify(ipBlacklist.value))
}

const removeFromBlacklist = (ip: string) => {
  const index = ipBlacklist.value.indexOf(ip)
  if (index !== -1) {
    ipBlacklist.value.splice(index, 1)
    ElMessage.success(`已将 ${ip} 从黑名单移除`)
    
    // 更新本地存储
    localStorage.setItem('ipBlacklist', JSON.stringify(ipBlacklist.value))
  }
}

// 添加保存黑名单设置的函数
const saveBlacklistSettings = () => {
  localStorage.setItem('blacklistAutoCleaning', blacklistAutoCleaning.value ? 'true' : 'false');
  ElMessage.success(`已${blacklistAutoCleaning.value ? '启用' : '禁用'}黑名单自动清理`);
}

// 添加立即清理所有黑名单IP的函数
const cleanAllBlacklistedIps = async () => {
  try {
    // 先显示加载中
    const loading = ElLoading.service({
      lock: true,
      text: '正在清理黑名单IP...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    // 调用清理函数
    await cleanBlacklistedIps();
    
    // 关闭加载
    loading.close();
    
    ElMessage.success('黑名单IP清理完成');
  } catch (error) {
    ElMessage.error('清理黑名单IP失败');
    console.error('清理黑名单IP失败:', error);
  }
}

// Redis导入表单
const redisImportForm = reactive({
  count: 10,
  type: 'HTTP',
  source: '手动导入',
  ipList: ''
})

const importing = ref(false)

// 导入IP到Redis
const handleImportToRedis = async () => {
  if (!redisImportForm.ipList.trim()) {
    ElMessage.warning('请输入要导入的IP列表')
    return
  }
  
  try {
    importing.value = true
    
    // 解析IP列表
    const lines = redisImportForm.ipList.split('\n').filter(line => line.trim())
    
    if (lines.length === 0) {
      ElMessage.warning('没有有效的IP地址')
      return
    }
    
    // 创建导入任务
    const importTasks = lines.map(async (line) => {
      const [ip, portStr] = line.split(':')
      const port = parseInt(portStr)
      
      if (!ip || !port || isNaN(port)) {
        console.warn(`跳过无效的IP格式: ${line}`)
        return { success: false, message: `无效的格式: ${line}` }
      }
      
      try {
        // 添加到Redis
        await request.post('/api/proxy', {
          ip: ip.trim(),
          port,
          type: redisImportForm.type,
          source: redisImportForm.source
        })
        
        return { success: true, ip, port }
      } catch (error) {
        console.error(`添加IP失败: ${ip}:${port}`, error)
        return { success: false, message: `添加失败: ${ip}:${port}`, error }
      }
    })
    
    // 等待所有任务完成
    const results = await Promise.all(importTasks)
    
    // 统计结果
    const successCount = results.filter(r => r.success).length
    const failedCount = results.length - successCount
    
    if (successCount > 0) {
      ElMessage.success(`成功导入${successCount}个IP到Redis`)
      
      // 刷新IP列表
      await fetchIpList()
      
      // 清空输入框
      redisImportForm.ipList = ''
    }
    
    if (failedCount > 0) {
      ElMessage.warning(`${failedCount}个IP导入失败`)
    }
    
  } catch (error) {
    ElMessage.error('导入过程中发生错误')
    console.error('导入IP失败:', error)
  } finally {
    importing.value = false
  }
}

// 生成随机IP和端口
const generateRandomIP = () => {
  const a = Math.floor(Math.random() * 255) + 1
  const b = Math.floor(Math.random() * 255) + 1
  const c = Math.floor(Math.random() * 255) + 1
  const d = Math.floor(Math.random() * 255) + 1
  return `${a}.${b}.${c}.${d}`
}

const generateRandomPort = () => {
  return Math.floor(Math.random() * 60000) + 1024
}

// 添加随机示例IP
const handleAddRandomIps = () => {
  const count = redisImportForm.count || 10
  let ips = []
  
  for (let i = 0; i < count; i++) {
    const ip = generateRandomIP()
    const port = generateRandomPort()
    ips.push(`${ip}:${port}`)
  }
  
  redisImportForm.ipList = ips.join('\n')
  ElMessage.success(`已生成${count}个示例IP`)
}

// 添加直接向Redis添加IP的方法
const addIPToRedis = async (ip: string, port: number, type: string = 'HTTP', source: string = '手动添加') => {
  try {
    const response = await request.post('/api/proxy', {
      ip,
      port,
      type,
      source
    })
    
    if (response.data && response.data.success) {
      return { success: true, message: '添加成功' }
    } else {
      return { success: false, message: response.data?.message || '添加失败' }
    }
  } catch (error) {
    console.error('向Redis添加IP失败:', error)
    return { success: false, message: `添加失败: ${error instanceof Error ? error.message : '未知错误'}` }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.status-info {
  padding: 10px 0;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-label {
  font-size: 14px;
  color: #909399;
}

.status-value {
  font-size: 16px;
  font-weight: bold;
}

.valid-count {
  color: #67c23a;
}

.count-percentage {
  font-size: 14px;
  color: #909399;
  margin-left: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.response-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.el-table .testing-row {
  background-color: #f0f9eb;
}

.filter-row {
  margin-bottom: 10px;
}

.ip-dashboard-card {
  margin-top: 16px;
  margin-bottom: 16px;
}

.dashboard-content {
  min-height: 200px;
}

.performance-chart {
  margin-top: 24px;
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.test-results-content {
  padding: 20px;
}

.result-item {
  margin-bottom: 10px;
}

.label {
  font-weight: bold;
}

.value {
  margin-left: 10px;
}

.details {
  white-space: pre-wrap;
}

.batch-test-results-content {
  padding: 10px;
}

.test-summary {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.summary-item {
  margin-right: 20px;
  margin-bottom: 5px;
}

.summary-label {
  font-weight: bold;
  margin-right: 5px;
}

.summary-value {
  font-size: 16px;
}

.summary-value.success {
  color: #67c23a;
}

.summary-value.error {
  color: #f56c6c;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 10px;
  flex-wrap: wrap;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  text-align: center;
}

.status-tag.success {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.status-tag.error {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}

.error-message {
  color: #f56c6c;
}

.empty-results {
  padding: 20px;
  text-align: center;
}

.info-icon {
  color: #909399;
  cursor: help;
  font-size: 16px;
}

.ip-management-content {
  padding: 20px;
}

.ip-input-group {
  margin-bottom: 20px;
}

.ip-input {
  width: 300px;
  margin-right: 10px;
}

.common-actions {
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.blacklist-section {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 10px;
}

.blacklist-hint {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.blacklist-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.empty-blacklist {
  color: #909399;
  font-style: italic;
}

.blacklist-settings {
  margin-bottom: 15px;
}

.blacklist-switch {
  margin-bottom: 5px;
}

.blacklist-actions {
  margin-top: 10px;
}

.redis-import-section {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 10px;
}

.redis-form {
  margin-top: 10px;
}

.ip-textarea-item {
  margin-bottom: 10px;
}
</style> 
