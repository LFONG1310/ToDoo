import { useState } from 'react'

export default function TareaForm({ onCrear }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')

  function handleSubmit(event) {
    event.preventDefault()
    if (!title.trim()) {
      setError('Escribe un título para la tarea.')
      return
    }

    onCrear({ title: title.trim(), description: description.trim() })
    setTitle('')
    setDescription('')
    setError('')
  }

  return (
    <form onSubmit={handleSubmit} className="mb-6 rounded-xl bg-white p-4 shadow-sm">
      <h2 className="mb-3 font-semibold text-blue-800">Nueva tarea</h2>
      <label htmlFor="tarea-title" className="block text-sm font-medium text-gray-700">
        Título
      </label>
      <input
        id="tarea-title"
        value={title}
        onChange={(event) => { setTitle(event.target.value); setError('') }}
        maxLength={100}
        required
        aria-invalid={Boolean(error)}
        aria-describedby={error ? 'tarea-error' : undefined}
        className="mt-1 w-full rounded-lg border border-gray-300 p-2 focus:outline-blue-700"
      />
      {error && <p id="tarea-error" role="alert" className="mt-1 text-sm text-red-700">{error}</p>}
      <label htmlFor="tarea-description" className="mt-3 block text-sm font-medium text-gray-700">
        Descripción (opcional)
      </label>
      <textarea
        id="tarea-description"
        value={description}
        onChange={(event) => setDescription(event.target.value)}
        maxLength={500}
        rows={3}
        className="mt-1 w-full rounded-lg border border-gray-300 p-2 focus:outline-blue-700"
      />
      <button type="submit" className="mt-3 rounded-lg bg-blue-700 px-4 py-2 font-medium text-white hover:bg-blue-800">
        Agregar tarea
      </button>
    </form>
  )
}
