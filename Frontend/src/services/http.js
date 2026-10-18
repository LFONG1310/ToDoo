// Cliente HTTP compartido: envia la cookie de sesion, pide JSON y agrega el token CSRF
// en las peticiones que modifican datos.

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

const mensajesGenerales = {
  0: 'No se pudo conectar con el servidor. Intenta de nuevo.',
  401: 'Tu sesión terminó. Inicia sesión de nuevo.',
  403: 'Tu sesión necesita actualizarse. Recarga la página.',
  503: 'El servidor no pudo conectarse a la base de datos. Intenta de nuevo.',
}
const mensajePorDefecto = 'No se pudo completar la acción. Intenta de nuevo.'

export async function request(url, { mensajes = {}, json, headers, ...options } = {}) {
  let response
  try {
    response = await fetch(url, {
      ...options,
      credentials: 'same-origin',
      cache: 'no-store',
      headers: {
        Accept: 'application/json',
        ...(json === undefined ? {} : { 'Content-Type': 'application/json' }),
        ...headers,
      },
      body: json === undefined ? options.body : JSON.stringify(json),
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new ApiError(mensajes[0] ?? mensajesGenerales[0], 0)
  }
  if (!response.ok) {
    const { status } = response
    throw new ApiError(mensajes[status] ?? mensajesGenerales[status] ?? mensajes.default ?? mensajePorDefecto, status)
  }
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

let csrfPendiente = null

export function obtenerCsrf() {
  csrfPendiente ??= request('/api/csrf')
    .then(({ token }) => token)
    .catch((error) => {
      csrfPendiente = null
      throw error
    })
  return csrfPendiente
}

// El token cambia con cada sesion; se olvida al iniciar o cerrar sesion.
export function olvidarCsrf() {
  csrfPendiente = null
}

// POST/PATCH/DELETE con token CSRF. Si el servidor responde 403 se pide un token nuevo
// y se reintenta una sola vez (por ejemplo, despues de que la sesion se renovo).
export async function enviar(url, { method = 'POST', mensajes, ...options } = {}) {
  const intentar = async () => request(url, {
    ...options,
    method,
    mensajes,
    headers: { 'X-CSRF-TOKEN': await obtenerCsrf() },
  })
  try {
    return await intentar()
  } catch (error) {
    if (error.status !== 403) throw error
    olvidarCsrf()
    return intentar()
  }
}
