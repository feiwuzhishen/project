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

export const getUserAssetConfig = (userId) => {
  return api.get(`/user/${userId}/asset`)
}

export const getUserStockHolds = (userId) => {
  return api.get(`/user/${userId}/stocks`)
}

export const getUserDetail = (userId) => {
  return api.get(`/user/${userId}/detail`)
}

export const getAllUsers = () => {
  return api.get('/user/list')
}
