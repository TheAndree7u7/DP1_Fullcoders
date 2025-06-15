import type { CamionEstado } from "../types";
import { API_URLS } from "../config/api";

export async function averiarCamion(camionId: string): Promise<CamionEstado> {
  const response = await fetch(`${API_URLS.SIMULACION_BASE}/averiar-camion/${camionId}`, {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("No se pudo averiar el camión");
  }
  return response.json();
}

export async function averiarCamionTipo(camionId: string, tipo: number): Promise<CamionEstado> {
  const response = await fetch(`${API_URLS.SIMULACION_BASE}/averiar-camion/${camionId}?tipo=${tipo}`, {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("No se pudo averiar el camión");
  }
  return response.json();
}
