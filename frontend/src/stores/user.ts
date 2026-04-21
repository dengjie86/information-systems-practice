import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<Record<string, any>>({})

  const setToken = (val: string) => {
    token.value = val
    localStorage.setItem('token', val)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
  }

  return { token, userInfo, setToken, logout }
})
