import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type Role = 'STUDENT' | 'ADMIN' | 'WORKER'

export interface UserInfo {
  id: number
  username: string
  realName: string
  role: Role
  phone?: string
  dormBuilding?: string
  dormRoom?: string
  status?: number
}

const STORAGE_KEY = {
  token: 'token',
  user: 'userInfo',
} as const

function loadUserInfo(): UserInfo | null {
  const raw = localStorage.getItem(STORAGE_KEY.user)
  if (!raw) return null
  try { return JSON.parse(raw) } catch { return null }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(STORAGE_KEY.token) || '')
  const userInfo = ref<UserInfo | null>(loadUserInfo())

  const role = computed(() => userInfo.value?.role ?? null)
  const isLoggedIn = computed(() => !!token.value)

  const setToken = (val: string) => {
    token.value = val
    localStorage.setItem(STORAGE_KEY.token, val)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem(STORAGE_KEY.user, JSON.stringify(info))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(STORAGE_KEY.token)
    localStorage.removeItem(STORAGE_KEY.user)
  }

  return { token, userInfo, role, isLoggedIn, setToken, setUserInfo, logout }
})
