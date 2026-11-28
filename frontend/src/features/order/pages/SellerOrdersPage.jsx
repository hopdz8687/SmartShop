import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AnimatePresence, motion } from 'framer-motion'
import {
  AlertCircle,
  ArrowRight,
  Check,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  ClipboardList,
  Loader2,
  MapPin,
  PackageCheck,
  User as UserIcon,
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

export function SellerOrdersPage() {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [busyId, setBusyId] = useState(null)
  const [feedback, setFeedback] = useState(null)

  const ordersQuery = useQuery({
    queryKey: ['seller', 'orders', page],
    queryFn: async () => {
      const res = await orderApi.getSellerOrders({ page, size: 10 })
      return res.data
    },
  })

  const confirmMutation = useMutation({
    mutationFn: (id) => orderApi.confirmSellerOrder(id),
    onMutate: (id) => {
      setBusyId(id)
      setFeedback(null)
    },
    onSuccess: (res) => {
      setFeedback({
        type: 'success',
        message: `Đã xác nhận đơn hàng ${res.data?.sellerOrderCode || ''}.`,
      })
      queryClient.invalidateQueries({ queryKey: ['seller', 'orders'] })
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể xác nhận đơn hàng.',
      })
    },
    onSettled: () => setBusyId(null),
  })

  const processingMutation = useMutation({
    mutationFn: (id) => orderApi.startProcessingSellerOrder(id),
    onMutate: (id) => {
      setBusyId(id)
      setFeedback(null)
    },
    onSuccess: (res) => {
      setFeedback({
        type: 'success',
        message: `Đơn hàng ${res.data?.sellerOrderCode || ''} đã chuyển sang Đang xử lý.`,
      })
      queryClient.invalidateQueries({ queryKey: ['seller', 'orders'] })
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể cập nhật trạng thái xử lý.',
      })
    },
    onSettled: () => setBusyId(null),
  })

  const pageData = ordersQuery.data
  const orders = pageData?.content || []
  const totalPages = pageData?.totalPages ?? 0

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-foreground">
              Quản lý đơn hàng cửa hàng
            </h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Tiếp nhận, xác nhận và chuẩn bị hàng cho các đơn đặt từ gian hàng của bạn.
            </p>
          </div>
          {pageData && (
            <span className="text-xs font-medium text-muted-foreground">
              Tổng cộng {pageData.totalElements} đơn hàng
            </span>
          )}
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
                  {feedback.type === 'success' ? 'Cập nhật thành công' : 'Thao tác thất bại'}
                </AlertTitle>
                <AlertDescription className="text-current/90">
                  {feedback.message}
                </AlertDescription>
              </Alert>
            </motion.div>
          )}
        </AnimatePresence>

        {ordersQuery.isLoading ? (
          <div className="space-y-4" aria-busy="true" aria-label="Đang tải đơn hàng">
            {[1, 2, 3].map((idx) => (
              <Card key={idx} className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60 pb-3">
                  <div className="flex items-center justify-between">
                    <Skeleton className="h-5 w-44" />
                    <Skeleton className="h-5 w-28 rounded-full" />
                  </div>
                </CardHeader>
                <CardContent className="space-y-3 pt-4">
                  <Skeleton className="h-4 w-3/4" />
                  <div className="flex items-center justify-between pt-1">
                    <Skeleton className="h-6 w-32" />
                    <Skeleton className="h-8 w-36 rounded-lg" />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : ordersQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không thể tải danh sách đơn hàng</AlertTitle>
            <AlertDescription>
              {ordersQuery.error?.message || 'Vui lòng thử lại sau.'}
            </AlertDescription>
          </Alert>
        ) : orders.length === 0 ? (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.2 }}
          >
            <Card className="border-border/80 shadow-sm">
              <CardContent className="flex flex-col items-center justify-center gap-4 py-16 text-center">
                <span className="flex size-16 items-center justify-center rounded-full bg-secondary text-primary">
                  <ClipboardList className="size-8" aria-hidden="true" />
                </span>
                <div className="max-w-sm space-y-1">
                  <h2 className="text-lg font-semibold text-foreground">
                    Chưa có đơn hàng nào
                  </h2>
                  <p className="text-sm text-muted-foreground">
                    Khi khách hàng đặt mua sản phẩm từ cửa hàng của bạn, đơn hàng sẽ xuất hiện
                    tại đây.
                  </p>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => {
              const items = order.items || []
              const totalQty = items.reduce((sum, i) => sum + i.quantity, 0)
              const isBusy = busyId === order.id

              return (
                <motion.div
                  key={order.id}
                  initial={{ opacity: 0, y: 6 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.18 }}
                >
                  <Card className="border-border/80 shadow-sm transition-shadow hover:shadow-md">
                    <CardHeader className="flex flex-col gap-2 border-b border-border/60 bg-muted/20 py-3.5 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <CardTitle className="flex flex-wrap items-center gap-2 text-base">
                          <span className="font-mono text-primary">
                            {order.sellerOrderCode}
                          </span>
                          <OrderStatusBadge status={order.status} />
                          {order.payment && (
                            <PaymentStatusBadge status={order.payment.paymentStatus} />
                          )}
                        </CardTitle>
                        <CardDescription className="mt-1 text-xs">
                          Mã đơn tổng: <span className="font-mono">{order.orderCode}</span> •
                          Đặt lúc: {formatDateTime(order.createdAt)} • {totalQty} sản phẩm
                        </CardDescription>
                      </div>

                      <div className="text-left sm:text-right">
                        <p className="text-xs text-muted-foreground">Tổng thu</p>
                        <p className="text-lg font-bold text-primary tabular-nums">
                          {formatCurrency(order.totalAmount)}
                        </p>
                      </div>
                    </CardHeader>

                    <CardContent className="flex flex-col gap-4 pt-4 sm:flex-row sm:items-center sm:justify-between">
                      <div className="min-w-0 space-y-1.5 text-xs text-muted-foreground">
                        <p className="flex items-center gap-1.5 text-sm font-medium text-foreground">
                          <UserIcon className="size-3.5 text-primary" aria-hidden="true" />
                          <span>
                            {order.recipientName} • {order.recipientPhone}
                          </span>
                        </p>
                        <p className="flex items-center gap-1.5">
                          <MapPin className="size-3.5 shrink-0" aria-hidden="true" />
                          <span className="truncate">
                            {[
                              order.shippingAddress,
                              order.shippingWard,
                              order.shippingDistrict,
                              order.shippingProvince,
                            ]
                              .filter(Boolean)
                              .join(', ')}
                          </span>
                        </p>
                      </div>

                      <div className="flex flex-wrap items-center gap-2 shrink-0">
                        {order.status === 'PENDING' && (
                          <Button
                            type="button"
                            size="sm"
                            disabled={isBusy}
                            onClick={() => confirmMutation.mutate(order.id)}
                            className="cursor-pointer"
                          >
                            {isBusy ? (
                              <Loader2 className="size-3.5 animate-spin" aria-hidden="true" />
                            ) : (
                              <Check className="size-3.5" aria-hidden="true" />
                            )}
                            Xác nhận đơn
                          </Button>
                        )}

                        {order.status === 'CONFIRMED' && (
                          <Button
                            type="button"
                            size="sm"
                            disabled={isBusy}
                            onClick={() => processingMutation.mutate(order.id)}
                            className="cursor-pointer"
                          >
                            {isBusy ? (
                              <Loader2 className="size-3.5 animate-spin" aria-hidden="true" />
                            ) : (
                              <PackageCheck className="size-3.5" aria-hidden="true" />
                            )}
                            Chuẩn bị hàng
                          </Button>
                        )}

                        <Button asChild variant="outline" size="sm">
                          <Link to={`/seller/orders/${order.id}`}>
                            Chi tiết
                            <ArrowRight className="size-3.5" aria-hidden="true" />
                          </Link>
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                </motion.div>
              )
            })}

            {totalPages > 1 && (
              <div className="flex items-center justify-between border-t border-border/60 pt-4">
                <span className="text-xs text-muted-foreground">
                  Trang {page + 1} / {totalPages}
                </span>
                <div className="flex gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={page === 0}
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    className="cursor-pointer"
                  >
                    <ChevronLeft className="size-4" aria-hidden="true" />
                    Trước
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={page + 1 >= totalPages}
                    onClick={() => setPage((p) => p + 1)}
                    className="cursor-pointer"
                  >
                    Sau
                    <ChevronRight className="size-4" aria-hidden="true" />
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </AppShell>
  )
}
