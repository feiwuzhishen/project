<template>
  <a-layout class="app-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      width="220"
      class="sider"
    >
      <div class="logo">
        <h1 v-if="!collapsed">金融学习平台</h1>
        <h1 v-else>金融</h1>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="light"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-menu-item key="/">
          <DashboardOutlined />
          <span>仪表盘</span>
        </a-menu-item>
        <a-menu-item key="/learn">
          <BookOutlined />
          <span>学习中心</span>
        </a-menu-item>
        <a-menu-item key="/quiz">
          <QuestionCircleOutlined />
          <span>风险测评</span>
        </a-menu-item>
        <a-menu-item key="/invest">
          <StockOutlined />
          <span>模拟投资</span>
        </a-menu-item>
        <a-menu-item key="/world">
          <GlobalOutlined />
          <span>全球市场</span>
        </a-menu-item>
        <a-menu-item key="/community">
          <TeamOutlined />
          <span>社区</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="header">
        <menu-unfold-outlined
          v-if="collapsed"
          class="trigger"
          @click="() => (collapsed = !collapsed)"
        />
        <menu-fold-outlined
          v-else
          class="trigger"
          @click="() => (collapsed = !collapsed)"
        />
        <div class="header-right">
          <a-dropdown :trigger="['click']" @openChange="handleNotificationDropdownVisible">
            <a-badge :count="notificationStore.unreadCount" :offset="[-5, 5]" :overflow-count="99">
              <bell-outlined class="bell-icon" />
            </a-badge>
            <template #overlay>
              <a-menu class="notification-menu">
                <a-menu-item-group title="通知列表">
                  <a-menu-item v-for="item in notificationStore.notifications" :key="item.id" @click="handleNotificationClick(item)">
                    <div class="notification-item" :class="{ unread: !item.isRead }">
                      <div class="notification-header">
                        <a-tag :color="getNotificationColor(item.type)" size="small">{{ getNotificationTypeText(item.type) }}</a-tag>
                        <span class="notification-time">{{ formatTime(item.createdAt) }}</span>
                      </div>
                      <div class="notification-title">{{ item.title }}</div>
                      <div v-if="item.content" class="notification-content">{{ item.content }}</div>
                    </div>
                  </a-menu-item>
                  <a-menu-item v-if="notificationStore.notifications.length === 0">
                    <div class="notification-empty">暂无新通知</div>
                  </a-menu-item>
                </a-menu-item-group>
                <a-menu-divider />
                <a-menu-item v-if="notificationStore.unreadCount > 0" @click="handleMarkAllRead">
                  <div class="mark-all-read">全部标记已读</div>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-dropdown>
            <div class="user-info">
              <a-avatar :src="userStore.avatar" :style="{ backgroundColor: userStore.avatar ? 'transparent' : '#1890ff' }">
                <template v-if="!userStore.avatar"><user-outlined /></template>
              </a-avatar>
              <span class="username">{{ currentUsername }}</span>
            </div>
            <template #overlay>
              <a-menu @click="handleUserMenuClick">
                <a-menu-item key="profile">个人中心</a-menu-item>
                <a-menu-item key="settings">设置</a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>

    <FloatingBall />
  </a-layout>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import FloatingBall from './FloatingBall.vue'
import { useUserStore } from '@/stores/userStore'
import { useNotificationStore } from '@/stores/notificationStore'
import {
  DashboardOutlined,
  BookOutlined,
  QuestionCircleOutlined,
  StockOutlined,
  GlobalOutlined,
  TeamOutlined,
  BellOutlined,
  UserOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const collapsed = ref(false)
const selectedKeys = ref([route.path])

const currentUsername = computed(() => userStore.nickname || '用户')

watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
})

onMounted(() => {
  userStore.checkAuth()
  if (userStore.userId) {
    notificationStore.startPolling(userStore.userId, 30000)
  }
})

onUnmounted(() => {
  notificationStore.stopPolling()
})

const handleMenuClick = ({ key }) => {
  router.push(key)
}

const handleUserMenuClick = ({ key }) => {
  if (key === 'profile') {
    router.push('/profile')
  } else if (key === 'settings') {
    router.push('/settings')
  } else if (key === 'logout') {
    userStore.logout()
    notificationStore.reset()
    message.success('已退出登录')
    router.push('/login')
  }
}

const handleLogout = () => {
  userStore.logout()
  notificationStore.reset()
  message.success('已退出登录')
  router.push('/login')
}

const handleNotificationDropdownVisible = (visible) => {
  if (visible && userStore.userId) {
    notificationStore.fetchNotifications(userStore.userId)
  }
}

const handleNotificationClick = async (item) => {
  if (!item.isRead && userStore.userId) {
    await notificationStore.markNotificationAsRead(item.id, userStore.userId)
  }
}

const handleMarkAllRead = async () => {
  if (userStore.userId) {
    const res = await notificationStore.markAllNotificationsAsRead(userStore.userId)
    if (res.success) {
      message.success('已全部标记为已读')
    }
  }
}

const getNotificationColor = (type) => {
  const colors = {
    system: 'blue',
    message: 'green',
    warning: 'orange',
    notice: 'purple'
  }
  return colors[type] || 'default'
}

const getNotificationTypeText = (type) => {
  const texts = {
    system: '系统',
    message: '消息',
    warning: '警告',
    notice: '公告'
  }
  return texts[type] || '通知'
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.sider {
  background: #ffffff !important;
  border-right: 1px solid #e8e8e8;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo h1 {
  color: #1a1a1a;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header {
  background: #ffffff !important;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.trigger {
  font-size: 18px;
  color: #1a1a1a;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.bell-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
}

.notification-menu {
  max-height: 400px;
  overflow-y: auto;
  min-width: 300px;
}

.notification-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 0;
}

.notification-item.unread {
  background: #f0f5ff;
  margin: -4px -12px;
  padding: 8px 12px;
  border-radius: 4px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-title {
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 500;
}

.notification-content {
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 250px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-empty {
  text-align: center;
  color: #999;
  padding: 12px;
}

.mark-all-read {
  text-align: center;
  color: #1890ff;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.username {
  color: #1a1a1a;
}

.content {
  margin: 24px;
  padding: 24px;
  background: #ffffff;
  border-radius: 8px;
  min-height: 280px;
}
</style>
