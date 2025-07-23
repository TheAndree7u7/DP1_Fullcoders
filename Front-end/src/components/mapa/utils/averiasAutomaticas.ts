/**
 * @file averiasAutomaticas.ts
 * @description Utilidades para el manejo de averías automáticas en el componente Mapa
 */

import { averiarCamionConEstado } from "../../../services/averiaApiService";
import { toast, Bounce } from 'react-toastify';
import { capturarEstadoCompleto, generarResumenEstado, type EstadoSimulacionCompleto } from "../../../context/simulacion/utils/estado";
import { calcularTimestampSimulacion } from "../../../context/simulacion/utils/tiempo";
import type { CamionEstado, RutaCamion, Bloqueo } from "../../../context/simulacion/types";
import type { Almacen } from "../../../types";

/**
 * Función para manejar la avería automática de un camión
 * @param {string} camionId - ID del camión averiado automáticamente
 * @param {string} tipoNodo - Tipo de nodo de avería automática (AVERIA_AUTOMATICA_T1, AVERIA_AUTOMATICA_T2, AVERIA_AUTOMATICA_T3)
 * @param {Object} estadoSimulacion - Estado completo actual de la simulación
 * @param {boolean} mostrarToasts - Si debe mostrar notificaciones toast (por defecto true)
 * @returns {Promise<void>}
 */
export const handleAveriaAutomatica = async (
  camionId: string,
  tipoNodo: string,
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
  mostrarToasts: boolean = true
): Promise<void> => {
  console.log("🚛💥 INICIO DE AVERÍA AUTOMÁTICA:", {
    camionId,
    tipoNodo,
    horaSimulacion: estadoSimulacion.horaSimulacion,
    fechaHoraSimulacion: estadoSimulacion.fechaHoraSimulacion,
    horaActual: estadoSimulacion.horaActual
  });

  try {
    // Calcular el timestamp correcto de simulación
    const timestampSimulacion = calcularTimestampSimulacion(
      estadoSimulacion.fechaHoraSimulacion,
      estadoSimulacion.horaSimulacion
    );
    
    // Extraer el tipo de avería (T1, T2, T3) del tipo de nodo
    let tipoAveria: number;
    let tipoAveriaString: string;
    
    if (tipoNodo === 'AVERIA_AUTOMATICA_T1') {
      tipoAveria = 1;
      tipoAveriaString = 'T1';
    } else if (tipoNodo === 'AVERIA_AUTOMATICA_T2') {
      tipoAveria = 2;
      tipoAveriaString = 'T2';
    } else if (tipoNodo === 'AVERIA_AUTOMATICA_T3') {
      tipoAveria = 3;
      tipoAveriaString = 'T3';
    } else {
      tipoAveria = 1; // Por defecto T1
      tipoAveriaString = 'T1';
    }

    // Capturar el estado completo actual
    console.log("📸 CAPTURANDO ESTADO COMPLETO PARA AVERÍA AUTOMÁTICA...");
    const estadoCompleto: EstadoSimulacionCompleto = capturarEstadoCompleto(estadoSimulacion);
    
    // Generar resumen del estado para logs
    const resumenEstado = generarResumenEstado(estadoCompleto);
    console.log("📊 RESUMEN DEL ESTADO AL MOMENTO DE LA AVERÍA AUTOMÁTICA:");
    console.log(resumenEstado);
    
    // Enviar avería automática al backend con el tipo correcto (T1, T2, T3)
    console.log("📡 ENVIANDO AVERÍA AUTOMÁTICA AL BACKEND...");
    console.log("📅 TIMESTAMP USADO PARA AVERÍA AUTOMÁTICA:", timestampSimulacion);
    console.log("🔧 TIPO DE AVERÍA:", tipoAveriaString, "(ID:", tipoAveria, ")");
    await averiarCamionConEstado(camionId, tipoAveria, timestampSimulacion, estadoCompleto);
    
    // Mostrar toast informativo
    if (mostrarToasts) {
      toast.info(`🚛💥 Camión ${camionId} averiado automáticamente (${tipoAveriaString})`, {
        position: "top-right",
        autoClose: 5000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
    }
    
    console.log("✅ AVERÍA AUTOMÁTICA PROCESADA EXITOSAMENTE:", {
      camionId,
      tipoNodo,
      tipoAveria: tipoAveriaString,
      tipoAveriaId: tipoAveria,
      timestampUsado: timestampSimulacion,
      estadoCapturado: true
    });
    
  } catch (error) {
    console.error("❌ ERROR AL PROCESAR AVERÍA AUTOMÁTICA:", error);
    
    if (mostrarToasts) {
      toast.error(`❌ Error al procesar la avería automática: ${error instanceof Error ? error.message : 'Error desconocido'}`, {
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
    
    // Re-lanzar el error para que el llamador pueda manejarlo
    throw error;
  }
}; 