import React from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import { AlertTriangle, CheckCircle } from 'lucide-react';

/**
 * @component ControlAveriasAutomaticas
 * @description Componente para controlar la activación/desactivación de averías automáticas
 */
const ControlAveriasAutomaticas: React.FC = () => {
  const { averiasAutomaticasActivas, toggleAveriasAutomaticas } = useSimulacion();

  return (
    <div className="bg-white rounded-lg shadow-md p-4 border border-gray-200">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          {averiasAutomaticasActivas ? (
            <AlertTriangle className="w-5 h-5 text-orange-500" />
          ) : (
            <CheckCircle className="w-5 h-5 text-green-500" />
          )}
          <div>
            <h3 className="font-semibold text-gray-900">
              Averías Automáticas
            </h3>
            <p className="text-sm text-gray-600">
              {averiasAutomaticasActivas 
                ? 'Activadas - Los camiones se averiarán automáticamente'
                : 'Desactivadas - Los camiones no se averiarán automáticamente'
              }
            </p>
          </div>
        </div>
        
        <button
          onClick={toggleAveriasAutomaticas}
          className={`px-4 py-2 rounded-md font-medium transition-colors ${
            averiasAutomaticasActivas
              ? 'bg-orange-100 text-orange-700 hover:bg-orange-200 border border-orange-300'
              : 'bg-green-100 text-green-700 hover:bg-green-200 border border-green-300'
          }`}
        >
          {averiasAutomaticasActivas ? 'Desactivar' : 'Activar'}
        </button>
      </div>
      
      <div className="mt-3 p-3 bg-gray-50 rounded-md">
        <p className="text-xs text-gray-600">
          <strong>Nota:</strong> Cuando están activadas, los camiones se marcarán automáticamente como averiados 
          al pasar por nodos con averías automáticas (T1, T2, T3) y se mostrarán notificaciones toast.
        </p>
      </div>
    </div>
  );
};

export default ControlAveriasAutomaticas; 