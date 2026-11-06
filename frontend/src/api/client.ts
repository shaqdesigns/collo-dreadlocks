import axios from 'axios'
import { readSession } from '../auth/session'
import type { Role, Session } from '../types/auth'
import type {
  AdminSalonist,
  Booking,
  BookingSlot,
  CalendarDay,
  ManualWork,
  Remark,
  RevenueReport,
  Salonist,
  SalonistEarnings,
  Service,
  ServicePhoto,
  StudioNotification,
  Unavailability,
  ClientNote,
  ClientProfile,
  ClientSummary,
  Customer,
} from '../types/catalog'

function resolveApiBase(): string {
  const configured = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '')
  if (configured.startsWith('http') && !configured.endsWith('/api')) {
    return `${configured}/api`
  }
  return configured
}

const apiBase = resolveApiBase()

function absoluteMedia(path: string): string {
  if (/^https?:\/\//i.test(path) || !apiBase.startsWith('http')) {
    return path
  }
  const origin = apiBase.replace(/\/api$/, '')
  return `${origin}${path.startsWith('/') ? path : `/${path}`}`
}

function rewriteMedia(value: unknown): void {
  if (!value || typeof value !== 'object') {
    return
  }
  if (Array.isArray(value)) {
    value.forEach(rewriteMedia)
    return
  }
  const record = value as Record<string, unknown>
  for (const [key, child] of Object.entries(record)) {
    if ((key === 'imageUrl' || key === 'profileImageUrl') && typeof child === 'string') {
      record[key] = absoluteMedia(child)
    } else if (child && typeof child === 'object') {
      rewriteMedia(child)
    }
  }
}

const http = axios.create({
  baseURL: apiBase,
  timeout: 15000,
})

http.interceptors.response.use((response) => {
  rewriteMedia(response.data)
  return response
})

http.interceptors.request.use((config) => {
  const session = readSession()
  if (session) {
    config.headers.Authorization = `Bearer ${session.token}`
  }
  return config
})

export type ServiceWrite = {
  name: string
  category: string
  description: string | null
  price: number
  durationMinutes: number
}

export async function getServices(): Promise<Service[]> {
  const { data } = await http.get<Service[]>('/services')
  return data
}

export async function createService(payload: ServiceWrite): Promise<Service> {
  const { data } = await http.post<Service>('/admin/services', payload)
  return data
}

export async function updateService(id: number, payload: ServiceWrite): Promise<Service> {
  const { data } = await http.put<Service>(`/admin/services/${id}`, payload)
  return data
}

export async function deleteService(id: number): Promise<void> {
  await http.delete(`/admin/services/${id}`)
}

export async function getService(id: number): Promise<Service> {
  const { data } = await http.get<Service>(`/services/${id}`)
  return data
}

export async function getSalonistsForService(id: number): Promise<Salonist[]> {
  const { data } = await http.get<Salonist[]>(`/services/${id}/salonists`)
  return data
}

export async function getSalonists(): Promise<Salonist[]> {
  const { data } = await http.get<Salonist[]>('/salonists')
  return data
}

export async function getSalonist(id: number): Promise<Salonist> {
  const { data } = await http.get<Salonist>(`/salonists/${id}`)
  return data
}

export async function getServicesForSalonist(id: number): Promise<Service[]> {
  const { data } = await http.get<Service[]>(`/salonists/${id}/services`)
  return data
}

export async function getSlots(
  serviceId: number,
  date: string,
  salonistId?: number,
): Promise<BookingSlot[]> {
  const { data } = await http.get<BookingSlot[]>('/bookings/slots', {
    params: { serviceId, date, salonistId },
  })
  return data
}

export async function login(email: string, password: string): Promise<Session> {
  const { data } = await http.post<Session>('/auth/login', { email, password })
  return data
}

export async function registerClient(payload: {
  name: string
  phone: string
  email: string
  password: string
  dateOfBirth: string | null
}): Promise<Session> {
  const { data } = await http.post<Session>('/auth/register', payload)
  return data
}

export async function getAdminBookings(): Promise<Booking[]> {
  const { data } = await http.get<Booking[]>('/admin/bookings')
  return data
}

export async function confirmBooking(id: number): Promise<Booking> {
  const { data } = await http.post<Booking>(`/admin/bookings/${id}/confirm`)
  return data
}

export async function cancelBooking(id: number): Promise<Booking> {
  const { data } = await http.post<Booking>(`/admin/bookings/${id}/cancel`)
  return data
}

export async function getMyBookings(): Promise<Booking[]> {
  const { data } = await http.get<Booking[]>('/me/bookings')
  return data
}

export async function getSalonistBookings(): Promise<Booking[]> {
  const { data } = await http.get<Booking[]>('/salonist/bookings')
  return data
}

export async function completeSalonistBooking(id: number): Promise<Booking> {
  const { data } = await http.post<Booking>(`/salonist/bookings/${id}/complete`)
  return data
}

export async function getAdminSalonists(): Promise<AdminSalonist[]> {
  const { data } = await http.get<AdminSalonist[]>('/admin/salonists')
  return data
}

export async function createSalonistAccess(
  salonistId: number,
  email: string,
  password: string,
): Promise<void> {
  await http.post(`/admin/salonists/${salonistId}/access`, { email, password })
}

export async function getAdminUnavailability(): Promise<Unavailability[]> {
  const { data } = await http.get<Unavailability[]>('/admin/unavailability-requests')
  return data
}

export async function approveUnavailability(id: number): Promise<Unavailability> {
  const { data } = await http.post<Unavailability>(`/admin/unavailability-requests/${id}/approve`, {})
  return data
}

export async function rejectUnavailability(id: number): Promise<Unavailability> {
  const { data } = await http.post<Unavailability>(`/admin/unavailability-requests/${id}/reject`, {})
  return data
}

export async function getAdminRemarks(): Promise<Remark[]> {
  const { data } = await http.get<Remark[]>('/admin/remarks')
  return data
}

export async function getSalonistProfile(): Promise<Salonist> {
  const { data } = await http.get<Salonist>('/salonist/profile')
  return data
}

export async function getSalonistUnavailability(): Promise<Unavailability[]> {
  const { data } = await http.get<Unavailability[]>('/salonist/unavailability-requests')
  return data
}

export async function requestTimeOff(payload: {
  startAt: string
  endAt: string
  reason: string | null
}): Promise<Unavailability> {
  const { data } = await http.post<Unavailability>('/salonist/unavailability-requests', payload)
  return data
}

export async function cancelTimeOff(id: number): Promise<Unavailability> {
  const { data } = await http.post<Unavailability>(`/salonist/unavailability-requests/${id}/cancel`)
  return data
}

export async function getSalonistRemarks(): Promise<Remark[]> {
  const { data } = await http.get<Remark[]>('/salonist/remarks')
  return data
}

export async function getMyRemarks(): Promise<Remark[]> {
  const { data } = await http.get<Remark[]>('/me/remarks')
  return data
}

export async function leaveRemark(bookingId: number, body: string): Promise<Remark> {
  const { data } = await http.post<Remark>(`/me/bookings/${bookingId}/remarks`, { body })
  return data
}

export async function deleteSalonist(salonistId: number): Promise<void> {
  await http.delete(`/admin/salonists/${salonistId}`)
}

export async function createSalonist(payload: {
  name: string
  phone: string
  email: string | null
  bio: string | null
  serviceIds: number[]
}): Promise<AdminSalonist> {
  const { data } = await http.post<AdminSalonist>('/admin/salonists', payload)
  return data
}

export async function uploadSalonistPhoto(salonistId: number, file: File): Promise<Salonist> {
  const body = new FormData()
  body.append('file', file)
  const { data } = await http.post<Salonist>(`/admin/salonists/${salonistId}/photo`, body)
  return data
}

export async function getServicePhotos(serviceId: number): Promise<ServicePhoto[]> {
  const { data } = await http.get<ServicePhoto[]>(`/services/${serviceId}/photos`)
  return data
}

export async function uploadServicePhoto(serviceId: number, file: File): Promise<ServicePhoto> {
  const body = new FormData()
  body.append('file', file)
  const { data } = await http.post<ServicePhoto>(`/admin/services/${serviceId}/photos`, body)
  return data
}

export async function getCalendar(date: string): Promise<CalendarDay> {
  const { data } = await http.get<CalendarDay>('/admin/calendar', { params: { date } })
  return data
}

function notificationPath(role: Role): string {
  if (role === 'ADMIN') {
    return '/admin/notifications'
  }
  if (role === 'SALONIST') {
    return '/salonist/notifications'
  }
  return '/me/notifications'
}

export async function getNotifications(role: Role): Promise<StudioNotification[]> {
  const { data } = await http.get<StudioNotification[]>(notificationPath(role))
  return data
}

export async function markNotificationRead(role: Role, id: number): Promise<void> {
  await http.post(`${notificationPath(role)}/${id}/read`)
}

export async function markAllNotificationsRead(role: Role): Promise<void> {
  await http.post(`${notificationPath(role)}/read-all`)
}

export async function getClients(query: string): Promise<ClientSummary[]> {
  const { data } = await http.get<ClientSummary[]>('/admin/crm/clients', {
    params: query.trim() ? { q: query.trim() } : {},
  })
  return data
}

export async function getClientBirthdays(): Promise<Customer[]> {
  const { data } = await http.get<Customer[]>('/admin/crm/clients/birthdays')
  return data
}

export async function getClient(id: number): Promise<ClientProfile> {
  const { data } = await http.get<ClientProfile>(`/admin/crm/clients/${id}`)
  return data
}

export async function addClientNote(id: number, body: string): Promise<ClientNote> {
  const { data } = await http.post<ClientNote>(`/admin/crm/clients/${id}/notes`, { body })
  return data
}

export async function updateCustomer(
  id: number,
  payload: {
    name: string
    phone: string
    email: string | null
    dateOfBirth: string | null
    isActive: boolean
  },
): Promise<Customer> {
  const { data } = await http.put<Customer>(`/admin/customers/${id}`, payload)
  return data
}

export async function getRevenueReport(): Promise<RevenueReport> {
  const { data } = await http.get<RevenueReport>('/admin/revenue')
  return data
}

export async function recordManualWork(payload: {
  salonistId: number
  serviceId: number
  price: number
  workedAt: string
  customerName: string | null
  note: string | null
}): Promise<ManualWork> {
  const { data } = await http.post<ManualWork>('/admin/revenue/manual-works', payload)
  return data
}

export async function getSalonistEarnings(): Promise<SalonistEarnings> {
  const { data } = await http.get<SalonistEarnings>('/salonist/revenue')
  return data
}

export async function recordSalonistWork(payload: {
  serviceId: number
  price: number
  workedAt: string
  customerName: string | null
  note: string | null
}): Promise<ManualWork> {
  const { data } = await http.post<ManualWork>('/salonist/revenue/manual-works', payload)
  return data
}

export async function createBooking(payload: {
  serviceId: number
  salonistId?: number
  startAt: string
  customer: { name: string; phone: string; email: string | null }
}): Promise<Booking> {
  const { data } = await http.post<Booking>('/bookings', payload)
  return data
}
