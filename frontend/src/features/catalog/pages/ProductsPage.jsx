import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  Loader2,
  Package,
  Search,
  SlidersHorizontal,
  Store,
} from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { catalogApi } from '@/features/catalog/api/catalogApi'
import { formatPriceRange } from '@/features/catalog/utils/catalogFormatters'

const SORT_OPTIONS = [
  { value: 'newest', label: 'Mới nhất' },
  { value: 'price_asc', label: 'Giá thấp đến cao' },
  { value: 'price_desc', label: 'Giá cao đến thấp' },
]

export function ProductsPage() {
  const [keywordInput, setKeywordInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [categoryId, setCategoryId] = useState(null)
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [sort, setSort] = useState('newest')
  const [page, setPage] = useState(0)

  const categoriesQuery = useQuery({
    queryKey: ['public', 'categories'],
    queryFn: async () => {
      const res = await catalogApi.getPublicCategories()
      return res.data || []
    },
  })

  const productsQuery = useQuery({
    queryKey: ['public', 'products', { keyword, categoryId, minPrice, maxPrice, sort, page }],
    queryFn: async () => {
      const res = await catalogApi.searchPublicProducts({
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
        minPrice: minPrice !== '' ? minPrice : undefined,
        maxPrice: maxPrice !== '' ? maxPrice : undefined,
        sort,
        page,
        size: 12,
      })
      return res.data
    },
  })

  const categories = categoriesQuery.data || []
  const pageData = productsQuery.data
  const products = pageData?.content || []
  const totalPages = pageData?.totalPages ?? 0

  const handleSearchSubmit = (e) => {
    e.preventDefault()
    setKeyword(keywordInput.trim())
    setPage(0)
  }

  const handleSelectCategory = (id) => {
    setCategoryId(id)
    setPage(0)
  }

  const handleResetFilters = () => {
    setKeywordInput('')
    setKeyword('')
    setCategoryId(null)
    setMinPrice('')
    setMaxPrice('')
    setSort('newest')
    setPage(0)
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-foreground">
              Khám phá sản phẩm
            </h1>
            <p className="mt-1 text-sm text-muted-foreground">
              Tìm kiếm và lọc sản phẩm đang mở bán từ các gian hàng chính hãng trên SmartShop.
            </p>
          </div>
          {pageData && (
            <span className="text-xs font-medium text-muted-foreground">
              {pageData.totalElements} sản phẩm
            </span>
          )}
        </div>

        <Card className="border-border/80 shadow-sm">
          <CardContent className="space-y-4 pt-5">
            <form
              onSubmit={handleSearchSubmit}
              className="grid gap-3 md:grid-cols-12"
            >
              <div className="relative md:col-span-5">
                <Search
                  className="pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2 text-muted-foreground"
                  aria-hidden="true"
                />
                <Input
                  value={keywordInput}
                  onChange={(e) => setKeywordInput(e.target.value)}
                  placeholder="Tìm theo tên sản phẩm hoặc thương hiệu..."
                  aria-label="Từ khóa tìm kiếm"
                  className="pl-8"
                />
              </div>

              <div className="flex items-center gap-2 md:col-span-3">
                <Input
                  type="number"
                  min={0}
                  value={minPrice}
                  onChange={(e) => {
                    setMinPrice(e.target.value)
                    setPage(0)
                  }}
                  placeholder="Giá từ"
                  aria-label="Giá tối thiểu"
                />
                <span className="text-muted-foreground">–</span>
                <Input
                  type="number"
                  min={0}
                  value={maxPrice}
                  onChange={(e) => {
                    setMaxPrice(e.target.value)
                    setPage(0)
                  }}
                  placeholder="Đến"
                  aria-label="Giá tối đa"
                />
              </div>

              <div className="flex items-center gap-2 md:col-span-4">
                <SlidersHorizontal
                  className="size-4 shrink-0 text-muted-foreground"
                  aria-hidden="true"
                />
                <select
                  value={sort}
                  onChange={(e) => {
                    setSort(e.target.value)
                    setPage(0)
                  }}
                  aria-label="Sắp xếp sản phẩm"
                  className="h-8 flex-1 rounded-lg border border-input bg-card px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                >
                  {SORT_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
                <Button type="submit" size="sm" className="cursor-pointer">
                  Tìm
                </Button>
              </div>
            </form>

            <div className="flex flex-wrap items-center gap-2 border-t border-border/60 pt-3">
              <span className="text-xs font-medium text-muted-foreground">
                Danh mục:
              </span>
              <Button
                type="button"
                size="sm"
                variant={categoryId === null ? 'default' : 'outline'}
                onClick={() => handleSelectCategory(null)}
                className="h-7 cursor-pointer rounded-full px-3 text-xs"
              >
                Tất cả
              </Button>
              {categories.map((cat) => (
                <Button
                  key={cat.id}
                  type="button"
                  size="sm"
                  variant={categoryId === cat.id ? 'default' : 'outline'}
                  onClick={() => handleSelectCategory(cat.id)}
                  className="h-7 cursor-pointer rounded-full px-3 text-xs"
                >
                  {cat.name}
                </Button>
              ))}
              {(keyword || categoryId !== null || minPrice !== '' || maxPrice !== '' || sort !== 'newest') && (
                <Button
                  type="button"
                  size="sm"
                  variant="ghost"
                  onClick={handleResetFilters}
                  className="ml-auto h-7 cursor-pointer text-xs text-primary"
                >
                  Xóa bộ lọc
                </Button>
              )}
            </div>
          </CardContent>
        </Card>

        {productsQuery.isLoading ? (
          <div className="flex items-center justify-center gap-2 py-16 text-sm text-muted-foreground">
            <Loader2 className="size-5 animate-spin text-primary" aria-hidden="true" />
            Đang tải sản phẩm...
          </div>
        ) : productsQuery.isError ? (
          <Alert variant="destructive">
            <AlertCircle className="size-4" aria-hidden="true" />
            <AlertTitle>Không thể tải danh sách sản phẩm</AlertTitle>
            <AlertDescription>
              {productsQuery.error?.message || 'Vui lòng thử lại sau.'}
            </AlertDescription>
          </Alert>
        ) : products.length === 0 ? (
          <Card className="border-border/80 shadow-sm">
            <CardContent className="flex flex-col items-center justify-center gap-3 py-16 text-center">
              <span className="flex size-12 items-center justify-center rounded-full bg-secondary text-primary">
                <Package className="size-6" aria-hidden="true" />
              </span>
              <div>
                <p className="font-semibold text-foreground">
                  Không tìm thấy sản phẩm phù hợp
                </p>
                <p className="mt-1 text-sm text-muted-foreground">
                  Thử thay đổi từ khóa, khoảng giá hoặc chọn danh mục khác.
                </p>
              </div>
              <Button variant="outline" size="sm" onClick={handleResetFilters}>
                Xem tất cả sản phẩm
              </Button>
            </CardContent>
          </Card>
        ) : (
          <>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {products.map((product) => (
                <Link
                  key={product.id}
                  to={`/products/${product.id}`}
                  className="group block rounded-xl border border-border/80 bg-card shadow-xs transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-md focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-ring/50"
                >
                  <div className="aspect-[4/3] w-full overflow-hidden rounded-t-xl bg-muted/50">
                    {product.primaryImageUrl ? (
                      <img
                        src={product.primaryImageUrl}
                        alt={product.name}
                        loading="lazy"
                        className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                      />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center text-muted-foreground/50">
                        <Package className="size-10" aria-hidden="true" />
                      </div>
                    )}
                  </div>

                  <div className="space-y-2 p-4">
                    <div className="flex items-center justify-between gap-2">
                      <span className="truncate rounded-md bg-secondary px-2 py-0.5 text-[11px] font-medium text-secondary-foreground">
                        {product.categoryName}
                      </span>
                      {product.brand && (
                        <span className="truncate text-[11px] text-muted-foreground">
                          {product.brand}
                        </span>
                      )}
                    </div>

                    <h2 className="line-clamp-2 min-h-[2.5rem] text-sm font-semibold text-foreground group-hover:text-primary">
                      {product.name}
                    </h2>

                    <p className="text-base font-bold text-primary">
                      {formatPriceRange(product.minPrice, product.maxPrice)}
                    </p>

                    <div className="flex items-center gap-1.5 border-t border-border/50 pt-2 text-xs text-muted-foreground">
                      <Store className="size-3.5 shrink-0" aria-hidden="true" />
                      <span className="truncate">{product.storeName}</span>
                    </div>
                  </div>
                </Link>
              ))}
            </div>

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
          </>
        )}
      </div>
    </AppShell>
  )
}
