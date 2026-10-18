

export default function TareaCard({ tarea }){
  return (
    <article className="rounded-lg border border-gray-200 bg-gray-50 p-3">
      <h3 className="font-medium wrap-break-word text-gray-900">{tarea.title}</h3>
      {tarea.description && (
        <p className="mt-2 whitespace-pre-wrap wrap-break-word text-sm text-gray-600">
          {tarea.description}
        </p>
      )}
    </article>
  );
}
