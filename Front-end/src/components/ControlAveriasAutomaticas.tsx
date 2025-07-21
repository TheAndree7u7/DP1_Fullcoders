import React, { useState } from 'react';
import { AVERIAS_AUTOMATICAS_CONFIG } from '../config/constants';
import { Settings, AlertTriangle, Info } from 'lucide-react';

/**
 * @component ControlAveriasAutomaticas
 * @description Componente para controlar la configuración de averías automáticas
 */
const ControlAveriasAutomaticas: React.FC = () => {
  const [configVisible, setConfigVisible] = useState(false);
  const [config, setConfig] = useState({
    activado: AVERIAS_AUTOMATICAS_CONFIG.ACTIVADO,
    paquetesParaAveria: AVERIAS_AUTOMATICAS_CONFIG.PAQUETES_PARA_AVERIA,
    porcentajeMinimo: AVERIAS_AUTOMATICAS_CONFIG.PORCENTAJE_MINIMO_TIEMPO * 100,
    porcentajeMaximo: AVERIAS_AUTOMATICAS_CONFIG.PORCENTAJE_MAXIMO_TIEMPO * 100,
    prioridadCapacidadMinima: AVERIAS_AUTOMATICAS_CONFIG.PRIORIDAD_CAPACIDAD_MINIMA
  });

  const handleConfigChange = (key: string, value: boolean | number) => {
    setConfig(prev => ({
      ...prev,
      [key]: value
    }));
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4 mb-4">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <AlertTriangle className="w-5 h-5 text-orange-500" />
          <h3 className="text-lg font-semibold text-gray-800">
            Averías Automáticas
          </h3>
        </div>
        <button
          onClick={() => setConfigVisible(!configVisible)}
          className="flex items-center gap-1 text-sm text-gray-600 hover:text-gray-800 transition-colors"
        >
          <Settings className="w-4 h-4" />
          Configuración
        </button>
      </div>

      {/* Estado actual */}
      <div className="flex items-center gap-2 mb-3">
        <div className={`w-3 h-3 rounded-full ${config.activado ? 'bg-green-500' : 'bg-red-500'}`}></div>
        <span className="text-sm font-medium">
          {config.activado ? 'Activado' : 'Desactivado'}
        </span>
      </div>

      {/* Información básica */}
      <div className="text-sm text-gray-600 mb-4">
        <p>• Ocurre cada {config.paquetesParaAveria} paquete(s)</p>
        <p>• Entre {config.porcentajeMinimo}% y {config.porcentajeMaximo}% del tiempo del intervalo</p>
        <p>• Prioridad: {config.prioridadCapacidadMinima ? 'Menor capacidad primero' : 'Aleatorio'}</p>
      </div>

      {/* Configuración expandible */}
      {configVisible && (
        <div className="border-t pt-4 space-y-4">
          <div className="flex items-center justify-between">
            <label className="text-sm font-medium text-gray-700">
              Activar averías automáticas
            </label>
            <input
              type="checkbox"
              checked={config.activado}
              onChange={(e) => handleConfigChange('activado', e.target.checked)}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="text-sm font-medium text-gray-700 block mb-1">
              Paquetes para avería
            </label>
            <input
              type="number"
              min="1"
              max="10"
              value={config.paquetesParaAveria}
              onChange={(e) => handleConfigChange('paquetesParaAveria', parseInt(e.target.value))}
              className="w-20 px-2 py-1 border border-gray-300 rounded text-sm"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">
                % Mínimo tiempo
              </label>
              <input
                type="number"
                min="1"
                max="50"
                value={config.porcentajeMinimo}
                onChange={(e) => handleConfigChange('porcentajeMinimo', parseInt(e.target.value))}
                className="w-full px-2 py-1 border border-gray-300 rounded text-sm"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">
                % Máximo tiempo
              </label>
              <input
                type="number"
                min="1"
                max="100"
                value={config.porcentajeMaximo}
                onChange={(e) => handleConfigChange('porcentajeMaximo', parseInt(e.target.value))}
                className="w-full px-2 py-1 border border-gray-300 rounded text-sm"
              />
            </div>
          </div>

          <div className="flex items-center justify-between">
            <label className="text-sm font-medium text-gray-700">
              Prioridad por menor capacidad
            </label>
            <input
              type="checkbox"
              checked={config.prioridadCapacidadMinima}
              onChange={(e) => handleConfigChange('prioridadCapacidadMinima', e.target.checked)}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
          </div>

          {/* Información adicional */}
          <div className="bg-blue-50 border border-blue-200 rounded p-3">
            <div className="flex items-start gap-2">
              <Info className="w-4 h-4 text-blue-500 mt-0.5 flex-shrink-0" />
              <div className="text-xs text-blue-700">
                <p className="font-medium mb-1">Criterios de selección:</p>
                <ul className="space-y-1">
                  <li>• Solo camiones "En Ruta"</li>
                  <li>• No en nodos de tipo PEDIDO</li>
                  <li>• No averiados recientemente</li>
                  <li>• Con menor capacidad GLP (si está habilitado)</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ControlAveriasAutomaticas; 