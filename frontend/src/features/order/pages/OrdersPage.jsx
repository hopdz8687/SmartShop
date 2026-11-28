import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import {
  AlertCircle,
  ArrowRight,
  ChevronLeft,
  ChevronRight,
  Package,
  ReceiptText,
  ShoppingBag,
  Store,
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
import { OrderStatusBadge } from '@/features/order/components/OrderStatusBadge'
import { formatDateTime } from '@/features/store/components/StoreStatusBadge'

export function OrdersPage() {
  const [page, setPage] = useState(0)

  const ordersQuery = useQuery({
    queryKey: ['orders', 'my', page],
    queryFn: async () => {
      const res = await orderApi.getMyOrders({ page, size: 10 })
      return res.data
    },
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
              Đơn mua của tôi
            </h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Theo dõi lịch sử đặt hàng, trạng thái xử lý theo từng cửa hàng và thanh toán COD.
            </p>
          </div>
          {pageData && (
            <span className="text-xs font-medium text-muted-foreground">
              Tổng cộng {pageData.totalElements} đơn hàng
            </span>
          )}
        </div>

        {ordersQuery.isLoading ? (
          <div className="space-y-4" aria-busy="true" aria-label="Đang tải đơn hàng">
            {[1, 2, 3].map((idx) => (
              <Card key={idx} className="border-border/80 shadow-sm">
                <CardHeader className="border-b border-border/60 pb-3">
                  <div className="flex items-center justify-between">
                    <Skeleton className="h-5 w-40" />
                    <Skeleton className="h-5 w-24 rounded-full" />
                  </div>
                </CardHeader>
                <CardContent className="space-y-3 pt-4">
                  <Skeleton className="h-4 w-2/3" />
                  <div className="flex items-center justify-between pt-1">
                    <Skeleton className="h-6 w-32" />
                    <Skeleton className="h-8 w-28 rounded-lg" />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : ordersQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không thể tải lịch sử đơn hàng</AlertTitle>
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
                  <ReceiptText className="size-8" aria-hidden="true" />
                </span>
                <div className="max-w-sm space-y-1">
                  <h2 className="text-lg font-semibold text-foreground">
                    Bạn chưa có đơn hàng nào
                  </h2>
                  <p className="text-sm text-muted-foreground">
                    Khi bạn đặt hàng thành công, toàn bộ thông tin đơn hàng sẽ hiển thị tại đây.
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
          <div className="space-y-4">
            {orders.map((order) => {
              const sellerOrders = order.sellerOrders || []
              const totalItemsCount = sellerOrders.reduce(
                (sum, so) =>
                  sum +
                  (so.items || []).reduce((itemSum, item) => itemSum + item.quantity, 0),
                0,
              )

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
                          <span className="font-mono">{order.orderCode}</span>
                          <OrderStatusBadge status={order.orderStatus} />
                        </CardTitle>
                        <CardDescription className="mt-0.5 text-xs">
                          Ngày đặt: {formatDateTime(order.createdAt)} • {totalItemsCount}{' '}
                          sản phẩm
                        </CardDescription>
                      </div>

                      <div className="text-left sm:text-right">
                        <p className="text-xs text-muted-foreground">Tổng thanh toán</p>
                        <p className="text-lg font-bold text-primary tabular-nums">
                          {formatCurrency(order.totalAmount)}
                        </p>
                      </div>
                    </CardHeader>

                    <CardContent className="flex flex-col gap-4 pt-4 sm:flex-row sm:items-center sm:justify-between">
                      <div className="space-y-2 min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          {sellerOrders.map((so) => (
                            <span
                              key={so.id}
                              className="inline-flex items-center gap-1.5 rounded-md border border-border bg-card px-2.5 py-1 text-xs font-medium text-foreground"
                            >
                              <Store className="size-3.5 text-primary" aria-hidden="true" />
                              {so.storeName}
                            </span>
                          ))}
                        </div>
                        <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
                          <Package className="size-3.5 shrink-0" aria-hidden="true" />
                          <span className="truncate">
                            Giao tới: {order.recipientName} ({order.recipientPhone}) —{' '}
                            {order.shippingProvince}
                          </span>
                        </p>
                      </div>

                      <Button asChild variant="outline" size="sm" className="w-fit shrink-0">
                        <Link to={`/orders/${order.id}`}>
                          Xem chi tiết
                          <ArrowRight className="size-3.5" aria-hidden="true" />
                        </Link>
                      </Button>
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
