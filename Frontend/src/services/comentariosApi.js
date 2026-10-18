import { enviar, request } from './http.js'

export const MAX_COMENTARIO = 1000

// Cuenta caracteres reales (un emoji cuenta como 1), igual que el backend y MySQL.
export function contarCaracteres(texto) {
  return [...texto].length
}

const url = (proyectoId, comentarioId) => {
  const base = `/api/proyectos/${encodeURIComponent(proyectoId)}/comentarios`
  return comentarioId === undefined ? base : `${base}/${encodeURIComponent(comentarioId)}`
}

const sinAcceso = 'Este proyecto no existe o ya no formas parte de él.'
const invalido = `El comentario no puede estar vacío ni tener más de ${MAX_COMENTARIO} caracteres.`

export function listarComentarios(proyectoId, signal) {
  return request(url(proyectoId), {
    signal,
    mensajes: { 404: sinAcceso, default: 'No se pudo cargar la conversación. Intenta de nuevo.' },
  })
}

export function publicarComentario(proyectoId, contenido) {
  return enviar(url(proyectoId), {
    method: 'POST',
    json: { contenido },
    mensajes: { 400: invalido, 404: sinAcceso, default: 'No se pudo publicar el comentario. Intenta de nuevo.' },
  })
}

export function editarComentario(proyectoId, comentarioId, contenido) {
  return enviar(url(proyectoId, comentarioId), {
    method: 'PATCH',
    json: { contenido },
    mensajes: {
      400: invalido,
      403: 'Solo el autor puede editar este comentario.',
      404: 'Este comentario ya no existe.',
      default: 'No se pudo guardar el cambio. Intenta de nuevo.',
    },
  })
}

export function eliminarComentario(proyectoId, comentarioId) {
  return enviar(url(proyectoId, comentarioId), {
    method: 'DELETE',
    mensajes: {
      403: 'Solo el autor o un administrador del proyecto pueden eliminar este comentario.',
      404: 'Este comentario ya no existe.',
      default: 'No se pudo eliminar el comentario. Intenta de nuevo.',
    },
  })
}
