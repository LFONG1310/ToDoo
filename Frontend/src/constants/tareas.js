// Mismos valores que la columna tareas.prioridad de propuesta_mysql.sql.
export const PRIORIDADES = [
  { id: 'alta', name: 'Alta', className: 'bg-red-100 text-red-800' },
  { id: 'media', name: 'Media', className: 'bg-amber-100 text-amber-800' },
  { id: 'baja', name: 'Baja', className: 'bg-green-100 text-green-800' },
]

export const PRIORIDAD_POR_DEFECTO = 'media'

// TEMPORAL: sin login ni API de usuarios, el equipo y el usuario actual son fijos.
// Reemplazar por GET /api/proyectos/{id}/responsables y el usuario de la sesión.
export const MIEMBROS_EQUIPO = [
  { id: 1, nombre: 'Yo' },
  { id: 2, nombre: 'Compañero 1' },
  { id: 3, nombre: 'Compañero 2' },
]

export const USUARIO_ACTUAL_ID = 1
