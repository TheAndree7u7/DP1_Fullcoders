import React, { useState, useEffect } from 'react';
import { obtenerTipoSimulacionActual, type TipoSimulacionActualResponse } from '../services/simulacionApiService';

interface TipoSimulacionInfoProps {
  className?: string;
}

const TipoSimulacionInfo: React.FC<TipoSimulacionInfoProps> = ({ className = '' }) => {
  const [tipoActual, setTipoActual] = useState<TipoSimulacionActualResponse | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    cargarTipoSimulacion();
  }, []);

  const cargarTipoSimulacion = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await obtenerTipoSimulacionActual();
      setTipoActual(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error desconocido');
    } finally {
      setCargando(false);
    }
  };

  const obtenerIconoTipo = (tipo: string) => {
    switch (tipo) {
      case 'DIARIA':
        return '⚡';
      case 'SEMANAL':
        return '📊';
      case 'COLAPSO':
        return '🚨';
      default:
        return '❓';
    }
  };

  const obtenerColorTipo = (tipo: string) => {
    switch (tipo) {
      case 'DIARIA':
        return 'text-blue-600 bg-blue-100 border-blue-200';
      case 'SEMANAL':
        return 'text-green-600 bg-green-100 border-green-200';
      case 'COLAPSO':
        return 'text-red-600 bg-red-100 border-red-200';
      default:
        return 'text-gray-600 bg-gray-100 border-gray-200';
    }
  };

  if (cargando) {
    return (
      <div className={`bg-white rounded-lg shadow-md p-4 ${className}`}>
        <div className="flex items-center justify-center">
          <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600 mr-2"></div>
          <span className="text-gray-600">Cargando tipo de simulación...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`bg-white rounded-lg shadow-md p-4 ${className}`}>
        <div className="flex items-center justify-center text-red-600">
          <span className="mr-2">❌</span>
          <span>Error: {error}</span>
        </div>
      </div>
    );
  }

  if (!tipoActual) {
    return (
      <div className={`bg-white rounded-lg shadow-md p-4 ${className}`}>
        <div className="text-center text-gray-500">
          No hay información disponible
        </div>
      </div>
    );
  }

  return (
    <div className={`bg-white rounded-lg shadow-md p-4 ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <span className="text-2xl mr-3">{obtenerIconoTipo(tipoActual.tipoSimulacion)}</span>
          <div>
            <h3 className="font-semibold text-gray-900">Tipo de Simulación Actual</h3>
            <p className="text-sm text-gray-600">{tipoActual.descripcion}</p>
          </div>
        </div>
        <div className={`px-3 py-1 rounded-full border text-sm font-medium ${obtenerColorTipo(tipoActual.tipoSimulacion)}`}>
          {tipoActual.tipoSimulacion}
        </div>
      </div>
      <div className="mt-3 pt-3 border-t border-gray-200">
        <div className="flex justify-between text-xs text-gray-500">
          <span>Última actualización:</span>
          <span>{new Date(tipoActual.timestamp).toLocaleString()}</span>
        </div>
      </div>
    </div>
  );
};

export default TipoSimulacionInfo; 