import { enviar, olvidarCsrf, request } from './http.js'

export async function iniciarSesion(correo, contrasena) {
  olvidarCsrf()
  return request('/api/auth/login', {
    method: 'POST',
    json: { correo, contrasena },
    mensajes: {
      400: 'Escribe tu correo y tu contraseña.',
      401: 'Correo o contraseña incorrectos.',
    },
  })
}

// Devuelve el usuario de la sesion actual, o null si no hay sesion.
export async function obtenerSesion(signal) {
  try {
    return await request('/api/auth/sesion', { signal })
  } catch (error) {
    if (error.status === 401) return null
    throw error
  }
}

export async function cerrarSesion() {
  try {
    await enviar('/api/auth/logout')
  } finally {
    olvidarCsrf()
  }
}
