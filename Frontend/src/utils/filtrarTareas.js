// Misma semántica que GET /api/proyectos/{id}/tareas: los filtros vacíos no restringen,
// y "solo mis tareas" con otro responsable elegido no devuelve nada.
export const FILTROS_VACIOS = { consulta: '', prioridad: '', responsableId: null, soloMias: false }

export function hayFiltrosActivos(filtros) {
  return Boolean(filtros.consulta.trim() || filtros.prioridad || filtros.responsableId || filtros.soloMias)
}

export function filtrarTareas(tareas, filtros, usuarioActualId) {
  const consulta = filtros.consulta.trim().toLocaleLowerCase('es')
  const responsableId = filtros.soloMias ? usuarioActualId : filtros.responsableId
  if (filtros.soloMias && filtros.responsableId && filtros.responsableId !== usuarioActualId) return []

  return tareas.filter((tarea) => (
    (!filtros.prioridad || tarea.prioridad === filtros.prioridad) &&
    (!responsableId || tarea.responsableId === responsableId) &&
    (!consulta ||
      tarea.title.toLocaleLowerCase('es').includes(consulta) ||
      tarea.description.toLocaleLowerCase('es').includes(consulta))
  ))
}
