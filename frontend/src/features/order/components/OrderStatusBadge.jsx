const ORDER_STATUS_MAP = {
  PENDING: {
    label: 'Chờ xác nhận',
    className: 'border-amber-200 bg-amber-50 text-amber-700',
  },
  CONFIRMED: {
    label: 'Đã xác nhận',
    className: 'border-blue-200 bg-blue-50 text-blue-700',
  },
  PROCESSING: {
    label: 'Đang xử lý',
    className: 'border-indigo-200 bg-indigo-50 text-indigo-700',
  },
  PARTIALLY_SHIPPED: {
    label: 'Giao một phần',
    className: 'border-sky-200 bg-sky-50 text-sky-700',
  },
  SHIPPED: {
    label: 'Đang giao hàng',
    className: 'border-purple-200 bg-purple-50 text-purple-700',
  },
  DELIVERED: {
    label: 'Đã giao hàng',
    className: 'border-green-200 bg-green-50 text-green-700',
  },
  CANCELLED: {
    label: 'Đã hủy',
    className: 'border-red-200 bg-red-50 text-red-700',
  },
}

const PAYMENT_STATUS_MAP = {
  PENDING: {
    label: 'Chưa thanh toán',
    className: 'border-amber-200 bg-amber-50 text-amber-700',
  },
  PAID: {
    label: 'Đã thanh toán',
    className: 'border-green-200 bg-green-50 text-green-700',
  },
  FAILED: {
    label: 'Thanh toán lỗi',
    className: 'border-red-200 bg-red-50 text-red-700',
  },
  CANCELLED: {
    label: 'Đã hủy',
    className: 'border-slate-200 bg-slate-100 text-slate-600',
  },
  REFUNDED: {
    label: 'Đã hoàn tiền',
    className: 'border-sky-200 bg-sky-50 text-sky-700',
  },
}

export function OrderStatusBadge({ status }) {
  const meta = ORDER_STATUS_MAP[status] || {
    label: status || 'Không rõ',
    className: 'border-border bg-secondary text-secondary-foreground',
  }

  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold ${meta.className}`}
    >
      {meta.label}
    </span>
  )
}

export function PaymentStatusBadge({ status }) {
  const meta = PAYMENT_STATUS_MAP[status] || {
    label: status || 'Không rõ',
    className: 'border-border bg-secondary text-secondary-foreground',
  }

  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold ${meta.className}`}
    >
      COD • {meta.label}
    </span>
  )
}
