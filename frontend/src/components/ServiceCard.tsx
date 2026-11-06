import { Link } from 'react-router-dom'
import { formatCategory, formatDuration, formatPrice } from '../lib/format'
import type { Service } from '../types/catalog'

export function ServiceCard({ service }: { service: Service }) {
  return (
    <Link
      to={`/services/${service.id}`}
      className="group flex flex-col overflow-hidden border border-fog bg-paper transition-colors hover:border-gold"
    >
      <div className="aspect-[4/3] overflow-hidden bg-fog">
        {service.imageUrl ? (
          <img src={service.imageUrl} alt="" className="h-full w-full object-cover" />
        ) : (
          <div className="flex h-full items-end p-6 font-display text-3xl text-ink/30">{service.name}</div>
        )}
      </div>
      <div className="flex flex-1 flex-col p-6">
        <p className="text-xs uppercase tracking-[0.2em] text-gold">
          {formatCategory(service.category)}
        </p>
        <h3 className="font-display mt-3 text-2xl leading-tight group-hover:text-clay">
          {service.name}
        </h3>
        <p className="mt-3 line-clamp-3 flex-1 text-sm text-muted">
          {service.description}
        </p>
        <div className="mt-6 flex items-center justify-between text-sm">
          <span className="font-medium">{formatPrice(service.price)}</span>
          <span className="text-muted">{formatDuration(service.durationMinutes)}</span>
        </div>
      </div>
    </Link>
  )
}
