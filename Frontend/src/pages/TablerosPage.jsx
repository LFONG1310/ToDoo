import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import EncabezadoApp from '../components/EncabezadoApp.jsx'
import { listarTableros } from '../services/tablerosApi.js'
import { useSesion } from '../sesion/sesionContext.js'

const nombresRol = { PROPIETARIO: 'Propietario', RESPONSABLE: 'Responsable' }

export default function TablerosPage() {
  const { sesionExpirada } = useSesion()
  const [tableros, setTableros] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    listarTableros(controller.signal)
      .then((datos) => {
        setTableros(datos)
        setError(null)
      })
      .catch((failure) => {
        if (failure.name === 'AbortError') return
        if (failure.status === 401) sesionExpirada()
        else setError(failure)
      })
      .finally(() => {
        if (!controller.signal.aborted) setCargando(false)
      })
    return () => controller.abort()
  }, [sesionExpirada])

  return (
    <div className="pagina">
      <EncabezadoApp />
      <main className="pagina-contenido">
        <h1 className="pagina-titulo">Mis tableros</h1>
        <p className="pagina-intro">Entra a un tablero para conversar con sus responsables.</p>
        {cargando ? (
          <p className="estado-vacio" role="status">Cargando tableros…</p>
        ) : error ? (
          <p className="estado-vacio" role="alert">{error.message}</p>
        ) : tableros.length === 0 ? (
          <div className="estado-vacio">
            <strong>Aún no participas en ningún tablero</strong>
            <p>Cuando el propietario de un tablero te agregue, aparecerá aquí.</p>
          </div>
        ) : (
          <ul className="lista-tableros">
            {tableros.map((tablero) => (
              <li key={tablero.id}>
                <Link to={`/tableros/${tablero.id}`} className="tarjeta-tablero">
                  <span className="tarjeta-tablero-nombre">{tablero.nombre}</span>
                  {tablero.descripcion && <span className="tarjeta-tablero-descripcion">{tablero.descripcion}</span>}
                  <span className="tarjeta-tablero-meta">
                    {nombresRol[tablero.mi_rol] ?? tablero.mi_rol} · {tablero.total_miembros}{' '}
                    {tablero.total_miembros === 1 ? 'miembro' : 'miembros'}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  )
}
