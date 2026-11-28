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

  async getSellerOrders({ page = 0, size = 10 } = {}) {
    const res = await apiClient.get('/seller/orders', { params: { page, size } })
    return res.data
  },

  async getSellerOrderDetail(id) {
    const res = await apiClient.get(`/seller/orders/${id}`)
    return res.data
  },

  async confirmSellerOrder(id) {
    const res = await apiClient.put(`/seller/orders/${id}/confirm`)
    return res.data
  },

  async startProcessingSellerOrder(id) {
    const res = await apiClient.put(`/seller/orders/${id}/processing`)
    return res.data
  },
}
