// Sin tarjeta de referencia, la tarea queda al final de la columna.
export function moverTarea(tareas, tareaId, estado, antesDeId) {
  const tarea = tareas.find((actual) => actual.id === tareaId)
  if (!tarea || tareaId === antesDeId) return tareas

  if (antesDeId && !tareas.some((actual) => actual.id === antesDeId && actual.state === estado)) {
    return tareas
  }

  const restantes = tareas.filter((actual) => actual.id !== tareaId)
  const posicion = antesDeId
    ? restantes.findIndex((actual) => actual.id === antesDeId)
    : restantes.length
  restantes.splice(posicion, 0, { ...tarea, state: estado })
  return restantes
}
