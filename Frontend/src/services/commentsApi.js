const csrfUrl = '/api/notificaciones/csrf'

class CommentsApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'CommentsApiError'
    this.status = status
  }
}

async function request(url, options = {}) {
  let response
  try {
    response = await fetch(url, {
      ...options,
      credentials: 'same-origin',
      cache: 'no-store',
      headers: { Accept: 'application/json', ...options.headers },
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new CommentsApiError('No se pudo conectar con el servidor. Intenta de nuevo.', 0)
  }
  if (!response.ok) {
    const message = response.status === 400
      ? 'El comentario no es valido o es demasiado largo.'
      : response.status === 401
        ? 'Inicia sesion para ver y escribir comentarios.'
        : response.status === 403
          ? 'Tu sesion necesita actualizarse. Recarga la pagina.'
          : 'No se pudieron guardar o consultar los comentarios. Intenta de nuevo.'
    throw new CommentsApiError(message, response.status)
  }
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

export function getComments(taskId, signal) {
  return request(`/api/tareas/${taskId}/comentarios`, { signal })
}

export async function addComment(taskId, texto) {
  const { token } = await request(csrfUrl)
  return request(`/api/tareas/${taskId}/comentarios`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': token },
    body: JSON.stringify({ texto }),
  })
}