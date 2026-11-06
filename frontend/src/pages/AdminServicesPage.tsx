import { type FormEvent, useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import {
  createService,
  deleteService,
  getServicePhotos,
  getServices,
  updateService,
  uploadServicePhoto,
  type ServiceWrite,
} from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { AdminNav } from '../components/AdminNav'
import { apiError } from '../lib/apiError'
import { formatPrice } from '../lib/format'
import type { Service, ServicePhoto } from '../types/catalog'

const CATEGORIES = ['DREADLOCKS', 'HAIR', 'NAILS', 'BEAUTY', 'TATTOO', 'PIERCING']

export function AdminServicesPage() {
  const { session } = useAuth()
  const [services, setServices] = useState<Service[]>([])
  const [photos, setPhotos] = useState<Record<number, ServicePhoto[]>>({})
  const [error, setError] = useState<string | null>(null)
  const [showCreate, setShowCreate] = useState(false)

  useEffect(() => {
    if (session?.role !== 'ADMIN') {
      return
    }
    getServices()
      .then(async (next) => {
        setServices(next)
        const entries = await Promise.all(
          next.map(async (service) => [service.id, await getServicePhotos(service.id)] as const),
        )
        setPhotos(Object.fromEntries(entries))
      })
      .catch(() => setError('Could not load services.'))
  }, [session])

  if (!session) {
    return <Navigate to="/login" replace />
  }
  if (session.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  return (
    <main className="mx-auto max-w-5xl px-5 py-16">
      <p className="text-xs uppercase tracking-[0.25em] text-gold">Admin</p>
      <h1 className="font-display mt-3 text-5xl">Services</h1>
      <p className="mt-3 max-w-2xl text-sm text-muted">
        Change a price, add a service, or remove one that has never been booked. Photos uploaded here appear on every portal.
      </p>
      <AdminNav />
      <div className="mt-8">
        <button
          type="button"
          onClick={() => setShowCreate((current) => !current)}
          className="rounded-full bg-ink px-5 py-2 text-sm text-paper"
        >
          Add service
        </button>
        {showCreate && (
          <ServiceForm
            submitLabel="Save service"
            initial={{
              name: '',
              category: 'DREADLOCKS',
              description: '',
              price: '',
              durationMinutes: '60',
            }}
            onSubmit={async (payload) => {
              const created = await createService(payload)
              setServices((current) => [...current, created].sort((a, b) => a.name.localeCompare(b.name)))
              setPhotos((current) => ({ ...current, [created.id]: [] }))
              setShowCreate(false)
            }}
          />
        )}
      </div>
      {error && <p className="mt-6 text-sm text-clay">{error}</p>}
      <div className="mt-8 space-y-6">
        {services.map((service) => (
          <article key={service.id} className="border border-fog p-5">
            <ServiceForm
              submitLabel="Save changes"
              initial={{
                name: service.name,
                category: service.category,
                description: service.description ?? '',
                price: String(service.price),
                durationMinutes: String(service.durationMinutes),
              }}
              onSubmit={async (payload) => {
                const saved = await updateService(service.id, payload)
                setServices((current) =>
                  current.map((item) => (item.id === saved.id ? { ...item, ...saved } : item)),
                )
              }}
            />
            <div className="mt-4 flex flex-wrap items-center justify-between gap-4">
              <p className="text-sm text-muted">Current price {formatPrice(service.price)}</p>
              <button
                type="button"
                className="rounded-full border border-clay px-4 py-2 text-sm text-clay"
                onClick={async () => {
                  if (!window.confirm(`Delete ${service.name}?`)) {
                    return
                  }
                  try {
                    await deleteService(service.id)
                    setServices((current) => current.filter((item) => item.id !== service.id))
                  } catch (cause) {
                    setError(apiError(cause, 'Could not delete that service.'))
                  }
                }}
              >
                Delete service
              </button>
            </div>
            <label className="mt-4 block text-sm">
              <span className="text-muted">Upload a photo</span>
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                className="mt-2 block text-sm"
                onChange={async (event) => {
                  const file = event.target.files?.[0]
                  event.target.value = ''
                  if (!file) {
                    return
                  }
                  try {
                    const photo = await uploadServicePhoto(service.id, file)
                    setPhotos((current) => ({
                      ...current,
                      [service.id]: [photo, ...(current[service.id] ?? [])],
                    }))
                    setServices((current) =>
                      current.map((item) =>
                        item.id === service.id ? { ...item, imageUrl: photo.imageUrl } : item,
                      ),
                    )
                  } catch (cause) {
                    setError(apiError(cause, 'Could not upload that photo.'))
                  }
                }}
              />
            </label>
            <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
              {(photos[service.id] ?? []).map((photo) => (
                <img key={photo.id} src={photo.imageUrl} alt="" className="aspect-square w-full object-cover" />
              ))}
            </div>
          </article>
        ))}
      </div>
    </main>
  )
}

function ServiceForm({
  initial,
  submitLabel,
  onSubmit,
}: {
  initial: {
    name: string
    category: string
    description: string
    price: string
    durationMinutes: string
  }
  submitLabel: string
  onSubmit: (payload: ServiceWrite) => Promise<void>
}) {
  const [name, setName] = useState(initial.name)
  const [category, setCategory] = useState(initial.category)
  const [description, setDescription] = useState(initial.description)
  const [price, setPrice] = useState(initial.price)
  const [durationMinutes, setDurationMinutes] = useState(initial.durationMinutes)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)
  const [saving, setSaving] = useState(false)
  const categories = CATEGORIES.includes(category) ? CATEGORIES : [category, ...CATEGORIES]

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setSaved(false)
    try {
      await onSubmit({
        name: name.trim(),
        category,
        description: description.trim() || null,
        price: Number(price),
        durationMinutes: Number(durationMinutes),
      })
      setSaved(true)
    } catch (cause) {
      setError(apiError(cause, 'Could not save that service.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="grid gap-3 sm:grid-cols-2" onSubmit={submit}>
      <label className="block text-sm sm:col-span-2">
        <span className="text-muted">Name</span>
        <input
          required
          value={name}
          onChange={(event) => setName(event.target.value)}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        />
      </label>
      <label className="block text-sm">
        <span className="text-muted">Category</span>
        <select
          value={category}
          onChange={(event) => setCategory(event.target.value)}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        >
          {categories.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>
      <label className="block text-sm">
        <span className="text-muted">Price, Ksh</span>
        <input
          required
          type="number"
          min="0"
          step="0.01"
          value={price}
          onChange={(event) => setPrice(event.target.value)}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        />
      </label>
      <label className="block text-sm">
        <span className="text-muted">Duration, minutes</span>
        <input
          required
          type="number"
          min="1"
          max="600"
          value={durationMinutes}
          onChange={(event) => setDurationMinutes(event.target.value)}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        />
      </label>
      <label className="block text-sm sm:col-span-2">
        <span className="text-muted">Description</span>
        <textarea
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          rows={2}
          className="mt-2 w-full border border-fog bg-paper px-3 py-2"
        />
      </label>
      <div className="sm:col-span-2 flex flex-wrap items-center gap-3">
        <button
          type="submit"
          disabled={saving}
          className="rounded-full bg-ink px-4 py-2 text-sm text-paper disabled:opacity-60"
        >
          {saving ? 'Saving…' : submitLabel}
        </button>
        {saved && <p className="text-sm text-sage">Saved.</p>}
        {error && <p className="text-sm text-clay">{error}</p>}
      </div>
    </form>
  )
}
