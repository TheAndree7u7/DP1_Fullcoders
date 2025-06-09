import type { Individuo } from "../types";
import { log, error as logError } from '../logControl';

const API_BASE_URL = "http://localhost:8085/api/simulacion";

export async function getMejorIndividuo(): Promise<Individuo> {
  try {
    log("Iniciando solicitud al servidor...");
    const response = await fetch(`${API_BASE_URL}/mejor`);
    log("Respuesta recibida:", {
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries())
    });
    
    // Si la respuesta está vacía o no es JSON
    const contentType = response.headers.get("content-type");
    log("Content-Type:", contentType);
    
    if (!contentType || !contentType.includes("application/json")) {
      logError("Tipo de contenido inválido:", contentType);
      throw new Error("La respuesta del servidor no es JSON válido");
    }

    if (response.status === 204) {
      log("No hay datos disponibles (204)");
      throw new Error("No hay datos disponibles en este momento");
    }

    if (!response.ok) {
      const errorData = await response.json();
      logError("Error en la respuesta:", errorData);
      throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    log("Datos recibidos:", data);
    
    if (!data) {
      logError("Respuesta vacía");
      throw new Error("La respuesta está vacía");
    }

    return data as Individuo;
  } catch (error) {
    logError("Error al obtener el mejor individuo:", error instanceof Error ? error.message : String(error));
    throw error;
  }
}
