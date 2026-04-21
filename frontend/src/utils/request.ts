import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  err => Promise.reject(err)
)

request.interceptors.response.use(
  res => {
    const { code, data, msg } = res.data
    if (code === 200) return data
    
    ElMessage.error(msg || '接口异常')
    return Promise.reject(new Error(msg || 'Error'))
  },
  err => {
    ElMessage.error(err?.response?.data?.msg || err?.message || '网络异常')
    return Promise.reject(err)
  }
)

export default request
