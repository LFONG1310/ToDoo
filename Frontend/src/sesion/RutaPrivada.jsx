import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useSesion } from './sesionContext.js'

// Muestra la ruta solo con sesion iniciada; si no, manda al inicio de sesion y recuerda a donde iba.
export default function RutaPrivada() {
  const { usuario, cargando, error, reintentar } = useSesion()
  const location = useLocation()

  if (cargando) {
    return <p className="estado-pantalla" role="status">Cargando…</p>
  }
  if (error) {
    return (
      <div className="estado-pantalla" role="alert">
        <p>{error.message}</p>
        <button type="button" className="boton boton-secundario" onClick={reintentar}>Reintentar</button>
      </div>
    )
  }
  if (!usuario) {
    return <Navigate to="/" replace state={{ desde: location.pathname }} />
  }
  return <Outlet />
}
