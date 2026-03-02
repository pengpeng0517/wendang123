import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = ref(JSON.parse(userInfoStr === 'undefined' ? 'null' : userInfoStr || 'null'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const getToken = computed(() => token.value)
  const getUserInfo = computed(() => userInfo.value)

  // 操作
  /**
   * 用户登录
   */
  const handleLogin = async (username, password) => {
    try {
      console.log('userStore.handleLogin 被调用，用户名:', username)
      const data = await login({ username, password })
      console.log('login API 返回结果:', data)
      
      // 保存token和用户信息
      token.value = data.data.token
      userInfo.value = data.data.userInfo
      console.log('保存用户信息成功:', { token: token.value, userInfo: userInfo.value })
      
      // 存储到localStorage
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('userInfo', JSON.stringify(data.data.userInfo || null))
      localStorage.setItem('userRole', data.data.userInfo?.role || '')
      console.log('存储到localStorage成功')
      
      return Promise.resolve(data)
    } catch (error) {
      console.error('userStore.handleLogin 失败:', error)
      return Promise.reject(error)
    }
  }

  /**
   * 用户登出
   */
  const handleLogout = () => {
    // 清除状态
    token.value = ''
    userInfo.value = null
    
    // 清除localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userRole')
    
    // 跳转到登录页
    router.push('/login')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    getToken,
    getUserInfo,
    handleLogin,
    handleLogout
  }
})