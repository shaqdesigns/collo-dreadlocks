import { type FormEvent, useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { getAdminSalonists, getRevenueReport, getServices, recordManualWork } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AdminNav } from '../components/AdminNav'
import { apiError } from '../lib/apiError'
import { formatDateTime, formatPrice } from '../lib/format'
import type { AdminSalonist, RevenueReport, Service } from '../types/catalog'

function nowLocal(): string {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}T${hour}:${minute}`
}

export function AdminRevenuePage() {
  const { session } = useAuth()
  const [report, setReport] = useState<RevenueReport | null>(null)
  const [salonists, setSalonists] = useState<AdminSalonist[]>([])
  const [services, setServices] = useState<Service[]>([])
  const [error, setError] = useState<string | null>(null)
  const [salonistId, setSalonistId] = useState(0)
  const [serviceId, setServiceId] = useState(0)
  const [price, setPrice] = useState('')
  const [workedAt, setWorkedAt] = useState(nowLocal())
  const [customerName, setCustomerName] = useState('')
  const [note, setNote] = useState('')
  const [saving, setSaving] = useState(false)

  async function load() {
    const [nextReport, nextSalonists, nextServices] = await Promise.all([
      getRevenueReport(),
      getAdminSalonists(),
      getServices(),
    ])
    setReport(nextReport)
    setSalonists(nextSalonists)
    setServices(nextServices)
    setSalonistId((current) => current || nextSalonists[0]?.id || 0)
    setServiceId((current) => current || nextServices[0]?.id || 0)
  }

  useEffect(() => {
    if (session?.role !== 'ADMIN') {
      return
    }
    load().catch(() => setError('Could not load revenue.'))
  }, [session])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await recordManualWork({
        salonistId,
        serviceId,
        price: Number(price),
        workedAt: workedAt.length === 16 ? `${workedAt}:00` : workedAt,
        customerName: customerName.trim() || null,
        note: note.trim() || null,
      })
      setPrice('')
      setCustomerName('')
      setNote('')
      await load()
    } catch (cause) {
      setError(apiError(cause, 'Could not add that work.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="mx-auto max-w-5xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Admin</p>
      <h1 className="font-display mt-3 text-5xl">Revenue</h1>
      <p className="mt-3 max-w-2xl text-sm text-muted">
        Completed bookings and walk-in work recorded here. Each salonist is paid 40% of what the client paid.
      </p>
      <AdminNav />
      {error && <p className="mt-6 text-sm text-clay">{error}</p>}
      {report && (
        <>
          <div className="mt-8 grid gap-4 sm:grid-cols-3">
            <figure className="border border-fog p-5">
              <figcaption className="text-xs uppercase tracking-[0.2em] text-muted">Taken</figcaption>
              <p className="font-display mt-2 text-4xl">{formatPrice(report.totalRevenue)}</p>
              <p className="mt-2 text-sm text-muted">
                Bookings {formatPrice(report.bookingRevenue)} · Walk-ins {formatPrice(report.manualRevenue)}
              </p>
            </figure>
            <figure className="border border-fog p-5">
              <figcaption className="text-xs uppercase tracking-[0.2em] text-muted">Salonist pay, 40%</figcaption>
              <p className="font-display mt-2 text-4xl">{formatPrice(report.salonistPayout)}</p>
            </figure>
            <figure className="border border-fog p-5">
              <figcaption className="text-xs uppercase tracking-[0.2em] text-muted">Studio keeps</figcaption>
              <p className="font-display mt-2 text-4xl">{formatPrice(report.studioShare)}</p>
            </figure>
          </div>
          <div className="mt-6 grid gap-4 sm:grid-cols-2">
            <p className="border border-fog p-5 text-sm">
              Most sought: <span className="font-medium">{report.mostSought?.serviceName ?? 'None yet'}</span>
              {report.mostSought ? ` · ${report.mostSought.soughtCount} visits` : ''}
            </p>
            <p className="border border-fog p-5 text-sm">
              Highest revenue: <span className="font-medium">{report.highestRevenue?.serviceName ?? 'None yet'}</span>
              {report.highestRevenue ? ` · ${formatPrice(report.highestRevenue.revenue)}` : ''}
            </p>
          </div>
          <h2 className="font-display mt-12 text-3xl">By salonist</h2>
          <div className="mt-4 space-y-3">
            {report.salonists.map((row) => (
              <p key={row.salonistId} className="flex flex-wrap justify-between gap-2 border-b border-fog py-3 text-sm">
                <span>{row.salonistName}</span>
                <span>
                  {formatPrice(row.revenue)} taken · pay {formatPrice(row.payout)} · {row.completedBookings} completed · {row.manualWorks} walk-ins
                </span>
              </p>
            ))}
            {report.salonists.length === 0 && <p className="text-sm text-muted">No completed work yet.</p>}
          </div>
          <h2 className="font-display mt-12 text-3xl">By service</h2>
          <div className="mt-4 space-y-3">
            {report.services.map((row) => (
              <p key={row.serviceId} className="flex flex-wrap justify-between gap-2 border-b border-fog py-3 text-sm">
                <span>{row.serviceName}</span>
                <span>{row.soughtCount} sought · {formatPrice(row.revenue)}</span>
              </p>
            ))}
          </div>
        </>
      )}
      <h2 className="font-display mt-12 text-3xl">Add walk-in work</h2>
      <form className="mt-4 grid gap-4 sm:grid-cols-2" onSubmit={onSubmit}>
        <label className="text-sm">
          <span className="text-muted">Salonist</span>
          <select value={salonistId} onChange={(event) => setSalonistId(Number(event.target.value))} className="mt-2 w-full border border-fog bg-paper px-3 py-2">
            {salonists.map((salonist) => (
              <option key={salonist.id} value={salonist.id}>{salonist.name}</option>
            ))}
          </select>
        </label>
        <label className="text-sm">
          <span className="text-muted">Service</span>
          <select
            value={serviceId}
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
        <label className="text-sm">
          <span className="text-muted">Amount paid</span>
          <input required type="number" min="1" step="1" value={price} onChange={(event) => setPrice(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
        </label>
        <label className="text-sm">
          <span className="text-muted">When</span>
          <input required type="datetime-local" value={workedAt} onChange={(event) => setWorkedAt(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
        </label>
        <label className="text-sm">
          <span className="text-muted">Client name</span>
          <input value={customerName} onChange={(event) => setCustomerName(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
        </label>
        <label className="text-sm">
          <span className="text-muted">Note</span>
          <input value={note} onChange={(event) => setNote(event.target.value)} className="mt-2 w-full border border-fog bg-paper px-3 py-2" />
        </label>
        <div className="sm:col-span-2">
          <button type="submit" disabled={saving} className="rounded-full bg-ink px-5 py-2 text-sm text-paper disabled:opacity-60">
            {saving ? 'Saving…' : 'Add work'}
          </button>
        </div>
      </form>
      {report && report.manualWorks.length > 0 && (
        <div className="mt-8 space-y-2">
          {report.manualWorks.map((work) => (
            <p key={work.id} className="text-sm text-muted">
              {work.salonistName} · {work.serviceName} · {formatPrice(work.price)} · {formatDateTime(work.workedAt)}
              {work.customerName ? ` · ${work.customerName}` : ''}
            </p>
          ))}
        </div>
      )}
    </main>
  )
}
