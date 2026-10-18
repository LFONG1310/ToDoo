export function resumenProyecto(tareas) {
  const porHacer = tareas.filter((tarea) => tarea.state === 'todo').length
  const enProgreso = tareas.filter((tarea) => tarea.state === 'in-progress').length
  const hechas = tareas.filter((tarea) => tarea.state === 'done').length
  const total = porHacer + enProgreso + hechas
  const estado = total === 0 ? 'sin-tareas'
    : hechas === total ? 'completado'
    : enProgreso > 0 || hechas > 0 ? 'en-progreso'
    : 'por-comenzar'
  const porcentaje = total === 0 ? 0
    : hechas === total ? 100 : Math.min(99, Math.round(hechas / total * 100))

  return { total, porHacer, enProgreso, hechas, estado, porcentaje }
}
