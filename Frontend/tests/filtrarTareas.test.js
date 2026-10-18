import test from 'node:test'
import assert from 'node:assert/strict'
import { FILTROS_VACIOS, filtrarTareas, hayFiltrosActivos } from '../src/utils/filtrarTareas.js'

const tarea = (id, prioridad, responsableId, title = `Tarea ${id}`) => (
  { id, title, description: '', state: 'todo', prioridad, responsableId }
)
const tareas = [
  tarea('1', 'alta', 1, 'Diseñar login'),
  tarea('2', 'media', 2),
  tarea('3', 'baja', 1),
  tarea('4', 'alta', 2),
  tarea('5', 'alta', null),
]
const ids = (lista) => lista.map((actual) => actual.id)
const filtrar = (filtros) => ids(filtrarTareas(tareas, { ...FILTROS_VACIOS, ...filtros }, 1))

test('sin filtros muestra todas las tareas en su orden', () => {
  assert.deepEqual(filtrar({}), ['1', '2', '3', '4', '5'])
  assert.equal(hayFiltrosActivos(FILTROS_VACIOS), false)
})

test('filtra por prioridad', () => {
  assert.deepEqual(filtrar({ prioridad: 'alta' }), ['1', '4', '5'])
})

test('filtra por responsable y lo combina con la prioridad', () => {
  assert.deepEqual(filtrar({ responsableId: 2 }), ['2', '4'])
  assert.deepEqual(filtrar({ responsableId: 2, prioridad: 'alta' }), ['4'])
})

test('solo mis tareas usa el usuario actual y excluye las sin responsable', () => {
  assert.deepEqual(filtrar({ soloMias: true }), ['1', '3'])
  assert.deepEqual(filtrar({ soloMias: true, responsableId: 1 }), ['1', '3'])
  assert.deepEqual(filtrar({ soloMias: true, responsableId: 2 }), [])
  assert.equal(hayFiltrosActivos({ ...FILTROS_VACIOS, soloMias: true }), true)
})

test('combina los filtros con la búsqueda de texto', () => {
  assert.deepEqual(filtrar({ consulta: '  LOGIN ', prioridad: 'alta' }), ['1'])
  assert.equal(hayFiltrosActivos({ ...FILTROS_VACIOS, consulta: '   ' }), false)
})
