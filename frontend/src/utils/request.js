import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      // 设置Authorization请求头
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 检查响应状态码
    if (res.code !== 200) {
      // 显示错误消息
      ElMessage.error(res.message || '操作失败')
      
      // 如果是401未授权，跳转到登录页
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.handleLogout()
      }
      
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    
    return res
  },
  error => {
    // 响应错误处理
    console.error('响应错误:', error)
    
    // 显示错误消息
    let message = '网络错误，请稍后重试'
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      if (data && data.message) {
        message = data.message
      } else {
        switch (status) {
          case 400:
            message = '请求参数错误'
            break
          case 401:
            message = '未授权，请重新登录'
            const userStore = useUserStore()
            userStore.handleLogout()
            break
          case 403:
            message = '拒绝访问'
            break
          case 404:
            message = '请求的资源不存在'
            break
          case 500:
            message = '服务器内部错误'
            break
          default:
            message = `请求失败 (${status})`
        }
      }
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request