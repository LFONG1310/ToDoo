import { afterEach, beforeEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { olvidarCsrf } from './http.js'
import {
  contarCaracteres,
  editarComentario,
  eliminarComentario,
  listarComentarios,
  publicarComentario,
} from './comentariosApi.js'

const originalFetch = globalThis.fetch
beforeEach(() => olvidarCsrf())
afterEach(() => { globalThis.fetch = originalFetch })

function servidor(responder) {
  const llamadas = []
  globalThis.fetch = async (url, options = {}) => {
    llamadas.push({ url, method: options.method ?? 'GET', options })
    if (url === '/api/csrf') return Response.json({ token: 'tok-1' })
    return responder(url, options, llamadas)
  }
  return llamadas
}

test('lee la conversacion del tablero con la sesion y sin cache', async () => {
  const llamadas = servidor(() => Response.json([{ id: 1, contenido: 'Hola' }]))
  assert.deepEqual(await listarComentarios(7), [{ id: 1, contenido: 'Hola' }])
  assert.equal(llamadas[0].url, '/api/proyectos/7/comentarios')
  assert.equal(llamadas[0].options.credentials, 'same-origin')
  assert.equal(llamadas[0].options.cache, 'no-store')
})

test('publica con el token CSRF y solo envia el texto', async () => {
  const llamadas = servidor((url, options) => {
    assert.equal(options.headers['X-CSRF-TOKEN'], 'tok-1')
    assert.equal(options.headers['Content-Type'], 'application/json')
    assert.deepEqual(JSON.parse(options.body), { contenido: 'Listo' })
    return Response.json({ id: 9, contenido: 'Listo' }, { status: 201 })
  })
  assert.equal((await publicarComentario(7, 'Listo')).id, 9)
  assert.deepEqual(llamadas.map((l) => `${l.method} ${l.url}`), ['GET /api/csrf', 'POST /api/proyectos/7/comentarios'])
})

test('reutiliza el token CSRF entre peticiones', async () => {
  const llamadas = servidor(() => Response.json({ id: 1 }))
  await publicarComentario(1, 'a')
  await editarComentario(1, 1, 'b')
  assert.equal(llamadas.filter((l) => l.url === '/api/csrf').length, 1)
  assert.equal(llamadas[2].method, 'PATCH')
  assert.equal(llamadas[2].url, '/api/proyectos/1/comentarios/1')
})

test('pide un token nuevo y reintenta una vez si el servidor responde 403', async () => {
  let intentos = 0
  const llamadas = servidor(() => {
    intentos += 1
    return intentos === 1 ? new Response(null, { status: 403 }) : new Response(null, { status: 204 })
  })
  assert.equal(await eliminarComentario(3, 4), null)
  assert.equal(llamadas.filter((l) => l.url === '/api/csrf').length, 2)
  assert.equal(intentos, 2)
})

test('explica por que no se puede editar un comentario ajeno', async () => {
  servidor(() => new Response(null, { status: 403 }))
  await assert.rejects(editarComentario(1, 2, 'x'), { status: 403, message: 'Solo el autor puede editar este comentario.' })
})

test('distingue un tablero sin acceso de una sesion vencida', async () => {
  servidor(() => new Response(null, { status: 404 }))
  await assert.rejects(listarComentarios(1), { status: 404, message: 'Este proyecto no existe o ya no formas parte de él.' })
  servidor(() => new Response(null, { status: 401 }))
  await assert.rejects(listarComentarios(1), { status: 401, message: 'Tu sesión terminó. Inicia sesión de nuevo.' })
})

test('no publica si no se pudo obtener el token', async () => {
  let publicaciones = 0
  globalThis.fetch = async (url) => {
    if (url === '/api/csrf') return new Response(null, { status: 503 })
    publicaciones += 1
    return Response.json({})
  }
  await assert.rejects(publicarComentario(1, 'x'), { status: 503 })
  assert.equal(publicaciones, 0)
})

test('reporta un fallo de conexion sin confundirlo con una cancelacion', async () => {
  globalThis.fetch = async () => { throw new TypeError('Network error') }
  await assert.rejects(listarComentarios(1), { status: 0 })
  globalThis.fetch = async () => { throw new DOMException('Cancelado', 'AbortError') }
  await assert.rejects(listarComentarios(1), { name: 'AbortError' })
})

test('cuenta caracteres como MySQL y no como unidades UTF-16', () => {
  assert.equal(contarCaracteres('hola'), 4)
  assert.equal('😀😀'.length, 4)
  assert.equal(contarCaracteres('😀😀'), 2)
})
