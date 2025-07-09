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

// Servicio original: POST /api/averias/averiar-camion
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

// Nuevo servicio mejorado: POST /api/averias/averiar-camion-con-estado
export async function averiarCamionConEstado(
  codigoCamion: string,
  tipo: number,
  fechaHoraReporte: string,
  estadoCompleto: object
): Promise<AveriaResponse> {
  console.log("🚛💥 AVERÍA: Enviando avería con estado completo para camión", codigoCamion);
  console.log("📊 AVERÍA: Estado completo incluido en la petición");
  console.log("📊 AVERÍA: Tamaño del estado (JSON):", JSON.stringify(estadoCompleto).length, "caracteres");

  const response = await fetch(`${API_CONFIG.BASE_URL}/averias/averiar-camion-con-estado`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      codigoCamion,
      tipoIncidente: `TI${tipo}`,
      fechaHoraReporte,
      estadoSimulacion: estadoCompleto
    })
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("❌ AVERÍA: Error al averiar camión con estado:", errorText);
    throw new Error(`No se pudo averiar el camión: ${errorText}`);
  }

  const result = await response.json();
  console.log("✅ AVERÍA: Camión averiado exitosamente con estado capturado");
  return result;
}
