import { request } from './http.js'

export function listarTableros(signal) {
  return request('/api/tableros', {
    signal,
    mensajes: { default: 'No se pudieron cargar tus tableros. Intenta de nuevo.' },
  })
}

export function obtenerTablero(tableroId, signal) {
  return request(`/api/tableros/${encodeURIComponent(tableroId)}`, {
    signal,
    mensajes: {
      400: 'Este tablero no existe o no formas parte de él.',
      404: 'Este tablero no existe o no formas parte de él.',
      default: 'No se pudo cargar el tablero. Intenta de nuevo.',
    },
  })
}
