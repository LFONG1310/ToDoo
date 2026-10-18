import NavBar from '../../components/Nav/NavBar'
import ProyectoCard from '../../components/Proyectos/ProyectoCard'

export default function ProyectosPage() {
  return(
    <div className="min-h-screen bg-gray-100">
      <NavBar />
      <main className="mx-auto max-w-7xl p-4 sm:p-6">
        <h1 className="mb-6 text-2xl font-bold text-blue-800">Proyectos</h1>
        <ProyectoCard />
      </main>
    </div>
  )
}
