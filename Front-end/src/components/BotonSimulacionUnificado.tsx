import React, { useState } from 'react';
import { Settings } from 'lucide-react';
import ModalControlYDetalles from './ModalControlYDetalles';

interface BotonSimulacionUnificadoProps {
  className?: string;
}

const BotonSimulacionUnificado: React.FC<BotonSimulacionUnificadoProps> = ({
  className = ''
}) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleOpenModal = () => {
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
  };

  return (
    <>
      <button
        onClick={handleOpenModal}
        className={`bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center gap-2 ${className}`}
        title="Abrir control de simulación y detalles"
      >
        <Settings size={16} />
        Control y Detalles
      </button>

      <ModalControlYDetalles
        isOpen={isModalOpen}
        onClose={handleCloseModal}
      />
    </>
  );
};

export default BotonSimulacionUnificado;
