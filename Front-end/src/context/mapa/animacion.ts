import { useCallback, useRef } from 'react';
import { CONFIG_SIMULACION } from '../../config/simulacion';
import { useAnimacionUtils } from '../../hooks/useAnimacionUtils';
import type { CamionMapa } from './types';
import type { MapaAction } from './reducer';

export const useAnimacionLogic = (
  camiones: CamionMapa[],
  dispatch: React.Dispatch<MapaAction>
) => {
  const intervalRef = useRef<number | null>(null);
  const { calcularTiempo, procesarCamion } = useAnimacionUtils();

  const actualizarPosiciones = useCallback(() => {
    const tiempoPorNodo = calcularTiempo();
    const camionesActualizados = camiones.map(camion => 
      procesarCamion(camion, tiempoPorNodo)
    );
    dispatch({ type: 'ACTUALIZAR_POSICIONES_CAMIONES', payload: camionesActualizados });
  }, [camiones, calcularTiempo, procesarCamion, dispatch]);

  const iniciarAnimacion = useCallback(() => {
    if (intervalRef.current) return;
    
    const ahora = Date.now();
    const camionesConTiempoInicio = camiones.map(camion => ({
      ...camion,
      tiempoInicioMovimiento: camion.enMovimiento ? ahora : camion.tiempoInicioMovimiento
    }));
    
    if (camionesConTiempoInicio.some(c => c.enMovimiento)) {
      dispatch({ type: 'ACTUALIZAR_POSICIONES_CAMIONES', payload: camionesConTiempoInicio });
    }
    
    intervalRef.current = window.setInterval(actualizarPosiciones, CONFIG_SIMULACION.intervaloActualizacion);
  }, [actualizarPosiciones, camiones, dispatch]);

  const detenerAnimacion = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  }, []);

  return {
    iniciarAnimacion,
    detenerAnimacion,
    intervalRef
  };
};
