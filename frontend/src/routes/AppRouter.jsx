import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AccountPage } from '@/features/auth/pages/AccountPage'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { RegisterPage } from '@/features/auth/pages/RegisterPage'
import { AdminCategoriesPage } from '@/features/catalog/pages/AdminCategoriesPage'
import { ProductDetailPage } from '@/features/catalog/pages/ProductDetailPage'
import { ProductsPage } from '@/features/catalog/pages/ProductsPage'
import { SellerProductFormPage } from '@/features/catalog/pages/SellerProductFormPage'
import { SellerProductsPage } from '@/features/catalog/pages/SellerProductsPage'
import { AdminSellerApplicationsPage } from '@/features/store/pages/AdminSellerApplicationsPage'
import { SellerApplicationPage } from '@/features/store/pages/SellerApplicationPage'
import { SellerStorePage } from '@/features/store/pages/SellerStorePage'
import { ProtectedRoute } from '@/routes/ProtectedRoute'
import { PublicOnlyRoute } from '@/routes/PublicOnlyRoute'

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicOnlyRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route path="/account" element={<AccountPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['CUSTOMER']} />}>
          <Route path="/seller-application" element={<SellerApplicationPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['SELLER']} />}>
          <Route path="/seller/store" element={<SellerStorePage />} />
          <Route path="/seller/products" element={<SellerProductsPage />} />
          <Route path="/seller/products/new" element={<SellerProductFormPage />} />
          <Route path="/seller/products/:id" element={<SellerProductFormPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
          <Route path="/admin/categories" element={<AdminCategoriesPage />} />
          <Route
            path="/admin/seller-applications"
            element={<AdminSellerApplicationsPage />}
          />
        </Route>

        <Route path="/" element={<Navigate to="/products" replace />} />
        <Route path="*" element={<Navigate to="/products" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
