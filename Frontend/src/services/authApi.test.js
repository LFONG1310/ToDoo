import { afterEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { cerrarSesion, iniciarSesion, obtenerSesion } from './authApi.js'

const originalFetch = globalThis.fetch
afterEach(() => { globalThis.fetch = originalFetch })

test('inicia sesion enviando las credenciales como JSON', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/auth/login')
    assert.equal(options.method, 'POST')
    assert.deepEqual(JSON.parse(options.body), { correo: 'luis@todoo.dev', contrasena: 'x' })
    return Response.json({ id: 1, nombre: 'Luis' })
  }
  assert.equal((await iniciarSesion('luis@todoo.dev', 'x')).nombre, 'Luis')
})

test('muestra un mensaje claro si las credenciales son incorrectas', async () => {
  globalThis.fetch = async () => new Response(null, { status: 401 })
  await assert.rejects(iniciarSesion('a@b.c', 'mal'), { status: 401, message: 'Correo o contraseña incorrectos.' })
})

test('sin sesion devuelve null en lugar de fallar', async () => {
  globalThis.fetch = async () => new Response(null, { status: 401 })
  assert.equal(await obtenerSesion(), null)
  globalThis.fetch = async () => new Response(null, { status: 503 })
  await assert.rejects(obtenerSesion(), { status: 503 })
})

test('al cerrar sesion pide un token nuevo la siguiente vez', async () => {
  const urls = []
  globalThis.fetch = async (url) => {
    urls.push(url)
    if (url === '/api/csrf') return Response.json({ token: 't' })
    return new Response(null, { status: 204 })
  }
  await cerrarSesion()
  await cerrarSesion()
  assert.deepEqual(urls, ['/api/csrf', '/api/auth/logout', '/api/csrf', '/api/auth/logout'])
})
