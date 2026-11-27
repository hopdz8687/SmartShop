import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import {
  ClipboardCheck,
  Loader2,
  LogOut,
  ShoppingBag,
  Store,
  User as UserIcon,
  UserCheck,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/hooks/useAuth'

function navLinkClass({ isActive }) {
  return isActive
    ? 'inline-flex items-center gap-1.5 rounded-lg bg-secondary px-3 py-1.5 text-sm font-semibold text-primary transition-colors'
    : 'inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground'
}

export function AppShell({ children }) {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [loggingOut, setLoggingOut] = useState(false)

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
            <NavLink to="/account" className={navLinkClass}>
              <UserIcon className="size-4" aria-hidden="true" />
              Tài khoản
            </NavLink>

            {isCustomer && (
              <NavLink to="/seller-application" className={navLinkClass}>
                <UserCheck className="size-4" aria-hidden="true" />
                Đăng ký bán hàng
              </NavLink>
            )}

            {isSeller && (
              <NavLink to="/seller/store" className={navLinkClass}>
                <Store className="size-4" aria-hidden="true" />
                Cửa hàng
              </NavLink>
            )}

            {isAdmin && (
              <NavLink to="/admin/seller-applications" className={navLinkClass}>
                <ClipboardCheck className="size-4" aria-hidden="true" />
                Duyệt Seller
              </NavLink>
            )}
          </nav>

          <Button
            type="button"
            variant="outline"
            onClick={handleLogout}
            disabled={loggingOut}
            className="order-2 cursor-pointer sm:order-3"
          >
            {loggingOut ? (
              <Loader2 className="size-4 animate-spin" aria-hidden="true" />
            ) : (
              <LogOut className="size-4" aria-hidden="true" />
            )}
            Đăng xuất
          </Button>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  )
}
