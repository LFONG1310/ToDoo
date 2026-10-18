import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage.jsx'
import NotificationsPage from './pages/NotificationsPage.jsx'
import TablerosPage from './pages/TablerosPage.jsx'
import TableroPage from './pages/TableroPage.jsx'
import RutaPrivada from './sesion/RutaPrivada.jsx'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route element={<RutaPrivada />}>
        <Route path="/tableros" element={<TablerosPage />} />
        <Route path="/tableros/:tableroId" element={<TableroPage />} />
        <Route path="/notificaciones" element={<NotificationsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
