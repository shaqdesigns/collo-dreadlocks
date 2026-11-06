import { useEffect, useState } from 'react'
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '../api/client'
import { formatDateTime } from '../lib/format'
import type { Role } from '../types/auth'
import type { StudioNotification } from '../types/catalog'

const titles: Record<Role, string> = {
  ADMIN: 'New bookings',
  SALONIST: 'Your chair',
  CUSTOMER: 'Your visits',
}

const emailNotes: Record<Role, string> = {
  ADMIN: 'The studio, the salonist, and the client each receive an email for the alerts that concern them, once studio email is set up.',
  SALONIST: 'You receive an email when a client requests you and when the studio confirms the visit, once studio email is set up.',
  CUSTOMER: 'You receive an email when your request is received and when the studio confirms your visit, once studio email is set up.',
}

export function NotificationBell({ role }: { role: Role }) {
  const [open, setOpen] = useState(false)
  const [items, setItems] = useState<StudioNotification[]>([])

  useEffect(() => {
    let active = true
    const load = () => {
      getNotifications(role)
        .then((next) => {
          if (active) {
            setItems(next)
          }
        })
        .catch(() => {
          if (active) {
            setItems([])
          }
        })
    }
    load()
    const timer = window.setInterval(load, 20000)
    return () => {
      active = false
      window.clearInterval(timer)
    }
  }, [role])

  const unread = items.filter((item) => !item.readAt).length

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        className="relative text-sm text-ink hover:text-clay"
      >
        Notifications
        {unread > 0 && (
          <span className="ml-1 inline-flex min-w-5 justify-center rounded-full bg-clay px-1.5 text-xs text-paper">
            {unread}
          </span>
        )}
      </button>
      {open && (
        <div className="absolute right-0 z-30 mt-3 w-80 border border-fog bg-paper p-4 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <p className="text-xs uppercase tracking-[0.2em] text-gold">{titles[role]}</p>
            {unread > 0 && (
              <button
                type="button"
                className="text-xs underline decoration-gold"
                onClick={async () => {
                  await markAllNotificationsRead(role)
                  setItems((current) => current.map((item) => ({ ...item, readAt: item.readAt ?? new Date().toISOString() })))
                }}
              >
                Mark all read
              </button>
            )}
          </div>
          <p className="mt-2 text-xs text-muted">{emailNotes[role]}</p>
          <div className="mt-3 max-h-80 space-y-3 overflow-y-auto">
            {items.map((item) => (
              <button
                key={item.id}
                type="button"
                className={`block w-full text-left text-sm ${item.readAt ? 'text-muted' : 'text-ink'}`}
                onClick={async () => {
                  if (!item.readAt) {
                    await markNotificationRead(role, item.id)
                    setItems((current) =>
                      current.map((entry) =>
                        entry.id === item.id ? { ...entry, readAt: new Date().toISOString() } : entry,
                      ),
                    )
                  }
                }}
              >
                <span className="block">{item.message}</span>
                <span className="mt-1 block text-xs text-muted">{formatDateTime(item.createdAt)}</span>
              </button>
            ))}
            {items.length === 0 && <p className="text-sm text-muted">No notifications yet.</p>}
          </div>
        </div>
      )}
    </div>
  )
}
