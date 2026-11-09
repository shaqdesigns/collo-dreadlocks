import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { cancelBooking, confirmBooking, getAdminBookings } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AdminNav } from '../components/AdminNav'
import { apiError } from '../lib/apiError'
import { formatDateTime, formatPrice } from '../lib/format'
import type { Booking } from '../types/catalog'

function canApprove(booking: Booking): boolean {
  if (booking.status !== 'PENDING') {
    return false
  }
  if (!booking.paymentMethod) {
    return true
  }
  if (booking.paymentMethod === 'CASH' && booking.paymentStatus === 'AWAITING') {
    return true
  }
  return booking.paymentStatus === 'PAID'
}

export function AdminPage() {
  const { session } = useAuth()
  const [bookings, setBookings] = useState<Booking[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (session?.role !== 'ADMIN') {
      return
    }
    getAdminBookings()
      .then(setBookings)
      .catch(() => setError('Could not load bookings.'))
  }, [session])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  async function update(id: number, action: 'confirm' | 'cancel') {
    setError(null)
    try {
      const next = action === 'confirm' ? await confirmBooking(id) : await cancelBooking(id)
      setBookings((current) => current.map((item) => (item.id === next.id ? next : item)))
    } catch (cause) {
      setError(apiError(cause, 'Could not update that booking.'))
    }
  }

  return (
    <main className="mx-auto max-w-5xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Admin</p>
      <h1 className="font-display mt-3 text-5xl">Bookings</h1>
      <p className="mt-3 max-w-2xl text-sm text-muted">
        Confirm a cash booking to send it to the salonist. The client pays at the studio on the day. M-Pesa still waits until Safaricom confirms the PIN, then you approve that payment.
      </p>
      <p className="mt-3 text-sm text-muted">{session.email}</p>
      <AdminNav />
      {error && <p className="mt-6 text-sm text-clay">{error}</p>}
      <div className="mt-8 space-y-4">
        {bookings.map((booking) => (
          <article key={booking.id} className="border border-fog p-5">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <p className="font-display text-2xl">{booking.serviceName}</p>
                <p className="mt-1 text-sm text-muted">
                  {booking.customerName} · {booking.customerPhone}
                </p>
                <p className="mt-1 text-sm">{booking.salonistName}</p>
                <p className="mt-1 text-sm">{formatDateTime(booking.startAt)}</p>
                {booking.paymentMethod && (
                  <p className="mt-2 text-sm">
                    {booking.paymentMethod} · {booking.paymentStatus}
                    {booking.paymentPhone ? ` · ${booking.paymentPhone}` : ''}
                    {booking.mpesaReceipt ? ` · ${booking.mpesaReceipt}` : ''}
                  </p>
                )}
                {booking.paymentMethod === 'MPESA' && booking.paymentStatus === 'AWAITING' && (
                  <p className="mt-1 text-sm text-muted">Waiting for the client to enter their M-Pesa PIN.</p>
                )}
                {booking.paymentMethod === 'CASH' && booking.paymentStatus === 'AWAITING' && (
                  <p className="mt-1 text-sm text-muted">Pays cash on the day of the visit.</p>
                )}
              </div>
              <div className="text-right">
                <p className="text-xs uppercase tracking-widest text-gold">{booking.status}</p>
                <p className="mt-2 text-sm">{formatPrice(booking.price)}</p>
              </div>
            </div>
            {(booking.status === 'PENDING' || booking.status === 'CONFIRMED') && (
              <div className="mt-4 flex gap-3">
                {canApprove(booking) && (
                  <button type="button" onClick={() => update(booking.id, 'confirm')} className="rounded-full bg-ink px-4 py-2 text-sm text-paper">
                    {booking.paymentMethod === 'CASH' ? 'Confirm booking' : 'Approve payment'}
                  </button>
                )}
                <button type="button" onClick={() => update(booking.id, 'cancel')} className="rounded-full border border-fog px-4 py-2 text-sm">
                  Cancel
                </button>
              </div>
            )}
          </article>
        ))}
        {bookings.length === 0 && !error && (
          <p className="text-sm text-muted">No bookings yet.</p>
        )}
      </div>
    </main>
  )
}
