import type { CamionEstado } from "../types";
const API_BASE_URL = "http://localhost:8085/api/simulacion";

export async function averiarCamion(camionId: string): Promise<CamionEstado> {
  const response = await fetch(`${API_BASE_URL}/averiar-camion/${camionId}`, {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("No se pudo averiar el camión");
  }
  return response.json();
}
