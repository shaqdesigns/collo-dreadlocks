export type Service = {
  id: number
  name: string
  slug: string
  category: string
  description: string | null
  price: number
  durationMinutes: number
  imageUrl: string | null
  isActive: boolean
}

export type Salonist = {
  id: number
  name: string
  phone: string
  email: string | null
  bio: string | null
  profileImageUrl: string | null
  isActive: boolean
}

export type AdminSalonist = Salonist & {
  hasPortalAccess: boolean
}

export type Unavailability = {
  id: number
  salonistId: number
  startAt: string
  endAt: string
  reason: string | null
  status: string
  adminNote: string | null
}

export type Remark = {
  id: number
  bookingId: number
  customerId: number
  customerName: string
  salonistId: number
  salonistName: string
  serviceId: number
  serviceName: string
  appointmentAt: string
  body: string
  createdAt: string
}

export type ServicePhoto = {
  id: number
  serviceId: number
  imageUrl: string
  createdAt: string
}

export type StudioNotification = {
  id: number
  message: string
  bookingId: number | null
  readAt: string | null
  createdAt: string
}

export type Customer = {
  id: number
  name: string
  phone: string
  email: string | null
  dateOfBirth: string | null
  isActive: boolean
}

export type ClientSummary = {
  id: number
  name: string
  phone: string
  email: string | null
  active: boolean
  visits: number
  spent: number
  lastVisitAt: string | null
  nextVisitAt: string | null
}

export type ClientNote = {
  id: number
  customerId: number
  body: string
  createdAt: string
}

export type ClientProfile = {
  customer: Customer
  visits: number
  spent: number
  lastVisitAt: string | null
  nextVisitAt: string | null
  bookings: Booking[]
  remarks: Remark[]
  notes: ClientNote[]
}

export type CalendarBlock = {
  kind: 'BOOKING' | 'OFF'
  status: string
  startAt: string
  endAt: string
  label: string
  bookingId: number | null
}

export type CalendarColumn = {
  salonistId: number
  salonistName: string
  active: boolean
  blocks: CalendarBlock[]
}

export type CalendarDay = {
  date: string
  salonists: CalendarColumn[]
}

export type ManualWork = {
  id: number
  salonistId: number
  salonistName: string
  serviceId: number
  serviceName: string
  customerName: string | null
  price: number
  workedAt: string
  note: string | null
}

export type ServiceRevenue = {
  serviceId: number
  serviceName: string
  soughtCount: number
  revenue: number
}

export type SalonistRevenue = {
  salonistId: number
  salonistName: string
  completedBookings: number
  manualWorks: number
  revenue: number
  payout: number
}

export type RevenueReport = {
  payoutRate: number
  totalRevenue: number
  salonistPayout: number
  studioShare: number
  bookingRevenue: number
  manualRevenue: number
  mostSought: ServiceRevenue | null
  highestRevenue: ServiceRevenue | null
  services: ServiceRevenue[]
  salonists: SalonistRevenue[]
  manualWorks: ManualWork[]
}

export type SalonistEarnings = {
  payoutRate: number
  revenue: number
  payout: number
  completedBookings: number
  manualWorks: number
}

export type BookingSlot = {
  salonistId: number
  salonistName: string
  startAt: string
  endAt: string
}

export type Booking = {
  id: number
  reference: string
  status: string
  customerId: number
  customerName: string
  customerPhone: string
  serviceId: number
  serviceName: string
  salonistId: number
  salonistName: string
  startAt: string
  endAt: string
  durationMinutes: number
  price: number
  paymentMethod?: string | null
  paymentStatus?: string | null
  paymentPhone?: string | null
  mpesaReceipt?: string | null
  paymentMessage?: string | null
}
