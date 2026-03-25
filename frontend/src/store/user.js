import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, logout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const userType = ref(parseInt(localStorage.getItem('userType')) || null)

  const isLoggedIn = computed(() => !!token.value)
  const isConsumer = computed(() => userType.value === 1)
  const isMerchant = computed(() => userType.value === 2)
  const isAdmin = computed(() => userType.value === 3)

  async function loginAction(username, password) {
    const res = await login(username, password)
    token.value = res.data
    localStorage.setItem('token', res.data)

    // Get user info
    await getUserInfoAction()
    return res
  }

  async function registerAction(userData) {
    const res = await register(userData)
    return res
  }

  async function getUserInfoAction() {
    const res = await getUserInfo()
    userInfo.value = res.data
    userType.value = res.data.userType
    localStorage.setItem('userType', res.data.userType)
    return res
  }

  function logoutAction() {
    token.value = ''
    userInfo.value = null
    userType.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userType')
  }

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  return {
    token,
    userInfo,
    userType,
    isLoggedIn,
    isConsumer,
    isMerchant,
    isAdmin,
    loginAction,
    registerAction,
    getUserInfoAction,
    logoutAction,
    setToken
  }
})
