import { apiClient } from '@/services/apiClient'

export const cartApi = {
  async getCart() {
    const res = await apiClient.get('/cart')
    return res.data
  },

  async addItem(payload) {
    const res = await apiClient.post('/cart/items', payload)
    return res.data
  },

  async updateItem(id, payload) {
    const res = await apiClient.put(`/cart/items/${id}`, payload)
    return res.data
  },

  async removeItem(id) {
    const res = await apiClient.delete(`/cart/items/${id}`)
    return res.data
  },
}
