import test from 'node:test'
import assert from 'node:assert/strict'
import { moverTarea } from '../src/utils/moverTarea.js'

const tareas = [
  { id: 'a', title: 'Primera', description: 'Descripción', state: 'todo' },
  { id: 'b', title: 'Segunda', description: '', state: 'todo' },
  { id: 'c', title: 'Tercera', description: '', state: 'done' },
]

test('ordena una tarea antes de otra sin modificar el array original', () => {
  const resultado = moverTarea(tareas, 'b', 'todo', 'a')
  assert.deepEqual(resultado.map((tarea) => tarea.id), ['b', 'a', 'c'])
  assert.deepEqual(tareas.map((tarea) => tarea.id), ['a', 'b', 'c'])
  assert.deepEqual(resultado.find((tarea) => tarea.id === 'b'), tareas[1])
})

test('mueve a otra columna antes de una tarjeta y conserva el contenido', () => {
  const resultado = moverTarea(tareas, 'a', 'done', 'c')
  assert.deepEqual(resultado.filter((tarea) => tarea.state === 'done').map((tarea) => tarea.id), ['a', 'c'])
  assert.deepEqual(resultado.find((tarea) => tarea.id === 'a'), { ...tareas[0], state: 'done' })
  assert.equal(new Set(resultado.map((tarea) => tarea.id)).size, tareas.length)
})

test('soltar en una columna vacía o sin tarjeta coloca al final', () => {
  assert.equal(moverTarea(tareas, 'a', 'in-progress').at(-1).state, 'in-progress')
  assert.deepEqual(moverTarea(tareas, 'a', 'todo').filter((tarea) => tarea.state === 'todo').map((tarea) => tarea.id), ['b', 'a'])
})

test('ignora soltar sobre sí misma y destinos o tareas inexistentes', () => {
  assert.equal(moverTarea(tareas, 'a', 'todo', 'a'), tareas)
  assert.equal(moverTarea(tareas, 'a', 'todo', 'c'), tareas)
  assert.equal(moverTarea(tareas, 'a', 'done', 'inexistente'), tareas)
  assert.equal(moverTarea(tareas, 'inexistente', 'done'), tareas)
})
