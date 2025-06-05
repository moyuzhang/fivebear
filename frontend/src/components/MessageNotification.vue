<template>
    <div class="message-notification-container">
        <!-- 消息弹窗列表 -->
        <transition-group name="notification" tag="div" class="notification-list">
            <div v-for="notification in notifications" :key="notification.id" :class="[
                'notification-item',
                `notification-${notification.level}`,
                { 'notification-closable': notification.closable }
            ]">
                <!-- 消息图标 -->
                <div class="notification-icon">
                    <el-icon v-if="notification.level === 'info'" color="#409EFF">
                        <InfoFilled />
                    </el-icon>
                    <el-icon v-else-if="notification.level === 'success'" color="#67C23A">
                        <SuccessFilled />
                    </el-icon>
                    <el-icon v-else-if="notification.level === 'warning'" color="#E6A23C">
                        <WarningFilled />
                    </el-icon>
                    <el-icon v-else-if="notification.level === 'error'" color="#F56C6C">
                        <CircleCloseFilled />
                    </el-icon>
                    <el-icon v-else color="#909399">
                        <Bell />
                    </el-icon>
                </div>

                <!-- 消息内容 -->
                <div class="notification-content">
                    <div class="notification-title">
                        {{ notification.title }}
                    </div>
                    <div class="notification-message">
                        {{ notification.message }}
                    </div>
                    <div v-if="notification.data" class="notification-data">
                        <el-collapse v-if="shouldShowData(notification)" accordion>
                            <el-collapse-item title="详细信息" name="details">
                                <pre>{{ formatData(notification.data) }}</pre>
                            </el-collapse-item>
                        </el-collapse>
                    </div>
                    <div class="notification-meta">
                        <span class="notification-time">{{ formatTime(notification.timestamp) }}</span>
                        <span v-if="notification.meta?.requiresRole" class="notification-role">
                            {{ notification.meta.requiredRole || '管理员' }}
                        </span>
                    </div>
                </div>

                <!-- 关闭按钮 -->
                <div v-if="notification.closable" class="notification-close">
                    <el-button type="text" size="small" @click="closeNotification(notification.id)">
                        <el-icon>
                            <Close />
                        </el-icon>
                    </el-button>
                </div>
            </div>
        </transition-group>

        <!-- 全局消息数量指示器 -->
        <div v-if="totalCount > 0" class="message-indicator" @click="toggleMessageCenter">
            <el-badge :value="totalCount" :max="99">
                <el-icon size="24">
                    <Bell />
                </el-icon>
            </el-badge>
        </div>

        <!-- 消息中心抽屉 -->
        <el-drawer v-model="showMessageCenter" title="消息中心" direction="rtl" size="400px">
            <div class="message-center">
                <div class="message-center-header">
                    <el-tabs v-model="activeTab">
                        <el-tab-pane label="系统消息" name="system"></el-tab-pane>
                        <el-tab-pane label="用户活动" name="user" v-if="isAdmin"></el-tab-pane>
                        <el-tab-pane label="所有消息" name="all"></el-tab-pane>
                    </el-tabs>
                    <div class="message-center-actions">
                        <el-button size="small" @click="clearMessages">清空</el-button>
                        <el-button size="small" @click="markAllAsRead">全部已读</el-button>
                    </div>
                </div>

                <div class="message-center-content">
                    <div v-for="msg in filteredMessages" :key="msg.id"
                        :class="['message-item', { 'message-unread': !msg.read }]" @click="markAsRead(msg.id)">
                        <div class="message-item-header">
                            <span :class="`message-type message-type-${msg.level}`">
                                {{ getMessageTypeLabel(msg.type) }}
                            </span>
                            <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
                        </div>
                        <div class="message-item-content">{{ msg.message }}</div>
                        <div v-if="msg.data" class="message-item-data">
                            {{ formatSimpleData(msg.data) }}
                        </div>
                    </div>

                    <div v-if="filteredMessages.length === 0" class="no-messages">
                        <el-empty description="暂无消息" />
                    </div>
                </div>
            </div>
        </el-drawer>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
    InfoFilled,
    SuccessFilled,
    WarningFilled,
    CircleCloseFilled,
    Bell,
    Close
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getGlobalWebSocket } from '@/utils/unifiedWebSocket'

interface MessageData {
    onlineUserCount?: number
    totalConnections?: number
    username?: string
    action?: string
    [key: string]: any
}

interface NotificationMessage {
    id: string
    type: string
    title: string
    message: string
    level: 'info' | 'success' | 'warning' | 'error'
    timestamp: string
    data?: MessageData
    meta?: {
        description?: string
        requiresRole?: boolean
        requiredRole?: string
    }
    closable: boolean
    autoClose: boolean
    duration: number
    read: boolean
}

// 响应式数据
const notifications = ref<NotificationMessage[]>([])
const allMessages = ref<NotificationMessage[]>([])
const showMessageCenter = ref(false)
const activeTab = ref('system')

// 用户store
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin())

// 计算属性
const totalCount = computed(() => allMessages.value.filter(msg => !msg.read).length)

const filteredMessages = computed(() => {
    switch (activeTab.value) {
        case 'system':
            return allMessages.value.filter(msg =>
                msg.type.startsWith('SYSTEM_') || msg.type === 'CONNECTION_SUCCESS'
            )
        case 'user':
            return allMessages.value.filter(msg =>
                msg.type.startsWith('ADMIN_') || msg.type === 'FORCE_LOGOUT'
            )
        case 'all':
        default:
            return allMessages.value
    }
})

// WebSocket连接
let webSocket: any = null

// 组件挂载
onMounted(() => {
    setupWebSocketListener()
})

// 组件卸载
onUnmounted(() => {
    if (webSocket) {
        webSocket.offMessage('*', handleWebSocketMessage)
    }
})

// 设置WebSocket监听器
const setupWebSocketListener = () => {
    try {
        webSocket = getGlobalWebSocket()
        if (webSocket) {
            // 监听所有消息
            webSocket.onMessage('*', handleWebSocketMessage)
            console.log('📡 MessageNotification: WebSocket监听器已设置')
        }
    } catch (error) {
        console.error('❌ MessageNotification: 设置WebSocket监听器失败:', error)
    }
}

// 处理WebSocket消息
const handleWebSocketMessage = (message: any) => {
    console.log('📩 MessageNotification: 收到消息:', message)

    // 创建通知消息
    const notification = createNotification(message)

    // 添加到消息列表
    allMessages.value.unshift(notification)

    // 限制消息数量
    if (allMessages.value.length > 100) {
        allMessages.value = allMessages.value.slice(0, 100)
    }

    // 显示弹窗通知
    if (shouldShowPopupNotification(message)) {
        showPopupNotification(notification)
    }

    // 播放提示音（可选）
    playNotificationSound(message.type)
}

// 创建通知消息
const createNotification = (message: any): NotificationMessage => {
    const id = `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

    return {
        id,
        type: message.type || 'UNKNOWN',
        title: getMessageTitle(message.type),
        message: message.message || '未知消息',
        level: getMessageLevel(message.level || message.type),
        timestamp: message.timestamp || new Date().toISOString(),
        data: message.data,
        meta: message.meta,
        closable: true,
        autoClose: shouldAutoClose(message.type),
        duration: getNotificationDuration(message.type),
        read: false
    }
}

// 获取消息标题
const getMessageTitle = (type: string): string => {
    const titleMap: Record<string, string> = {
        'SYSTEM_INFO': '系统信息',
        'SYSTEM_WARNING': '系统警告',
        'SYSTEM_ERROR': '系统错误',
        'ADMIN_ONLINE_USER_COUNT': '在线用户统计',
        'ADMIN_SYSTEM_STATUS': '系统状态',
        'ADMIN_USER_ACTION': '用户活动',
        'CONNECTION_SUCCESS': '连接成功',
        'FORCE_LOGOUT': '强制下线',
        'BUSINESS_INFO': '业务信息',
        'BUSINESS_WARNING': '业务警告',
        'BUSINESS_ERROR': '业务错误'
    }
    return titleMap[type] || '系统消息'
}

// 获取消息级别
const getMessageLevel = (level: string): 'info' | 'success' | 'warning' | 'error' => {
    const levelMap: Record<string, 'info' | 'success' | 'warning' | 'error'> = {
        'info': 'info',
        'success': 'success',
        'warning': 'warning',
        'error': 'error',
        'SYSTEM_INFO': 'info',
        'SYSTEM_WARNING': 'warning',
        'SYSTEM_ERROR': 'error',
        'CONNECTION_SUCCESS': 'success',
        'FORCE_LOGOUT': 'error'
    }
    return levelMap[level] || 'info'
}

// 获取消息类型标签
const getMessageTypeLabel = (type: string): string => {
    const labelMap: Record<string, string> = {
        'SYSTEM_INFO': '系统',
        'SYSTEM_WARNING': '警告',
        'SYSTEM_ERROR': '错误',
        'ADMIN_ONLINE_USER_COUNT': '统计',
        'ADMIN_SYSTEM_STATUS': '状态',
        'ADMIN_USER_ACTION': '活动',
        'CONNECTION_SUCCESS': '连接',
        'FORCE_LOGOUT': '登出',
        'BUSINESS_INFO': '业务',
        'BUSINESS_WARNING': '业务',
        'BUSINESS_ERROR': '业务'
    }
    return labelMap[type] || '消息'
}

// 判断是否显示弹窗通知
const shouldShowPopupNotification = (message: any): boolean => {
    const importantTypes = [
        'FORCE_LOGOUT',
        'SYSTEM_ERROR',
        'SYSTEM_WARNING',
        'BUSINESS_ERROR',
        'BUSINESS_WARNING'
    ]
    return importantTypes.includes(message.type)
}

// 判断是否自动关闭
const shouldAutoClose = (type: string): boolean => {
    const noAutoCloseTypes = ['FORCE_LOGOUT', 'SYSTEM_ERROR']
    return !noAutoCloseTypes.includes(type)
}

// 获取通知持续时间
const getNotificationDuration = (type: string): number => {
    const durationMap: Record<string, number> = {
        'FORCE_LOGOUT': 0, // 不自动关闭
        'SYSTEM_ERROR': 0,
        'SYSTEM_WARNING': 8000,
        'BUSINESS_ERROR': 6000,
        'BUSINESS_WARNING': 5000,
        'ADMIN_USER_ACTION': 3000
    }
    return durationMap[type] || 4000
}

// 显示弹窗通知
const showPopupNotification = (notification: NotificationMessage) => {
    notifications.value.push(notification)

    // 自动关闭
    if (notification.autoClose && notification.duration > 0) {
        setTimeout(() => {
            closeNotification(notification.id)
        }, notification.duration)
    }
}

// 关闭通知
const closeNotification = (id: string) => {
    const index = notifications.value.findIndex(item => item.id === id)
    if (index > -1) {
        notifications.value.splice(index, 1)
    }
}

// 切换消息中心
const toggleMessageCenter = () => {
    showMessageCenter.value = !showMessageCenter.value
}

// 标记消息为已读
const markAsRead = (id: string) => {
    const message = allMessages.value.find(msg => msg.id === id)
    if (message) {
        message.read = true
    }
}

// 标记所有消息为已读
const markAllAsRead = () => {
    allMessages.value.forEach(msg => {
        msg.read = true
    })
}

// 清空消息
const clearMessages = () => {
    allMessages.value = []
    notifications.value = []
}

// 判断是否显示详细数据
const shouldShowData = (notification: NotificationMessage): boolean => {
    return !!(notification.data && Object.keys(notification.data).length > 0)
}

// 格式化数据
const formatData = (data: any): string => {
    try {
        return JSON.stringify(data, null, 2)
    } catch {
        return String(data)
    }
}

// 格式化简单数据
const formatSimpleData = (data: any): string => {
    if (!data) return ''

    if (typeof data === 'object') {
        const keys = Object.keys(data).slice(0, 3)
        const preview = keys.map(key => `${key}: ${data[key]}`).join(', ')
        return preview + (Object.keys(data).length > 3 ? '...' : '')
    }

    return String(data).substring(0, 50) + (String(data).length > 50 ? '...' : '')
}

// 格式化时间
const formatTime = (timestamp: string): string => {
    try {
        const date = new Date(timestamp)
        const now = new Date()
        const diff = now.getTime() - date.getTime()

        if (diff < 60000) {
            return '刚刚'
        } else if (diff < 3600000) {
            return `${Math.floor(diff / 60000)}分钟前`
        } else if (diff < 86400000) {
            return `${Math.floor(diff / 3600000)}小时前`
        } else {
            return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5)
        }
    } catch {
        return timestamp
    }
}

// 播放提示音
const playNotificationSound = (type: string) => {
    if (!window.Audio) return

    try {
        const importantTypes = ['FORCE_LOGOUT', 'SYSTEM_ERROR', 'BUSINESS_ERROR']
        if (importantTypes.includes(type)) {
            // 播放重要提示音（可以添加音频文件）
            console.log('🔊 播放重要消息提示音')
        }
    } catch (error) {
        console.warn('⚠️ 播放提示音失败:', error)
    }
}

// 暴露方法给父组件
defineExpose({
    addMessage: (message: any) => handleWebSocketMessage(message),
    clearMessages,
    markAllAsRead
})
</script>

<style scoped>
.message-notification-container {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    z-index: 9999;
}

/* 弹窗通知列表 */
.notification-list {
    position: fixed;
    top: 20px;
    right: 20px;
    max-width: 400px;
    pointer-events: auto;
}

.notification-item {
    display: flex;
    align-items: flex-start;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    padding: 16px;
    margin-bottom: 12px;
    border-left: 4px solid #409EFF;
    transition: all 0.3s ease;
}

.notification-item:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
    transform: translateY(-2px);
}

.notification-info {
    border-left-color: #409EFF;
}

.notification-success {
    border-left-color: #67C23A;
}

.notification-warning {
    border-left-color: #E6A23C;
}

.notification-error {
    border-left-color: #F56C6C;
}

.notification-icon {
    margin-right: 12px;
    font-size: 20px;
    flex-shrink: 0;
}

.notification-content {
    flex: 1;
    min-width: 0;
}

.notification-title {
    font-weight: 600;
    font-size: 14px;
    margin-bottom: 4px;
    color: #303133;
}

.notification-message {
    font-size: 13px;
    color: #606266;
    line-height: 1.4;
    margin-bottom: 8px;
}

.notification-data {
    margin: 8px 0;
}

.notification-data pre {
    font-size: 11px;
    background: #f5f7fa;
    padding: 8px;
    border-radius: 4px;
    overflow-x: auto;
    max-height: 120px;
}

.notification-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 11px;
    color: #909399;
}

.notification-role {
    background: #f0f2f5;
    padding: 2px 6px;
    border-radius: 10px;
    font-size: 10px;
}

.notification-close {
    margin-left: 8px;
    flex-shrink: 0;
}

/* 消息指示器 */
.message-indicator {
    position: fixed;
    bottom: 100px;
    right: 30px;
    background: white;
    border-radius: 50%;
    padding: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    cursor: pointer;
    pointer-events: auto;
    transition: all 0.3s ease;
}

.message-indicator:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 消息中心 */
.message-center {
    height: 100%;
    display: flex;
    flex-direction: column;
}

.message-center-header {
    border-bottom: 1px solid #ebeef5;
    padding-bottom: 16px;
    margin-bottom: 16px;
}

.message-center-actions {
    margin-top: 12px;
    text-align: right;
}

.message-center-content {
    flex: 1;
    overflow-y: auto;
}

.message-item {
    padding: 12px;
    border-radius: 6px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
    border: 1px solid transparent;
}

.message-item:hover {
    background-color: #f5f7fa;
}

.message-unread {
    background-color: #f0f9ff;
    border-color: #409EFF;
}

.message-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
}

.message-type {
    font-size: 11px;
    padding: 2px 6px;
    border-radius: 10px;
    font-weight: 500;
}

.message-type-info {
    background: #e1f3ff;
    color: #409EFF;
}

.message-type-success {
    background: #f0f9ff;
    color: #67C23A;
}

.message-type-warning {
    background: #fdf6ec;
    color: #E6A23C;
}

.message-type-error {
    background: #fef0f0;
    color: #F56C6C;
}

.message-time {
    font-size: 11px;
    color: #909399;
}

.message-item-content {
    font-size: 13px;
    color: #606266;
    margin-bottom: 4px;
}

.message-item-data {
    font-size: 11px;
    color: #909399;
    background: #f5f7fa;
    padding: 4px 8px;
    border-radius: 4px;
}

.no-messages {
    text-align: center;
    padding: 40px 20px;
}

/* 动画效果 */
.notification-enter-active {
    transition: all 0.3s ease;
}

.notification-leave-active {
    transition: all 0.3s ease;
}

.notification-enter-from {
    opacity: 0;
    transform: translateX(100%);
}

.notification-leave-to {
    opacity: 0;
    transform: translateX(100%);
}

.notification-move {
    transition: transform 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .notification-list {
        top: 10px;
        right: 10px;
        left: 10px;
        max-width: none;
    }

    .notification-item {
        padding: 12px;
    }

    .message-indicator {
        bottom: 80px;
        right: 20px;
        padding: 10px;
    }
}
</style>