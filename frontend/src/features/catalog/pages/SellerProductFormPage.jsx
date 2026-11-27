import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useFieldArray, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  ImagePlus,
  Loader2,
  Plus,
  Save,
  Star,
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
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { catalogApi } from '@/features/catalog/api/catalogApi'
import {
  createProductSchema,
  productImageSchema,
  updateProductSchema,
  variantItemSchema,
} from '@/features/catalog/schemas/catalogSchemas'
import { formatCurrency } from '@/features/catalog/utils/catalogFormatters'

export function SellerProductFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [feedback, setFeedback] = useState(null)
  const [editingVariant, setEditingVariant] = useState(null)

  const categoriesQuery = useQuery({
    queryKey: ['public', 'categories'],
    queryFn: async () => {
      const res = await catalogApi.getPublicCategories()
      return res.data || []
    },
  })

  const productQuery = useQuery({
    queryKey: ['seller', 'products', 'detail', id],
    queryFn: async () => {
      const res = await catalogApi.getSellerProductDetail(id)
      return res.data
    },
    enabled: isEdit,
  })

  const categories = categoriesQuery.data || []
  const product = productQuery.data

  // ── Create Form ──────────────────────────────────────────────────────────────
  const createForm = useForm({
    resolver: zodResolver(createProductSchema),
    defaultValues: {
      categoryId: '',
      name: '',
      brand: '',
      description: '',
      status: 'ACTIVE',
      primaryImageUrl: '',
      variants: [
        {
          sku: '',
          variantName: 'Mặc định',
          price: 0,
          stockQuantity: 1,
          isDefault: true,
          status: 'ACTIVE',
        },
      ],
    },
  })

  const variantFields = useFieldArray({
    control: createForm.control,
    name: 'variants',
  })

  // ── Edit Product Info Form ───────────────────────────────────────────────────
  const editForm = useForm({
    resolver: zodResolver(updateProductSchema),
    defaultValues: {
      categoryId: '',
      name: '',
      brand: '',
      description: '',
      status: 'ACTIVE',
    },
  })

  useEffect(() => {
    if (isEdit && product) {
      editForm.reset({
        categoryId: String(product.categoryId || ''),
        name: product.name || '',
        brand: product.brand || '',
        description: product.description || '',
        status: product.status || 'ACTIVE',
      })
    }
  }, [isEdit, product, editForm])

  // ── Variant & Image Forms (Edit Mode) ────────────────────────────────────────
  const variantForm = useForm({
    resolver: zodResolver(variantItemSchema),
    defaultValues: {
      sku: '',
      variantName: '',
      price: 0,
      stockQuantity: 0,
      isDefault: false,
      status: 'ACTIVE',
    },
  })

  const imageForm = useForm({
    resolver: zodResolver(productImageSchema),
    defaultValues: {
      imageUrl: '',
      altText: '',
      displayOrder: 0,
      isPrimary: false,
    },
  })

  const invalidateProduct = () => {
    queryClient.invalidateQueries({ queryKey: ['seller', 'products'] })
    queryClient.invalidateQueries({ queryKey: ['public', 'products'] })
    if (id) {
      queryClient.invalidateQueries({ queryKey: ['seller', 'products', 'detail', id] })
    }
  }

  const createMutation = useMutation({
    mutationFn: (values) => {
      const payload = {
        categoryId: Number(values.categoryId),
        name: values.name.trim(),
        brand: values.brand?.trim() || undefined,
        description: values.description?.trim() || undefined,
        status: values.status,
        variants: values.variants.map((v) => ({
          sku: v.sku.trim(),
          variantName: v.variantName?.trim() || undefined,
          price: Number(v.price),
          stockQuantity: Number(v.stockQuantity),
          isDefault: Boolean(v.isDefault),
          status: v.status,
        })),
        images: values.primaryImageUrl?.trim()
          ? [{ imageUrl: values.primaryImageUrl.trim(), isPrimary: true, displayOrder: 0 }]
          : [],
      }
      return catalogApi.createSellerProduct(payload)
    },
    onMutate: () => setFeedback(null),
    onSuccess: (res) => {
      invalidateProduct()
      navigate(`/seller/products/${res.data.id}`, { replace: true })
    },
    onError: (err) => {
      setFeedback({ type: 'error', message: err?.message || 'Không thể tạo sản phẩm.' })
    },
  })

  const updateMutation = useMutation({
    mutationFn: (values) =>
      catalogApi.updateSellerProduct(id, {
        categoryId: Number(values.categoryId),
        name: values.name.trim(),
        brand: values.brand?.trim() || undefined,
        description: values.description?.trim() || undefined,
        status: values.status,
      }),
    onMutate: () => setFeedback(null),
    onSuccess: () => {
      setFeedback({ type: 'success', message: 'Đã cập nhật thông tin sản phẩm.' })
      invalidateProduct()
    },
    onError: (err) => {
      setFeedback({ type: 'error', message: err?.message || 'Không thể cập nhật sản phẩm.' })
    },
  })

  const saveVariantMutation = useMutation({
    mutationFn: (values) => {
      const payload = {
        sku: values.sku.trim(),
        variantName: values.variantName?.trim() || undefined,
        price: Number(values.price),
        stockQuantity: Number(values.stockQuantity),
        isDefault: Boolean(values.isDefault),
        status: values.status,
      }
      return editingVariant
        ? catalogApi.updateSellerVariant(editingVariant.id, payload)
        : catalogApi.addSellerVariant(id, payload)
    },
    onMutate: () => setFeedback(null),
    onSuccess: () => {
      setFeedback({
        type: 'success',
        message: editingVariant ? 'Đã cập nhật biến thể.' : 'Đã thêm biến thể mới.',
      })
      setEditingVariant(null)
      variantForm.reset({
        sku: '',
        variantName: '',
        price: 0,
        stockQuantity: 0,
        isDefault: false,
        status: 'ACTIVE',
      })
      invalidateProduct()
    },
    onError: (err) => {
      setFeedback({ type: 'error', message: err?.message || 'Không thể lưu biến thể.' })
    },
  })

  const deactivateVariantMutation = useMutation({
    mutationFn: (variantId) => catalogApi.deactivateSellerVariant(variantId),
    onMutate: () => setFeedback(null),
    onSuccess: () => {
      setFeedback({ type: 'success', message: 'Đã ngừng hoạt động biến thể.' })
      invalidateProduct()
    },
    onError: (err) => {
      setFeedback({ type: 'error', message: err?.message || 'Không thể ngừng biến thể.' })
    },
  })

  const addImageMutation = useMutation({
    mutationFn: (values) =>
      catalogApi.addSellerImage(id, {
        imageUrl: values.imageUrl.trim(),
        altText: values.altText?.trim() || undefined,
        displayOrder: Number(values.displayOrder || 0),
        isPrimary: Boolean(values.isPrimary),
      }),
    onMutate: () => setFeedback(null),
    onSuccess: () => {
      setFeedback({ type: 'success', message: 'Đã thêm hình ảnh sản phẩm.' })
      imageForm.reset({ imageUrl: '', altText: '', displayOrder: 0, isPrimary: false })
      invalidateProduct()
    },
    onError: (err) => {
      setFeedback({ type: 'error', message: err?.message || 'Không thể thêm hình ảnh.' })
    },
  })

  const deleteImageMutation = useMutation({
    mutationFn: (imageId) => catalogApi.deleteSellerImage(imageId),
    onMutate: () => setFeedback(null),
    onSuccess: () => {
      setFeedback({ type: 'success', message: 'Đã xóa hình ảnh.' })
      invalidateProduct()
    },
    onError: (err) => {
      setFeedback({ type: 'error', message: err?.message || 'Không thể xóa hình ảnh.' })
    },
  })

  const handleSetCreateDefault = (targetIndex) => {
    const current = createForm.getValues('variants')
    current.forEach((_, idx) => {
      createForm.setValue(`variants.${idx}.isDefault`, idx === targetIndex)
    })
  }

  const startEditVariant = (v) => {
    setEditingVariant(v)
    variantForm.reset({
      sku: v.sku || '',
      variantName: v.variantName || '',
      price: Number(v.price ?? 0),
      stockQuantity: Number(v.stockQuantity ?? 0),
      isDefault: Boolean(v.isDefault),
      status: v.status || 'ACTIVE',
    })
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <Button asChild variant="ghost" size="sm" className="-ml-2 text-muted-foreground">
            <Link to="/seller/products">
              <ArrowLeft className="size-4" aria-hidden="true" />
              Quay lại danh sách sản phẩm
            </Link>
          </Button>
        </div>

        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            {isEdit ? 'Quản lý chi tiết sản phẩm' : 'Tạo sản phẩm mới'}
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            {isEdit
              ? 'Cập nhật thông tin sản phẩm, biến thể giá/tồn kho và thư viện hình ảnh.'
              : 'Nhập thông tin sản phẩm kèm ít nhất 1 biến thể đang hoạt động (ACTIVE) làm mặc định.'}
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

        {!isEdit ? (
          <form
            onSubmit={createForm.handleSubmit((vals) => createMutation.mutate(vals))}
            className="space-y-6"
            noValidate
          >
            <Card className="border-border/80 shadow-sm">
              <CardHeader className="border-b border-border/60">
                <CardTitle className="text-lg">Thông tin chung</CardTitle>
              </CardHeader>
              <CardContent className="grid gap-4 pt-5 sm:grid-cols-2">
                <div className="space-y-1.5 sm:col-span-2">
                  <Label htmlFor="createName">
                    Tên sản phẩm <span className="text-destructive">*</span>
                  </Label>
                  <Input
                    id="createName"
                    placeholder="Ví dụ: Bàn phím cơ SmartKey Pro"
                    {...createForm.register('name')}
                  />
                  {createForm.formState.errors.name && (
                    <p className="text-xs font-medium text-destructive">
                      {createForm.formState.errors.name.message}
                    </p>
                  )}
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="createCategory">
                    Danh mục <span className="text-destructive">*</span>
                  </Label>
                  <select
                    id="createCategory"
                    className="h-8 w-full rounded-lg border border-input bg-card px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                    {...createForm.register('categoryId')}
                  >
                    <option value="">-- Chọn danh mục --</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                  {createForm.formState.errors.categoryId && (
                    <p className="text-xs font-medium text-destructive">
                      {createForm.formState.errors.categoryId.message}
                    </p>
                  )}
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="createBrand">Thương hiệu</Label>
                  <Input id="createBrand" {...createForm.register('brand')} />
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="createStatus">Trạng thái mở bán</Label>
                  <select
                    id="createStatus"
                    className="h-8 w-full rounded-lg border border-input bg-card px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                    {...createForm.register('status')}
                  >
                    <option value="ACTIVE">Đang bán (ACTIVE)</option>
                    <option value="DRAFT">Nháp (DRAFT)</option>
                  </select>
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="createPrimaryImageUrl">URL ảnh chính (không bắt buộc)</Label>
                  <Input
                    id="createPrimaryImageUrl"
                    placeholder="https://example.com/product.jpg"
                    {...createForm.register('primaryImageUrl')}
                  />
                </div>

                <div className="space-y-1.5 sm:col-span-2">
                  <Label htmlFor="createDescription">Mô tả</Label>
                  <textarea
                    id="createDescription"
                    rows={3}
                    className="w-full min-w-0 rounded-lg border border-input bg-transparent px-3 py-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                    {...createForm.register('description')}
                  />
                </div>
              </CardContent>
            </Card>

            <Card className="border-border/80 shadow-sm">
              <CardHeader className="flex flex-row items-center justify-between border-b border-border/60">
                <div>
                  <CardTitle className="text-lg">Biến thể sản phẩm (Variants)</CardTitle>
                  <CardDescription>
                    Bắt buộc có ít nhất 1 biến thể ACTIVE và đúng 1 biến thể mặc định.
                  </CardDescription>
                </div>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() =>
                    variantFields.append({
                      sku: '',
                      variantName: '',
                      price: 0,
                      stockQuantity: 1,
                      isDefault: false,
                      status: 'ACTIVE',
                    })
                  }
                  className="cursor-pointer"
                >
                  <Plus className="size-4" aria-hidden="true" />
                  Thêm biến thể
                </Button>
              </CardHeader>
              <CardContent className="space-y-4 pt-5">
                {variantFields.fields.map((field, index) => (
                  <div
                    key={field.id}
                    className="grid gap-3 rounded-lg border border-border/80 bg-muted/20 p-4 sm:grid-cols-12"
                  >
                    <div className="space-y-1 sm:col-span-3">
                      <Label>SKU *</Label>
                      <Input
                        placeholder="SKU-001"
                        {...createForm.register(`variants.${index}.sku`)}
                      />
                      {createForm.formState.errors.variants?.[index]?.sku && (
                        <p className="text-xs text-destructive">
                          {createForm.formState.errors.variants[index].sku.message}
                        </p>
                      )}
                    </div>

                    <div className="space-y-1 sm:col-span-3">
                      <Label>Tên phân loại</Label>
                      <Input
                        placeholder="Đen / 128GB"
                        {...createForm.register(`variants.${index}.variantName`)}
                      />
                    </div>

                    <div className="space-y-1 sm:col-span-2">
                      <Label>Giá (₫) *</Label>
                      <Input
                        type="number"
                        min={0}
                        {...createForm.register(`variants.${index}.price`)}
                      />
                    </div>

                    <div className="space-y-1 sm:col-span-2">
                      <Label>Tồn kho *</Label>
                      <Input
                        type="number"
                        min={0}
                        {...createForm.register(`variants.${index}.stockQuantity`)}
                      />
                    </div>

                    <div className="flex items-end justify-between gap-2 sm:col-span-2">
                      <label className="flex cursor-pointer items-center gap-1.5 text-xs font-medium">
                        <input
                          type="radio"
                          name="defaultVariantRadio"
                          checked={Boolean(createForm.watch(`variants.${index}.isDefault`))}
                          onChange={() => handleSetCreateDefault(index)}
                        />
                        Mặc định
                      </label>
                      {variantFields.fields.length > 1 && (
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          onClick={() => variantFields.remove(index)}
                          aria-label="Xóa dòng biến thể"
                          className="cursor-pointer text-red-600"
                        >
                          <Trash2 className="size-4" aria-hidden="true" />
                        </Button>
                      )}
                    </div>
                  </div>
                ))}

                <div className="flex justify-end pt-2">
                  <Button
                    type="submit"
                    disabled={createMutation.isPending}
                    className="cursor-pointer"
                  >
                    {createMutation.isPending ? (
                      <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                    ) : (
                      <Save className="size-4" aria-hidden="true" />
                    )}
                    Tạo sản phẩm
                  </Button>
                </div>
              </CardContent>
            </Card>
          </form>
        ) : productQuery.isLoading ? (
          <div className="flex items-center justify-center gap-2 py-12 text-sm text-muted-foreground">
            <Loader2 className="size-5 animate-spin text-primary" aria-hidden="true" />
            Đang tải thông tin sản phẩm...
          </div>
        ) : productQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không thể tải sản phẩm</AlertTitle>
            <AlertDescription>
              {productQuery.error?.message || 'Vui lòng thử lại sau.'}
            </AlertDescription>
          </Alert>
        ) : product ? (
          <div className="space-y-6">
            {/* Edit Product Info */}
            <Card className="border-border/80 shadow-sm">
              <CardHeader className="border-b border-border/60">
                <CardTitle className="text-lg">Thông tin sản phẩm</CardTitle>
                <CardDescription>Slug: {product.slug}</CardDescription>
              </CardHeader>
              <CardContent className="pt-5">
                <form
                  onSubmit={editForm.handleSubmit((vals) => updateMutation.mutate(vals))}
                  className="grid gap-4 sm:grid-cols-2"
                  noValidate
                >
                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="editName">Tên sản phẩm *</Label>
                    <Input id="editName" {...editForm.register('name')} />
                    {editForm.formState.errors.name && (
                      <p className="text-xs text-destructive">
                        {editForm.formState.errors.name.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <Label htmlFor="editCategory">Danh mục *</Label>
                    <select
                      id="editCategory"
                      className="h-8 w-full rounded-lg border border-input bg-card px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                      {...editForm.register('categoryId')}
                    >
                      {categories.map((c) => (
                        <option key={c.id} value={c.id}>
                          {c.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="space-y-1.5">
                    <Label htmlFor="editStatus">Trạng thái</Label>
                    <select
                      id="editStatus"
                      className="h-8 w-full rounded-lg border border-input bg-card px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                      {...editForm.register('status')}
                    >
                      <option value="ACTIVE">Đang bán (ACTIVE)</option>
                      <option value="DRAFT">Nháp (DRAFT)</option>
                      <option value="INACTIVE">Ngừng bán (INACTIVE)</option>
                    </select>
                  </div>

                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="editBrand">Thương hiệu</Label>
                    <Input id="editBrand" {...editForm.register('brand')} />
                  </div>

                  <div className="space-y-1.5 sm:col-span-2">
                    <Label htmlFor="editDescription">Mô tả</Label>
                    <textarea
                      id="editDescription"
                      rows={3}
                      className="w-full min-w-0 rounded-lg border border-input bg-transparent px-3 py-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                      {...editForm.register('description')}
                    />
                  </div>

                  <div className="flex justify-end sm:col-span-2">
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
                      Lưu thông tin
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>

            {/* Variants Manager */}
            <Card className="border-border/80 shadow-sm">
              <CardHeader className="border-b border-border/60">
                <CardTitle className="text-lg">Quản lý biến thể (Variants)</CardTitle>
                <CardDescription>
                  Thêm, sửa hoặc ngừng hoạt động biến thể. Luôn giữ ít nhất 1 biến thể ACTIVE mặc định.
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-5 pt-5">
                <div className="space-y-2.5">
                  {(product.variants || []).map((v) => (
                    <div
                      key={v.id}
                      className="flex flex-col gap-3 rounded-lg border border-border/80 bg-muted/20 p-3.5 sm:flex-row sm:items-center sm:justify-between"
                    >
                      <div className="space-y-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="font-semibold text-foreground">
                            {v.variantName || v.sku}
                          </span>
                          {v.isDefault && (
                            <span className="rounded-full border border-blue-200 bg-secondary px-2 py-0.5 text-[11px] font-semibold text-primary">
                              Mặc định
                            </span>
                          )}
                          <span
                            className={`rounded-full border px-2 py-0.5 text-[11px] font-semibold ${
                              v.status === 'ACTIVE'
                                ? 'border-green-200 bg-green-50 text-green-700'
                                : 'border-slate-200 bg-slate-100 text-slate-600'
                            }`}
                          >
                            {v.status}
                          </span>
                        </div>
                        <p className="text-xs text-muted-foreground">
                          SKU: <span className="font-mono">{v.sku}</span> • Giá:{' '}
                          <strong className="text-primary">{formatCurrency(v.price)}</strong> •
                          Kho: {v.stockQuantity}
                        </p>
                      </div>

                      <div className="flex gap-2">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => startEditVariant(v)}
                          className="cursor-pointer"
                        >
                          Sửa
                        </Button>
                        {!v.isDefault && v.status === 'ACTIVE' && (
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            disabled={deactivateVariantMutation.isPending}
                            onClick={() => deactivateVariantMutation.mutate(v.id)}
                            className="cursor-pointer border-red-200 text-red-600 hover:bg-red-50"
                          >
                            Ngừng bán
                          </Button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>

                <form
                  onSubmit={variantForm.handleSubmit((vals) =>
                    saveVariantMutation.mutate(vals),
                  )}
                  className="grid gap-3 rounded-lg border border-border bg-card p-4 sm:grid-cols-12"
                  noValidate
                >
                  <div className="sm:col-span-12">
                    <p className="text-sm font-semibold text-foreground">
                      {editingVariant
                        ? `Chỉnh sửa biến thể #${editingVariant.sku}`
                        : 'Thêm biến thể mới'}
                    </p>
                  </div>
                  <div className="space-y-1 sm:col-span-3">
                    <Label htmlFor="vSku">SKU *</Label>
                    <Input id="vSku" {...variantForm.register('sku')} />
                  </div>
                  <div className="space-y-1 sm:col-span-3">
                    <Label htmlFor="vName">Tên phân loại</Label>
                    <Input id="vName" {...variantForm.register('variantName')} />
                  </div>
                  <div className="space-y-1 sm:col-span-2">
                    <Label htmlFor="vPrice">Giá (₫) *</Label>
                    <Input id="vPrice" type="number" min={0} {...variantForm.register('price')} />
                  </div>
                  <div className="space-y-1 sm:col-span-2">
                    <Label htmlFor="vStock">Tồn kho *</Label>
                    <Input
                      id="vStock"
                      type="number"
                      min={0}
                      {...variantForm.register('stockQuantity')}
                    />
                  </div>
                  <div className="space-y-1 sm:col-span-2">
                    <Label htmlFor="vStatus">Trạng thái</Label>
                    <select
                      id="vStatus"
                      className="h-8 w-full rounded-lg border border-input bg-card px-2 text-sm"
                      {...variantForm.register('status')}
                    >
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="INACTIVE">INACTIVE</option>
                    </select>
                  </div>
                  <div className="flex flex-wrap items-center justify-between gap-2 sm:col-span-12">
                    <label className="flex cursor-pointer items-center gap-2 text-xs font-medium">
                      <input type="checkbox" {...variantForm.register('isDefault')} />
                      Đặt làm biến thể mặc định
                    </label>
                    <div className="flex gap-2">
                      {editingVariant && (
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setEditingVariant(null)
                            variantForm.reset()
                          }}
                        >
                          Hủy sửa
                        </Button>
                      )}
                      <Button
                        type="submit"
                        size="sm"
                        disabled={saveVariantMutation.isPending}
                        className="cursor-pointer"
                      >
                        {saveVariantMutation.isPending && (
                          <Loader2 className="size-3.5 animate-spin" aria-hidden="true" />
                        )}
                        {editingVariant ? 'Lưu biến thể' : 'Thêm biến thể'}
                      </Button>
                    </div>
                  </div>
                </form>
              </CardContent>
            </Card>

            {/* Images Manager */}
            <Card className="border-border/80 shadow-sm">
              <CardHeader className="border-b border-border/60">
                <CardTitle className="text-lg">Hình ảnh sản phẩm</CardTitle>
                <CardDescription>
                  Quản lý URL hình ảnh và chọn ảnh đại diện chính của sản phẩm.
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-5 pt-5">
                {(product.images || []).length > 0 && (
                  <div className="grid gap-3 sm:grid-cols-3 md:grid-cols-4">
                    {product.images.map((img) => (
                      <div
                        key={img.id}
                        className="relative overflow-hidden rounded-lg border border-border bg-muted/20"
                      >
                        <div className="aspect-square w-full">
                          <img
                            src={img.imageUrl}
                            alt={img.altText || product.name}
                            className="h-full w-full object-cover"
                          />
                        </div>
                        <div className="flex items-center justify-between gap-1 p-2">
                          {img.isPrimary ? (
                            <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-primary">
                              <Star className="size-3 fill-primary" aria-hidden="true" />
                              Ảnh chính
                            </span>
                          ) : (
                            <span className="text-[11px] text-muted-foreground">
                              Thứ tự: {img.displayOrder}
                            </span>
                          )}
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            disabled={deleteImageMutation.isPending}
                            onClick={() => deleteImageMutation.mutate(img.id)}
                            aria-label="Xóa ảnh"
                            className="h-7 cursor-pointer px-2 text-red-600 hover:bg-red-50"
                          >
                            <Trash2 className="size-3.5" aria-hidden="true" />
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                <form
                  onSubmit={imageForm.handleSubmit((vals) => addImageMutation.mutate(vals))}
                  className="grid gap-3 rounded-lg border border-border bg-card p-4 sm:grid-cols-12"
                  noValidate
                >
                  <div className="space-y-1 sm:col-span-6">
                    <Label htmlFor="imgUrl">URL hình ảnh *</Label>
                    <Input
                      id="imgUrl"
                      placeholder="https://example.com/photo.jpg"
                      {...imageForm.register('imageUrl')}
                    />
                    {imageForm.formState.errors.imageUrl && (
                      <p className="text-xs text-destructive">
                        {imageForm.formState.errors.imageUrl.message}
                      </p>
                    )}
                  </div>
                  <div className="space-y-1 sm:col-span-4">
                    <Label htmlFor="imgAlt">Mô tả ảnh (Alt)</Label>
                    <Input id="imgAlt" {...imageForm.register('altText')} />
                  </div>
                  <div className="space-y-1 sm:col-span-2">
                    <Label htmlFor="imgOrder">Thứ tự</Label>
                    <Input
                      id="imgOrder"
                      type="number"
                      min={0}
                      {...imageForm.register('displayOrder')}
                    />
                  </div>
                  <div className="flex items-center justify-between sm:col-span-12">
                    <label className="flex cursor-pointer items-center gap-2 text-xs font-medium">
                      <input type="checkbox" {...imageForm.register('isPrimary')} />
                      Đặt làm ảnh chính
                    </label>
                    <Button
                      type="submit"
                      size="sm"
                      disabled={addImageMutation.isPending}
                      className="cursor-pointer"
                    >
                      {addImageMutation.isPending ? (
                        <Loader2 className="size-3.5 animate-spin" aria-hidden="true" />
                      ) : (
                        <ImagePlus className="size-3.5" aria-hidden="true" />
                      )}
                      Thêm ảnh
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          </div>
        ) : null}
      </div>
    </AppShell>
  )
}
