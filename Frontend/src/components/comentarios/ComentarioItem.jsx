import { useId, useState } from 'react'
import { MAX_COMENTARIO, contarCaracteres } from '../../services/comentariosApi.js'

const formatoFecha = new Intl.DateTimeFormat('es-MX', { dateStyle: 'medium', timeStyle: 'short' })

function iniciales(nombre) {
  return nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0].toUpperCase())
    .join('')
}

export default function ComentarioItem({ comentario, esPropio, puedeEliminar, onGuardar, onEliminar }) {
  const [editando, setEditando] = useState(false)
  const [borrador, setBorrador] = useState('')
  const [guardando, setGuardando] = useState(false)
  const [confirmando, setConfirmando] = useState(false)
  const editorId = useId()
  const caracteres = contarCaracteres(borrador.trim())
  const borradorValido = caracteres > 0 && caracteres <= MAX_COMENTARIO

  function empezarEdicion() {
    setBorrador(comentario.contenido)
    setConfirmando(false)
    setEditando(true)
  }

  async function guardar(event) {
    event.preventDefault()
    if (!borradorValido || guardando) return
    setGuardando(true)
    const ok = await onGuardar(comentario.id, borrador.trim())
    setGuardando(false)
    if (ok) setEditando(false)
  }

  async function eliminar() {
    setGuardando(true)
    const ok = await onEliminar(comentario.id)
    if (!ok) {
      setGuardando(false)
      setConfirmando(false)
    }
  }

  function teclasEditor(event) {
    if (event.key === 'Escape') {
      event.preventDefault()
      setEditando(false)
    } else if (event.key === 'Enter' && !event.shiftKey && !event.nativeEvent.isComposing) {
      event.preventDefault()
      event.currentTarget.form.requestSubmit()
    }
  }

  const fecha = new Date(comentario.fecha_creacion)

  return (
    <li className={`comentario ${esPropio ? 'comentario-propio' : ''}`}>
      <span className="comentario-avatar" aria-hidden="true">{iniciales(comentario.autor_nombre)}</span>
      <article className="comentario-cuerpo" aria-label={`Comentario de ${comentario.autor_nombre}`}>
        <header className="comentario-meta">
          <strong>{comentario.autor_nombre}{esPropio && <span className="comentario-tu"> (tú)</span>}</strong>
          <time dateTime={comentario.fecha_creacion}>{formatoFecha.format(fecha)}</time>
          {comentario.fecha_edicion && (
            <span className="comentario-editado" title={`Editado: ${formatoFecha.format(new Date(comentario.fecha_edicion))}`}>
              (editado)
            </span>
          )}
        </header>

        {editando ? (
          <form className="comentario-editor" onSubmit={guardar}>
            <label htmlFor={editorId} className="solo-lectores">Editar comentario</label>
            <textarea
              id={editorId}
              value={borrador}
              onChange={(e) => setBorrador(e.target.value)}
              onKeyDown={teclasEditor}
              rows={3}
              autoFocus
            />
            <div className="comentario-editor-acciones">
              <span className={caracteres > MAX_COMENTARIO ? 'contador excedido' : 'contador'}>
                {caracteres}/{MAX_COMENTARIO}
              </span>
              <button type="button" className="boton boton-secundario" onClick={() => setEditando(false)} disabled={guardando}>
                Cancelar
              </button>
              <button type="submit" className="boton boton-primario" disabled={!borradorValido || guardando}>
                {guardando ? 'Guardando…' : 'Guardar'}
              </button>
            </div>
          </form>
        ) : (
          <p className="comentario-texto">{comentario.contenido}</p>
        )}

        {!editando && (esPropio || puedeEliminar) && (
          <div className="comentario-acciones">
            {confirmando ? (
              <>
                <span>¿Eliminar este comentario?</span>
                <button type="button" className="boton-texto peligro" onClick={eliminar} disabled={guardando}>
                  {guardando ? 'Eliminando…' : 'Sí, eliminar'}
                </button>
                <button type="button" className="boton-texto" onClick={() => setConfirmando(false)} disabled={guardando}>
                  No
                </button>
              </>
            ) : (
              <>
                {esPropio && (
                  <button type="button" className="boton-texto" onClick={empezarEdicion}>Editar</button>
                )}
                {puedeEliminar && (
                  <button type="button" className="boton-texto peligro" onClick={() => setConfirmando(true)}>Eliminar</button>
                )}
              </>
            )}
          </div>
        )}
      </article>
    </li>
  )
}
