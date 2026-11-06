import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getSalonist, getServicesForSalonist } from '../api/client'
import { ServiceCard } from '../components/ServiceCard'
import { StatusMessage } from '../components/StatusMessage'
import type { Salonist, Service } from '../types/catalog'

export function SalonistDetailPage() {
  const { id } = useParams()
  const salonistId = Number(id)
  const [salonist, setSalonist] = useState<Salonist | null>(null)
  const [services, setServices] = useState<Service[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!salonistId) {
      setError('Salonist not found.')
      return
    }
    Promise.all([getSalonist(salonistId), getServicesForSalonist(salonistId)])
      .then(([nextSalonist, nextServices]) => {
        setSalonist(nextSalonist)
        setServices(nextServices)
      })
      .catch(() => setError('This salonist is not available.'))
  }, [salonistId])

  if (error || !salonist) {
    return (
      <main className="mx-auto max-w-3xl px-5 py-16">
        <StatusMessage error={error} loading={!error && !salonist} />
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-6xl px-5 py-16">
      <div className="mt-6 flex items-center gap-5">
        <div className="flex h-20 w-20 items-center justify-center overflow-hidden rounded-full bg-ink font-display text-3xl text-gold">
          {salonist.profileImageUrl ? (
            <img src={salonist.profileImageUrl} alt="" className="h-full w-full object-cover" />
          ) : (
            salonist.name.slice(0, 1)
          )}
        </div>
        <div>
          <p className="text-xs uppercase tracking-[0.25em] text-gold">Salonist</p>
          <h1 className="font-display mt-2 text-5xl">{salonist.name}</h1>
        </div>
      </div>
      <p className="mt-6 max-w-2xl text-lg text-muted">
        {salonist.bio ?? 'Collo Dreadlocks stylist in Kisii.'}
      </p>
      <Link
        to={`/book?salonistId=${salonist.id}`}
        className="mt-8 inline-flex rounded-full bg-ink px-6 py-3 text-sm text-paper hover:bg-clay"
      >
        Book with {salonist.name.split(' ')[0]}
      </Link>
      <h2 className="font-display mt-14 text-3xl">Services offered</h2>
      <div className="mt-6 grid gap-4 md:grid-cols-2">
        {services.map((service) => (
          <ServiceCard key={service.id} service={service} />
        ))}
      </div>
    </main>
  )
}
