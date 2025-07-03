import { API_URLS } from "../config/api";

export interface AveriaRequest {
  codigoCamion: string;
  tipoIncidente: string;
  descripcion?: string;
  fechaHoraReporte?: string; // ISO string format
  posicionesCamiones?: { id: string; ubicacion: string }[];
}

export interface AveriaResponse {
  mensaje: string;
  averia: {
    id: number;
    codigoCamion: string;
    tipoIncidente: string;
    fechaHoraOcurrencia: string;
    fechaHoraDisponible: string;
    estado: boolean;
  };
  fechaInicioRecalculo: string;
  fechaFinRecalculo: string;
  camionAveriado: string;
}

/**
 * Registra una avería y fuerza el recálculo de la simulación
 */
export async function registrarAveriaConRecalculo(averiaRequest: AveriaRequest): Promise<AveriaResponse> {
  try {
    console.log("🚨 FRONTEND: Registrando avería con recálculo...", averiaRequest);
    
    // Asegurar que siempre se envíe la fecha de reporte
    const requestWithDate = {
      ...averiaRequest,
      fechaHoraReporte: averiaRequest.fechaHoraReporte || new Date().toISOString()
    };
    
    const response = await fetch(`${API_URLS.AVERIAS}/averia-con-recalculo`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestWithDate),
    });

    if (!response.ok) {
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Error ${response.status}`);
      } else {
        const errorText = await response.text();
        throw new Error(errorText || `Error ${response.status}`);
      }
    }

    const result = await response.json();
    console.log("✅ FRONTEND: Avería registrada y simulación recalculada:", result);
    
    return result;
  } catch (error) {
    console.error("❌ FRONTEND: Error al registrar avería con recálculo:", error);
    throw error;
  }
}

/**
 * Lista todas las averías activas
 */
export async function listarAveriasActivas(): Promise<AveriaResponse['averia'][]> {
  try {
    const response = await fetch(`${API_URLS.AVERIAS}/activas`);
    
    if (!response.ok) {
      throw new Error(`Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error al obtener averías activas:", error);
    throw error;
  }
}

/**
 * Lista los códigos de camiones averiados
 */
export async function listarCamionesAveriados(): Promise<string[]> {
  try {
    const response = await fetch(`${API_URLS.AVERIAS}/camiones-averiados`);
    
    if (!response.ok) {
      throw new Error(`Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error al obtener camiones averiados:", error);
    throw error;
  }
} 