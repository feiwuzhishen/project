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

export const getUserInfo = (userId) => {
  return api.get(`/user/${userId}`)
}

export const getUserDetail = (userId) => {
  return api.get(`/user/${userId}/detail`)
}

export const updateUserInfo = (userId, data) => {
  return api.put(`/user/${userId}`, data)
}

export const uploadAvatar = (file, userId) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userId)
  
  return axios.post('/api/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 30000
  }).then(response => response.data)
}

export const getAllUsers = () => {
  return api.get('/user/list')
}
