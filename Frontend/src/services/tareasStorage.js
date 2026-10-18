const STORAGE_KEY = 'todoo.kanban.tareas'
const ESTADOS = new Set(['todo', 'in-progress', 'done'])

export function cargarTablero(storage) {
  try {
    const guardadas = JSON.parse((storage ?? localStorage).getItem(STORAGE_KEY) ?? '[]')
    if (!Array.isArray(guardadas)) throw new Error('Formato de tablero inválido')

    const ids = new Set()
    const tareas = guardadas.filter((tarea) => {
      const valida = tarea &&
        typeof tarea.id === 'string' && tarea.id.length > 0 && !ids.has(tarea.id) &&
        typeof tarea.title === 'string' && tarea.title.trim().length > 0 &&
        tarea.title.length <= 100 &&
        typeof tarea.description === 'string' && tarea.description.length <= 500 &&
        ESTADOS.has(tarea.state)

      if (valida) ids.add(tarea.id)
      return valida
    })

    return {
      tareas,
      error: tareas.length === guardadas.length ? '' :
        'Se recuperaron solo las tareas válidas. Los datos guardados no cambiarán hasta que modifiques el tablero.',
    }
  } catch {
    return {
      tareas: [],
      error: 'No se pudieron cargar las tareas. Los datos guardados no cambiarán hasta que modifiques el tablero.',
    }
  }
}

export function guardarTareas(tareas, storage) {
  try {
    (storage ?? localStorage).setItem(STORAGE_KEY, JSON.stringify(tareas))
    return ''
  } catch {
    return 'No se pudieron guardar las tareas. Tus cambios están solo en esta pestaña; reintenta antes de salir.'
  }
}
