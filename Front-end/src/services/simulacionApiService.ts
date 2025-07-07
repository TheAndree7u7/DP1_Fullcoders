import type { Individuo } from "../types";
import { API_URLS } from "../config/api";

export async function getMejorIndividuo(fecha?: string): Promise<Individuo> {
  try {
    console.log("Iniciando solicitud al servidor...");
    
    // Si no se proporciona fecha, usar la fecha actual
    const fechaRequest = fecha || new Date().toISOString();
    console.log("Enviando solicitud con fecha:", fechaRequest);
    
    const response = await fetch(`${API_URLS.MEJOR_INDIVIDUO}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        fecha: fechaRequest
      })
    });
    
    console.log("Respuesta recibida:", {
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries())
    });
    
    // Si la respuesta está vacía o no es JSON
    const contentType = response.headers.get("content-type");
    console.log("Content-Type:", contentType);
    
    if (!contentType || !contentType.includes("application/json")) {
      console.error("Tipo de contenido inválido:", contentType);
      throw new Error("La respuesta del servidor no es JSON válido");
    }

    if (response.status === 204) {
      console.log("No hay datos disponibles (204)");
      throw new Error("No hay datos disponibles en este momento");
    }

    if (!response.ok) {
      const errorData = await response.json();
      console.error("Error en la respuesta:", errorData);
      throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    console.log("Datos recibidos:", data);
    
    if (!data) {
      console.error("Respuesta vacía");
      throw new Error("La respuesta está vacía");
    }

    return data as Individuo;
  } catch (error) {
    console.error("Error al obtener el mejor individuo:", error);
    throw error;
  }
}

/**
 * Inicia una nueva simulación con una fecha específica
 * @param fechaInicio - Fecha y hora de inicio en formato ISO (YYYY-MM-DDTHH:MM:SS)
 * @returns Promise con el mensaje de confirmación
 */
export async function iniciarSimulacion(fechaInicio: string): Promise<string> {
  try {
    console.log("Iniciando simulación con fecha:", fechaInicio);
    
    const response = await fetch(`${API_URLS.INICIAR_SIMULACION}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        fechaInicio: fechaInicio
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const mensaje = await response.text();
    console.log("Simulación iniciada exitosamente:", mensaje);
    return mensaje;
  } catch (error) {
    console.error("Error al iniciar simulación:", error);
    throw error;
  }
}

/**
 * Reinicia la simulación actual
 * @returns Promise con el mensaje de confirmación
 */
export async function reiniciarSimulacion(): Promise<string> {
  try {
    console.log("Reiniciando simulación...");
    
    const response = await fetch(`${API_URLS.REINICIAR_SIMULACION}`, {
      method: 'GET'
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const mensaje = await response.text();
    console.log("Simulación reiniciada exitosamente:", mensaje);
    return mensaje;
  } catch (error) {
    console.error("Error al reiniciar simulación:", error);
    throw error;
  }
}

/**
 * Obtiene información sobre el estado de la simulación
 * @returns Promise con la información de la simulación
 */
export async function obtenerInfoSimulacion(): Promise<{
  totalPaquetes: number;
  paqueteActual: number;
  enProceso: boolean;
  tiempoActual: string;
}> {
  try {
    console.log("Obteniendo información de simulación...");
    
    const response = await fetch(`${API_URLS.INFO_SIMULACION}`, {
      method: 'GET'
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const info = await response.json();
    console.log("Información de simulación obtenida:", info);
    return info;
  } catch (error) {
    console.error("Error al obtener información de simulación:", error);
    throw error;
  }
}
