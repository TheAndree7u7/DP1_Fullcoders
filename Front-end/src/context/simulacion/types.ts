/**
 * @file types.ts
 * @description Tipos y constantes para la simulación de rutas de camiones
 */

import type { Pedido, Individuo, Almacen, Coordenada } from "../../types";

/**
 * Constantes de configuración de la simulación
 */
export const HORAS_POR_ACTUALIZACION = 1; // 1 hora = 60 minutos por intervalo
export const HORA_INICIAL = 0;
export const HORA_PRIMERA_ACTUALIZACION = 0;
export const NODOS_PARA_ACTUALIZACION = 50; // 50 nodos por intervalo de 60 minutos
export const INCREMENTO_PORCENTAJE = 1;

// Calcular segundos por nodo de manera consistente
// 60 minutos = 3600 segundos divididos en 50 nodos = 72 segundos por nodo
export const SEGUNDOS_POR_NODO = (HORAS_POR_ACTUALIZACION * 60 * 60) / NODOS_PARA_ACTUALIZACION; // 72 segundos

/**
 * @interface CamionEstado
 * @description Representa el estado actual de un camión en la simulación
 */
export interface CamionEstado {
  id: string;
  ubicacion: string; // "(x,y)"
  porcentaje: number;
  estado: "Disponible" | "Averiado" | "En Mantenimiento" | "En Mantenimiento Preventivo" | "En Mantenimiento por Avería";
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