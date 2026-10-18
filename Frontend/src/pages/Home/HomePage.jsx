import NavBar from "../../components/Nav/NavBar";
import ProyectoCard from "../../components/Proyectos/ProyectoCard";
import { Link } from "react-router-dom";
import { useState } from 'react'
import { cargarTablero } from '../../services/tareasStorage'
import { PROYECTO_PRINCIPAL } from '../../constants/proyectos'

export default function HomePage() {
  const [{ tareas, error }] = useState(() => cargarTablero())
  return (
    <>
      <NavBar />
      <main className="min-h-screen bg-gray-100 p-4 sm:p-6">
        <section className="mx-auto max-w-7xl">
          <h1 className="mb-6 text-2xl font-bold text-blue-800">Inicio</h1>
          {error && <p role="alert" className="mb-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-900">{error}</p>}
          <div className="max-w-md"><ProyectoCard proyecto={PROYECTO_PRINCIPAL} tareas={tareas} /></div>
          <Link
            to="/proyectos"
            className="mt-4 inline-block rounded-lg bg-blue-700 px-4 py-2 font-medium text-white hover:bg-blue-800"
          >
            Ver proyectos
          </Link>
        </section>
      </main>
    </>
  );
}
