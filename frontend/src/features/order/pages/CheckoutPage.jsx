import { useMemo, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import {
  AlertCircle,
  ArrowLeft,
  Banknote,
  CheckCircle2,
  Loader2,
  MapPin,
  Package,
  ShieldCheck,
  ShoppingBag,
  Store,
  Truck,
} from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { CART_QUERY_KEY, useCartQuery } from '@/features/cart/hooks/useCart'
import { formatCurrency } from '@/features/catalog/utils/catalogFormatters'
import { orderApi } from '@/features/order/api/orderApi'
import { checkoutSchema } from '@/features/order/schemas/checkoutSchema'
import { useAuth } from '@/hooks/useAuth'

const CHECKOUT_SELECTION_KEY = 'smartshop_checkout_cart_item_ids'
const EMPTY_CART_ITEMS = []

function focusErrorSummary() {
  setTimeout(() => {
    document.getElementById('checkout-error-summary')?.focus()
  }, 0)
}

function readStoredSelection() {
  try {
    const raw = sessionStorage.getItem(CHECKOUT_SELECTION_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

export function CheckoutPage() {
  const location = useLocation()
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const cartQuery = useCartQuery()
  const [placedOrder, setPlacedOrder] = useState(null)
  const [apiError, setApiError] = useState(null)

  const selectedIds = useMemo(() => {
    const fromState = location.state?.selectedCartItemIds
    if (Array.isArray(fromState) && fromState.length > 0) {
      return fromState
    }
    return readStoredSelection()
  }, [location.state])

  const selectedSet = useMemo(() => new Set(selectedIds), [selectedIds])
  const cartItems = cartQuery.data?.items ?? EMPTY_CART_ITEMS
  const selectedItems = useMemo(
    () => cartItems.filter((item) => selectedSet.has(item.id)),
    [cartItems, selectedSet],
  )

  const storeGroups = useMemo(() => {
    const map = new Map()
    for (const item of selectedItems) {
      if (!map.has(item.storeId)) {
        map.set(item.storeId, {
          storeId: item.storeId,
          storeName: item.storeName,
          subtotal: 0,
          items: [],
        })
      }
      const grp = map.get(item.storeId)
      grp.items.push(item)
      grp.subtotal += Number(item.subtotal || 0)
    }
    return Array.from(map.values())
  }, [selectedItems])

  const totalSubtotal = useMemo(
    () => selectedItems.reduce((sum, i) => sum + Number(i.subtotal || 0), 0),
    [selectedItems],
  )

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(checkoutSchema),
    values: {
      recipientName: user?.fullName || '',
      recipientPhone: user?.phone || '',
      shippingProvince: '',
      shippingDistrict: '',
      shippingWard: '',
      shippingAddress: '',
      customerNote: '',
    },
  })

  const checkoutMutation = useMutation({
    mutationFn: (values) =>
      orderApi.checkout({
        cartItemIds: selectedItems.map((i) => i.id),
        recipientName: values.recipientName.trim(),
        recipientPhone: values.recipientPhone.trim(),
        shippingProvince: values.shippingProvince.trim(),
        shippingDistrict: values.shippingDistrict?.trim() || undefined,
        shippingWard: values.shippingWard.trim(),
        shippingAddress: values.shippingAddress.trim(),
        customerNote: values.customerNote?.trim() || undefined,
      }),
    onMutate: () => setApiError(null),
    onSuccess: (res) => {
      sessionStorage.removeItem(CHECKOUT_SELECTION_KEY)
      setPlacedOrder(res.data)
      queryClient.invalidateQueries({ queryKey: CART_QUERY_KEY })
      queryClient.invalidateQueries({ queryKey: ['public', 'products'] })
    },
    onError: (err) => {
      setApiError(err?.message || 'Đặt hàng thất bại. Vui lòng kiểm tra lại tồn kho.')
      queryClient.invalidateQueries({ queryKey: CART_QUERY_KEY })
      focusErrorSummary()
    },
  })

  const validationErrorEntries = Object.entries(errors)

  // ── Success Confirmation Screen ─────────────────────────────────────────────
  if (placedOrder) {
    return (
      <AppShell>
        <motion.div
          initial={{ opacity: 0, scale: 0.98 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.25 }}
          className="mx-auto max-w-3xl space-y-6"
        >
          <Card className="border-green-200 bg-green-50/50 shadow-sm">
            <CardContent className="flex flex-col items-center gap-3 py-8 text-center">
              <motion.span
                initial={{ scale: 0.5, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={{ type: 'spring', stiffness: 260, damping: 20 }}
                className="flex size-16 items-center justify-center rounded-full bg-green-100 text-green-700"
              >
                <CheckCircle2 className="size-9" aria-hidden="true" />
              </motion.span>
              <div>
                <h1 className="text-2xl font-bold text-foreground">
                  Đặt hàng thành công!
                </h1>
                <p className="mt-1 text-sm text-muted-foreground">
                  Mã đơn hàng tổng:{' '}
                  <strong className="font-mono text-foreground">
                    {placedOrder.orderCode}
                  </strong>
                </p>
              </div>
              <div className="mt-2 flex flex-wrap justify-center gap-3">
                <Button asChild>
                  <Link to={`/orders/${placedOrder.id}`}>
                    Xem chi tiết đơn hàng
                  </Link>
                </Button>
                <Button asChild variant="outline">
                  <Link to="/orders">Đơn mua của tôi</Link>
                </Button>
                <Button asChild variant="ghost">
                  <Link to="/products">
                    <ShoppingBag className="size-4" aria-hidden="true" />
                    Tiếp tục mua sắm
                  </Link>
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card className="border-border/80 shadow-sm">
            <CardHeader className="border-b border-border/60">
              <CardTitle className="text-base">Thông tin nhận hàng (Snapshot)</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3 pt-4 text-sm sm:grid-cols-2">
              <div>
                <p className="text-xs text-muted-foreground">Người nhận</p>
                <p className="font-semibold text-foreground">
                  {placedOrder.recipientName} • {placedOrder.recipientPhone}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Địa chỉ giao hàng</p>
                <p className="font-medium text-foreground">
                  {[
                    placedOrder.shippingAddress,
                    placedOrder.shippingWard,
                    placedOrder.shippingDistrict,
                    placedOrder.shippingProvince,
                  ]
                    .filter(Boolean)
                    .join(', ')}
                </p>
              </div>
              {placedOrder.customerNote && (
                <div className="sm:col-span-2">
                  <p className="text-xs text-muted-foreground">Ghi chú</p>
                  <p className="text-foreground">{placedOrder.customerNote}</p>
                </div>
              )}
            </CardContent>
          </Card>

          <div className="space-y-3">
            <h2 className="text-base font-semibold text-foreground">
              Chi tiết đơn hàng theo từng cửa hàng ({placedOrder.sellerOrders?.length || 0})
            </h2>
            {(placedOrder.sellerOrders || []).map((so) => (
              <Card key={so.id} className="border-border/80 shadow-sm">
                <CardHeader className="flex flex-row items-center justify-between border-b border-border/60 bg-muted/20 py-3">
                  <div className="flex items-center gap-2 text-sm font-semibold">
                    <Store className="size-4 text-primary" aria-hidden="true" />
                    <span>{so.storeName}</span>
                    <span className="font-mono text-xs text-muted-foreground">
                      ({so.sellerOrderCode})
                    </span>
                  </div>
                  <span className="rounded-full border border-amber-200 bg-amber-50 px-2.5 py-0.5 text-xs font-semibold text-amber-700">
                    COD • {so.payment?.paymentStatus || 'PENDING'}
                  </span>
                </CardHeader>
                <CardContent className="divide-y divide-border/50 pt-2">
                  {(so.items || []).map((item) => (
                    <div
                      key={item.id}
                      className="flex items-center justify-between gap-3 py-3 text-sm"
                    >
                      <div className="min-w-0">
                        <p className="font-medium text-foreground">{item.productName}</p>
                        <p className="text-xs text-muted-foreground">
                          {item.variantDescription || item.sku} × {item.quantity}
                        </p>
                      </div>
                      <span className="font-semibold text-foreground tabular-nums">
                        {formatCurrency(item.subtotal)}
                      </span>
                    </div>
                  ))}
                  <div className="flex items-center justify-between pt-3 text-sm font-bold">
                    <span>Tổng đơn cửa hàng</span>
                    <span className="text-primary tabular-nums">
                      {formatCurrency(so.totalAmount)}
                    </span>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </motion.div>
      </AppShell>
    )
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <Button asChild variant="ghost" size="sm" className="-ml-2 text-muted-foreground">
            <Link to="/cart">
              <ArrowLeft className="size-4" aria-hidden="true" />
              Quay lại giỏ hàng
            </Link>
          </Button>

          <div className="flex items-center gap-2 text-xs font-medium text-muted-foreground">
            <span>1. Giỏ hàng</span>
            <span>/</span>
            <span className="font-semibold text-primary">2. Xác nhận & Đặt hàng</span>
            <span>/</span>
            <span>3. Hoàn tất</span>
          </div>
        </div>

        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Thanh toán đơn hàng (COD)
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Nhập thông tin giao hàng và kiểm tra các đơn hàng được tách theo từng cửa hàng.
          </p>
        </div>

        {cartQuery.isLoading ? (
          <div className="grid gap-6 lg:grid-cols-12" aria-busy="true">
            <div className="space-y-4 lg:col-span-7">
              <Skeleton className="h-72 w-full rounded-xl" />
              <Skeleton className="h-44 w-full rounded-xl" />
            </div>
            <div className="lg:col-span-5">
              <Skeleton className="h-64 w-full rounded-xl" />
            </div>
          </div>
        ) : selectedItems.length === 0 ? (
          <Card className="border-border/80 shadow-sm">
            <CardContent className="flex flex-col items-center justify-center gap-3 py-14 text-center">
              <span className="flex size-12 items-center justify-center rounded-full bg-secondary text-primary">
                <Package className="size-6" aria-hidden="true" />
              </span>
              <div>
                <h2 className="font-semibold text-foreground">
                  Chưa có sản phẩm nào được chọn để thanh toán
                </h2>
                <p className="mt-1 text-sm text-muted-foreground">
                  Vui lòng quay lại giỏ hàng và chọn ít nhất một sản phẩm khả dụng.
                </p>
              </div>
              <Button asChild size="sm">
                <Link to="/cart">Về giỏ hàng</Link>
              </Button>
            </CardContent>
          </Card>
        ) : (
          <form
            onSubmit={handleSubmit(
              (vals) => checkoutMutation.mutate(vals),
              focusErrorSummary,
            )}
            className="grid gap-6 lg:grid-cols-12"
            noValidate
          >
            {/* Left Column: Address + COD + Store Groups */}
            <div className="space-y-5 lg:col-span-7">
              {(apiError || validationErrorEntries.length > 0) && (
                <div
                  id="checkout-error-summary"
                  tabIndex={-1}
                  role="alert"
                  aria-labelledby="checkout-error-title"
                  className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800 outline-none focus-visible:ring-2 focus-visible:ring-red-400"
                >
                  <div className="flex items-center gap-2 font-semibold" id="checkout-error-title">
                    <AlertCircle className="size-4 shrink-0" aria-hidden="true" />
                    Vui lòng kiểm tra lại thông tin đặt hàng
                  </div>
                  {apiError && <p className="mt-1.5">{apiError}</p>}
                  {validationErrorEntries.length > 0 && (
                    <ul className="mt-2 list-disc space-y-1 pl-5 text-xs">
                      {validationErrorEntries.map(([field, err]) => (
                        <li key={field}>
                          <a
                            href={`#${field}`}
                            className="underline underline-offset-2 hover:opacity-80"
                          >
                            {err?.message}
                          </a>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              )}

              <Card className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60">
                  <div className="flex items-center gap-2">
                    <MapPin className="size-5 text-primary" aria-hidden="true" />
                    <div>
                      <CardTitle className="text-lg">Địa chỉ nhận hàng</CardTitle>
                      <CardDescription>
                        Thông tin người nhận được lưu cố định trên đơn hàng.
                      </CardDescription>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="grid gap-4 pt-5 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label htmlFor="recipientName">
                      Họ tên người nhận <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="recipientName"
                      aria-invalid={Boolean(errors.recipientName)}
                      {...register('recipientName')}
                    />
                    {errors.recipientName && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.recipientName.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <Label htmlFor="recipientPhone">
                      Số điện thoại <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="recipientPhone"
                      placeholder="0909123456"
                      aria-invalid={Boolean(errors.recipientPhone)}
                      {...register('recipientPhone')}
                    />
                    {errors.recipientPhone && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.recipientPhone.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <Label htmlFor="shippingProvince">
                      Tỉnh / Thành phố <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="shippingProvince"
                      placeholder="TP. Hồ Chí Minh"
                      aria-invalid={Boolean(errors.shippingProvince)}
                      {...register('shippingProvince')}
                    />
                    {errors.shippingProvince && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.shippingProvince.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <Label htmlFor="shippingDistrict">Quận / Huyện</Label>
                    <Input
                      id="shippingDistrict"
                      placeholder="Quận 1"
                      aria-invalid={Boolean(errors.shippingDistrict)}
                      {...register('shippingDistrict')}
                    />
                  </div>

                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="shippingWard">
                      Phường / Xã <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="shippingWard"
                      placeholder="Phường Bến Nghé"
                      aria-invalid={Boolean(errors.shippingWard)}
                      {...register('shippingWard')}
                    />
                    {errors.shippingWard && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.shippingWard.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="shippingAddress">
                      Địa chỉ chi tiết <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="shippingAddress"
                      placeholder="Số nhà, tên đường..."
                      aria-invalid={Boolean(errors.shippingAddress)}
                      {...register('shippingAddress')}
                    />
                    {errors.shippingAddress && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.shippingAddress.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="customerNote">Ghi chú cho đơn hàng</Label>
                    <textarea
                      id="customerNote"
                      rows={2}
                      placeholder="Ví dụ: Giao hàng giờ hành chính..."
                      className="w-full min-w-0 rounded-lg border border-input bg-transparent px-3 py-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                      {...register('customerNote')}
                    />
                  </div>
                </CardContent>
              </Card>

              <Card className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60">
                  <div className="flex items-center gap-2">
                    <Banknote className="size-5 text-primary" aria-hidden="true" />
                    <div>
                      <CardTitle className="text-lg">Phương thức thanh toán</CardTitle>
                      <CardDescription>
                        MVP hiện hỗ trợ thanh toán tiền mặt khi nhận hàng (COD).
                      </CardDescription>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="pt-4">
                  <div className="flex items-start gap-3 rounded-lg border border-primary bg-secondary/40 p-3.5">
                    <Truck className="mt-0.5 size-5 shrink-0 text-primary" aria-hidden="true" />
                    <div className="text-sm">
                      <p className="font-semibold text-foreground">
                        Thanh toán khi nhận hàng (COD)
                      </p>
                      <p className="mt-0.5 text-xs text-muted-foreground">
                        Hệ thống tạo riêng một phiếu thanh toán COD cho mỗi cửa hàng.
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Selected Items Split by Store */}
              <div className="space-y-3">
                <h2 className="text-sm font-semibold text-foreground">
                  Sản phẩm đặt mua ({storeGroups.length} đơn theo cửa hàng)
                </h2>
                {storeGroups.map((group) => (
                  <Card key={group.storeId} className="border-border/80 shadow-sm">
                    <CardHeader className="flex flex-row items-center justify-between border-b border-border/60 bg-muted/20 py-3">
                      <div className="flex items-center gap-2 text-sm font-semibold">
                        <Store className="size-4 text-primary" aria-hidden="true" />
                        <span>{group.storeName}</span>
                      </div>
                      <span className="text-sm font-bold text-primary tabular-nums">
                        {formatCurrency(group.subtotal)}
                      </span>
                    </CardHeader>
                    <CardContent className="divide-y divide-border/50 py-1">
                      {group.items.map((item) => (
                        <div
                          key={item.id}
                          className="flex items-center justify-between gap-3 py-3 text-sm"
                        >
                          <div className="flex min-w-0 items-center gap-3">
                            <div className="size-12 shrink-0 overflow-hidden rounded-md border border-border bg-muted/30">
                              {item.primaryImageUrl ? (
                                <img
                                  src={item.primaryImageUrl}
                                  alt={item.productName}
                                  className="h-full w-full object-cover"
                                />
                              ) : (
                                <div className="flex h-full w-full items-center justify-center text-muted-foreground/50">
                                  <Package className="size-4" aria-hidden="true" />
                                </div>
                              )}
                            </div>
                            <div className="min-w-0">
                              <p className="truncate font-medium text-foreground">
                                {item.productName}
                              </p>
                              <p className="text-xs text-muted-foreground">
                                {item.variantName || item.sku} × {item.quantity}
                              </p>
                            </div>
                          </div>
                          <span className="shrink-0 font-semibold text-foreground tabular-nums">
                            {formatCurrency(item.subtotal)}
                          </span>
                        </div>
                      ))}
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>

            {/* Right Sticky Summary */}
            <div className="lg:col-span-5">
              <Card className="border-border/80 shadow-sm lg:sticky lg:top-24">
                <CardHeader className="border-b border-border/60 pb-4">
                  <CardTitle className="text-lg">Xác nhận thanh toán</CardTitle>
                  <CardDescription>
                    Tổng thanh toán được tính từ giá bán hiện tại trên máy chủ.
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4 pt-5">
                  <div className="space-y-2.5 text-sm">
                    <div className="flex justify-between text-muted-foreground">
                      <span>Số dòng sản phẩm</span>
                      <span className="font-medium text-foreground tabular-nums">
                        {selectedItems.length}
                      </span>
                    </div>
                    <div className="flex justify-between text-muted-foreground">
                      <span>Số đơn cửa hàng</span>
                      <span className="font-medium text-foreground tabular-nums">
                        {storeGroups.length}
                      </span>
                    </div>
                    <div className="flex justify-between text-muted-foreground">
                      <span>Tạm tính</span>
                      <span className="font-semibold text-foreground tabular-nums">
                        {formatCurrency(totalSubtotal)}
                      </span>
                    </div>
                    <div className="flex justify-between text-muted-foreground">
                      <span>Phí vận chuyển</span>
                      <span className="font-medium text-foreground tabular-nums">
                        {formatCurrency(0)}
                      </span>
                    </div>
                    <div className="flex justify-between text-muted-foreground">
                      <span>Giảm giá</span>
                      <span className="font-medium text-foreground tabular-nums">
                        {formatCurrency(0)}
                      </span>
                    </div>
                  </div>

                  <div className="flex items-baseline justify-between border-t border-border/60 pt-4">
                    <span className="text-sm font-semibold text-foreground">
                      Tổng thanh toán (COD)
                    </span>
                    <span className="text-2xl font-bold text-primary tabular-nums">
                      {formatCurrency(totalSubtotal)}
                    </span>
                  </div>

                  <motion.div whileTap={{ scale: 0.98 }}>
                    <Button
                      type="submit"
                      disabled={checkoutMutation.isPending}
                      className="h-11 w-full cursor-pointer text-sm font-semibold shadow-xs"
                    >
                      {checkoutMutation.isPending ? (
                        <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                      ) : (
                        <CheckCircle2 className="size-4" aria-hidden="true" />
                      )}
                      Đặt hàng ngay (COD)
                    </Button>
                  </motion.div>

                  <div className="flex items-start gap-2 rounded-lg bg-secondary/50 p-3 text-xs text-secondary-foreground">
                    <ShieldCheck
                      className="mt-0.5 size-4 shrink-0 text-primary"
                      aria-hidden="true"
                    />
                    <span>
                      Tồn kho được giữ nguyên tử trong một giao dịch. Nếu có sản phẩm hết
                      hàng, toàn bộ đơn sẽ được hủy an toàn.
                    </span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </form>
        )}
      </div>
    </AppShell>
  )
}
