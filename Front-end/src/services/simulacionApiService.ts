import type { Individuo } from "../types";
import { API_URLS } from "../config/api";

export async function getMejorIndividuo(fecha?: string): Promise<Individuo> {
  try {
    console.log("Iniciando solicitud al servidor...");
    
    // Construir la URL con el parámetro de fecha si se proporciona
    let url = `${API_URLS.MEJOR_INDIVIDUO}`;
    if (fecha) {
      url += `?fecha=${encodeURIComponent(fecha)}`;
      console.log("Solicitando paquete para fecha:", fecha);
    } else {
      console.log("Solicitando paquete sin fecha específica");
    }
    
    const response = await fetch(url);
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

/**
 * Elimina los paquetes futuros de la simulación, manteniendo solo el actual
 * @returns Promise con el mensaje de confirmación
 */
export async function eliminarPaquetesFuturos(): Promise<string> {
  try {
    console.log("🗑️ PAQUETES: Eliminando paquetes futuros de la simulación...");
    
    const response = await fetch(`${API_URLS.ELIMINAR_PAQUETES_FUTUROS}`, {
      method: 'DELETE'
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ PAQUETES: Error al eliminar paquetes futuros:", errorText);
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const mensaje = await response.text();
    console.log("✅ PAQUETES: Paquetes futuros eliminados exitosamente:", mensaje);
    return mensaje;
  } catch (error) {
    console.error("Error al eliminar paquetes futuros:", error);
    throw error;
  }
}

/**
 * Obtiene el siguiente paquete disponible en la simulación
 * @returns Promise con el siguiente paquete o null si no hay más paquetes
 */
export async function obtenerSiguientePaquete(): Promise<Individuo | null> {
  try {
    console.log("📦 PAQUETES: Obteniendo siguiente paquete de la simulación...");
    
    const response = await fetch(`${API_URLS.MEJOR_INDIVIDUO}`, {
      method: 'GET'
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ PAQUETES: Error al obtener siguiente paquete:", errorText);
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const contentType = response.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
      console.error("❌ PAQUETES: Tipo de contenido inválido:", contentType);
      throw new Error("La respuesta del servidor no es JSON válido");
    }

    if (response.status === 204) {
      console.log("⏳ PAQUETES: No hay más paquetes disponibles (204)");
      return null;
    }

    const data = await response.json();
    
    if (!data) {
      console.log("⏳ PAQUETES: Respuesta vacía - no hay más paquetes");
      return null;
    }

    console.log("✅ PAQUETES: Siguiente paquete obtenido exitosamente");
    return data;
  } catch (error) {
    console.error("❌ PAQUETES: Error al obtener siguiente paquete:", error);
    throw error;
  }
}
