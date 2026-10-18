import { useEffect, useId, useRef, useState } from 'react'

export default function TareaForm({ onCrear, tarea, onGuardar, onCancelar }) {
  const formId = useId()
  const titleRef = useRef(null)
  const [title, setTitle] = useState(tarea?.title ?? '')
  const [description, setDescription] = useState(tarea?.description ?? '')
  const [error, setError] = useState('')

  useEffect(() => {
    if (tarea) titleRef.current?.focus()
  }, [tarea])

  function handleSubmit(event) {
    event.preventDefault()
    if (!title.trim()) {
      setError('Escribe un título para la tarea.')
      titleRef.current?.focus()
      return
    }

    const datos = { title: title.trim(), description: description.trim() }
    if (tarea) {
      onGuardar(datos)
      return
    }
    onCrear(datos)
    setTitle('')
    setDescription('')
    setError('')
    titleRef.current?.focus()
  }

  return (
    <form
      onSubmit={handleSubmit}
      onKeyDown={(event) => {
        if (tarea && event.key === 'Escape') {
          event.preventDefault()
          onCancelar()
        }
      }}
      className="mb-6 rounded-xl bg-white p-4 shadow-sm"
    >
      <h2 className="mb-3 font-semibold text-blue-800">{tarea ? 'Editar tarea' : 'Nueva tarea'}</h2>
      <label htmlFor={`${formId}-title`} className="block text-sm font-medium text-gray-700">
        Título
      </label>
      <input
        id={`${formId}-title`}
        ref={titleRef}
        value={title}
        onChange={(event) => { setTitle(event.target.value); setError('') }}
        maxLength={100}
        required
        aria-invalid={Boolean(error)}
        aria-describedby={error ? `${formId}-error` : undefined}
        className="mt-1 w-full rounded-lg border border-gray-300 p-2 focus:outline-blue-700"
      />
      {error && <p id={`${formId}-error`} role="alert" className="mt-1 text-sm text-red-700">{error}</p>}
      <label htmlFor={`${formId}-description`} className="mt-3 block text-sm font-medium text-gray-700">
        Descripción (opcional)
      </label>
      <textarea
        id={`${formId}-description`}
        value={description}
        onChange={(event) => setDescription(event.target.value)}
        maxLength={500}
        rows={3}
        className="mt-1 w-full rounded-lg border border-gray-300 p-2 focus:outline-blue-700"
      />
      <button type="submit" className="mt-3 rounded-lg bg-blue-700 px-4 py-2 font-medium text-white hover:bg-blue-800">
        {tarea ? 'Guardar cambios' : 'Agregar tarea'}
      </button>
      {tarea && (
        <button type="button" onClick={onCancelar} className="ml-2 rounded-lg px-4 py-2 font-medium text-gray-700 hover:bg-gray-100">
          Cancelar
        </button>
      )}
    </form>
  )
}
