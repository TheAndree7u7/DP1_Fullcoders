import { Route, Routes } from 'react-router-dom';
import './App.css';
import SeleccionVista from './views/SeleccionVista';
import SimulacionSemanalConCarga from './views/SimulacionSemanalConCarga';
import SimulacionAlColapso from './views/SimulacionAlColapso';
import SimulacionDiaria from './views/SimulacionDiaria';
import CargaSimulacionDiaria from './views/CargaSimulacionDiaria';
import { SimulacionProvider } from './context/SimulacionContext';
import { ToastContainer, Bounce } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function App() {
  return (
    <SimulacionProvider>
      <Routes>
        <Route path="/" element={<SeleccionVista />} />
        <Route path="/simulacion-semanal" element={<SimulacionSemanalConCarga />} />
        <Route path="/colapso-logistico" element={<SimulacionAlColapso />} />
        <Route path="/carga-simulacion-diaria" element={<CargaSimulacionDiaria />} />
        <Route path="/ejecucion-tiempo-real" element={<SimulacionDiaria />} />
      </Routes>
      <ToastContainer
        position="top-right"
        autoClose={5000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick={false}
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="light"
        transition={Bounce}
      />
    </SimulacionProvider>
  );
}

export default App;
