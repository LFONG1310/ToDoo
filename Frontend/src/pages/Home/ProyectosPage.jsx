import NavBar from '../../components/Nav/NavBar'
import ProyectoCard from '../../components/Proyectos/ProyectoCard'
import { useState } from 'react'
import { cargarTablero } from '../../services/tareasStorage'
import { PROYECTO_PRINCIPAL } from '../../constants/proyectos'

export default function ProyectosPage() {
  const [{ tareas, error }] = useState(() => cargarTablero())
  return(
    <div className="min-h-screen bg-gray-100">
      <NavBar />
      <main className="mx-auto max-w-7xl p-4 sm:p-6">
        <h1 className="mb-6 text-2xl font-bold text-blue-800">Proyectos</h1>
        <p className="mb-6 text-gray-600">Consulta el avance de tus tareas y accede al tablero del proyecto.</p>
        {error && <p role="alert" className="mb-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-900">{error}</p>}
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
          <ProyectoCard proyecto={PROYECTO_PRINCIPAL} tareas={tareas} />
        </div>
      </main>
    </div>
  )
}
