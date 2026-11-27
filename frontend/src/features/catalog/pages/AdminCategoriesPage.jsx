import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AlertCircle,
  CheckCircle2,
  EyeOff,
  FolderTree,
  Loader2,
  Pencil,
  Plus,
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
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { StoreStatusBadge } from '@/features/store/components/StoreStatusBadge'
import { catalogApi } from '@/features/catalog/api/catalogApi'
import { categorySchema } from '@/features/catalog/schemas/catalogSchemas'

const ADMIN_CATEGORIES_KEY = ['admin', 'categories']

export function AdminCategoriesPage() {
  const queryClient = useQueryClient()
  const [editingCat, setEditingCat] = useState(null)
  const [feedback, setFeedback] = useState(null)

  const categoriesQuery = useQuery({
    queryKey: ADMIN_CATEGORIES_KEY,
    queryFn: async () => {
      const res = await catalogApi.getAdminCategories()
      return res.data || []
    },
  })

  const categories = categoriesQuery.data || []

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      name: '',
      parentId: '',
      description: '',
      status: 'ACTIVE',
    },
  })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ADMIN_CATEGORIES_KEY })
    queryClient.invalidateQueries({ queryKey: ['public', 'categories'] })
  }

  const saveMutation = useMutation({
    mutationFn: (values) => {
      const payload = {
        name: values.name.trim(),
        parentId: values.parentId ? Number(values.parentId) : null,
        description: values.description?.trim() || null,
        status: values.status,
      }
      return editingCat
        ? catalogApi.updateCategory(editingCat.id, payload)
        : catalogApi.createCategory(payload)
    },
    onMutate: () => setFeedback(null),
    onSuccess: () => {
      setFeedback({
        type: 'success',
        message: editingCat
          ? 'Đã cập nhật danh mục thành công.'
          : 'Đã tạo danh mục mới thành công.',
      })
      setEditingCat(null)
      reset({ name: '', parentId: '', description: '', status: 'ACTIVE' })
      invalidate()
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể lưu danh mục.',
      })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => catalogApi.deleteCategory(id),
    onMutate: () => setFeedback(null),
    onSuccess: () => {
      setFeedback({
        type: 'success',
        message: 'Đã chuyển danh mục sang trạng thái ngừng hoạt động.',
      })
      invalidate()
    },
    onError: (err) => {
      setFeedback({
        type: 'error',
        message: err?.message || 'Không thể ẩn danh mục.',
      })
    },
  })

  const startEdit = (cat) => {
    setEditingCat(cat)
    setFeedback(null)
    reset({
      name: cat.name || '',
      parentId: cat.parentId ? String(cat.parentId) : '',
      description: cat.description || '',
      status: cat.status || 'ACTIVE',
    })
  }

  const cancelEdit = () => {
    setEditingCat(null)
    reset({ name: '', parentId: '', description: '', status: 'ACTIVE' })
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Quản lý danh mục sản phẩm
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Tạo mới, chỉnh sửa phân cấp và quản lý trạng thái hiển thị danh mục.
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

        <div className="grid gap-6 lg:grid-cols-12">
          <Card className="border-border/80 shadow-sm lg:col-span-5">
            <CardHeader className="border-b border-border/60">
              <div className="flex items-center justify-between gap-2">
                <div>
                  <CardTitle className="text-lg">
                    {editingCat ? `Sửa danh mục #${editingCat.id}` : 'Tạo danh mục mới'}
                  </CardTitle>
                  <CardDescription>
                    Slug sẽ được hệ thống tự động tạo từ tên danh mục.
                  </CardDescription>
                </div>
                {editingCat && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={cancelEdit}
                    aria-label="Hủy chỉnh sửa"
                    className="cursor-pointer"
                  >
                    <X className="size-4" aria-hidden="true" />
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent className="pt-5">
              <form
                onSubmit={handleSubmit((vals) => saveMutation.mutate(vals))}
                className="space-y-4"
                noValidate
              >
                <div className="space-y-1.5">
                  <Label htmlFor="catName">
                    Tên danh mục <span className="text-destructive">*</span>
                  </Label>
                  <Input
                    id="catName"
                    placeholder="Ví dụ: Điện thoại & Phụ kiện"
                    aria-invalid={Boolean(errors.name)}
                    {...register('name')}
                  />
                  {errors.name && (
                    <p className="text-xs font-medium text-destructive">
                      {errors.name.message}
                    </p>
                  )}
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="catParent">Danh mục cha (không bắt buộc)</Label>
                  <select
                    id="catParent"
                    className="h-8 w-full rounded-lg border border-input bg-card px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                    {...register('parentId')}
                  >
                    <option value="">-- Không có (danh mục gốc) --</option>
                    {categories
                      .filter((c) => !editingCat || c.id !== editingCat.id)
                      .map((c) => (
                        <option key={c.id} value={c.id}>
                          {c.name}
                        </option>
                      ))}
                  </select>
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="catStatus">Trạng thái</Label>
                  <select
                    id="catStatus"
                    className="h-8 w-full rounded-lg border border-input bg-card px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                    {...register('status')}
                  >
                    <option value="ACTIVE">Đang hoạt động (ACTIVE)</option>
                    <option value="INACTIVE">Ngừng hoạt động (INACTIVE)</option>
                  </select>
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="catDescription">Mô tả</Label>
                  <textarea
                    id="catDescription"
                    rows={3}
                    placeholder="Mô tả ngắn về danh mục..."
                    className="w-full min-w-0 rounded-lg border border-input bg-transparent px-3 py-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                    {...register('description')}
                  />
                  {errors.description && (
                    <p className="text-xs font-medium text-destructive">
                      {errors.description.message}
                    </p>
                  )}
                </div>

                <div className="flex gap-2">
                  <Button
                    type="submit"
                    disabled={saveMutation.isPending}
                    className="flex-1 cursor-pointer"
                  >
                    {saveMutation.isPending ? (
                      <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                    ) : (
                      <Plus className="size-4" aria-hidden="true" />
                    )}
                    {editingCat ? 'Lưu cập nhật' : 'Tạo danh mục'}
                  </Button>
                  {editingCat && (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={cancelEdit}
                      className="cursor-pointer"
                    >
                      Hủy
                    </Button>
                  )}
                </div>
              </form>
            </CardContent>
          </Card>

          <Card className="border-border/80 shadow-sm lg:col-span-7">
            <CardHeader className="border-b border-border/60">
              <div className="flex items-center gap-2">
                <FolderTree className="size-5 text-primary" aria-hidden="true" />
                <div>
                  <CardTitle className="text-lg">Danh sách danh mục</CardTitle>
                  <CardDescription>
                    {categories.length} danh mục trong hệ thống
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="pt-4">
              {categoriesQuery.isLoading ? (
                <div className="flex items-center justify-center gap-2 py-10 text-sm text-muted-foreground">
                  <Loader2 className="size-4 animate-spin text-primary" aria-hidden="true" />
                  Đang tải danh mục...
                </div>
              ) : categories.length === 0 ? (
                <p className="py-10 text-center text-sm text-muted-foreground">
                  Chưa có danh mục nào.
                </p>
              ) : (
                <div className="space-y-2.5">
                  {categories.map((cat) => (
                    <div
                      key={cat.id}
                      className="flex flex-col gap-3 rounded-lg border border-border/80 bg-muted/20 p-3.5 sm:flex-row sm:items-center sm:justify-between"
                    >
                      <div className="min-w-0 space-y-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="font-semibold text-foreground">
                            {cat.name}
                          </span>
                          <StoreStatusBadge status={cat.status} />
                        </div>
                        <p className="text-xs text-muted-foreground">
                          Slug: <span className="font-mono">{cat.slug}</span>
                          {cat.parentName ? ` • Cha: ${cat.parentName}` : ''}
                        </p>
                        {cat.description && (
                          <p className="line-clamp-1 text-xs text-muted-foreground">
                            {cat.description}
                          </p>
                        )}
                      </div>

                      <div className="flex shrink-0 gap-2">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => startEdit(cat)}
                          className="cursor-pointer"
                        >
                          <Pencil className="size-3.5" aria-hidden="true" />
                          Sửa
                        </Button>
                        {cat.status === 'ACTIVE' && (
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            disabled={deleteMutation.isPending}
                            onClick={() => deleteMutation.mutate(cat.id)}
                            className="cursor-pointer border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
                          >
                            <EyeOff className="size-3.5" aria-hidden="true" />
                            Ẩn
                          </Button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </AppShell>
  )
}
