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

export const getUserHonor = (userId) => {
  return api.get(`/honor/user/${userId}`)
}

export const addHonor = (userId, honorType) => {
  return api.post('/honor/add', { userId, honorType })
}

export const addHonorForPost = (userId) => {
  return api.post(`/honor/post/${userId}`)
}

export const addHonorForComment = (userId) => {
  return api.post(`/honor/comment/${userId}`)
}

export const addHonorForLiked = (userId) => {
  return api.post(`/honor/liked/${userId}`)
}

export const addHonorForConsecutiveLogin = (userId) => {
  return api.post(`/honor/login/${userId}`)
}

export const initUserHonor = (userId) => {
  return api.post(`/honor/init/${userId}`)
}
