import { z } from 'zod'

export const createSellerApplicationSchema = z.object({
  businessName: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập tên cửa hàng dự kiến')
    .max(200, 'Tên cửa hàng không được vượt quá 200 ký tự'),
  applicantNote: z
    .string()
    .max(2000, 'Ghi chú không được vượt quá 2000 ký tự')
    .optional()
    .or(z.literal('')),
})

export const rejectSellerApplicationSchema = z.object({
  rejectionReason: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập lý do từ chối')
    .max(1000, 'Lý do từ chối không được vượt quá 1000 ký tự'),
})

export const updateStoreSchema = z.object({
  storeName: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập tên cửa hàng')
    .max(200, 'Tên cửa hàng không được vượt quá 200 ký tự'),
  description: z
    .string()
    .max(5000, 'Mô tả không được vượt quá 5000 ký tự')
    .optional()
    .or(z.literal('')),
  logoUrl: z
    .string()
    .max(1000, 'URL logo không được vượt quá 1000 ký tự')
    .optional()
    .or(z.literal('')),
  contactEmail: z
    .string()
    .trim()
    .max(255, 'Email liên hệ không được vượt quá 255 ký tự')
    .refine(
      (val) => val === '' || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val),
      'Email liên hệ không đúng định dạng',
    )
    .optional()
    .or(z.literal('')),
  contactPhone: z
    .string()
    .trim()
    .max(20, 'Số điện thoại liên hệ không được vượt quá 20 ký tự')
    .optional()
    .or(z.literal('')),
})
