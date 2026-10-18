import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import todooLogo from '../assets/todoo-logo.png'
import { useSesion } from '../sesion/sesionContext.js'

const inputClass =
  'block w-full rounded-md border border-gray-300 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 shadow-xs placeholder:text-gray-400 focus:border-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-300'

export default function LoginPage() {
  const { usuario, iniciarSesion } = useSesion()
  const navigate = useNavigate()
  const location = useLocation()
  const [correo, setCorreo] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState('')
  const destino = location.state?.desde ?? '/tableros'

  if (usuario && !enviando) {
    return <Navigate to={destino} replace />
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (enviando) return
    setEnviando(true)
    setError('')
    try {
      await iniciarSesion(correo, contrasena)
      navigate(destino, { replace: true })
    } catch (failure) {
      setError(failure.message)
      setEnviando(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-linear-to-b from-blue-800 to-blue-700">
      <header className="bg-white">
        <img src={todooLogo} alt="ToDoo" className="h-12 w-auto max-w-full object-contain px-1 py-1" />
      </header>
      <main className="flex flex-1 items-center justify-center px-4 py-10">
        <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
          <h1 className="mb-5 text-center text-xl font-bold">
            Bienvenido a <span>To</span>
            <span className="text-blue-700/90">Doo</span>
          </h1>

          <form className="mx-auto max-w-sm" onSubmit={handleSubmit}>
            <div className="mb-5">
              <label htmlFor="email" className="mb-2.5 block text-sm font-medium text-gray-900">
                Correo electrónico
              </label>
              <input
                type="email"
                id="email"
                autoComplete="email"
                className={inputClass}
                placeholder="nombre@ejemplo.com"
                value={correo}
                onChange={(e) => setCorreo(e.target.value)}
                required
              />
            </div>
            <div className="mb-5">
              <label htmlFor="password" className="mb-2.5 block text-sm font-medium text-gray-900">
                Contraseña
              </label>
              <input
                type="password"
                id="password"
                autoComplete="current-password"
                className={inputClass}
                placeholder="••••••••"
                value={contrasena}
                onChange={(e) => setContrasena(e.target.value)}
                required
              />
            </div>
            {error && (
              <p role="alert" className="mb-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
                {error}
              </p>
            )}
            <div className="text-center">
              <button
                type="submit"
                disabled={enviando}
                className="rounded-md bg-blue-700 px-4 py-2.5 text-center text-sm font-medium text-white hover:bg-blue-800 focus:outline-none focus:ring-4 focus:ring-blue-300 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {enviando ? 'Entrando…' : 'Iniciar sesión'}
              </button>
              <p className="pt-3 text-sm text-gray-600">
                ¿No tienes una cuenta? Pídele al propietario de tu tablero que te registre.
              </p>
            </div>
          </form>
        </div>
      </main>
    </div>
  )
}
