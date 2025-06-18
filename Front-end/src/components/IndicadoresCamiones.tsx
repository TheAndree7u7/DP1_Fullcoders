// components/IndicadoresCamiones.tsx
import { useSimulacion } from '../context/SimulacionContext';
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
    <div className="grid grid-cols-2 gap-2">
      {/* Camiones Disponibles */}
      <div className="flex items-center gap-2 bg-blue-50 rounded-lg p-3">
        <div className="bg-blue-100 p-2 rounded-lg">
          <CircleDashed size={20} className="text-blue-600" />
        </div>
        <div>
          <div className="text-sm font-medium">Disponibles</div>
          <div className="text-2xl font-bold">{contadorEstados.Disponible}</div>
        </div>
      </div>
      
      {/* Camiones En Camino */}
      <div className="flex items-center gap-2 bg-green-50 rounded-lg p-3">
        <div className="bg-green-100 p-2 rounded-lg">
          <Fuel size={20} className="text-green-600" />
        </div>
        <div>
          <div className="text-sm font-medium">En Ruta</div>
          <div className="text-2xl font-bold">{contadorEstados['En Camino']}</div>
        </div>
      </div>
      
      {/* Camiones En Mantenimiento */}
      <div className="flex items-center gap-2 bg-yellow-50 rounded-lg p-3">
        <div className="bg-yellow-100 p-2 rounded-lg">
          <Wrench size={20} className="text-yellow-600" />
        </div>
        <div>
          <div className="text-sm font-medium">Mantenimiento</div>
          <div className="text-2xl font-bold">{contadorEstados['En Mantenimiento']}</div>
        </div>
      </div>
      
      {/* Camiones Averiados */}
      <div className="flex items-center gap-2 bg-red-50 rounded-lg p-3">
        <div className="bg-red-100 p-2 rounded-lg">
          <AlertTriangle size={20} className="text-red-600" />
        </div>
        <div>
          <div className="text-sm font-medium">Averiados</div>
          <div className="text-2xl font-bold">{contadorEstados.Averiado}</div>
        </div>
      </div>
      
      {/* Camiones con Entregas Completadas */}
      <div className="flex items-center gap-2 bg-emerald-50 rounded-lg p-3 col-span-2">
        <div className="bg-emerald-100 p-2 rounded-lg">
          <CheckCircle2 size={20} className="text-emerald-600" />
        </div>
        <div>
          <div className="text-sm font-medium">Entregas Completadas</div>
          <div className="text-2xl font-bold">{contadorEstados.Entregado}</div>
        </div>
      </div>
    </div>
  );
};

export default IndicadoresCamiones;
