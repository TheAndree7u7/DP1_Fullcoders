import { API_CONFIG } from "../config/api";
import type { EstadoSimulacionCompleto } from "../context/simulacion/utils/estado";

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
  estadoCompleto: EstadoSimulacionCompleto
): Promise<AveriaResponse> {
  console.log("🚛💥 AVERÍA: Enviando avería con estado completo para camión", codigoCamion);
  console.log("📊 AVERÍA: Estado completo incluido en la petición");
  console.log("📊 AVERÍA: Tamaño del estado (JSON):", JSON.stringify(estadoCompleto).length, "caracteres");

  // 🔍 AGREGADO: Logs detallados de todos los datos que se envían
  const datosEnvio = {
    codigoCamion,
    tipoIncidente: `TI${tipo}`,
    fechaHoraReporte,
    estadoSimulacion: estadoCompleto
  };

  console.log("📡 ===== DATOS COMPLETOS QUE SE ENVÍAN AL BACKEND =====");
  console.log("🏷️  DATOS BÁSICOS DE LA AVERÍA:");
  console.log("   - Código del camión:", datosEnvio.codigoCamion);
  console.log("   - Tipo de incidente:", datosEnvio.tipoIncidente);
  console.log("   - Fecha y hora del reporte:", datosEnvio.fechaHoraReporte);
  
  console.log("🔢 ESTADO DE LA SIMULACIÓN:");
  console.log("   - Timestamp:", estadoCompleto.timestamp);
  console.log("   - Hora actual:", estadoCompleto.horaActual);
  console.log("   - Hora simulación:", estadoCompleto.horaSimulacion);
  console.log("   - Fecha/hora simulación:", estadoCompleto.fechaHoraSimulacion);
  console.log("   - Fecha inicio simulación:", estadoCompleto.fechaInicioSimulacion);
  console.log("   - Día simulación:", estadoCompleto.diaSimulacion);
  console.log("   - Tiempo real simulación:", estadoCompleto.tiempoRealSimulacion);
  console.log("   - Tiempo transcurrido simulado:", estadoCompleto.tiempoTranscurridoSimulado);
  
  console.log("🚛 CAMIONES EN EL ESTADO:");
  console.log("   - Cantidad total de camiones:", estadoCompleto.camiones?.length || 0);
  if (estadoCompleto.camiones?.length && estadoCompleto.camiones.length > 0) {
    estadoCompleto.camiones.forEach((camion, index) => {
      console.log(`   📋 Camión ${index + 1}:`, {
        id: camion.id,
        estado: camion.estado,
        ubicacion: camion.ubicacion,
        porcentaje: camion.porcentaje,
        capacidadGLP: `${camion.capacidadActualGLP}/${camion.capacidadMaximaGLP}`,
        combustible: `${camion.combustibleActual}/${camion.combustibleMaximo}`,
        tipo: camion.tipo,
        velocidad: camion.velocidadPromedio
      });
    });
  }
  
  console.log("🗺️ RUTAS DE CAMIONES:");
  console.log("   - Cantidad total de rutas:", estadoCompleto.rutasCamiones?.length || 0);
  if (estadoCompleto.rutasCamiones?.length && estadoCompleto.rutasCamiones.length > 0) {
    estadoCompleto.rutasCamiones.forEach((ruta, index) => {
      console.log(`   📋 Ruta ${index + 1}:`, {
        id: ruta.id,
        puntoDestino: ruta.puntoDestino,
        cantidadPedidos: ruta.pedidos?.length || 0,
        pedidos: ruta.pedidos?.map((p) => ({
          codigo: p.codigo,
          coordenada: `(${p.coordenada?.x}, ${p.coordenada?.y})`,
          volumen: p.volumenGLPAsignado,
          estado: p.estado,
          fechaLimite: p.fechaLimite
        }))
      });
    });
  }
  
  console.log("🏪 ALMACENES EN EL ESTADO:");
  console.log("   - Cantidad total de almacenes:", estadoCompleto.almacenes?.length || 0);
  if (estadoCompleto.almacenes?.length && estadoCompleto.almacenes.length > 0) {
    estadoCompleto.almacenes.forEach((almacen, index) => {
      console.log(`   📋 Almacén ${index + 1}:`, {
        nombre: almacen.nombre,
        coordenada: `(${almacen.coordenada?.x}, ${almacen.coordenada?.y})`,
        capacidadGLP: `${almacen.capacidadActualGLP}/${almacen.capacidadMaximaGLP}`,
        tipo: almacen.tipo,
        activo: almacen.activo,
        esCentral: almacen.esCentral
      });
    });
  }
  
  console.log("🚧 BLOQUEOS EN EL ESTADO:");
  console.log("   - Cantidad total de bloqueos:", estadoCompleto.bloqueos?.length || 0);
  if (estadoCompleto.bloqueos?.length && estadoCompleto.bloqueos.length > 0) {
    estadoCompleto.bloqueos.forEach((bloqueo, index) => {
      console.log(`   📋 Bloqueo ${index + 1}:`, {
        coordenadas: bloqueo.coordenadas,
        fechaInicio: bloqueo.fechaInicio,
        fechaFin: bloqueo.fechaFin
      });
    });
  }
  
  console.log("📝 DATOS COMPLETOS EN JSON (para copiar/pegar):");
  console.log(JSON.stringify(datosEnvio, null, 2));
  
  console.log("📊 ESTADÍSTICAS DEL ENVÍO:");
  console.log("   - Tamaño total del JSON:", JSON.stringify(datosEnvio).length, "caracteres");
  console.log("   - Tamaño del estado:", JSON.stringify(estadoCompleto).length, "caracteres");
  console.log("   - URL destino:", `${API_CONFIG.BASE_URL}/averias/averiar-camion-con-estado`);
  console.log("🔚 ===== FIN DE DATOS DE ENVÍO =====");

  const response = await fetch(`${API_CONFIG.BASE_URL}/averias/averiar-camion-con-estado`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(datosEnvio)
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("❌ AVERÍA: Error al averiar camión con estado:", errorText);
    throw new Error(`No se pudo averiar el camión: ${errorText}`);
  }

  const result = await response.json();
  console.log("✅ AVERÍA: Camión averiado exitosamente con estado capturado");
  console.log("📥 RESPUESTA DEL BACKEND:", result);
  return result;
}
