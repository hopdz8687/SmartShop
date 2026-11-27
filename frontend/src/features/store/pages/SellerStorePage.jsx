import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AlertCircle, CheckCircle2, Loader2, Save, Store } from 'lucide-react'
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
import { storeApi } from '@/features/store/api/storeApi'
import {
  formatDateTime,
  StoreStatusBadge,
} from '@/features/store/components/StoreStatusBadge'
import { updateStoreSchema } from '@/features/store/schemas/storeSchemas'

const MY_STORE_KEY = ['seller', 'store', 'me']

export function SellerStorePage() {
  const queryClient = useQueryClient()
  const [feedback, setFeedback] = useState(null)

  const storeQuery = useQuery({
    queryKey: MY_STORE_KEY,
    queryFn: async () => {
      const res = await storeApi.getMyStore()
      return res.data
    },
  })

  const store = storeQuery.data

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(updateStoreSchema),
    defaultValues: {
      storeName: '',
      description: '',
      logoUrl: '',
      contactEmail: '',
      contactPhone: '',
    },
  })

  useEffect(() => {
    if (store) {
      reset({
        storeName: store.storeName || '',
        description: store.description || '',
        logoUrl: store.logoUrl || '',
        contactEmail: store.contactEmail || '',
        contactPhone: store.contactPhone || '',
      })
    }
  }, [store, reset])

  const updateMutation = useMutation({
    mutationFn: (values) =>
      storeApi.updateMyStore({
        storeName: values.storeName.trim(),
        description: values.description?.trim() || '',
        logoUrl: values.logoUrl?.trim() || '',
        contactEmail: values.contactEmail?.trim() || '',
        contactPhone: values.contactPhone?.trim() || '',
      }),
    onMutate: () => setFeedback(null),
    onSuccess: (res) => {
      queryClient.setQueryData(MY_STORE_KEY, res.data)
      setFeedback({
        type: 'success',
        message: 'Đã cập nhật thông tin cửa hàng thành công.',
      })
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể cập nhật thông tin cửa hàng.',
      })
    },
  })

  const onSubmit = (values) => {
    updateMutation.mutate(values)
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Cửa hàng của tôi
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Xem và cập nhật thông tin hiển thị của gian hàng trên SmartShop.
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

        {storeQuery.isLoading ? (
          <Card className="border-border/80 shadow-sm">
            <CardContent className="flex items-center justify-center gap-2 py-12 text-sm text-muted-foreground">
              <Loader2 className="size-4 animate-spin text-primary" aria-hidden="true" />
              Đang tải thông tin cửa hàng...
            </CardContent>
          </Card>
        ) : storeQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không thể tải cửa hàng</AlertTitle>
            <AlertDescription>
              {storeQuery.error?.message || 'Vui lòng thử lại sau.'}
            </AlertDescription>
          </Alert>
        ) : store ? (
          <Card className="border-border/80 shadow-sm">
            <CardHeader className="border-b border-border/60">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex items-center gap-3">
                  <span className="flex size-11 items-center justify-center rounded-lg bg-secondary text-primary border border-blue-200">
                    <Store className="size-5" aria-hidden="true" />
                  </span>
                  <div>
                    <CardTitle className="text-xl">{store.storeName}</CardTitle>
                    <CardDescription className="mt-0.5">
                      Slug: <span className="font-mono text-xs">{store.slug}</span> • Tạo
                      lúc: {formatDateTime(store.createdAt)}
                    </CardDescription>
                  </div>
                </div>
                <StoreStatusBadge status={store.status} />
              </div>
            </CardHeader>

            <CardContent className="pt-6">
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="storeName">
                      Tên cửa hàng <span className="text-destructive">*</span>
                    </Label>
                    <Input
                      id="storeName"
                      aria-invalid={Boolean(errors.storeName)}
                      {...register('storeName')}
                    />
                    {errors.storeName && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.storeName.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <Label htmlFor="contactEmail">Email liên hệ</Label>
                    <Input
                      id="contactEmail"
                      type="email"
                      placeholder="store@example.com"
                      aria-invalid={Boolean(errors.contactEmail)}
                      {...register('contactEmail')}
                    />
                    {errors.contactEmail && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.contactEmail.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <Label htmlFor="contactPhone">Số điện thoại liên hệ</Label>
                    <Input
                      id="contactPhone"
                      placeholder="0909123456"
                      aria-invalid={Boolean(errors.contactPhone)}
                      {...register('contactPhone')}
                    />
                    {errors.contactPhone && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.contactPhone.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="logoUrl">Logo URL</Label>
                    <Input
                      id="logoUrl"
                      placeholder="https://example.com/logo.png"
                      aria-invalid={Boolean(errors.logoUrl)}
                      {...register('logoUrl')}
                    />
                    {errors.logoUrl && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.logoUrl.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="description">Mô tả cửa hàng</Label>
                    <textarea
                      id="description"
                      rows={4}
                      placeholder="Giới thiệu về cửa hàng, sản phẩm chủ đạo và chính sách hỗ trợ..."
                      aria-invalid={Boolean(errors.description)}
                      className="w-full min-w-0 rounded-lg border border-input bg-transparent px-3 py-2 text-sm transition-colors outline-none placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-destructive"
                      {...register('description')}
                    />
                    {errors.description && (
                      <p className="text-xs font-medium text-destructive">
                        {errors.description.message}
                      </p>
                    )}
                  </div>
                </div>

                <div className="flex justify-end pt-2">
                  <Button
                    type="submit"
                    disabled={updateMutation.isPending}
                    className="cursor-pointer"
                  >
                    {updateMutation.isPending ? (
                      <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                    ) : (
                      <Save className="size-4" aria-hidden="true" />
                    )}
                    Lưu thay đổi
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        ) : null}
      </div>
    </AppShell>
  )
}
