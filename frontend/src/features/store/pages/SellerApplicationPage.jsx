import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AlertCircle,
  CheckCircle2,
  ClipboardList,
  Loader2,
  Send,
  Store,
  XCircle,
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
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/hooks/useAuth'
import { storeApi } from '@/features/store/api/storeApi'
import {
  formatDateTime,
  StoreStatusBadge,
} from '@/features/store/components/StoreStatusBadge'
import { createSellerApplicationSchema } from '@/features/store/schemas/storeSchemas'

const MY_APPLICATIONS_KEY = ['seller-applications', 'me']

export function SellerApplicationPage() {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const [feedback, setFeedback] = useState(null)
  const [cancellingId, setCancellingId] = useState(null)

  const isSeller = (user?.roles || []).includes('SELLER')

  const applicationsQuery = useQuery({
    queryKey: MY_APPLICATIONS_KEY,
    queryFn: async () => {
      const res = await storeApi.getMySellerApplications()
      return res.data || []
    },
  })

  const applications = applicationsQuery.data || []
  const hasPending = applications.some((app) => app.status === 'PENDING')

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(createSellerApplicationSchema),
    defaultValues: {
      businessName: '',
      applicantNote: '',
    },
  })

  const submitMutation = useMutation({
    mutationFn: (values) =>
      storeApi.submitSellerApplication({
        businessName: values.businessName.trim(),
        applicantNote: values.applicantNote?.trim() || undefined,
      }),
    onSuccess: () => {
      setFeedback({
        type: 'success',
        message: 'Đã gửi hồ sơ đăng ký bán hàng thành công.',
      })
      reset()
      queryClient.invalidateQueries({ queryKey: MY_APPLICATIONS_KEY })
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể gửi hồ sơ đăng ký.',
      })
    },
  })

  const cancelMutation = useMutation({
    mutationFn: (id) => storeApi.cancelSellerApplication(id),
    onMutate: (id) => {
      setCancellingId(id)
      setFeedback(null)
    },
    onSuccess: () => {
      setFeedback({
        type: 'success',
        message: 'Đã hủy hồ sơ đăng ký đang chờ duyệt.',
      })
      queryClient.invalidateQueries({ queryKey: MY_APPLICATIONS_KEY })
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể hủy hồ sơ.',
      })
    },
    onSettled: () => {
      setCancellingId(null)
    },
  })

  const onSubmit = (values) => {
    setFeedback(null)
    submitMutation.mutate(values)
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Đăng ký trở thành người bán
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Gửi hồ sơ mở gian hàng trên SmartShop và theo dõi trạng thái phê duyệt.
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

        {isSeller ? (
          <Card className="border-blue-200 bg-secondary/40 shadow-sm">
            <CardContent className="flex flex-col gap-4 py-6 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-start gap-3">
                <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                  <Store className="size-5" aria-hidden="true" />
                </span>
                <div>
                  <h2 className="font-semibold text-foreground">
                    Tài khoản của bạn đã có quyền Seller
                  </h2>
                  <p className="mt-0.5 text-sm text-muted-foreground">
                    Bạn đã sở hữu cửa hàng và có thể cập nhật thông tin gian hàng ngay.
                  </p>
                </div>
              </div>
              <Button asChild className="shrink-0">
                <Link to="/seller/store">Quản lý cửa hàng</Link>
              </Button>
            </CardContent>
          </Card>
        ) : (
          <Card className="border-border/80 shadow-sm">
            <CardHeader>
              <CardTitle className="text-lg">Gửi hồ sơ đăng ký Seller</CardTitle>
              <CardDescription>
                Mỗi tài khoản chỉ có tối đa 1 hồ sơ đang chờ duyệt tại một thời điểm.
              </CardDescription>
            </CardHeader>
            <CardContent>
              {hasPending ? (
                <Alert className="border-amber-200 bg-amber-50 text-amber-800">
                  <AlertCircle className="size-4" aria-hidden="true" />
                  <AlertTitle>Bạn đang có hồ sơ chờ duyệt</AlertTitle>
                  <AlertDescription className="text-amber-800/90">
                    Vui lòng chờ Admin xét duyệt hoặc hủy hồ sơ đang chờ bên dưới trước khi
                    gửi hồ sơ mới.
                  </AlertDescription>
                </Alert>
              ) : (
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
                  <div className="space-y-1.5">
                    <Label htmlFor="businessName">
                      Tên cửa hàng dự kiến <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="businessName"
                      placeholder="Ví dụ: SmartTech Store"
                      aria-invalid={Boolean(errors.businessName)}
                      {...register('businessName')}
                    />
                    {errors.businessName && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.businessName.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <Label htmlFor="applicantNote">Ghi chú giới thiệu (không bắt buộc)</Label>
                    <textarea
                      id="applicantNote"
                      rows={3}
                      placeholder="Mô tả ngắn về ngành hàng hoặc kinh nghiệm bán hàng của bạn..."
                      aria-invalid={Boolean(errors.applicantNote)}
                      className="w-full min-w-0 rounded-lg border border-input bg-transparent px-3 py-2 text-sm transition-colors outline-none placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-destructive"
                      {...register('applicantNote')}
                    />
                    {errors.applicantNote && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.applicantNote.message}
                      </p>
                    )}
                  </div>

                  <Button
                    type="submit"
                    disabled={submitMutation.isPending}
                    className="cursor-pointer"
                  >
                    {submitMutation.isPending ? (
                      <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                    ) : (
                      <Send className="size-4" aria-hidden="true" />
                    )}
                    Gửi hồ sơ đăng ký
                  </Button>
                </form>
              )}
            </CardContent>
          </Card>
        )}

        <Card className="border-border/80 shadow-sm">
          <CardHeader className="border-b border-border/60">
            <div className="flex items-center gap-2">
              <ClipboardList className="size-5 text-primary" aria-hidden="true" />
              <div>
                <CardTitle className="text-lg">Lịch sử hồ sơ của tôi</CardTitle>
                <CardDescription>
                  Danh sách các hồ sơ đăng ký bán hàng bạn đã gửi.
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="pt-6">
            {applicationsQuery.isLoading ? (
              <div className="flex items-center justify-center gap-2 py-8 text-sm text-muted-foreground">
                <Loader2 className="size-4 animate-spin text-primary" aria-hidden="true" />
                Đang tải danh sách hồ sơ...
              </div>
            ) : applicationsQuery.isError ? (
              <Alert variant="destructive">
                <AlertCircle className="size-4" aria-hidden="true" />
                <AlertTitle>Không thể tải dữ liệu</AlertTitle>
                <AlertDescription>
                  {applicationsQuery.error?.message || 'Vui lòng thử lại sau.'}
                </AlertDescription>
              </Alert>
            ) : applications.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">
                Bạn chưa gửi hồ sơ đăng ký bán hàng nào.
              </p>
            ) : (
              <div className="space-y-3">
                {applications.map((app) => (
                  <div
                    key={app.id}
                    className="flex flex-col gap-3 rounded-lg border border-border/80 bg-muted/20 p-4 sm:flex-row sm:items-start sm:justify-between"
                  >
                    <div className="space-y-1.5 min-w-0">
                      <div className="flex flex-wrap items-center gap-2.5">
                        <span className="font-semibold text-foreground">
                          {app.businessName}
                        </span>
                        <StoreStatusBadge status={app.status} />
                      </div>
                      {app.applicantNote && (
                        <p className="text-sm text-muted-foreground">
                          Ghi chú: {app.applicantNote}
                        </p>
                      )}
                      {app.status === 'REJECTED' && app.rejectionReason && (
                        <p className="rounded-md border border-red-200 bg-red-50 px-2.5 py-1.5 text-xs text-red-700">
                          Lý do từ chối: {app.rejectionReason}
                        </p>
                      )}
                      <p className="text-xs text-muted-foreground">
                        Ngày gửi: {formatDateTime(app.createdAt)}
                        {app.reviewedAt
                          ? ` • Duyệt lúc: ${formatDateTime(app.reviewedAt)}`
                          : ''}
                      </p>
                    </div>

                    {app.status === 'PENDING' && (
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        disabled={cancellingId === app.id}
                        onClick={() => cancelMutation.mutate(app.id)}
                        className="shrink-0 cursor-pointer border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
                      >
                        {cancellingId === app.id ? (
                          <Loader2 className="size-3.5 animate-spin" aria-hidden="true" />
                        ) : (
                          <XCircle className="size-3.5" aria-hidden="true" />
                        )}
                        Hủy hồ sơ
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </AppShell>
  )
}
