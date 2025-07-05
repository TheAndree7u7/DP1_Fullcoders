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

// Nuevo servicio con recálculo dinámico: POST /api/averias/averiar-camion-dinamico
export async function averiarCamionDinamico(
  codigoCamion: string,
  tipo: number,
  fechaHoraReporte: string,
  coordenada?: { fila: number; columna: number }
): Promise<AveriaResponse> {
  const body: {
    codigoCamion: string;
    tipoIncidente: string;
    fechaHoraReporte: string;
    coordenada?: { fila: number; columna: number };
  } = {
    codigoCamion,
    tipoIncidente: `TI${tipo}`,
    fechaHoraReporte
  };
  
  // Agregar coordenada si se proporciona
  if (coordenada) {
    body.coordenada = coordenada;
  }
  
  const response = await fetch(`${API_CONFIG.BASE_URL}/averias/averiar-camion-dinamico`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(body)
  });
  
  if (!response.ok) {
    throw new Error("No se pudo averiar el camión con recálculo dinámico");
  }
  
  return response.json();
}
