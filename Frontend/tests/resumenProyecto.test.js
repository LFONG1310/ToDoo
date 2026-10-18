import test from 'node:test'
import assert from 'node:assert/strict'
import { resumenProyecto } from '../src/utils/resumenProyecto.js'

test('un proyecto vacío muestra cero avance y estado Sin tareas', () => {
  assert.deepEqual(resumenProyecto([]), {
    total: 0, porHacer: 0, enProgreso: 0, hechas: 0, estado: 'sin-tareas', porcentaje: 0,
  })
})

test('un proyecto pendiente está por comenzar y no tiene avance completado', () => {
  const resumen = resumenProyecto([{ state: 'todo' }, { state: 'todo' }])
  assert.equal(resumen.estado, 'por-comenzar')
  assert.equal(resumen.porHacer, 2)
  assert.equal(resumen.porcentaje, 0)
})

test('cuenta cada columna y calcula el avance por tareas completadas', () => {
  assert.deepEqual(resumenProyecto([{ state: 'todo' }, { state: 'in-progress' }, { state: 'done' }]), {
    total: 3, porHacer: 1, enProgreso: 1, hechas: 1, estado: 'en-progreso', porcentaje: 33,
  })
  assert.equal(resumenProyecto([{ state: 'todo' }, { state: 'done' }]).estado, 'en-progreso')
})

test('solo muestra 100% y Completado cuando todas las tareas están hechas', () => {
  const completo = resumenProyecto([{ state: 'done' }, { state: 'done' }])
  assert.equal(completo.estado, 'completado')
  assert.equal(completo.porcentaje, 100)
  const casiCompleto = resumenProyecto([...Array.from({ length: 200 }, () => ({ state: 'done' })), { state: 'todo' }])
  assert.equal(casiCompleto.estado, 'en-progreso')
  assert.equal(casiCompleto.porcentaje, 99)
})
