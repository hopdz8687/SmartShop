import axios from 'axios'
import { tokenStorage } from '@/lib/tokenStorage'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

let unauthorizedHandler = null

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  timeout: 15000,
})

apiClient.interceptors.request.use(
  (config) => {
    const token = tokenStorage.getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    const errorCode = data?.errorCode
    const requestUrl = error.config?.url || ''
    const hadAuthHeader = Boolean(error.config?.headers?.Authorization)

    const isAuthAttemptEndpoint =
      requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register')

    // Only clear token on 401 when an authenticated session token was sent and rejected
    // as UNAUTHORIZED (expired/invalid/revoked), not on login credential errors or business 401s.
    if (
      status === 401 &&
      hadAuthHeader &&
      !isAuthAttemptEndpoint &&
      (errorCode === 'UNAUTHORIZED' || requestUrl.includes('/users/me'))
    ) {
      tokenStorage.clearToken()
      if (typeof unauthorizedHandler === 'function') {
        unauthorizedHandler()
      }
    }

    return Promise.reject(normalizeApiError(error))
  },
)

export function normalizeApiError(error) {
  if (!error.response) {
    return {
      status: 0,
      errorCode: 'NETWORK_ERROR',
      message: 'Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng.',
      fieldErrors: null,
    }
  }

  const { status, data } = error.response
  return {
    status,
    errorCode: data?.errorCode || 'REQUEST_FAILED',
    message: data?.message || 'Đã xảy ra lỗi. Vui lòng thử lại.',
    fieldErrors: data?.errors || null,
  }
}
