import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('社区API错误:', error)
    return Promise.reject(error)
  }
)

export const createPost = (data) => {
  return api.post('/community/post', data)
}

export const getPosts = (page = 1, size = 10, userId = null) => {
  const params = { page, size }
  if (userId) params.userId = userId
  return api.get('/community/posts', { params })
}

export const getLatestPosts = (since = null, userId = null) => {
  const params = {}
  if (since) params.since = since
  if (userId) params.userId = userId
  return api.get('/community/posts/latest', { params })
}

export const addComment = (postId, data) => {
  return api.post(`/community/post/${postId}/comment`, data)
}

export const getComments = (postId) => {
  return api.get(`/community/post/${postId}/comments`)
}

export const likePost = (postId, userId) => {
  return api.post(`/community/post/${postId}/like`, { userId })
}

export const unlikePost = (postId, userId) => {
  return api.delete(`/community/post/${postId}/like`, { params: { userId } })
}
