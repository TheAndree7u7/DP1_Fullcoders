/**
 * @file debugAveriasAutomaticas.ts
 * @description Utilidades para diagnosticar averías automáticas
 */

import { AVERIAS_AUTOMATICAS_CONFIG } from '../config/constants';
import type { CamionEstado, RutaCamion } from '../context/simulacion/types';
import type { EstadoAveriasAutomaticas } from '../context/simulacion/autoAverias';

/**
 * @function diagnosticarEstadoAveriasAutomaticas
 * @description Diagnostica el estado actual de las averías automáticas
 */
export const diagnosticarEstadoAveriasAutomaticas = (
  estadoAverias: EstadoAveriasAutomaticas | null | undefined,
  paqueteActual: number,
  camiones: CamionEstado[],
  rutasCamiones: RutaCamion[],
  fechaHoraInicioIntervalo: string | null,
  fechaHoraFinIntervalo: string | null,
  simulacionActiva: boolean,
  running: boolean
) => {
  console.log('🔍 ===== DIAGNÓSTICO AVERÍAS AUTOMÁTICAS =====');
  
  // 1. Verificar configuración
  console.log('📋 CONFIGURACIÓN:');
  console.log(`  - ACTIVADO: ${AVERIAS_AUTOMATICAS_CONFIG.ACTIVADO}`);
  console.log(`  - PAQUETES_PARA_AVERIA: ${AVERIAS_AUTOMATICAS_CONFIG.PAQUETES_PARA_AVERIA}`);
  console.log(`  - PRIORIDAD_CAPACIDAD_MINIMA: ${AVERIAS_AUTOMATICAS_CONFIG.PRIORIDAD_CAPACIDAD_MINIMA}`);
  
  // 2. Verificar estado de averías
  console.log('📊 ESTADO DE AVERÍAS:');
  console.log(`  - contadorPaquetes: ${estadoAverias?.contadorPaquetes || 'undefined'}`);
  console.log(`  - ultimoPaqueteConAveria: ${estadoAverias?.ultimoPaqueteConAveria || 'undefined'}`);
  console.log(`  - averiasRealizadas: ${JSON.stringify(estadoAverias?.averiasRealizadas || [])}`);
  
  // 3. Verificar estado de simulación
  console.log('🎮 ESTADO DE SIMULACIÓN:');
  console.log(`  - simulacionActiva: ${simulacionActiva}`);
  console.log(`  - running: ${running}`);
  console.log(`  - paqueteActual: ${paqueteActual}`);
  
  // 4. Verificar datos disponibles
  console.log('📦 DATOS DISPONIBLES:');
  console.log(`  - camiones.length: ${camiones?.length || 0}`);
  console.log(`  - rutasCamiones.length: ${rutasCamiones?.length || 0}`);
  console.log(`  - fechaHoraInicioIntervalo: ${fechaHoraInicioIntervalo || 'null'}`);
  console.log(`  - fechaHoraFinIntervalo: ${fechaHoraFinIntervalo || 'null'}`);
  
  // 5. Verificar camiones en ruta
  if (camiones && camiones.length > 0) {
    console.log('🚛 CAMIONES EN RUTA:');
    camiones.forEach(camion => {
      console.log(`  - ${camion.id}: estado=${camion.estado}, capacidad=${camion.capacidadActualGLP}, ubicacion=${camion.ubicacion}`);
    });
  }
  
  // 6. Verificar rutas
  if (rutasCamiones && rutasCamiones.length > 0) {
    console.log('🗺️ RUTAS DISPONIBLES:');
    rutasCamiones.forEach(ruta => {
      console.log(`  - ${ruta.id}: ${ruta.ruta.length} nodos, ${ruta.pedidos.length} pedidos`);
    });
  }
  
  // 7. Análisis de problemas potenciales
  console.log('⚠️ ANÁLISIS DE PROBLEMAS:');
  
  if (!AVERIAS_AUTOMATICAS_CONFIG.ACTIVADO) {
    console.log('  ❌ PROBLEMA: Averías automáticas desactivadas en configuración');
  }
  
  if (!simulacionActiva) {
    console.log('  ❌ PROBLEMA: Simulación no está activa');
  }
  
  if (!running) {
    console.log('  ❌ PROBLEMA: Running no está activo');
  }
  
  if (!camiones || camiones.length === 0) {
    console.log('  ❌ PROBLEMA: No hay camiones disponibles');
  }
  
  if (!rutasCamiones || rutasCamiones.length === 0) {
    console.log('  ❌ PROBLEMA: No hay rutas disponibles');
  }
  
  if (!fechaHoraInicioIntervalo || !fechaHoraFinIntervalo) {
    console.log('  ❌ PROBLEMA: Fechas de intervalo no disponibles');
  }
  
  const camionesEnRuta = camiones?.filter(c => c.estado === 'En Ruta') || [];
  if (camionesEnRuta.length === 0) {
    console.log('  ❌ PROBLEMA: No hay camiones en estado "En Ruta"');
  }
  
  console.log('🔍 ===== FIN DIAGNÓSTICO =====');
};

/**
 * @function verificarContadorPaquetes
 * @description Verifica si el contador de paquetes está funcionando correctamente
 */
export const verificarContadorPaquetes = (
  estadoAverias: EstadoAveriasAutomaticas | null | undefined,
  paqueteActual: number
) => {
  console.log('🔢 VERIFICACIÓN CONTADOR PAQUETES:');
  console.log(`  - contadorPaquetes actual: ${estadoAverias?.contadorPaquetes || 0}`);
  console.log(`  - paqueteActual: ${paqueteActual}`);
  console.log(`  - PAQUETES_PARA_AVERIA: ${AVERIAS_AUTOMATICAS_CONFIG.PAQUETES_PARA_AVERIA}`);
  
  if (estadoAverias?.contadorPaquetes) {
    const modulo = estadoAverias.contadorPaquetes % AVERIAS_AUTOMATICAS_CONFIG.PAQUETES_PARA_AVERIA;
    console.log(`  - contadorPaquetes % PAQUETES_PARA_AVERIA = ${modulo}`);
    console.log(`  - Debería ocurrir avería: ${modulo === 0 ? 'SÍ' : 'NO'}`);
  }
}; 