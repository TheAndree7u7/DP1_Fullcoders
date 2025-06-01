import type { Individuo } from "../models/Individuo";
const API_BASE_URL = "http://localhost:8085/api/simulacion";

export async function getMejorIndividuo(): Promise<Individuo> {
  try {
    const response = await fetch(`${API_BASE_URL}/mejor`);
    if (!response.ok) {
      let errorMessage = `Error ${response.status}: ${response.statusText}`;
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch (e) {
        console.error("Error al parsear el cuerpo de la respuesta:", e);
      }
      throw new Error(errorMessage);
    }
    const data: Individuo = await response.json();
    return data;
  } catch (error) {
    console.error("Error al obtener el mejor individuo:", error);
    throw error;
  }
}
