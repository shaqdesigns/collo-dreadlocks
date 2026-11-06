import { useEffect, useState } from 'react'
import { getSalonists } from '../api/client'
import { SalonistCard } from '../components/SalonistCard'
import { StatusMessage } from '../components/StatusMessage'
import type { Salonist } from '../types/catalog'

export function SalonistsPage() {
  const [salonists, setSalonists] = useState<Salonist[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getSalonists()
      .then(setSalonists)
      .catch(() => setError('Could not load salonists.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="mx-auto max-w-6xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">The studio</p>
      <h1 className="font-display mt-3 text-5xl">Salonists</h1>
      <p className="mt-4 max-w-2xl text-muted">
        Choose a preferred stylist, or let the booking calendar offer whoever is free for your service.
      </p>
      <div className="mt-8">
        <StatusMessage loading={loading} error={error} />
      </div>
      <div className="mt-8 grid gap-4 md:grid-cols-3">
        {salonists.map((salonist) => (
          <SalonistCard key={salonist.id} salonist={salonist} />
        ))}
      </div>
    </main>
  )
}
