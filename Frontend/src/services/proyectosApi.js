import { request } from './http.js'

const sinAcceso = 'Este proyecto no existe o no formas parte de él.'

export function listarProyectos(signal) {
  return request('/api/proyectos', {
    signal,
    mensajes: { default: 'No se pudieron cargar tus proyectos. Intenta de nuevo.' },
  })
}

// Detalle con mi_rol y los miembros activos.
export function obtenerProyecto(proyectoId, signal) {
  return request(`/api/proyectos/${encodeURIComponent(proyectoId)}`, {
    signal,
    mensajes: { 400: sinAcceso, 404: sinAcceso, default: 'No se pudo cargar el proyecto. Intenta de nuevo.' },
  })
}
