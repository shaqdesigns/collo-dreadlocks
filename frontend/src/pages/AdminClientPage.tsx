import { type FormEvent, useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { addClientNote, getClient, updateCustomer } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AdminNav } from '../components/AdminNav'
import { apiError } from '../lib/apiError'
import { formatDateTime, formatPrice } from '../lib/format'
import type { ClientProfile } from '../types/catalog'

export function AdminClientPage() {
  const { session } = useAuth()
  const { id } = useParams()
  const clientId = Number(id)
  const [profile, setProfile] = useState<ClientProfile | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [note, setNote] = useState('')
  const [savingNote, setSavingNote] = useState(false)

  useEffect(() => {
    if (session?.role !== 'ADMIN' || !Number.isInteger(clientId)) {
      return
    }
    getClient(clientId)
      .then(setProfile)
      .catch(() => setError('Could not load that client.'))
  }, [session, clientId])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  async function saveNote(event: FormEvent) {
    event.preventDefault()
    if (!profile) {
      return
    }
    setSavingNote(true)
    setError(null)
    try {
      const saved = await addClientNote(profile.customer.id, note.trim())
      setProfile({ ...profile, notes: [saved, ...profile.notes] })
      setNote('')
    } catch (cause) {
      setError(apiError(cause, 'Could not save that note.'))
    } finally {
      setSavingNote(false)
    }
  }

  return (
    <main className="mx-auto max-w-5xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Client</p>
      <h1 className="font-display mt-3 text-5xl">{profile?.customer.name ?? 'Client'}</h1>
      <AdminNav />
      <Link to="/admin/clients" className="mt-6 inline-block text-sm text-muted underline decoration-gold">
        All clients
      </Link>
      {error && <p className="mt-6 text-sm text-clay">{error}</p>}
      {profile && (
        <>
          <p className="mt-6 text-sm">
            {profile.visits} visits · {formatPrice(profile.spent)} taken
            {profile.nextVisitAt ? ` · next ${formatDateTime(profile.nextVisitAt)}` : ''}
          </p>
          <ContactForm
            profile={profile}
            onSaved={(customer) => setProfile({ ...profile, customer })}
          />
          <section className="mt-10">
            <h2 className="text-xs uppercase tracking-[0.2em] text-muted">Studio notes</h2>
            <form className="mt-3 space-y-3" onSubmit={saveNote}>
              <textarea
                required
                value={note}
                onChange={(event) => setNote(event.target.value)}
                rows={3}
                placeholder="A private note for the studio"
                className="w-full border border-fog bg-paper px-3 py-2 text-sm"
              />
              <button
                type="submit"
                disabled={savingNote}
                className="rounded-full bg-ink px-4 py-2 text-sm text-paper disabled:opacity-60"
              >
                {savingNote ? 'Saving…' : 'Add note'}
              </button>
            </form>
            <div className="mt-4 space-y-3">
              {profile.notes.map((item) => (
                <blockquote key={item.id} className="border-l border-gold pl-3 text-sm">
                  <p>{item.body}</p>
                  <p className="mt-1 text-muted">{formatDateTime(item.createdAt)}</p>
                </blockquote>
              ))}
              {profile.notes.length === 0 && <p className="text-sm text-muted">No studio notes yet.</p>}
            </div>
          </section>
          <section className="mt-10">
            <h2 className="text-xs uppercase tracking-[0.2em] text-muted">Visits</h2>
            <div className="mt-3 space-y-3">
              {profile.bookings.map((booking) => (
                <p key={booking.id} className="text-sm">
                  {booking.serviceName} · {booking.salonistName} · {formatDateTime(booking.startAt)} · {booking.status} · {formatPrice(booking.price)}
                </p>
              ))}
              {profile.bookings.length === 0 && <p className="text-sm text-muted">No bookings yet.</p>}
            </div>
          </section>
          <section className="mt-10">
            <h2 className="text-xs uppercase tracking-[0.2em] text-muted">Remarks from this client</h2>
            <div className="mt-3 space-y-3">
              {profile.remarks.map((remark) => (
                <blockquote key={remark.id} className="border-l border-gold pl-3 text-sm">
                  <p>{remark.body}</p>
                  <p className="mt-1 text-muted">{remark.serviceName} · {formatDateTime(remark.appointmentAt)}</p>
                </blockquote>
              ))}
              {profile.remarks.length === 0 && <p className="text-sm text-muted">No remarks yet.</p>}
            </div>
          </section>
        </>
      )}
    </main>
  )
}

function ContactForm({
  profile,
  onSaved,
}: {
  profile: ClientProfile
  onSaved: (customer: ClientProfile['customer']) => void
}) {
  const customer = profile.customer
  const [name, setName] = useState(customer.name)
  const [phone, setPhone] = useState(customer.phone)
  const [email, setEmail] = useState(customer.email ?? '')
  const [dateOfBirth, setDateOfBirth] = useState(customer.dateOfBirth ?? '')
  const [active, setActive] = useState(customer.isActive)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)
  const [saving, setSaving] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setSaved(false)
    try {
      const updated = await updateCustomer(customer.id, {
        name: name.trim(),
        phone: phone.trim(),
        email: email.trim() || null,
        dateOfBirth: dateOfBirth || null,
        isActive: active,
      })
      onSaved(updated)
      setSaved(true)
    } catch (cause) {
      setError(apiError(cause, 'Could not save this client.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="mt-8 grid gap-3 border border-fog p-5 sm:grid-cols-2" onSubmit={onSubmit}>
      <label className="block text-sm">
        <span className="text-muted">Name</span>
        <input required value={name} onChange={(event) => setName(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
      </label>
      <label className="block text-sm">
        <span className="text-muted">Phone</span>
        <input required value={phone} onChange={(event) => setPhone(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
      </label>
      <label className="block text-sm">
        <span className="text-muted">Email</span>
        <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
      </label>
      <label className="block text-sm">
        <span className="text-muted">Date of birth</span>
        <input type="date" value={dateOfBirth} onChange={(event) => setDateOfBirth(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
      </label>
      <label className="flex items-center gap-2 text-sm sm:col-span-2">
        <input type="checkbox" checked={active} onChange={(event) => setActive(event.target.checked)} />
        Active client
      </label>
      <div className="flex flex-wrap items-center gap-3 sm:col-span-2">
        <button type="submit" disabled={saving} className="rounded-full bg-ink px-4 py-2 text-sm text-paper disabled:opacity-60">
          {saving ? 'Saving…' : 'Save client'}
        </button>
        {saved && <p className="text-sm text-sage">Saved.</p>}
        {error && <p className="text-sm text-clay">{error}</p>}
      </div>
    </form>
  )
}
