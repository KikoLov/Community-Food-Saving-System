import axios from 'axios'
import { Message } from '@/utils/message'
import { useUserStore } from '@/store/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// Request interceptor
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = 'Bearer ' + userStore.token
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code === 200) {
      return res
    } else if (res.code === 429) {
      Message.warning(res.msg || '请求过于频繁，请稍后再试')
      return Promise.reject(new Error(res.msg || '请求过于频繁'))
    } else if (res.code === 401) {
      Message.error('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logoutAction()
      router.push('/login')
      return Promise.reject(new Error(res.msg || '未授权'))
    } else if (res.code === 403) {
      Message.error('没有权限访问')
      return Promise.reject(new Error(res.msg || '禁止访问'))
    } else {
      Message.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    console.error('Response error:', error)
    const status = error.response?.status
    const data = error.response?.data
    const backendMsg =
      data && typeof data === 'object' && typeof data.msg === 'string'
        ? data.msg
        : null
    const reqUrl = error.config?.url || ''

    // 后端对错误常返回 HTTP 4xx + JSON { code, msg }，需优先展示 msg，避免只显示 “Request failed with status code 401”
    if (status === 401) {
      // 登录/注册失败：只提示原因，不清 token、不跳转
      if (reqUrl.includes('/auth/login') || reqUrl.includes('/auth/register')) {
        const text = backendMsg || '用户名或密码错误'
        Message.error(text)
        return Promise.reject(new Error(text))
      }
      Message.error(backendMsg || '登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logoutAction()
      router.push('/login')
      return Promise.reject(new Error(backendMsg || '未授权'))
    }

    if (status === 403) {
      const text = backendMsg || '没有权限访问'
      Message.error(text)
      return Promise.reject(new Error(text))
    }

    if (status === 429) {
      const text = backendMsg || '请求过于频繁，请稍后再试'
      Message.warning(text)
      return Promise.reject(new Error(text))
    }

    const text = backendMsg || error.message || '网络错误'
    Message.error(text)
    return Promise.reject(new Error(text))
  }
)

export default request
