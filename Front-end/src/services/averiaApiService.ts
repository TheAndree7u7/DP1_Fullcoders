 
import { API_CONFIG } from "../config/api";

// Tipo para la respuesta de avería (puedes ajustarlo según lo que devuelva el backend)
export interface AveriaResponse {
  id: number;
  codigoCamion: string;
  tipoIncidente: string;
  fechaHoraOcurrencia: string;
  fechaHoraDisponible: string;
  tiempoReparacionEstimado: number;
  turnoOcurrencia?: number;
  // ...otros campos posibles
}

// Nuevo servicio: POST /api/averias/averiar-camion
export async function averiarCamionTipo(
  codigoCamion: string,
  tipo: number,
  fechaHoraReporte: string
): Promise<AveriaResponse> {
  const response = await fetch(`${API_CONFIG.BASE_URL}/averias/averiar-camion`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      codigoCamion,
      tipoIncidente: `TI${tipo}`,
      fechaHoraReporte
    })
  });
  if (!response.ok) {
    throw new Error("No se pudo averiar el camión");
  }
  return response.json();
}
