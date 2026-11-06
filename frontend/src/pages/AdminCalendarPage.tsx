import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { confirmBooking, getCalendar } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AdminNav } from '../components/AdminNav'
import { apiError } from '../lib/apiError'
import type { CalendarBlock, CalendarDay } from '../types/catalog'

const START_MINUTES = 8 * 60
const END_MINUTES = 18 * 60
const STEP = 30

function todayIso(): string {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function shiftDate(value: string, days: number): string {
  const [year, month, day] = value.split('-').map(Number)
  const next = new Date(year, month - 1, day + days)
  const nextMonth = String(next.getMonth() + 1).padStart(2, '0')
  const nextDay = String(next.getDate()).padStart(2, '0')
  return `${next.getFullYear()}-${nextMonth}-${nextDay}`
}

function labelTime(minutes: number): string {
  const hour = Math.floor(minutes / 60)
  const minute = minutes % 60
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}

function cellBounds(date: string, minutes: number): [number, number] {
  const [year, month, day] = date.split('-').map(Number)
  const start = new Date(year, month - 1, day, Math.floor(minutes / 60), minutes % 60, 0, 0)
  return [start.getTime(), start.getTime() + STEP * 60_000]
}

function covers(block: CalendarBlock, start: number, end: number): boolean {
  return new Date(block.startAt).getTime() < end && start < new Date(block.endAt).getTime()
}

function startsHere(block: CalendarBlock, start: number, end: number): boolean {
  const blockStart = new Date(block.startAt).getTime()
  return blockStart >= start && blockStart < end
}

export function AdminCalendarPage() {
  const { session } = useAuth()
  const [date, setDate] = useState(todayIso())
  const [day, setDay] = useState<CalendarDay | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (session?.role !== 'ADMIN') {
      return
    }
    getCalendar(date)
      .then(setDay)
      .catch(() => setError('Could not load the calendar.'))
  }, [session, date])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  const rows = []
  for (let minutes = START_MINUTES; minutes < END_MINUTES; minutes += STEP) {
    rows.push(minutes)
  }

  async function confirm(id: number) {
    setError(null)
    try {
      await confirmBooking(id)
      setDay(await getCalendar(date))
    } catch (cause) {
      setError(apiError(cause, 'Could not confirm that booking.'))
    }
  }

  return (
    <main className="mx-auto max-w-6xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Admin</p>
      <h1 className="font-display mt-3 text-5xl">Calendar</h1>
      <p className="mt-3 max-w-2xl text-sm text-muted">
        A booked or approved time-off block makes that salonist unavailable. Confirm a pending visit only when the chair is free.
      </p>
      <AdminNav />
      <div className="mt-8 flex items-center gap-4">
        <button type="button" onClick={() => setDate(shiftDate(date, -1))} className="rounded-full border border-fog px-4 py-2 text-sm">
          Previous day
        </button>
        <p className="font-display text-2xl">{date}</p>
        <button type="button" onClick={() => setDate(shiftDate(date, 1))} className="rounded-full border border-fog px-4 py-2 text-sm">
          Next day
        </button>
      </div>
      {error && <p className="mt-4 text-sm text-clay">{error}</p>}
      <div className="mt-6 flex flex-wrap gap-4 text-xs uppercase tracking-widest text-muted">
        <span>Pending can be confirmed</span>
        <span>Confirmed is booked</span>
        <span>Off is approved time away</span>
      </div>
      <div className="mt-6 overflow-x-auto">
        <div
          className="grid min-w-[720px] border border-fog text-sm"
          style={{ gridTemplateColumns: `72px repeat(${Math.max(day?.salonists.length ?? 1, 1)}, minmax(160px, 1fr))` }}
        >
          <div className="border-b border-r border-fog bg-paper p-2" />
          {(day?.salonists ?? []).map((salonist) => (
            <div key={salonist.salonistId} className="border-b border-r border-fog p-2">
              <p className="font-medium">{salonist.salonistName}</p>
              <p className="text-xs uppercase tracking-widest text-gold">{salonist.active ? 'Active' : 'Inactive'}</p>
            </div>
          ))}
          {rows.map((minutes) => (
            <div key={minutes} className="contents">
              <div className="border-b border-r border-fog p-2 text-xs text-muted">{labelTime(minutes)}</div>
              {(day?.salonists ?? []).map((salonist) => {
                const [start, end] = cellBounds(day?.date ?? date, minutes)
                const blocks = salonist.blocks.filter((block) => covers(block, start, end))
                const unavailable = blocks.length > 0
                return (
                  <div
                    key={`${salonist.salonistId}-${minutes}`}
                    className={`min-h-12 border-b border-r border-fog p-1 ${unavailable ? 'bg-fog/70' : 'bg-paper'}`}
                  >
                    {blocks.filter((block) => startsHere(block, start, end)).map((block) => (
                      <div key={`${block.kind}-${block.bookingId ?? block.startAt}`} className="text-xs">
                        <p className={block.kind === 'OFF' ? 'text-muted' : 'text-ink'}>{block.label}</p>
                        <p className="uppercase tracking-widest text-gold">{block.kind === 'OFF' ? 'Off' : block.status}</p>
                        {block.kind === 'BOOKING' && block.status === 'PENDING' && block.bookingId && (
                          <button
                            type="button"
                            onClick={() => confirm(block.bookingId!)}
                            className="mt-1 rounded-full bg-ink px-2 py-1 text-[11px] text-paper"
                          >
                            Confirm
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                )
              })}
            </div>
          ))}
        </div>
      </div>
    </main>
  )
}
