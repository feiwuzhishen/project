import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('投资分析API错误:', error)
    return Promise.reject(error)
  }
)

export const getFundamentalAnalysis = (stockCode) => {
  return api.get(`/analysis/fundamental/${stockCode}`)
}

export const getTechnicalAnalysis = (stockCode) => {
  return api.get(`/analysis/technical/${stockCode}`)
}

export const getInvestmentSuggestion = (stockCode) => {
  return api.get(`/analysis/suggestion/${stockCode}`)
}

export const getTradeReasonAnalysis = (data) => {
  return api.post('/analysis/trade-reason', data)
}
