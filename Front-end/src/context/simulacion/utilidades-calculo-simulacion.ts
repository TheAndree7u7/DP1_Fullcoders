/**
 * @file utilidades-calculo-simulacion.ts
 * @description Funciones de utilidad para cálculos y conversiones en la simulación
 * Incluye funciones para parsear coordenadas, adaptar objetos y formatear tiempo
 */

import type { Coordenada, Camion } from "../../types";
import type { CamionEstado } from "../SimulacionContext";
import { CONFIGURACION_TIEMPO_SIMULACION } from "./constantes-configuracion-simulacion";

/**
 * @interface FormatoTiempoSimulacion
 * @description Formato para representar tiempo en la simulación
 */
export interface FormatoTiempoSimulacion {
  horas: number;
  minutos: number;
  segundos: number;
  textoFormateado: string;
}

/**
 * @function parseCoordenadasDeCadena
 * @description Convierte una cadena de coordenadas en formato "(x,y)" a objeto Coordenada
 * @param {string} coordenadaTexto - Coordenada en formato "(x,y)"
 * @returns {Coordenada} Objeto con coordenadas x e y
 * @throws {Error} Si la coordenada no tiene formato válido
 */
export const parseCoordenadasDeCadena = (coordenadaTexto: string): Coordenada => {
  const expresionRegularCoordenadas = /\((\d+),\s*(\d+)\)/;
  const coincidencia = coordenadaTexto.match(expresionRegularCoordenadas);
  
  if (!coincidencia) {
    throw new Error(`Formato de coordenada inválido: ${coordenadaTexto}`);
  }
  
  return { 
    x: parseInt(coincidencia[1], 10), 
    y: parseInt(coincidencia[2], 10) 
  };
};

/**
 * @function adaptarCamionEstadoParaCalculos
 * @description Convierte un CamionEstado a formato compatible con funciones de cálculo de types.ts
 * @param {CamionEstado} camionEstado - Estado actual del camión
 * @returns {Camion} Objeto camión compatible con funciones de cálculo
 */
export const adaptarCamionEstadoParaCalculos = (camionEstado: CamionEstado): Camion => {
  return {
    codigo: camionEstado.id,
    capacidadActualGLP: camionEstado.capacidadActualGLP,
    capacidadMaximaGLP: camionEstado.capacidadMaximaGLP,
    combustibleActual: camionEstado.combustibleActual,
    combustibleMaximo: camionEstado.combustibleMaximo,
    distanciaMaxima: camionEstado.distanciaMaxima,
    estado: camionEstado.estado,
    pesoCarga: camionEstado.pesoCarga,
    pesoCombinado: camionEstado.pesoCombinado,
    tara: camionEstado.tara,
    tipo: camionEstado.tipo,
    velocidadPromedio: camionEstado.velocidadPromedio,
  };
};

/**
 * @function calcularTiempoTranscurridoDesdeInicio
 * @description Calcula el tiempo transcurrido desde el inicio de la simulación
 * @param {Date} fechaInicioSimulacion - Fecha de inicio de la simulación
 * @returns {FormatoTiempoSimulacion} Tiempo transcurrido formateado
 */
export const calcularTiempoTranscurridoDesdeInicio = (
  fechaInicioSimulacion: Date
): FormatoTiempoSimulacion => {
  const fechaActual = new Date();
  const diferenciaMilisegundos = fechaActual.getTime() - fechaInicioSimulacion.getTime();
  const segundosTotales = Math.floor(diferenciaMilisegundos / 1000);
  
  const horas = Math.floor(segundosTotales / 3600);
  const minutos = Math.floor((segundosTotales % 3600) / 60);
  const segundos = segundosTotales % 60;
  
  const textoFormateado = `${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${segundos.toString().padStart(2, '0')}`;
  
  return {
    horas,
    minutos,
    segundos,
    textoFormateado,
  };
};

/**
 * @function calcularHoraSimulacionDesdeFechaBase
 * @description Calcula la hora de simulación basada en la fecha base y el nodo actual
 * @param {string} fechaHoraBase - Fecha y hora base de la simulación
 * @param {number} nodoActual - Nodo actual en la simulación
 * @returns {string} Hora formateada de la simulación
 */
export const calcularHoraSimulacionDesdeFechaBase = (
  fechaHoraBase: string,
  nodoActual: number
): string => {
  const fechaBase = new Date(fechaHoraBase);
  
  // Calculamos qué nodo estamos dentro del ciclo actual (0-99)
  const nodoEnCicloActual = nodoActual % CONFIGURACION_TIEMPO_SIMULACION.NODOS_POR_ACTUALIZACION_COMPLETA;
  
  // Calculamos el avance por nodo (segundos totales divididos por nodos totales)
  const segundosPorNodo = (
    CONFIGURACION_TIEMPO_SIMULACION.HORAS_POR_CICLO_ACTUALIZACION * 
    CONFIGURACION_TIEMPO_SIMULACION.SEGUNDOS_POR_HORA
  ) / CONFIGURACION_TIEMPO_SIMULACION.NODOS_POR_ACTUALIZACION_COMPLETA;
  
  // Calculamos segundos adicionales para el incremento local
  const segundosAdicionalesEnCiclo = nodoEnCicloActual * segundosPorNodo;
  
  // Crear nueva fecha sumando los segundos
  const fechaCalculada = new Date(fechaBase.getTime() + segundosAdicionalesEnCiclo * 1000);
  
  // Formatear solo la hora
  return fechaCalculada.toLocaleTimeString(
    CONFIGURACION_TIEMPO_SIMULACION.FORMATO_HORA_ESPANOL,
    CONFIGURACION_TIEMPO_SIMULACION.FORMATO_HORA_COMPLETA
  );
};

/**
 * @function extraerDiaDeFecha
 * @description Extrae el día de una fecha ISO
 * @param {string} fechaISO - Fecha en formato ISO
 * @returns {number} Día del mes
 */
export const extraerDiaDeFecha = (fechaISO: string): number => {
  const fecha = new Date(fechaISO);
  return fecha.getDate();
};

/**
 * @function validarCoordenadaTexto
 * @description Valida si una cadena tiene formato de coordenada válido
 * @param {string} coordenadaTexto - Texto a validar
 * @returns {boolean} True si es válida, false si no
 */
export const validarCoordenadaTexto = (coordenadaTexto: string): boolean => {
  const expresionRegularCoordenadas = /^\(\d+,\s*\d+\)$/;
  return expresionRegularCoordenadas.test(coordenadaTexto);
};

/**
 * @function formatearCoordenada
 * @description Convierte un objeto Coordenada a formato texto "(x,y)"
 * @param {Coordenada} coordenada - Objeto coordenada
 * @returns {string} Coordenada formateada como texto
 */
export const formatearCoordenada = (coordenada: Coordenada): string => {
  return `(${coordenada.x},${coordenada.y})`;
}; 