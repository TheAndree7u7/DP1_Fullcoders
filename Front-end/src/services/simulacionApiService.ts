import type { Individuo } from "../types";
const API_BASE_URL = "http://localhost:8085/api/simulacion";

export async function getMejorIndividuo(): Promise<Individuo> {
  try {
    console.log("Iniciando solicitud al servidor...");
    const response = await fetch(`${API_BASE_URL}/mejor`);
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

export async function getEstadoRapido(): Promise<{ status: string; message: string; timestamp: number }> {
  try {
    console.log("🚀 Solicitando estado rápido...");
    const response = await fetch(`${API_BASE_URL}/estado-rapido`);
    
    if (!response.ok) {
      throw new Error(`Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    console.log("⚡ Estado rápido recibido:", data);
    return data;
  } catch (error) {
    console.error("❌ Error al obtener estado rápido:", error);
    throw error;
  }
}

// Función híbrida que decide qué endpoint usar
export async function getDataOptimizada(necesitaActualizacionCompleta: boolean): Promise<Individuo | { status: string }> {
  if (necesitaActualizacionCompleta) {
    console.log("🔄 Solicitando actualización completa del algoritmo genético...");
    return await getMejorIndividuo();
  } else {
    console.log("⚡ Usando interpolación local...");
    return await getEstadoRapido();
  }
}
