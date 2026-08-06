import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../services/api'
import { useRouter } from 'vue-router'

export const useAdminStore = defineStore('admin', () => {
  const router = useRouter()
  const error = ref<string>('')
  const toast = ref<string>('')

  let toastTimer: number | undefined
  function notify(message: string) {
    toast.value = message
    if (toastTimer) clearTimeout(toastTimer)
    toastTimer = window.setTimeout(() => (toast.value = ''), 3000)
  }

  async function login(mobile?: string, password?: string) {
    try {
      await api.login(mobile, password)
      error.value = ''
      router.push('/dashboard')
    } catch (e) {
      error.value = e instanceof Error ? e.message : '登录失败'
      notify(error.value)
    }
  }

  function logout() {
    api.clearSession()
    router.push('/login')
  }

  return { error, toast, notify, login, logout }
})
