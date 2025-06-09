import { Route, Routes } from 'react-router-dom';
import './App.css';
import SimulacionSemanal from './views/SimulacionSemanal';
import { SimulacionProvider } from './context/SimulacionContext';
import TiempoReal from './views/TiempoReal';

function App() {
  return (
    <SimulacionProvider>
      <Routes>
        <Route path="/" element={<SimulacionSemanal />} />
        <Route path="/tiempo-real" element={<TiempoReal />} />
      </Routes>
    </SimulacionProvider>
  );
}

export default App;
