const vndFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})

export function formatCurrency(amount) {
  if (amount === null || amount === undefined || Number.isNaN(Number(amount))) {
    return 'Liên hệ'
  }
  return vndFormatter.format(Number(amount))
}

export function formatPriceRange(minPrice, maxPrice) {
  if (minPrice === null || minPrice === undefined) return 'Liên hệ'
  if (maxPrice === null || maxPrice === undefined || Number(minPrice) === Number(maxPrice)) {
    return formatCurrency(minPrice)
  }
  return `${formatCurrency(minPrice)} – ${formatCurrency(maxPrice)}`
}

const PRODUCT_STATUS_CONFIG = {
  DRAFT: { label: 'Nháp', className: 'bg-slate-100 text-slate-700 border-slate-200' },
  PENDING: { label: 'Chờ duyệt', className: 'bg-amber-50 text-amber-700 border-amber-200' },
  ACTIVE: { label: 'Đang bán', className: 'bg-green-50 text-green-700 border-green-200' },
  REJECTED: { label: 'Từ chối', className: 'bg-red-50 text-red-700 border-red-200' },
  INACTIVE: { label: 'Ngừng bán', className: 'bg-slate-100 text-slate-600 border-slate-200' },
}

export function getProductStatusMeta(status) {
  return (
    PRODUCT_STATUS_CONFIG[status] || {
      label: status || 'Không rõ',
      className: 'bg-secondary text-secondary-foreground border-border',
    }
  )
}
