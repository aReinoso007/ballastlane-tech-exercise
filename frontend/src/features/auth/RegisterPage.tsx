import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../../shared/api/http'
import { Alert } from '../../shared/ui/Alert'
import { Button } from '../../shared/ui/Button'
import { TextField } from '../../shared/ui/TextField'
import { register } from './api'

export function RegisterPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setErrors([])
    setSubmitting(true)
    try {
      await register(username, email, password)
      navigate('/login', { replace: true, state: { registered: true } })
    } catch (e) {
      if (e instanceof ApiError) setErrors(e.errors.length > 0 ? e.errors : [e.message])
      else setErrors(['Something went wrong. Please try again.'])
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-md rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
      <h1 className="mb-4 text-2xl font-bold">Create account</h1>
      {errors.length > 0 && (
        <Alert tone="error">
          <ul className="list-disc pl-4">
            {errors.map((e) => (
              <li key={e}>{e}</li>
            ))}
          </ul>
        </Alert>
      )}
      <form onSubmit={onSubmit} className="mt-4 space-y-4" noValidate>
        <TextField label="Username" name="username" value={username} onChange={setUsername}
          autoComplete="username" hint="3-30 characters: letters, digits, _ . -" required />
        <TextField label="Email" name="email" type="email" value={email} onChange={setEmail}
          autoComplete="email" required />
        <TextField label="Password" name="password" type="password" value={password} onChange={setPassword}
          autoComplete="new-password" hint="At least 8 characters, with a letter and a digit" required />
        <Button type="submit" disabled={submitting} className="w-full">
          {submitting ? 'Creating…' : 'Create account'}
        </Button>
      </form>
      <p className="mt-4 text-sm text-slate-600">
        Already registered?{' '}
        <Link to="/login" className="font-medium text-brand hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  )
}
