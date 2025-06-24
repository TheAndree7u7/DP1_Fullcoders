import { Route, Routes } from 'react-router-dom';
import './App.css';
import SimulacionSemanal from './views/SimulacionSemanal';
import { SimulacionProvider } from './context/SimulacionContext';
import { ToastContainer, Bounce } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function App() {
  return (
    <SimulacionProvider>
      <Routes>
        <Route path="/" element={<SimulacionSemanal />} />
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
