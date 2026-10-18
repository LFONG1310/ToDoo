import { Link, NavLink } from 'react-router-dom'
import todooLogo from '../../assets/todoo-logo.png'

export default function NavBar() {
  return (
    <nav aria-label="Navegación principal" className="w-full text-black flex min-h-16 items-center gap-3 bg-white shadow-md p-4 flex-wrap">
      <Link to="/" aria-label="ToDoo: inicio">
        <img src={todooLogo} alt="ToDoo" className="w-25 h-14 object-contain" />
      </Link>
      <div className="flex flex-row flex-wrap justify-between items-center gap-4 w-full">
        <ul className="flex flex-wrap justify-center items-center gap-4">
          <li>
            <NavLink to="/" end className={({ isActive }) => isActive ? 'font-semibold text-blue-700' : 'hover:text-blue-700'}>Inicio</NavLink>
          </li>
          <li>
            <NavLink to="/proyectos" className={({ isActive }) => isActive ? 'font-semibold text-blue-700' : 'hover:text-blue-700'}>Proyectos</NavLink>
          </li>
          <li>
            <NavLink to="/kanban" className={({ isActive }) => isActive ? 'font-semibold text-blue-700' : 'hover:text-blue-700'}>Tablero</NavLink>
          </li>
        </ul>
        <button className="whitespace-nowrap">Cerrar sesión</button>
      </div>
    </nav>
  );
}
