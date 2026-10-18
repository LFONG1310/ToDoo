import test from 'node:test'
import assert from 'node:assert/strict'
import { cargarTablero, guardarTareas } from '../src/services/tareasStorage.js'

function memoria(valor = null) {
  return {
    getItem: () => valor,
    setItem: (_key, nuevo) => { valor = nuevo },
  }
}

const tarea = {
  id: '1', title: 'Preparar entrega', description: 'Revisar tablero', state: 'todo',
  prioridad: 'alta', responsableId: 2,
}

test('un navegador sin tareas empieza con el tablero vacío', () => {
  assert.deepEqual(cargarTablero(memoria()), { tareas: [], error: '' })
})

test('conserva contenido, estados y orden después de guardar y cargar', () => {
  const storage = memoria()
  const tareas = [{ ...tarea, id: '2', state: 'done' }, tarea]
  assert.equal(guardarTareas(tareas, storage), '')
  assert.deepEqual(cargarTablero(storage), { tareas, error: '' })
})

test('no sobrescribe datos dañados al intentar cargarlos', () => {
  for (const valor of ['JSON dañado', '{}', 'null']) {
    const storage = memoria(valor)
    const resultado = cargarTablero(storage)
    assert.deepEqual(resultado.tareas, [])
    assert.ok(resultado.error)
    assert.equal(storage.getItem(), valor)
  }
})

test('recupera tareas válidas y descarta duplicados y campos incorrectos', () => {
  const guardadas = [tarea, tarea, null, { ...tarea, id: '2', state: 'otro' },
    { ...tarea, id: '3', title: ' ' }, { ...tarea, id: '4', description: 7 },
    { ...tarea, id: '5', title: 'a'.repeat(101) }, { ...tarea, id: '6', prioridad: 'urgente' },
    { ...tarea, id: '7', responsableId: 'Ana' }]
  const storage = memoria(JSON.stringify(guardadas))
  const resultado = cargarTablero(storage)
  assert.deepEqual(resultado.tareas, [tarea])
  assert.ok(resultado.error)
  assert.deepEqual(JSON.parse(storage.getItem()), guardadas)
})

test('las tareas guardadas antes de los filtros reciben prioridad media y sin responsable', () => {
  const antigua = { id: '1', title: 'Preparar entrega', description: '', state: 'todo' }
  const resultado = cargarTablero(memoria(JSON.stringify([antigua])))
  assert.deepEqual(resultado, {
    tareas: [{ ...antigua, prioridad: 'media', responsableId: null }],
    error: '',
  })
})

test('informa cuando el almacenamiento no se puede leer', () => {
  const resultado = cargarTablero({ getItem() { throw new Error('Bloqueado') } })
  assert.deepEqual(resultado.tareas, [])
  assert.ok(resultado.error)
})

test('permite reintentar el guardado después de una cuota excedida', () => {
  const storage = memoria()
  const setItem = storage.setItem
  storage.setItem = () => { throw new Error('Cuota excedida') }
  assert.ok(guardarTareas([tarea], storage))
  storage.setItem = setItem
  assert.equal(guardarTareas([tarea], storage), '')
  assert.deepEqual(cargarTablero(storage).tareas, [tarea])
})
