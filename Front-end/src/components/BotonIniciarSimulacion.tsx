import React from 'react';
import { Play } from 'lucide-react';
import { useSimulacion } from '../hooks/useSimulacionContext';

interface BotonIniciarSimulacionProps {
  className?: string;
}

const BotonIniciarSimulacion: React.FC<BotonIniciarSimulacionProps> = ({
  className = ''
}) => {
  const { isRunning, isPaused, startSimulation, pauseSimulation } = useSimulacion();

  const handleIniciarPausar = () => {
    if (!isRunning) {
      startSimulation();
    } else if (isPaused) {
      startSimulation(); // Reanudar usando startSimulation
    } else {
      pauseSimulation();
    }
  };

  const getButtonText = () => {
    if (!isRunning) return 'Iniciar Simulación';
    if (isPaused) return 'Reanudar';
    return 'Pausar';
  };

  const getButtonIcon = () => {
    if (!isRunning || isPaused) {
      return <Play size={16} fill="currentColor" />;
    }
    return (
      <div className="w-4 h-4 flex items-center justify-center">
        <div className="w-1 h-3 bg-current mr-0.5"></div>
        <div className="w-1 h-3 bg-current"></div>
      </div>
    );
  };

  return (
    <button
      onClick={handleIniciarPausar}
      className={`bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center gap-2 shadow-lg ${className}`}
      title={getButtonText()}
    >
      {getButtonIcon()}
      {getButtonText()}
    </button>
  );
};

export default BotonIniciarSimulacion;
