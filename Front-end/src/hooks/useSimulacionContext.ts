import { useContext } from 'react';
import { SimulacionContext } from '../context/SimulacionContextDefinition';
import type { SimulacionContextType } from '../context/SimulacionContext';

// Hook to use the simulation context
export const useSimulacion = (): SimulacionContextType => {
  const context = useContext(SimulacionContext);
  if (!context) {
    throw new Error("useSimulacion debe usarse dentro de SimulacionProvider");
  }
  return context;
};

// Specialized hooks for specific functionality
export const useSimulationState = () => {
  const { 
    tiempoTranscurridoSimulado, 
    tiempoRealSimulacion, 
    fechaHoraSimulacion,
    horaActual,
    isRunning,
    isPaused,
    speed,
    isLoading,
    error,
    bloqueos
  } = useSimulacion();
  
  return {
    tiempoTranscurridoSimulado,
    tiempoRealSimulacion,
    fechaHoraSimulacion,
    horaActual,
    isRunning,
    isPaused,
    speed,
    isLoading,
    error,
    bloqueos
  };
};

export const useSimulationControls = () => {
  const {
    startSimulation,
    pauseSimulation,
    stopSimulation,
    resetSimulation,
    setSpeed,
    updateTime,
    setError,
    setLoading,
    setBloqueos,
    addBloqueo,
    removeBloqueo
  } = useSimulacion();
  
  return {
    startSimulation,
    pauseSimulation,
    stopSimulation,
    resetSimulation,
    setSpeed,
    updateTime,
    setError,
    setLoading,
    setBloqueos,
    addBloqueo,
    removeBloqueo
  };
};

// Specialized hook for bloqueos management
export const useBloqueosManagement = () => {
  const { bloqueos, setBloqueos, addBloqueo, removeBloqueo } = useSimulacion();
  
  return {
    bloqueos,
    setBloqueos,
    addBloqueo,
    removeBloqueo
  };
};
