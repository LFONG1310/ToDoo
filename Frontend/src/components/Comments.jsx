import { useEffect, useState } from 'react'
import { addComment, getComments } from '../services/commentsApi.js'

export default function Comments({ taskId }) {
  const [comments, setComments] = useState([])
  const [text, setText] = useState('')
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    getComments(taskId, controller.signal)
      .then((data) => {
        setComments(data)
        setError('')
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err.message)
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [taskId])

  const handleSubmit = async (e) => {
    e.preventDefault()
    const clean = text.trim()
    if (!clean || sending) return
    setSending(true)
    try {
      await addComment(taskId, clean)
      setComments(await getComments(taskId))
      setText('')
      setError('')
    } catch (err) {
      setError(err.message)
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="comments">
      <h4>Comentarios ({comments.length})</h4>

      {loading && <p>Cargando comentarios...</p>}
      {error && <p role="alert">{error}</p>}
      {!loading && !error && comments.length === 0 && <p>Aún no hay comentarios.</p>}

      <ul>
        {comments.map((c) => (
          <li key={c.id}>
            <strong>Usuario {c.usuario_id}</strong>{' '}
            <small>{new Date(c.fecha_creacion).toLocaleString()}</small>
            <p>{c.texto}</p>
          </li>
        ))}
      </ul>

      <form onSubmit={handleSubmit}>
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Escribe un comentario..."
          maxLength={1000}
        />
        <button type="submit" disabled={sending}>Comentar</button>
      </form>
    </div>
  )
}