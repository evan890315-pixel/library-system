import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user  = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

  async function register(payload) {
    const res = await authApi.register(payload)
    return res.data
  }

  async function login(payload) {
    const res = await authApi.login(payload)
    if (res.data.success) {
      token.value = res.data.data.token
      user.value  = {
        userId:      res.data.data.userId,
        userName:    res.data.data.userName,
        phoneNumber: res.data.data.phoneNumber
      }
      localStorage.setItem('token', token.value)
      localStorage.setItem('user',  JSON.stringify(user.value))
    }
    return res.data
  }

  function logout() {
    token.value = ''
    user.value  = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, register, login, logout }
})
