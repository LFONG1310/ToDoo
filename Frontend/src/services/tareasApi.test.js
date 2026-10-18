import { afterEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { consultaFiltros, getResponsables, getTareas } from './tareasApi.js'

const originalFetch = globalThis.fetch
afterEach(() => { globalThis.fetch = originalFetch })

test('traduce los filtros del tablero a los parametros del backend', () => {
  assert.equal(consultaFiltros({ prioridad: '', responsableId: null, soloMias: false }), '')
  assert.equal(consultaFiltros({ prioridad: 'alta', responsableId: 2, soloMias: false }), '?prioridad=alta&responsableId=2')
  // "Solo mis tareas" lo resuelve el backend con la sesión, sin enviar un id.
  assert.equal(consultaFiltros({ prioridad: '', responsableId: 2, soloMias: true }), '?soloMias=true')
})

test('consulta las tareas filtradas con sesion y las adapta al tablero', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/proyectos/1/tareas?prioridad=baja')
    assert.equal(options.credentials, 'same-origin')
    return Response.json([{
      id: 7, proyecto_id: 1, titulo: 'Probar filtros', descripcion: '', prioridad: 'baja',
      estado: 'done', posicion: 0, responsable: { id: 2, nombre: 'Beto' }, fecha_limite: '2026-11-01T12:00:00Z',
    }])
  }
  assert.deepEqual(await getTareas(1, { prioridad: 'baja' }), [{
    id: '7', title: 'Probar filtros', description: '', state: 'done', prioridad: 'baja',
    responsableId: 2, responsableNombre: 'Beto', fechaLimite: '2026-11-01T12:00:00Z',
  }])
})

test('muestra un mensaje claro si el proyecto no es accesible', async () => {
  globalThis.fetch = async () => new Response(null, { status: 404 })
  await assert.rejects(getResponsables(9), { status: 404, message: 'El proyecto no existe o no perteneces a él.' })
})
