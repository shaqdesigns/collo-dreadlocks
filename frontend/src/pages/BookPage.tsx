import { type FormEvent, useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  createBooking,
  getSalonistsForService,
  getServices,
  getSlots,
} from '../api/client'
import { StatusMessage } from '../components/StatusMessage'
import { formatDateTime, formatDuration, formatPrice } from '../lib/format'
import type { Booking, BookingSlot, Salonist, Service } from '../types/catalog'

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

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!serviceId || !startAt || !name.trim() || !phone.trim()) {
      setError('Choose a service, a time, then enter your name and phone.')
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
      })
      setBooking(created)
    } catch {
      setError('That slot may have just been taken. Pick another time.')
    } finally {
      setSubmitting(false)
    }
  }

  if (booking) {
    return (
      <main className="mx-auto max-w-xl px-5 py-16">
        <p className="text-xs uppercase tracking-[0.25em] text-gold">Reserved</p>
        <h1 className="font-display mt-3 text-5xl">Request received</h1>
        <p className="mt-4 text-muted">
          Your appointment is pending confirmation. Save this reference.
        </p>
        <div className="mt-8 border border-fog p-6">
          <p className="text-sm text-muted">Reference</p>
          <p className="mt-1 font-medium">{booking.reference}</p>
          <p className="mt-4">{booking.serviceName}</p>
          <p className="text-muted">{booking.salonistName}</p>
          <p className="mt-2">{formatDateTime(booking.startAt)}</p>
          <p className="mt-4 text-sm uppercase tracking-widest text-gold">{booking.status}</p>
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

        <StatusMessage error={error} />
        <button
          type="submit"
          disabled={submitting}
          className="rounded-full bg-ink px-6 py-3 text-sm text-paper hover:bg-clay disabled:opacity-60"
        >
          {submitting ? 'Sending…' : 'Request appointment'}
        </button>
      </form>
    </main>
  )
}
