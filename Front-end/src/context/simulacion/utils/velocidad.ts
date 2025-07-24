/**
 * @file velocidad.ts
 * @description Utilidades para manejar la velocidad de los camiones y calcular intervalos dinámicos
 */

import type { CamionEstado } from '../types';
import { calcularIntervaloTiempoReal } from '../types';

/**
 * @function calcularVelocidadPromedioCamiones
 * @description Calcula la velocidad promedio de todos los camiones activos
 * @param {CamionEstado[]} camiones - Array de camiones
 * @returns {number} Velocidad promedio en km/h
 */
export const calcularVelocidadPromedioCamiones = (camiones: CamionEstado[]): number => {
  if (camiones.length === 0) return 60; // Velocidad por defecto
  
  const camionesActivos = camiones.filter(camion => 
    camion.estado !== 'Averiado' && 
    camion.estado !== 'En Mantenimiento' &&
    camion.estado !== 'En Mantenimiento Preventivo' &&
    camion.estado !== 'En Mantenimiento por Avería'
  );
  
  if (camionesActivos.length === 0) return 60; // Velocidad por defecto si no hay camiones activos
  
  const velocidadTotal = camionesActivos.reduce((sum, camion) => 
    sum + (camion.velocidadPromedio || 60), 0
  );
  
  return velocidadTotal / camionesActivos.length;
};

/**
 * @function calcularIntervaloDinamico
 * @description Calcula el intervalo dinámico basado en la velocidad promedio de los camiones
 * @param {number} segundosPorNodo - Segundos base por nodo
 * @param {CamionEstado[]} camiones - Array de camiones para calcular velocidad promedio
 * @returns {number} Intervalo en milisegundos
 */
export const calcularIntervaloDinamico = (
  segundosPorNodo: number,
  camiones: CamionEstado[]
): number => {
  const velocidadPromedio = calcularVelocidadPromedioCamiones(camiones);
  return calcularIntervaloTiempoReal(segundosPorNodo, velocidadPromedio);
};

/**
 * @function obtenerVelocidadCamionEspecifico
 * @description Obtiene la velocidad de un camión específico
 * @param {string} camionId - ID del camión
 * @param {CamionEstado[]} camiones - Array de camiones
 * @returns {number} Velocidad del camión en km/h
 */
export const obtenerVelocidadCamionEspecifico = (
  camionId: string,
  camiones: CamionEstado[]
): number => {
  const camion = camiones.find(c => c.id === camionId);
  return camion?.velocidadPromedio || 60;
};

/**
 * @function formatearVelocidad
 * @description Formatea la velocidad para mostrar en la interfaz
 * @param {number} velocidad - Velocidad en km/h
 * @returns {string} Velocidad formateada
 */
export const formatearVelocidad = (velocidad: number): string => {
  return `${velocidad.toFixed(1)} km/h`;
};

/**
 * @function obtenerRangoVelocidad
 * @description Obtiene el rango de velocidades de los camiones
 * @param {CamionEstado[]} camiones - Array de camiones
 * @returns {{min: number, max: number, promedio: number}} Rango de velocidades
 */
export const obtenerRangoVelocidad = (camiones: CamionEstado[]): {
  min: number;
  max: number;
  promedio: number;
} => {
  if (camiones.length === 0) {
    return { min: 60, max: 60, promedio: 60 };
  }
  
  const velocidades = camiones
    .map(c => c.velocidadPromedio || 60)
    .filter(v => v > 0);
  
  if (velocidades.length === 0) {
    return { min: 60, max: 60, promedio: 60 };
  }
  
  return {
    min: Math.min(...velocidades),
    max: Math.max(...velocidades),
    promedio: velocidades.reduce((sum, v) => sum + v, 0) / velocidades.length
  };
}; 