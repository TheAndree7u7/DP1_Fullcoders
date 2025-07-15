import React from 'react';
import { usePaqueteState, usePaqueteInfo } from '../hooks/usePaqueteContext';
import { formatearFecha } from '../types';

// ============================
// INTERFACES
// ============================

interface PaqueteDisplayProps {
  variant?: 'compact' | 'detailed' | 'card';
  showHeader?: boolean;
  className?: string;
}

// ============================
// COMPONENTE PRINCIPAL
// ============================

const PaqueteDisplay: React.FC<PaqueteDisplayProps> = ({
  variant = 'detailed',
  showHeader = true,
  className = ''
}) => {
  const { isLoading, error, ultimaActualizacion } = usePaqueteState();
  const { totalCamiones, totalPedidos, fechaSimulacion, tienePaquete } = usePaqueteInfo();

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
        {showHeader && (
          <h3 className="text-sm font-medium text-red-800 mb-2">
            Error en Paquete
          </h3>
        )}
        <p className="text-sm text-red-600">{error}</p>
      </div>
    );
  }

  // Sin paquete disponible
  if (!tienePaquete) {
    return (
      <div className={`bg-gray-50 border border-gray-200 rounded-lg p-4 ${className}`}>
        {showHeader && (
          <h3 className="text-sm font-medium text-gray-700 mb-2">
            Paquete Actual
          </h3>
        )}
        <p className="text-sm text-gray-500">
          No hay paquete disponible. Inicie la simulación para cargar un paquete.
        </p>
      </div>
    );
  }

  // Renderizado según variante
  switch (variant) {
    case 'compact':
      return renderCompact();
    case 'card':
      return renderCard();
    default:
      return renderDetailed();
  }

  function renderCompact() {
    return (
      <div className={`inline-flex items-center gap-2 text-sm ${className}`}>
        <span className="font-medium">Paquete:</span>
        <span>{totalCamiones} camiones</span>
        <span>•</span>
        <span>{totalPedidos} pedidos</span>
      </div>
    );
  }

  function renderCard() {
    return (
      <div className={`bg-white border border-gray-200 rounded-lg p-4 shadow-sm ${className}`}>
        {showHeader && (
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-lg font-semibold text-gray-800">
              Paquete Actual
            </h3>
            <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse" 
                 title="Paquete cargado"></div>
          </div>
        )}
        
        <div className="grid grid-cols-2 gap-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">{totalCamiones}</div>
            <div className="text-sm text-gray-600">Camiones</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">{totalPedidos}</div>
            <div className="text-sm text-gray-600">Pedidos</div>
          </div>
        </div>
        
        {fechaSimulacion && (
          <div className="mt-3 pt-3 border-t border-gray-100">
            <div className="text-xs text-gray-500">
              Fecha simulación: {formatearFecha(fechaSimulacion)}
            </div>
          </div>
        )}
      </div>
    );
  }

  function renderDetailed() {
    return (
      <div className={`bg-white rounded-lg shadow-sm border p-4 ${className}`}>
        {showHeader && (
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-800">
              Información del Paquete
            </h3>
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
              <span className="text-xs text-green-600 font-medium">Activo</span>
            </div>
          </div>
        )}

        <div className="space-y-3">
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-blue-50 rounded-lg p-3">
              <div className="text-sm text-blue-600 font-medium">Camiones</div>
              <div className="text-xl font-bold text-blue-800">{totalCamiones}</div>
            </div>
            
            <div className="bg-green-50 rounded-lg p-3">
              <div className="text-sm text-green-600 font-medium">Pedidos</div>
              <div className="text-xl font-bold text-green-800">{totalPedidos}</div>
            </div>
          </div>

          {fechaSimulacion && (
            <div className="bg-gray-50 rounded-lg p-3">
              <div className="text-sm text-gray-600 font-medium mb-1">
                Fecha de Simulación
              </div>
              <div className="text-sm font-mono text-gray-800">
                {formatearFecha(fechaSimulacion)}
              </div>
            </div>
          )}

          {ultimaActualizacion && (
            <div className="text-xs text-gray-500 text-center pt-2 border-t">
              Actualizado: {formatearFecha(ultimaActualizacion.toISOString())}
            </div>
          )}
        </div>
      </div>
    );
  }
};

export default PaqueteDisplay;
