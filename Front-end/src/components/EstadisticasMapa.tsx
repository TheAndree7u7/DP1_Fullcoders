import React from 'react';
import { useMapaStats, useMapaState } from '../hooks/useMapaContext';

// ============================
// INTERFACES
// ============================

interface EstadisticasMapaProps {
  variant?: 'compact' | 'detailed';
  className?: string;
}

// ============================
// COMPONENTE PRINCIPAL
// ============================

const EstadisticasMapa: React.FC<EstadisticasMapaProps> = ({
  variant = 'detailed',
  className = ''
}) => {
  const { 
    totalCamiones, 
    camionesPorEstado, 
    totalPedidos, 
    pedidosAsignados, 
    pedidosSinAsignar 
  } = useMapaStats();
  
  const { isLoading, error } = useMapaState();

  // Estado de carga
  if (isLoading) {
    return (
      <div className={`bg-white rounded-lg p-4 ${className}`}>
        <div className="animate-pulse">
          <div className="h-4 bg-gray-200 rounded w-3/4 mb-3"></div>
          <div className="h-3 bg-gray-200 rounded w-1/2 mb-2"></div>
          <div className="h-3 bg-gray-200 rounded w-2/3"></div>
        </div>
      </div>
    );
  }

  // Estado de error
  if (error) {
    return (
      <div className={`bg-red-50 border border-red-200 rounded-lg p-4 ${className}`}>
        <h3 className="text-sm font-medium text-red-800 mb-2">
          Error en Estadísticas
        </h3>
        <p className="text-sm text-red-600">{error}</p>
      </div>
    );
  }

  // Renderizado según variante
  if (variant === 'compact') {
    return renderCompact();
  }

  return renderDetailed();

  function renderCompact() {
    return (
      <div className={`inline-flex items-center gap-3 text-sm ${className}`}>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
          <span>{totalCamiones} camiones</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 bg-green-500 rounded-full"></div>
          <span>{pedidosAsignados}/{totalPedidos} pedidos</span>
        </div>
      </div>
    );
  }

  function renderDetailed() {
    return (
      <div className={`bg-white rounded-lg shadow-sm border p-4 ${className}`}>
        <h3 className="text-lg font-semibold mb-4 text-gray-800">
          Estadísticas del Mapa
        </h3>

        <div className="space-y-4">
          {/* Estadísticas de Camiones */}
          <div className="bg-blue-50 rounded-lg p-3">
            <h4 className="text-sm font-medium text-blue-800 mb-2">
              Camiones ({totalCamiones})
            </h4>
            <div className="grid grid-cols-2 gap-2 text-xs">
              {Object.entries(camionesPorEstado).map(([estado, cantidad]) => (
                <div key={estado} className="flex justify-between">
                  <span className="text-blue-700 capitalize">
                    {estado.toLowerCase().replace('_', ' ')}:
                  </span>
                  <span className="font-medium text-blue-800">{cantidad}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Estadísticas de Pedidos */}
          <div className="bg-green-50 rounded-lg p-3">
            <h4 className="text-sm font-medium text-green-800 mb-2">
              Pedidos ({totalPedidos})
            </h4>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <div className="flex justify-between">
                <span className="text-green-700">Asignados:</span>
                <span className="font-medium text-green-800">{pedidosAsignados}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-green-700">Sin asignar:</span>
                <span className="font-medium text-green-800">{pedidosSinAsignar}</span>
              </div>
            </div>
          </div>

          {/* Indicador de progreso */}
          {totalPedidos > 0 && (
            <div className="bg-gray-50 rounded-lg p-3">
              <div className="flex justify-between text-xs text-gray-600 mb-1">
                <span>Progreso de asignación</span>
                <span>{Math.round((pedidosAsignados / totalPedidos) * 100)}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${(pedidosAsignados / totalPedidos) * 100}%` }}
                ></div>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }
};

export default EstadisticasMapa;
