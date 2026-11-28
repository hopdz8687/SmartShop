import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { AnimatePresence, motion } from 'framer-motion'
import {
  AlertCircle,
  ArrowRight,
  Loader2,
  Minus,
  Package,
  Plus,
  ShieldCheck,
  ShoppingBag,
  ShoppingCart,
  Store,
  Trash2,
} from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { cartApi } from '@/features/cart/api/cartApi'
import { CART_QUERY_KEY, useCartQuery } from '@/features/cart/hooks/useCart'
import { formatCurrency } from '@/features/catalog/utils/catalogFormatters'

const CHECKOUT_SELECTION_KEY = 'smartshop_checkout_cart_item_ids'
const EMPTY_ITEMS = []

export function CartPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const cartQuery = useCartQuery()
  const [deselectedIds, setDeselectedIds] = useState(() => new Set())
  const [busyItemId, setBusyItemId] = useState(null)
  const [feedback, setFeedback] = useState(null)

  const items = cartQuery.data?.items ?? EMPTY_ITEMS

  const storeGroups = useMemo(() => {
    const map = new Map()
    for (const item of items) {
      if (!map.has(item.storeId)) {
        map.set(item.storeId, {
          storeId: item.storeId,
          storeName: item.storeName,
          items: [],
        })
      }
      map.get(item.storeId).items.push(item)
    }
    return Array.from(map.values())
  }, [items])

  const availableItems = useMemo(() => items.filter((item) => item.available), [items])
  const selectedItems = useMemo(
    () => availableItems.filter((item) => !deselectedIds.has(item.id)),
    [availableItems, deselectedIds],
  )
  const selectedStoreCount = useMemo(
    () => new Set(selectedItems.map((i) => i.storeId)).size,
    [selectedItems],
  )
  const selectedTotalQty = useMemo(
    () => selectedItems.reduce((sum, i) => sum + i.quantity, 0),
    [selectedItems],
  )
  const selectedSubtotal = useMemo(
    () => selectedItems.reduce((sum, i) => sum + Number(i.subtotal || 0), 0),
    [selectedItems],
  )
  const allAvailableSelected =
    availableItems.length > 0 && selectedItems.length === availableItems.length

  const updateMutation = useMutation({
    mutationFn: ({ id, quantity }) => cartApi.updateItem(id, { quantity }),
    onMutate: ({ id }) => {
      setBusyItemId(id)
      setFeedback(null)
    },
    onSuccess: (res) => {
      if (res?.data) {
        queryClient.setQueryData(CART_QUERY_KEY, res.data)
      }
    },
    onError: (err) => {
      setFeedback(err?.message || 'Không thể cập nhật số lượng.')
    },
    onSettled: () => setBusyItemId(null),
  })

  const removeMutation = useMutation({
    mutationFn: (id) => cartApi.removeItem(id),
    onMutate: (id) => {
      setBusyItemId(id)
      setFeedback(null)
    },
    onSuccess: (res, removedId) => {
      if (res?.data) {
        queryClient.setQueryData(CART_QUERY_KEY, res.data)
      }
      setDeselectedIds((prev) => {
        if (!prev.has(removedId)) return prev
        const next = new Set(prev)
        next.delete(removedId)
        return next
      })
    },
    onError: (err) => {
      setFeedback(err?.message || 'Không thể xóa sản phẩm khỏi giỏ.')
    },
    onSettled: () => setBusyItemId(null),
  })

  const toggleItem = (id) => {
    setDeselectedIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  const toggleStoreGroup = (group) => {
    const groupAvail = group.items.filter((i) => i.available)
    if (groupAvail.length === 0) return
    const allGroupSelected = groupAvail.every((i) => !deselectedIds.has(i.id))
    setDeselectedIds((prev) => {
      const next = new Set(prev)
      for (const item of groupAvail) {
        if (allGroupSelected) {
          next.add(item.id)
        } else {
          next.delete(item.id)
        }
      }
      return next
    })
  }

  const toggleAll = () => {
    if (allAvailableSelected) {
      setDeselectedIds(new Set(availableItems.map((i) => i.id)))
    } else {
      setDeselectedIds(new Set())
    }
  }

  const handleProceedToCheckout = () => {
    const selectedIds = selectedItems.map((i) => i.id)
    if (selectedIds.length === 0) return
    sessionStorage.setItem(CHECKOUT_SELECTION_KEY, JSON.stringify(selectedIds))
    navigate('/checkout', { state: { selectedCartItemIds: selectedIds } })
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-foreground">
              Giỏ hàng của bạn
            </h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Chọn sản phẩm muốn thanh toán. Đơn hàng sẽ tự động tách theo từng cửa hàng.
            </p>
          </div>
          {items.length > 0 && (
            <span className="text-xs font-medium text-muted-foreground">
              {items.length} phân loại • {storeGroups.length} cửa hàng
            </span>
          )}
        </div>

        {feedback && (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Cập nhật thất bại</AlertTitle>
            <AlertDescription>{feedback}</AlertDescription>
          </Alert>
        )}

        {cartQuery.isLoading ? (
          <div className="grid gap-6 lg:grid-cols-12" aria-busy="true" aria-label="Đang tải giỏ hàng">
            <div className="space-y-4 lg:col-span-8">
              {[1, 2].map((key) => (
                <Card key={key} className="border-border/80 shadow-sm">
                  <CardHeader className="border-b border-border/60 pb-3">
                    <Skeleton className="h-5 w-44" />
                  </CardHeader>
                  <CardContent className="space-y-4 pt-4">
                    <div className="flex gap-4">
                      <Skeleton className="size-20 rounded-lg" />
                      <div className="flex-1 space-y-2">
                        <Skeleton className="h-5 w-2/3" />
                        <Skeleton className="h-4 w-1/3" />
                        <Skeleton className="h-8 w-28" />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
            <div className="lg:col-span-4">
              <Card className="border-border/80 shadow-sm">
                <CardContent className="space-y-4 pt-6">
                  <Skeleton className="h-6 w-36" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-10 w-full rounded-lg" />
                </CardContent>
              </Card>
            </div>
          </div>
        ) : cartQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không thể tải giỏ hàng</AlertTitle>
            <AlertDescription>
              {cartQuery.error?.message || 'Vui lòng thử lại sau.'}
            </AlertDescription>
          </Alert>
        ) : items.length === 0 ? (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.22 }}
          >
            <Card className="border-border/80 shadow-sm">
              <CardContent className="flex flex-col items-center justify-center gap-4 py-16 text-center">
                <span className="flex size-16 items-center justify-center rounded-full bg-secondary text-primary">
                  <ShoppingCart className="size-8" aria-hidden="true" />
                </span>
                <div className="max-w-sm space-y-1">
                  <h2 className="text-lg font-semibold text-foreground">
                    Giỏ hàng của bạn đang trống
                  </h2>
                  <p className="text-sm text-muted-foreground">
                    Hãy khám phá các sản phẩm đang mở bán và thêm phiên bản yêu thích vào giỏ.
                  </p>
                </div>
                <Button asChild className="mt-1">
                  <Link to="/products">
                    <ShoppingBag className="size-4" aria-hidden="true" />
                    Khám phá sản phẩm
                  </Link>
                </Button>
              </CardContent>
            </Card>
          </motion.div>
        ) : (
          <div className="grid gap-6 lg:grid-cols-12">
            {/* Cart Items Grouped by Store */}
            <div className="space-y-4 lg:col-span-8">
              <div className="flex items-center justify-between rounded-xl border border-border/80 bg-card px-4 py-3 shadow-xs">
                <label className="flex cursor-pointer items-center gap-2.5 text-sm font-semibold text-foreground">
                  <input
                    type="checkbox"
                    checked={allAvailableSelected}
                    disabled={availableItems.length === 0}
                    onChange={toggleAll}
                    className="size-4 rounded border-input accent-primary"
                  />
                  Chọn tất cả ({availableItems.length} sản phẩm khả dụng)
                </label>
                <span className="text-xs font-medium text-primary tabular-nums">
                  Đã chọn: {selectedItems.length}
                </span>
              </div>

              <AnimatePresence initial={false}>
                {storeGroups.map((group) => {
                  const groupAvail = group.items.filter((i) => i.available)
                  const allGroupSelected =
                    groupAvail.length > 0 &&
                    groupAvail.every((i) => !deselectedIds.has(i.id))

                  return (
                    <motion.div
                      key={group.storeId}
                      layout
                      initial={{ opacity: 0, y: 8 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, scale: 0.98 }}
                      transition={{ duration: 0.2 }}
                    >
                      <Card className="overflow-hidden border-border/80 shadow-sm">
                        <CardHeader className="border-b border-border/60 bg-muted/30 py-3">
                          <label className="flex cursor-pointer items-center gap-2.5 text-sm font-semibold text-foreground">
                            <input
                              type="checkbox"
                              checked={allGroupSelected}
                              disabled={groupAvail.length === 0}
                              onChange={() => toggleStoreGroup(group)}
                              className="size-4 rounded border-input accent-primary"
                            />
                            <Store className="size-4 text-primary" aria-hidden="true" />
                            <span>{group.storeName}</span>
                          </label>
                        </CardHeader>

                        <CardContent className="divide-y divide-border/60 p-0">
                          <AnimatePresence initial={false}>
                            {group.items.map((item) => {
                              const isSelected =
                                item.available && !deselectedIds.has(item.id)
                              const isBusy = busyItemId === item.id

                              return (
                                <motion.div
                                  key={item.id}
                                  layout
                                  initial={{ opacity: 0 }}
                                  animate={{ opacity: 1 }}
                                  exit={{ opacity: 0, height: 0, overflow: 'hidden' }}
                                  transition={{ duration: 0.18 }}
                                  className={`flex flex-col gap-4 p-4 transition-colors sm:flex-row sm:items-center ${
                                    isSelected ? 'bg-secondary/20' : ''
                                  } ${!item.available ? 'opacity-70' : ''}`}
                                >
                                  <div className="flex min-w-0 flex-1 items-start gap-3">
                                    <input
                                      type="checkbox"
                                      checked={isSelected}
                                      disabled={!item.available}
                                      onChange={() => toggleItem(item.id)}
                                      aria-label={`Chọn ${item.productName}`}
                                      className="mt-2 size-4 shrink-0 cursor-pointer rounded border-input accent-primary disabled:cursor-not-allowed"
                                    />

                                    <Link
                                      to={`/products/${item.productId}`}
                                      className="size-20 shrink-0 overflow-hidden rounded-lg border border-border bg-muted/40"
                                    >
                                      {item.primaryImageUrl ? (
                                        <img
                                          src={item.primaryImageUrl}
                                          alt={item.productName}
                                          className="h-full w-full object-cover"
                                        />
                                      ) : (
                                        <div className="flex h-full w-full items-center justify-center text-muted-foreground/50">
                                          <Package className="size-6" aria-hidden="true" />
                                        </div>
                                      )}
                                    </Link>

                                    <div className="min-w-0 flex-1 space-y-1">
                                      <Link
                                        to={`/products/${item.productId}`}
                                        className="line-clamp-2 text-sm font-semibold text-foreground hover:text-primary"
                                      >
                                        {item.productName}
                                      </Link>
                                      <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                                        {item.variantName && (
                                          <span className="rounded bg-muted px-2 py-0.5 font-medium text-foreground">
                                            {item.variantName}
                                          </span>
                                        )}
                                        <span className="font-mono">{item.sku}</span>
                                      </div>
                                      <p className="text-sm font-semibold text-primary tabular-nums">
                                        {formatCurrency(item.unitPrice)}
                                      </p>
                                      {!item.available && (
                                        <p className="text-xs font-medium text-destructive">
                                          Sản phẩm tạm ngừng bán hoặc không đủ tồn kho (còn{' '}
                                          {item.stockQuantity})
                                        </p>
                                      )}
                                    </div>
                                  </div>

                                  <div className="flex items-center justify-between gap-4 sm:justify-end">
                                    <div className="inline-flex items-center rounded-lg border border-input bg-card">
                                      <button
                                        type="button"
                                        aria-label="Giảm số lượng"
                                        disabled={isBusy || item.quantity <= 1}
                                        onClick={() =>
                                          updateMutation.mutate({
                                            id: item.id,
                                            quantity: item.quantity - 1,
                                          })
                                        }
                                        className="flex size-8 cursor-pointer items-center justify-center hover:bg-muted disabled:cursor-not-allowed disabled:opacity-40"
                                      >
                                        <Minus className="size-3.5" aria-hidden="true" />
                                      </button>
                                      <span className="min-w-[2.2rem] text-center text-xs font-semibold tabular-nums">
                                        {isBusy ? (
                                          <Loader2
                                            className="mx-auto size-3.5 animate-spin text-primary"
                                            aria-hidden="true"
                                          />
                                        ) : (
                                          item.quantity
                                        )}
                                      </span>
                                      <button
                                        type="button"
                                        aria-label="Tăng số lượng"
                                        disabled={
                                          isBusy || item.quantity >= item.stockQuantity
                                        }
                                        onClick={() =>
                                          updateMutation.mutate({
                                            id: item.id,
                                            quantity: item.quantity + 1,
                                          })
                                        }
                                        className="flex size-8 cursor-pointer items-center justify-center hover:bg-muted disabled:cursor-not-allowed disabled:opacity-40"
                                      >
                                        <Plus className="size-3.5" aria-hidden="true" />
                                      </button>
                                    </div>

                                    <div className="min-w-[6.5rem] text-right">
                                      <p className="text-sm font-bold text-foreground tabular-nums">
                                        {formatCurrency(item.subtotal)}
                                      </p>
                                    </div>

                                    <Button
                                      type="button"
                                      variant="ghost"
                                      size="sm"
                                      disabled={isBusy}
                                      onClick={() => removeMutation.mutate(item.id)}
                                      aria-label={`Xóa ${item.productName}`}
                                      className="h-8 cursor-pointer px-2 text-muted-foreground hover:bg-red-50 hover:text-destructive"
                                    >
                                      <Trash2 className="size-4" aria-hidden="true" />
                                    </Button>
                                  </div>
                                </motion.div>
                              )
                            })}
                          </AnimatePresence>
                        </CardContent>
                      </Card>
                    </motion.div>
                  )
                })}
              </AnimatePresence>
            </div>

            {/* Sticky Order Summary */}
            <div className="lg:col-span-4">
              <Card className="border-border/80 shadow-sm lg:sticky lg:top-24">
                <CardHeader className="border-b border-border/60 pb-4">
                  <CardTitle className="text-lg">Tóm tắt đơn hàng</CardTitle>
                  <CardDescription>
                    Giá và tồn kho được kiểm tra lại từ hệ thống khi đặt hàng.
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4 pt-5">
                  <div className="space-y-2.5 text-sm">
                    <div className="flex items-center justify-between text-muted-foreground">
                      <span>Sản phẩm đã chọn</span>
                      <span className="font-medium text-foreground tabular-nums">
                        {selectedItems.length} dòng ({selectedTotalQty} SP)
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-muted-foreground">
                      <span>Số đơn theo cửa hàng</span>
                      <span className="font-medium text-foreground tabular-nums">
                        {selectedStoreCount} đơn
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-muted-foreground">
                      <span>Tạm tính</span>
                      <span className="font-semibold text-foreground tabular-nums">
                        {formatCurrency(selectedSubtotal)}
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-muted-foreground">
                      <span>Phí vận chuyển (MVP)</span>
                      <span className="font-medium text-foreground tabular-nums">
                        {formatCurrency(0)}
                      </span>
                    </div>
                  </div>

                  <div className="flex items-baseline justify-between border-t border-border/60 pt-4">
                    <span className="text-sm font-semibold text-foreground">
                      Tổng thanh toán
                    </span>
                    <span className="text-2xl font-bold text-primary tabular-nums">
                      {formatCurrency(selectedSubtotal)}
                    </span>
                  </div>

                  <motion.div whileTap={{ scale: selectedItems.length > 0 ? 0.98 : 1 }}>
                    <Button
                      type="button"
                      disabled={selectedItems.length === 0}
                      onClick={handleProceedToCheckout}
                      className="h-10 w-full cursor-pointer text-sm font-semibold shadow-xs"
                    >
                      Tiến hành thanh toán ({selectedItems.length})
                      <ArrowRight className="size-4" aria-hidden="true" />
                    </Button>
                  </motion.div>

                  <div className="flex items-start gap-2 rounded-lg bg-secondary/50 p-3 text-xs text-secondary-foreground">
                    <ShieldCheck
                      className="mt-0.5 size-4 shrink-0 text-primary"
                      aria-hidden="true"
                    />
                    <span>
                      Hỗ trợ thanh toán khi nhận hàng (COD). Các sản phẩm không chọn sẽ
                      được giữ nguyên trong giỏ.
                    </span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        )}
      </div>
    </AppShell>
  )
}
