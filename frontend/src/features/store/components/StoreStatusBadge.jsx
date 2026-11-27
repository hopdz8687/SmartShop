const STATUS_CONFIG = {
  PENDING: {
    label: 'Chờ duyệt',
    className: 'bg-amber-50 text-amber-700 border-amber-200',
  },
  APPROVED: {
    label: 'Đã duyệt',
    className: 'bg-green-50 text-green-700 border-green-200',
  },
  REJECTED: {
    label: 'Từ chối',
    className: 'bg-red-50 text-red-700 border-red-200',
  },
  CANCELLED: {
    label: 'Đã hủy',
    className: 'bg-slate-100 text-slate-600 border-slate-200',
  },
  ACTIVE: {
    label: 'Đang hoạt động',
    className: 'bg-green-50 text-green-700 border-green-200',
  },
  INACTIVE: {
    label: 'Ngừng hoạt động',
    className: 'bg-slate-100 text-slate-600 border-slate-200',
  },
  SUSPENDED: {
    label: 'Tạm khóa',
    className: 'bg-red-50 text-red-700 border-red-200',
  },
}

export function StoreStatusBadge({ status }) {
  const config = STATUS_CONFIG[status] || {
    label: status || 'Không rõ',
    className: 'bg-secondary text-secondary-foreground border-border',
  }

  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold ${config.className}`}
    >
      {config.label}
    </span>
  )
}

export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}
