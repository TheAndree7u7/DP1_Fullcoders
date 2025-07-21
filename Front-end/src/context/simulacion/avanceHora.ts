/**
 * @file avanceHora.ts
 * @description Lógica para el avance de hora en la simulación
 */

import type { 
  CamionEstado, 
  RutaCamion, 
  IndividuoConBloqueos 
} from "./types";
import type { Almacen } from "../../types";
import { NODOS_PARA_ACTUALIZACION } from "./types";
import { avanzarTodosLosCamiones } from "./camionLogic";

/**
 * @function verificarYRecargarAlmacenes
 * @description Verifica si es medianoche (00:00:00) y recarga SOLO los almacenes intermedios a su capacidad máxima
 */
const verificarYRecargarAlmacenes = (
  almacenes: Almacen[],
  fechaHoraSimulacion: string | null,
  nodosRestantesAntesDeActualizar: number
): Almacen[] => {
  if (!fechaHoraSimulacion) return almacenes;

  try {
    // Calcular la hora actual de simulación
    const fechaBase = new Date(fechaHoraSimulacion);
    // Calcular cuántos nodos han pasado desde el inicio del ciclo actual
    const nodosTranscurridos = NODOS_PARA_ACTUALIZACION - nodosRestantesAntesDeActualizar;
    const segundosAdicionales = nodosTranscurridos * 36; // 36 segundos por nodo
    const fechaActual = new Date(fechaBase.getTime() + segundosAdicionales * 1000);
    
    // Log para debugging
    // console.log("🕒 VERIFICANDO RECARGA:", {
    //   fechaBase: fechaBase.toISOString(),
    //   nodosTranscurridos,
    //   segundosAdicionales,
    //   fechaActual: fechaActual.toISOString(),
    //   hora: fechaActual.getHours(),
    //   minutos: fechaActual.getMinutes(),
    //   segundos: fechaActual.getSeconds()
    // });
    
    // Verificar si es medianoche (00:00:00)
    const hora = fechaActual.getHours();
    const minutos = fechaActual.getMinutes();
    const segundos = fechaActual.getSeconds();
    
    if (hora === 0 && minutos === 0 && segundos === 0) {
      console.log("🔄 RECARGA AUTOMÁTICA: Es medianoche (00:00:00), recargando SOLO almacenes intermedios...");
      
      // Recargar SOLO los almacenes intermedios (SECUNDARIO) a su capacidad máxima
      const almacenesRecargados = almacenes.map(almacen => {
        if (almacen.tipo === 'SECUNDARIO') {
          console.log(`🔄 Recargando almacén intermedio: ${almacen.nombre}`);
          return {
            ...almacen,
            capacidadActualGLP: almacen.capacidadMaximaGLP,
            capacidadActualCombustible: almacen.capacidadCombustible
          };
        }
        // Los almacenes centrales no se recargan automáticamente
        return almacen;
      });
      
      // Contar cuántos almacenes intermedios se recargaron
      const almacenesIntermedios = almacenes.filter(a => a.tipo === 'SECUNDARIO');
      const almacenesRecargadosCount = almacenesIntermedios.length;
      
      console.log(`✅ RECARGA COMPLETADA: ${almacenesRecargadosCount} almacenes intermedios recargados a su capacidad máxima`);
      console.log("📊 ESTADO ALMACENES INTERMEDIOS:", almacenesRecargados
        .filter(a => a.tipo === 'SECUNDARIO')
        .map(a => ({
          nombre: a.nombre,
          GLP: `${a.capacidadActualGLP}/${a.capacidadMaximaGLP}`,
          Combustible: `${a.capacidadActualCombustible}/${a.capacidadCombustible}`
        })));
      
      return almacenesRecargados;
    }
  } catch (error) {
    console.warn("⚠️ Error al verificar recarga automática de almacenes:", error);
  }
  
  return almacenes;
};

/**
 * @function avanzarHora
 * @description Avanza la simulación una hora, actualizando la posición de los camiones
 * y recargando datos del backend cuando sea necesario
 */
export const avanzarHora = async (
  camiones: CamionEstado[],
  rutasCamiones: RutaCamion[],
  almacenes: Almacen[],
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
  cargarDatosSimulacion: () => Promise<void>,
  setAlmacenes: (almacenes: Almacen[]) => void,
  fechaHoraSimulacion: string | null
): Promise<void> => {
  if (esperandoActualizacion || !simulacionActiva) return;

  // Verificar si necesitamos solicitar anticipadamente la próxima solución
  const nodosTres4 = Math.floor(NODOS_PARA_ACTUALIZACION * 0.1);
  const nodosRestantes = nodosRestantesAntesDeActualizar - 1;

  if (nodosRestantes === nodosTres4 && !solicitudAnticipadaEnviada) {
    console.log("📅 ANTICIPADA: Llegamos a 3/4 del ciclo (nodo", NODOS_PARA_ACTUALIZACION - nodosRestantes, "de", NODOS_PARA_ACTUALIZACION, ") - Solicitando próxima solución...");
    setSolicitudAnticipadaEnviada(true);
    await cargarSolucionAnticipadaLocal();
  }

  // Verificar y recargar almacenes si es medianoche
  const almacenesActualizados = verificarYRecargarAlmacenes(almacenes, fechaHoraSimulacion, nodosRestantesAntesDeActualizar);
  if (almacenesActualizados !== almacenes) {
    setAlmacenes(almacenesActualizados);
  }

  // Avanzar todos los camiones
  const nuevosCamiones = avanzarTodosLosCamiones(camiones, rutasCamiones, almacenesActualizados, setAlmacenes);

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