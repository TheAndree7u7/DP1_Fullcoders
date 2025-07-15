import React from 'react';
import Modal from './Modal';
import PaqueteDisplay from './PaqueteDisplay';
import EstadisticasMapa from './EstadisticasMapa';
import RutasCamiones from './RutasCamiones';
import { usePaqueteState } from '../hooks/usePaqueteContext';

// ============================
// INTERFACES
// ============================

interface ModalInformacionPaqueteProps {
  isOpen: boolean;
  onClose: () => void;
}

// ============================
// COMPONENTE PRINCIPAL
// ============================

const ModalInformacionPaquete: React.FC<ModalInformacionPaqueteProps> = ({
  isOpen,
  onClose
}) => {
  const { paqueteActual: paquete, isLoading, error } = usePaqueteState();

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Información del Paquete de Simulación"
      size="xl"
    >
      <div className="space-y-6">
        {/* Estado de carga */}
        {isLoading && (
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span className="ml-3 text-gray-600">Cargando información del paquete...</span>
          </div>
        )}

        {/* Estado de error */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <h3 className="text-sm font-medium text-red-800 mb-2">
              Error al cargar el paquete
            </h3>
            <p className="text-sm text-red-600">{error}</p>
          </div>
        )}

        {/* Contenido del paquete */}
        {paquete && !isLoading && !error && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Información del paquete */}
            <div className="space-y-4">
              <PaqueteDisplay 
                variant="detailed" 
                className="h-fit"
              />
              
              <EstadisticasMapa 
                variant="detailed"
                className="h-fit"
              />
            </div>

            {/* Rutas de camiones */}
            <div>
              <RutasCamiones className="h-full" />
            </div>
          </div>
        )}

        {/* Estado sin paquete */}
        {!paquete && !isLoading && !error && (
          <div className="text-center py-8">
            <div className="mx-auto w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <svg 
                className="w-8 h-8 text-gray-400" 
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path 
                  strokeLinecap="round" 
                  strokeLinejoin="round" 
                  strokeWidth={2} 
                  d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2 2v-5m16 0h-2M4 13h2m13-8V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v1M7 10h10" 
                />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              No hay paquete cargado
            </h3>
            <p className="text-gray-600">
              Inicie una simulación para cargar automáticamente el mejor paquete.
            </p>
          </div>
        )}
      </div>
    </Modal>
  );
};

export default ModalInformacionPaquete;
