import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { AlertCircle, CheckCircle2, Loader2, LogIn } from 'lucide-react'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PasswordInput } from '@/features/auth/components/PasswordInput'
import { authApi } from '@/features/auth/api/authApi'
import { loginSchema } from '@/features/auth/schemas/authSchemas'
import { useAuth } from '@/hooks/useAuth'

function getLoginErrorMessage(error) {
  if (error?.status === 401 || error?.errorCode === 'INVALID_CREDENTIALS') {
    return 'Email hoặc mật khẩu không chính xác.'
  }
  if (error?.status === 403 || error?.errorCode === 'ACCOUNT_NOT_ACTIVE') {
    return 'Tài khoản hiện không ở trạng thái hoạt động hoặc đã bị khóa.'
  }
  return error?.message || 'Đăng nhập thất bại. Vui lòng thử lại.'
}

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { setSession } = useAuth()
  const [serverError, setServerError] = useState('')

  const registeredEmail = location.state?.registeredEmail || ''
  const redirectTarget = location.state?.from?.pathname || '/account'

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: registeredEmail,
      password: '',
    },
  })

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (response) => {
      const { accessToken, user } = response.data
      setSession(accessToken, user)
      navigate(redirectTarget, { replace: true })
    },
    onError: (error) => {
      if (error?.fieldErrors) {
        for (const [field, message] of Object.entries(error.fieldErrors)) {
          if (field === 'email' || field === 'password') {
            setError(field, { type: 'server', message })
          }
        }
      }
      setServerError(getLoginErrorMessage(error))
    },
  })

  const onSubmit = (values) => {
    setServerError('')
    loginMutation.mutate({
      email: values.email.trim(),
      password: values.password,
    })
  }

  return (
    <AuthLayout
      title="Đăng nhập SmartShop"
      subtitle="Chào mừng bạn quay trở lại. Nhập thông tin tài khoản để tiếp tục."
    >
      <Card className="shadow-sm border-border/80">
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <CardContent className="space-y-4 pt-6">
            {registeredEmail ? (
              <Alert className="border-green-200 bg-green-50 text-green-900">
                <CheckCircle2 className="size-4 text-green-600" aria-hidden="true" />
                <AlertDescription>
                  Đăng ký thành công! Vui lòng đăng nhập bằng tài khoản vừa tạo.
                </AlertDescription>
              </Alert>
            ) : null}

            {serverError ? (
              <Alert variant="destructive">
                <AlertCircle className="size-4" aria-hidden="true" />
                <AlertDescription>{serverError}</AlertDescription>
              </Alert>
            ) : null}

            <div className="space-y-2">
              <Label htmlFor="login-email">Email</Label>
              <Input
                id="login-email"
                type="email"
                autoComplete="email"
                placeholder="name@example.com"
                aria-invalid={Boolean(errors.email)}
                {...register('email')}
              />
              {errors.email ? (
                <p className="text-xs font-medium text-destructive" role="alert">
                  {errors.email.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="login-password">Mật khẩu</Label>
              <PasswordInput
                id="login-password"
                autoComplete="current-password"
                placeholder="Nhập mật khẩu"
                aria-invalid={Boolean(errors.password)}
                {...register('password')}
              />
              {errors.password ? (
                <p className="text-xs font-medium text-destructive" role="alert">
                  {errors.password.message}
                </p>
              ) : null}
            </div>
          </CardContent>

          <CardFooter className="flex flex-col gap-4 pt-2">
            <Button
              type="submit"
              size="lg"
              className="w-full h-11 font-semibold cursor-pointer"
              disabled={loginMutation.isPending}
            >
              {loginMutation.isPending ? (
                <>
                  <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                  Đang đăng nhập...
                </>
              ) : (
                <>
                  <LogIn className="size-4" aria-hidden="true" />
                  Đăng nhập
                </>
              )}
            </Button>

            <p className="text-center text-sm text-muted-foreground">
              Chưa có tài khoản?{' '}
              <Link
                to="/register"
                className="font-semibold text-primary hover:underline underline-offset-4"
              >
                Đăng ký tài khoản Customer
              </Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </AuthLayout>
  )
}
