import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AnimatePresence, motion } from 'framer-motion'
import {
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  Loader2,
  Minus,
  Package,
  Plus,
  ShoppingCart,
  Store,
  Tag,
  Zap,
} from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { cartApi } from '@/features/cart/api/cartApi'
import { CART_QUERY_KEY } from '@/features/cart/hooks/useCart'
import { catalogApi } from '@/features/catalog/api/catalogApi'
import { formatCurrency } from '@/features/catalog/utils/catalogFormatters'

const CHECKOUT_SELECTION_KEY = 'smartshop_checkout_cart_item_ids'

export function ProductDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [selectedVariantId, setSelectedVariantId] = useState(null)
  const [selectedImageUrl, setSelectedImageUrl] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const [cartFeedback, setCartFeedback] = useState(null)

  const productQuery = useQuery({
    queryKey: ['public', 'products', 'detail', id],
    queryFn: async () => {
      const res = await catalogApi.getPublicProductDetail(id)
      return res.data
    },
    enabled: Boolean(id),
  })

  const product = productQuery.data

  const variants = product?.variants || []
  const selectedVariant =
    variants.find((v) => v.id === selectedVariantId) ||
    variants.find((v) => v.isDefault) ||
    variants[0] ||
    null

  const images = product?.images || []
  const activeImageUrl =
    selectedImageUrl ||
    images.find((img) => img.isPrimary)?.imageUrl ||
    images[0]?.imageUrl ||
    null

  const maxStock = selectedVariant?.stockQuantity ?? 0
  const isOutOfStock = !selectedVariant || maxStock <= 0
  const clampedQty = isOutOfStock ? 1 : Math.min(Math.max(1, quantity), maxStock)

  const addToCartMutation = useMutation({
    mutationFn: ({ variantId, qty }) =>
      cartApi.addItem({ variantId, quantity: qty }),
    onMutate: () => setCartFeedback(null),
    onSuccess: (res, variables) => {
      if (res?.data) {
        queryClient.setQueryData(CART_QUERY_KEY, res.data)
      } else {
        queryClient.invalidateQueries({ queryKey: CART_QUERY_KEY })
      }

      if (variables.goToCheckout) {
        const matched = (res?.data?.items || []).find(
          (item) => item.variantId === variables.variantId,
        )
        const selectedIds = matched ? [matched.id] : []
        if (selectedIds.length > 0) {
          sessionStorage.setItem(CHECKOUT_SELECTION_KEY, JSON.stringify(selectedIds))
          navigate('/checkout', { state: { selectedCartItemIds: selectedIds } })
        } else {
          navigate('/cart')
        }
        return
      }

      setCartFeedback({
        type: 'success',
        message: `Đã thêm ${variables.qty} sản phẩm vào giỏ hàng.`,
      })
    },
    onError: (err) => {
      setCartFeedback({
        type: 'error',
        message: err?.message || 'Không thể thêm sản phẩm vào giỏ hàng.',
      })
    },
  })

  const handleSelectVariant = (variant) => {
    setSelectedVariantId(variant.id)
    setQuantity(1)
    setCartFeedback(null)
  }

  const handleAdd = (goToCheckout = false) => {
    if (!selectedVariant || isOutOfStock) return
    addToCartMutation.mutate({
      variantId: selectedVariant.id,
      qty: clampedQty,
      goToCheckout,
    })
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <Button asChild variant="ghost" size="sm" className="-ml-2 text-muted-foreground">
            <Link to="/products">
              <ArrowLeft className="size-4" aria-hidden="true" />
              Quay lại danh sách sản phẩm
            </Link>
          </Button>
        </div>

        {productQuery.isLoading ? (
          <div className="grid gap-6 lg:grid-cols-12" aria-busy="true" aria-label="Đang tải sản phẩm">
            <div className="space-y-3 lg:col-span-5">
              <Skeleton className="aspect-square w-full rounded-xl" />
              <div className="flex gap-2">
                <Skeleton className="size-16 rounded-lg" />
                <Skeleton className="size-16 rounded-lg" />
                <Skeleton className="size-16 rounded-lg" />
              </div>
            </div>
            <div className="lg:col-span-7">
              <Card className="border-border/80 shadow-sm">
                <CardContent className="space-y-5 pt-6">
                  <div className="flex gap-2">
                    <Skeleton className="h-6 w-24" />
                    <Skeleton className="h-6 w-32" />
                  </div>
                  <Skeleton className="h-8 w-3/4" />
                  <Skeleton className="h-10 w-44" />
                  <div className="flex gap-2 pt-2">
                    <Skeleton className="h-14 w-28 rounded-lg" />
                    <Skeleton className="h-14 w-28 rounded-lg" />
                  </div>
                  <Skeleton className="h-11 w-full rounded-lg" />
                  <Skeleton className="h-24 w-full" />
                </CardContent>
              </Card>
            </div>
          </div>
        ) : productQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không tìm thấy sản phẩm</AlertTitle>
            <AlertDescription>
              {productQuery.error?.message || 'Sản phẩm không tồn tại hoặc đã ngừng bán.'}
            </AlertDescription>
          </Alert>
        ) : product ? (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.22 }}
            className="grid gap-6 lg:grid-cols-12"
          >
            <div className="space-y-3 lg:col-span-5">
              <Card className="overflow-hidden border-border/80 shadow-sm">
                <div className="aspect-square w-full bg-muted/40">
                  {activeImageUrl ? (
                    <motion.img
                      key={activeImageUrl}
                      initial={{ opacity: 0.6 }}
                      animate={{ opacity: 1 }}
                      transition={{ duration: 0.2 }}
                      src={activeImageUrl}
                      alt={product.name}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <div className="flex h-full w-full items-center justify-center text-muted-foreground/50">
                      <Package className="size-16" aria-hidden="true" />
                    </div>
                  )}
                </div>
              </Card>

              {images.length > 1 && (
                <div className="flex flex-wrap gap-2">
                  {images.map((img) => {
                    const active = activeImageUrl === img.imageUrl
                    return (
                      <button
                        key={img.id}
                        type="button"
                        onClick={() => setSelectedImageUrl(img.imageUrl)}
                        aria-label={img.altText || 'Xem ảnh sản phẩm'}
                        className={`size-16 cursor-pointer overflow-hidden rounded-lg border-2 transition-all ${
                          active
                            ? 'border-primary ring-2 ring-primary/30'
                            : 'border-border hover:border-primary/50'
                        }`}
                      >
                        <img
                          src={img.imageUrl}
                          alt={img.altText || product.name}
                          className="h-full w-full object-cover"
                        />
                      </button>
                    )
                  })}
                </div>
              )}
            </div>

            <div className="space-y-5 lg:col-span-7">
              <Card className="border-border/80 shadow-sm">
                <CardContent className="space-y-5 pt-6">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="inline-flex items-center gap-1 rounded-md bg-secondary px-2.5 py-1 text-xs font-semibold text-secondary-foreground">
                      <Tag className="size-3" aria-hidden="true" />
                      {product.categoryName}
                    </span>
                    {product.brand && (
                      <span className="rounded-md border border-border bg-muted/40 px-2.5 py-1 text-xs font-medium text-muted-foreground">
                        Thương hiệu: {product.brand}
                      </span>
                    )}
                    <span className="inline-flex items-center gap-1 rounded-md border border-blue-200 bg-blue-50 px-2.5 py-1 text-xs font-medium text-primary">
                      <Store className="size-3" aria-hidden="true" />
                      {product.storeName}
                    </span>
                  </div>

                  <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                      {product.name}
                    </h1>
                    {selectedVariant && (
                      <p className="mt-3 text-3xl font-bold text-primary tabular-nums">
                        {formatCurrency(selectedVariant.price)}
                      </p>
                    )}
                  </div>

                  {variants.length > 0 && (
                    <div className="space-y-2.5 border-t border-border/60 pt-4">
                      <p className="text-sm font-semibold text-foreground">
                        Phiên bản / Phân loại
                      </p>
                      <div className="flex flex-wrap gap-2">
                        {variants.map((variant) => {
                          const isSelected = selectedVariant?.id === variant.id
                          return (
                            <button
                              key={variant.id}
                              type="button"
                              onClick={() => handleSelectVariant(variant)}
                              className={`cursor-pointer rounded-lg border px-3.5 py-2 text-left text-sm transition-all ${
                                isSelected
                                  ? 'border-primary bg-secondary font-semibold text-primary ring-2 ring-primary/20'
                                  : 'border-border bg-card text-foreground hover:border-primary/50'
                              }`}
                            >
                              <div>{variant.variantName || variant.sku}</div>
                              <div className="text-xs text-muted-foreground tabular-nums">
                                {formatCurrency(variant.price)}
                              </div>
                            </button>
                          )
                        })}
                      </div>

                      {selectedVariant && (
                        <div className="flex flex-wrap items-center gap-4 pt-1 text-xs text-muted-foreground">
                          <span>
                            SKU:{' '}
                            <strong className="font-mono text-foreground">
                              {selectedVariant.sku}
                            </strong>
                          </span>
                          <span className="inline-flex items-center gap-1">
                            <CheckCircle2
                              className={`size-3.5 ${
                                selectedVariant.stockQuantity > 0
                                  ? 'text-green-600'
                                  : 'text-red-600'
                              }`}
                              aria-hidden="true"
                            />
                            {selectedVariant.stockQuantity > 0
                              ? `Còn ${selectedVariant.stockQuantity} sản phẩm`
                              : 'Tạm hết hàng'}
                          </span>
                        </div>
                      )}
                    </div>
                  )}

                  {selectedVariant && (
                    <div className="space-y-4 border-t border-border/60 pt-4">
                      <div className="flex flex-wrap items-center gap-4">
                        <span className="text-sm font-semibold text-foreground">
                          Số lượng
                        </span>
                        <div className="inline-flex items-center rounded-lg border border-input bg-card">
                          <button
                            type="button"
                            aria-label="Giảm số lượng"
                            disabled={isOutOfStock || clampedQty <= 1}
                            onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                            className="flex size-9 cursor-pointer items-center justify-center text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-40"
                          >
                            <Minus className="size-3.5" aria-hidden="true" />
                          </button>
                          <span className="min-w-[2.5rem] text-center text-sm font-semibold tabular-nums">
                            {clampedQty}
                          </span>
                          <button
                            type="button"
                            aria-label="Tăng số lượng"
                            disabled={isOutOfStock || clampedQty >= maxStock}
                            onClick={() => setQuantity((q) => Math.min(maxStock, q + 1))}
                            className="flex size-9 cursor-pointer items-center justify-center text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-40"
                          >
                            <Plus className="size-3.5" aria-hidden="true" />
                          </button>
                        </div>
                        {!isOutOfStock && (
                          <span className="text-xs text-muted-foreground">
                            Tối đa {maxStock}
                          </span>
                        )}
                      </div>

                      <div className="flex flex-col gap-2.5 sm:flex-row">
                        <motion.div whileTap={{ scale: isOutOfStock ? 1 : 0.98 }} className="flex-1">
                          <Button
                            type="button"
                            disabled={isOutOfStock || addToCartMutation.isPending}
                            onClick={() => handleAdd(false)}
                            className="h-10 w-full cursor-pointer text-sm font-semibold shadow-xs"
                          >
                            {addToCartMutation.isPending &&
                            !addToCartMutation.variables?.goToCheckout ? (
                              <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                            ) : (
                              <ShoppingCart className="size-4" aria-hidden="true" />
                            )}
                            {isOutOfStock ? 'Tạm hết hàng' : 'Thêm vào giỏ hàng'}
                          </Button>
                        </motion.div>

                        <motion.div whileTap={{ scale: isOutOfStock ? 1 : 0.98 }} className="sm:w-44">
                          <Button
                            type="button"
                            variant="outline"
                            disabled={isOutOfStock || addToCartMutation.isPending}
                            onClick={() => handleAdd(true)}
                            className="h-10 w-full cursor-pointer border-primary/40 text-sm font-semibold text-primary hover:bg-secondary"
                          >
                            {addToCartMutation.isPending &&
                            addToCartMutation.variables?.goToCheckout ? (
                              <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                            ) : (
                              <Zap className="size-4" aria-hidden="true" />
                            )}
                            Mua ngay
                          </Button>
                        </motion.div>
                      </div>

                      <AnimatePresence>
                        {cartFeedback && (
                          <motion.div
                            initial={{ opacity: 0, y: -6 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -6 }}
                            transition={{ duration: 0.18 }}
                          >
                            <Alert
                              variant={cartFeedback.type === 'error' ? 'destructive' : 'default'}
                              className={
                                cartFeedback.type === 'success'
                                  ? 'border-green-200 bg-green-50 text-green-800'
                                  : 'border-red-200 bg-red-50 text-red-800'
                              }
                            >
                              {cartFeedback.type === 'success' ? (
                                <CheckCircle2 className="size-4" aria-hidden="true" />
                              ) : (
                                <AlertCircle className="size-4" aria-hidden="true" />
                              )}
                              <AlertTitle>
                                {cartFeedback.type === 'success'
                                  ? 'Đã cập nhật giỏ hàng'
                                  : 'Không thể thêm vào giỏ'}
                              </AlertTitle>
                              <AlertDescription className="flex flex-wrap items-center justify-between gap-2 text-current/90">
                                <span>{cartFeedback.message}</span>
                                {cartFeedback.type === 'success' && (
                                  <Link
                                    to="/cart"
                                    className="font-semibold underline underline-offset-2 hover:opacity-80"
                                  >
                                    Xem giỏ hàng →
                                  </Link>
                                )}
                              </AlertDescription>
                            </Alert>
                          </motion.div>
                        )}
                      </AnimatePresence>
                    </div>
                  )}

                  <div className="space-y-2 border-t border-border/60 pt-4">
                    <h2 className="text-sm font-semibold text-foreground">
                      Mô tả sản phẩm
                    </h2>
                    <p className="whitespace-pre-line text-sm leading-relaxed text-muted-foreground">
                      {product.description || 'Chưa có mô tả chi tiết cho sản phẩm này.'}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </div>
          </motion.div>
        ) : null}
      </div>
    </AppShell>
  )
}
