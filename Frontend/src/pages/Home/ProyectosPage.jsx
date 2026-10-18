import NavBar from '../../components/Nav/NavBar'
import ProyectoCard from '../../components/Proyectos/ProyectoCard'
import { useState } from 'react'
import { cargarTablero } from '../../services/tareasStorage'
import { ESTADOS_PROYECTO, PROYECTO_PRINCIPAL } from '../../constants/proyectos'
import { resumenProyecto } from '../../utils/resumenProyecto'

export default function ProyectosPage() {
  const [tablero, setTablero] = useState(() => cargarTablero())
  const { tareas, error } = tablero
  const [actualizado, setActualizado] = useState(false)
  const [busqueda, setBusqueda] = useState('')
  const [estado, setEstado] = useState('todos')
  const proyectos = [{ ...PROYECTO_PRINCIPAL, tareas }]
  const resumen = resumenProyecto(tareas)
  const consulta = busqueda.trim().toLocaleLowerCase('es')
  const visibles = proyectos.filter((proyecto) => (
    `${proyecto.name} ${proyecto.description}`.toLocaleLowerCase('es').includes(consulta) &&
    (estado === 'todos' || resumenProyecto(proyecto.tareas).estado === estado)
  ))

  function limpiarFiltros() {
    setBusqueda('')
    setEstado('todos')
  }

  function actualizarProyectos() {
    setTablero(cargarTablero())
    setActualizado(true)
  }
  return(
    <div className="min-h-screen bg-gray-100">
      <NavBar />
      <main className="mx-auto max-w-7xl p-4 sm:p-6">
        <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-bold text-blue-800">Proyectos</h1>
          <button type="button" onClick={actualizarProyectos} className="rounded-lg border border-blue-200 bg-white px-4 py-2 text-sm font-semibold text-blue-700 hover:bg-blue-50">
            Actualizar proyectos
          </button>
        </div>
        <p className="mb-6 text-gray-600">Consulta el avance de tus tareas y accede al tablero del proyecto.</p>
        {error && <p role="alert" className="mb-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-900">{error}</p>}
        <dl aria-label="Resumen de proyectos" className="mb-6 grid grid-cols-2 gap-3 lg:grid-cols-4">
          {[
            ['Proyectos', proyectos.length],
            ['Tareas', resumen.total],
            ['Completadas', resumen.hechas],
            ['Pendientes', resumen.porHacer + resumen.enProgreso],
          ].map(([label, cantidad]) => (
            <div key={label} className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
              <dt className="text-sm text-gray-600">{label}</dt>
              <dd className="mt-1 text-2xl font-bold text-blue-800">{cantidad}</dd>
            </div>
          ))}
        </dl>
        <div className="mb-4 flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-4 sm:flex-row sm:items-end">
          <div className="flex-1">
            <label htmlFor="buscar-proyectos" className="mb-1 block text-sm font-medium text-gray-700">Buscar proyectos</label>
            <input id="buscar-proyectos" type="search" value={busqueda} onChange={(event) => setBusqueda(event.target.value)} placeholder="Nombre o descripción del proyecto" className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:outline-blue-700" />
          </div>
          <div>
            <label htmlFor="estado-proyectos" className="mb-1 block text-sm font-medium text-gray-700">Estado del proyecto</label>
            <select id="estado-proyectos" value={estado} onChange={(event) => setEstado(event.target.value)} className="w-full rounded-lg border border-gray-300 bg-white p-2.5 text-sm focus:outline-blue-700">
              <option value="todos">Todos los estados</option>
              {Object.entries(ESTADOS_PROYECTO).map(([id, opcion]) => <option key={id} value={id}>{opcion.label}</option>)}
            </select>
          </div>
          {(busqueda || estado !== 'todos') && (
            <button type="button" onClick={limpiarFiltros} className="rounded-lg px-3 py-2.5 text-sm font-semibold text-blue-700 hover:bg-blue-50">Limpiar filtros</button>
          )}
        </div>
        <p role="status" className="mb-4 text-sm text-gray-600">
          Mostrando {visibles.length} de {proyectos.length} {proyectos.length === 1 ? 'proyecto' : 'proyectos'}.
          {actualizado && !error && ' Resumen actualizado.'}
        </p>
        {visibles.length > 0 ? (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
            {visibles.map((proyecto) => <ProyectoCard key={proyecto.id} proyecto={proyecto} tareas={proyecto.tareas} />)}
          </div>
        ) : (
          <div className="rounded-xl border border-dashed border-gray-300 bg-white px-5 py-12 text-center">
            <h2 className="font-semibold text-gray-800">No hay proyectos que coincidan</h2>
            <p className="mt-2 text-sm text-gray-600">Prueba otro nombre o selecciona un estado diferente.</p>
            <button type="button" onClick={limpiarFiltros} className="mt-4 rounded-lg bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800">Mostrar todos los proyectos</button>
          </div>
        )}
      </main>
    </div>
  )
}
