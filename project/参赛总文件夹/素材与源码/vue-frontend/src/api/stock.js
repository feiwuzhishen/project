import axios from 'axios'

const createApi = (signal) => axios.create({
  baseURL: '/api',
  timeout: 5000,
  signal
})

const handleResponse = (response) => {
  const data = response.data
  return {
    ...data,
    success: data.code === 200
  }
}

const handleError = (error) => {
  if (error.name === 'CanceledError' || error.code === 'ERR_CANCELED') {
    console.log('请求已取消')
    return Promise.reject({ canceled: true, message: '请求已取消' })
  }
  console.error('股票API错误:', error)
  return Promise.reject(error)
}

export const getStockList = (codes, signal) => {
  const api = createApi(signal)
  return api.get('/stock/list', {
    params: { codes: codes.join(',') }
  }).then(handleResponse).catch(handleError)
}

export const searchStock = (keyword, signal) => {
  const api = createApi(signal)
  return api.get('/stock/search', {
    params: { keyword }
  }).then(handleResponse).catch(handleError)
}

export const getStockRealtime = (code, signal) => {
  const api = createApi(signal)
  return api.get(`/stock/realtime/${code}`).then(handleResponse).catch(handleError)
}
