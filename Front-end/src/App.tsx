import { Route, Routes } from 'react-router-dom';
import './App.css';
import Bienvenida from './views/Bienvenida';
import SimulacionSemanal from './views/SimulacionSemanal';
import ModuloEnDesarrollo from './components/ModuloEnDesarrollo';
import { SimulacionProvider } from './context/SimulacionContext';
import { ToastContainer, Bounce } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function App() {
  return (
    <SimulacionProvider>
      <Routes>
        <Route path="/" element={<Bienvenida />} />
        <Route path="/simulacion-semanal" element={<SimulacionSemanal />} />
        <Route path="/ejecucion-real" element={<ModuloEnDesarrollo titulo="Ejecución en tiempo Real" />} />
        <Route path="/colapso-logistico" element={<ModuloEnDesarrollo titulo="Colapso Logístico" />} />
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
