import React from 'react';
import { AVERIAS_AUTOMATICAS_CONFIG } from '../config/constants';
import { useSimulacion } from '../context/SimulacionContext';
import { diagnosticarEstadoAveriasAutomaticas } from '../utils/debugAveriasAutomaticas';

/**
 * @component ControlAveriasAutomaticas
 * @description Componente para controlar y configurar averías automáticas
 */
const ControlAveriasAutomaticas: React.FC = () => {
  const { 
    estadoAveriasAutomaticas, 
    paqueteActualConsumido, 
    camiones, 
    rutasCamiones, 
    fechaHoraInicioIntervalo, 
    fechaHoraFinIntervalo,
    simulacionActiva,
    limpiarEstadoAveriasAutomaticas
  } = useSimulacion();

  const ejecutarDiagnostico = () => {
    diagnosticarEstadoAveriasAutomaticas(
      estadoAveriasAutomaticas,
      paqueteActualConsumido,
      camiones,
      rutasCamiones,
      fechaHoraInicioIntervalo,
      fechaHoraFinIntervalo,
      simulacionActiva,
      true // running - asumimos que está activo si la simulación está activa
    );
  };

  return (
    <div className="bg-white border border-gray-300 rounded-lg p-4 shadow-lg">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-800">
          🚛💥 Averías Automáticas
        </h3>
        <div className="flex gap-2">
          <button
            onClick={ejecutarDiagnostico}
            className="px-3 py-1 bg-blue-500 text-white text-xs rounded hover:bg-blue-600 transition-colors"
            title="Ejecutar diagnóstico"
          >
            🔍 Debug
          </button>
          <button
            onClick={limpiarEstadoAveriasAutomaticas}
            className="px-3 py-1 bg-red-500 text-white text-xs rounded hover:bg-red-600 transition-colors"
            title="Limpiar estado de averías"
          >
            🔄 Reset
          </button>
        </div>
      </div>

      {/* Estado actual */}
      <div className="mb-4 p-3 bg-gray-50 rounded">
        <h4 className="font-medium text-gray-700 mb-2">Estado Actual:</h4>
        <div className="text-sm space-y-1">
          <div className="flex justify-between">
            <span>Activado:</span>
            <span className={`font-semibold ${AVERIAS_AUTOMATICAS_CONFIG.ACTIVADO ? 'text-green-600' : 'text-red-600'}`}>
              {AVERIAS_AUTOMATICAS_CONFIG.ACTIVADO ? 'SÍ' : 'NO'}
            </span>
          </div>
          <div className="flex justify-between">
            <span>Contador paquetes:</span>
            <span className="font-semibold">{estadoAveriasAutomaticas?.contadorPaquetes || 0}</span>
          </div>
          <div className="flex justify-between">
            <span>Paquete actual:</span>
            <span className="font-semibold">{paqueteActualConsumido}</span>
          </div>
          <div className="flex justify-between">
            <span>Simulación activa:</span>
            <span className={`font-semibold ${simulacionActiva ? 'text-green-600' : 'text-red-600'}`}>
              {simulacionActiva ? 'SÍ' : 'NO'}
            </span>
          </div>
        </div>
      </div>

      {/* Configuración */}
      <div className="mb-4">
        <h4 className="font-medium text-gray-700 mb-2">Configuración:</h4>
        <div className="text-sm space-y-2">
          <div className="flex justify-between">
            <span>Paquetes para avería:</span>
            <span className="font-semibold">{AVERIAS_AUTOMATICAS_CONFIG.PAQUETES_PARA_AVERIA}</span>
          </div>
          <div className="flex justify-between">
            <span>Prioridad capacidad mínima:</span>
            <span className={`font-semibold ${AVERIAS_AUTOMATICAS_CONFIG.PRIORIDAD_CAPACIDAD_MINIMA ? 'text-green-600' : 'text-red-600'}`}>
              {AVERIAS_AUTOMATICAS_CONFIG.PRIORIDAD_CAPACIDAD_MINIMA ? 'SÍ' : 'NO'}
            </span>
          </div>
          <div className="flex justify-between">
            <span>Tipos de avería:</span>
            <span className="font-semibold">{AVERIAS_AUTOMATICAS_CONFIG.TIPOS_AVERIA_DISPONIBLES.join(', ')}</span>
          </div>
        </div>
      </div>

      {/* Criterios de selección */}
      <div className="mb-4">
        <h4 className="font-medium text-gray-700 mb-2">Criterios de Selección:</h4>
        <div className="text-sm space-y-1">
          <div>• Solo camiones "En Ruta"</div>
          <div>• No en nodos de tipo "PEDIDO"</div>
          <div>• No haber tenido avería automática reciente</div>
          {AVERIAS_AUTOMATICAS_CONFIG.PRIORIDAD_CAPACIDAD_MINIMA && (
            <div>• Prioridad: menor capacidad primero</div>
          )}
        </div>
      </div>

      {/* Información adicional */}
      <div className="text-xs text-gray-500">
        <div>📊 Camiones disponibles: {camiones?.length || 0}</div>
        <div>🗺️ Rutas disponibles: {rutasCamiones?.length || 0}</div>
        <div>📅 Fechas intervalo: {fechaHoraInicioIntervalo && fechaHoraFinIntervalo ? 'Disponibles' : 'No disponibles'}</div>
      </div>
    </div>
  );
};

export default ControlAveriasAutomaticas; 