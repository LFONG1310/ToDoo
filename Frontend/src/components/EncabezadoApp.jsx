import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import todooLogo from '../assets/todoo-logo.png'
import { useSesion } from '../sesion/sesionContext.js'

export default function EncabezadoApp() {
  const { usuario, cerrarSesion } = useSesion()
  const navigate = useNavigate()
  const [saliendo, setSaliendo] = useState(false)

  async function salir() {
    setSaliendo(true)
    try {
      await cerrarSesion()
    } finally {
      navigate('/', { replace: true })
    }
  }

  return (
    <header className="encabezado-app">
      <Link to="/tableros" aria-label="ToDoo: ir a mis tableros">
        <img src={todooLogo} alt="ToDoo" className="encabezado-logo" />
      </Link>
      <nav className="encabezado-nav" aria-label="Principal">
        <NavLink to="/tableros" end>Tableros</NavLink>
        <NavLink to="/notificaciones">Notificaciones</NavLink>
      </nav>
      <div className="encabezado-usuario">
        <span>{usuario?.nombre}</span>
        <button type="button" className="boton boton-secundario" onClick={salir} disabled={saliendo}>
          {saliendo ? 'Saliendo…' : 'Cerrar sesión'}
        </button>
      </div>
    </header>
  )
}
