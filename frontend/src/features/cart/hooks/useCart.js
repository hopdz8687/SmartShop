import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/hooks/useAuth'
import { cartApi } from '@/features/cart/api/cartApi'

export const CART_QUERY_KEY = ['cart']

export function useCartQuery() {
  const { isAuthenticated } = useAuth()

  return useQuery({
    queryKey: CART_QUERY_KEY,
    queryFn: async () => {
      const res = await cartApi.getCart()
      return res.data
    },
    enabled: isAuthenticated,
    staleTime: 30 * 1000,
  })
}
