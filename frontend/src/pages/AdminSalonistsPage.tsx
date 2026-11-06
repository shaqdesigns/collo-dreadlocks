import { type FormEvent, useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import {
  approveUnavailability,
  createSalonist,
  createSalonistAccess,
  deleteSalonist,
  getAdminBookings,
  getAdminRemarks,
  getAdminSalonists,
  getAdminUnavailability,
  getServices,
  rejectUnavailability,
  uploadSalonistPhoto,
} from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AdminNav } from '../components/AdminNav'
import { apiError } from '../lib/apiError'
import { formatDateTime } from '../lib/format'
import type { AdminSalonist, Booking, Remark, Service, Unavailability } from '../types/catalog'

function coversNow(request: Unavailability): boolean {
  if (request.status !== 'APPROVED') {
    return false
  }
  const now = Date.now()
  return new Date(request.startAt).getTime() <= now && now < new Date(request.endAt).getTime()
}

export function AdminSalonistsPage() {
  const { session } = useAuth()
  const [salonists, setSalonists] = useState<AdminSalonist[]>([])
  const [bookings, setBookings] = useState<Booking[]>([])
  const [requests, setRequests] = useState<Unavailability[]>([])
  const [remarks, setRemarks] = useState<Remark[]>([])
  const [services, setServices] = useState<Service[]>([])
  const [error, setError] = useState<string | null>(null)
  const [showCreate, setShowCreate] = useState(false)

  useEffect(() => {
    if (session?.role !== 'ADMIN') {
      return
    }
    Promise.all([getAdminSalonists(), getAdminBookings(), getAdminUnavailability(), getAdminRemarks(), getServices()])
      .then(([nextSalonists, nextBookings, nextRequests, nextRemarks, nextServices]) => {
        setSalonists(nextSalonists)
        setBookings(nextBookings)
        setRequests(nextRequests)
        setRemarks(nextRemarks)
        setServices(nextServices)
      })
      .catch(() => setError('Could not load salonists.'))
  }, [session])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  async function remove(salonist: AdminSalonist) {
    if (!window.confirm(`Delete ${salonist.name}?`)) {
      return
    }
    try {
      await deleteSalonist(salonist.id)
      setSalonists((current) => current.filter((item) => item.id !== salonist.id))
    } catch (cause) {
      setError(apiError(cause, 'Could not delete that salonist.'))
    }
  }

  async function review(id: number, action: 'approve' | 'reject') {
    const next = action === 'approve' ? await approveUnavailability(id) : await rejectUnavailability(id)
    setRequests((current) => current.map((item) => (item.id === next.id ? next : item)))
  }

  return (
    <main className="mx-auto max-w-5xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Admin</p>
      <h1 className="font-display mt-3 text-5xl">Salonists</h1>
      <p className="mt-3 text-sm text-muted">{session.email}</p>
      <AdminNav />
      <div className="mt-8">
        <button
          type="button"
          onClick={() => setShowCreate((current) => !current)}
          className="rounded-full bg-ink px-5 py-2 text-sm text-paper"
        >
          Add salonist
        </button>
        {showCreate && (
          <CreateSalonistForm
            services={services}
            onCreated={(created) => {
              setSalonists((current) => [...current, created].sort((a, b) => a.name.localeCompare(b.name)))
              setShowCreate(false)
            }}
          />
        )}
      </div>
      {error && <p className="mt-6 text-sm text-clay">{error}</p>}
      <div className="mt-8 space-y-8">
        {salonists.map((salonist) => {
          const timeOff = requests.filter((item) => item.salonistId === salonist.id)
          const work = bookings.filter(
            (item) => item.salonistId === salonist.id && item.status === 'COMPLETED',
          )
          const notes = remarks.filter((item) => item.salonistId === salonist.id)
          const offNow = timeOff.some(coversNow)
          return (
            <article key={salonist.id} className="border border-fog p-5">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                  <h2 className="font-display text-3xl">{salonist.name}</h2>
                  <p className="mt-1 text-sm text-muted">
                    {salonist.phone}
                    {salonist.email ? ` · ${salonist.email}` : ''}
                  </p>
                  <button
                    type="button"
                    onClick={() => remove(salonist)}
                    className="mt-3 rounded-full border border-clay px-4 py-2 text-sm text-clay"
                  >
                    Delete salonist
                  </button>
                  <label className="mt-3 block text-sm text-muted">
                    Profile photo
                    <input
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      className="mt-2 block text-sm text-ink"
                      onChange={async (event) => {
                        const file = event.target.files?.[0]
                        event.target.value = ''
                        if (!file) {
                          return
                        }
                        const updated = await uploadSalonistPhoto(salonist.id, file)
                        setSalonists((current) =>
                          current.map((item) =>
                            item.id === salonist.id ? { ...item, profileImageUrl: updated.profileImageUrl } : item,
                          ),
                        )
                      }}
                    />
                  </label>
                </div>
                <div className="text-right text-xs uppercase tracking-widest text-gold">
                  {salonist.profileImageUrl && (
                    <img src={salonist.profileImageUrl} alt="" className="mb-3 ml-auto h-16 w-16 rounded-full object-cover" />
                  )}
                  <p>{salonist.isActive ? 'Active' : 'Inactive'}</p>
                  <p className="mt-1">{offNow ? 'Off now' : 'On the chair'}</p>
                  <p className="mt-1">{salonist.hasPortalAccess ? 'Login set' : 'No login'}</p>
                </div>
              </div>

              <AccessForm
                salonist={salonist}
                onSaved={(email) => {
                  setSalonists((current) =>
                    current.map((item) =>
                      item.id === salonist.id ? { ...item, email, hasPortalAccess: true } : item,
                    ),
                  )
                }}
              />

              <section className="mt-8">
                <h3 className="text-xs uppercase tracking-[0.2em] text-muted">Time off</h3>
                <div className="mt-3 space-y-3">
                  {timeOff.map((item) => (
                    <div key={item.id} className="flex flex-wrap items-start justify-between gap-3 text-sm">
                      <div>
                        <p>
                          {formatDateTime(item.startAt)} – {formatDateTime(item.endAt)}
                        </p>
                        {item.reason && <p className="text-muted">{item.reason}</p>}
                      </div>
                      <div className="text-right">
                        <p className="text-xs uppercase tracking-widest text-gold">{item.status}</p>
                        {item.status === 'PENDING' && (
                          <div className="mt-2 flex gap-2">
                            <button
                              type="button"
                              onClick={() => review(item.id, 'approve')}
                              className="rounded-full bg-ink px-3 py-1 text-xs text-paper"
                            >
                              Approve
                            </button>
                            <button
                              type="button"
                              onClick={() => review(item.id, 'reject')}
                              className="rounded-full border border-fog px-3 py-1 text-xs"
                            >
                              Reject
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                  {timeOff.length === 0 && <p className="text-sm text-muted">No time-off requests.</p>}
                </div>
              </section>

              <section className="mt-8">
                <h3 className="text-xs uppercase tracking-[0.2em] text-muted">Previous work</h3>
                <div className="mt-3 space-y-2">
                  {work.map((item) => (
                    <p key={item.id} className="text-sm">
                      {item.serviceName} · {item.customerName} · {formatDateTime(item.startAt)}
                    </p>
                  ))}
                  {work.length === 0 && <p className="text-sm text-muted">No completed appointments yet.</p>}
                </div>
              </section>

              <section className="mt-8">
                <h3 className="text-xs uppercase tracking-[0.2em] text-muted">Client remarks</h3>
                <div className="mt-3 space-y-3">
                  {notes.map((item) => (
                    <blockquote key={item.id} className="border-l border-gold pl-3 text-sm">
                      <p>{item.body}</p>
                      <p className="mt-1 text-muted">
                        {item.customerName} · {item.serviceName} · {formatDateTime(item.appointmentAt)}
                      </p>
                    </blockquote>
                  ))}
                  {notes.length === 0 && (
                    <p className="text-sm text-muted">No remarks yet. Clients leave these after a completed visit.</p>
                  )}
                </div>
              </section>
            </article>
          )
        })}
        {salonists.length === 0 && !error && <p className="text-sm text-muted">No salonists yet.</p>}
      </div>
    </main>
  )
}

function AccessForm({
  salonist,
  onSaved,
}: {
  salonist: AdminSalonist
  onSaved: (email: string) => void
}) {
  const [email, setEmail] = useState(salonist.email ?? '')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setMessage(null)
    try {
      await createSalonistAccess(salonist.id, email.trim(), password)
      setPassword('')
      setMessage('Login saved. Share this email and password with the salonist. They use Sign in.')
      onSaved(email.trim())
    } catch (cause) {
      setError(apiError(cause, 'Could not save the salonist login.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="mt-6 grid gap-3 border-t border-fog pt-5 sm:grid-cols-[1fr_1fr_auto]" onSubmit={onSubmit}>
      <label className="block text-sm">
        <span className="text-muted">Login email</span>
        <input
          type="email"
          required
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        />
      </label>
      <label className="block text-sm">
        <span className="text-muted">Password</span>
        <input
          type="password"
          required
          minLength={8}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        />
      </label>
      <div className="sm:self-end">
        <button
          type="submit"
          disabled={saving}
          className="rounded-full bg-ink px-4 py-2 text-sm text-paper disabled:opacity-60"
        >
          {saving ? 'Saving…' : salonist.hasPortalAccess ? 'Reset login' : 'Create login'}
        </button>
      </div>
      {message && <p className="text-sm text-sage sm:col-span-3">{message}</p>}
      {error && <p className="text-sm text-clay sm:col-span-3">{error}</p>}
    </form>
  )
}

function CreateSalonistForm({
  services,
  onCreated,
}: {
  services: Service[]
  onCreated: (salonist: AdminSalonist) => void
}) {
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [bio, setBio] = useState('')
  const [serviceIds, setServiceIds] = useState<number[]>([])
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  function toggle(id: number) {
    setServiceIds((current) => (current.includes(id) ? current.filter((item) => item !== id) : [...current, id]))
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const created = await createSalonist({
        name: name.trim(),
        phone: phone.trim(),
        email: email.trim() || null,
        bio: bio.trim() || null,
        serviceIds,
      })
      onCreated(created)
    } catch (cause) {
      setError(apiError(cause, 'Could not add the salonist.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="mt-4 space-y-4 border border-fog p-5" onSubmit={onSubmit}>
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
        <span className="text-muted">Bio</span>
        <textarea value={bio} onChange={(event) => setBio(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" rows={3} />
      </label>
      <fieldset>
        <legend className="text-sm text-muted">Services they can do</legend>
        <div className="mt-3 grid gap-2 sm:grid-cols-2">
          {services.map((service) => (
            <label key={service.id} className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={serviceIds.includes(service.id)} onChange={() => toggle(service.id)} />
              {service.name}
            </label>
          ))}
        </div>
      </fieldset>
      {error && <p className="text-sm text-clay">{error}</p>}
      <button type="submit" disabled={saving} className="rounded-full bg-ink px-5 py-2 text-sm text-paper disabled:opacity-60">
        {saving ? 'Saving…' : 'Save salonist'}
      </button>
    </form>
  )
}
