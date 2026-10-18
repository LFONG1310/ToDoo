import { BrowserRouter } from 'react-router-dom'
import AppRoutes from './Routes.jsx'
import SesionProvider from './sesion/SesionProvider.jsx'

export default function App() {
  return (
    <BrowserRouter>
      <SesionProvider>
        <AppRoutes />
      </SesionProvider>
    </BrowserRouter>
  )
}
