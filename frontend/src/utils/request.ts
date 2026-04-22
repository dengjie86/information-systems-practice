import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

function handleUnauthorized(msg?: string) {
  useUserStore().logout()
  router.push('/login')
  ElMessage.warning(msg || '登录已失效，请重新登录')
}

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
    const { code, data, msg } = res.data ?? {}
    if (code === 200) return data
    if (code === 401) {
      handleUnauthorized(msg)
      return Promise.reject(new Error(msg))
    }
    ElMessage.error(msg || '接口异常')
    return Promise.reject(new Error(msg || 'Error'))
  },
  err => {
    if (err?.response?.status === 401) {
      handleUnauthorized()
      return Promise.reject(err)
    }
    ElMessage.error(err?.response?.data?.msg || err?.message || '网络异常')
    return Promise.reject(err)
  }
)

export default request
