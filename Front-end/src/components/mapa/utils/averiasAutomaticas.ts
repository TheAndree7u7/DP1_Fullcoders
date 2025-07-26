/**
 * @file averiasAutomaticas.ts
 * @description Utilidades para el manejo de averías automáticas en el componente Mapa
 */

import { toast, Bounce } from 'react-toastify';
import type { CamionEstado, RutaCamion, Bloqueo } from "../../../context/simulacion/types";
import type { Almacen } from "../../../types";

/**
 * Función para manejar la avería automática de un camión (solo frontend)
 * @param {string} camionId - ID del camión averiado automáticamente
 * @param {string} tipoNodo - Tipo de nodo de avería automática (AVERIA_AUTOMATICA_T1, AVERIA_AUTOMATICA_T2, AVERIA_AUTOMATICA_T3)
 * @param {Object} estadoSimulacion - Estado completo actual de la simulación
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
  }
): Promise<void> => {
  console.log("🚛💥 INICIO DE AVERÍA AUTOMÁTICA:", {
    camionId,
    tipoNodo,
    horaSimulacion: estadoSimulacion.horaSimulacion,
    fechaHoraSimulacion: estadoSimulacion.fechaHoraSimulacion,
    horaActual: estadoSimulacion.horaActual
  });

  try {
    // Extraer el tipo de avería (T1, T2, T3) del tipo de nodo
    let tipoAveriaString: string;
    
    if (tipoNodo === 'AVERIA_AUTOMATICA_T1') {
      tipoAveriaString = 'T1';
    } else if (tipoNodo === 'AVERIA_AUTOMATICA_T2') {
      tipoAveriaString = 'T2';
    } else if (tipoNodo === 'AVERIA_AUTOMATICA_T3') {
      tipoAveriaString = 'T3';
    } else {
      tipoAveriaString = 'T1'; // Por defecto T1
    }
    
    // Mostrar toast informativo
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
    
    console.log("✅ AVERÍA AUTOMÁTICA PROCESADA EXITOSAMENTE:", {
      camionId,
      tipoNodo,
      tipoAveria: tipoAveriaString
    });
    
  } catch (error) {
    console.error("❌ ERROR AL PROCESAR AVERÍA AUTOMÁTICA:", error);
    
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
    
    // Re-lanzar el error para que el llamador pueda manejarlo
    throw error;
  }
}; 