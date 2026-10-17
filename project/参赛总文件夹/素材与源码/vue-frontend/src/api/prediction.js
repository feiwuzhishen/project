import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('预测API错误:', error)
    return Promise.reject(error)
  }
)

export const createPrediction = (userId, stockCode, predictionType) => {
  return api.post('/prediction/create', null, {
    params: { userId, stockCode, predictionType }
  })
}

export const getUserPredictions = (userId) => {
  return api.get(`/prediction/user/${userId}`)
}

export const getPredictionAccuracy = (userId) => {
  return api.get(`/prediction/accuracy/${userId}`)
}

export const checkPredictionResult = (predictionId) => {
  return api.post('/prediction/check', null, {
    params: { predictionId }
  })
}

export const analyzeTrend = (stockCode) => {
  return api.get(`/prediction/analyze/${stockCode}`)
}
