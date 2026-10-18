import NavBar from "../../components/Nav/NavBar";
import ProyectoCard from "../../components/Proyectos/ProyectoCard";
import { Link } from "react-router-dom";

export default function HomePage() {
  return (
    <>
      <NavBar />
      <main className="w-screen h-screen flex flex-col justify-start items-start bg-gray-100 p-4">
        <section>
          <ProyectoCard />
          <Link
            to="/kanban"
            className="mt-4 inline-block rounded-lg bg-blue-700 px-4 py-2 font-medium text-white hover:bg-blue-800"
          >
            Abrir tablero de tareas
          </Link>
        </section>
      </main>
    </>
  );
}
