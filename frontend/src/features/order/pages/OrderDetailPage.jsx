import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import {
  AlertCircle,
  ArrowLeft,
  Banknote,
  MapPin,
  Package,
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
import {
  OrderStatusBadge,
  PaymentStatusBadge,
} from '@/features/order/components/OrderStatusBadge'
import { formatDateTime } from '@/features/store/components/StoreStatusBadge'

export function OrderDetailPage() {
  const { id } = useParams()

  const orderQuery = useQuery({
    queryKey: ['orders', 'detail', id],
    queryFn: async () => {
      const res = await orderApi.getOrderDetail(id)
      return res.data
    },
    enabled: Boolean(id),
  })

  const order = orderQuery.data

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <Button asChild variant="ghost" size="sm" className="-ml-2 text-muted-foreground">
            <Link to="/orders">
              <ArrowLeft className="size-4" aria-hidden="true" />
              Quay lại danh sách đơn mua
            </Link>
          </Button>
        </div>

        {orderQuery.isLoading ? (
          <div className="space-y-5" aria-busy="true" aria-label="Đang tải chi tiết đơn hàng">
            <Skeleton className="h-24 w-full rounded-xl" />
            <Skeleton className="h-36 w-full rounded-xl" />
            <Skeleton className="h-52 w-full rounded-xl" />
          </div>
        ) : orderQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không thể tải chi tiết đơn hàng</AlertTitle>
            <AlertDescription>
              {orderQuery.error?.message ||
                'Đơn hàng không tồn tại hoặc bạn không có quyền xem đơn hàng này.'}
            </AlertDescription>
          </Alert>
        ) : order ? (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.2 }}
            className="space-y-6"
          >
            {/* Header Summary */}
            <Card className="border-border/80 shadow-sm">
              <CardHeader className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <CardTitle className="flex flex-wrap items-center gap-2.5 text-xl">
                    <span>Đơn hàng</span>
                    <span className="font-mono text-primary">{order.orderCode}</span>
                    <OrderStatusBadge status={order.orderStatus} />
                  </CardTitle>
                  <CardDescription className="mt-1">
                    Đặt lúc: {formatDateTime(order.createdAt)}
                  </CardDescription>
                </div>

                <div className="text-left sm:text-right">
                  <p className="text-xs text-muted-foreground">Tổng thanh toán</p>
                  <p className="text-2xl font-bold text-primary tabular-nums">
                    {formatCurrency(order.totalAmount)}
                  </p>
                </div>
              </CardHeader>
            </Card>

            {/* Recipient & Address Snapshot */}
            <Card className="border-border/80 shadow-sm">
              <CardHeader className="border-b border-border/60 pb-3">
                <div className="flex items-center gap-2">
                  <MapPin className="size-4 text-primary" aria-hidden="true" />
                  <CardTitle className="text-base">
                    Thông tin người nhận & Địa chỉ giao hàng
                  </CardTitle>
                </div>
              </CardHeader>
              <CardContent className="grid gap-4 pt-4 text-sm sm:grid-cols-2">
                <div>
                  <p className="text-xs text-muted-foreground">Người nhận</p>
                  <p className="mt-0.5 font-semibold text-foreground">
                    {order.recipientName}
                  </p>
                  <p className="text-muted-foreground">{order.recipientPhone}</p>
                </div>

                <div>
                  <p className="text-xs text-muted-foreground">Địa chỉ nhận hàng</p>
                  <p className="mt-0.5 font-medium text-foreground">
                    {[
                      order.shippingAddress,
                      order.shippingWard,
                      order.shippingDistrict,
                      order.shippingProvince,
                    ]
                      .filter(Boolean)
                      .join(', ')}
                  </p>
                </div>

                {order.customerNote && (
                  <div className="sm:col-span-2">
                    <p className="text-xs text-muted-foreground">Ghi chú của bạn</p>
                    <p className="mt-0.5 text-foreground">{order.customerNote}</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Seller Orders */}
            <div className="space-y-4">
              <h2 className="text-base font-semibold text-foreground">
                Chi tiết theo từng cửa hàng ({order.sellerOrders?.length || 0})
              </h2>

              {(order.sellerOrders || []).map((so) => (
                <Card key={so.id} className="overflow-hidden border-border/80 shadow-sm">
                  <CardHeader className="flex flex-col gap-2 border-b border-border/60 bg-muted/20 py-3.5 sm:flex-row sm:items-center sm:justify-between">
                    <div className="flex flex-wrap items-center gap-2">
                      <Store className="size-4 text-primary" aria-hidden="true" />
                      <span className="text-sm font-semibold text-foreground">
                        {so.storeName}
                      </span>
                      <span className="font-mono text-xs text-muted-foreground">
                        ({so.sellerOrderCode})
                      </span>
                    </div>

                    <div className="flex flex-wrap items-center gap-2">
                      <OrderStatusBadge status={so.status} />
                      {so.payment && (
                        <PaymentStatusBadge status={so.payment.paymentStatus} />
                      )}
                    </div>
                  </CardHeader>

                  <CardContent className="divide-y divide-border/50 pt-2">
                    {(so.items || []).map((item) => (
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
                            <p className="font-medium text-foreground">
                              {item.productName}
                            </p>
                            <p className="text-xs text-muted-foreground">
                              {item.variantDescription || 'Mặc định'} • SKU:{' '}
                              <span className="font-mono">{item.sku}</span>
                            </p>
                            <p className="text-xs text-muted-foreground tabular-nums">
                              {formatCurrency(item.unitPrice)} × {item.quantity}
                            </p>
                          </div>
                        </div>

                        <div className="text-left sm:text-right">
                          <p className="text-sm font-bold text-foreground tabular-nums">
                            {formatCurrency(item.subtotal)}
                          </p>
                        </div>
                      </div>
                    ))}

                    <div className="flex flex-col gap-2 pt-3 text-sm sm:flex-row sm:items-center sm:justify-between">
                      <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                        <Banknote className="size-4 text-primary" aria-hidden="true" />
                        <span>
                          Thanh toán {so.payment?.paymentMethod || 'COD'}:{' '}
                          <strong className="text-foreground tabular-nums">
                            {formatCurrency(so.payment?.amount ?? so.totalAmount)}
                          </strong>
                        </span>
                      </div>

                      <div className="flex items-center gap-2">
                        <span className="text-xs text-muted-foreground">
                          Tổng đơn cửa hàng:
                        </span>
                        <span className="text-base font-bold text-primary tabular-nums">
                          {formatCurrency(so.totalAmount)}
                        </span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {/* Order Totals */}
            <Card className="border-border/80 shadow-sm">
              <CardContent className="space-y-2 pt-5 text-sm">
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
                  <span className="font-semibold text-foreground">Tổng cộng</span>
                  <span className="text-xl font-bold text-primary tabular-nums">
                    {formatCurrency(order.totalAmount)}
                  </span>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        ) : null}
      </div>
    </AppShell>
  )
}
