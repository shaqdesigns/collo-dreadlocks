# Collo Dreadlocks Kisii — Postman API Guide

**Base URL:** `http://localhost:8080`  
**Content-Type:** `application/json` for every request that has a body  
**Date:** 4 November 2026

Restart the backend on port 8080 before testing. Use **Java 21**.

Suggested Postman collection variables:

| Variable | Example |
|---|---|
| `baseUrl` | `http://localhost:8080` |
| `adminToken` | from admin login |
| `salonistToken` | from salonist login |
| `clientToken` | from client register/login |
| `bookingId` | numeric id from create booking |
| `bookingReference` | UUID from create booking |
| `availabilityId` | from create availability |
| `unavailabilityId` | from salonist unavailability request |
| `customerId` | from create/list customers |

Authorization header for protected routes:

```
Authorization: Bearer {{adminToken}}
```

---

## How to run this

1. Public catalogue (no token)
2. Guest booking (no token)
3. Admin login, then admin routes
4. Create salonist portal access, then salonist routes
5. Client register, then `/api/me/bookings`

Use a **future weekday**. Seeded hours are Monday–Saturday `09:00–17:00` (Africa/Nairobi).  
`2026-11-10` is a Tuesday and is a good slot date.

Error body shape used across the API:

```json
{
  "status": "404",
  "error": "Service Not Found",
  "message": "Service not found: 99999"
}
```

---

# A. Public (no login)

## 1. Health

`GET {{baseUrl}}/api/health`

Body: none

**200**

```json
{
  "status": "UP",
  "service": "collo-dreadlocks-backend"
}
```

---

## 2. List active services

`GET {{baseUrl}}/api/services`

**200** — array of ~25 active services. Inactive tattoo/piercing services are omitted.

```json
[
  {
    "id": 1,
    "name": "Dreadlock Installation",
    "slug": "dreadlock-installation",
    "category": "DREADLOCKS",
    "description": "Professional dreadlock installation tailored to your preferred style and hair length.",
    "price": 2500.00,
    "durationMinutes": 180,
    "imageUrl": null,
    "isActive": true
  }
]
```

---

## 3. Get one service

`GET {{baseUrl}}/api/services/1`

**200** — one `ServiceResponse` as above.

`GET {{baseUrl}}/api/services/99999`  
**404**

```json
{
  "status": "404",
  "error": "Service Not Found",
  "message": "Service not found: 99999"
}
```

---

## 4. Salonists for a service

`GET {{baseUrl}}/api/services/1/salonists`

**200** — active salonists assigned to that service.

```json
[
  {
    "id": 1,
    "name": "Collo Dreadlocks Stylist 1",
    "phone": "0700000001",
    "email": "stylist1@collodreadlocks.local",
    "bio": "Specialist in dreadlock installation, retouching and styling.",
    "profileImageUrl": null,
    "isActive": true
  }
]
```

IDs/names depend on your seed data. If empty, that service has no assigned salonist and cannot be booked.

---

## 5. List active salonists

`GET {{baseUrl}}/api/salonists`

**200** — array of `SalonistResponse`.

---

## 6. Get one salonist

`GET {{baseUrl}}/api/salonists/1`

**200** — one salonist.

`GET {{baseUrl}}/api/salonists/99999`  
**404** `"Salonist not found: 99999"`

---

## 7. Services for a salonist

`GET {{baseUrl}}/api/salonists/1/services`

**200** — array of active services assigned to salonist 1.

---

## 8. Public weekly hours

`GET {{baseUrl}}/api/salonists/1/availability`

**200** — active windows only, ordered Monday first.

```json
[
  {
    "id": 1,
    "salonistId": 1,
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "isActive": true
  }
]
```

Seeded data: Monday–Saturday 09:00–17:00. Sunday has no rows unless you added one.

`GET {{baseUrl}}/api/salonists/99999/availability`  
**404** salonist not found.

---

## 9. Available booking slots

`GET {{baseUrl}}/api/bookings/slots?serviceId=1&date=2026-11-10`

Optional filter: `&salonistId=1`

**200**

```json
[
  {
    "salonistId": 1,
    "salonistName": "Collo Dreadlocks Stylist 1",
    "startAt": "2026-11-10T09:00:00",
    "endAt": "2026-11-10T12:00:00"
  }
]
```

Dreadlock Installation is 180 minutes, so 09:00 ends at 12:00. Slots step every 15 minutes and skip past times and occupied/unavailable windows.

**400** if `date` is in the past: `"date cannot be in the past"`  
**404** if the service is missing/inactive.

---

## 10. Guest booking (no account)

`POST {{baseUrl}}/api/bookings`

```json
{
  "serviceId": 1,
  "salonistId": 1,
  "startAt": "2026-11-10T09:00:00",
  "customer": {
    "name": "Guest Client",
    "phone": "0799000001",
    "email": null
  }
}
```

`salonistId` may be omitted. The API picks any assigned salonist who is free at `startAt`.

**201**

```json
{
  "id": 1,
  "reference": "11111111-1111-1111-1111-111111111111",
  "status": "PENDING",
  "customerId": 4,
  "customerName": "Guest Client",
  "customerPhone": "0799000001",
  "serviceId": 1,
  "serviceName": "Dreadlock Installation",
  "salonistId": 1,
  "salonistName": "Collo Dreadlocks Stylist 1",
  "startAt": "2026-11-10T09:00:00",
  "endAt": "2026-11-10T12:00:00",
  "durationMinutes": 180,
  "price": 2500.00
}
```

Save `id` and `reference`. Guest bookings do **not** set date of birth.

Same slot again: **409** `"This appointment slot is no longer available"`  
Unavailable time: **400** `"The selected time is not available"`  
Salonist not assigned to the service: **404** salonist not found.

---

## 11. Lookup booking by reference (no login)

`GET {{baseUrl}}/api/bookings/{{bookingReference}}`

**200** — same `BookingResponse` as create.

Unknown UUID: **404** `"Booking not found: ..."`

---

# B. Auth

## 12. Admin login

`POST {{baseUrl}}/api/auth/login`

```json
{
  "email": "davidsimiyu341@gmail.com",
  "password": "DevAdmin123!"
}
```

**200**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "davidsimiyu341@gmail.com",
  "role": "ADMIN",
  "salonistId": null,
  "customerId": null
}
```

Copy `token` into `{{adminToken}}`.

Wrong password: **401** `"Invalid email or password"`

---

## 13. Client register (optional portal)

`POST {{baseUrl}}/api/auth/register`

```json
{
  "name": "Mercy Kemunto",
  "phone": "0722000001",
  "email": "mercy@example.com",
  "password": "Client123",
  "dateOfBirth": "1998-03-12"
}
```

`dateOfBirth` may be `null`. Password must be 8–72 characters.

**201** — `AuthResponse` with `role": "CUSTOMER"` and a `customerId`. Save `{{clientToken}}`.

Registering with the admin email: **409** `"This email cannot be used to register"`  
Duplicate email: **409** `"An account with this email already exists"`

---

## 14. Client / salonist login

`POST {{baseUrl}}/api/auth/login`

```json
{
  "email": "mercy@example.com",
  "password": "Client123"
}
```

**200** — `role` is `CUSTOMER` or `SALONIST`.

---

## 15. Current user

`GET {{baseUrl}}/api/auth/me`  
Header: `Authorization: Bearer {{adminToken}}` (or any valid token)

**200** — same shape as login.  
No/invalid token: **401** or **403**.

---

# C. Client portal

Header: `Authorization: Bearer {{clientToken}}`

## 16. My bookings

`GET {{baseUrl}}/api/me/bookings`

**200** — array of `BookingResponse` for that customer (empty `[]` if none).

Admin or salonist token: **403**.

---

# D. Admin — bookings

Header: `Authorization: Bearer {{adminToken}}` on all `/api/admin/**` routes.  
Missing token: **401/403**.

## 17. List bookings

`GET {{baseUrl}}/api/admin/bookings`  
`GET {{baseUrl}}/api/admin/bookings?status=PENDING`  
`GET {{baseUrl}}/api/admin/bookings?date=2026-11-10`  
`GET {{baseUrl}}/api/admin/bookings?status=PENDING&date=2026-11-10`

`status` values: `PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`

**200** — array of `BookingResponse`.

---

## 18. Get booking by id

`GET {{baseUrl}}/api/admin/bookings/{{bookingId}}`

**200** / **404**

---

## 19. Confirm

`POST {{baseUrl}}/api/admin/bookings/{{bookingId}}/confirm`  
Body: none

**200** — `status": "CONFIRMED"`  
Illegal transition (e.g. already completed): **400** `"Cannot change booking from ... to ..."`

---

## 20. Cancel

`POST {{baseUrl}}/api/admin/bookings/{{bookingId}}/cancel`  
Body: none

**200** — `CANCELLED`  
Allowed from `PENDING` or `CONFIRMED`.

---

## 21. Complete

`POST {{baseUrl}}/api/admin/bookings/{{bookingId}}/complete`  
Body: none

**200** — `COMPLETED`  
Only from `CONFIRMED`.

---

## 22. No-show

`POST {{baseUrl}}/api/admin/bookings/{{bookingId}}/no-show`  
Body: none

**200** — `NO_SHOW`  
Only from `CONFIRMED`.

---

# E. Admin — customers

## 23. List customers

`GET {{baseUrl}}/api/admin/customers`  
`GET {{baseUrl}}/api/admin/customers?phone=0711000001`

**200** — array of:

```json
{
  "id": 1,
  "name": "Amina Nyaboke",
  "phone": "0711000001",
  "email": "amina@example.com",
  "dateOfBirth": "1995-11-05",
  "isActive": true
}
```

---

## 24. Upcoming birthdays

`GET {{baseUrl}}/api/admin/customers/birthdays`  
`GET {{baseUrl}}/api/admin/customers/birthdays?days=7`

Default window is 7 days including today. Max `days` = 366. Wraps over New Year.

**200** — customers with a date of birth in that window.  
**400** if `days` is 0 or greater than 366.

---

## 25. Get customer

`GET {{baseUrl}}/api/admin/customers/1`

**200** / **404** `"Customer not found: 1"`

---

## 26. Customer booking history

`GET {{baseUrl}}/api/admin/customers/1/bookings`

**200** — that customer’s bookings, newest first.

---

## 27. Create customer

`POST {{baseUrl}}/api/admin/customers`

```json
{
  "name": "Jane Moraa",
  "phone": "0711000004",
  "email": "jane@example.com",
  "dateOfBirth": "1992-03-14",
  "isActive": true
}
```

**201** — `CustomerResponse`  
Duplicate phone: **409** `"A customer with this phone already exists"`

---

## 28. Update customer

`PUT {{baseUrl}}/api/admin/customers/1`

Same body as create.

**200** / **404** / **409** (phone used by someone else)

---

## 29. Deactivate customer

`DELETE {{baseUrl}}/api/admin/customers/1`  
Body: none

**200** — `isActive: false` (row is not deleted)

---

# F. Admin — availability

## 30. All windows (including inactive)

`GET {{baseUrl}}/api/admin/salonists/1/availability`

**200** — array of `AvailabilityResponse`

---

## 31. Add hours

`POST {{baseUrl}}/api/admin/salonists/1/availability`

```json
{
  "dayOfWeek": "SUNDAY",
  "startTime": "10:00:00",
  "endTime": "14:00:00",
  "isActive": true
}
```

`dayOfWeek`: `MONDAY` … `SUNDAY`

**201** — created window. Save `id` as `{{availabilityId}}`.

End before start: **400** `"End time must be after start time"`  
Overlap with an active window on the same day: **409**  
Adjacent windows (09:00–12:00 and 12:00–17:00) are allowed.

---

## 32. Update hours

`PUT {{baseUrl}}/api/admin/salonists/1/availability/{{availabilityId}}`

```json
{
  "dayOfWeek": "SUNDAY",
  "startTime": "11:00:00",
  "endTime": "15:00:00",
  "isActive": true
}
```

**200** / **404** `"Availability not found: ..."`

---

## 33. Deactivate hours

`DELETE {{baseUrl}}/api/admin/salonists/1/availability/{{availabilityId}}`  
Body: none

**200** — `isActive: false`

---

# G. Admin — salonist portal access

## 34. Create or reset salonist login

`POST {{baseUrl}}/api/admin/salonists/1/access`

```json
{
  "email": "stylist1@collodreadlocks.local",
  "password": "Salonist123"
}
```

**201**

```json
{
  "token": "eyJ...",
  "userId": 2,
  "email": "stylist1@collodreadlocks.local",
  "role": "SALONIST",
  "salonistId": 1,
  "customerId": null
}
```

You can ignore this token and log in as the salonist with request **14**. Save `{{salonistToken}}` from that login.

Using the admin email here: **409** `"This email is reserved for the administrator"`  
Email already used by another account: **409**

---

# H. Admin — unavailability requests

## 35. List requests

`GET {{baseUrl}}/api/admin/unavailability-requests`  
`GET {{baseUrl}}/api/admin/unavailability-requests?status=PENDING`

`status`: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`

**200** — array of:

```json
{
  "id": 1,
  "salonistId": 1,
  "startAt": "2026-11-12T09:00:00",
  "endAt": "2026-11-12T17:00:00",
  "reason": "Personal appointment",
  "status": "PENDING",
  "adminNote": null
}
```

---

## 36. Approve (hides slots from clients)

`POST {{baseUrl}}/api/admin/unavailability-requests/{{unavailabilityId}}/approve`

Body optional:

```json
{
  "adminNote": "Approved for 12 Nov"
}
```

**200** — `status": "APPROVED"`

Then `GET /api/bookings/slots?serviceId=1&date=2026-11-12&salonistId=1` should omit times inside that window.

Only `PENDING` can be approved. Otherwise **400**.

---

## 37. Reject

`POST {{baseUrl}}/api/admin/unavailability-requests/{{unavailabilityId}}/reject`

```json
{
  "adminNote": "Need coverage that day"
}
```

**200** — `REJECTED`. Clients still see normal hours.

---

# I. Salonist portal

Header: `Authorization: Bearer {{salonistToken}}`

## 38. My work / bookings

`GET {{baseUrl}}/api/salonist/bookings`  
`GET {{baseUrl}}/api/salonist/bookings?date=2026-11-10`

**200** — that salonist’s bookings only.

---

## 39. Mark a job completed

`POST {{baseUrl}}/api/salonist/bookings/{{bookingId}}/complete`  
Body: none

**200** — `COMPLETED` (booking must be `CONFIRMED` and belong to this salonist)  
Wrong salonist’s booking: **403**  
Bad status: **400**

---

## 40. List my unavailability requests

`GET {{baseUrl}}/api/salonist/unavailability-requests`

**200** — this salonist’s requests.

---

## 41. Request unavailability (not visible to clients yet)

`POST {{baseUrl}}/api/salonist/unavailability-requests`

```json
{
  "startAt": "2026-11-12T09:00:00",
  "endAt": "2026-11-12T17:00:00",
  "reason": "Personal appointment"
}
```

**201** — `status": "PENDING"`. Save `{{unavailabilityId}}`.

End before start: **400**  
Overlap with another pending/approved request: **409**  
Period already ended: **400** `"Unavailability cannot end in the past"`

Until admin **approves**, public slots stay unchanged.

---

## 42. Cancel a pending request

`POST {{baseUrl}}/api/salonist/unavailability-requests/{{unavailabilityId}}/cancel`  
Body: none

**200** — `CANCELLED`  
Already approved/rejected: **400** `"Only pending requests can be cancelled"`

---

# J. Status codes

| Code | When |
|---|---|
| 200 | Success |
| 201 | Created |
| 400 | Validation / illegal status change / invalid time |
| 401 | Bad login or missing/invalid token |
| 403 | Authenticated but wrong role, or booking not yours |
| 404 | Resource not found |
| 409 | Slot taken, duplicate phone/email, overlapping hours/unavailability |

---

# Suggested confirmation checklist

- [ ] Health is `UP`
- [ ] Services list is ~25 items
- [ ] Guest booking creates `PENDING` without a password
- [ ] Duplicate slot returns 409
- [ ] Admin login works; `/api/admin/customers` fails without the token
- [ ] Admin confirm changes booking to `CONFIRMED`
- [ ] Salonist access + login works; salonist sees only their bookings
- [ ] Unavailability stays bookable while `PENDING`
- [ ] After admin approve, those slots disappear for clients
- [ ] Client register + `/api/me/bookings` works with the client token only

When this list is green, we can start the frontend.
