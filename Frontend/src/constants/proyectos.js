// El frontend actual dispone de un único tablero compartido.
export const PROYECTO_PRINCIPAL = {
  id: 'principal',
  name: 'Mi tablero de tareas',
  description: 'Organiza tus tareas y sigue su avance desde Por hacer hasta Hecho.',
  boardUrl: '/kanban',
}

export const ESTADOS_PROYECTO = {
  'sin-tareas': { label: 'Sin tareas', className: 'bg-gray-100 text-gray-700' },
  'por-comenzar': { label: 'Por comenzar', className: 'bg-blue-100 text-blue-800' },
  'en-progreso': { label: 'En progreso', className: 'bg-amber-100 text-amber-800' },
  completado: { label: 'Completado', className: 'bg-green-100 text-green-800' },
}
