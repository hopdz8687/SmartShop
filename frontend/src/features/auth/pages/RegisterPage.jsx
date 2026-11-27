import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { AlertCircle, Loader2, UserPlus } from 'lucide-react'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PasswordInput } from '@/features/auth/components/PasswordInput'
import { authApi } from '@/features/auth/api/authApi'
import { registerSchema } from '@/features/auth/schemas/authSchemas'

const REGISTER_FIELDS = new Set(['fullName', 'email', 'phone', 'password'])

function getRegisterErrorMessage(error) {
  if (error?.status === 409 || error?.errorCode === 'EMAIL_ALREADY_EXISTS') {
    return 'Email này đã được đăng ký. Vui lòng sử dụng email khác hoặc đăng nhập.'
  }
  return error?.message || 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.'
}

export function RegisterPage() {
  const navigate = useNavigate()
  const [serverError, setServerError] = useState('')

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      fullName: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
    },
  })

  const registerMutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: (response) => {
      navigate('/login', {
        replace: true,
        state: { registeredEmail: response.data.email },
      })
    },
    onError: (error) => {
      if (error?.errorCode === 'EMAIL_ALREADY_EXISTS') {
        setError('email', {
          type: 'server',
          message: 'Email này đã tồn tại trong hệ thống',
        })
      }
      if (error?.fieldErrors) {
        for (const [field, message] of Object.entries(error.fieldErrors)) {
          if (REGISTER_FIELDS.has(field)) {
            setError(field, { type: 'server', message })
          }
        }
      }
      setServerError(getRegisterErrorMessage(error))
    },
  })

  const onSubmit = (values) => {
    setServerError('')
    const trimmedPhone = values.phone?.trim()
    registerMutation.mutate({
      fullName: values.fullName.trim(),
      email: values.email.trim(),
      phone: trimmedPhone ? trimmedPhone : undefined,
      password: values.password,
    })
  }

  return (
    <AuthLayout
      title="Tạo tài khoản SmartShop"
      subtitle="Đăng ký tài khoản khách hàng (Customer) để bắt đầu mua sắm."
    >
      <Card className="shadow-sm border-border/80">
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <CardContent className="space-y-4 pt-6">
            {serverError ? (
              <Alert variant="destructive">
                <AlertCircle className="size-4" aria-hidden="true" />
                <AlertDescription>{serverError}</AlertDescription>
              </Alert>
            ) : null}

            <div className="space-y-2">
              <Label htmlFor="register-fullName">Họ và tên</Label>
              <Input
                id="register-fullName"
                type="text"
                autoComplete="name"
                placeholder="Nguyễn Văn A"
                aria-invalid={Boolean(errors.fullName)}
                {...register('fullName')}
              />
              {errors.fullName ? (
                <p className="text-xs font-medium text-destructive" role="alert">
                  {errors.fullName.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="register-email">Email</Label>
              <Input
                id="register-email"
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
              <Label htmlFor="register-phone">
                Số điện thoại{' '}
                <span className="text-xs font-normal text-muted-foreground">
                  (tùy chọn)
                </span>
              </Label>
              <Input
                id="register-phone"
                type="tel"
                autoComplete="tel"
                placeholder="0901234567"
                aria-invalid={Boolean(errors.phone)}
                {...register('phone')}
              />
              {errors.phone ? (
                <p className="text-xs font-medium text-destructive" role="alert">
                  {errors.phone.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="register-password">Mật khẩu</Label>
              <PasswordInput
                id="register-password"
                autoComplete="new-password"
                placeholder="Tối thiểu 8 ký tự"
                aria-invalid={Boolean(errors.password)}
                {...register('password')}
              />
              {errors.password ? (
                <p className="text-xs font-medium text-destructive" role="alert">
                  {errors.password.message}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <Label htmlFor="register-confirmPassword">Xác nhận mật khẩu</Label>
              <PasswordInput
                id="register-confirmPassword"
                autoComplete="new-password"
                placeholder="Nhập lại mật khẩu"
                aria-invalid={Boolean(errors.confirmPassword)}
                {...register('confirmPassword')}
              />
              {errors.confirmPassword ? (
                <p className="text-xs font-medium text-destructive" role="alert">
                  {errors.confirmPassword.message}
                </p>
              ) : null}
            </div>
          </CardContent>

          <CardFooter className="flex flex-col gap-4 pt-2">
            <Button
              type="submit"
              size="lg"
              className="w-full h-11 font-semibold cursor-pointer"
              disabled={registerMutation.isPending}
            >
              {registerMutation.isPending ? (
                <>
                  <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                  Đang tạo tài khoản...
                </>
              ) : (
                <>
                  <UserPlus className="size-4" aria-hidden="true" />
                  Đăng ký
                </>
              )}
            </Button>

            <p className="text-center text-sm text-muted-foreground">
              Đã có tài khoản?{' '}
              <Link
                to="/login"
                className="font-semibold text-primary hover:underline underline-offset-4"
              >
                Đăng nhập
              </Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </AuthLayout>
  )
}
