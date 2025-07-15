import type { Individuo } from "../types";
import { API_URLS } from "../config/api";

/**
 * Obtiene el mejor individuo (paquete) de la simulación sin parámetros de fecha
 * @returns Promise con el paquete actual
 * @deprecated Use obtenerMejorIndividuo with fecha parameter instead
 */
export async function getMejorIndividuo(): Promise<Individuo> {
  try {
    const response = await fetch(`${API_URLS.MEJOR_INDIVIDUO}`);
    
    const contentType = response.headers.get("content-type");
    
    if (!contentType || !contentType.includes("application/json")) {
      throw new Error("La respuesta del servidor no es JSON válido");
    }

    if (response.status === 204) {
      throw new Error("No hay datos disponibles en este momento");
    }

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    
    if (!data) {
      throw new Error("La respuesta está vacía");
    }

    return data as Individuo;
  } catch (error) {
    if (error instanceof TypeError && error.message.includes('fetch')) {
      throw new Error('Error de conexión: No se pudo conectar con el servidor');
    }
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
    const url = new URL(API_URLS.INICIAR_SIMULACION);
    url.searchParams.append('fecha', fechaInicio);
    
    const response = await fetch(url.toString(), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      const errorMessage = errorText || `Error del servidor (${response.status})`;
      throw new Error(`Error al iniciar simulación: ${errorMessage}`);
    }

    const mensaje = await response.text();
    if (!mensaje) {
      throw new Error('El servidor no proporcionó una respuesta válida');
    }
    
    return mensaje;
  } catch (error) {
    if (error instanceof TypeError && error.message.includes('fetch')) {
      throw new Error('Error de conexión: No se pudo conectar con el servidor');
    }
    throw error;
  }
}

/**
 * Reinicia la simulación actual
 * @returns Promise con el mensaje de confirmación
 */
export async function reiniciarSimulacion(): Promise<string> {
  const response = await fetch(`${API_URLS.REINICIAR_SIMULACION}`, {
    method: 'GET'
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error ${response.status}: ${errorText}`);
  }

  const mensaje = await response.text();
  return mensaje;
}

/**
 * Elimina los paquetes futuros de la simulación, manteniendo solo el actual
 * @returns Promise con el mensaje de confirmación
 */
export async function eliminarPaquetesFuturos(): Promise<string> {
  const response = await fetch(`${API_URLS.ELIMINAR_PAQUETES_FUTUROS}`, {
    method: 'DELETE'
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error ${response.status}: ${errorText}`);
  }

  const mensaje = await response.text();
  return mensaje;
}

/**
 * Obtiene el mejor individuo (paquete) de la simulación para una fecha específica
 * @param fecha - Fecha en formato ISO (YYYY-MM-DDTHH:MM:SS)
 * @returns Promise con el paquete o null si no hay paquetes disponibles
 */
export async function obtenerMejorIndividuo(fecha: string): Promise<Individuo | null> {
  try {
    const url = new URL(API_URLS.MEJOR_INDIVIDUO);
    url.searchParams.append('fecha', fecha);
    
    const response = await fetch(url.toString(), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    if (response.status === 204) {
      return null;
    }

    if (!response.ok) {
      const errorText = await response.text();
      const errorMessage = errorText || `Error del servidor (${response.status})`;
      throw new Error(`Error al obtener paquete: ${errorMessage}`);
    }

    const contentType = response.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
      throw new Error("La respuesta del servidor no es JSON válido");
    }

    const data = await response.json();
    
    if (!data) {
      return null;
    }

    return data as Individuo;
  } catch (error) {
    if (error instanceof TypeError && error.message.includes('fetch')) {
      throw new Error('Error de conexión: No se pudo conectar con el servidor');
    }
    throw error;
  }
}

/**
 * Obtiene el siguiente paquete disponible en la simulación
 * @returns Promise con el siguiente paquete o null si no hay más paquetes
 * @deprecated Use obtenerMejorIndividuo instead
 */
export async function obtenerSiguientePaquete(): Promise<Individuo | null> {
  const response = await fetch(`${API_URLS.MEJOR_INDIVIDUO}`, {
    method: 'GET'
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error ${response.status}: ${errorText}`);
  }

  const contentType = response.headers.get("content-type");
  if (!contentType || !contentType.includes("application/json")) {
    throw new Error("La respuesta del servidor no es JSON válido");
  }

  if (response.status === 204) {
    return null;
  }

  const data = await response.json();
  
  if (!data) {
    return null;
  }

  return data;
}
