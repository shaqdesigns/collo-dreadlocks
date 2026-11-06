import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { homeForRole } from '../auth/session'
import { NotificationBell } from './NotificationBell'

const links = [
  { to: '/', label: 'Home' },
  { to: '/services', label: 'Services' },
  { to: '/salonists', label: 'Salonists' },
  { to: '/about', label: 'About' },
  { to: '/contact', label: 'Contact' },
]

export function Layout() {
  const { session, signOut } = useAuth()
  const navigate = useNavigate()

  function logout() {
    signOut()
    navigate('/')
  }

  return (
    <div className="min-h-svh bg-paper text-ink">
      <header className="border-b border-fog/80 bg-paper/90 backdrop-blur-sm sticky top-0 z-20">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-6 px-5 py-4">
          <NavLink to="/" className="font-display text-2xl tracking-tight text-ink">
            Collo <span className="text-gold">Dreadlocks</span>
          </NavLink>
          <nav className="hidden items-center gap-7 text-sm font-medium text-muted md:flex">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.to === '/'}
                className={({ isActive }) =>
                  isActive ? 'text-ink' : 'hover:text-ink transition-colors'
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
          <div className="flex items-center gap-3">
            {session ? (
              <>
                <NavLink to={homeForRole(session.role)} className="hidden text-sm text-muted hover:text-ink sm:inline">
                  {session.role === 'ADMIN' ? 'Admin' : session.role === 'SALONIST' ? 'My chair' : 'My bookings'}
                </NavLink>
                {session && <NotificationBell role={session.role} />}
                <button type="button" onClick={logout} className="text-sm text-muted hover:text-ink">
                  Sign out
                </button>
              </>
            ) : (
              <NavLink to="/login" className="text-sm font-medium text-ink hover:text-clay">
                Sign in
              </NavLink>
            )}
            <NavLink
              to="/book"
              className="rounded-full bg-ink px-4 py-2 text-sm font-medium text-paper hover:bg-clay transition-colors"
            >
              Book now
            </NavLink>
          </div>
        </div>
        <nav className="flex gap-4 overflow-x-auto border-t border-fog px-5 py-3 text-sm text-muted md:hidden">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === '/'}
              className={({ isActive }) => (isActive ? 'text-ink' : '')}
            >
              {link.label}
            </NavLink>
          ))}
          {!session && <NavLink to="/login">Sign in</NavLink>}
        </nav>
      </header>
      <Outlet />
      <footer className="border-t border-fog bg-ink text-paper">
        <div className="mx-auto flex max-w-6xl flex-col gap-6 px-5 py-12 md:flex-row md:justify-between">
          <div>
            <p className="font-display text-3xl">Collo Dreadlocks Kisii</p>
            <p className="mt-2 max-w-sm text-sm text-fog">
              Installation, retouch and care. Book online, walk in by appointment.
            </p>
          </div>
          <div className="text-sm text-fog">
            <p>Mon–Sat · 9:00 – 17:00</p>
            <p className="mt-1">Kisii, Kenya</p>
            <NavLink to="/book" className="mt-4 inline-block text-gold">
              Reserve a chair →
            </NavLink>
          </div>
        </div>
      </footer>
    </div>
  )
}
