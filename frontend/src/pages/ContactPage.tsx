import { Link } from 'react-router-dom'

export function ContactPage() {
  return (
    <main className="mx-auto max-w-3xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Visit</p>
      <h1 className="font-display mt-3 text-5xl">Contact</h1>
      <div className="mt-10 grid gap-8 border border-fog p-8 md:grid-cols-2">
        <div>
          <p className="text-sm text-muted">Hours</p>
          <p className="mt-2 text-lg">Monday – Saturday</p>
          <p>09:00 – 17:00</p>
        </div>
        <div>
          <p className="text-sm text-muted">Studio</p>
          <p className="mt-2 text-lg">Kisii, Kenya</p>
          <p>By appointment, with space for walk-ins when the chair is free.</p>
        </div>
      </div>
      <Link
        to="/book"
        className="mt-8 inline-flex rounded-full bg-ink px-6 py-3 text-sm text-paper hover:bg-clay"
      >
        Book online
      </Link>
    </main>
  )
}
