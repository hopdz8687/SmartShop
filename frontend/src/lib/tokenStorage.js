const TOKEN_STORAGE_KEY = 'smartshop_access_token'

export const tokenStorage = {
  getToken() {
    try {
      return window.localStorage.getItem(TOKEN_STORAGE_KEY)
    } catch {
      return null
    }
  },

  setToken(token) {
    if (!token) return
    try {
      window.localStorage.setItem(TOKEN_STORAGE_KEY, token)
    } catch {
      // Ignore storage quota errors
    }
  },

  clearToken() {
    try {
      window.localStorage.removeItem(TOKEN_STORAGE_KEY)
    } catch {
      // Ignore storage errors
    }
  },
}
