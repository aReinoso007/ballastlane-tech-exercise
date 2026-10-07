import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../../shared/api/http'
import { Alert } from '../../shared/ui/Alert'
import { Button } from '../../shared/ui/Button'
import { TextField } from '../../shared/ui/TextField'
import { useAuth } from './useAuth'

export function LoginPage() {
  const { signIn, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/'
  const justRegistered = (location.state as { registered?: boolean } | null)?.registered === true

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) return <Navigate to={redirectTo} replace />

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await signIn(username, password)
      navigate(redirectTo, { replace: true })
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Something went wrong. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-md rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
      <h1 className="mb-1 text-2xl font-bold">Sign in</h1>
      <p className="mb-4 text-sm text-slate-600">
        Demo account: <code className="rounded bg-slate-100 px-1">demo</code> /{' '}
        <code className="rounded bg-slate-100 px-1">Demo1234!</code>
      </p>
      {justRegistered && <Alert tone="success">Account created. You can sign in now.</Alert>}
      {error && <Alert tone="error">{error}</Alert>}
      <form onSubmit={onSubmit} className="mt-4 space-y-4" noValidate>
        <TextField label="Username" name="username" value={username} onChange={setUsername}
          autoComplete="username" required />
        <TextField label="Password" name="password" type="password" value={password} onChange={setPassword}
          autoComplete="current-password" required />
        <Button type="submit" disabled={submitting || !username || !password} className="w-full">
          {submitting ? 'Signing in…' : 'Sign in'}
        </Button>
      </form>
      <p className="mt-4 text-sm text-slate-600">
        No account?{' '}
        <Link to="/register" className="font-medium text-brand hover:underline">
          Create one
        </Link>
      </p>
    </div>
  )
}
