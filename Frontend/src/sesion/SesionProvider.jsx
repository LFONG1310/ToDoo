import { useCallback, useEffect, useMemo, useState } from 'react'
import { cerrarSesion as cerrarSesionApi, iniciarSesion as iniciarSesionApi, obtenerSesion } from '../services/authApi.js'
import { SesionContext } from './sesionContext.js'

export default function SesionProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [reintentos, setReintentos] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    obtenerSesion(controller.signal)
      .then((actual) => {
        setUsuario(actual)
        setError(null)
      })
      .catch((failure) => {
        if (failure.name !== 'AbortError') setError(failure)
      })
      .finally(() => {
        if (!controller.signal.aborted) setCargando(false)
      })
    return () => controller.abort()
  }, [reintentos])

  const reintentar = useCallback(() => {
    setCargando(true)
    setError(null)
    setReintentos((n) => n + 1)
  }, [])

  const iniciarSesion = useCallback(async (correo, contrasena) => {
    const actual = await iniciarSesionApi(correo, contrasena)
    setUsuario(actual)
    setError(null)
    return actual
  }, [])

  const cerrarSesion = useCallback(async () => {
    try {
      await cerrarSesionApi()
    } finally {
      setUsuario(null)
    }
  }, [])

  // Lo llaman las pantallas cuando el backend responde 401 (la sesion vencio).
  const sesionExpirada = useCallback(() => setUsuario(null), [])

  const valor = useMemo(
    () => ({ usuario, cargando, error, reintentar, iniciarSesion, cerrarSesion, sesionExpirada }),
    [usuario, cargando, error, reintentar, iniciarSesion, cerrarSesion, sesionExpirada],
  )
  return <SesionContext.Provider value={valor}>{children}</SesionContext.Provider>
}
