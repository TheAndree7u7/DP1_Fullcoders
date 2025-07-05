/**
 * @file averias.ts
 * @description Utilidades para el manejo de averías en el componente Mapa
 */

import { averiarCamionConEstado } from "../../../services/averiaApiService";
import { eliminarPaquetesFuturos } from "../../../services/simulacionApiService";
import { toast, Bounce } from 'react-toastify';
import { pausarSimulacion as pausarSimulacionUtil } from "../../../context/simulacion/utils/controles";
import { capturarEstadoCompleto, generarResumenEstado, convertirEstadoParaBackend, type EstadoSimulacionCompleto } from "../../../context/simulacion/utils/estado";
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
  }
): Promise<void> => {
  setAveriando(camionId + '-' + tipo);
  
  console.log("🚛💥 INICIO DE AVERÍA:", {
    camionId,
    tipo,
    timestamp: new Date().toISOString(),
    horaSimulacion: estadoSimulacion.horaSimulacion
  });

  try {
    const fechaHoraReporte = new Date().toISOString();
    
    // 1. Capturar el estado completo actual
    console.log("📸 CAPTURANDO ESTADO COMPLETO...");
    const estadoCompleto: EstadoSimulacionCompleto = capturarEstadoCompleto(estadoSimulacion);
    
    // 2. Generar resumen del estado para logs
    const resumenEstado = generarResumenEstado(estadoCompleto);
    console.log("📊 RESUMEN DEL ESTADO AL MOMENTO DE LA AVERÍA:");
    console.log(resumenEstado);
    
    // 3. Eliminar paquetes futuros (mantener solo el actual)
    console.log("🗑️ ELIMINANDO PAQUETES FUTUROS...");
    try {
      await eliminarPaquetesFuturos();
      console.log("✅ Paquetes futuros eliminados exitosamente");
    } catch (error) {
      console.warn("⚠️ Error al eliminar paquetes futuros:", error);
      // Continuamos con la avería aunque falle la eliminación de paquetes
    }
    
    // 4. Enviar avería con estado completo al backend
    console.log("📡 ENVIANDO AVERÍA CON ESTADO COMPLETO...");
    const estadoParaBackend = convertirEstadoParaBackend(estadoCompleto);
    await averiarCamionConEstado(camionId, tipo, fechaHoraReporte, estadoParaBackend);
    
    // 5. Marcar el camión como averiado en el contexto
    console.log("🔄 ACTUALIZANDO ESTADO LOCAL...");
    marcarCamionAveriado(camionId);
    
    // 6. Pausar la simulación usando la utilidad
    console.log("⏸️ PAUSANDO SIMULACIÓN...");
    pausarSimulacionUtil(setSimulacionActiva);
    
    // 7. Mostrar toast de éxito
    toast.error(`🚛💥 Camión ${camionId} averiado (Tipo ${tipo}) - Estado guardado y simulación pausada`, {
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
      estadoCapturado: true,
      paquetesFuturosEliminados: true,
      simulacionPausada: true
    });
    
  } catch (error) {
    console.error("❌ ERROR AL PROCESAR AVERÍA:", error);
    toast.error('❌ Error al averiar el camión y capturar el estado', {
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
  } finally {
    setAveriando(null);
    setClickedCamion(null);
    console.log("🔚 PROCESO DE AVERÍA FINALIZADO");
  }
}; 