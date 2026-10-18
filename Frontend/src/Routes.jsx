import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/Auth/LoginPage.jsx'
import RegisterPage from './pages/Auth/RegisterPage.jsx'
import HomePage from './pages/Home/HomePage.jsx'
import KanbanPage from './pages/Home/KanbanPage.jsx'
import ProyectosPage from './pages/Home/ProyectosPage.jsx'
import NotificationsPage from './pages/NotificationsPage.jsx'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/home" element={<Navigate to="/" replace />} />
      <Route path="/proyectos" element={<ProyectosPage />} />
      <Route path="/kanban" element={<KanbanPage />} />
      <Route path="/notificaciones" element={<NotificationsPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
    </Routes>
  )
}
