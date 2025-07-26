/**
 * @file pollingManager.ts
 * @description Manejo del polling para obtener paquetes de simulación
 */

import { getMejorIndividuo } from "../../services/simulacionApiService";
import type { IndividuoConBloqueos } from "./types";

/**
 * @function iniciarPollingPrimerPaquete
 * @description Inicia el polling para obtener el primer paquete disponible
 */
export const iniciarPollingPrimerPaquete = (
  setPollingActivo: (value: boolean) => void
): void => {
  console.log("🔄 POLLING: Iniciando polling para primer paquete...");
  setPollingActivo(true);
};

/**
 * @function ejecutarPollingPrimerPaquete
 * @description Ejecuta el polling para obtener el primer paquete
 */
export const ejecutarPollingPrimerPaquete = (
  fechaInicioSimulacion: string | null,
  setPollingActivo: (value: boolean) => void,
  setCargando: (value: boolean) => void,
  onPaqueteEncontrado: (data: IndividuoConBloqueos) => Promise<void>
): (() => void) => {
  console.log("🔄 POLLING: Iniciando polling automático para obtener primer paquete...");

  let intentos = 0;
  const maxIntentos = 60; // Máximo 120 segundos de polling

  const interval = setInterval(async () => {
    intentos++;

    if (intentos > maxIntentos) {
      console.log("⏰ POLLING: Timeout alcanzado, desactivando polling...");
      setPollingActivo(false);
      setCargando(false);
      console.log("⚠️ POLLING: Estado de carga cambiado a false por timeout");
      return;
    }

    try {
      // console.log("🔍 POLLING: Buscando nuevos paquetes...");
      const paquete = await getMejorIndividuo(fechaInicioSimulacion ?? "");
      const data = paquete as IndividuoConBloqueos;

      // Verificar si hay datos válidos
      if (data && data.cromosoma && Array.isArray(data.cromosoma)) {
        console.log("✅ POLLING: Primer paquete encontrado (camiones:", data.cromosoma.length, "), desactivando polling...");
        setPollingActivo(false);

        // Aplicar el primer paquete
        await onPaqueteEncontrado(data);
      } else {
        console.log("⏳ POLLING: No hay paquetes disponibles aún, continuando...");
      }
    } catch (error) {
      // Silenciar errores esperados cuando no hay paquetes disponibles
      const errorStr = String(error);
      if (!errorStr.includes('No hay paquetes') && !errorStr.includes('null')) {
        console.log("⚠️ POLLING: Error al buscar paquetes:", error);
      }
    }
  }, 2000); // Verificar cada 2 segundos

  return () => {
    console.log("🛑 POLLING: Limpiando interval de polling");
    console.log("___________________________FIN DEL POLLING___________________________FIN");
    clearInterval(interval);
  };
}; 