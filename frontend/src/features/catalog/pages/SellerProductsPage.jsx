import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AlertCircle,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Loader2,
  Package,
  Pencil,
  Plus,
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
import { catalogApi } from '@/features/catalog/api/catalogApi'
import {
  formatPriceRange,
  getProductStatusMeta,
} from '@/features/catalog/utils/catalogFormatters'

export function SellerProductsPage() {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [feedback, setFeedback] = useState(null)

  const productsQuery = useQuery({
    queryKey: ['seller', 'products', page],
    queryFn: async () => {
      const res = await catalogApi.getSellerProducts({ page, size: 10 })
      return res.data
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => catalogApi.deleteSellerProduct(id),
    onMutate: () => setFeedback(null),
    onSuccess: () => {
      setFeedback({
        type: 'success',
        message: 'Đã xóa sản phẩm khỏi gian hàng.',
      })
      queryClient.invalidateQueries({ queryKey: ['seller', 'products'] })
      queryClient.invalidateQueries({ queryKey: ['public', 'products'] })
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể xóa sản phẩm.',
      })
    },
  })

  const pageData = productsQuery.data
  const products = pageData?.content || []
  const totalPages = pageData?.totalPages ?? 0

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-foreground">
              Sản phẩm của cửa hàng
            </h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Quản lý danh sách sản phẩm, biến thể giá/tồn kho và hình ảnh thuộc Store của bạn.
            </p>
          </div>
          <Button asChild className="w-fit shrink-0">
            <Link to="/seller/products/new">
              <Plus className="size-4" aria-hidden="true" />
              Thêm sản phẩm mới
            </Link>
          </Button>
        </div>

        {feedback && (
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
        )}

        <Card className="border-border/80 shadow-sm">
          <CardHeader className="border-b border-border/60">
            <CardTitle className="text-lg">Danh sách sản phẩm</CardTitle>
            <CardDescription>
              {pageData ? `Tổng cộng ${pageData.totalElements} sản phẩm` : 'Đang tải...'}
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-4">
            {productsQuery.isLoading ? (
              <div className="flex items-center justify-center gap-2 py-12 text-sm text-muted-foreground">
                <Loader2 className="size-4 animate-spin text-primary" aria-hidden="true" />
                Đang tải sản phẩm...
              </div>
            ) : productsQuery.isError ? (
              <Alert variant="destructive">
                <AlertCircle className="size-4" aria-hidden="true" />
                <AlertTitle>Không thể tải sản phẩm</AlertTitle>
                <AlertDescription>
                  {productsQuery.error?.message || 'Vui lòng thử lại sau.'}
                </AlertDescription>
              </Alert>
            ) : products.length === 0 ? (
              <div className="flex flex-col items-center justify-center gap-3 py-12 text-center">
                <span className="flex size-12 items-center justify-center rounded-full bg-secondary text-primary">
                  <Package className="size-6" aria-hidden="true" />
                </span>
                <div>
                  <p className="font-semibold text-foreground">
                    Cửa hàng chưa có sản phẩm nào
                  </p>
                  <p className="mt-1 text-sm text-muted-foreground">
                    Bắt đầu bằng cách tạo sản phẩm đầu tiên kèm biến thể và hình ảnh.
                  </p>
                </div>
                <Button asChild size="sm">
                  <Link to="/seller/products/new">
                    <Plus className="size-4" aria-hidden="true" />
                    Tạo sản phẩm
                  </Link>
                </Button>
              </div>
            ) : (
              <div className="space-y-3">
                {products.map((item) => {
                  const statusMeta = getProductStatusMeta(item.status)
                  return (
                    <div
                      key={item.id}
                      className="flex flex-col gap-4 rounded-lg border border-border/80 bg-muted/20 p-4 sm:flex-row sm:items-center sm:justify-between"
                    >
                      <div className="flex min-w-0 items-center gap-3.5">
                        <div className="size-16 shrink-0 overflow-hidden rounded-lg border border-border bg-card">
                          {item.primaryImageUrl ? (
                            <img
                              src={item.primaryImageUrl}
                              alt={item.name}
                              className="h-full w-full object-cover"
                            />
                          ) : (
                            <div className="flex h-full w-full items-center justify-center text-muted-foreground/50">
                              <Package className="size-6" aria-hidden="true" />
                            </div>
                          )}
                        </div>

                        <div className="min-w-0 space-y-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <span className="truncate font-semibold text-foreground">
                              {item.name}
                            </span>
                            <span
                              className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] font-semibold ${statusMeta.className}`}
                            >
                              {statusMeta.label}
                            </span>
                          </div>
                          <p className="text-xs text-muted-foreground">
                            Danh mục: {item.categoryName}
                            {item.brand ? ` • ${item.brand}` : ''}
                          </p>
                          <p className="text-sm font-bold text-primary">
                            {formatPriceRange(item.minPrice, item.maxPrice)}
                          </p>
                        </div>
                      </div>

                      <div className="flex shrink-0 gap-2">
                        <Button asChild variant="outline" size="sm">
                          <Link to={`/seller/products/${item.id}`}>
                            <Pencil className="size-3.5" aria-hidden="true" />
                            Quản lý
                          </Link>
                        </Button>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          disabled={deleteMutation.isPending}
                          onClick={() => deleteMutation.mutate(item.id)}
                          className="cursor-pointer border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
                        >
                          <Trash2 className="size-3.5" aria-hidden="true" />
                          Xóa
                        </Button>
                      </div>
                    </div>
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
          </CardContent>
        </Card>
      </div>
    </AppShell>
  )
}
