import React from 'react';
import { usePaquete } from '../hooks/usePaqueteContext';
import { useSimulacion } from '../hooks/useSimulacionContext';

interface IndicadorNodoActualProps {
  className?: string;
}

const IndicadorNodoActual: React.FC<IndicadorNodoActualProps> = ({
  className = ''
}) => {
  const { paqueteActual } = usePaquete();
  const { isRunning, isPaused, tiempoTranscurridoSimulado } = useSimulacion();

  // Calcular información del nodo actual
  const calcularInfoNodo = () => {
    if (!paqueteActual?.cromosoma || !isRunning) {
      return {
        nodoActual: 0,
        totalNodos: 0,
        tiempoPorNodo: 0,
        camionesActivos: 0
      };
    }

    const cromosoma = paqueteActual.cromosoma;
    let nodoActualMax = 0;
    let totalNodosMax = 0;
    let camionesActivos = 0;

    cromosoma.forEach(gen => {
      if (gen.camion.estado === 'EN_RUTA' || gen.camion.estado === 'ENTREGANDO_GLP_A_CLIENTE') {
        camionesActivos++;
      }
      
      const nodoActual = gen.posNodo || 0;
      const totalNodos = gen.rutaFinal?.length || gen.nodos?.length || 0;
      
      nodoActualMax = Math.max(nodoActualMax, nodoActual);
      totalNodosMax = Math.max(totalNodosMax, totalNodos);
    });

    // Calcular tiempo promedio por nodo (en segundos)
    const tiempoPorNodo = totalNodosMax > 0 && nodoActualMax > 0 
      ? Math.floor(tiempoTranscurridoSimulado / nodoActualMax) 
      : 0;

    return {
      nodoActual: nodoActualMax,
      totalNodos: totalNodosMax,
      tiempoPorNodo,
      camionesActivos
    };
  };

  const infoNodo = calcularInfoNodo();
  const progreso = infoNodo.totalNodos > 0 
    ? (infoNodo.nodoActual / infoNodo.totalNodos) * 100 
    : 0;

  // Estado de simulación
  const obtenerEstadoSimulacion = () => {
    if (!isRunning) return { texto: 'Detenida', color: 'text-red-600 bg-red-50' };
    if (isPaused) return { texto: 'Pausada', color: 'text-yellow-600 bg-yellow-50' };
    return { texto: 'Ejecutándose', color: 'text-green-600 bg-green-50' };
  };

  const estado = obtenerEstadoSimulacion();

  return (
    <div className={`bg-white rounded-lg shadow-sm border border-gray-200 p-4 ${className}`}>
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-gray-800">
          Ejecución en Tiempo Real
        </h3>
        <span className={`text-xs px-2 py-1 rounded-full ${estado.color}`}>
          {estado.texto}
        </span>
      </div>

      <div className="space-y-3">
        {/* Nodo Actual */}
        <div className="flex justify-between items-center">
          <span className="text-sm text-gray-600">Nodo Actual:</span>
          <span className="text-sm font-medium text-blue-600">
            {infoNodo.nodoActual} / {infoNodo.totalNodos}
          </span>
        </div>

        {/* Progreso visual */}
        {infoNodo.totalNodos > 0 && (
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div 
              className="bg-blue-600 h-2 rounded-full transition-all duration-300"
              style={{ width: `${progreso}%` }}
            />
          </div>
        )}

        {/* Tiempo por nodo */}
        <div className="flex justify-between items-center">
          <span className="text-sm text-gray-600">Seg/Nodo:</span>
          <span className="text-sm font-medium text-green-600">
            {infoNodo.tiempoPorNodo}s
          </span>
        </div>

        {/* Camiones activos */}
        <div className="flex justify-between items-center">
          <span className="text-sm text-gray-600">Camiones Activos:</span>
          <span className="text-sm font-medium text-purple-600">
            {infoNodo.camionesActivos}
          </span>
        </div>

        {/* Tiempo total transcurrido */}
        <div className="flex justify-between items-center text-xs text-gray-500 pt-2 border-t">
          <span>Tiempo Total:</span>
          <span>{Math.floor(tiempoTranscurridoSimulado)}s</span>
        </div>
      </div>
    </div>
  );
};

export default IndicadorNodoActual;
