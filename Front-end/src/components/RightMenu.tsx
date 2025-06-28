import React from 'react';
import { ChevronRight, Play, Pause } from 'lucide-react';
import TablaPedidos from './TablaPedidos';
import MetricasRendimiento from './MetricasRendimiento';
import CardsCamiones from './CardCamion';
import IndicadoresCamiones from './IndicadoresCamiones';

interface RightMenuProps {
  expanded: boolean;
  setExpanded: (value: boolean) => void;
  // Controles de simulación
  isRunning?: boolean;
  setIsRunning?: (value: boolean) => void;
  velocidad?: number;
  setVelocidad?: (value: number) => void;
}

const RightMenu: React.FC<RightMenuProps> = ({ 
  expanded, 
  setExpanded, 
  isRunning = false, 
  setIsRunning = () => {}, 
  velocidad = 300, 
  setVelocidad = () => {} 
}) => {
  if (!expanded) return null;

  return (
    <div className="transition-all duration-300 bg-white rounded-xl p-4 shadow-md h-full flex flex-col">
      <button
        onClick={() => setExpanded(false)}
        className="self-end mb-2"
        title="Ocultar menú"
      >
        <ChevronRight size={12} />
      </button>

      <div className="flex flex-col gap-4 overflow-y-auto">
        {/* Controles de Simulación */}
        <div className="border-b border-gray-200 pb-4">
          <div className="text-md font-bold text-gray-700 mb-3">Controles de Simulación</div>
          
          {/* Contenedor para alinear horizontalmente los controles */}
          <div className="flex flex-row items-center gap-3">
            {/* Botón Pausar/Reanudar */}
            <button
              onClick={() => setIsRunning(!isRunning)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-colors whitespace-nowrap ${
                isRunning 
                  ? 'bg-orange-500 hover:bg-orange-600 text-white' 
                  : 'bg-green-500 hover:bg-green-600 text-white'
              }`}
            >
              {isRunning ? <Pause size={16} /> : <Play size={16} />}
              {isRunning ? 'Pausar' : 'Reanudar'}
            </button>

            {/* Control de Velocidad */}
            <div className="flex items-center gap-2">
              <label htmlFor="velocidad" className="text-sm font-medium text-gray-600 whitespace-nowrap">
                Velocidad:
              </label>
              <input
                id="velocidad"
                type="number"
                min="50"
                max="2000"
                step="50"
                value={velocidad}
                onChange={(e) => setVelocidad(Number(e.target.value))}
                className="w-20 px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <span className="text-sm text-gray-500">ms</span>
            </div>
          </div>
        </div>

        <div className="text-md font-bold text-gray-700">Estado de los camiones</div>
        <CardsCamiones />
        <div className="text-md font-bold text-gray-700">Datos de los camiones</div>
        <TablaPedidos />
        <div className="text-md font-bold text-gray-700">Métricas de rendimiento</div>
        <MetricasRendimiento />
        <div className="text-xs font-bold text-gray-700">Indicadores de camiones</div>
        <IndicadoresCamiones />
      </div>
    </div>
  );
};

export default RightMenu;
