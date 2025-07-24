/**
 * @file ControlVelocidad.tsx
 * @description Componente para controlar la velocidad de la simulación basada en la velocidad de los camiones
 */

import React, { useState, useEffect } from 'react';
import type { CamionEstado } from '../context/simulacion/types';
import {
  calcularVelocidadPromedioCamiones,
  calcularIntervaloDinamico,
  formatearVelocidad,
  obtenerRangoVelocidad
} from '../context/simulacion/utils';

interface ControlVelocidadProps {
  camiones: CamionEstado[];
  segundosPorNodo: number;
  onSegundosPorNodoChange: (segundos: number) => void;
  onIntervaloChange: (intervalo: number) => void;
  intervaloActual: number;
}

const ControlVelocidad: React.FC<ControlVelocidadProps> = ({
  camiones,
  segundosPorNodo,
  onSegundosPorNodoChange,
  onIntervaloChange,
  intervaloActual
}) => {
  const [modoDinamico, setModoDinamico] = useState(false);
  const [velocidadPromedio, setVelocidadPromedio] = useState(60);
  const [rangoVelocidad, setRangoVelocidad] = useState({ min: 60, max: 60, promedio: 60 });

  // Calcular velocidad promedio y rango cuando cambien los camiones
  useEffect(() => {
    const promedio = calcularVelocidadPromedioCamiones(camiones);
    const rango = obtenerRangoVelocidad(camiones);
    setVelocidadPromedio(promedio);
    setRangoVelocidad(rango);
  }, [camiones]);

  // Calcular intervalo dinámico cuando cambie la configuración
  useEffect(() => {
    if (modoDinamico) {
      const nuevoIntervalo = calcularIntervaloDinamico(segundosPorNodo, camiones);
      onIntervaloChange(nuevoIntervalo);
    }
  }, [modoDinamico, segundosPorNodo, camiones, onIntervaloChange]);

  const handleModoDinamicoChange = (activo: boolean) => {
    setModoDinamico(activo);
    if (activo) {
      // Recalcular intervalo con velocidad dinámica
      const nuevoIntervalo = calcularIntervaloDinamico(segundosPorNodo, camiones);
      onIntervaloChange(nuevoIntervalo);
    }
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-md border">
      <h3 className="text-lg font-semibold mb-3 text-gray-800">
        ⚡ Control de Velocidad de Simulación
      </h3>
      
      {/* Información de velocidad de camiones */}
      <div className="mb-4 p-3 bg-gray-50 rounded">
        <h4 className="font-medium text-gray-700 mb-2">📊 Velocidad de Camiones</h4>
        <div className="grid grid-cols-3 gap-2 text-sm">
          <div>
            <span className="text-gray-600">Promedio:</span>
            <div className="font-semibold">{formatearVelocidad(velocidadPromedio)}</div>
          </div>
          <div>
            <span className="text-gray-600">Mínima:</span>
            <div className="font-semibold">{formatearVelocidad(rangoVelocidad.min)}</div>
          </div>
          <div>
            <span className="text-gray-600">Máxima:</span>
            <div className="font-semibold">{formatearVelocidad(rangoVelocidad.max)}</div>
          </div>
        </div>
      </div>

      {/* Control de segundos por nodo */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          ⏱️ Segundos por nodo (tiempo real):
        </label>
        <div className="flex items-center gap-2">
          <input
            type="number"
            min={0.1}
            max={100}
            step={0.1}
            value={segundosPorNodo}
            onChange={(e) => onSegundosPorNodoChange(parseFloat(e.target.value))}
            className="flex-1 border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={modoDinamico}
          />
          <span className="text-sm text-gray-600">segundos</span>
        </div>
        <p className="text-xs text-gray-500 mt-1">
          Tiempo que dura cada nodo en la simulación en tiempo real
        </p>
      </div>

      {/* Modo dinámico */}
      <div className="mb-4">
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={modoDinamico}
            onChange={(e) => handleModoDinamicoChange(e.target.checked)}
            className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span className="text-sm font-medium text-gray-700">
            🚀 Modo dinámico (ajustar según velocidad de camiones)
          </span>
        </label>
        <p className="text-xs text-gray-500 mt-1 ml-6">
          El intervalo se ajusta automáticamente según la velocidad promedio de los camiones activos
        </p>
      </div>

      {/* Información del intervalo actual */}
      <div className="p-3 bg-blue-50 rounded border border-blue-200">
        <h4 className="font-medium text-blue-800 mb-2">📈 Intervalo Actual</h4>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span className="text-blue-600">Intervalo:</span>
            <div className="font-semibold text-blue-800">{intervaloActual}ms</div>
          </div>
          <div>
            <span className="text-blue-600">Frecuencia:</span>
            <div className="font-semibold text-blue-800">
              {(1000 / intervaloActual).toFixed(1)} actualizaciones/s
            </div>
          </div>
        </div>
        {modoDinamico && (
          <div className="mt-2 text-xs text-blue-600">
            💡 Ajustado dinámicamente según velocidad promedio: {formatearVelocidad(velocidadPromedio)}
          </div>
        )}
      </div>

      {/* Presets de velocidad */}
      <div className="mt-4">
        <h4 className="text-sm font-medium text-gray-700 mb-2">⚡ Presets de Velocidad</h4>
        <div className="flex flex-wrap gap-2">
          {[
            { label: 'Lento', segundos: 120 },
            { label: 'Normal', segundos: 62.9 },
            { label: 'Rápido', segundos: 30 },
            { label: 'Muy Rápido', segundos: 10 }
          ].map((preset) => (
            <button
              key={preset.label}
              onClick={() => onSegundosPorNodoChange(preset.segundos)}
              disabled={modoDinamico}
              className={`px-3 py-1 text-xs rounded border transition-colors ${
                segundosPorNodo === preset.segundos
                  ? 'bg-blue-500 text-white border-blue-500'
                  : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
              } ${modoDinamico ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
              {preset.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ControlVelocidad; 