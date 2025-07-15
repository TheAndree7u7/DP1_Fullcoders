import React, { useState } from 'react';
import { useMapaState } from '../hooks/useMapaContext';
import { formatearCoordenada } from '../types';

// ============================
// INTERFACES
// ============================

interface RutasCamionesProps {
  camionSeleccionado?: string | null;
  onCamionSeleccionado?: (camionId: string | null) => void;
  className?: string;
}

// ============================
// COMPONENTE PRINCIPAL
// ============================

const RutasCamiones: React.FC<RutasCamionesProps> = ({
  camionSeleccionado,
  onCamionSeleccionado,
  className = ''
}) => {
  const { camiones, isLoading, error } = useMapaState();
  const [expandido, setExpandido] = useState<string | null>(camionSeleccionado || null);

  // Estado de carga
  if (isLoading) {
    return (
      <div className={`bg-white rounded-lg p-4 ${className}`}>
        <div className="animate-pulse">
          <div className="h-4 bg-gray-200 rounded w-1/2 mb-3"></div>
          <div className="space-y-2">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-3 bg-gray-200 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  // Estado de error
  if (error) {
    return (
      <div className={`bg-red-50 border border-red-200 rounded-lg p-4 ${className}`}>
        <h3 className="text-sm font-medium text-red-800 mb-2">
          Error en Rutas
        </h3>
        <p className="text-sm text-red-600">{error}</p>
      </div>
    );
  }

  // Sin camiones
  if (camiones.length === 0) {
    return (
      <div className={`bg-gray-50 border border-gray-200 rounded-lg p-4 ${className}`}>
        <h3 className="text-sm font-medium text-gray-700 mb-2">
          Rutas de Camiones
        </h3>
        <p className="text-sm text-gray-500">
          No hay camiones disponibles. Cargue un paquete para ver las rutas.
        </p>
      </div>
    );
  }

  const handleToggleCamion = (camionId: string) => {
    const nuevoExpandido = expandido === camionId ? null : camionId;
    setExpandido(nuevoExpandido);
    onCamionSeleccionado?.(nuevoExpandido);
  };

  const obtenerColorEstado = (estado: string): string => {
    switch (estado) {
      case 'DISPONIBLE': return 'bg-green-100 text-green-800';
      case 'EN_RUTA': return 'bg-blue-100 text-blue-800';
      case 'ENTREGANDO_GLP_A_CLIENTE': return 'bg-purple-100 text-purple-800';
      case 'EN_MANTENIMIENTO': return 'bg-gray-100 text-gray-800';
      case 'SIN_COMBUSTIBLE': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const formatearEstado = (estado: string): string => {
    return estado.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  };

  return (
    <div className={`bg-white rounded-lg shadow-sm border p-4 ${className}`}>
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-800">
          Rutas de Camiones
        </h3>
        <span className="text-sm text-gray-500">
          {camiones.length} camion{camiones.length !== 1 ? 'es' : ''}
        </span>
      </div>

      <div className="space-y-2 max-h-96 overflow-y-auto">
        {camiones.map((camion) => (
          <div key={camion.id} className="border border-gray-200 rounded-lg">
            {/* Header del camión */}
            <button
              onClick={() => handleToggleCamion(camion.id)}
              className="w-full p-3 text-left hover:bg-gray-50 transition-colors"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div 
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: camion.color }}
                  ></div>
                  <span className="font-medium text-gray-800">
                    {camion.codigo}
                  </span>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${obtenerColorEstado(camion.estado)}`}>
                    {formatearEstado(camion.estado)}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  {camion.pedidosAsignados.length > 0 && (
                    <span className="text-xs text-gray-500">
                      {camion.pedidosAsignados.length} pedido{camion.pedidosAsignados.length !== 1 ? 's' : ''}
                    </span>
                  )}
                  <span className="text-xs text-gray-400">
                    {expandido === camion.id ? '▼' : '▶'}
                  </span>
                </div>
              </div>
            </button>

            {/* Detalles expandidos */}
            {expandido === camion.id && (
              <div className="border-t border-gray-100 p-3 bg-gray-50">
                <div className="space-y-3">
                  {/* Información básica */}
                  <div className="grid grid-cols-2 gap-3 text-sm">
                    <div>
                      <span className="text-gray-600">Posición:</span>
                      <div className="font-mono text-xs text-gray-800">
                        {formatearCoordenada(camion.posicion)}
                      </div>
                    </div>
                    {camion.destino && (
                      <div>
                        <span className="text-gray-600">Destino:</span>
                        <div className="font-mono text-xs text-gray-800">
                          {formatearCoordenada(camion.destino)}
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Progreso */}
                  {camion.progreso > 0 && (
                    <div>
                      <div className="flex justify-between text-xs text-gray-600 mb-1">
                        <span>Progreso de ruta</span>
                        <span>{camion.progreso}%</span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-1.5">
                        <div 
                          className="bg-blue-600 h-1.5 rounded-full transition-all duration-300"
                          style={{ width: `${Math.min(camion.progreso, 100)}%` }}
                        ></div>
                      </div>
                    </div>
                  )}

                  {/* Pedidos asignados */}
                  {camion.pedidosAsignados.length > 0 && (
                    <div>
                      <span className="text-sm text-gray-600 font-medium">
                        Pedidos asignados:
                      </span>
                      <div className="mt-1 flex flex-wrap gap-1">
                        {camion.pedidosAsignados.map((pedidoId) => (
                          <span 
                            key={pedidoId}
                            className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full font-mono"
                          >
                            {pedidoId}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Ruta */}
                  {camion.ruta.length > 0 && (
                    <div>
                      <span className="text-sm text-gray-600 font-medium">
                        Ruta ({camion.ruta.length} puntos):
                      </span>
                      <div className="mt-1 max-h-20 overflow-y-auto bg-white rounded border p-2">
                        <div className="flex flex-wrap gap-1 text-xs font-mono">
                          {camion.ruta.slice(0, 10).map((punto, index) => (
                            <span key={index} className="text-gray-600">
                              {formatearCoordenada(punto)}
                              {index < camion.ruta.length - 1 && index < 9 && ' → '}
                            </span>
                          ))}
                          {camion.ruta.length > 10 && (
                            <span className="text-gray-400">
                              ... +{camion.ruta.length - 10} más
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default RutasCamiones;
