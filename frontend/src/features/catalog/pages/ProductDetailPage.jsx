import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  Loader2,
  Package,
  Store,
  Tag,
} from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { catalogApi } from '@/features/catalog/api/catalogApi'
import { formatCurrency } from '@/features/catalog/utils/catalogFormatters'

export function ProductDetailPage() {
  const { id } = useParams()
  const [selectedVariantId, setSelectedVariantId] = useState(null)
  const [selectedImageUrl, setSelectedImageUrl] = useState(null)

  const productQuery = useQuery({
    queryKey: ['public', 'products', 'detail', id],
    queryFn: async () => {
      const res = await catalogApi.getPublicProductDetail(id)
      return res.data
    },
    enabled: Boolean(id),
  })

  const product = productQuery.data

  const variants = product?.variants || []
  const selectedVariant =
    variants.find((v) => v.id === selectedVariantId) ||
    variants.find((v) => v.isDefault) ||
    variants[0] ||
    null

  const images = product?.images || []
  const activeImageUrl =
    selectedImageUrl ||
    images.find((img) => img.isPrimary)?.imageUrl ||
    images[0]?.imageUrl ||
    null

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <Button asChild variant="ghost" size="sm" className="-ml-2 text-muted-foreground">
            <Link to="/products">
              <ArrowLeft className="size-4" aria-hidden="true" />
              Quay lại danh sách sản phẩm
            </Link>
          </Button>
        </div>

        {productQuery.isLoading ? (
          <div className="flex items-center justify-center gap-2 py-16 text-sm text-muted-foreground">
            <Loader2 className="size-5 animate-spin text-primary" aria-hidden="true" />
            Đang tải chi tiết sản phẩm...
          </div>
        ) : productQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không tìm thấy sản phẩm</AlertTitle>
            <AlertDescription>
              {productQuery.error?.message || 'Sản phẩm không tồn tại hoặc đã ngừng bán.'}
            </AlertDescription>
          </Alert>
        ) : product ? (
          <div className="grid gap-6 lg:grid-cols-12">
            {/* Gallery */}
            <div className="space-y-3 lg:col-span-5">
              <Card className="overflow-hidden border-border/80 shadow-sm">
                <div className="aspect-square w-full bg-muted/40">
                  {activeImageUrl ? (
                    <img
                      src={activeImageUrl}
                      alt={product.name}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <div className="flex h-full w-full items-center justify-center text-muted-foreground/50">
                      <Package className="size-16" aria-hidden="true" />
                    </div>
                  )}
                </div>
              </Card>

              {images.length > 1 && (
                <div className="flex flex-wrap gap-2">
                  {images.map((img) => {
                    const active = activeImageUrl === img.imageUrl
                    return (
                      <button
                        key={img.id}
                        type="button"
                        onClick={() => setSelectedImageUrl(img.imageUrl)}
                        aria-label={img.altText || 'Xem ảnh sản phẩm'}
                        className={`size-16 cursor-pointer overflow-hidden rounded-lg border-2 transition-all ${
                          active
                            ? 'border-primary ring-2 ring-primary/30'
                            : 'border-border hover:border-primary/50'
                        }`}
                      >
                        <img
                          src={img.imageUrl}
                          alt={img.altText || product.name}
                          className="h-full w-full object-cover"
                        />
                      </button>
                    )
                  })}
                </div>
              )}
            </div>

            {/* Info & Variants */}
            <div className="space-y-5 lg:col-span-7">
              <Card className="border-border/80 shadow-sm">
                <CardContent className="space-y-5 pt-6">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="inline-flex items-center gap-1 rounded-md bg-secondary px-2.5 py-1 text-xs font-semibold text-secondary-foreground">
                      <Tag className="size-3" aria-hidden="true" />
                      {product.categoryName}
                    </span>
                    {product.brand && (
                      <span className="rounded-md border border-border bg-muted/40 px-2.5 py-1 text-xs font-medium text-muted-foreground">
                        Thương hiệu: {product.brand}
                      </span>
                    )}
                    <span className="inline-flex items-center gap-1 rounded-md border border-blue-200 bg-blue-50 px-2.5 py-1 text-xs font-medium text-primary">
                      <Store className="size-3" aria-hidden="true" />
                      {product.storeName}
                    </span>
                  </div>

                  <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                      {product.name}
                    </h1>
                    {selectedVariant && (
                      <p className="mt-3 text-3xl font-bold text-primary">
                        {formatCurrency(selectedVariant.price)}
                      </p>
                    )}
                  </div>

                  {/* Variant Picker */}
                  {(product.variants || []).length > 0 && (
                    <div className="space-y-2.5 border-t border-border/60 pt-4">
                      <p className="text-sm font-semibold text-foreground">
                        Phiên bản / Phân loại
                      </p>
                      <div className="flex flex-wrap gap-2">
                        {product.variants.map((variant) => {
                          const isSelected = selectedVariant?.id === variant.id
                          return (
                            <button
                              key={variant.id}
                              type="button"
                              onClick={() => setSelectedVariantId(variant.id)}
                              className={`cursor-pointer rounded-lg border px-3.5 py-2 text-left text-sm transition-all ${
                                isSelected
                                  ? 'border-primary bg-secondary font-semibold text-primary ring-2 ring-primary/20'
                                  : 'border-border bg-card text-foreground hover:border-primary/50'
                              }`}
                            >
                              <div>{variant.variantName || variant.sku}</div>
                              <div className="text-xs text-muted-foreground">
                                {formatCurrency(variant.price)}
                              </div>
                            </button>
                          )
                        })}
                      </div>

                      {selectedVariant && (
                        <div className="flex flex-wrap items-center gap-4 pt-1 text-xs text-muted-foreground">
                          <span>
                            SKU:{' '}
                            <strong className="font-mono text-foreground">
                              {selectedVariant.sku}
                            </strong>
                          </span>
                          <span className="inline-flex items-center gap-1">
                            <CheckCircle2
                              className={`size-3.5 ${
                                selectedVariant.stockQuantity > 0
                                  ? 'text-green-600'
                                  : 'text-red-600'
                              }`}
                              aria-hidden="true"
                            />
                            {selectedVariant.stockQuantity > 0
                              ? `Còn ${selectedVariant.stockQuantity} sản phẩm`
                              : 'Tạm hết hàng'}
                          </span>
                        </div>
                      )}
                    </div>
                  )}

                  {/* Description */}
                  <div className="space-y-2 border-t border-border/60 pt-4">
                    <h2 className="text-sm font-semibold text-foreground">
                      Mô tả sản phẩm
                    </h2>
                    <p className="whitespace-pre-line text-sm leading-relaxed text-muted-foreground">
                      {product.description || 'Chưa có mô tả chi tiết cho sản phẩm này.'}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        ) : null}
      </div>
    </AppShell>
  )
}
