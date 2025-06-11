import React from 'react';
import { useSimulacion } from '../context/SimulacionContext';

const IndicadoresCamiones: React.FC = () => {
  const { camiones } = useSimulacion();

  const totalCamiones = camiones.length;
  const camionesEnUso = camiones.filter(c => c.estado === 'En Camino').length;
  const porcentajeEnUso = totalCamiones > 0 ? (camionesEnUso / totalCamiones) * 100 : 0;

  return (
    <div>
      <h3>Indicadores de camiones</h3>
      <p>Camiones en uso: {porcentajeEnUso.toFixed(2)}%</p>
    </div>
  );
};

export default IndicadoresCamiones;
