import { apiClient } from '@/services/apiClient'

export const authApi = {
  async register(payload) {
    const { data } = await apiClient.post('/auth/register', payload)
    return data
  },

  async login(payload) {
    const { data } = await apiClient.post('/auth/login', payload)
    return data
  },

  async logout() {
    const { data } = await apiClient.post('/auth/logout')
    return data
  },

  async getCurrentUser() {
    const { data } = await apiClient.get('/users/me')
    return data
  },
}
