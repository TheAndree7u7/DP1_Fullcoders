/**
 * @file stateManager.ts
 * @description Manejo de la limpieza y gestión del estado de la simulación
 */

import { 
  HORA_INICIAL, 
  NODOS_PARA_ACTUALIZACION
} from "./types";
import type {
  CamionEstado,
  RutaCamion,
  Bloqueo,
  IndividuoConBloqueos
} from "./types";
import type { Pedido } from "../../types";

/**
 * @function limpiarEstadoParaNuevaSimulacion
 * @description Limpia el estado para una nueva simulación y carga los primeros datos
 */
export const limpiarEstadoParaNuevaSimulacion = (
  setCamiones: (camiones: CamionEstado[]) => void,
  setRutasCamiones: (rutas: RutaCamion[]) => void,
  setBloqueos: (bloqueos: Bloqueo[]) => void,
  setPedidosNoAsignados: (pedidos: Pedido[]) => void,
  setFechaHoraSimulacion: (fecha: string | null) => void,
  setFechaHoraInicioIntervalo: (fecha: string | null) => void,
  setFechaHoraFinIntervalo: (fecha: string | null) => void,
  setDiaSimulacion: (dia: number | null) => void,
  setTiempoTranscurridoSimulado: (tiempo: string) => void,
  setHoraSimulacionAcumulada: (hora: string) => void,
  setFechaHoraAcumulada: (fecha: string) => void,
  setHoraActual: (hora: number) => void,
  setNodosRestantesAntesDeActualizar: (nodos: number) => void,
  setEsperandoActualizacion: (esperando: boolean) => void,
  setSolicitudAnticipadaEnviada: (enviada: boolean) => void,
  setProximaSolucionCargada: (solucion: IndividuoConBloqueos | null) => void,
  setPaqueteActualConsumido: (paquete: number) => void,
  setInicioSimulacion: (fecha: Date) => void,
  setTiempoRealSimulacion: (tiempo: string) => void,
  setSimulacionActiva: (activa: boolean) => void,
  setPollingActivo: (activo: boolean) => void,
  setCargando: (cargando: boolean) => void,
  fechaInicioSimulacion: string | null
): void => {
  console.log("🧹 LIMPIEZA: Limpiando estado para nueva simulación...");

  // Limpiar datos de simulación anterior (pero NO los almacenes)
  setCamiones([]);
  setRutasCamiones([]);
  setBloqueos([]);
  setPedidosNoAsignados([]);
  setFechaHoraSimulacion(null);
  // NO limpiar fechaInicioSimulacion para mantenerla durante toda la simulación semanal
  setFechaHoraInicioIntervalo(null);
  setFechaHoraFinIntervalo(null);
  setDiaSimulacion(null);
  setTiempoTranscurridoSimulado("00:00:00");
  setHoraSimulacionAcumulada("00:00:00");
  setFechaHoraAcumulada("");

  // Resetear contadores
  setHoraActual(HORA_INICIAL);
  setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
  setEsperandoActualizacion(false);
  setSolicitudAnticipadaEnviada(false);
  setProximaSolucionCargada(null);
  setPaqueteActualConsumido(0); // Resetear contador de paquetes

  // Iniciar contador de tiempo
  setInicioSimulacion(new Date());
  setTiempoRealSimulacion("00:00:00");
  setSimulacionActiva(true);

  // Detener cualquier polling anterior
  setPollingActivo(false);

  console.log("✅ LIMPIEZA: Estado limpio, manteniendo fecha de inicio de simulación semanal:", fechaInicioSimulacion);

  // Mientras esperamos el primer paquete, mostrar estado de carga
  setCargando(true);
  console.log("🔄 LIMPIEZA: Configurando estado de carga mientras esperamos primer paquete...");

  // No intentar cargar datos inmediatamente, solo usar polling para obtener el primer paquete
  console.log("🔄 LIMPIEZA: Iniciando polling para obtener el primer paquete disponible...");
  setPollingActivo(true);
};

/**
 * @function limpiarSimulacionCompleta
 * @description Limpia completamente la simulación incluyendo la fecha de inicio
 * Se usa cuando se inicia una nueva simulación semanal
 */
export const limpiarSimulacionCompleta = (
  setCamiones: (camiones: CamionEstado[]) => void,
  setRutasCamiones: (rutas: RutaCamion[]) => void,
  setBloqueos: (bloqueos: Bloqueo[]) => void,
  setPedidosNoAsignados: (pedidos: Pedido[]) => void,
  setFechaHoraSimulacion: (fecha: string | null) => void,
  setFechaInicioSimulacion: (fecha: string | null) => void,
  setFechaHoraInicioIntervalo: (fecha: string | null) => void,
  setFechaHoraFinIntervalo: (fecha: string | null) => void,
  setDiaSimulacion: (dia: number | null) => void,
  setTiempoTranscurridoSimulado: (tiempo: string) => void,
  setHoraSimulacionAcumulada: (hora: string) => void,
  setFechaHoraAcumulada: (fecha: string) => void,
  setHoraActual: (hora: number) => void,
  setNodosRestantesAntesDeActualizar: (nodos: number) => void,
  setEsperandoActualizacion: (esperando: boolean) => void,
  setSolicitudAnticipadaEnviada: (enviada: boolean) => void,
  setProximaSolucionCargada: (solucion: IndividuoConBloqueos | null) => void,
  setPaqueteActualConsumido: (paquete: number) => void,
  setSimulacionActiva: (activa: boolean) => void,
  setPollingActivo: (activo: boolean) => void,
  setCargando: (cargando: boolean) => void
): void => {
  console.log("🧹 LIMPIEZA COMPLETA: Limpiando toda la simulación incluyendo fecha de inicio...");

  // Limpiar TODOS los datos de simulación incluyendo fecha de inicio
  setCamiones([]);
  setRutasCamiones([]);
  setBloqueos([]);
  setPedidosNoAsignados([]);
  setFechaHoraSimulacion(null);
  setFechaInicioSimulacion(null); // Limpiar fecha de inicio para nueva simulación semanal
  setFechaHoraInicioIntervalo(null);
  setFechaHoraFinIntervalo(null);
  setDiaSimulacion(null);
  setTiempoTranscurridoSimulado("00:00:00");
  setHoraSimulacionAcumulada("00:00:00");
  setFechaHoraAcumulada("");

  // Resetear contadores
  setHoraActual(HORA_INICIAL);
  setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
  setEsperandoActualizacion(false);
  setSolicitudAnticipadaEnviada(false);
  setProximaSolucionCargada(null);
  setPaqueteActualConsumido(0);

  // Detener simulación
  setSimulacionActiva(false);
  setPollingActivo(false);
  setCargando(false);

  console.log("✅ LIMPIEZA COMPLETA: Simulación completamente limpiada, lista para nueva simulación semanal");
}; 