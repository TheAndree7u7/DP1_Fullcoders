/**
 * @file controladores-tiempo-simulacion.ts
 * @description Controladores para manejar el tiempo y el flujo de la simulación
 * Incluye funciones para iniciar, detener, reiniciar y controlar el avance temporal
 */

import { 
  HORA_INICIAL_SIMULACION,
  HORA_PRIMERA_ACTUALIZACION_DATOS,
  NODOS_REQUERIDOS_ANTES_ACTUALIZACION,
  PROPORCION_SOLICITUD_ANTICIPADA,
  INTERVALO_ACTUALIZACION_CONTADOR_TIEMPO,
  CONFIGURACION_MOVIMIENTO_CAMIONES
} from "./constantes-configuracion-simulacion";
import { calcularTiempoTranscurridoDesdeInicio } from "./utilidades-calculo-simulacion";

/**
 * @interface EstadoContadorTiempo
 * @description Estado del contador de tiempo real de la simulación
 */
export interface EstadoContadorTiempo {
  fechaInicio: Date | null;
  tiempoTranscurrido: string;
  simulacionActiva: boolean;
  intervalId: number | null;
}

/**
 * @interface ControladorTiempoSimulacion
 * @description Controlador para manejar el tiempo de la simulación
 */
export interface ControladorTiempoSimulacion {
  iniciarContador: () => void;
  detenerContador: () => void;
  reiniciarContador: () => void;
  obtenerTiempoTranscurrido: () => string;
  estaActivo: () => boolean;
}

/**
 * @function crearControladorTiempo
 * @description Crea un controlador de tiempo para la simulación
 * @param {(tiempo: string) => void} actualizarTiempo - Callback para actualizar el tiempo
 * @returns {ControladorTiempoSimulacion} Controlador de tiempo
 */
export const crearControladorTiempo = (
  actualizarTiempo: (tiempo: string) => void
): ControladorTiempoSimulacion => {
  const estado: EstadoContadorTiempo = {
    fechaInicio: null,
    tiempoTranscurrido: "00:00:00",
    simulacionActiva: false,
    intervalId: null,
  };

  const iniciarContador = () => {
    if (estado.simulacionActiva) {
      console.log("⏱️ TIEMPO: Contador ya está activo");
      return;
    }

    estado.fechaInicio = new Date();
    estado.simulacionActiva = true;
    estado.tiempoTranscurrido = "00:00:00";
    
    console.log("⏱️ TIEMPO: Iniciando contador de tiempo real de simulación...");
    
    estado.intervalId = setInterval(() => {
      if (estado.fechaInicio) {
        const tiempoCalculado = calcularTiempoTranscurridoDesdeInicio(estado.fechaInicio);
        estado.tiempoTranscurrido = tiempoCalculado.textoFormateado;
        actualizarTiempo(tiempoCalculado.textoFormateado);
        
        // Log cada cierto tiempo para debugging
        if (tiempoCalculado.segundos % CONFIGURACION_MOVIMIENTO_CAMIONES.INTERVALO_LOG_CONTADOR_SEGUNDOS === 0) {
          console.log("⏱️ TIEMPO: Tiempo transcurrido:", tiempoCalculado.textoFormateado);
        }
      }
    }, INTERVALO_ACTUALIZACION_CONTADOR_TIEMPO);
  };

  const detenerContador = () => {
    if (estado.intervalId) {
      clearInterval(estado.intervalId);
      estado.intervalId = null;
    }
    estado.simulacionActiva = false;
    console.log("⏱️ TIEMPO: Contador detenido");
  };

  const reiniciarContador = () => {
    detenerContador();
    estado.fechaInicio = null;
    estado.tiempoTranscurrido = "00:00:00";
    actualizarTiempo("00:00:00");
    console.log("⏱️ TIEMPO: Contador reiniciado");
  };

  const obtenerTiempoTranscurrido = () => estado.tiempoTranscurrido;
  const estaActivo = () => estado.simulacionActiva;

  return {
    iniciarContador,
    detenerContador,
    reiniciarContador,
    obtenerTiempoTranscurrido,
    estaActivo,
  };
};

/**
 * @interface EstadoControlSimulacion
 * @description Estado del control de simulación
 */
export interface EstadoControlSimulacion {
  horaActual: number;
  nodosRestantesParaActualizacion: number;
  esperandoActualizacion: boolean;
  solicitudAnticipadaEnviada: boolean;
}

/**
 * @function calcularNodosParaSolicitudAnticipada
 * @description Calcula en qué nodo se debe enviar la solicitud anticipada
 * @returns {number} Número de nodo para solicitud anticipada
 */
export const calcularNodosParaSolicitudAnticipada = (): number => {
  return Math.floor(NODOS_REQUERIDOS_ANTES_ACTUALIZACION * PROPORCION_SOLICITUD_ANTICIPADA);
};

/**
 * @function determinarSiDebeEnviarSolicitudAnticipada
 * @description Determina si se debe enviar una solicitud anticipada
 * @param {number} nodosRestantes - Nodos restantes antes de actualización
 * @param {boolean} solicitudYaEnviada - Si ya se envió la solicitud
 * @returns {boolean} True si se debe enviar la solicitud
 */
export const determinarSiDebeEnviarSolicitudAnticipada = (
  nodosRestantes: number,
  solicitudYaEnviada: boolean
): boolean => {
  const nodoParaSolicitud = calcularNodosParaSolicitudAnticipada();
  const nodoActualEnCiclo = NODOS_REQUERIDOS_ANTES_ACTUALIZACION - nodosRestantes;
  
  return nodoActualEnCiclo >= nodoParaSolicitud && !solicitudYaEnviada;
};

/**
 * @function determinarSiDebeActualizarDatos
 * @description Determina si se deben actualizar los datos de simulación
 * @param {number} nodosRestantes - Nodos restantes antes de actualización
 * @returns {boolean} True si se deben actualizar los datos
 */
export const determinarSiDebeActualizarDatos = (nodosRestantes: number): boolean => {
  return nodosRestantes <= 0;
};

/**
 * @function avanzarHoraSimulacion
 * @description Avanza la hora de la simulación
 * @param {number} horaActual - Hora actual
 * @returns {number} Nueva hora
 */
export const avanzarHoraSimulacion = (horaActual: number): number => {
  return horaActual + 1;
};

/**
 * @function reducirNodosRestantes
 * @description Reduce el contador de nodos restantes
 * @param {number} nodosRestantes - Nodos restantes actuales
 * @returns {number} Nuevos nodos restantes
 */
export const reducirNodosRestantes = (nodosRestantes: number): number => {
  return nodosRestantes - 1;
};

/**
 * @function reiniciarContadorNodos
 * @description Reinicia el contador de nodos para el siguiente ciclo
 * @returns {number} Contador reiniciado
 */
export const reiniciarContadorNodos = (): number => {
  return NODOS_REQUERIDOS_ANTES_ACTUALIZACION;
};

/**
 * @function obtenerHoraInicialSimulacion
 * @description Obtiene la hora inicial de la simulación
 * @returns {number} Hora inicial
 */
export const obtenerHoraInicialSimulacion = (): number => {
  return HORA_INICIAL_SIMULACION;
};

/**
 * @function obtenerHoraPrimeraActualizacion
 * @description Obtiene la hora de la primera actualización
 * @returns {number} Hora de primera actualización
 */
export const obtenerHoraPrimeraActualizacion = (): number => {
  return HORA_PRIMERA_ACTUALIZACION_DATOS;
};

/**
 * @function crearEstadoControlSimulacionInicial
 * @description Crea el estado inicial del control de simulación
 * @returns {EstadoControlSimulacion} Estado inicial
 */
export const crearEstadoControlSimulacionInicial = (): EstadoControlSimulacion => {
  return {
    horaActual: HORA_INICIAL_SIMULACION,
    nodosRestantesParaActualizacion: NODOS_REQUERIDOS_ANTES_ACTUALIZACION,
    esperandoActualizacion: false,
    solicitudAnticipadaEnviada: false,
  };
};

/**
 * @function reiniciarEstadoControlSimulacion
 * @description Reinicia el estado del control de simulación
 * @returns {EstadoControlSimulacion} Estado reiniciado
 */
export const reiniciarEstadoControlSimulacion = (): EstadoControlSimulacion => {
  return {
    horaActual: HORA_PRIMERA_ACTUALIZACION_DATOS,
    nodosRestantesParaActualizacion: NODOS_REQUERIDOS_ANTES_ACTUALIZACION,
    esperandoActualizacion: false,
    solicitudAnticipadaEnviada: false,
  };
};

/**
 * @function logEstadoSimulacion
 * @description Registra el estado actual de la simulación para debugging
 * @param {EstadoControlSimulacion} estado - Estado actual
 */
export const logEstadoSimulacion = (estado: EstadoControlSimulacion): void => {
  const nodoActual = NODOS_REQUERIDOS_ANTES_ACTUALIZACION - estado.nodosRestantesParaActualizacion;
  const porcentajeCompletado = (nodoActual / NODOS_REQUERIDOS_ANTES_ACTUALIZACION) * 100;
  
  console.log(`📊 SIMULACIÓN: Estado actual - Hora: ${estado.horaActual}, Nodo: ${nodoActual}/${NODOS_REQUERIDOS_ANTES_ACTUALIZACION} (${porcentajeCompletado.toFixed(1)}%)`);
  
  if (estado.esperandoActualizacion) {
    console.log("⏳ SIMULACIÓN: Esperando actualización de datos...");
  }
  
  if (estado.solicitudAnticipadaEnviada) {
    console.log("📅 SIMULACIÓN: Solicitud anticipada enviada");
  }
}; 