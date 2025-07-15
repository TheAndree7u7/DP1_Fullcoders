import React, { useState } from 'react';
import ControlSimulacion from './ControlSimulacion';

interface BotonControlSimulacionProps {
  className?: string;
}

const BotonControlSimulacion: React.FC<BotonControlSimulacionProps> = ({
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
        className={`bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors font-medium ${className}`}
      >
        Configurar Simulación
      </button>

      <ControlSimulacion
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        showAsModal={true}
      />
    </>
  );
};

export default BotonControlSimulacion;
