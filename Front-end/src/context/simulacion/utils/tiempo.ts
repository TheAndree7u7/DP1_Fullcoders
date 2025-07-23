/**
 * @file tiempo.ts
 * @description Utilidades para el manejo del tiempo en la simulación
 */

/**
 * @function formatearTiempoTranscurrido
 * @description Convierte tiempo en formato HH:MM:SS a formato legible como "transcurrieron X días Y horas Z minutos"
 * @param {string} tiempoHMS - Tiempo en formato HH:MM:SS
 * @returns {string} Tiempo formateado de manera legible
 */
export const formatearTiempoTranscurrido = (tiempoHMS: string): string => {
  if (!tiempoHMS || tiempoHMS === "00:00:00") {
    return "No hay tiempo transcurrido";
  }

  const partes = tiempoHMS.split(":");
  const horas = parseInt(partes[0]);
  const minutos = parseInt(partes[1]);
  const segundos = parseInt(partes[2]);

  const totalSegundos = horas * 3600 + minutos * 60 + segundos;
  const dias = Math.floor(totalSegundos / 86400);
  const horasRestantes = Math.floor((totalSegundos % 86400) / 3600);
  const minutosRestantes = Math.floor((totalSegundos % 3600) / 60);

  const resultado = "Transcurrieron ";
  const partes_resultado = [];

  if (dias > 0) {
    partes_resultado.push(`${dias} día${dias > 1 ? 's' : ''}`);
  }
  if (horasRestantes > 0) {
    partes_resultado.push(`${horasRestantes} hora${horasRestantes > 1 ? 's' : ''}`);
  }
  if (minutosRestantes > 0) {
    partes_resultado.push(`${minutosRestantes} minuto${minutosRestantes > 1 ? 's' : ''}`);
  }

  if (partes_resultado.length === 0) {
    return "Transcurrieron menos de un minuto";
  }

  return resultado + partes_resultado.join(' y ');
};

/**
 * @function formatearTiempoTranscurridoCompleto
 * @description Calcula y formatea el tiempo transcurrido entre dos fechas considerando días completos
 * @param {string} fechaActual - Fecha actual de la simulación
 * @param {string} fechaInicio - Fecha de inicio de la simulación
 * @returns {string} Tiempo formateado de manera legible
 */
export const formatearTiempoTranscurridoCompleto = (
  fechaActual: string,
  fechaInicio: string
): string => {
  try {
    const fechaActualDate = new Date(fechaActual);
    const fechaInicioDate = new Date(fechaInicio);
    
    const diferenciaMilisegundos = fechaActualDate.getTime() - fechaInicioDate.getTime();
    
    if (diferenciaMilisegundos < 0) {
      return "Tiempo no válido";
    }
    
    const totalSegundos = Math.floor(diferenciaMilisegundos / 1000);
    const dias = Math.floor(totalSegundos / 86400);
    const horas = Math.floor((totalSegundos % 86400) / 3600);
    const minutos = Math.floor((totalSegundos % 3600) / 60);
    
    const partes = [];
    
    if (dias > 0) {
      partes.push(`${dias} día${dias > 1 ? 's' : ''}`);
    }
    if (horas > 0) {
      partes.push(`${horas} hora${horas > 1 ? 's' : ''}`);
    }
    if (minutos > 0) {
      partes.push(`${minutos} minuto${minutos > 1 ? 's' : ''}`);
    }
    
    if (partes.length === 0) {
      return "Menos de un minuto";
    }
    
    return `Transcurrieron ${partes.join(' y ')}`;
  } catch (error) {
    console.warn("Error al formatear tiempo transcurrido completo:", error);
    return "Error en cálculo de tiempo";
  }
};

/**
 * @function formatearFechaParaBackend
 * @description Convierte una fecha a formato LocalDateTime compatible con el backend (sin zona horaria)
 * @param {Date | string} fecha - Fecha a formatear
 * @returns {string} Fecha en formato YYYY-MM-DDTHH:mm:ss
 */
export const formatearFechaParaBackend = (fecha: Date | string): string => {
  if (typeof fecha === 'string') {
    // Si ya es una fecha en formato LocalDateTime, devolverla tal como está
    if (fecha.match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/)) {
      return fecha;
    }
    // Si es una fecha ISO con zona horaria, convertirla
    const date = new Date(fecha);
    return formatearFechaLocal(date);
  } else {
    // Si es un objeto Date, convertir a LocalDateTime
    return formatearFechaLocal(fecha);
  }
};

/**
 * @function formatearFechaLocal
 * @description Convierte una fecha a formato LocalDateTime sin zona horaria
 * @param {Date} fecha - Fecha a formatear
 * @returns {string} Fecha en formato YYYY-MM-DDTHH:mm:ss
 */
const formatearFechaLocal = (fecha: Date): string => {
  const año = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  const hora = String(fecha.getHours()).padStart(2, '0');
  const minuto = String(fecha.getMinutes()).padStart(2, '0');
  const segundo = String(fecha.getSeconds()).padStart(2, '0');
  
  return `${año}-${mes}-${dia}T${hora}:${minuto}:${segundo}`;
};

/**
 * @function calcularTimestampSimulacion
 * @description Calcula el timestamp correcto de simulación usando la fecha base con hora 00:00:00 y la horaSimulacion calculada
 * @param {string | null} fechaHoraSimulacion - Fecha y hora base de la simulación del backend
 * @param {string} horaSimulacion - Hora actual de la simulación ya calculada (formato HH:MM:SS)
 * @returns {string} Timestamp en formato LocalDateTime compatible con el backend (sin zona horaria)
 */
export const calcularTimestampSimulacion = (
  fechaHoraSimulacion: string | null,
  horaSimulacion: string
): string => {
  // Si no hay fecha de simulación, devolver timestamp actual como fallback
  if (!fechaHoraSimulacion) {
    console.warn("⚠️ No hay fechaHoraSimulacion disponible, usando timestamp actual como fallback");
    return formatearFechaParaBackend(new Date());
  }

  // Extraer solo la fecha (sin hora) de fechaHoraSimulacion de manera más directa
  const fechaOriginal = fechaHoraSimulacion.split('T')[0]; // Obtener solo la parte de fecha "2025-01-01"
  
  // Parsear la horaSimulacion (formato HH:MM:SS)
  const partesHora = horaSimulacion.split(':');
  const horas = parseInt(partesHora[0]);
  const minutos = parseInt(partesHora[1]);
  const segundos = parseInt(partesHora[2]);
  
  // Crear el timestamp completo combinando fecha + hora de simulación
  // Formato LocalDateTime: YYYY-MM-DDTHH:mm:ss (sin zona horaria)
  const fechaSimulacionCompleta = `${fechaOriginal}T${String(horas).padStart(2, '0')}:${String(minutos).padStart(2, '0')}:${String(segundos).padStart(2, '0')}`;
  
  // console.log("📅 TIMESTAMP SIMULACIÓN:", {
  //   fechaHoraSimulacionOriginal: fechaHoraSimulacion,
  //   fechaExtraida: fechaOriginal,
  //   horaSimulacion: horaSimulacion,
  //   fechaFinal: fechaSimulacionCompleta
  // });
  
  return fechaSimulacionCompleta;
}; 