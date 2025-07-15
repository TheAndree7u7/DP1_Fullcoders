import { useCallback } from 'react';
import { CONFIG_SIMULACION } from '../config/simulacion';
import type { Coordenada } from '../types';
import type { CamionMapa } from '../context/mapa/types';

export const useAnimacionUtils = () => {
  const calcularTiempo = useCallback(() => {
    return (CONFIG_SIMULACION.periodoLlamado * 1000) / CONFIG_SIMULACION.nodosEquivalentes;
  }, []);

  const interpolar = useCallback((inicio: Coordenada, fin: Coordenada, progreso: number): Coordenada => ({
    x: inicio.x + (fin.x - inicio.x) * progreso,
    y: inicio.y + (fin.y - inicio.y) * progreso,
  }), []);

  const calcularRotacion = useCallback((inicio: Coordenada, fin: Coordenada): number => {
    const deltaX = fin.x - inicio.x;
    const deltaY = fin.y - inicio.y;
    return Math.atan2(deltaY, deltaX) * (180 / Math.PI);
  }, []);

  const procesarCamion = useCallback((camion: CamionMapa, tiempoPorNodo: number) => {
    if (!camion.enMovimiento || camion.ruta.length <= 1) {
      return camion;
    }

    const ahora = Date.now();
    const tiempoTranscurrido = ahora - camion.tiempoInicioMovimiento;
    const nodoActual = Math.floor(tiempoTranscurrido / tiempoPorNodo);
    const progreso = (tiempoTranscurrido % tiempoPorNodo) / tiempoPorNodo;

    if (nodoActual >= camion.ruta.length - 1) {
      return {
        ...camion,
        nodoActual: camion.ruta.length - 1,
        posicionInterpolada: camion.ruta[camion.ruta.length - 1],
        enMovimiento: false,
      };
    }

    const inicio = camion.ruta[nodoActual];
    const fin = camion.ruta[nodoActual + 1];
    const posicionInterpolada = interpolar(inicio, fin, progreso);

    return {
      ...camion,
      nodoActual,
      posicionInterpolada,
      rotacion: calcularRotacion(inicio, fin),
    };
  }, [interpolar, calcularRotacion]);

  return {
    calcularTiempo,
    interpolar,
    calcularRotacion,
    procesarCamion,
  };
};
