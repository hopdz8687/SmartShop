import { apiClient } from '@/services/apiClient'

export const catalogApi = {
  // Public
  async getPublicCategories() {
    const res = await apiClient.get('/categories')
    return res.data
  },

  async getPublicProducts({ page = 0, size = 12 } = {}) {
    const res = await apiClient.get('/products', { params: { page, size } })
    return res.data
  },

  async searchPublicProducts({
    keyword,
    categoryId,
    storeId,
    minPrice,
    maxPrice,
    sort = 'newest',
    page = 0,
    size = 12,
  } = {}) {
    const params = { sort, page, size }
    if (keyword) params.keyword = keyword
    if (categoryId) params.categoryId = categoryId
    if (storeId) params.storeId = storeId
    if (minPrice !== '' && minPrice !== undefined && minPrice !== null) {
      params.minPrice = minPrice
    }
    if (maxPrice !== '' && maxPrice !== undefined && maxPrice !== null) {
      params.maxPrice = maxPrice
    }
    const res = await apiClient.get('/products/search', { params })
    return res.data
  },

  async getPublicProductDetail(id) {
    const res = await apiClient.get(`/products/${id}`)
    return res.data
  },

  // Admin Categories
  async getAdminCategories() {
    const res = await apiClient.get('/admin/categories')
    return res.data
  },

  async createCategory(payload) {
    const res = await apiClient.post('/admin/categories', payload)
    return res.data
  },

  async updateCategory(id, payload) {
    const res = await apiClient.put(`/admin/categories/${id}`, payload)
    return res.data
  },

  async deleteCategory(id) {
    const res = await apiClient.delete(`/admin/categories/${id}`)
    return res.data
  },

  // Seller Products
  async getSellerProducts({ page = 0, size = 10 } = {}) {
    const res = await apiClient.get('/seller/products', { params: { page, size } })
    return res.data
  },

  async getSellerProductDetail(id) {
    const res = await apiClient.get(`/seller/products/${id}`)
    return res.data
  },

  async createSellerProduct(payload) {
    const res = await apiClient.post('/seller/products', payload)
    return res.data
  },

  async updateSellerProduct(id, payload) {
    const res = await apiClient.put(`/seller/products/${id}`, payload)
    return res.data
  },

  async deleteSellerProduct(id) {
    const res = await apiClient.delete(`/seller/products/${id}`)
    return res.data
  },

  // Seller Variants
  async addSellerVariant(productId, payload) {
    const res = await apiClient.post(`/seller/products/${productId}/variants`, payload)
    return res.data
  },

  async updateSellerVariant(variantId, payload) {
    const res = await apiClient.put(`/seller/variants/${variantId}`, payload)
    return res.data
  },

  async deactivateSellerVariant(variantId) {
    const res = await apiClient.delete(`/seller/variants/${variantId}`)
    return res.data
  },

  // Seller Images
  async addSellerImage(productId, payload) {
    const res = await apiClient.post(`/seller/products/${productId}/images`, payload)
    return res.data
  },

  async deleteSellerImage(imageId) {
    const res = await apiClient.delete(`/seller/images/${imageId}`)
    return res.data
  },
}
