import { Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage.jsx'
import TestPage from './pages/TestPage.jsx'
import NotificationsPage from './pages/NotificationsPage.jsx'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/test" element={<TestPage />} />
      <Route path="/notificaciones" element={<NotificationsPage />} />
    </Routes>
  )
}