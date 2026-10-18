import { enviar, request } from './http.js'

export const MAX_COMENTARIO = 1000

// Cuenta caracteres reales (un emoji cuenta como 1), igual que el backend y MySQL.
export function contarCaracteres(texto) {
  return [...texto].length
}

const url = (tableroId, comentarioId) => {
  const base = `/api/tableros/${encodeURIComponent(tableroId)}/comentarios`
  return comentarioId === undefined ? base : `${base}/${encodeURIComponent(comentarioId)}`
}

const sinAcceso = 'Este tablero no existe o ya no formas parte de él.'
const invalido = `El comentario no puede estar vacío ni tener más de ${MAX_COMENTARIO} caracteres.`

export function listarComentarios(tableroId, signal) {
  return request(url(tableroId), {
    signal,
    mensajes: { 404: sinAcceso, default: 'No se pudo cargar la conversación. Intenta de nuevo.' },
  })
}

export function publicarComentario(tableroId, texto) {
  return enviar(url(tableroId), {
    method: 'POST',
    json: { texto },
    mensajes: { 400: invalido, 404: sinAcceso, default: 'No se pudo publicar el comentario. Intenta de nuevo.' },
  })
}

export function editarComentario(tableroId, comentarioId, texto) {
  return enviar(url(tableroId, comentarioId), {
    method: 'PATCH',
    json: { texto },
    mensajes: {
      400: invalido,
      403: 'Solo el autor puede editar este comentario.',
      404: 'Este comentario ya no existe.',
      default: 'No se pudo guardar el cambio. Intenta de nuevo.',
    },
  })
}

export function eliminarComentario(tableroId, comentarioId) {
  return enviar(url(tableroId, comentarioId), {
    method: 'DELETE',
    mensajes: {
      403: 'Solo el autor o el propietario del tablero pueden eliminar este comentario.',
      404: 'Este comentario ya no existe.',
      default: 'No se pudo eliminar el comentario. Intenta de nuevo.',
    },
  })
}
