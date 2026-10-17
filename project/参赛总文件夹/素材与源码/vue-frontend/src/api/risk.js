import axios from 'axios'

const API_BASE = '/api/risk'

export const riskApi = {
  getUserRiskProfile(userId) {
    return axios.get(`${API_BASE}/profile/${userId}`).then(res => res.data)
  },

  setUserRiskProfile(data) {
    return axios.post(`${API_BASE}/profile`, data).then(res => res.data)
  },

  checkStockRiskMatch(userId, stockCode) {
    return axios.get(`${API_BASE}/check/${userId}/${stockCode}`).then(res => res.data)
  },

  checkPortfolioRisk(userId) {
    return axios.get(`${API_BASE}/portfolio/${userId}`).then(res => res.data)
  },

  getRiskTypes() {
    return axios.get(`${API_BASE}/types`).then(res => res.data)
  }
}
