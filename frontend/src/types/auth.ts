export type Role = 'ADMIN' | 'SALONIST' | 'CUSTOMER'

export type Session = {
  token: string
  userId: number
  email: string
  role: Role
  salonistId: number | null
  customerId: number | null
}
