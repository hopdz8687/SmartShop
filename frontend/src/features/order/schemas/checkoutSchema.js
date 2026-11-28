import { z } from 'zod'

export const checkoutSchema = z.object({
  recipientName: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập họ tên người nhận')
    .max(150, 'Họ tên tối đa 150 ký tự'),
  recipientPhone: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập số điện thoại người nhận')
    .max(20, 'Số điện thoại tối đa 20 ký tự')
    .regex(/^[0-9+\-\s()]{8,20}$/, 'Số điện thoại không đúng định dạng'),
  shippingProvince: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập Tỉnh / Thành phố')
    .max(100, 'Tỉnh / Thành phố tối đa 100 ký tự'),
  shippingDistrict: z
    .string()
    .max(100, 'Quận / Huyện tối đa 100 ký tự')
    .optional()
    .or(z.literal('')),
  shippingWard: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập Phường / Xã')
    .max(100, 'Phường / Xã tối đa 100 ký tự'),
  shippingAddress: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập địa chỉ chi tiết')
    .max(500, 'Địa chỉ chi tiết tối đa 500 ký tự'),
  customerNote: z
    .string()
    .max(1000, 'Ghi chú tối đa 1000 ký tự')
    .optional()
    .or(z.literal('')),
})
