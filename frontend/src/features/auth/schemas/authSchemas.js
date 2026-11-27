import { z } from 'zod'

export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập email')
    .email('Email không đúng định dạng'),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu'),
})

export const registerSchema = z
  .object({
    fullName: z
      .string()
      .trim()
      .min(1, 'Vui lòng nhập họ và tên')
      .max(150, 'Họ và tên không vượt quá 150 ký tự'),
    email: z
      .string()
      .trim()
      .min(1, 'Vui lòng nhập email')
      .email('Email không đúng định dạng')
      .max(255, 'Email không vượt quá 255 ký tự'),
    phone: z
      .string()
      .trim()
      .max(20, 'Số điện thoại không vượt quá 20 ký tự')
      .optional()
      .or(z.literal('')),
    password: z
      .string()
      .min(8, 'Mật khẩu phải có ít nhất 8 ký tự')
      .max(72, 'Mật khẩu không vượt quá 72 ký tự'),
    confirmPassword: z.string().min(1, 'Vui lòng xác nhận mật khẩu'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Mật khẩu xác nhận không khớp',
    path: ['confirmPassword'],
  })
