import { Link } from 'react-router-dom'
import { ESTADOS_PROYECTO } from '../../constants/proyectos'
import { resumenProyecto } from '../../utils/resumenProyecto'

const ESTADOS_TAREA = {
  todo: 'Por hacer',
  'in-progress': 'En progreso',
  done: 'Hecho',
}

export default function ProyectoCard({ proyecto, tareas }) {
  const resumen = resumenProyecto(tareas)
  const estado = ESTADOS_PROYECTO[resumen.estado]

  return (
    <article aria-label={proyecto.name} className="flex min-w-0 flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
      <div className="h-2 bg-blue-700" />
      <div className="flex flex-1 flex-col p-5 sm:p-6">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <h2 className="text-xl font-bold wrap-break-word text-blue-900">{proyecto.name}</h2>
          <span className={`rounded-full px-3 py-1 text-xs font-semibold ${estado.className}`}>
            {estado.label}
          </span>
        </div>
        <p className="mt-3 text-sm leading-6 text-gray-600">{proyecto.description}</p>
        <div className="mt-6">
          <div className="flex justify-between gap-2 text-sm">
            <span className="font-medium text-gray-700">Avance del proyecto</span>
            <span className="font-semibold text-blue-800">{resumen.porcentaje}%</span>
          </div>
          <progress
            aria-label={`Avance de ${proyecto.name}`}
            value={resumen.hechas}
            max={resumen.total || 1}
            className="mt-2 h-2 w-full appearance-none overflow-hidden rounded-full [&::-webkit-progress-bar]:bg-blue-100 [&::-webkit-progress-value]:bg-blue-700 [&::-moz-progress-bar]:bg-blue-700"
          />
          <p className="mt-1 text-xs text-gray-500">{resumen.hechas} de {resumen.total} tareas completadas</p>
        </div>
        <dl className="my-5 grid grid-cols-3 gap-2 rounded-lg bg-gray-50 p-3 text-center">
          <div><dt className="text-xs text-gray-600">Por hacer</dt><dd className="mt-1 text-lg font-bold text-blue-800">{resumen.porHacer}</dd></div>
          <div><dt className="text-xs text-gray-600">En progreso</dt><dd className="mt-1 text-lg font-bold text-amber-800">{resumen.enProgreso}</dd></div>
          <div><dt className="text-xs text-gray-600">Hecho</dt><dd className="mt-1 text-lg font-bold text-green-800">{resumen.hechas}</dd></div>
        </dl>
        {resumen.total === 0 && <p className="mb-4 text-sm text-gray-500">Este proyecto aún no tiene tareas. Agrega la primera desde su tablero.</p>}
        {resumen.total > 0 && (
          <details className="mb-5 rounded-lg border border-gray-200">
            <summary className="cursor-pointer rounded-lg px-3 py-2 text-sm font-semibold text-blue-800 focus-visible:outline-2 focus-visible:outline-blue-700">
              Ver tareas ({resumen.total})
            </summary>
            <ul aria-label={`Tareas de ${proyecto.name}`} className="max-h-64 overflow-y-auto divide-y divide-gray-100 px-3">
              {tareas.map((tarea) => (
                <li key={tarea.id} className="py-3">
                  <p className="text-sm font-medium wrap-break-word text-gray-900">{tarea.title}</p>
                  <span className="text-xs text-gray-500">{ESTADOS_TAREA[tarea.state]}</span>
                  {tarea.description && <p className="mt-1 whitespace-pre-wrap text-xs wrap-break-word text-gray-600">{tarea.description}</p>}
                </li>
              ))}
            </ul>
          </details>
        )}
        <Link to={proyecto.boardUrl} className="mt-auto inline-block rounded-lg bg-blue-700 px-4 py-2.5 text-center text-sm font-semibold text-white hover:bg-blue-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700">
          Abrir tablero
        </Link>
      </div>
    </article>
  );
}
