import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    adminName: localStorage.getItem('admin_name') || '管理员'
  }),
  actions: {
    setAuth(token, adminName) {
      this.token = token
      this.adminName = adminName
      localStorage.setItem('admin_token', token)
      localStorage.setItem('admin_name', adminName)
    },
    logout() {
      this.token = ''
      this.adminName = ''
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_name')
    }
  }
})
