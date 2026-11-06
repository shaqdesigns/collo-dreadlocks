import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import axios from 'axios'
import { registerClient } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export function RegisterPage() {
  const navigate = useNavigate()
  const { signIn } = useAuth()
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const session = await registerClient({
        name: name.trim(),
        phone: phone.trim(),
        email: email.trim(),
        password,
        dateOfBirth: dateOfBirth || null,
      })
      signIn(session)
      navigate('/me')
    } catch (cause) {
      if (axios.isAxiosError(cause) && cause.response?.status === 409) {
        setError('That email is already in use.')
      } else {
        setError('Could not create the account.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="mx-auto max-w-md px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Client portal</p>
      <h1 className="font-display mt-3 text-5xl">Create an account</h1>
      <p className="mt-4 text-muted">Optional. Booking still works without an account.</p>
      <form className="mt-10 space-y-5" onSubmit={onSubmit}>
        <label className="block">
          <span className="text-sm text-muted">Name</span>
          <input className="mt-2 w-full border border-fog bg-paper px-3 py-3" value={name} onChange={(event) => setName(event.target.value)} required />
        </label>
        <label className="block">
          <span className="text-sm text-muted">Phone</span>
          <input className="mt-2 w-full border border-fog bg-paper px-3 py-3" value={phone} onChange={(event) => setPhone(event.target.value)} required />
        </label>
        <label className="block">
          <span className="text-sm text-muted">Email</span>
          <input type="email" className="mt-2 w-full border border-fog bg-paper px-3 py-3" value={email} onChange={(event) => setEmail(event.target.value)} required />
        </label>
        <label className="block">
          <span className="text-sm text-muted">Password</span>
          <input type="password" minLength={8} className="mt-2 w-full border border-fog bg-paper px-3 py-3" value={password} onChange={(event) => setPassword(event.target.value)} required />
        </label>
        <label className="block">
          <span className="text-sm text-muted">Birthday (optional)</span>
          <input type="date" className="mt-2 w-full border border-fog bg-paper px-3 py-3" value={dateOfBirth} onChange={(event) => setDateOfBirth(event.target.value)} />
        </label>
        {error && <p className="text-sm text-clay">{error}</p>}
        <button type="submit" disabled={submitting} className="rounded-full bg-ink px-6 py-3 text-sm text-paper hover:bg-clay disabled:opacity-60">
          {submitting ? 'Creating…' : 'Create account'}
        </button>
      </form>
      <p className="mt-8 text-sm text-muted">
        Already registered?{' '}
        <Link to="/login" className="text-ink underline decoration-gold">Sign in</Link>
      </p>
    </main>
  )
}
