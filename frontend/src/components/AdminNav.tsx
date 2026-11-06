import { NavLink } from 'react-router-dom'

const links = [
  { to: '/admin', label: 'Bookings', end: true },
  { to: '/admin/calendar', label: 'Calendar', end: false },
  { to: '/admin/salonists', label: 'Salonists', end: false },
  { to: '/admin/clients', label: 'Clients', end: false },
  { to: '/admin/services', label: 'Services', end: false },
  { to: '/admin/revenue', label: 'Revenue', end: false },
]

export function AdminNav() {
  return (
    <nav className="mt-8 flex flex-wrap gap-x-6 gap-y-2 text-sm">
      {links.map((link) => (
        <NavLink
          key={link.to}
          to={link.to}
          end={link.end}
          className={({ isActive }) =>
            isActive ? 'border-b border-gold pb-1 text-ink' : 'pb-1 text-muted hover:text-ink'
          }
        >
          {link.label}
        </NavLink>
      ))}
    </nav>
  )
}
