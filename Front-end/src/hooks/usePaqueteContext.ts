import { useContext } from 'react';
import { PaqueteContext } from '../context/PaqueteContextDefinition';
import type { PaqueteContextType } from '../context/PaqueteContext';

// ============================
// HOOK PRINCIPAL
// ============================

export const usePaquete = (): PaqueteContextType => {
  const context = useContext(PaqueteContext);
  if (!context) {
    throw new Error("usePaquete debe usarse dentro de PaqueteProvider");
  }
  return context;
};

// ============================
// HOOKS ESPECIALIZADOS
// ============================

export const usePaqueteState = () => {
  const { 
    paqueteActual, 
    isLoading, 
    error, 
    ultimaActualizacion 
  } = usePaquete();
  
  return {
    paqueteActual,
    isLoading,
    error,
    ultimaActualizacion
  };
};

export const usePaqueteActions = () => {
  const { 
    setPaquete, 
    setLoading, 
    setError, 
    limpiarPaquete 
  } = usePaquete();
  
  return {
    setPaquete,
    setLoading,
    setError,
    limpiarPaquete
  };
};

// ============================
// HOOK CON LÓGICA DE NEGOCIO
// ============================

export const usePaqueteInfo = () => {
  const { paqueteActual } = usePaquete();
  
  const totalCamiones = paqueteActual?.cromosoma?.length || 0;
  const totalPedidos = paqueteActual?.cromosoma?.reduce(
    (total, gen) => total + (gen.pedidos?.length || 0), 
    0
  ) || 0;
  
  const fechaSimulacion = paqueteActual?.fechaHoraSimulacion || null;
  
  return {
    totalCamiones,
    totalPedidos,
    fechaSimulacion,
    tienePaquete: !!paqueteActual
  };
};
