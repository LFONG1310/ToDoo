import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import EncabezadoApp from '../components/EncabezadoApp.jsx'
import ComentariosTablero from '../components/comentarios/ComentariosTablero.jsx'
import { obtenerTablero } from '../services/tablerosApi.js'
import { useSesion } from '../sesion/sesionContext.js'

const nombresRol = { PROPIETARIO: 'Propietario', RESPONSABLE: 'Responsable' }

export default function TableroPage() {
  const { tableroId } = useParams()
  // La key reinicia todo el estado al cambiar de tablero.
  return <VistaTablero key={tableroId} tableroId={tableroId} />
}

function VistaTablero({ tableroId }) {
  const { usuario, sesionExpirada } = useSesion()
  const [tablero, setTablero] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    obtenerTablero(tableroId, controller.signal)
      .then((datos) => {
        setTablero(datos)
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
  }, [tableroId, sesionExpirada])

  return (
    <div className="pagina">
      <EncabezadoApp />
      <main className="pagina-contenido">
        <Link to="/tableros" className="enlace-volver">← Mis tableros</Link>
        {cargando ? (
          <p className="estado-vacio" role="status">Cargando tablero…</p>
        ) : error ? (
          <div className="estado-vacio" role="alert">
            <strong>{error.message}</strong>
            <Link to="/tableros" className="boton-texto">Ver mis tableros</Link>
          </div>
        ) : (
          <>
            <h1 className="pagina-titulo">{tablero.nombre}</h1>
            {tablero.descripcion && <p className="pagina-intro">{tablero.descripcion}</p>}
            <div className="tablero-distribucion">
              <ComentariosTablero
                tableroId={tablero.id}
                usuarioId={usuario.id}
                esPropietario={tablero.mi_rol === 'PROPIETARIO'}
              />
              <aside className="tablero-miembros" aria-labelledby="titulo-responsables">
                <h2 id="titulo-responsables">Responsables ({tablero.miembros.length})</h2>
                <ul>
                  {tablero.miembros.map((miembro) => (
                    <li key={miembro.usuario_id}>
                      <span>
                        {miembro.nombre}
                        {miembro.usuario_id === usuario.id && <span className="comentario-tu"> (tú)</span>}
                      </span>
                      <span className={`etiqueta-rol rol-${miembro.rol.toLowerCase()}`}>
                        {nombresRol[miembro.rol] ?? miembro.rol}
                      </span>
                    </li>
                  ))}
                </ul>
              </aside>
            </div>
          </>
        )}
      </main>
    </div>
  )
}
