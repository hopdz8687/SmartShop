import { apiClient } from '@/services/apiClient'

export const orderApi = {
  async checkout(payload) {
    const res = await apiClient.post('/orders', payload)
    return res.data
  },
}
