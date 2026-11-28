import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { AnimatePresence, motion } from 'framer-motion'
import {
  ClipboardCheck,
  FolderTree,
  Loader2,
  LogOut,
  Package,
  ReceiptText,
  ShoppingBag,
  ShoppingCart,
  Store,
  User as UserIcon,
  UserCheck,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useCartQuery } from '@/features/cart/hooks/useCart'
import { useAuth } from '@/hooks/useAuth'

function navLinkClass({ isActive }) {
  return isActive
    ? 'inline-flex items-center gap-1.5 rounded-lg bg-secondary px-3 py-1.5 text-sm font-semibold text-primary transition-colors'
    : 'inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground'
}

export function AppShell({ children }) {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const cartQuery = useCartQuery()
  const [loggingOut, setLoggingOut] = useState(false)

  const totalCartItems = cartQuery.data?.totalItems ?? 0
  const roles = user?.roles || []
  const isCustomer = roles.includes('CUSTOMER')
  const isSeller = roles.includes('SELLER')
  const isAdmin = roles.includes('ADMIN')

  const handleLogout = async () => {
    setLoggingOut(true)
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="sticky top-0 z-20 border-b border-border bg-card/95 backdrop-blur">
        <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-3 px-4 py-3 sm:px-6">
          <div className="flex items-center gap-2.5">
            <span className="flex size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-xs">
              <ShoppingBag className="size-4" aria-hidden="true" />
            </span>
            <span className="text-lg font-bold tracking-tight text-foreground">
              SmartShop
            </span>
          </div>

          <nav
            aria-label="Điều hướng chính"
            className="order-3 flex w-full flex-wrap items-center gap-1 border-t border-border/60 pt-2 sm:order-2 sm:w-auto sm:border-t-0 sm:pt-0"
          >
            <NavLink to="/products" className={navLinkClass}>
              <ShoppingBag className="size-4" aria-hidden="true" />
              Sản phẩm
            </NavLink>

            <NavLink to="/account" className={navLinkClass}>
              <UserIcon className="size-4" aria-hidden="true" />
              Tài khoản
            </NavLink>

            {isCustomer && (
              <>
                <NavLink to="/orders" className={navLinkClass}>
                  <ReceiptText className="size-4" aria-hidden="true" />
                  Đơn mua
                </NavLink>
                <NavLink to="/seller-application" className={navLinkClass}>
                  <UserCheck className="size-4" aria-hidden="true" />
                  Đăng ký bán hàng
                </NavLink>
              </>
            )}

            {isSeller && (
              <>
                <NavLink to="/seller/products" className={navLinkClass}>
                  <Package className="size-4" aria-hidden="true" />
                  QL Sản phẩm
                </NavLink>
                <NavLink to="/seller/store" className={navLinkClass}>
                  <Store className="size-4" aria-hidden="true" />
                  Cửa hàng
                </NavLink>
              </>
            )}

            {isAdmin && (
              <>
                <NavLink to="/admin/categories" className={navLinkClass}>
                  <FolderTree className="size-4" aria-hidden="true" />
                  Danh mục
                </NavLink>
                <NavLink to="/admin/seller-applications" className={navLinkClass}>
                  <ClipboardCheck className="size-4" aria-hidden="true" />
                  Duyệt Seller
                </NavLink>
              </>
            )}
          </nav>

          <div className="order-2 flex items-center gap-2 sm:order-3">
            <NavLink
              to="/cart"
              aria-label={`Giỏ hàng (${totalCartItems} sản phẩm)`}
              className={({ isActive }) =>
                `inline-flex h-8 items-center gap-2 rounded-lg border px-3 text-sm font-medium transition-colors ${
                  isActive
                    ? 'border-primary bg-secondary text-primary font-semibold'
                    : 'border-border bg-card text-foreground hover:bg-muted'
                }`
              }
            >
              <ShoppingCart className="size-4 text-primary" aria-hidden="true" />
              <span className="hidden md:inline">Giỏ hàng</span>
              <span className="inline-flex min-w-[1.35rem] items-center justify-center rounded-full bg-primary px-1.5 py-0.5 text-[11px] font-bold leading-none text-primary-foreground tabular-nums">
                <AnimatePresence mode="popLayout" initial={false}>
                  <motion.span
                    key={totalCartItems}
                    initial={{ scale: 0.75, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    exit={{ scale: 0.75, opacity: 0 }}
                    transition={{ duration: 0.18 }}
                  >
                    {totalCartItems}
                  </motion.span>
                </AnimatePresence>
              </span>
            </NavLink>

            <Button
              type="button"
              variant="outline"
              onClick={handleLogout}
              disabled={loggingOut}
              className="cursor-pointer"
            >
              {loggingOut ? (
                <Loader2 className="size-4 animate-spin" aria-hidden="true" />
              ) : (
                <LogOut className="size-4" aria-hidden="true" />
              )}
              <span className="hidden sm:inline">Đăng xuất</span>
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  )
}
