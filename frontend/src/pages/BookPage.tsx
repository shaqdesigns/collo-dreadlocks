import { type FormEvent, useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  createBooking,
  getBooking,
  getSalonistsForService,
  getServices,
  getSlots,
} from '../api/client'
import { StatusMessage } from '../components/StatusMessage'
import { apiError } from '../lib/apiError'
import { formatDateTime, formatDuration, formatPrice } from '../lib/format'
import type { Booking, BookingSlot, Salonist, Service } from '../types/catalog'

function paymentCopy(booking: Booking): string {
  if (booking.paymentMethod === 'CASH') {
    if (booking.status === 'CONFIRMED' || booking.status === 'COMPLETED') {
      return 'The visit is confirmed. Pay cash when you arrive at the studio.'
    }
    return 'Pay cash when you arrive. The studio confirms the visit, then the salonist sees it.'
  }
  if (booking.paymentStatus === 'AWAITING') {
    return `A prompt was sent to ${booking.paymentPhone}. Enter your M-Pesa PIN on the phone. This page updates when Safaricom confirms it. The salonist sees the visit only after the admin approves the payment.`
  }
  if (booking.paymentStatus === 'PAID') {
    return 'Safaricom received the payment. The studio still has to approve it before the salonist is told.'
  }
  if (booking.paymentStatus === 'APPROVED') {
    return 'The payment is approved and the salonist has the visit.'
  }
  if (booking.paymentStatus === 'FAILED') {
    return booking.paymentMessage || 'The M-Pesa payment was not completed. The chair has been released.'
  }
  return 'Your appointment is pending confirmation. Save this reference.'
}

function tomorrowIso(): string {
  const date = new Date()
  date.setDate(date.getDate() + 1)
  return date.toISOString().slice(0, 10)
}

export function BookPage() {
  const [params] = useSearchParams()
  const [services, setServices] = useState<Service[]>([])
  const [salonists, setSalonists] = useState<Salonist[]>([])
  const [slots, setSlots] = useState<BookingSlot[]>([])
  const [serviceId, setServiceId] = useState(Number(params.get('serviceId') ?? 0))
  const [salonistId, setSalonistId] = useState(Number(params.get('salonistId') ?? 0))
  const [date, setDate] = useState(tomorrowIso())
  const [startAt, setStartAt] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [paymentMethod, setPaymentMethod] = useState<'MPESA' | 'CASH'>('MPESA')
  const [mpesaPhone, setMpesaPhone] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [booking, setBooking] = useState<Booking | null>(null)

  useEffect(() => {
    getServices()
      .then(setServices)
      .catch(() => setError('Could not load services.'))
  }, [])

  useEffect(() => {
    if (!serviceId) {
      setSalonists([])
      return
    }
    getSalonistsForService(serviceId)
      .then(setSalonists)
      .catch(() => setSalonists([]))
  }, [serviceId])

  useEffect(() => {
    if (!serviceId || !date) {
      setSlots([])
      return
    }
    getSlots(serviceId, date, salonistId || undefined)
      .then((next) => {
        setSlots(next)
        setStartAt('')
      })
      .catch(() => setSlots([]))
  }, [serviceId, date, salonistId])

  const selectedService = useMemo(
    () => services.find((service) => service.id === serviceId),
    [services, serviceId],
  )

  useEffect(() => {
    if (!booking || booking.paymentMethod !== 'MPESA' || booking.paymentStatus !== 'AWAITING') {
      return
    }
    const timer = window.setInterval(() => {
      getBooking(booking.reference)
        .then(setBooking)
        .catch(() => undefined)
    }, 5000)
    return () => window.clearInterval(timer)
  }, [booking])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!serviceId || !startAt || !name.trim() || !phone.trim()) {
      setError('Choose a service, a time, then enter your name and phone.')
      return
    }
    if (paymentMethod === 'MPESA' && !(mpesaPhone.trim() || phone.trim())) {
      setError('Enter the M-Pesa number that should receive the PIN prompt.')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const created = await createBooking({
        serviceId,
        salonistId: salonistId || undefined,
        startAt,
        customer: { name: name.trim(), phone: phone.trim(), email: null },
        paymentMethod,
        mpesaPhone: paymentMethod === 'MPESA' ? (mpesaPhone.trim() || phone.trim()) : null,
      })
      setBooking(created)
    } catch (cause) {
      setError(apiError(cause, 'The booking was not completed. This time is still open.'))
    } finally {
      setSubmitting(false)
    }
  }

  if (booking) {
    return (
      <main className="mx-auto max-w-xl px-5 py-16">
        <p className="text-xs uppercase tracking-[0.25em] text-gold">Reserved</p>
        <h1 className="font-display mt-3 text-5xl">Request received</h1>
        <p className="mt-4 text-muted">{paymentCopy(booking)}</p>
        <div className="mt-8 border border-fog p-6">
          <p className="text-sm text-muted">Reference</p>
          <p className="mt-1 font-medium">{booking.reference}</p>
          <p className="mt-4">{booking.serviceName}</p>
          <p className="text-muted">{booking.salonistName}</p>
          <p className="mt-2">{formatDateTime(booking.startAt)}</p>
          <p className="mt-4 text-sm uppercase tracking-widest text-gold">
            {booking.paymentMethod ?? 'PAYMENT'} · {booking.paymentStatus ?? booking.status}
          </p>
          {booking.mpesaReceipt && (
            <p className="mt-2 text-sm">M-Pesa receipt {booking.mpesaReceipt}</p>
          )}
        </div>
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-2xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Reserve</p>
      <h1 className="font-display mt-3 text-5xl">Book a chair</h1>
      <p className="mt-4 text-muted">No account needed. Name and phone are enough.</p>
      <form className="mt-10 space-y-8" onSubmit={onSubmit}>
        <label className="block">
          <span className="text-sm text-muted">Service</span>
          <select
            className="mt-2 w-full border border-fog bg-paper px-3 py-3"
            value={serviceId || ''}
            onChange={(event) => setServiceId(Number(event.target.value))}
          >
            <option value="">Select a service</option>
            {services.map((service) => (
              <option key={service.id} value={service.id}>
                {service.name} · {formatPrice(service.price)}
              </option>
            ))}
          </select>
        </label>

        <label className="block">
          <span className="text-sm text-muted">Salonist</span>
          <select
            className="mt-2 w-full border border-fog bg-paper px-3 py-3"
            value={salonistId || ''}
            onChange={(event) => setSalonistId(Number(event.target.value))}
          >
            <option value="">Any available</option>
            {salonists.map((salonist) => (
              <option key={salonist.id} value={salonist.id}>
                {salonist.name}
              </option>
            ))}
          </select>
        </label>

        <label className="block">
          <span className="text-sm text-muted">Date</span>
          <input
            type="date"
            className="mt-2 w-full border border-fog bg-paper px-3 py-3"
            value={date}
            onChange={(event) => setDate(event.target.value)}
          />
        </label>

        <div>
          <p className="text-sm text-muted">Time</p>
          {selectedService && (
            <p className="mt-1 text-xs text-muted">
              {formatDuration(selectedService.durationMinutes)}
            </p>
          )}
          <div className="mt-3 flex flex-wrap gap-2">
            {slots.map((slot) => (
              <button
                key={`${slot.salonistId}-${slot.startAt}`}
                type="button"
                onClick={() => {
                  setStartAt(slot.startAt)
                  setSalonistId(slot.salonistId)
                }}
                className={`rounded-full px-3 py-2 text-sm ${
                  startAt === slot.startAt
                    ? 'bg-ink text-paper'
                    : 'border border-fog text-muted'
                }`}
              >
                {new Date(slot.startAt).toLocaleTimeString('en-KE', {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </button>
            ))}
            {serviceId && slots.length === 0 && (
              <p className="text-sm text-muted">No open chairs on this date.</p>
            )}
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <label className="block">
            <span className="text-sm text-muted">Your name</span>
            <input
              className="mt-2 w-full border border-fog bg-paper px-3 py-3"
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
            />
          </label>
          <label className="block">
            <span className="text-sm text-muted">Phone</span>
            <input
              className="mt-2 w-full border border-fog bg-paper px-3 py-3"
              value={phone}
              onChange={(event) => setPhone(event.target.value)}
              required
            />
          </label>
        </div>

        <fieldset>
          <legend className="text-sm text-muted">Payment</legend>
          <div className="mt-3 flex gap-3">
            <button
              type="button"
              onClick={() => setPaymentMethod('MPESA')}
              className={`rounded-full px-4 py-2 text-sm ${paymentMethod === 'MPESA' ? 'bg-ink text-paper' : 'border border-fog'}`}
            >
              M-Pesa
            </button>
            <button
              type="button"
              onClick={() => setPaymentMethod('CASH')}
              className={`rounded-full px-4 py-2 text-sm ${paymentMethod === 'CASH' ? 'bg-ink text-paper' : 'border border-fog'}`}
            >
              Pay cash
            </button>
          </div>
          {paymentMethod === 'MPESA' && (
            <label className="mt-4 block">
              <span className="text-sm text-muted">M-Pesa phone. This number receives the PIN prompt.</span>
              <input
                className="mt-2 w-full border border-fog bg-paper px-3 py-3"
                value={mpesaPhone}
                placeholder={phone || '07XXXXXXXX'}
                onChange={(event) => setMpesaPhone(event.target.value)}
              />
            </label>
          )}
          {paymentMethod === 'CASH' && (
            <p className="mt-3 text-sm text-muted">
              Pay at the studio on the day of the visit. The salonist sees the booking once the studio confirms it.
            </p>
          )}
        </fieldset>

        <StatusMessage error={error} />
        <button
          type="submit"
          disabled={submitting}
          className="rounded-full bg-ink px-6 py-3 text-sm text-paper hover:bg-clay disabled:opacity-60"
        >
          {submitting ? 'Sending…' : paymentMethod === 'MPESA' ? 'Pay with M-Pesa' : 'Request appointment'}
        </button>
      </form>
    </main>
  )
}
