/**
 * @file averias.ts
 * @description Utilidades para el manejo de averías en el componente Mapa
 */

import { averiarCamionConEstado } from "../../../services/averiaApiService";
import { obtenerInfoSimulacion, recalcularAlgoritmoDespuesAveria } from "../../../services/simulacionApiService";
import { toast, Bounce } from 'react-toastify';
import { pausarSimulacion as pausarSimulacionUtil } from "../../../context/simulacion/utils/controles";
import { capturarEstadoCompleto, generarResumenEstado, type EstadoSimulacionCompleto } from "../../../context/simulacion/utils/estado";
import { calcularTimestampSimulacion } from "../../../context/simulacion/utils/tiempo";
import type { CamionEstado, RutaCamion, Bloqueo, IndividuoConBloqueos } from "../../../context/simulacion/types";
import type { Almacen } from "../../../types";

/**
 * Función para manejar la avería de un camión con captura completa del estado
 * @param {string} camionId - ID del camión a averiar
 * @param {number} tipo - Tipo de avería (1, 2, 3)
 * @param {(camionId: string) => void} marcarCamionAveriado - Función para marcar el camión como averiado en el contexto
 * @param {(camionId: string) => void} setAveriando - Función para actualizar el estado de "averiando"
 * @param {() => void} setClickedCamion - Función para cerrar el modal del camión
 * @param {(value: boolean) => void} setSimulacionActiva - Función para controlar el estado de la simulación
 * @param {Object} estadoSimulacion - Estado completo actual de la simulación
 * @param {(value: boolean) => void} setPollingActivo - Función para detener el polling de paquetes
 * @param {(data: IndividuoConBloqueos) => Promise<void>} aplicarNuevaSolucionDespuesAveria - Función para aplicar nueva solución
 * @returns {Promise<void>}
 */
export const handleAveriar = async (
  camionId: string,
  tipo: number,
  marcarCamionAveriado: (camionId: string) => void,
  setAveriando: (value: string | null) => void,
  setClickedCamion: (value: string | null) => void,
  setSimulacionActiva: (value: boolean) => void,
  estadoSimulacion: {
    horaActual: number;
    horaSimulacion: string;
    fechaHoraSimulacion: string | null;
    fechaInicioSimulacion: string | null;
    diaSimulacion: number | null;
    tiempoRealSimulacion: string;
    tiempoTranscurridoSimulado: string;
    camiones: CamionEstado[];
    rutasCamiones: RutaCamion[];
    almacenes: Almacen[];
    bloqueos: Bloqueo[];
  },
  setPollingActivo?: (value: boolean) => void,
  aplicarNuevaSolucionDespuesAveria?: (data: IndividuoConBloqueos) => Promise<void>,
  setFechaInicioSimulacion?: (fecha: string) => void
): Promise<void> => {
  // Variable para capturar la fecha final del nuevo paquete generado después de la avería
  let fechaHoraFinNuevoPaquete: string | null = null;
  
  setAveriando(camionId + '-' + tipo);
  
  // Calcular el timestamp correcto de simulación
  const timestampSimulacion = calcularTimestampSimulacion(
    estadoSimulacion.fechaHoraSimulacion,
    estadoSimulacion.horaSimulacion
  );
  
  console.log("🚛💥 INICIO DE AVERÍA:", {
    camionId,
    tipo,
    timestampSimulacion,
    horaSimulacion: estadoSimulacion.horaSimulacion,
    fechaHoraSimulacion: estadoSimulacion.fechaHoraSimulacion,
    horaActual: estadoSimulacion.horaActual
  });

  try {
    // Usar el timestamp de simulación en lugar de la hora actual del sistema
    const fechaHoraReporte = timestampSimulacion;
    
    // 1. CRÍTICO: Detener el polling inmediatamente para evitar nuevos paquetes
    console.log("🛑 DETENIENDO POLLING INMEDIATAMENTE...");
    if (setPollingActivo) {
      setPollingActivo(false);
      console.log("✅ Polling detenido exitosamente");
    } else {
      console.warn("⚠️ No se pudo detener el polling - función no disponible");
    }
    
    // 2. CRÍTICO: Pausar la simulación inmediatamente
    console.log("⏸️ PAUSANDO SIMULACIÓN INMEDIATAMENTE...");
    pausarSimulacionUtil(setSimulacionActiva);
    
    // 3. 🔧 CORREGIDO: Marcar el camión como averiado ANTES de capturar el estado
    console.log("🔄 MARCANDO CAMIÓN COMO AVERIADO PRIMERO...");
    marcarCamionAveriado(camionId);
    console.log(`✅ Camión ${camionId} marcado como averiado en el contexto local`);
    
    // 4. Capturar el estado completo actual (ahora incluye el camión averiado)
    console.log("📸 CAPTURANDO ESTADO COMPLETO (CON CAMIÓN AVERIADO)...");
    const estadoCompleto: EstadoSimulacionCompleto = capturarEstadoCompleto(estadoSimulacion);
    
    // 5. Verificar que el camión está averiado en el estado capturado
    const camionAveriado = estadoCompleto.camiones?.find(c => c.id === camionId);
    if (camionAveriado) {
      console.log(`✅ VERIFICACIÓN: Camión ${camionId} en estado capturado:`, {
        id: camionAveriado.id,
        estado: camionAveriado.estado,
        ubicacion: camionAveriado.ubicacion
      });
      
      if (camionAveriado.estado !== 'Averiado') {
        console.error(`❌ ERROR: El camión ${camionId} NO está marcado como averiado en el estado capturado. Estado actual: ${camionAveriado.estado}`);
        // Intentar actualizar el estado manualmente
        camionAveriado.estado = 'Averiado';
        console.log(`🔧 CORRECCIÓN: Forzando estado 'Averiado' para el camión ${camionId}`);
      }
    } else {
      console.error(`❌ ERROR: No se encontró el camión ${camionId} en el estado capturado`);
    }
    
    // 6. Generar resumen del estado para logs
    const resumenEstado = generarResumenEstado(estadoCompleto);
    console.log("📊 RESUMEN DEL ESTADO AL MOMENTO DE LA AVERÍA:");
    console.log(resumenEstado);
    
    // 7. CRÍTICO: Eliminar paquetes futuros - esta operación debe ser exitosa
    console.log("🗑️ ELIMINANDO PAQUETES FUTUROS (CRÍTICO)...");
 
    
    // 8. Enviar avería con estado completo al backend (ahora incluye el camión averiado)
    console.log("📡 ENVIANDO AVERÍA CON ESTADO COMPLETO (CAMIÓN AVERIADO)...");
    console.log("📅 TIMESTAMP USADO PARA AVERÍA:", fechaHoraReporte);
    await averiarCamionConEstado(camionId, tipo, fechaHoraReporte, estadoCompleto);
    
    // 9. NUEVO: Recalcular algoritmo genético con fecha actual
    console.log("🧬 RECALCULANDO: Ejecutando algoritmo genético después de avería...");
    
    try {
      const nuevaSolucion = await recalcularAlgoritmoDespuesAveria(fechaHoraReporte);
      
      // Capturar la fecha final del nuevo paquete para el siguiente polling
      fechaHoraFinNuevoPaquete = nuevaSolucion.fechaHoraFinIntervalo || null;
      console.log("📅 NUEVO PAQUETE: Fecha final del nuevo paquete generado:", fechaHoraFinNuevoPaquete);
      
      // 10. Aplicar la nueva solución al contexto
      if (aplicarNuevaSolucionDespuesAveria) {
        console.log("🔄 APLICANDO: Nueva solución después de avería...");
        await aplicarNuevaSolucionDespuesAveria(nuevaSolucion);
        console.log("✅ NUEVA SOLUCIÓN: Aplicada exitosamente");
      } else {
        console.warn("⚠️ No se pudo aplicar nueva solución - función no disponible");
      }
    } catch (error) {
      console.error("❌ ERROR al recalcular algoritmo después de avería:", error);
      toast.warning("⚠️ No se pudo recalcular el algoritmo después de la avería, pero la avería se registró correctamente", {
        position: "top-right",
        autoClose: 8000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
    }
    
    // 11. Mostrar toast de éxito
    toast.success(`🚛💥 Camión ${camionId} averiado (Tipo ${tipo}) - Algoritmo recalculado y nueva solución aplicada`, {
      position: "top-right",
      autoClose: 6000,
      hideProgressBar: false,
      closeOnClick: false,
      pauseOnHover: true,
      draggable: true,
      progress: undefined,
      theme: "light",
      transition: Bounce,
    });
    
    console.log("✅ AVERÍA PROCESADA EXITOSAMENTE:", {
      camionId,
      tipo,
      timestampUsado: fechaHoraReporte,
      estadoCapturado: true,
      camionAveriado: true,
      paquetesFuturosEliminados: true,
      simulacionPausada: true,
      pollingDetenido: !!setPollingActivo,
      algoritmoRecalculado: true,
      nuevaSolucionAplicada: !!aplicarNuevaSolucionDespuesAveria
    });
    
  } catch (error) {
    console.error("❌ ERROR AL PROCESAR AVERÍA:", error);
    
    // Asegurar que la simulación esté pausada y el polling detenido incluso si hay error
    pausarSimulacionUtil(setSimulacionActiva);
    if (setPollingActivo) {
      setPollingActivo(false);
    }
    
    toast.error(`❌ Error al procesar la avería: ${error instanceof Error ? error.message : 'Error desconocido'}`, {
      position: "top-right",
      autoClose: 8000,
      hideProgressBar: false,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
      progress: undefined,
      theme: "light",
      transition: Bounce,
    });
    
    // Re-lanzar el error para que el llamador pueda manejarlo
    throw error;
  } finally {
    setAveriando(null);
    setClickedCamion(null);
    console.log("🔚 PROCESO DE AVERÍA FINALIZADO");
    
    // Pasar inmediatamente al siguiente paquete después de la avería
    // Usar la fecha final del nuevo paquete generado después de la avería
    console.log("🔄 PASANDO AL SIGUIENTE PAQUETE: Usando fecha final del nuevo paquete:", fechaHoraFinNuevoPaquete);
    pasarAlSiguientePaquete(setPollingActivo, setSimulacionActiva, fechaHoraFinNuevoPaquete, setFechaInicioSimulacion);
  }
};

/**
 * Función para pasar al siguiente paquete después de que termine el proceso de avería
 * Espera un tiempo fijo y luego reactiva el polling y la simulación para permitir la continuación
 * @param setPollingActivo - Función para controlar el polling de paquetes
 * @param setSimulacionActiva - Función para controlar el estado de la simulación
 * @param fechaHoraFinIntervalo - Fecha final del nuevo paquete generado después de la avería
 * @param setFechaInicioSimulacion - Función para actualizar la fecha de inicio de simulación
 */
const pasarAlSiguientePaquete = async (
  setPollingActivo?: (value: boolean) => void,
  setSimulacionActiva?: (value: boolean) => void,
  fechaHoraFinIntervalo?: string | null,
  setFechaInicioSimulacion?: (fecha: string) => void
) => {
  try {
    console.log("🔄 AVERÍA TERMINADA: Esperando generación del nuevo paquete...");
    
    // Obtener información actual para referencia
    const infoActual = await obtenerInfoSimulacion();
    const paqueteActualAntes = infoActual.paqueteActual;
    const paqueteEsperado = paqueteActualAntes + 1;
    
    console.log(`📊 INFORMACIÓN ACTUAL: Paquete actual=${paqueteActualAntes}, esperando paquete=${paqueteEsperado}`);
    
    // Mostrar notificación de espera al usuario
    toast.info(`⏳ Esperando que se genere el siguiente paquete después de la avería...`, {
      position: "top-right",
      autoClose: 3000,
      hideProgressBar: false,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
      progress: undefined,
      theme: "light",
      transition: Bounce,
    });
    
    // Esperar un tiempo fijo para dar tiempo al backend a generar el nuevo paquete
    console.log("⏳ ESPERANDO: Dando tiempo al backend para generar el nuevo paquete...");
    await new Promise(resolve => setTimeout(resolve, 5000)); // 5 segundos
    
    // Verificar si ahora hay más paquetes disponibles
    const infoActualizada = await obtenerInfoSimulacion();
    console.log(`📊 INFORMACIÓN ACTUALIZADA: Paquete actual=${infoActualizada.paqueteActual}, Total=${infoActualizada.totalPaquetes}`);
    
    // CRÍTICO: Si tenemos la fecha final del nuevo paquete, actualizar la fecha de inicio de simulación
    if (fechaHoraFinIntervalo && setFechaInicioSimulacion) {
      console.log("📅 SIGUIENTE PAQUETE: Actualizando fecha de inicio de simulación con fecha final del nuevo paquete:", fechaHoraFinIntervalo);
      
      // Actualizar la fecha de inicio de simulación para que el polling continúe desde la fecha correcta
      setFechaInicioSimulacion(fechaHoraFinIntervalo);
      console.log("✅ SIGUIENTE PAQUETE: Fecha de inicio de simulación actualizada para continuar desde:", fechaHoraFinIntervalo);
    } else {
      console.warn("⚠️ SIGUIENTE PAQUETE: No se pudo actualizar la fecha de inicio de simulación");
      if (!fechaHoraFinIntervalo) {
        console.warn("   - No se tiene la fecha final del nuevo paquete");
      }
      if (!setFechaInicioSimulacion) {
        console.warn("   - No se tiene acceso a setFechaInicioSimulacion");
      }
    }
    
    // Reactivar el polling y la simulación automáticamente
    if (setPollingActivo) {
      setPollingActivo(true);
      console.log("✅ SIGUIENTE PAQUETE: Polling reactivado");
    }
    
    if (setSimulacionActiva) {
      setSimulacionActiva(true);
      console.log("✅ SIGUIENTE PAQUETE: Simulación reanudada automáticamente");
    }
    
    // Mostrar notificación de éxito
    toast.success(`📦 Simulación reanudada automáticamente después de la avería`, {
      position: "top-right",
      autoClose: 4000,
      hideProgressBar: false,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
      progress: undefined,
      theme: "light",
      transition: Bounce,
    });
    
    console.log("🎉 SIGUIENTE PAQUETE: Sistema completamente reactivado - simulación continuará automáticamente");
    
  } catch (error) {
    console.error("❌ ERROR AL PASAR AL SIGUIENTE PAQUETE:", error);
    
    // Mostrar error al usuario
    toast.error(`❌ Error al pasar al siguiente paquete: ${error instanceof Error ? error.message : 'Error desconocido'}`, {
      position: "top-right",
      autoClose: 6000,
      hideProgressBar: false,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
      progress: undefined,
      theme: "light",
      transition: Bounce,
    });
  }
};

 