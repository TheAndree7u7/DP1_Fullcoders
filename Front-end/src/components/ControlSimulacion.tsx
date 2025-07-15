import React, { useState } from 'react';
import { useSimulacion } from '../hooks/useSimulacionContext';
import { usePaqueteService } from '../hooks/usePaqueteService';
import { useSimulacionUpdater } from '../hooks/useSimulacionUpdater';
import { iniciarSimulacion as iniciarSimulacionApi } from '../services/simulacionApiService';
import { formatearFechaParaAPI, obtenerFechaInicioDefault } from '../services/dateUtils';
import Modal from './Modal';

interface ControlSimulacionProps {
  isOpen?: boolean;
  onClose?: () => void;
  showAsModal?: boolean;
}

const ControlSimulacion: React.FC<ControlSimulacionProps> = ({
  isOpen = true,
  onClose = () => {},
  showAsModal = false
}) => {
  const { isLoading, setLoading, setError, error } = useSimulacion();
  const { cargarPaquete } = usePaqueteService();
  const { actualizarDesdeIndividuo } = useSimulacionUpdater();
  
  const fechaDefault = obtenerFechaInicioDefault();
  const [fechaInicio, setFechaInicio] = useState(fechaDefault);
  const [mensaje, setMensaje] = useState<string>('');

  const handleIniciar = async () => {
    try {
      setLoading(true);
      setError(null);
      setMensaje('');
      
      const fechaFormateada = formatearFechaParaAPI(fechaInicio);
      
      // Iniciar simulación
      const respuesta = await iniciarSimulacionApi(fechaFormateada);
      setMensaje(respuesta);
      
      // Cargar el paquete inmediatamente después de iniciar
      try {
        const paquete = await cargarPaquete(fechaFormateada);
        
        // 🚀 Actualizar contexto de simulación con datos del paquete
        actualizarDesdeIndividuo(paquete);
        
        setMensaje(respuesta + ' - Paquete cargado exitosamente');
        
        // Cerrar el modal después de éxito solo si es modal independiente
        if (showAsModal) {
          setTimeout(() => {
            onClose();
          }, 2000);
        }
      } catch (paqueteError) {
        const paqueteErrorMessage = paqueteError instanceof Error 
          ? paqueteError.message 
          : 'Error al cargar paquete';
        setMensaje(respuesta + ` - Advertencia: ${paqueteErrorMessage}`);
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error 
        ? error.message 
        : 'Error desconocido al iniciar simulación';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // Renderizar como modal o como componente
  if (showAsModal) {
    return (
      <Modal
        isOpen={isOpen}
        onClose={onClose}
        title="Control de Simulación"
        size="md"
      >
        {renderControlContent()}
      </Modal>
    );
  }

  return renderControlContent();

  function renderControlContent() {
    return (
      <div className="space-y-4">
        <div>
          <label 
            htmlFor="fecha-inicio" 
            className="block text-sm font-medium text-gray-700 mb-2"
          >
            Fecha y Hora de Inicio
          </label>
          <input
            id="fecha-inicio"
            type="datetime-local"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
            disabled={isLoading}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed text-gray-900 bg-white"
          />
        </div>

        <button
          onClick={handleIniciar}
          disabled={isLoading || !fechaInicio}
          className="w-full bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors font-medium"
        >
          {isLoading ? 'Iniciando...' : 'Iniciar Simulación'}
        </button>

        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-md">
            <p className="text-sm text-red-700 font-medium">
              Error al iniciar:
            </p>
            <p className="text-sm text-red-600 mt-1">
              {error}
            </p>
          </div>
        )}

        {mensaje && !error && (
          <div className="p-3 bg-green-50 border border-green-200 rounded-md">
            <p className="text-sm text-green-700 font-medium">
              Simulación iniciada:
            </p>
            <p className="text-sm text-green-600 mt-1">
              {mensaje}
            </p>
          </div>
        )}
      </div>
    );
  }
};

export default ControlSimulacion;
