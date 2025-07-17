import { iniciarSimulacionPost } from './simulacionApiService';

/**
 * Ejemplo de uso de la función iniciarSimulacionPost
 * Esta función usa el método POST para iniciar la simulación
 */

export async function ejemploIniciarSimulacion() {
  try {
    // Formato de fecha ISO: YYYY-MM-DDTHH:MM:SS
    const fechaInicio = "2025-01-01T00:00:00";
    
    console.log("🚀 Iniciando simulación con fecha:", fechaInicio);
    
    const resultado = await iniciarSimulacionPost(fechaInicio);
    
    console.log("✅ Simulación iniciada exitosamente:", resultado);
    
    return resultado;
  } catch (error) {
    console.error("❌ Error al iniciar simulación:", error);
    throw error;
  }
}

/**
 * Ejemplo de uso con fecha actual
 */
export async function iniciarSimulacionConFechaActual() {
  try {
    // Obtener fecha y hora actual en formato ISO
    const fechaActual = new Date().toISOString().slice(0, 19); // Formato: YYYY-MM-DDTHH:MM:SS
    
    console.log("🚀 Iniciando simulación con fecha actual:", fechaActual);
    
    const resultado = await iniciarSimulacionPost(fechaActual);
    
    console.log("✅ Simulación iniciada exitosamente:", resultado);
    
    return resultado;
  } catch (error) {
    console.error("❌ Error al iniciar simulación:", error);
    throw error;
  }
}

/**
 * Ejemplo de uso con fecha específica
 */
export async function iniciarSimulacionConFechaEspecifica(anio: number, mes: number, dia: number, hora: number = 0, minuto: number = 0) {
  try {
    // Formatear fecha en formato ISO
    const fecha = `${anio.toString().padStart(4, '0')}-${mes.toString().padStart(2, '0')}-${dia.toString().padStart(2, '0')}T${hora.toString().padStart(2, '0')}:${minuto.toString().padStart(2, '0')}:00`;
    
    console.log("🚀 Iniciando simulación con fecha específica:", fecha);
    
    const resultado = await iniciarSimulacionPost(fecha);
    
    console.log("✅ Simulación iniciada exitosamente:", resultado);
    
    return resultado;
  } catch (error) {
    console.error("❌ Error al iniciar simulación:", error);
    throw error;
  }
} 