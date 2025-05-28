import { Route, Routes } from 'react-router-dom';
import './App.css';
import SimulacionSemanal from './views/SimulacionSemanal';
import { SimulacionProvider } from './context/SimulacionContext';

function App() {
  return (
    <SimulacionProvider>
      <Routes>
        <Route path="/" element={<SimulacionSemanal />} />
      </Routes>
    </SimulacionProvider>
  );
}

export default App;
