import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const getMatchedPartners = (userId) => {
  return api.get('/partner/match', { params: { userId } })
}

export const sendPartnerRequest = (userId, partnerId) => {
  return api.post('/partner/request', { userId, partnerId })
}

export const acceptPartnerRequest = (id) => {
  return api.post(`/partner/accept/${id}`)
}

export const rejectPartnerRequest = (id) => {
  return api.delete(`/partner/reject/${id}`)
}

export const getPartnerList = (userId) => {
  return api.get('/partner/list', { params: { userId } })
}

export const getPendingRequests = (userId) => {
  return api.get('/partner/pending', { params: { userId } })
}

export const sendMessage = (senderId, receiverId, content) => {
  return api.post('/partner/message', { senderId, receiverId, content })
}

export const getMessages = (userId, partnerId) => {
  return api.get('/partner/messages', { params: { userId, partnerId } })
}

export const getUnreadCount = (userId) => {
  return api.get('/partner/unread-count', { params: { userId } })
}
