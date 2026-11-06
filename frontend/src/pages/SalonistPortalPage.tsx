import { type FormEvent, useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import {
  cancelTimeOff,
  completeSalonistBooking,
  getSalonistBookings,
  getSalonistEarnings,
  getSalonistProfile,
  getSalonistRemarks,
  getSalonistUnavailability,
  getServices,
  recordSalonistWork,
  requestTimeOff,
} from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { apiError } from '../lib/apiError'
import { formatDateTime, formatPrice } from '../lib/format'
import type { Booking, Remark, Salonist, SalonistEarnings, Service, Unavailability } from '../types/catalog'

function coversNow(request: Unavailability): boolean {
  if (request.status !== 'APPROVED') {
    return false
  }
  const now = Date.now()
  return new Date(request.startAt).getTime() <= now && now < new Date(request.endAt).getTime()
}

function toApiDateTime(value: string): string {
  return value.length === 16 ? `${value}:00` : value
}

export function SalonistPortalPage() {
  const { session } = useAuth()
  const [profile, setProfile] = useState<Salonist | null>(null)
  const [bookings, setBookings] = useState<Booking[]>([])
  const [requests, setRequests] = useState<Unavailability[]>([])
  const [remarks, setRemarks] = useState<Remark[]>([])
  const [earnings, setEarnings] = useState<SalonistEarnings | null>(null)
  const [services, setServices] = useState<Service[]>([])
  const [error, setError] = useState<string | null>(null)
  const [startAt, setStartAt] = useState('')
  const [endAt, setEndAt] = useState('')
  const [reason, setReason] = useState('')
  const [requestError, setRequestError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (session?.role !== 'SALONIST') {
      return
    }
    Promise.all([
      getSalonistProfile(),
      getSalonistBookings(),
      getSalonistUnavailability(),
      getSalonistRemarks(),
      getSalonistEarnings(),
      getServices(),
    ])
      .then(([nextProfile, nextBookings, nextRequests, nextRemarks, nextEarnings, nextServices]) => {
        setProfile(nextProfile)
        setBookings(nextBookings)
        setRequests(nextRequests)
        setRemarks(nextRemarks)
        setEarnings(nextEarnings)
        setServices(nextServices)
      })
      .catch(() => setError('Could not load your chair.'))
  }, [session])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'SALONIST') {
    return <Navigate to="/" replace />
  }

  const upcoming = bookings.filter((item) => item.status === 'PENDING' || item.status === 'CONFIRMED')
  const previous = bookings.filter((item) => item.status === 'COMPLETED')
  const offNow = requests.some(coversNow)

  async function onRequestOff(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setRequestError(null)
    try {
      const created = await requestTimeOff({
        startAt: toApiDateTime(startAt),
        endAt: toApiDateTime(endAt),
        reason: reason.trim() || null,
      })
      setRequests((current) => [created, ...current])
      setStartAt('')
      setEndAt('')
      setReason('')
    } catch (cause) {
      setRequestError(apiError(cause, 'Could not send the time-off request.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="mx-auto max-w-3xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Salonist</p>
      <div className="mt-6 flex items-center gap-4">
        {profile?.profileImageUrl && (
          <img src={profile.profileImageUrl} alt="" className="h-16 w-16 rounded-full object-cover" />
        )}
        <div>
          <h1 className="font-display text-5xl">{profile?.name ?? 'My chair'}</h1>
          <p className="mt-2 text-sm text-muted">{session.email}</p>
        </div>
      </div>
      {error && <p className="mt-6 text-sm text-clay">{error}</p>}

      <section className="mt-8 border border-fog p-5">
        <h2 className="text-xs uppercase tracking-[0.2em] text-muted">My earnings</h2>
        <p className="mt-3 text-sm">
          {earnings
            ? `${formatPrice(earnings.revenue)} taken · your 40% is ${formatPrice(earnings.payout)}`
            : 'Earnings appear after completed work.'}
        </p>
        <WalkInForm services={services} onSaved={async () => setEarnings(await getSalonistEarnings())} />
      </section>

      <section className="mt-8 border border-fog p-5">
        <h2 className="text-xs uppercase tracking-[0.2em] text-muted">Status</h2>
        <p className="mt-3 text-sm">
          {profile ? (profile.isActive ? 'Active' : 'Inactive') : '—'} · {offNow ? 'Off now' : 'On the chair'}
        </p>
      </section>

      <section className="mt-8 border border-fog p-5">
        <h2 className="text-xs uppercase tracking-[0.2em] text-muted">Request time off</h2>
        <p className="mt-3 text-sm text-muted">
          Clients still see your usual hours until an admin approves this.
        </p>
        <form className="mt-4 space-y-4" onSubmit={onRequestOff}>
          <label className="block text-sm">
            <span className="text-muted">From</span>
            <input
              type="datetime-local"
              required
              value={startAt}
              onChange={(event) => setStartAt(event.target.value)}
              className="mt-2 w-full border border-fog bg-paper px-3 py-2"
            />
          </label>
          <label className="block text-sm">
            <span className="text-muted">Until</span>
            <input
              type="datetime-local"
              required
              value={endAt}
              onChange={(event) => setEndAt(event.target.value)}
              className="mt-2 w-full border border-fog bg-paper px-3 py-2"
            />
          </label>
          <label className="block text-sm">
            <span className="text-muted">Reason</span>
            <input
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              className="mt-2 w-full border border-fog bg-paper px-3 py-2"
            />
          </label>
          {requestError && <p className="text-sm text-clay">{requestError}</p>}
          <button
            type="submit"
            disabled={saving}
            className="rounded-full bg-ink px-4 py-2 text-sm text-paper disabled:opacity-60"
          >
            {saving ? 'Sending…' : 'Request time off'}
          </button>
        </form>
        <div className="mt-6 space-y-3">
          {requests.map((item) => (
            <div key={item.id} className="flex flex-wrap items-start justify-between gap-3 text-sm">
              <div>
                <p>
                  {formatDateTime(item.startAt)} – {formatDateTime(item.endAt)}
                </p>
                {item.reason && <p className="text-muted">{item.reason}</p>}
                {item.adminNote && <p className="text-muted">Admin: {item.adminNote}</p>}
              </div>
              <div className="text-right">
                <p className="text-xs uppercase tracking-widest text-gold">{item.status}</p>
                {item.status === 'PENDING' && (
                  <button
                    type="button"
                    className="mt-2 text-xs underline decoration-gold"
                    onClick={async () => {
                      const next = await cancelTimeOff(item.id)
                      setRequests((current) => current.map((entry) => (entry.id === next.id ? next : entry)))
                    }}
                  >
                    Cancel request
                  </button>
                )}
              </div>
            </div>
          ))}
          {requests.length === 0 && <p className="text-sm text-muted">No time-off requests.</p>}
        </div>
      </section>

      <section className="mt-8">
        <h2 className="text-xs uppercase tracking-[0.2em] text-muted">On the chair</h2>
        <div className="mt-4 space-y-4">
          {upcoming.map((booking) => (
            <article key={booking.id} className="border border-fog p-5">
              <p className="font-display text-2xl">{booking.serviceName}</p>
              <p className="mt-1 text-sm text-muted">{booking.customerName} · {booking.customerPhone}</p>
              <p className="mt-1 text-sm">{formatDateTime(booking.startAt)}</p>
              <p className="mt-2 text-xs uppercase tracking-widest text-gold">{booking.status}</p>
              {booking.status === 'CONFIRMED' && (
                <button
                  type="button"
                  className="mt-4 rounded-full bg-ink px-4 py-2 text-sm text-paper"
                  onClick={async () => {
                    const next = await completeSalonistBooking(booking.id)
                    setBookings((current) => current.map((item) => (item.id === next.id ? next : item)))
                  }}
                >
                  Mark completed
                </button>
              )}
            </article>
          ))}
          {upcoming.length === 0 && <p className="text-sm text-muted">No upcoming appointments.</p>}
        </div>
      </section>

      <section className="mt-10">
        <h2 className="text-xs uppercase tracking-[0.2em] text-muted">Previous work</h2>
        <div className="mt-4 space-y-3">
          {previous.map((booking) => (
            <p key={booking.id} className="text-sm">
              {booking.serviceName} · {booking.customerName} · {formatDateTime(booking.startAt)}
            </p>
          ))}
          {previous.length === 0 && <p className="text-sm text-muted">No completed appointments yet.</p>}
        </div>
      </section>

      <section className="mt-10">
        <h2 className="text-xs uppercase tracking-[0.2em] text-muted">Remarks from clients</h2>
        <div className="mt-4 space-y-3">
          {remarks.map((item) => (
            <blockquote key={item.id} className="border-l border-gold pl-3 text-sm">
              <p>{item.body}</p>
              <p className="mt-1 text-muted">
                {item.customerName} · {item.serviceName} · {formatDateTime(item.appointmentAt)}
              </p>
            </blockquote>
          ))}
          {remarks.length === 0 && <p className="text-sm text-muted">No remarks yet.</p>}
        </div>
      </section>
    </main>
  )
}

function WalkInForm({
  services,
  onSaved,
}: {
  services: Service[]
  onSaved: () => Promise<void>
}) {
  const [serviceId, setServiceId] = useState(0)
  const [price, setPrice] = useState('')
  const [workedAt, setWorkedAt] = useState('')
  const [customerName, setCustomerName] = useState('')
  const [note, setNote] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!serviceId && services[0]) {
      setServiceId(services[0].id)
      setPrice(String(services[0].price))
    }
  }, [serviceId, services])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await recordSalonistWork({
        serviceId,
        price: Number(price),
        workedAt: workedAt.length === 16 ? `${workedAt}:00` : workedAt,
        customerName: customerName.trim() || null,
        note: note.trim() || null,
      })
      setCustomerName('')
      setNote('')
      await onSaved()
    } catch (cause) {
      setError(apiError(cause, 'Could not add that work.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="mt-5 space-y-3" onSubmit={onSubmit}>
      <p className="text-sm text-muted">Add a client you saw outside this website.</p>
      <label className="block text-sm">
        <span className="text-muted">Service</span>
        <select
          required
          value={serviceId || ''}
          onChange={(event) => {
            const next = Number(event.target.value)
            setServiceId(next)
            const service = services.find((item) => item.id === next)
            if (service) {
              setPrice(String(service.price))
            }
          }}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        >
          {services.map((service) => (
            <option key={service.id} value={service.id}>{service.name}</option>
          ))}
        </select>
      </label>
      <label className="block text-sm">
        <span className="text-muted">Amount paid</span>
        <input required type="number" min="1" step="1" value={price} onChange={(event) => setPrice(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
      </label>
      <label className="block text-sm">
        <span className="text-muted">When</span>
        <input required type="datetime-local" value={workedAt} onChange={(event) => setWorkedAt(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
      </label>
      <label className="block text-sm">
        <span className="text-muted">Client name</span>
        <input value={customerName} onChange={(event) => setCustomerName(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
      </label>
      <label className="block text-sm">
        <span className="text-muted">Note</span>
        <input value={note} onChange={(event) => setNote(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
      </label>
      {error && <p className="text-sm text-clay">{error}</p>}
      <button type="submit" disabled={saving} className="rounded-full border border-ink px-4 py-2 text-sm disabled:opacity-60">
        {saving ? 'Saving…' : 'Add walk-in'}
      </button>
    </form>
  )
}
