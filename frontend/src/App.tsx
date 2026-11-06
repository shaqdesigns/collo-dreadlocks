import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { Layout } from './components/Layout'
import { AboutPage } from './pages/AboutPage'
import { AdminPage } from './pages/AdminPage'
import { AdminCalendarPage } from './pages/AdminCalendarPage'
import { AdminClientPage } from './pages/AdminClientPage'
import { AdminClientsPage } from './pages/AdminClientsPage'
import { AdminRevenuePage } from './pages/AdminRevenuePage'
import { AdminSalonistsPage } from './pages/AdminSalonistsPage'
import { AdminServicesPage } from './pages/AdminServicesPage'
import { BookPage } from './pages/BookPage'
import { ContactPage } from './pages/ContactPage'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { MePage } from './pages/MePage'
import { RegisterPage } from './pages/RegisterPage'
import { SalonistDetailPage } from './pages/SalonistDetailPage'
import { SalonistPortalPage } from './pages/SalonistPortalPage'
import { SalonistsPage } from './pages/SalonistsPage'
import { ServiceDetailPage } from './pages/ServiceDetailPage'
import { ServicesPage } from './pages/ServicesPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<HomePage />} />
            <Route path="services" element={<ServicesPage />} />
            <Route path="services/:id" element={<ServiceDetailPage />} />
            <Route path="salonists" element={<SalonistsPage />} />
            <Route path="salonists/:id" element={<SalonistDetailPage />} />
            <Route path="about" element={<AboutPage />} />
            <Route path="contact" element={<ContactPage />} />
            <Route path="book" element={<BookPage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />
            <Route path="admin" element={<AdminPage />} />
            <Route path="admin/calendar" element={<AdminCalendarPage />} />
            <Route path="admin/salonists" element={<AdminSalonistsPage />} />
            <Route path="admin/services" element={<AdminServicesPage />} />
            <Route path="admin/clients" element={<AdminClientsPage />} />
            <Route path="admin/clients/:id" element={<AdminClientPage />} />
            <Route path="admin/revenue" element={<AdminRevenuePage />} />
            <Route path="salonist" element={<SalonistPortalPage />} />
            <Route path="me" element={<MePage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
