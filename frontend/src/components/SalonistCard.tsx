import { Link } from 'react-router-dom'
import type { Salonist } from '../types/catalog'

export function SalonistCard({ salonist }: { salonist: Salonist }) {
  const initials = salonist.name
    .split(' ')
    .slice(0, 2)
    .map((part) => part[0])
    .join('')

  return (
    <Link
      to={`/salonists/${salonist.id}`}
      className="border border-fog bg-paper p-6 transition-colors hover:border-gold"
    >
      <div className="flex h-16 w-16 items-center justify-center overflow-hidden rounded-full bg-ink font-display text-2xl text-gold">
        {salonist.profileImageUrl ? (
          <img src={salonist.profileImageUrl} alt="" className="h-full w-full object-cover" />
        ) : (
          initials
        )}
      </div>
      <h3 className="font-display mt-5 text-2xl">{salonist.name}</h3>
      <p className="mt-3 line-clamp-3 text-sm text-muted">
        {salonist.bio ?? 'Collo Dreadlocks stylist.'}
      </p>
    </Link>
  )
}
