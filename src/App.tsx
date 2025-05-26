import { Route, Routes } from 'react-router-dom'
import './App.css'
import SimulacionSemanal from './views/SimulacionSemanal'

function App() {

  return (
    <div>
      <Routes>
        <Route path="/" element={<SimulacionSemanal />} />

      </Routes>

    </div>
  )
}

export default App
