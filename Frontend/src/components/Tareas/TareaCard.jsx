import { useState } from 'react'
import TareaForm from './TareaForm'

export default function TareaCard({ tarea, estados, onCambiarEstado, onEditar, onEliminar }){
  const [editando, setEditando] = useState(false)
  const [confirmandoEliminar, setConfirmandoEliminar] = useState(false)

  if (editando) {
    return (
      <TareaForm
        tarea={tarea}
        onGuardar={(datos) => {
          onEditar(tarea.id, datos)
          setEditando(false)
        }}
        onCancelar={() => setEditando(false)}
      />
    )
  }

  return (
    <article className="rounded-lg border border-gray-200 bg-gray-50 p-3">
      <h3 className="font-medium wrap-break-word text-gray-900">{tarea.title}</h3>
      {tarea.description && (
        <p className="mt-2 whitespace-pre-wrap wrap-break-word text-sm text-gray-600">
          {tarea.description}
        </p>
      )}
      {confirmandoEliminar ? (
        <div className="mt-3 rounded-lg border border-red-200 bg-red-50 p-3">
          <p role="alert" className="text-sm text-red-800">
            ¿Eliminar esta tarea? Esta acción no se puede deshacer.
          </p>
          <div className="mt-3 flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => setConfirmandoEliminar(false)}
              className="rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={() => onEliminar(tarea.id)}
              className="rounded-lg bg-red-700 px-3 py-2 text-sm font-medium text-white hover:bg-red-800"
            >
              Confirmar eliminación
            </button>
          </div>
        </div>
      ) : (
        <>
      <label htmlFor={`estado-${tarea.id}`} className="mt-3 block text-sm font-medium text-gray-700">
        Estado
      </label>
      <select
        id={`estado-${tarea.id}`}
        value={tarea.state}
        onChange={(event) => onCambiarEstado(tarea.id, event.target.value)}
        className="mt-1 w-full rounded-lg border border-gray-300 bg-white p-2 text-sm focus:outline-blue-700"
      >
        {estados.map((estado) => (
          <option key={estado.id} value={estado.id}>{estado.name}</option>
        ))}
      </select>
      <button
        type="button"
        onClick={() => setEditando(true)}
        className="mt-3 rounded-lg px-3 py-2 text-sm font-medium text-blue-700 hover:bg-blue-100"
      >
        Editar tarea
      </button>
      <button
        type="button"
        onClick={() => setConfirmandoEliminar(true)}
        className="mt-3 rounded-lg px-3 py-2 text-sm font-medium text-red-700 hover:bg-red-100"
      >
        Eliminar tarea
      </button>
        </>
      )}
    </article>
  );
}
