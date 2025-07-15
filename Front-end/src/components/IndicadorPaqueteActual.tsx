import React, { useState, useEffect } from 'react';
import { Package, Clock, Activity } from 'lucide-react';
import { useSimulacion } from '../hooks/useSimulacionContext';

interface InfoSimulacion {
  totalPaquetes: number;
  paqueteActual: number;
  enProceso: boolean;
  tiempoActual: string;
}

interface IndicadorPaqueteActualProps {
  variant?: 'default' | 'compact' | 'detailed';
  showProgress?: boolean;
  showTime?: boolean;
}

const IndicadorPaqueteActual: React.FC<IndicadorPaqueteActualProps> = ({
  variant = 'default',
  showProgress = true,
  showTime = true
}) => {
  const [infoSimulacion, setInfoSimulacion] = useState<InfoSimulacion | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error] = useState<string>('');
  
  // Obtener datos del contexto de simulación
  const { isRunning, isPaused, fechaHoraSimulacion, tiempoTranscurridoSimulado } = useSimulacion();

  // Crear información de simulación basada en el contexto
  useEffect(() => {
    const crearInfoFromContext = () => {
      // Usar datos del contexto en lugar de hacer llamadas a API
      const totalEstimado = 100; // Valor por defecto, puede ser configurable
      const paqueteActual = Math.floor(tiempoTranscurridoSimulado / 10); // Estimación basada en tiempo
      
      setInfoSimulacion({
        totalPaquetes: totalEstimado,
        paqueteActual: Math.min(paqueteActual, totalEstimado),
        enProceso: isRunning && !isPaused,
        tiempoActual: fechaHoraSimulacion || new Date().toISOString()
      });
      setCargando(false);
    };

    crearInfoFromContext();
  }, [isRunning, isPaused, fechaHoraSimulacion, tiempoTranscurridoSimulado]);

  if (cargando) {
    return (
      <div className="bg-white rounded-lg shadow-md p-4 border border-gray-200">
        <div className="flex items-center justify-center">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500"></div>
          <span className="ml-2 text-gray-600">Cargando...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <div className="flex items-center text-red-600">
          <Activity className="w-4 h-4 mr-2" />
          <span className="text-sm font-medium">{error}</span>
        </div>
      </div>
    );
  }

  if (!infoSimulacion) {
    return (
      <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
        <div className="flex items-center text-gray-500">
          <Package className="w-4 h-4 mr-2" />
          <span className="text-sm">Sin información disponible</span>
        </div>
      </div>
    );
  }

  const porcentajeProgreso = infoSimulacion.totalPaquetes > 0 
    ? (infoSimulacion.paqueteActual / infoSimulacion.totalPaquetes) * 100 
    : 0;

  const obtenerColorEstado = () => {
    if (!infoSimulacion.enProceso) return 'from-red-500 to-red-600';
    return 'from-green-500 to-green-600';
  };

  const obtenerTextoEstado = () => {
    if (!infoSimulacion.enProceso) return 'Detenida';
    return 'En Proceso';
  };

  // Renderizado compacto
  if (variant === 'compact') {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Package className="w-4 h-4 text-blue-500" />
            <span className="text-sm font-medium text-gray-700">Paquete:</span>
          </div>
          <div className="flex items-center space-x-1">
            <span className="text-lg font-bold text-gray-900">
              {infoSimulacion.paqueteActual}
            </span>
            <span className="text-sm text-gray-500">
              / {infoSimulacion.totalPaquetes}
            </span>
          </div>
        </div>
        {showProgress && infoSimulacion.totalPaquetes > 0 && (
          <div className="mt-2">
            <div className="w-full bg-gray-200 rounded-full h-1.5">
              <div 
                className="bg-blue-500 h-1.5 rounded-full transition-all duration-300"
                style={{ width: `${porcentajeProgreso}%` }}
              ></div>
            </div>
          </div>
        )}
      </div>
    );
  }

  // Renderizado detallado
  if (variant === 'detailed') {
    return (
      <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
        {/* Header con estado */}
        <div className={`bg-gradient-to-r ${obtenerColorEstado()} text-white p-4`}>
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Activity className="w-5 h-5" />
              <span className="font-medium">{obtenerTextoEstado()}</span>
            </div>
            <div className="flex items-center space-x-1 text-sm opacity-90">
              <Clock className="w-4 h-4" />
              <span>Tiempo real</span>
            </div>
          </div>
        </div>

        {/* Contenido principal */}
        <div className="p-4">
          {/* Paquete actual destacado */}
          <div className="text-center mb-4">
            <div className="text-3xl font-bold text-gray-900 mb-1">
              {infoSimulacion.paqueteActual}
            </div>
            <div className="text-sm text-gray-600">
              de {infoSimulacion.totalPaquetes} paquetes total
            </div>
          </div>

          {/* Estadísticas en grid */}
          <div className="grid grid-cols-2 gap-3 mb-4">
            <div className="bg-blue-50 rounded-lg p-3 text-center">
              <div className="text-blue-600 text-xs uppercase font-medium">Progreso</div>
              <div className="text-blue-900 font-bold text-lg">
                {Math.round(porcentajeProgreso)}%
              </div>
            </div>
            <div className="bg-green-50 rounded-lg p-3 text-center">
              <div className="text-green-600 text-xs uppercase font-medium">Restantes</div>
              <div className="text-green-900 font-bold text-lg">
                {Math.max(0, infoSimulacion.totalPaquetes - infoSimulacion.paqueteActual)}
              </div>
            </div>
          </div>

          {/* Barra de progreso */}
          {showProgress && infoSimulacion.totalPaquetes > 0 && (
            <div className="mb-4">
              <div className="flex justify-between text-xs text-gray-600 mb-1">
                <span>Progreso de simulación</span>
                <span>{Math.round(porcentajeProgreso)}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-gradient-to-r from-blue-500 to-blue-600 h-2 rounded-full transition-all duration-500"
                  style={{ width: `${porcentajeProgreso}%` }}
                ></div>
              </div>
            </div>
          )}

          {/* Tiempo actual */}
          {showTime && infoSimulacion.tiempoActual && (
            <div className="bg-gray-50 rounded-lg p-3">
              <div className="text-gray-600 text-xs uppercase tracking-wide mb-1">
                Tiempo de Simulación
              </div>
              <div className="text-sm font-medium text-gray-800">
                {new Date(infoSimulacion.tiempoActual).toLocaleString('es-ES', {
                  weekday: 'short',
                  year: 'numeric',
                  month: 'short',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </div>
            </div>
          )}

          {/* Mensaje cuando no hay paquetes */}
          {infoSimulacion.totalPaquetes === 0 && (
            <div className="text-center py-4">
              <Package className="w-12 h-12 text-gray-300 mx-auto mb-2" />
              <div className="text-gray-600 font-medium">Sin paquetes disponibles</div>
              <div className="text-gray-500 text-sm">
                Inicia una simulación para generar datos
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }

  // Renderizado por defecto
  return (
    <div className="bg-white rounded-lg shadow-md border border-gray-200 p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center space-x-2">
          <Package className="w-5 h-5 text-blue-500" />
          <span className="font-medium text-gray-700">Paquete Actual</span>
        </div>
        <div className={`w-2 h-2 rounded-full ${
          infoSimulacion.enProceso ? 'bg-green-500' : 'bg-red-500'
        }`}></div>
      </div>

      <div className="text-center mb-4">
        <div className="text-4xl font-bold text-gray-900 mb-1">
          {infoSimulacion.paqueteActual}
        </div>
        <div className="text-gray-600">
          de {infoSimulacion.totalPaquetes} paquetes
        </div>
      </div>

      {showProgress && infoSimulacion.totalPaquetes > 0 && (
        <div className="mb-3">
          <div className="flex justify-between text-sm text-gray-600 mb-1">
            <span>Progreso</span>
            <span>{Math.round(porcentajeProgreso)}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div 
              className="bg-blue-500 h-2 rounded-full transition-all duration-300"
              style={{ width: `${porcentajeProgreso}%` }}
            ></div>
          </div>
        </div>
      )}

      {showTime && infoSimulacion.tiempoActual && (
        <div className="text-center text-sm text-gray-600">
          <Clock className="w-4 h-4 inline mr-1" />
          {new Date(infoSimulacion.tiempoActual).toLocaleString('es-ES', {
            hour: '2-digit',
            minute: '2-digit',
            day: '2-digit',
            month: '2-digit'
          })}
        </div>
      )}

      {infoSimulacion.totalPaquetes === 0 && (
        <div className="text-center py-2">
          <div className="text-gray-500 text-sm">Sin paquetes disponibles</div>
          <div className="text-gray-400 text-xs">Inicia una simulación</div>
        </div>
      )}
    </div>
  );
};

export default IndicadorPaqueteActual; 