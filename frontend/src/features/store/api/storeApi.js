import { apiClient } from '@/services/apiClient'

export const storeApi = {
  async submitSellerApplication(payload) {
    const response = await apiClient.post('/seller-applications', payload)
    return response.data
  },

  async getMySellerApplications() {
    const response = await apiClient.get('/seller-applications/me')
    return response.data
  },

  async cancelSellerApplication(id) {
    const response = await apiClient.put(`/seller-applications/${id}/cancel`)
    return response.data
  },

  async getAdminSellerApplications({ status, page = 0, size = 10 } = {}) {
    const params = { page, size }
    if (status) {
      params.status = status
    }
    const response = await apiClient.get('/admin/seller-applications', { params })
    return response.data
  },

  async getAdminSellerApplicationById(id) {
    const response = await apiClient.get(`/admin/seller-applications/${id}`)
    return response.data
  },

  async approveSellerApplication(id) {
    const response = await apiClient.put(`/admin/seller-applications/${id}/approve`)
    return response.data
  },

  async rejectSellerApplication(id, payload) {
    const response = await apiClient.put(`/admin/seller-applications/${id}/reject`, payload)
    return response.data
  },

  async getMyStore() {
    const response = await apiClient.get('/seller/stores/me')
    return response.data
  },

  async updateMyStore(payload) {
    const response = await apiClient.put('/seller/stores/me', payload)
    return response.data
  },
}
