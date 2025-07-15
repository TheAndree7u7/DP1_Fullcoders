import React, { useState } from 'react';
import { usePaqueteService } from '../hooks/usePaqueteService';
import { usePaqueteState } from '../hooks/usePaqueteContext';
import { formatearFechaParaAPI, obtenerFechaInicioDefault } from '../services/dateUtils';

// ============================
// COMPONENTE PARA CARGAR PAQUETE MANUALMENTE
// ============================

const CargadorPaquete: React.FC = () => {
  const { cargarPaquete } = usePaqueteService();
  const { isLoading, error } = usePaqueteState();
  const [fecha, setFecha] = useState(obtenerFechaInicioDefault());
  const [mensaje, setMensaje] = useState<string>('');

  const handleCargarPaquete = async () => {
    try {
      setMensaje('');
      const fechaFormateada = formatearFechaParaAPI(fecha);
      await cargarPaquete(fechaFormateada);
      setMensaje('Paquete cargado exitosamente');
    } catch (error) {
      const errorMessage = error instanceof Error 
        ? error.message 
        : 'Error desconocido al cargar paquete';
      setMensaje(`Error: ${errorMessage}`);
    }
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-sm border">
      <h3 className="text-lg font-semibold mb-4 text-gray-800">
        Cargar Paquete
      </h3>
      
      <div className="space-y-4">
        <div>
          <label 
            htmlFor="fecha-paquete" 
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Fecha del Paquete
          </label>
          <input
            id="fecha-paquete"
            type="datetime-local"
            value={fecha}
            onChange={(e) => setFecha(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
          />
        </div>

        <button
          onClick={handleCargarPaquete}
          disabled={isLoading || !fecha}
          className="w-full bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors font-medium"
        >
          {isLoading ? 'Cargando...' : 'Cargar Paquete'}
        </button>

        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-md">
            <p className="text-sm text-red-700 font-medium">
              Error:
            </p>
            <p className="text-sm text-red-600 mt-1">
              {error}
            </p>
          </div>
        )}

        {mensaje && !error && (
          <div className="p-3 bg-green-50 border border-green-200 rounded-md">
            <p className="text-sm text-green-600">
              {mensaje}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default CargadorPaquete;
