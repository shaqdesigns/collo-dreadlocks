import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getSalonistsForService, getService, getServicePhotos } from '../api/client'
import { StatusMessage } from '../components/StatusMessage'
import { formatCategory, formatDuration, formatPrice } from '../lib/format'
import type { Salonist, Service, ServicePhoto } from '../types/catalog'

export function ServiceDetailPage() {
  const { id } = useParams()
  const serviceId = Number(id)
  const [service, setService] = useState<Service | null>(null)
  const [photos, setPhotos] = useState<ServicePhoto[]>([])
  const [salonists, setSalonists] = useState<Salonist[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!serviceId) {
      setError('Service not found.')
      return
    }
    Promise.all([getService(serviceId), getSalonistsForService(serviceId), getServicePhotos(serviceId)])
      .then(([nextService, nextSalonists, nextPhotos]) => {
        setService(nextService)
        setSalonists(nextSalonists)
        setPhotos(nextPhotos)
      })
      .catch(() => setError('This service is not available.'))
  }, [serviceId])

  if (error || !service) {
    return (
      <main className="mx-auto max-w-3xl px-5 py-16">
        <StatusMessage error={error} loading={!error && !service} />
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-3xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">
        {formatCategory(service.category)}
      </p>
      <h1 className="font-display mt-3 text-5xl">{service.name}</h1>
      {photos.length > 0 && (
        <div className="mt-8 grid gap-3 sm:grid-cols-2">
          {photos.map((photo) => (
            <img key={photo.id} src={photo.imageUrl} alt="" className="aspect-[4/3] w-full object-cover" />
          ))}
        </div>
      )}
      <p className="mt-6 text-lg text-muted">{service.description}</p>
      <div className="mt-8 flex gap-8 border-y border-fog py-6 text-sm">
        <div>
          <p className="text-muted">From</p>
          <p className="text-xl font-medium">{formatPrice(service.price)}</p>
        </div>
        <div>
          <p className="text-muted">Duration</p>
          <p className="text-xl font-medium">{formatDuration(service.durationMinutes)}</p>
        </div>
      </div>
      <Link
        to={`/book?serviceId=${service.id}`}
        className="mt-8 inline-flex rounded-full bg-ink px-6 py-3 text-sm text-paper hover:bg-clay"
      >
        Book this service
      </Link>
      <h2 className="font-display mt-14 text-3xl">Who can do this</h2>
      <ul className="mt-4 space-y-3">
        {salonists.map((salonist) => (
          <li key={salonist.id}>
            <Link to={`/salonists/${salonist.id}`} className="text-gold hover:text-clay">
              {salonist.name}
            </Link>
          </li>
        ))}
        {salonists.length === 0 && (
          <li className="text-sm text-muted">No stylist is assigned yet. Choose “any available” when booking.</li>
        )}
      </ul>
    </main>
  )
}
