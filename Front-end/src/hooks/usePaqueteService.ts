import { useCallback } from 'react';
import { usePaqueteActions } from './usePaqueteContext';
import { obtenerMejorIndividuo } from '../services/simulacionApiService';

// ============================
// HOOK PARA LÓGICA DE PAQUETES
// ============================

export const usePaqueteService = () => {
  const { setPaquete, setLoading, setError } = usePaqueteActions();

  const cargarPaquete = useCallback(async (fecha: string) => {
    try {
      setLoading(true);
      setError(null);
      
      console.log("🔄 Cargando paquete para fecha:", fecha);
      const paquete = await obtenerMejorIndividuo(fecha);
      
      if (!paquete) {
        throw new Error("No se recibió paquete del servidor");
      }
      
      console.log("📦 Paquete cargado:", {
        genes: paquete.cromosoma?.length || 0,
        fitness: paquete.fitness
      });
      
      setPaquete(paquete);
      
      return paquete;
    } catch (error) {
      const errorMessage = error instanceof Error 
        ? error.message 
        : 'Error desconocido al cargar paquete';
      
      setError(errorMessage);
      throw error;
    } finally {
      setLoading(false);
    }
  }, [setPaquete, setLoading, setError]);

  const cargarPaqueteConReintento = useCallback(async (
    fecha: string, 
    intentosMaximos: number = 3,
    delayMs: number = 1000
  ) => {
    let ultimoError: Error | null = null;
    
    for (let intento = 1; intento <= intentosMaximos; intento++) {
      try {
        return await cargarPaquete(fecha);
      } catch (error) {
        ultimoError = error instanceof Error ? error : new Error('Error desconocido');
        
        if (intento < intentosMaximos) {
          await new Promise(resolve => setTimeout(resolve, delayMs * intento));
        }
      }
    }
    
    throw ultimoError;
  }, [cargarPaquete]);

  return {
    cargarPaquete,
    cargarPaqueteConReintento
  };
};
