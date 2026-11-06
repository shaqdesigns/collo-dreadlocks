import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { getClientBirthdays, getClients } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AdminNav } from '../components/AdminNav'
import { formatDateTime, formatPrice } from '../lib/format'
import type { ClientSummary, Customer } from '../types/catalog'

export function AdminClientsPage() {
  const { session } = useAuth()
  const [query, setQuery] = useState('')
  const [clients, setClients] = useState<ClientSummary[]>([])
  const [birthdays, setBirthdays] = useState<Customer[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (session?.role !== 'ADMIN') {
      return
    }
    getClientBirthdays()
      .then(setBirthdays)
      .catch(() => setBirthdays([]))
  }, [session])

  useEffect(() => {
    if (session?.role !== 'ADMIN') {
      return
    }
    const handle = window.setTimeout(() => {
      getClients(query)
        .then(setClients)
        .catch(() => setError('Could not load clients.'))
    }, 250)
    return () => window.clearTimeout(handle)
  }, [session, query])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  return (
    <main className="mx-auto max-w-5xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Clients</p>
      <h1 className="font-display mt-3 text-5xl">CRM</h1>
      <p className="mt-3 max-w-2xl text-sm text-muted">
        Everyone who has booked or registered. Open a client for their visits, remarks, and studio notes.
      </p>
      <AdminNav />
      {birthdays.length > 0 && (
        <p className="mt-8 text-sm text-muted">
          Birthdays in the next two weeks:{' '}
          {birthdays.map((client) => client.name).join(', ')}
        </p>
      )}
      <label className="mt-8 block text-sm">
        <span className="text-muted">Search name, phone, or email</span>
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        />
      </label>
      {error && <p className="mt-6 text-sm text-clay">{error}</p>}
      <div className="mt-6 space-y-4">
        {clients.map((client) => (
          <Link key={client.id} to={`/admin/clients/${client.id}`} className="block border border-fog p-5 hover:border-gold">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <h2 className="font-display text-3xl">{client.name}</h2>
                <p className="mt-1 text-sm text-muted">
                  {client.phone}
                  {client.email ? ` · ${client.email}` : ''}
                </p>
              </div>
              <p className="text-xs uppercase tracking-widest text-gold">{client.active ? 'Active' : 'Inactive'}</p>
            </div>
            <p className="mt-4 text-sm">
              {client.visits} visits · {formatPrice(client.spent)} taken
              {client.nextVisitAt ? ` · next ${formatDateTime(client.nextVisitAt)}` : ''}
              {client.lastVisitAt ? ` · last completed ${formatDateTime(client.lastVisitAt)}` : ''}
            </p>
          </Link>
        ))}
        {clients.length === 0 && !error && <p className="text-sm text-muted">No clients match that search.</p>}
      </div>
    </main>
  )
}
