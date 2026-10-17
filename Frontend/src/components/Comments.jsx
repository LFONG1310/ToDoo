import { useState } from 'react'

export default function Comments({ taskId, author = 'Usuario' }) {
  const storageKey = `todoo-comments-${taskId}`

  const [comments, setComments] = useState(() => {
    const saved = localStorage.getItem(storageKey)
    return saved ? JSON.parse(saved) : []
  })
  const [text, setText] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    const clean = text.trim()
    if (!clean) return

    const newComment = {
      id: crypto.randomUUID(),
      author,
      text: clean,
      createdAt: new Date().toISOString(),
    }

    const updated = [...comments, newComment]
    setComments(updated)
    localStorage.setItem(storageKey, JSON.stringify(updated))
    setText('')
  }

  return (
    <div className="comments">
      <h4>Comentarios ({comments.length})</h4>

      {comments.length === 0 && <p>Aún no hay comentarios.</p>}

      <ul>
        {comments.map((c) => (
          <li key={c.id}>
            <strong>{c.author}</strong>{' '}
            <small>{new Date(c.createdAt).toLocaleString()}</small>
            <p>{c.text}</p>
          </li>
        ))}
      </ul>

      <form onSubmit={handleSubmit}>
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Escribe un comentario..."
        />
        <button type="submit">Comentar</button>
      </form>
    </div>
  )
}