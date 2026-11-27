import { Link } from 'react-router-dom'
import {
  CheckCircle2,
  ClipboardCheck,
  Mail,
  Phone,
  Shield,
  Store,
  User as UserIcon,
  UserCheck,
} from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { useAuth } from '@/hooks/useAuth'

const ROLE_LABELS = {
  CUSTOMER: 'Customer',
  SELLER: 'Seller',
  ADMIN: 'Admin',
}

const STATUS_BADGE_CLASSES = {
  ACTIVE: 'bg-green-50 text-green-700 border-green-200',
  PENDING: 'bg-amber-50 text-amber-700 border-amber-200',
  INACTIVE: 'bg-red-50 text-red-700 border-red-200',
  LOCKED: 'bg-red-50 text-red-700 border-red-200',
}

export function AccountPage() {
  const { user } = useAuth()
  const roles = user?.roles || []
  const isCustomer = roles.includes('CUSTOMER')
  const isSeller = roles.includes('SELLER')
  const isAdmin = roles.includes('ADMIN')

  const statusBadgeClass =
    STATUS_BADGE_CLASSES[user?.status] || 'bg-secondary text-secondary-foreground border-border'

  return (
    <AppShell>
      <div className="space-y-6">
        <Card className="shadow-sm border-border/80">
          <CardHeader className="border-b border-border/60 pb-5">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-3">
                <span className="flex size-12 items-center justify-center rounded-full bg-secondary text-primary border border-blue-200">
                  <UserIcon className="size-6" aria-hidden="true" />
                </span>
                <div>
                  <CardTitle className="text-xl">{user?.fullName}</CardTitle>
                  <CardDescription className="mt-0.5">
                    Thông tin tài khoản hiện tại từ hệ thống
                  </CardDescription>
                </div>
              </div>

              <span
                className={`inline-flex w-fit items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold border ${statusBadgeClass}`}
              >
                <CheckCircle2 className="size-3.5" aria-hidden="true" />
                {user?.status}
              </span>
            </div>
          </CardHeader>

          <CardContent className="grid gap-5 pt-6 sm:grid-cols-2">
            <div className="flex items-start gap-3 rounded-lg border border-border/70 bg-muted/40 p-4">
              <Mail className="mt-0.5 size-4 text-primary" aria-hidden="true" />
              <div className="min-w-0">
                <p className="text-xs font-medium text-muted-foreground">Email</p>
                <p className="mt-1 truncate text-sm font-semibold text-foreground">
                  {user?.email}
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3 rounded-lg border border-border/70 bg-muted/40 p-4">
              <Phone className="mt-0.5 size-4 text-primary" aria-hidden="true" />
              <div className="min-w-0">
                <p className="text-xs font-medium text-muted-foreground">
                  Số điện thoại
                </p>
                <p className="mt-1 text-sm font-semibold text-foreground">
                  {user?.phone || 'Chưa cập nhật'}
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3 rounded-lg border border-border/70 bg-muted/40 p-4 sm:col-span-2">
              <Shield className="mt-0.5 size-4 text-primary" aria-hidden="true" />
              <div>
                <p className="text-xs font-medium text-muted-foreground">
                  Vai trò (Roles)
                </p>
                <div className="mt-2 flex flex-wrap gap-2">
                  {roles.map((role) => (
                    <span
                      key={role}
                      className="inline-flex items-center rounded-md bg-secondary px-2.5 py-1 text-xs font-semibold text-secondary-foreground border border-blue-200/80"
                    >
                      {ROLE_LABELS[role] || role}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {(isCustomer || isSeller || isAdmin) && (
          <Card className="border-border/80 shadow-sm">
            <CardHeader className="pb-3">
              <CardTitle className="text-base">Truy cập nhanh Store & Seller</CardTitle>
              <CardDescription>
                Chức năng khả dụng theo vai trò tài khoản của bạn.
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-wrap gap-3">
              {isCustomer && (
                <Button asChild variant="outline">
                  <Link to="/seller-application">
                    <UserCheck className="size-4 text-primary" aria-hidden="true" />
                    Đăng ký bán hàng
                  </Link>
                </Button>
              )}
              {isSeller && (
                <Button asChild>
                  <Link to="/seller/store">
                    <Store className="size-4" aria-hidden="true" />
                    Quản lý cửa hàng
                  </Link>
                </Button>
              )}
              {isAdmin && (
                <Button asChild variant="outline">
                  <Link to="/admin/seller-applications">
                    <ClipboardCheck className="size-4 text-primary" aria-hidden="true" />
                    Duyệt hồ sơ Seller
                  </Link>
                </Button>
              )}
            </CardContent>
          </Card>
        )}
      </div>
    </AppShell>
  )
}
