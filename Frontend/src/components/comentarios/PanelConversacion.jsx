import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { obtenerProyecto } from '../../services/proyectosApi.js'
import { useSesion } from '../../sesion/sesionContext.js'
import ComentariosTablero from './ComentariosTablero.jsx'
import './comentarios.css'

const nombresRol = { administrador: 'Administrador', miembro: 'Miembro' }

// Conversacion del equipo dentro del tablero. Necesita sesion porque los comentarios viven en MySQL.
export default function PanelConversacion({ proyectoId, volverA }) {
  const { usuario, cargando } = useSesion()

  if (cargando) {
    return <p className="conversacion conversacion-estado" role="status">Cargando conversación…</p>
  }
  if (!usuario) {
    return (
      <section className="conversacion conversacion-estado" aria-label="Conversación del equipo">
        <strong>Conversación del equipo</strong>
        <p>Inicia sesión para leer y escribir comentarios con los miembros del proyecto.</p>
        <Link to="/login" state={{ desde: volverA }} className="boton boton-primario conversacion-entrar">
          Iniciar sesión
        </Link>
      </section>
    )
  }
  // La key reinicia el estado si cambia el proyecto o la persona.
  return <PanelProyecto key={`${proyectoId}-${usuario.id}`} proyectoId={proyectoId} usuarioId={usuario.id} />
}

function PanelProyecto({ proyectoId, usuarioId }) {
  const { sesionExpirada } = useSesion()
  const [proyecto, setProyecto] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    obtenerProyecto(proyectoId, controller.signal)
      .then((datos) => setProyecto(datos))
      .catch((failure) => {
        if (failure.name === 'AbortError') return
        if (failure.status === 401) sesionExpirada()
        else setError(failure)
      })
    return () => controller.abort()
  }, [proyectoId, sesionExpirada])

  if (error) {
    return <p className="conversacion conversacion-estado" role="alert">{error.message}</p>
  }
  if (!proyecto) {
    return <p className="conversacion conversacion-estado" role="status">Cargando conversación…</p>
  }

  return (
    <div className="panel-conversacion">
      <ComentariosTablero
        proyectoId={proyecto.id}
        usuarioId={usuarioId}
        esAdministrador={proyecto.mi_rol === 'administrador'}
      />
      <aside className="panel-miembros" aria-labelledby="titulo-miembros">
        <h2 id="titulo-miembros">Miembros ({proyecto.miembros.length})</h2>
        <ul>
          {proyecto.miembros.map((miembro) => (
            <li key={miembro.usuario_id}>
              <span>
                {miembro.nombre}
                {miembro.usuario_id === usuarioId && <span className="comentario-tu"> (tú)</span>}
              </span>
              <span className={`etiqueta-rol rol-${miembro.rol}`}>{nombresRol[miembro.rol] ?? miembro.rol}</span>
            </li>
          ))}
        </ul>
      </aside>
    </div>
  )
}
