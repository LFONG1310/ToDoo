import { PRIORIDADES } from '../../constants/tareas'
import { FILTROS_VACIOS, hayFiltrosActivos } from '../../utils/filtrarTareas'

export default function FiltrosTablero({ filtros, miembros, onCambiar }) {
  function cambiar(campo, valor) {
    onCambiar({ ...filtros, [campo]: valor })
  }

  return (
    <div role="search" aria-label="Filtrar tareas" className="mb-6 flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-4 lg:flex-row lg:items-end">
      <div className="flex-1">
        <label htmlFor="buscar-tareas" className="mb-1 block text-sm font-medium text-gray-700">
          Buscar tareas
        </label>
        <input
          id="buscar-tareas"
          type="search"
          value={filtros.consulta}
          onChange={(event) => cambiar('consulta', event.target.value)}
          placeholder="Buscar por título o descripción"
          className="w-full rounded-lg border border-gray-300 bg-white p-2 focus:outline-blue-700"
        />
      </div>
      <div>
        <label htmlFor="filtro-prioridad" className="mb-1 block text-sm font-medium text-gray-700">
          Prioridad
        </label>
        <select
          id="filtro-prioridad"
          value={filtros.prioridad}
          onChange={(event) => cambiar('prioridad', event.target.value)}
          className="w-full rounded-lg border border-gray-300 bg-white p-2 focus:outline-blue-700"
        >
          <option value="">Todas</option>
          {PRIORIDADES.map((opcion) => <option key={opcion.id} value={opcion.id}>{opcion.name}</option>)}
        </select>
      </div>
      <div>
        <label htmlFor="filtro-responsable" className="mb-1 block text-sm font-medium text-gray-700">
          Responsable
        </label>
        <select
          id="filtro-responsable"
          value={filtros.responsableId ?? ''}
          onChange={(event) => cambiar('responsableId', event.target.value ? Number(event.target.value) : null)}
          disabled={filtros.soloMias}
          className="w-full rounded-lg border border-gray-300 bg-white p-2 focus:outline-blue-700 disabled:cursor-not-allowed disabled:bg-gray-100"
        >
          <option value="">Todos</option>
          {miembros.map((miembro) => <option key={miembro.id} value={miembro.id}>{miembro.nombre}</option>)}
        </select>
      </div>
      <label className="flex items-center gap-2 py-2 text-sm font-medium text-gray-700">
        <input
          type="checkbox"
          checked={filtros.soloMias}
          // Al marcarlo se descarta el responsable elegido para no combinar filtros contradictorios.
          onChange={(event) => onCambiar({ ...filtros, soloMias: event.target.checked, responsableId: null })}
          className="h-4 w-4 accent-blue-700"
        />
        Solo mis tareas
      </label>
      {hayFiltrosActivos(filtros) && (
        <button
          type="button"
          onClick={() => onCambiar(FILTROS_VACIOS)}
          className="rounded-lg px-3 py-2 text-sm font-semibold text-blue-700 hover:bg-blue-50"
        >
          Limpiar filtros
        </button>
      )}
    </div>
  )
}
