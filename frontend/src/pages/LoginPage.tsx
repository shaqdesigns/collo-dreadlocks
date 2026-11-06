import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { homeForRole } from '../auth/session'
import axios from 'axios'

export function LoginPage() {
  const navigate = useNavigate()
  const { signIn } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const session = await login(email.trim(), password)
      signIn(session)
      navigate(homeForRole(session.role))
    } catch (cause) {
      if (axios.isAxiosError(cause) && cause.response?.status === 401) {
        setError('Email or password is incorrect.')
      } else {
        setError('Could not sign in. Check that the backend is running.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="mx-auto max-w-md px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Studio access</p>
      <h1 className="font-display mt-3 text-5xl">Sign in</h1>
      <p className="mt-4 text-muted">
        Admin, salonist, and client accounts. Guests can still book without signing in.
      </p>
      <form className="mt-10 space-y-5" onSubmit={onSubmit}>
        <label className="block">
          <span className="text-sm text-muted">Email</span>
          <input
            type="email"
            autoComplete="username"
            className="mt-2 w-full border border-fog bg-paper px-3 py-3"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>
        <label className="block">
          <span className="text-sm text-muted">Password</span>
          <input
            type="password"
            autoComplete="current-password"
            className="mt-2 w-full border border-fog bg-paper px-3 py-3"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </label>
        {error && <p className="text-sm text-clay">{error}</p>}
        <button
          type="submit"
          disabled={submitting}
          className="rounded-full bg-ink px-6 py-3 text-sm text-paper hover:bg-clay disabled:opacity-60"
        >
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
      <p className="mt-8 text-sm text-muted">Join our family</p>
      <Link
        to="/register"
        className="mt-4 inline-block rounded-full border border-ink px-6 py-3 text-sm text-ink hover:bg-ink hover:text-paper"
      >
        Create account
      </Link>
    </main>
  )
}
