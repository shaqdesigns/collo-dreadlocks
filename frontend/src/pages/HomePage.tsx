import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getSalonists, getServices } from '../api/client'
import { SalonistCard } from '../components/SalonistCard'
import { ServiceCard } from '../components/ServiceCard'
import { StatusMessage } from '../components/StatusMessage'
import type { Salonist, Service } from '../types/catalog'

export function HomePage() {
  const [services, setServices] = useState<Service[]>([])
  const [salonists, setSalonists] = useState<Salonist[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([getServices(), getSalonists()])
      .then(([nextServices, nextSalonists]) => {
        const featured = ['dreadlock-installation', 'dreadlock-retouch', 'dreadlock-styling', 'dreadlock-repair']
        const rank = (slug: string) => {
          const index = featured.indexOf(slug)
          return index === -1 ? featured.length : index
        }
        const locServices = nextServices.filter((service) => service.category === 'DREADLOCKS')
        setServices([...locServices].sort((a, b) => rank(a.slug) - rank(b.slug)).slice(0, 4))
        setSalonists(nextSalonists.slice(0, 3))
      })
      .catch(() => setError('Could not reach the salon catalogue.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main>
      <section className="bg-ink text-paper">
        <div className="mx-auto grid max-w-6xl gap-12 px-5 py-20 md:grid-cols-[1.2fr_0.8fr] md:items-end md:py-28">
          <div>
            <p className="text-xs uppercase tracking-[0.35em] text-gold">Kisii · Kenya</p>
            <h1 className="font-display mt-6 text-5xl leading-[0.95] md:text-7xl">
              Locs, done properly.
            </h1>
            <p className="mt-6 max-w-xl text-lg text-fog">
              Professional dreadlock installation, retouching and maintenance, with hair, beauty and nail care in the same chair.
            </p>
            <div className="mt-10 flex flex-wrap gap-4">
              <Link
                to="/book"
                className="rounded-full bg-gold px-6 py-3 text-sm font-medium text-ink hover:bg-paper"
              >
                Book an appointment
              </Link>
              <Link
                to="/services"
                className="rounded-full border border-gold/50 px-6 py-3 text-sm text-paper hover:border-gold"
              >
                View services
              </Link>
            </div>
          </div>
          <div className="border border-gold/30 p-8">
            <p className="font-display text-3xl italic text-gold">Studio hours</p>
            <p className="mt-4 text-fog">Monday – Saturday</p>
            <p className="text-2xl">09:00 – 17:00</p>
            <p className="mt-6 text-sm text-fog">Sunday by request, after admin approval of stylist hours.</p>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-5 py-20">
        <div className="flex items-end justify-between gap-6">
          <div>
            <p className="text-xs uppercase tracking-[0.25em] text-gold">The work</p>
            <h2 className="font-display mt-3 text-4xl">Dreadlock services</h2>
          </div>
          <Link to="/services" className="hidden text-sm text-muted hover:text-ink md:block">
            Full catalogue →
          </Link>
        </div>
        <div className="mt-8">
          <StatusMessage loading={loading} error={error} />
        </div>
        <div className="mt-8 grid gap-4 md:grid-cols-2">
          {services.map((service) => (
            <ServiceCard key={service.id} service={service} />
          ))}
        </div>
      </section>

      <section className="border-y border-fog bg-fog/40">
        <div className="mx-auto max-w-6xl px-5 py-20">
          <p className="text-xs uppercase tracking-[0.25em] text-gold">The studio</p>
          <h2 className="font-display mt-3 text-4xl">Stylists</h2>
          <div className="mt-8 grid gap-4 md:grid-cols-3">
            {salonists.map((salonist) => (
              <SalonistCard key={salonist.id} salonist={salonist} />
            ))}
          </div>
        </div>
      </section>
    </main>
  )
}
