import NavBar from '../../components/Nav/NavBar'
import { useEffect, useState } from 'react'
import TareaCard from '../../components/Tareas/TareaCard'
import TareaForm from '../../components/Tareas/TareaForm'

const columns = [
  { id: 'todo', name: 'Por hacer' },
  { id: 'in-progress', name: 'En progreso' },
  { id: 'done', name: 'Hecho' },
]

const STORAGE_KEY = 'todoo.kanban.tareas'

function cargarTareas() {
  try {
    const guardadas = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]')
    if (!Array.isArray(guardadas)) return []

    const ids = new Set()
    return guardadas.filter((tarea) => {
      const valida = tarea &&
        typeof tarea.id === 'string' && tarea.id.length > 0 &&
        !ids.has(tarea.id) &&
        typeof tarea.title === 'string' && tarea.title.trim().length > 0 &&
        typeof tarea.description === 'string' &&
        columns.some((column) => column.id === tarea.state)

      if (valida) ids.add(tarea.id)
      return valida
    })
  } catch {
    return []
  }
}

export default function KanbanPage() {
  const [tareas, setTareas] = useState(cargarTareas)
  const [tareaArrastrada, setTareaArrastrada] = useState(null)
  const [columnaDestino, setColumnaDestino] = useState(null)
  const [busqueda, setBusqueda] = useState('')
  const consulta = busqueda.trim().toLocaleLowerCase('es')

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(tareas))
    } catch {
      console.warn('No se pudieron guardar las tareas en este navegador.')
    }
  }, [tareas])

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

  function eliminarTarea(tareaId) {
    setTareas((actuales) => actuales.filter((tarea) => tarea.id !== tareaId))
  }

  function finalizarArrastre() {
    setTareaArrastrada(null)
    setColumnaDestino(null)
  }

  function reordenarTarea(tareaId, vecinaId) {
    setTareas((actuales) => {
      const origen = actuales.findIndex((tarea) => tarea.id === tareaId)
      const destino = actuales.findIndex((tarea) => tarea.id === vecinaId)
      if (origen === -1 || destino === -1 ||
          actuales[origen].state !== actuales[destino].state) return actuales

      const ordenadas = [...actuales]
      ;[ordenadas[origen], ordenadas[destino]] = [ordenadas[destino], ordenadas[origen]]
      return ordenadas
    })
  }

  function soltarTarea(event, columnId) {
    if (!tareaArrastrada) return
    event.preventDefault()
    if (event.dataTransfer.getData('text/plain') === tareaArrastrada) {
      cambiarEstado(tareaArrastrada, columnId)
    }
    finalizarArrastre()
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <NavBar />
      <main className="mx-auto max-w-7xl p-4 sm:p-6">
        <h1 className="mb-6 text-2xl font-bold text-blue-800">Mi tablero de tareas</h1>
        <TareaForm onCrear={crearTarea} />
        <div className="mb-6">
          <label htmlFor="buscar-tareas" className="mb-1 block text-sm font-medium text-gray-700">
            Buscar tareas
          </label>
          <input
            id="buscar-tareas"
            type="search"
            value={busqueda}
            onChange={(event) => setBusqueda(event.target.value)}
            placeholder="Buscar por título o descripción"
            className="w-full rounded-lg border border-gray-300 bg-white p-2 focus:outline-blue-700"
          />
        </div>
        <div className="grid grid-cols-1 items-start gap-4 md:grid-cols-3">
          {columns.map((column) => {
            const tareasColumna = tareas.filter((tarea) => tarea.state === column.id)
            const tareasVisibles = tareasColumna.filter((tarea) => (
              tarea.title.toLocaleLowerCase('es').includes(consulta) ||
              tarea.description.toLocaleLowerCase('es').includes(consulta)
            ))
            const contador = consulta
              ? `${tareasVisibles.length} de ${tareasColumna.length}`
              : tareasColumna.length
            return (
            <section
              key={column.id}
              aria-labelledby={`column-${column.id}`}
              onDragOver={(event) => {
                if (!tareaArrastrada) return
                event.preventDefault()
                event.dataTransfer.dropEffect = 'move'
                setColumnaDestino(column.id)
              }}
              onDragLeave={(event) => {
                if (!event.currentTarget.contains(event.relatedTarget)) {
                  setColumnaDestino(null)
                }
              }}
              onDrop={(event) => soltarTarea(event, column.id)}
              className={`min-h-64 rounded-xl bg-white p-4 shadow-sm ${
                columnaDestino === column.id ? 'ring-2 ring-blue-500' : ''
              }`}
            >
              <header className="flex items-center justify-between gap-2">
                <h2 id={`column-${column.id}`} className="font-semibold text-blue-800">
                  {column.name}
                </h2>
                <span aria-label={`${contador} tareas`} className="rounded-full bg-blue-100 px-2 py-1 text-xs text-blue-800">
                  {contador}
                </span>
              </header>
              {tareasVisibles.length === 0 ? <p className="py-8 text-center text-sm text-gray-500">
                {consulta && tareasColumna.length > 0
                  ? 'No hay tareas que coincidan con la búsqueda.'
                  : 'No hay tareas en esta columna.'}
              </p> : (
                <div className="mt-4 flex flex-col gap-3">
                  {tareasVisibles.map((tarea, index) => (
                    <TareaCard
                      key={tarea.id}
                      tarea={tarea}
                      estados={columns}
                      onCambiarEstado={cambiarEstado}
                      onEditar={editarTarea}
                      onEliminar={eliminarTarea}
                      onIniciarArrastre={setTareaArrastrada}
                      onFinalizarArrastre={finalizarArrastre}
                      onSubir={index > 0
                        ? () => reordenarTarea(tarea.id, tareasVisibles[index - 1].id)
                        : undefined}
                      onBajar={index < tareasVisibles.length - 1
                        ? () => reordenarTarea(tarea.id, tareasVisibles[index + 1].id)
                        : undefined}
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
