import { z } from 'zod'

export const categorySchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập tên danh mục')
    .max(150, 'Tên danh mục tối đa 150 ký tự'),
  parentId: z.string().optional().or(z.literal('')),
  description: z
    .string()
    .max(1000, 'Mô tả tối đa 1000 ký tự')
    .optional()
    .or(z.literal('')),
  status: z.enum(['ACTIVE', 'INACTIVE']).default('ACTIVE'),
})

export const variantItemSchema = z.object({
  sku: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập SKU')
    .max(100, 'SKU tối đa 100 ký tự'),
  variantName: z
    .string()
    .max(255, 'Tên biến thể tối đa 255 ký tự')
    .optional()
    .or(z.literal('')),
  price: z.coerce
    .number({ invalid_type_error: 'Giá phải là số' })
    .min(0, 'Giá phải >= 0'),
  stockQuantity: z.coerce
    .number({ invalid_type_error: 'Tồn kho phải là số nguyên' })
    .int('Tồn kho phải là số nguyên')
    .min(0, 'Tồn kho phải >= 0'),
  isDefault: z.boolean().default(false),
  status: z.enum(['ACTIVE', 'INACTIVE']).default('ACTIVE'),
})

export const createProductSchema = z.object({
  categoryId: z.string().min(1, 'Vui lòng chọn danh mục'),
  name: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập tên sản phẩm')
    .max(255, 'Tên sản phẩm tối đa 255 ký tự'),
  brand: z
    .string()
    .max(150, 'Thương hiệu tối đa 150 ký tự')
    .optional()
    .or(z.literal('')),
  description: z.string().optional().or(z.literal('')),
  status: z.enum(['DRAFT', 'PENDING', 'ACTIVE', 'INACTIVE']).default('ACTIVE'),
  variants: z
    .array(variantItemSchema)
    .min(1, 'Sản phẩm phải có ít nhất 1 biến thể'),
  primaryImageUrl: z
    .string()
    .max(1000, 'URL ảnh tối đa 1000 ký tự')
    .optional()
    .or(z.literal('')),
})

export const updateProductSchema = z.object({
  categoryId: z.string().min(1, 'Vui lòng chọn danh mục'),
  name: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập tên sản phẩm')
    .max(255, 'Tên sản phẩm tối đa 255 ký tự'),
  brand: z
    .string()
    .max(150, 'Thương hiệu tối đa 150 ký tự')
    .optional()
    .or(z.literal('')),
  description: z.string().optional().or(z.literal('')),
  status: z.enum(['DRAFT', 'PENDING', 'ACTIVE', 'INACTIVE']),
})

export const productImageSchema = z.object({
  imageUrl: z
    .string()
    .trim()
    .min(1, 'Vui lòng nhập URL hình ảnh')
    .max(1000, 'URL ảnh tối đa 1000 ký tự'),
  altText: z
    .string()
    .max(255, 'Alt text tối đa 255 ký tự')
    .optional()
    .or(z.literal('')),
  displayOrder: z.coerce
    .number({ invalid_type_error: 'Thứ tự phải là số' })
    .int()
    .min(0, 'Thứ tự phải >= 0')
    .default(0),
  isPrimary: z.boolean().default(false),
})
