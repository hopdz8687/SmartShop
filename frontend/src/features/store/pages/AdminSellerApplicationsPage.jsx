import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AlertCircle,
  Check,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Eye,
  Loader2,
  X,
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
import { Label } from '@/components/ui/label'
import { storeApi } from '@/features/store/api/storeApi'
import {
  formatDateTime,
  StoreStatusBadge,
} from '@/features/store/components/StoreStatusBadge'
import { rejectSellerApplicationSchema } from '@/features/store/schemas/storeSchemas'

const STATUS_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'REJECTED', label: 'Từ chối' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]

export function AdminSellerApplicationsPage() {
  const queryClient = useQueryClient()
  const [statusFilter, setStatusFilter] = useState('PENDING')
  const [page, setPage] = useState(0)
  const [selectedId, setSelectedId] = useState(null)
  const [showRejectForm, setShowRejectForm] = useState(false)
  const [feedback, setFeedback] = useState(null)

  const listQuery = useQuery({
    queryKey: ['admin', 'seller-applications', statusFilter, page],
    queryFn: async () => {
      const res = await storeApi.getAdminSellerApplications({
        status: statusFilter || undefined,
        page,
        size: 10,
      })
      return res.data
    },
  })

  const detailQuery = useQuery({
    queryKey: ['admin', 'seller-applications', 'detail', selectedId],
    queryFn: async () => {
      const res = await storeApi.getAdminSellerApplicationById(selectedId)
      return res.data
    },
    enabled: Boolean(selectedId),
  })

  const {
    register,
    handleSubmit,
    reset: resetRejectForm,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(rejectSellerApplicationSchema),
    defaultValues: { rejectionReason: '' },
  })

  const invalidateAll = (id) => {
    queryClient.invalidateQueries({ queryKey: ['admin', 'seller-applications'] })
    if (id) {
      queryClient.invalidateQueries({
        queryKey: ['admin', 'seller-applications', 'detail', id],
      })
    }
  }

  const approveMutation = useMutation({
    mutationFn: (id) => storeApi.approveSellerApplication(id),
    onMutate: () => setFeedback(null),
    onSuccess: (_res, id) => {
      setShowRejectForm(false)
      setFeedback({
        type: 'success',
        message: 'Đã duyệt hồ sơ, tạo cửa hàng và cấp quyền Seller thành công.',
      })
      invalidateAll(id)
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể duyệt hồ sơ.',
      })
    },
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, rejectionReason }) =>
      storeApi.rejectSellerApplication(id, { rejectionReason }),
    onMutate: () => setFeedback(null),
    onSuccess: (_res, vars) => {
      setShowRejectForm(false)
      resetRejectForm()
      setFeedback({
        type: 'success',
        message: 'Đã từ chối hồ sơ đăng ký.',
      })
      invalidateAll(vars.id)
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể từ chối hồ sơ.',
      })
    },
  })

  const handleSelect = (id) => {
    setSelectedId(id)
    setShowRejectForm(false)
    resetRejectForm()
    setFeedback(null)
  }

  const handleFilterChange = (val) => {
    setStatusFilter(val)
    setPage(0)
    setSelectedId(null)
    setShowRejectForm(false)
  }

  const onRejectSubmit = (values) => {
    if (!selectedId) return
    rejectMutation.mutate({
      id: selectedId,
      rejectionReason: values.rejectionReason.trim(),
    })
  }

  const pageData = listQuery.data
  const items = pageData?.content || []
  const totalPages = pageData?.totalPages ?? 0
  const selectedApp = detailQuery.data

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Quản lý hồ sơ đăng ký Seller
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Xem xét, phê duyệt hoặc từ chối yêu cầu mở gian hàng của khách hàng.
          </p>
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

        <div className="flex flex-wrap gap-2">
          {STATUS_FILTERS.map((filter) => {
            const active = statusFilter === filter.value
            return (
              <Button
                key={filter.value || 'ALL'}
                type="button"
                variant={active ? 'default' : 'outline'}
                size="sm"
                onClick={() => handleFilterChange(filter.value)}
                className="cursor-pointer"
              >
                {filter.label}
              </Button>
            )
          })}
        </div>

        <div className="grid gap-6 lg:grid-cols-12">
          <Card className="border-border/80 shadow-sm lg:col-span-7">
            <CardHeader className="border-b border-border/60">
              <CardTitle className="text-lg">Danh sách hồ sơ</CardTitle>
              <CardDescription>
                {pageData ? `Tổng cộng ${pageData.totalElements} hồ sơ` : 'Đang tải...'}
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-4">
              {listQuery.isLoading ? (
                <div className="flex items-center justify-center gap-2 py-10 text-sm text-muted-foreground">
                  <Loader2 className="size-4 animate-spin text-primary" aria-hidden="true" />
                  Đang tải danh sách...
                </div>
              ) : listQuery.isError ? (
                <Alert variant="destructive">
                  <AlertCircle className="size-4" aria-hidden="true" />
                  <AlertTitle>Không thể tải danh sách</AlertTitle>
                  <AlertDescription>
                    {listQuery.error?.message || 'Vui lòng thử lại sau.'}
                  </AlertDescription>
                </Alert>
              ) : items.length === 0 ? (
                <p className="py-10 text-center text-sm text-muted-foreground">
                  Không có hồ sơ nào phù hợp bộ lọc.
                </p>
              ) : (
                <div className="space-y-3">
                  {items.map((item) => {
                    const isSelected = selectedId === item.id
                    return (
                      <div
                        key={item.id}
                        className={`flex flex-col gap-3 rounded-lg border p-4 transition-colors sm:flex-row sm:items-center sm:justify-between ${
                          isSelected
                            ? 'border-primary bg-secondary/50'
                            : 'border-border/80 bg-card hover:bg-muted/30'
                        }`}
                      >
                        <div className="min-w-0 space-y-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <span className="truncate font-semibold text-foreground">
                              {item.businessName}
                            </span>
                            <StoreStatusBadge status={item.status} />
                          </div>
                          <p className="truncate text-xs text-muted-foreground">
                            {item.applicantName} ({item.applicantEmail})
                          </p>
                          <p className="text-xs text-muted-foreground">
                            Gửi lúc: {formatDateTime(item.createdAt)}
                          </p>
                        </div>

                        <Button
                          type="button"
                          variant={isSelected ? 'default' : 'outline'}
                          size="sm"
                          onClick={() => handleSelect(item.id)}
                          className="shrink-0 cursor-pointer"
                        >
                          <Eye className="size-3.5" aria-hidden="true" />
                          Chi tiết
                        </Button>
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

          <Card className="border-border/80 shadow-sm lg:col-span-5">
            <CardHeader className="border-b border-border/60">
              <CardTitle className="text-lg">Chi tiết hồ sơ</CardTitle>
              <CardDescription>
                Chọn một hồ sơ từ danh sách để xem chi tiết và xử lý.
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-5">
              {!selectedId ? (
                <p className="py-10 text-center text-sm text-muted-foreground">
                  Chưa chọn hồ sơ nào.
                </p>
              ) : detailQuery.isLoading ? (
                <div className="flex items-center justify-center gap-2 py-10 text-sm text-muted-foreground">
                  <Loader2 className="size-4 animate-spin text-primary" aria-hidden="true" />
                  Đang tải chi tiết...
                </div>
              ) : detailQuery.isError ? (
                <Alert variant="destructive">
                  <AlertCircle className="size-4" aria-hidden="true" />
                  <AlertTitle>Lỗi tải chi tiết</AlertTitle>
                  <AlertDescription>
                    {detailQuery.error?.message || 'Không thể tải thông tin hồ sơ.'}
                  </AlertDescription>
                </Alert>
              ) : selectedApp ? (
                <div className="space-y-4">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <p className="text-xs font-medium text-muted-foreground">
                        Tên cửa hàng dự kiến
                      </p>
                      <h3 className="text-base font-bold text-foreground">
                        {selectedApp.businessName}
                      </h3>
                    </div>
                    <StoreStatusBadge status={selectedApp.status} />
                  </div>

                  <dl className="space-y-2.5 rounded-lg border border-border/70 bg-muted/30 p-3.5 text-sm">
                    <div>
                      <dt className="text-xs text-muted-foreground">Người đăng ký</dt>
                      <dd className="font-medium text-foreground">
                        {selectedApp.applicantName}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-xs text-muted-foreground">Email</dt>
                      <dd className="font-medium text-foreground">
                        {selectedApp.applicantEmail}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-xs text-muted-foreground">Ghi chú</dt>
                      <dd className="text-foreground">
                        {selectedApp.applicantNote || 'Không có'}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-xs text-muted-foreground">Ngày gửi</dt>
                      <dd className="text-foreground">
                        {formatDateTime(selectedApp.createdAt)}
                      </dd>
                    </div>
                    {selectedApp.reviewedAt && (
                      <div>
                        <dt className="text-xs text-muted-foreground">Thời gian xử lý</dt>
                        <dd className="text-foreground">
                          {formatDateTime(selectedApp.reviewedAt)}
                        </dd>
                      </div>
                    )}
                    {selectedApp.rejectionReason && (
                      <div>
                        <dt className="text-xs text-red-600">Lý do từ chối</dt>
                        <dd className="font-medium text-red-700">
                          {selectedApp.rejectionReason}
                        </dd>
                      </div>
                    )}
                  </dl>

                  {selectedApp.status === 'PENDING' && (
                    <div className="space-y-3 border-t border-border/60 pt-4">
                      <div className="flex flex-wrap gap-2">
                        <Button
                          type="button"
                          disabled={
                            approveMutation.isPending || rejectMutation.isPending
                          }
                          onClick={() => approveMutation.mutate(selectedApp.id)}
                          className="flex-1 cursor-pointer"
                        >
                          {approveMutation.isPending ? (
                            <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                          ) : (
                            <Check className="size-4" aria-hidden="true" />
                          )}
                          Duyệt hồ sơ
                        </Button>
                        <Button
                          type="button"
                          variant="outline"
                          disabled={
                            approveMutation.isPending || rejectMutation.isPending
                          }
                          onClick={() => setShowRejectForm((prev) => !prev)}
                          className="flex-1 cursor-pointer border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
                        >
                          <X className="size-4" aria-hidden="true" />
                          Từ chối
                        </Button>
                      </div>

                      {showRejectForm && (
                        <form
                          onSubmit={handleSubmit(onRejectSubmit)}
                          className="space-y-3 rounded-lg border border-red-200 bg-red-50/40 p-3"
                          noValidate
                        >
                          <div className="space-y-1.5">
                            <Label htmlFor="rejectionReason">
                              Lý do từ chối <span className="text-destructive">*</span>
                            </Label>
                            <textarea
                              id="rejectionReason"
                              rows={3}
                              placeholder="Nhập lý do từ chối để người đăng ký biết..."
                              aria-invalid={Boolean(errors.rejectionReason)}
                              className="w-full min-w-0 rounded-lg border border-input bg-card px-3 py-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                              {...register('rejectionReason')}
                            />
                            {errors.rejectionReason && (
                              <p className="text-xs font-medium text-destructive">
                                {errors.rejectionReason.message}
                              </p>
                            )}
                          </div>
                          <div className="flex justify-end gap-2">
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              onClick={() => setShowRejectForm(false)}
                              className="cursor-pointer"
                            >
                              Hủy
                            </Button>
                            <Button
                              type="submit"
                              variant="destructive"
                              size="sm"
                              disabled={rejectMutation.isPending}
                              className="cursor-pointer"
                            >
                              {rejectMutation.isPending && (
                                <Loader2
                                  className="size-3.5 animate-spin"
                                  aria-hidden="true"
                                />
                              )}
                              Xác nhận từ chối
                            </Button>
                          </div>
                        </form>
                      )}
                    </div>
                  )}
                </div>
              ) : null}
            </CardContent>
          </Card>
        </div>
      </div>
    </AppShell>
  )
}
