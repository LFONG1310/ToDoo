import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import todooLogo from '../../assets/todoo-logo.png'
import { useSesion } from '../../sesion/sesionContext.js'

export default function NavBar({ children }) {
  const { usuario, cerrarSesion } = useSesion()
  const navigate = useNavigate()
  const [saliendo, setSaliendo] = useState(false)

  async function salir() {
    setSaliendo(true)
    try {
      await cerrarSesion()
    } finally {
      setSaliendo(false)
      navigate('/login')
    }
  }

  return (
    <nav aria-label="Navegación principal" className="w-full text-black flex min-h-16 items-center gap-3 bg-white shadow-md p-4 flex-wrap">
      <Link to="/" aria-label="ToDoo: inicio">
        <img src={todooLogo} alt="ToDoo" className="w-25 h-14 object-contain" />
      </Link>
      <div className="flex flex-row flex-wrap justify-between items-center gap-4 w-full">
        <ul className="flex flex-wrap justify-center items-center gap-4">
          <li>
            <NavLink to="/" end className={({ isActive }) => isActive ? 'font-semibold text-blue-700' : 'hover:text-blue-700'}>Inicio</NavLink>
          </li>
          <li>
            <NavLink to="/proyectos" className={({ isActive }) => isActive ? 'font-semibold text-blue-700' : 'hover:text-blue-700'}>Proyectos</NavLink>
          </li>
          <li>
            <NavLink to="/kanban" className={({ isActive }) => isActive ? 'font-semibold text-blue-700' : 'hover:text-blue-700'}>Tablero</NavLink>
          </li>
          <li>
            <NavLink to="/notificaciones" className={({ isActive }) => isActive ? 'font-semibold text-blue-700' : 'hover:text-blue-700'}>Notificaciones</NavLink>
          </li>
        </ul>
        <div className="flex items-center gap-4">
          {children}
          {usuario ? (
            <>
              <span className="text-sm text-gray-600">{usuario.nombre}</span>
              <button type="button" onClick={salir} disabled={saliendo} className="whitespace-nowrap hover:text-blue-700 disabled:opacity-60">
                {saliendo ? 'Saliendo…' : 'Cerrar sesión'}
              </button>
            </>
          ) : (
            <Link to="/login" className="whitespace-nowrap font-semibold text-blue-700 hover:underline">Iniciar sesión</Link>
          )}
        </div>
      </div>
    </nav>
  );
}
