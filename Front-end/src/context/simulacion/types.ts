/**
 * @file types.ts
 * @description Tipos y constantes para la simulación de rutas de camiones
 */

import type { Pedido, Individuo, Almacen, Coordenada } from "../../types";

/**
 * Constantes de configuración de la simulación
 */
export const HORAS_POR_ACTUALIZACION = 2; // 1 hora = 60 minutos por intervalo
export const HORA_INICIAL = 0;
export const HORA_PRIMERA_ACTUALIZACION = 0;
export const NODOS_PARA_ACTUALIZACION = 140; // 100 nodos por intervalo de 60 minutos
export const INCREMENTO_PORCENTAJE = 1;

// Calcular segundos por nodo de manera consistente
// 60 minutos = 3600 segundos divididos en 100 nodos = 36 segundos por nodo
export const SEGUNDOS_POR_NODO = (HORAS_POR_ACTUALIZACION * 60 * 60) / NODOS_PARA_ACTUALIZACION; // 36 segundos

/**
 * @function obtenerSegundosPorNodoSegunTipo
 * @description Obtiene los segundos por nodo según el tipo de simulación
 * @param {string} tipoSimulacion - Tipo de simulación ('DIARIA', 'SEMANAL', 'COLAPSO')
 * @returns {number} Segundos por nodo para el tipo de simulación
 */
export const obtenerSegundosPorNodoSegunTipo = (tipoSimulacion: string): number => {
  switch (tipoSimulacion) {
    case 'DIARIA':
      // Simulación en tiempo real: 36 segundos por nodo (1 hora / 100 nodos)
      return SEGUNDOS_POR_NODO; // 36 segundos por nodo
    case 'SEMANAL':
      return 0.30;
    case 'COLAPSO':
      // Simulación semanal/colapso: 36 segundos por nodo (fijo)
      return SEGUNDOS_POR_NODO;
    default:
      // Por defecto usar el valor calculado
      return SEGUNDOS_POR_NODO;
  }
};

/**
 * @function calcularIntervaloTiempoReal
 * @description Calcula el intervalo en milisegundos para que cada nodo dure el tiempo especificado en tiempo real
 * @param {number} segundosPorNodo - Segundos que debe durar cada nodo en tiempo real (por defecto 62.9)
 * @param {number} velocidadCamion - Velocidad promedio del camión en km/h (opcional, para ajuste dinámico)
 * @returns {number} Intervalo en milisegundos
 */
export const calcularIntervaloTiempoReal = (
  segundosPorNodo: number = SEGUNDOS_POR_NODO, // 36 segundos por defecto
  velocidadCamion?: number
): number => {
  // Factor de ajuste basado en la velocidad del camión (si se proporciona)
  let factorAjuste = 1;
  
  if (velocidadCamion && velocidadCamion > 0) {
    // Velocidad de referencia: 60 km/h
    const velocidadReferencia = 70;
    // Ajustar el tiempo basado en la velocidad (camiones más rápidos = menos tiempo por nodo)
    factorAjuste = velocidadReferencia / velocidadCamion;
  }
  
  // Calcular el intervalo en milisegundos
  const intervaloMs = Math.round(segundosPorNodo * 1000 * factorAjuste);
  
  // Limitar el intervalo entre 100ms y 10000ms (0.1s a 10s)
  return Math.max(10, intervaloMs);
};

/**
 * @function calcularIntervaloSegunTipo
 * @description Calcula el intervalo según el tipo de simulación
 * @param {string} tipoSimulacion - Tipo de simulación ('DIARIA', 'SEMANAL', 'COLAPSO')
 * @param {number} segundosPorNodoPersonalizado - Segundos por nodo personalizado (solo para tiempo real)
 * @param {number} velocidadCamion - Velocidad promedio del camión (opcional)
 * @returns {number} Intervalo en milisegundos
 */
export const calcularIntervaloSegunTipo = (
  tipoSimulacion: string,
  segundosPorNodoPersonalizado?: number,
  velocidadCamion?: number
): number => {
  const segundosPorNodo = obtenerSegundosPorNodoSegunTipo(tipoSimulacion);
  
  // Para simulación en tiempo real, permitir personalización
  if (tipoSimulacion === 'DIARIA' && segundosPorNodoPersonalizado) {
    return calcularIntervaloTiempoReal(segundosPorNodoPersonalizado, velocidadCamion);
  }
  
  // Para otros tipos, usar el valor fijo
  return calcularIntervaloTiempoReal(segundosPorNodo, velocidadCamion);
};

/**
 * @function obtenerIntervaloPorDefecto
 * @description Obtiene el intervalo por defecto para la simulación en tiempo real
 * @returns {number} Intervalo en milisegundos (62.9 segundos por nodo)
 */
export const obtenerIntervaloPorDefecto = (): number => {
  return calcularIntervaloTiempoReal(62.9);
};

/**
 * @interface CamionEstado
 * @description Representa el estado actual de un camión en la simulación
 */
export interface CamionEstado {
  id: string;
  ubicacion: string; // "(x,y)"
  porcentaje: number;
  estado: "Disponible" | "Averiado" | "En Mantenimiento" | "En Mantenimiento Preventivo" | "En Mantenimiento por Avería" | "En Ruta";
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
  combustibleActual: number;
  combustibleMaximo: number;
  distanciaMaxima: number;
  pesoCarga: number;
  pesoCombinado: number;
  tara: number;
  tipo: string;
  velocidadPromedio: number;
}

/**
 * @interface RutaCamion
 * @description Define la ruta completa de un camión y sus pedidos asociados
 */
export interface RutaCamion {
  id: string; // camion.codigo
  ruta: string[]; // ["(12,8)", "(13,8)", ...]
  puntoDestino: string; // "(x,y)"
  pedidos: Pedido[];
  tiposNodos?: string[]; // Tipos de nodos correspondientes a cada posición en la ruta
}

/**
 * @interface Bloqueo
 * @description Representa un bloqueo en la simulación
 */
export interface Bloqueo {
  coordenadas: Coordenada[];
  fechaInicio: string; // ISO string
  fechaFin: string;    // ISO string
}

/**
 * @interface SimulacionContextType
 * @description Define la interfaz del contexto de simulación
 */
export interface SimulacionContextType {
  horaActual: number;
  camiones: CamionEstado[];
  rutasCamiones: RutaCamion[];
  almacenes: Almacen[];
  pedidosNoAsignados: Pedido[];
  fechaHoraSimulacion: string | null;
  fechaInicioSimulacion: string | null;
  fechaHoraInicioIntervalo: string | null;
  fechaHoraFinIntervalo: string | null;
  diaSimulacion: number | null;
  tiempoRealSimulacion: string;
  tiempoTranscurridoSimulado: string;
  simulacionActiva: boolean;
  horaSimulacion: string;
  horaSimulacionAcumulada: string;
  fechaHoraAcumulada: string;
  paqueteActualConsumido: number;
  tipoSimulacion: string;
  avanzarHora: () => void;
  reiniciar: () => Promise<void>;
  iniciarContadorTiempo: () => void;
  reiniciarYEmpezarNuevo: () => Promise<void>;
  limpiarEstadoParaNuevaSimulacion: () => void;
  iniciarPollingPrimerPaquete: () => void;
  pausarSimulacion: () => void;
  reanudarSimulacion: () => void;
  setSimulacionActiva: (value: boolean) => void;
  setPollingActivo: (value: boolean) => void;
  setTipoSimulacion: (tipo: string) => void;
  cargando: boolean;
  bloqueos: Bloqueo[];
  marcarCamionAveriado: (camionId: string) => void;
  limpiarSimulacionCompleta: () => void;
  obtenerInfoPaqueteActual: () => { inicio: string | null; fin: string | null; numero: number };
  setFechaInicioSimulacion: (fecha: string) => void;
  aplicarNuevaSolucionDespuesAveria: (data: IndividuoConBloqueos) => Promise<void>;
}

/**
 * Tipo para la solución precargada
 */
export type IndividuoConBloqueos = Individuo & {
  bloqueos?: Bloqueo[];
  almacenes?: Almacen[];
  pedidos?: Pedido[]; // Array de pedidos no asignados
  fechaHoraSimulacion?: string;
  fechaHoraInicioIntervalo?: string;
  fechaHoraFinIntervalo?: string;
}; 