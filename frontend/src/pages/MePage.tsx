import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { getMyBookings, getMyRemarks, leaveRemark } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { apiError } from '../lib/apiError'
import { formatDateTime } from '../lib/format'
import type { Booking, Remark } from '../types/catalog'

export function MePage() {
  const { session } = useAuth()
  const [bookings, setBookings] = useState<Booking[]>([])
  const [remarks, setRemarks] = useState<Remark[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (session?.role !== 'CUSTOMER') {
      return
    }
    Promise.all([getMyBookings(), getMyRemarks()])
      .then(([nextBookings, nextRemarks]) => {
        setBookings(nextBookings)
        setRemarks(nextRemarks)
      })
      .catch(() => setError('Could not load your bookings.'))
  }, [session])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'CUSTOMER') {
    return <Navigate to="/" replace />
  }

  return (
    <main className="mx-auto max-w-3xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Client</p>
      <h1 className="font-display mt-3 text-5xl">My bookings</h1>
      <p className="mt-3 text-sm text-muted">{session.email}</p>
      {error && <p className="mt-6 text-sm text-clay">{error}</p>}
      <div className="mt-8 space-y-4">
        {bookings.map((booking) => (
          <article key={booking.id} className="border border-fog p-5">
            <p className="font-display text-2xl">{booking.serviceName}</p>
            <p className="mt-1 text-sm text-muted">{booking.salonistName}</p>
            <p className="mt-1 text-sm">{formatDateTime(booking.startAt)}</p>
            <p className="mt-2 text-xs uppercase tracking-widest text-gold">{booking.status}</p>
            {booking.status === 'COMPLETED' && (
              <RemarkBox
                bookingId={booking.id}
                existing={remarks.find((item) => item.bookingId === booking.id)}
                onSaved={(remark) => setRemarks((current) => [remark, ...current])}
              />
            )}
          </article>
        ))}
        {bookings.length === 0 && !error && (
          <p className="text-sm text-muted">
            No account bookings yet. <Link to="/book" className="underline decoration-gold">Book a chair</Link>
          </p>
        )}
      </div>
    </main>
  )
}

function RemarkBox({
  bookingId,
  existing,
  onSaved,
}: {
  bookingId: number
  existing: Remark | undefined
  onSaved: (remark: Remark) => void
}) {
  const [body, setBody] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  if (existing) {
    return <p className="mt-4 border-l border-gold pl-3 text-sm">{existing.body}</p>
  }

  return (
    <form
      className="mt-4 space-y-3"
      onSubmit={async (event) => {
        event.preventDefault()
        setSaving(true)
        setError(null)
        try {
          onSaved(await leaveRemark(bookingId, body.trim()))
        } catch (cause) {
          setError(apiError(cause, 'Could not save the remark.'))
        } finally {
          setSaving(false)
        }
      }}
    >
      <label className="block text-sm">
        <span className="text-muted">Remark for your salonist</span>
        <textarea
          required
          maxLength={1000}
          value={body}
          onChange={(event) => setBody(event.target.value)}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
          rows={3}
        />
      </label>
      {error && <p className="text-sm text-clay">{error}</p>}
      <button
        type="submit"
        disabled={saving}
        className="rounded-full border border-ink px-4 py-2 text-sm disabled:opacity-60"
      >
        {saving ? 'Saving…' : 'Leave remark'}
      </button>
    </form>
  )
}
