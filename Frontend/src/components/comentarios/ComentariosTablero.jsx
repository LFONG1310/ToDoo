import { useEffect, useId, useRef, useState } from 'react'
import {
  MAX_COMENTARIO,
  contarCaracteres,
  editarComentario,
  eliminarComentario,
  listarComentarios,
  publicarComentario,
} from '../../services/comentariosApi.js'
import { useSesion } from '../../sesion/sesionContext.js'
import ComentarioItem from './ComentarioItem.jsx'
import './comentarios.css'

// Cada cuanto se buscan comentarios nuevos mientras la pestana esta visible.
const INTERVALO_MS = 15000

export default function ComentariosTablero({ tableroId, usuarioId, esPropietario }) {
  const { sesionExpirada } = useSesion()
  const [comentarios, setComentarios] = useState([])
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState(null)
  const [texto, setTexto] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [errorAccion, setErrorAccion] = useState('')
  const [aviso, setAviso] = useState('')
  // Cambia con cada publicacion/edicion/eliminacion para descartar respuestas de consultas viejas.
  const versionRef = useRef(0)
  const listaRef = useRef(null)
  const pegadoAbajoRef = useRef(true)
  const tituloId = useId()
  const campoId = useId()
  const ayudaId = useId()

  useEffect(() => {
    let activo = true
    let controller = null

    async function cargar() {
      controller?.abort()
      const actual = new AbortController()
      controller = actual
      const version = versionRef.current
      try {
        const datos = await listarComentarios(tableroId, actual.signal)
        if (activo && version === versionRef.current) {
          setComentarios(datos)
          setErrorCarga(null)
        }
      } catch (failure) {
        if (!activo || failure.name === 'AbortError') return
        if (failure.status === 401) sesionExpirada()
        else setErrorCarga(failure)
      } finally {
        if (activo && controller === actual) setCargando(false)
      }
    }

    function siEstaVisible() {
      if (document.visibilityState === 'visible') cargar()
    }

    cargar()
    const intervalo = setInterval(siEstaVisible, INTERVALO_MS)
    document.addEventListener('visibilitychange', siEstaVisible)
    return () => {
      activo = false
      controller?.abort()
      clearInterval(intervalo)
      document.removeEventListener('visibilitychange', siEstaVisible)
    }
  }, [tableroId, sesionExpirada])

  // Mantiene la vista en el ultimo mensaje, salvo que la persona haya subido a leer los anteriores.
  useEffect(() => {
    const lista = listaRef.current
    if (lista && pegadoAbajoRef.current) lista.scrollTop = lista.scrollHeight
  }, [comentarios])

  function alDesplazar(event) {
    const lista = event.currentTarget
    pegadoAbajoRef.current = lista.scrollHeight - lista.scrollTop - lista.clientHeight < 48
  }

  function reportar(failure) {
    if (failure.status === 401) {
      sesionExpirada()
      return
    }
    setAviso('')
    setErrorAccion(failure.message)
  }

  const limpio = texto.trim()
  const caracteres = contarCaracteres(limpio)
  const excedido = caracteres > MAX_COMENTARIO

  async function publicar(event) {
    event.preventDefault()
    if (!limpio || excedido || enviando) return
    setEnviando(true)
    setErrorAccion('')
    setAviso('')
    versionRef.current += 1
    try {
      const nuevo = await publicarComentario(tableroId, limpio)
      pegadoAbajoRef.current = true
      setComentarios((actuales) => [...actuales.filter((c) => c.id !== nuevo.id), nuevo])
      setTexto('')
      setAviso('Comentario publicado.')
    } catch (failure) {
      reportar(failure)
    } finally {
      setEnviando(false)
    }
  }

  async function guardarEdicion(comentarioId, nuevoTexto) {
    setErrorAccion('')
    setAviso('')
    versionRef.current += 1
    try {
      const actualizado = await editarComentario(tableroId, comentarioId, nuevoTexto)
      setComentarios((actuales) => actuales.map((c) => (c.id === comentarioId ? actualizado : c)))
      setAviso('Comentario actualizado.')
      return true
    } catch (failure) {
      if (failure.status === 404) setComentarios((actuales) => actuales.filter((c) => c.id !== comentarioId))
      reportar(failure)
      return false
    }
  }

  async function eliminar(comentarioId) {
    setErrorAccion('')
    setAviso('')
    versionRef.current += 1
    try {
      await eliminarComentario(tableroId, comentarioId)
      setComentarios((actuales) => actuales.filter((c) => c.id !== comentarioId))
      setAviso('Comentario eliminado.')
      return true
    } catch (failure) {
      if (failure.status === 404) setComentarios((actuales) => actuales.filter((c) => c.id !== comentarioId))
      reportar(failure)
      return false
    }
  }

  function teclasComposer(event) {
    if (event.key === 'Enter' && !event.shiftKey && !event.nativeEvent.isComposing) {
      event.preventDefault()
      event.currentTarget.form.requestSubmit()
    }
  }

  return (
    <section className="conversacion" aria-labelledby={tituloId}>
      <header className="conversacion-encabezado">
        <h2 id={tituloId}>Conversación del tablero</h2>
        <p>Solo los responsables de este tablero pueden ver estos mensajes.</p>
      </header>

      {cargando ? (
        <p className="conversacion-estado" role="status">Cargando conversación…</p>
      ) : errorCarga && comentarios.length === 0 ? (
        <p className="conversacion-estado" role="alert">{errorCarga.message}</p>
      ) : comentarios.length === 0 ? (
        <div className="conversacion-estado">
          <strong>Aún no hay comentarios</strong>
          <p>Escribe el primero para coordinarte con el equipo.</p>
        </div>
      ) : (
        <ol className="conversacion-lista" ref={listaRef} onScroll={alDesplazar} aria-label="Comentarios" tabIndex={0}>
          {comentarios.map((comentario) => (
            <ComentarioItem
              key={comentario.id}
              comentario={comentario}
              esPropio={comentario.usuario_id === usuarioId}
              puedeEliminar={comentario.usuario_id === usuarioId || esPropietario}
              onGuardar={guardarEdicion}
              onEliminar={eliminar}
            />
          ))}
        </ol>
      )}

      {errorCarga && comentarios.length > 0 && (
        <p className="conversacion-alerta" role="alert">No se pudieron cargar los mensajes más recientes. Se reintentará en unos segundos.</p>
      )}

      <form className="conversacion-composer" onSubmit={publicar}>
        <label htmlFor={campoId} className="solo-lectores">Escribe un comentario</label>
        <textarea
          id={campoId}
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          onKeyDown={teclasComposer}
          placeholder="Escribe un mensaje para el equipo…"
          rows={3}
          aria-describedby={ayudaId}
          aria-invalid={excedido}
          disabled={cargando}
        />
        <div className="conversacion-composer-pie">
          <span id={ayudaId} className="conversacion-ayuda">
            <span className={excedido ? 'contador excedido' : 'contador'}>{caracteres}/{MAX_COMENTARIO}</span>
            {' · '}Enter para enviar, Shift + Enter para un salto de línea
          </span>
          <button type="submit" className="boton boton-primario" disabled={!limpio || excedido || enviando || cargando}>
            {enviando ? 'Enviando…' : 'Comentar'}
          </button>
        </div>
        {errorAccion && <p className="conversacion-error" role="alert">{errorAccion}</p>}
        <p className="solo-lectores" role="status">{aviso}</p>
      </form>
    </section>
  )
}
