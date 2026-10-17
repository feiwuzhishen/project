import axios from 'axios'

let isBackendAvailable = true
const errorCountMap = new Map()
const MAX_ERROR_LOGS = 3

const api = axios.create({
  baseURL: '/api',
  timeout: 5000
})

api.interceptors.response.use(
  response => {
    isBackendAvailable = true
    return response.data
  },
  error => {
    if (error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED') {
      isBackendAvailable = false
    }
    
    const errorKey = `${error.config?.method}-${error.config?.url}`
    const currentCount = errorCountMap.get(errorKey) || 0
    
    if (currentCount < MAX_ERROR_LOGS) {
      console.warn(`API Warning (${currentCount + 1}/${MAX_ERROR_LOGS}):`, error.message || error)
      errorCountMap.set(errorKey, currentCount + 1)
    }
    
    return Promise.reject(error)
  }
)

export const isBackendConnected = () => isBackendAvailable

export const getNotifications = (userId, limit = 20) => {
  return api.get('/notification/list', { params: { userId, limit } })
}

export const getUnreadCount = (userId) => {
  return api.get('/notification/unread-count', { params: { userId } })
}

export const markAsRead = (notificationId, userId) => {
  return api.post(`/notification/read/${notificationId}`, null, { params: { userId } })
}

export const markAllAsRead = (userId) => {
  return api.post('/notification/read-all', null, { params: { userId } })
}

export const createNotification = (userId, type, title, content) => {
  return api.post('/notification/create', { userId, type, title, content })
}

export const deleteNotification = (notificationId, userId) => {
  return api.delete(`/notification/${notificationId}`, { params: { userId } })
}
