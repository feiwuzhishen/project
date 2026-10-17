import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead, isBackendConnected } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)
  let pollTimer = null

  const storeErrorCounts = new Map()
  const MAX_STORE_ERRORS = 3

  const logError = (key, error) => {
    const count = storeErrorCounts.get(key) || 0
    if (count < MAX_STORE_ERRORS) {
      console.warn(`${key} (${count + 1}/${MAX_STORE_ERRORS}):`, error.message || error)
      storeErrorCounts.set(key, count + 1)
    }
  }

  const hasUnread = computed(() => unreadCount.value > 0)

  const fetchNotifications = async (userId, limit = 20) => {
    if (!isBackendConnected()) return
    
    loading.value = true
    try {
      const res = await getNotifications(userId, limit)
      if (res.success) {
        notifications.value = res.data
      }
    } catch (error) {
      logError('获取通知失败', error)
    } finally {
      loading.value = false
    }
  }

  const fetchUnreadCount = async (userId) => {
    if (!isBackendConnected()) return
    
    try {
      const res = await getUnreadCount(userId)
      if (res.success) {
        unreadCount.value = res.count
      }
    } catch (error) {
      logError('获取未读数量失败', error)
    }
  }

  const markNotificationAsRead = async (notificationId, userId) => {
    try {
      const res = await markAsRead(notificationId, userId)
      if (res.success) {
        const notification = notifications.value.find(n => n.id === notificationId)
        if (notification) {
          notification.isRead = true
        }
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
      return res
    } catch (error) {
      logError('标记已读失败', error)
      return { success: false, error: error.message }
    }
  }

  const markAllNotificationsAsRead = async (userId) => {
    try {
      const res = await markAllAsRead(userId)
      if (res.success) {
        notifications.value.forEach(n => n.isRead = true)
        unreadCount.value = 0
      }
      return res
    } catch (error) {
      logError('全部标记已读失败', error)
      return { success: false, error: error.message }
    }
  }

  const startPolling = (userId, interval = 30000) => {
    stopPolling()
    fetchUnreadCount(userId)
    fetchNotifications(userId)
    pollTimer = setInterval(() => {
      fetchUnreadCount(userId)
      fetchNotifications(userId)
    }, interval)
  }

  const stopPolling = () => {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  const reset = () => {
    stopPolling()
    notifications.value = []
    unreadCount.value = 0
  }

  return {
    notifications,
    unreadCount,
    loading,
    hasUnread,
    fetchNotifications,
    fetchUnreadCount,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    startPolling,
    stopPolling,
    reset
  }
})
