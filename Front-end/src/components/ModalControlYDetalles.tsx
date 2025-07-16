import React, { useState } from 'react';
import Modal from './Modal';
import ControlSimulacion from './ControlSimulacion';
import PaqueteDisplay from './PaqueteDisplay';
import IndicadorNodoActual from './IndicadorNodoActual';
import EstadisticasMapa from './EstadisticasMapa';

interface ModalControlYDetallesProps {
  isOpen: boolean;
  onClose: () => void;
}

const ModalControlYDetalles: React.FC<ModalControlYDetallesProps> = ({
  isOpen,
  onClose
}) => {
  const [pestañaActiva, setPestañaActiva] = useState<'control' | 'detalles' | 'estadisticas'>('control');

  const renderContenidoPestaña = () => {
    switch (pestañaActiva) {
      case 'control':
        return (
          <div className="space-y-6">
            <ControlSimulacion 
              isOpen={true} 
              onClose={() => {}} 
              showAsModal={false}
            />
            <IndicadorNodoActual />
          </div>
        );
      
      case 'detalles':
        return (
          <div className="space-y-6">
            <PaqueteDisplay variant="detailed" showHeader={true} />
          </div>
        );
      
      case 'estadisticas':
        return (
          <div className="space-y-6">
            <EstadisticasMapa variant="detailed" />
          </div>
        );
      
      default:
        return null;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Control de Simulación y Detalles"
      size="xl"
    >
      <div className="space-y-6">
        {/* Pestañas */}
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setPestañaActiva('control')}
              className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
                pestañaActiva === 'control'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Control de Simulación
            </button>
            <button
              onClick={() => setPestañaActiva('detalles')}
              className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
                pestañaActiva === 'detalles'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Información Detallada
            </button>
            <button
              onClick={() => setPestañaActiva('estadisticas')}
              className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
                pestañaActiva === 'estadisticas'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Estadísticas del Mapa
            </button>
          </nav>
        </div>

        {/* Contenido de la pestaña activa */}
        <div className="min-h-[400px]">
          {renderContenidoPestaña()}
        </div>
      </div>
    </Modal>
  );
};

export default ModalControlYDetalles;
