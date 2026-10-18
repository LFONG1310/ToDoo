import NavBar from '../../components/Nav/NavBar'
import { useState } from 'react'
import TareaCard from '../../components/Tareas/TareaCard'
import TareaForm from '../../components/Tareas/TareaForm'

const columns = [
  { id: 'todo', name: 'Por hacer' },
  { id: 'in-progress', name: 'En progreso' },
  { id: 'done', name: 'Hecho' },
]

export default function KanbanPage() {
  const [tareas, setTareas] = useState([])

  function crearTarea(datos) {
    setTareas((actuales) => [
      ...actuales,
      { ...datos, id: crypto.randomUUID(), state: 'todo' },
    ])
  }

  function cambiarEstado(tareaId, nuevoEstado) {
    if (!columns.some((column) => column.id === nuevoEstado)) return

    setTareas((actuales) => actuales.map((tarea) => (
      tarea.id === tareaId ? { ...tarea, state: nuevoEstado } : tarea
    )))
  }

  function editarTarea(tareaId, { title, description }) {
    setTareas((actuales) => actuales.map((tarea) => (
      tarea.id === tareaId ? { ...tarea, title, description } : tarea
    )))
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <NavBar />
      <main className="mx-auto max-w-7xl p-4 sm:p-6">
        <h1 className="mb-6 text-2xl font-bold text-blue-800">Mi tablero de tareas</h1>
        <TareaForm onCrear={crearTarea} />
        <div className="grid grid-cols-1 items-start gap-4 md:grid-cols-3">
          {columns.map((column) => {
            const tareasColumna = tareas.filter((tarea) => tarea.state === column.id)
            return (
            <section
              key={column.id}
              aria-labelledby={`column-${column.id}`}
              className="min-h-64 rounded-xl bg-white p-4 shadow-sm"
            >
              <header className="flex items-center justify-between gap-2">
                <h2 id={`column-${column.id}`} className="font-semibold text-blue-800">
                  {column.name}
                </h2>
                <span aria-label={`${tareasColumna.length} tareas`} className="rounded-full bg-blue-100 px-2 py-1 text-xs text-blue-800">
                  {tareasColumna.length}
                </span>
              </header>
              {tareasColumna.length === 0 ? <p className="py-8 text-center text-sm text-gray-500">
                No hay tareas en esta columna.
              </p> : (
                <div className="mt-4 flex flex-col gap-3">
                  {tareasColumna.map((tarea) => (
                    <TareaCard
                      key={tarea.id}
                      tarea={tarea}
                      estados={columns}
                      onCambiarEstado={cambiarEstado}
                      onEditar={editarTarea}
                    />
                  ))}
                </div>
              )}
            </section>
            )
          })}
        </div>
      </main>
    </div>
  )
}
