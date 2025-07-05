/**
 * @file averias.ts
 * @description Utilidades para el manejo de averías en el componente Mapa
 */

import { averiarCamionTipo } from "../../../services/averiaApiService";
import { toast, Bounce } from 'react-toastify';

/**
 * Función para manejar la avería de un camión
 * @param {string} camionId - ID del camión a averiar
 * @param {number} tipo - Tipo de avería (1, 2, 3)
 * @param {(camionId: string) => void} marcarCamionAveriado - Función para marcar el camión como averiado en el contexto
 * @param {(camionId: string) => void} setAveriando - Función para actualizar el estado de "averiando"
 * @param {() => void} setClickedCamion - Función para cerrar el modal del camión
 * @param {() => void} pausarSimulacion - Función para pausar la simulación
 * @returns {Promise<void>}
 */
export const handleAveriar = async (
  camionId: string,
  tipo: number,
  marcarCamionAveriado: (camionId: string) => void,
  setAveriando: (value: string | null) => void,
  setClickedCamion: (value: string | null) => void,
  pausarSimulacion: () => void
): Promise<void> => {
  setAveriando(camionId + '-' + tipo);
  try {
    const fechaHoraReporte = new Date().toISOString();
    await averiarCamionTipo(camionId, tipo, fechaHoraReporte);
    
    // Marcar el camión como averiado en el contexto
    marcarCamionAveriado(camionId);
    
    // Pausar la simulación
    pausarSimulacion();
    
    // Mostrar toast de éxito
    toast.error(`🚛💥 Camión ${camionId} averiado (Tipo ${tipo}) - Simulación pausada`, {
      position: "top-right",
      autoClose: 5000,
      hideProgressBar: false,
      closeOnClick: false,
      pauseOnHover: true,
      draggable: true,
      progress: undefined,
      theme: "light",
      transition: Bounce,
    });
    
  } catch {
    toast.error('❌ Error al averiar el camión', {
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
  } finally {
    setAveriando(null);
    setClickedCamion(null);
  }
}; 