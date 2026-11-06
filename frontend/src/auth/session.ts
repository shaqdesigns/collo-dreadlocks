import type { Session } from '../types/auth'

const KEY = 'collo-session'

export function readSession(): Session | null {
  const raw = sessionStorage.getItem(KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as Session
  } catch {
    sessionStorage.removeItem(KEY)
    return null
  }
}

export function writeSession(session: Session) {
  sessionStorage.setItem(KEY, JSON.stringify(session))
}

export function clearSession() {
  sessionStorage.removeItem(KEY)
}

export function homeForRole(role: Session['role']): string {
  if (role === 'ADMIN') {
    return '/admin'
  }
  if (role === 'SALONIST') {
    return '/salonist'
  }
  return '/me'
}
