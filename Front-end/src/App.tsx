import { Route, Routes } from 'react-router-dom';
import './App.css';
import SimulacionSemanal from './views/SimulacionSemanal';
import { SimulacionProvider } from './context/SimulacionContext';
import TiempoReal from './views/TiempoReal';
import SeleccionVista from './views/SeleccionVista';
import ColapsoLogistico from './views/ColapsoLogistico';

function App() {
  return (
    <SimulacionProvider>
      <Routes>
        <Route path="/" element={<SeleccionVista />} />
        <Route path="/simulacion-semanal" element={<SimulacionSemanal />} />
        <Route path="/tiempo-real" element={<TiempoReal />} />
        <Route path="/colapso-logistico" element={<ColapsoLogistico />} />
      </Routes>
    </SimulacionProvider>
  );
}

export default App;
