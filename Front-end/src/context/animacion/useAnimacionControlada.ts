import { useCallback, useRef } from 'react';
import { CONFIG_SIMULACION } from '../../config/simulacion';
import { useAnimacionUtils } from '../../hooks/useAnimacionUtils';
import type { CamionMapa } from '../mapa/types';

export const useAnimacionControlada = () => {
  const intervalRef = useRef<number | null>(null);
  const camionesRef = useRef<CamionMapa[]>([]);
  const onActualizarRef = useRef<((camiones: CamionMapa[]) => void) | null>(null);
  const { calcularTiempo, procesarCamion } = useAnimacionUtils();

  const iniciarAnimacion = useCallback((
    camiones: CamionMapa[], 
    onActualizar: (camiones: CamionMapa[]) => void
  ) => {
    if (intervalRef.current) return false;
    
    const ahora = Date.now();
    const camionesConTiempo = camiones.map(camion => ({
      ...camion,
      tiempoInicioMovimiento: camion.enMovimiento ? ahora : camion.tiempoInicioMovimiento
    }));
    
    // Guardar referencias
    camionesRef.current = camionesConTiempo;
    onActualizarRef.current = onActualizar;
    
    // Actualizar inmediatamente
    onActualizar(camionesConTiempo);
    
    const actualizarPosiciones = () => {
      if (!onActualizarRef.current || camionesRef.current.length === 0) return;
      
      const tiempoPorNodo = calcularTiempo();
      const camionesActualizados = camionesRef.current.map(camion => 
        procesarCamion(camion, tiempoPorNodo)
      );
      
      // Actualizar la referencia con los nuevos valores
      camionesRef.current = camionesActualizados;
      onActualizarRef.current(camionesActualizados);
    };
    
    intervalRef.current = window.setInterval(actualizarPosiciones, CONFIG_SIMULACION.intervaloActualizacion);
    return true;
  }, [calcularTiempo, procesarCamion]);

  const detenerAnimacion = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
      return true;
    }
    return false;
  }, []);

  const estaAnimando = useCallback(() => {
    return intervalRef.current !== null;
  }, []);

  return {
    iniciarAnimacion,
    detenerAnimacion,
    estaAnimando
  };
};
