import { apiClient } from '@/services/apiClient'

export const orderApi = {
  async checkout(payload) {
    const res = await apiClient.post('/orders', payload)
    return res.data
  },

  async getMyOrders({ page = 0, size = 10 } = {}) {
    const res = await apiClient.get('/orders/my', { params: { page, size } })
    return res.data
  },

  async getOrderDetail(id) {
    const res = await apiClient.get(`/orders/${id}`)
    return res.data
  },
}
