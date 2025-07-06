/**
 * @file averias.ts
 * @description Utilidades para el manejo de averías en el componente Mapa
 */

import { averiarCamionConEstado } from "../../../services/averiaApiService";
import { eliminarPaquetesFuturos } from "../../../services/simulacionApiService";
import { toast, Bounce } from 'react-toastify';
import { pausarSimulacion as pausarSimulacionUtil } from "../../../context/simulacion/utils/controles";
import { capturarEstadoCompleto, generarResumenEstado, convertirEstadoParaBackend, type EstadoSimulacionCompleto } from "../../../context/simulacion/utils/estado";
import { calcularTimestampSimulacion } from "../../../context/simulacion/utils/tiempo";
import type { CamionEstado, RutaCamion, Bloqueo } from "../../../context/SimulacionContext";
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
  setPollingActivo?: (value: boolean) => void
): Promise<void> => {
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
    
    // 3. Capturar el estado completo actual
    console.log("📸 CAPTURANDO ESTADO COMPLETO...");
    const estadoCompleto: EstadoSimulacionCompleto = capturarEstadoCompleto(estadoSimulacion);
    
    // 4. Generar resumen del estado para logs
    const resumenEstado = generarResumenEstado(estadoCompleto);
    console.log("📊 RESUMEN DEL ESTADO AL MOMENTO DE LA AVERÍA:");
    console.log(resumenEstado);
    
    // 5. CRÍTICO: Eliminar paquetes futuros - esta operación debe ser exitosa
    console.log("🗑️ ELIMINANDO PAQUETES FUTUROS (CRÍTICO)...");
    try {
      await eliminarPaquetesFuturos();
      console.log("✅ Paquetes futuros eliminados exitosamente");
    } catch (error) {
      console.error("❌ ERROR CRÍTICO al eliminar paquetes futuros:", error);
      // Mostrar error específico al usuario
      toast.error(`❌ Error crítico: No se pudieron eliminar los paquetes futuros. La avería podría no procesarse correctamente.`, {
        position: "top-right",
        autoClose: 8000,
        hideProgressBar: false,
        closeOnClick: false,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      // Re-lanzar el error para que se maneje en el catch principal
      throw new Error(`Error crítico al eliminar paquetes futuros: ${error}`);
    }
    
    // 6. Enviar avería con estado completo al backend
    console.log("📡 ENVIANDO AVERÍA CON ESTADO COMPLETO...");
    console.log("📅 TIMESTAMP USADO PARA AVERÍA:", fechaHoraReporte);
    const estadoParaBackend = convertirEstadoParaBackend(estadoCompleto);
    await averiarCamionConEstado(camionId, tipo, fechaHoraReporte, estadoParaBackend);
    
    // 7. Marcar el camión como averiado en el contexto
    console.log("🔄 ACTUALIZANDO ESTADO LOCAL...");
    marcarCamionAveriado(camionId);
    
    // 8. Mostrar toast de éxito
    toast.success(`🚛💥 Camión ${camionId} averiado (Tipo ${tipo}) - Simulación pausada y paquetes futuros eliminados`, {
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
      paquetesFuturosEliminados: true,
      simulacionPausada: true,
      pollingDetenido: !!setPollingActivo
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
  }
}; 