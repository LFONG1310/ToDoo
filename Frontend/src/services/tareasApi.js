// Cliente de GET /api/proyectos/{id}/tareas y /responsables (Backend/main, paquete tareas).
// Aún no se usa en el tablero: las tareas siguen en localStorage hasta que existan
// login, proyectos en MySQL y la API para crear/editar tareas.

class TareasApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'TareasApiError'
    this.status = status
  }
}

async function request(path, signal) {
  let response
  try {
    response = await fetch(path, {
      signal,
      credentials: 'same-origin',
      cache: 'no-store',
      headers: { Accept: 'application/json' },
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new TareasApiError('No se pudo conectar con el servidor. Intenta de nuevo.', 0)
  }
  if (!response.ok) {
    const message = response.status === 401
      ? 'Inicia sesion para consultar las tareas.'
      : response.status === 404
        ? 'El proyecto no existe o no perteneces a él.'
        : response.status === 400
          ? 'Los filtros seleccionados no son validos.'
          : 'No se pudieron consultar las tareas. Intenta de nuevo.'
    throw new TareasApiError(message, response.status)
  }
  return response.json()
}

// La búsqueda de texto se aplica en el navegador; el backend solo recibe estos filtros.
export function consultaFiltros({ prioridad, responsableId, soloMias }) {
  const params = new URLSearchParams()
  if (prioridad) params.set('prioridad', prioridad)
  if (soloMias) params.set('soloMias', 'true')
  else if (responsableId) params.set('responsableId', String(responsableId))
  const consulta = params.toString()
  return consulta ? `?${consulta}` : ''
}

// Convierte la respuesta del backend al formato que usan TareaCard y filtrarTareas.
export function adaptarTarea(tarea) {
  return {
    id: String(tarea.id),
    title: tarea.titulo,
    description: tarea.descripcion,
    state: tarea.estado,
    prioridad: tarea.prioridad,
    responsableId: tarea.responsable?.id ?? null,
    responsableNombre: tarea.responsable?.nombre ?? '',
    fechaLimite: tarea.fecha_limite,
  }
}

export async function getTareas(proyectoId, filtros, signal) {
  const tareas = await request(`/api/proyectos/${proyectoId}/tareas${consultaFiltros(filtros)}`, signal)
  return tareas.map(adaptarTarea)
}

export function getResponsables(proyectoId, signal) {
  return request(`/api/proyectos/${proyectoId}/responsables`, signal)
}
