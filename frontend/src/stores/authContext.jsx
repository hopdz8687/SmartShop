import { useCallback, useEffect, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { authApi } from '@/features/auth/api/authApi'
import { tokenStorage } from '@/lib/tokenStorage'
import { setUnauthorizedHandler } from '@/services/apiClient'
import { AuthContext } from '@/stores/authContextObject'

const AUTH_ME_QUERY_KEY = ['auth', 'me']

export function AuthProvider({ children }) {
  const queryClient = useQueryClient()
  const [token, setToken] = useState(() => tokenStorage.getToken())

  const clearSession = useCallback(() => {
    tokenStorage.clearToken()
    setToken(null)
    queryClient.removeQueries({ queryKey: AUTH_ME_QUERY_KEY })
  }, [queryClient])

  useEffect(() => {
    setUnauthorizedHandler(clearSession)
    return () => setUnauthorizedHandler(null)
  }, [clearSession])

  const meQuery = useQuery({
    queryKey: [...AUTH_ME_QUERY_KEY, token],
    queryFn: async () => {
      try {
        const response = await authApi.getCurrentUser()
        return response.data
      } catch (error) {
        if (error?.status === 401 || error?.status === 403) {
          clearSession()
        }
        throw error
      }
    },
    enabled: Boolean(token),
    retry: false,
    staleTime: 5 * 60 * 1000,
  })

  const setSession = useCallback(
    (accessToken, user) => {
      tokenStorage.setToken(accessToken)
      setToken(accessToken)
      if (user) {
        queryClient.setQueryData([...AUTH_ME_QUERY_KEY, accessToken], user)
      }
    },
    [queryClient],
  )

  const logout = useCallback(async () => {
    try {
      if (tokenStorage.getToken()) {
        await authApi.logout()
      }
    } catch {
      // Stateless JWT: client must discard token even if logout request fails
    } finally {
      clearSession()
    }
  }, [clearSession])

  const value = {
    user: meQuery.data ?? null,
    token,
    isAuthenticated: Boolean(token && meQuery.data),
    isLoading: Boolean(token) && meQuery.isLoading,
    authError: meQuery.error ?? null,
    setSession,
    logout,
    refetchUser: meQuery.refetch,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
