import { PRIORIDAD_POR_DEFECTO, PRIORIDADES } from '../constants/tareas.js'

const STORAGE_KEY = 'todoo.kanban.tareas'
const ESTADOS = new Set(['todo', 'in-progress', 'done'])
const PRIORIDADES_VALIDAS = new Set(PRIORIDADES.map((prioridad) => prioridad.id))

// Las tareas guardadas antes de los filtros no tienen prioridad ni responsable.
function completarCampos(tarea) {
  if (!tarea || typeof tarea !== 'object') return tarea
  return {
    ...tarea,
    prioridad: tarea.prioridad ?? PRIORIDAD_POR_DEFECTO,
    responsableId: tarea.responsableId ?? null,
  }
}

export function cargarTablero(storage) {
  try {
    const guardadas = JSON.parse((storage ?? localStorage).getItem(STORAGE_KEY) ?? '[]')
    if (!Array.isArray(guardadas)) throw new Error('Formato de tablero inválido')

    const ids = new Set()
    const tareas = guardadas.map(completarCampos).filter((tarea) => {
      const valida = tarea &&
        typeof tarea.id === 'string' && tarea.id.length > 0 && !ids.has(tarea.id) &&
        typeof tarea.title === 'string' && tarea.title.trim().length > 0 &&
        tarea.title.length <= 100 &&
        typeof tarea.description === 'string' && tarea.description.length <= 500 &&
        ESTADOS.has(tarea.state) &&
        PRIORIDADES_VALIDAS.has(tarea.prioridad) &&
        (tarea.responsableId === null || (Number.isInteger(tarea.responsableId) && tarea.responsableId > 0))

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
