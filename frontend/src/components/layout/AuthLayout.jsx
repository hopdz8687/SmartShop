import { ShieldCheck, ShoppingBag, Store, Truck } from 'lucide-react'

const CURRENT_YEAR = new Date().getFullYear()

const HIGHLIGHTS = [
  {
    icon: Store,
    title: 'Đa cửa hàng chính hãng',
    description: 'Mua sắm tiện lợi từ nhiều gian hàng uy tín trên cùng một giỏ hàng.',
  },
  {
    icon: Truck,
    title: 'Thanh toán COD linh hoạt',
    description: 'Theo dõi trạng thái từng đơn hàng riêng biệt và nhận hàng tận nơi.',
  },
  {
    icon: ShieldCheck,
    title: 'Bảo mật tài khoản',
    description: 'Xác thực JWT an toàn và kiểm soát quyền truy cập theo vai trò.',
  },
]

export function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col lg:grid lg:grid-cols-12">
      {/* Brand panel (visible on desktop, compact header on mobile/tablet) */}
      <aside className="bg-gradient-to-br from-slate-950 via-blue-950 to-slate-900 text-slate-50 px-6 py-8 sm:px-10 lg:col-span-5 lg:flex lg:flex-col lg:justify-between lg:px-12 lg:py-12">
        <div>
          <div className="inline-flex items-center gap-2.5">
            <span className="flex size-10 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-sm">
              <ShoppingBag className="size-5" aria-hidden="true" />
            </span>
            <span className="text-xl font-bold tracking-tight text-white">
              SmartShop
            </span>
          </div>

          <div className="mt-8 lg:mt-14 max-w-md">
            <h1 className="text-2xl font-bold tracking-tight text-white sm:text-3xl">
              Nền tảng mua sắm đa cửa hàng thông minh
            </h1>
            <p className="mt-3 text-sm leading-relaxed text-blue-100/85 sm:text-base">
              Trải nghiệm mua sắm hiện đại, minh bạch đơn hàng và kết nối trực tiếp với các nhà bán hàng uy tín.
            </p>
          </div>
        </div>

        <ul className="mt-8 hidden space-y-5 lg:block">
          {HIGHLIGHTS.map((item) => {
            const Icon = item.icon
            return (
              <li key={item.title} className="flex items-start gap-3.5">
                <span className="mt-0.5 flex size-9 shrink-0 items-center justify-center rounded-lg bg-blue-900/60 text-blue-300 border border-blue-800/80">
                  <Icon className="size-4" aria-hidden="true" />
                </span>
                <div>
                  <p className="text-sm font-semibold text-white">{item.title}</p>
                  <p className="mt-0.5 text-xs leading-relaxed text-slate-300">
                    {item.description}
                  </p>
                </div>
              </li>
            )
          })}
        </ul>

        <p className="mt-8 hidden text-xs text-slate-400 lg:block">
          © {CURRENT_YEAR} SmartShop MVP. All rights reserved.
        </p>
      </aside>

      {/* Form panel */}
      <main className="flex flex-1 items-center justify-center px-4 py-8 sm:px-6 lg:col-span-7 lg:px-12 lg:py-12">
        <div className="w-full max-w-md">
          <div className="mb-6">
            <h2 className="text-2xl font-bold tracking-tight text-foreground">
              {title}
            </h2>
            {subtitle ? (
              <p className="mt-1.5 text-sm text-muted-foreground">{subtitle}</p>
            ) : null}
          </div>
          {children}
        </div>
      </main>
    </div>
  )
}
