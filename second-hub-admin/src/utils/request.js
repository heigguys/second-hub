import axios from 'axios'

const service = axios.create({
  baseURL: 'http://127.0.0.1:8080',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use((res) => {
  const body = res.data || {}
  if (body.code === 0) {
    return body.data
  }
  return Promise.reject(new Error(body.message || '请求失败'))
})

export default service
