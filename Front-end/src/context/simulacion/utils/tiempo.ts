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