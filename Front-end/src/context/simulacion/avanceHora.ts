/**
 * @file avanceHora.ts
 * @description Lógica para el avance de hora en la simulación
 */

import type { 
  CamionEstado, 
  RutaCamion, 
  IndividuoConBloqueos 
} from "./types";
import { NODOS_PARA_ACTUALIZACION } from "./types";
import { avanzarTodosLosCamiones } from "./camionLogic";


/**
 * @function avanzarHora
 * @description Avanza la simulación una hora, actualizando la posición de los camiones
 * y recargando datos del backend cuando sea necesario
 */
export const avanzarHora = async (
  camiones: CamionEstado[],
  rutasCamiones: RutaCamion[],
  esperandoActualizacion: boolean,
  simulacionActiva: boolean,
  nodosRestantesAntesDeActualizar: number,
  solicitudAnticipadaEnviada: boolean,
  proximaSolucionCargada: IndividuoConBloqueos | null,
  setCamiones: (camiones: CamionEstado[]) => void,
  setHoraActual: (hora: number | ((prev: number) => number)) => void,
  setNodosRestantesAntesDeActualizar: (nodos: number) => void,
  setEsperandoActualizacion: (esperando: boolean) => void,
  setSolicitudAnticipadaEnviada: (enviada: boolean) => void,
  cargarSolucionAnticipadaLocal: () => Promise<void>,
  aplicarSolucionPrecargada: (data: IndividuoConBloqueos) => Promise<void>,
  cargarDatosSimulacion: () => Promise<void>
): Promise<void> => {
  if (esperandoActualizacion || !simulacionActiva) return;

  // Verificar si necesitamos solicitar anticipadamente la próxima solución
  const nodosTres4 = Math.floor(NODOS_PARA_ACTUALIZACION * 0.25);
  const nodosRestantes = nodosRestantesAntesDeActualizar - 1;

  if (nodosRestantes === nodosTres4 && !solicitudAnticipadaEnviada) {
    console.log("📅 ANTICIPADA: Llegamos a 3/4 del ciclo (nodo", NODOS_PARA_ACTUALIZACION - nodosRestantes, "de", NODOS_PARA_ACTUALIZACION, ") - Solicitando próxima solución...");
    setSolicitudAnticipadaEnviada(true);
    await cargarSolucionAnticipadaLocal();
  }

  // Avanzar todos los camiones
  const nuevosCamiones = avanzarTodosLosCamiones(camiones, rutasCamiones);

  const quedan = nodosRestantesAntesDeActualizar - 1;
  setNodosRestantesAntesDeActualizar(quedan);

  //! Aqui se actualiza al siguiente paquete
  if (quedan <= 0) {
    setEsperandoActualizacion(true);
    setCamiones(nuevosCamiones);
    setHoraActual((prev) => prev + 1);

    // Si ya tenemos la solución anticipada cargada, usarla directamente
    if (proximaSolucionCargada) {
      console.log("⚡ TRANSICIÓN: Usando solución anticipada precargada para transición suave");
      await aplicarSolucionPrecargada(proximaSolucionCargada);
    } else {
      console.log("🔄 TRANSICIÓN: Solución anticipada no disponible, cargando en tiempo real...");
      await cargarDatosSimulacion();
    }
  } else {
    setCamiones(nuevosCamiones);
    setHoraActual((prev) => prev + 1);
  }
}; 