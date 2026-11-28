import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AnimatePresence, motion } from 'framer-motion'
import {
  AlertCircle,
  ArrowLeft,
  Banknote,
  Check,
  CheckCircle2,
  Loader2,
  MapPin,
  Package,
  PackageCheck,
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
import { formatCurrency } from '@/features/catalog/utils/catalogFormatters'
import { orderApi } from '@/features/order/api/orderApi'
import {
  OrderStatusBadge,
  PaymentStatusBadge,
} from '@/features/order/components/OrderStatusBadge'
import { formatDateTime } from '@/features/store/components/StoreStatusBadge'

const LIFECYCLE_STEPS = [
  { key: 'PENDING', label: 'Chờ xác nhận' },
  { key: 'CONFIRMED', label: 'Đã xác nhận' },
  { key: 'PROCESSING', label: 'Chuẩn bị hàng' },
  { key: 'SHIPPED', label: 'Đã gửi hàng' },
]

export function SellerOrderDetailPage() {
  const { id } = useParams()
  const queryClient = useQueryClient()
  const [feedback, setFeedback] = useState(null)

  const orderQuery = useQuery({
    queryKey: ['seller', 'orders', 'detail', id],
    queryFn: async () => {
      const res = await orderApi.getSellerOrderDetail(id)
      return res.data
    },
    enabled: Boolean(id),
  })

  const invalidate = (updatedData) => {
    if (updatedData) {
      queryClient.setQueryData(['seller', 'orders', 'detail', id], updatedData)
    } else {
      queryClient.invalidateQueries({ queryKey: ['seller', 'orders', 'detail', id] })
    }
    queryClient.invalidateQueries({ queryKey: ['seller', 'orders'] })
  }

  const confirmMutation = useMutation({
    mutationFn: () => orderApi.confirmSellerOrder(id),
    onMutate: () => setFeedback(null),
    onSuccess: (res) => {
      invalidate(res.data)
      setFeedback({
        type: 'success',
        message: 'Đã xác nhận đơn hàng thành công (CONFIRMED).',
      })
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể xác nhận đơn hàng.',
      })
    },
  })

  const processingMutation = useMutation({
    mutationFn: () => orderApi.startProcessingSellerOrder(id),
    onMutate: () => setFeedback(null),
    onSuccess: (res) => {
      invalidate(res.data)
      setFeedback({
        type: 'success',
        message: 'Đơn hàng đã chuyển sang trạng thái Đang chuẩn bị hàng (PROCESSING).',
      })
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể chuyển trạng thái xử lý.',
      })
    },
  })

  const order = orderQuery.data
  const currentStepIndex = order
    ? LIFECYCLE_STEPS.findIndex((s) => s.key === order.status)
    : -1
  const isMutating = confirmMutation.isPending || processingMutation.isPending

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <Button asChild variant="ghost" size="sm" className="-ml-2 text-muted-foreground">
            <Link to="/seller/orders">
              <ArrowLeft className="size-4" aria-hidden="true" />
              Quay lại danh sách đơn hàng
            </Link>
          </Button>
        </div>

        <AnimatePresence>
          {feedback && (
            <motion.div
              initial={{ opacity: 0, y: -6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              transition={{ duration: 0.18 }}
            >
              <Alert
                variant={feedback.type === 'error' ? 'destructive' : 'default'}
                className={
                  feedback.type === 'success'
                    ? 'border-green-200 bg-green-50 text-green-800'
                    : 'border-red-200 bg-red-50 text-red-800'
                }
              >
                {feedback.type === 'success' ? (
                  <CheckCircle2 className="size-4" aria-hidden="true" />
                ) : (
                  <AlertCircle className="size-4" aria-hidden="true" />
                )}
                <AlertTitle>
                  {feedback.type === 'success' ? 'Thành công' : 'Có lỗi xảy ra'}
                </AlertTitle>
                <AlertDescription className="text-current/90">
                  {feedback.message}
                </AlertDescription>
              </Alert>
            </motion.div>
          )}
        </AnimatePresence>

        {orderQuery.isLoading ? (
          <div className="grid gap-6 lg:grid-cols-12" aria-busy="true" aria-label="Đang tải đơn hàng">
            <div className="space-y-4 lg:col-span-8">
              <Skeleton className="h-32 w-full rounded-xl" />
              <Skeleton className="h-56 w-full rounded-xl" />
            </div>
            <div className="space-y-4 lg:col-span-4">
              <Skeleton className="h-36 w-full rounded-xl" />
              <Skeleton className="h-44 w-full rounded-xl" />
            </div>
          </div>
        ) : orderQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không thể tải chi tiết đơn hàng</AlertTitle>
            <AlertDescription>
              {orderQuery.error?.message ||
                'Đơn hàng không tồn tại hoặc không thuộc cửa hàng của bạn.'}
            </AlertDescription>
          </Alert>
        ) : order ? (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.2 }}
            className="grid gap-6 lg:grid-cols-12"
          >
            {/* Left Column: Header + Lifecycle + Items */}
            <div className="space-y-5 lg:col-span-8">
              <Card className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60 pb-4">
                  <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <CardTitle className="flex flex-wrap items-center gap-2.5 text-xl">
                        <span>Đơn cửa hàng</span>
                        <span className="font-mono text-primary">
                          {order.sellerOrderCode}
                        </span>
                      </CardTitle>
                      <CardDescription className="mt-1">
                        Mã đơn tổng: <span className="font-mono">{order.orderCode}</span> •
                        Đặt lúc: {formatDateTime(order.createdAt)}
                      </CardDescription>
                    </div>
                    <OrderStatusBadge status={order.status} />
                  </div>
                </CardHeader>

                {order.status !== 'CANCELLED' && (
                  <CardContent className="pt-4">
                    <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                      {LIFECYCLE_STEPS.map((step, index) => {
                        const completed =
                          order.status === 'DELIVERED' ||
                          (currentStepIndex >= 0 && index <= currentStepIndex)
                        return (
                          <div
                            key={step.key}
                            className={`rounded-lg border px-3 py-2 text-center text-xs font-semibold transition-colors ${
                              completed
                                ? 'border-primary/40 bg-secondary text-primary'
                                : 'border-border bg-muted/30 text-muted-foreground'
                            }`}
                          >
                            {step.label}
                          </div>
                        )
                      })}
                    </div>
                  </CardContent>
                )}
              </Card>

              <Card className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60 pb-3">
                  <CardTitle className="text-base">
                    Sản phẩm trong đơn ({order.items?.length || 0})
                  </CardTitle>
                </CardHeader>
                <CardContent className="divide-y divide-border/50 pt-2">
                  {(order.items || []).map((item) => (
                    <div
                      key={item.id}
                      className="flex flex-col gap-3 py-3.5 sm:flex-row sm:items-center sm:justify-between"
                    >
                      <div className="flex min-w-0 items-center gap-3">
                        <div className="size-16 shrink-0 overflow-hidden rounded-lg border border-border bg-muted/30">
                          {item.productImage ? (
                            <img
                              src={item.productImage}
                              alt={item.productName}
                              className="h-full w-full object-cover"
                            />
                          ) : (
                            <div className="flex h-full w-full items-center justify-center text-muted-foreground/50">
                              <Package className="size-6" aria-hidden="true" />
                            </div>
                          )}
                        </div>
                        <div className="min-w-0 space-y-0.5">
                          <p className="font-medium text-foreground">{item.productName}</p>
                          <p className="text-xs text-muted-foreground">
                            {item.variantDescription || 'Mặc định'} • SKU:{' '}
                            <span className="font-mono">{item.sku}</span>
                          </p>
                          <p className="text-xs text-muted-foreground tabular-nums">
                            {formatCurrency(item.unitPrice)} × {item.quantity}
                          </p>
                        </div>
                      </div>

                      <p className="text-sm font-bold text-foreground tabular-nums sm:text-right">
                        {formatCurrency(item.subtotal)}
                      </p>
                    </div>
                  ))}
                </CardContent>
              </Card>
            </div>

            {/* Right Column: Actions + Recipient + Totals */}
            <div className="space-y-5 lg:col-span-4">
              <Card className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60 pb-3">
                  <CardTitle className="text-base">Thao tác xử lý đơn</CardTitle>
                  <CardDescription>
                    Chuyển trạng thái tuần tự theo quy trình xử lý của Seller.
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-3 pt-4">
                  {order.status === 'PENDING' ? (
                    <motion.div whileTap={{ scale: 0.98 }}>
                      <Button
                        type="button"
                        disabled={isMutating}
                        onClick={() => confirmMutation.mutate()}
                        className="h-10 w-full cursor-pointer font-semibold shadow-xs"
                      >
                        {confirmMutation.isPending ? (
                          <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                        ) : (
                          <Check className="size-4" aria-hidden="true" />
                        )}
                        Xác nhận đơn hàng
                      </Button>
                    </motion.div>
                  ) : order.status === 'CONFIRMED' ? (
                    <motion.div whileTap={{ scale: 0.98 }}>
                      <Button
                        type="button"
                        disabled={isMutating}
                        onClick={() => processingMutation.mutate()}
                        className="h-10 w-full cursor-pointer font-semibold shadow-xs"
                      >
                        {processingMutation.isPending ? (
                          <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                        ) : (
                          <PackageCheck className="size-4" aria-hidden="true" />
                        )}
                        Bắt đầu chuẩn bị hàng
                      </Button>
                    </motion.div>
                  ) : (
                    <p className="rounded-lg bg-muted/50 p-3 text-xs text-muted-foreground">
                      Đơn hàng đang ở trạng thái <strong>{order.status}</strong>. Không có
                      thao tác xác nhận hoặc chuẩn bị hàng khả dụng.
                    </p>
                  )}
                </CardContent>
              </Card>

              <Card className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60 pb-3">
                  <div className="flex items-center gap-2">
                    <MapPin className="size-4 text-primary" aria-hidden="true" />
                    <CardTitle className="text-base">Người nhận & Giao hàng</CardTitle>
                  </div>
                </CardHeader>
                <CardContent className="space-y-2.5 pt-4 text-sm">
                  <div>
                    <p className="font-semibold text-foreground">{order.recipientName}</p>
                    <p className="text-muted-foreground">{order.recipientPhone}</p>
                  </div>
                  <p className="text-muted-foreground">
                    {[
                      order.shippingAddress,
                      order.shippingWard,
                      order.shippingDistrict,
                      order.shippingProvince,
                    ]
                      .filter(Boolean)
                      .join(', ')}
                  </p>
                  {order.customerNote && (
                    <div className="rounded-md bg-muted/40 p-2.5 text-xs">
                      <span className="font-semibold text-foreground">Ghi chú khách: </span>
                      <span className="text-muted-foreground">{order.customerNote}</span>
                    </div>
                  )}
                </CardContent>
              </Card>

              <Card className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60 pb-3">
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <Banknote className="size-4 text-primary" aria-hidden="true" />
                      <CardTitle className="text-base">Thanh toán COD</CardTitle>
                    </div>
                    {order.payment && (
                      <PaymentStatusBadge status={order.payment.paymentStatus} />
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-2 pt-4 text-sm">
                  <div className="flex justify-between text-muted-foreground">
                    <span>Tạm tính</span>
                    <span className="font-medium text-foreground tabular-nums">
                      {formatCurrency(order.subtotal)}
                    </span>
                  </div>
                  <div className="flex justify-between text-muted-foreground">
                    <span>Phí vận chuyển</span>
                    <span className="font-medium text-foreground tabular-nums">
                      {formatCurrency(order.shippingFee)}
                    </span>
                  </div>
                  <div className="flex justify-between text-muted-foreground">
                    <span>Giảm giá</span>
                    <span className="font-medium text-foreground tabular-nums">
                      {formatCurrency(order.discountAmount)}
                    </span>
                  </div>
                  <div className="flex items-baseline justify-between border-t border-border/60 pt-3">
                    <span className="font-semibold text-foreground">Tổng đơn</span>
                    <span className="text-xl font-bold text-primary tabular-nums">
                      {formatCurrency(order.totalAmount)}
                    </span>
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
