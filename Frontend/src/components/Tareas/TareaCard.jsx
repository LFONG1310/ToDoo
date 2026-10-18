

export default function TareaCard({ tarea, estados, onCambiarEstado }){
  return (
    <article className="rounded-lg border border-gray-200 bg-gray-50 p-3">
      <h3 className="font-medium wrap-break-word text-gray-900">{tarea.title}</h3>
      {tarea.description && (
        <p className="mt-2 whitespace-pre-wrap wrap-break-word text-sm text-gray-600">
          {tarea.description}
        </p>
      )}
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
    </article>
  );
}
