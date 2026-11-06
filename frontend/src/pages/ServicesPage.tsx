import { useEffect, useMemo, useState } from 'react'
import { getServices } from '../api/client'
import { ServiceCard } from '../components/ServiceCard'
import { StatusMessage } from '../components/StatusMessage'
import { formatCategory } from '../lib/format'
import type { Service } from '../types/catalog'

export function ServicesPage() {
  const [services, setServices] = useState<Service[]>([])
  const [category, setCategory] = useState('ALL')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getServices()
      .then(setServices)
      .catch(() => setError('Could not load services.'))
      .finally(() => setLoading(false))
  }, [])

  const categories = useMemo(() => {
    const preferred = ['DREADLOCKS', 'HAIR', 'BEAUTY', 'NAILS']
    const present = Array.from(new Set(services.map((service) => service.category)))
    return [
      'ALL',
      ...preferred.filter((item) => present.includes(item)),
      ...present.filter((item) => !preferred.includes(item)),
    ]
  }, [services])

  const visible =
    category === 'ALL'
      ? services
      : services.filter((service) => service.category === category)

  return (
    <main className="mx-auto max-w-6xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Catalogue</p>
      <h1 className="font-display mt-3 text-5xl">Services</h1>
      <p className="mt-4 max-w-2xl text-muted">
        Dreadlock installation and retouch first. Hair, beauty and nails when you need the rest.
      </p>
      <div className="mt-8 flex flex-wrap gap-2">
        {categories.map((item) => (
          <button
            key={item}
            type="button"
            onClick={() => setCategory(item)}
            className={`rounded-full px-4 py-2 text-sm ${
              category === item ? 'bg-ink text-paper' : 'border border-fog text-muted'
            }`}
          >
            {item === 'ALL' ? 'All' : formatCategory(item)}
          </button>
        ))}
      </div>
      <div className="mt-8">
        <StatusMessage loading={loading} error={error} />
      </div>
      <div className="mt-8 grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {visible.map((service) => (
          <ServiceCard key={service.id} service={service} />
        ))}
      </div>
    </main>
  )
}
