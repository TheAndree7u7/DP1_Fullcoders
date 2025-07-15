// components/IndicadoresCamiones.tsx
import { useSimulacion } from '../hooks/useSimulacionContext';
import { CircleDashed, Fuel, Wrench, CheckCircle2, AlertTriangle } from 'lucide-react';

/**
 * Componente que muestra indicadores resumen de los camiones
 * según su estado actual en la simulación
 */
const IndicadoresCamiones: React.FC = () => {
  const { camiones } = useSimulacion();

  // Contador de camiones por estado
  const contadorEstados = {
    Entregado: 0,
    'En Camino': 0,
    Averiado: 0,
    'En Mantenimiento': 0,
    Disponible: 0,
  };

  // Contabilizar camiones por estado
  camiones.forEach((camion) => {
    if (camion.estado in contadorEstados) {
      contadorEstados[camion.estado as keyof typeof contadorEstados]++;
    }
  });

  return (
    <div className="grid grid-cols-5 gap-1 mb-1">
      {/* Camiones Disponibles */}
      <div className="flex flex-col items-center justify-center bg-blue-50 rounded p-1 text-center">
        <div className="bg-blue-100 p-1 rounded">
          <CircleDashed size={14} className="text-blue-600" />
        </div>
        <div className="text-[10px] font-medium">Disponibles</div>
        <div className="text-sm font-bold">{contadorEstados.Disponible}</div>
      </div>
      
      {/* Camiones En Camino */}
      <div className="flex flex-col items-center justify-center bg-green-50 rounded p-1 text-center">
        <div className="bg-green-100 p-1 rounded">
          <Fuel size={14} className="text-green-600" />
        </div>
        <div className="text-[10px] font-medium">En Ruta</div>
        <div className="text-sm font-bold">{contadorEstados['En Camino']}</div>
      </div>
      
      {/* Camiones En Mantenimiento */}
      <div className="flex flex-col items-center justify-center bg-yellow-50 rounded p-1 text-center">
        <div className="bg-yellow-100 p-1 rounded">
          <Wrench size={14} className="text-yellow-600" />
        </div>
        <div className="text-[10px] font-medium">Mantenim.</div>
        <div className="text-sm font-bold">{contadorEstados['En Mantenimiento']}</div>
      </div>
      
      {/* Camiones Averiados */}
      <div className="flex flex-col items-center justify-center bg-red-50 rounded p-1 text-center">
        <div className="bg-red-100 p-1 rounded">
          <AlertTriangle size={14} className="text-red-600" />
        </div>
        <div className="text-[10px] font-medium">Averiados</div>
        <div className="text-sm font-bold">{contadorEstados.Averiado}</div>
      </div>
      
      {/* Camiones con Entregas Completadas */}
      <div className="flex flex-col items-center justify-center bg-emerald-50 rounded p-1 text-center">
        <div className="bg-emerald-100 p-1 rounded">
          <CheckCircle2 size={14} className="text-emerald-600" />
        </div>
        <div className="text-[10px] font-medium">Entregados</div>
        <div className="text-sm font-bold">{contadorEstados.Entregado}</div>
      </div>
    </div>
  );
};

export default IndicadoresCamiones;
