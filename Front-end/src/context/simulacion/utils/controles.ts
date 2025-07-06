/**
 * @file controles.ts
 * @description Utilidades para el control de la simulación (pausar, reanudar, etc.)
 */

/**
 * Función para pausar la simulación
 * @param {(value: boolean) => void} setSimulacionActiva - Función para actualizar el estado de simulación activa
 * @returns {void}
 */
export const pausarSimulacion = (setSimulacionActiva: (value: boolean) => void): void => {
  setSimulacionActiva(false);
  console.log("⏸️ SIMULACIÓN: Simulación pausada");
};

/**
 * Función para reanudar la simulación
 * @param {(value: boolean) => void} setSimulacionActiva - Función para actualizar el estado de simulación activa
 * @returns {void}
 */
export const reanudarSimulacion = (setSimulacionActiva: (value: boolean) => void): void => {
  setSimulacionActiva(true);
  console.log("▶️ SIMULACIÓN: Simulación reanudada");
};

/**
 * Función para iniciar el contador de tiempo real de la simulación
 * @param {(value: Date) => void} setInicioSimulacion - Función para establecer la fecha de inicio
 * @param {(value: string) => void} setTiempoRealSimulacion - Función para resetear el tiempo real
 * @param {(value: boolean) => void} setSimulacionActiva - Función para activar la simulación
 * @returns {void}
 */
export const iniciarContadorTiempo = (
  setInicioSimulacion: (value: Date) => void,
  setTiempoRealSimulacion: (value: string) => void,
  setSimulacionActiva: (value: boolean) => void
): void => {
  setInicioSimulacion(new Date());
  setTiempoRealSimulacion("00:00:00");
  setSimulacionActiva(true);
  console.log("⏱️ CONTADOR: Iniciando contador de tiempo real de simulación...");
};

/**
 * Función para iniciar el polling para obtener el primer paquete
 * @param {(value: boolean) => void} setPollingActivo - Función para activar el polling
 * @returns {void}
 */
export const iniciarPollingPrimerPaquete = (setPollingActivo: (value: boolean) => void): void => {
  console.log("🚀 POLLING: Activando polling para obtener primer paquete...");
  setPollingActivo(true);
}; 