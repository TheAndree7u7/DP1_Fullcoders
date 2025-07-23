import React, { useState, useEffect } from 'react';
import { useSimulacion } from '../context/SimulacionContext';
// import { formatearTiempoTranscurridoCompleto } from '../context/simulacion/utils/tiempo';
import { useCurrentDateTime } from '../hooks/useCurrentDateTime';
import { SEGUNDOS_POR_NODO, NODOS_PARA_ACTUALIZACION } from '../context/simulacion/types';

// Estilos CSS en línea
const styles = {
  fechaContainer: {
    display: 'flex',
    flexDirection: 'column' as const,
    backgroundColor: '#1a1a2e',
    borderRadius: '8px',
    padding: '12px 16px',
    color: 'white',
    fontFamily: 'Arial, sans-serif',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
    marginBottom: '16px',
  },
  titulo: {
    fontSize: '14px',
    fontWeight: 'bold' as const,
    marginBottom: '8px',
    display: 'flex',
    alignItems: 'center',
  },
  contenido: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '4px',
  },
  fechaCompleta: {
    fontSize: '16px',
    fontWeight: 'bold' as const,
  },
  infoAdicional: {
    fontSize: '14px',
    color: '#bbbbbb',
    display: 'flex',
    justifyContent: 'space-between',
  },
  destacado: {
    color: '#4cc9f0',
    fontWeight: 'bold' as const,
  },
  cargando: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '12px',
    color: '#999999',
  },
  iconoReloj: {
    marginRight: '6px',
  },
  etiqueta: {
    color: '#bbbbbb',
  }
};

/**
 * Función para calcular la hora exacta de simulación basada en los intervalos reales
 */
const calcularHoraExactaSimulacion = (
  fechaInicioIntervalo: string,
  fechaFinIntervalo: string,
  horaActual: number
): Date => {
  const fechaInicio = new Date(fechaInicioIntervalo);
  const fechaFin = new Date(fechaFinIntervalo);
  
  // Calcular la duración total del intervalo en milisegundos
  const duracionTotal = fechaFin.getTime() - fechaInicio.getTime();
  //
  // Calcular el progreso actual basado en el nodo actual
  // Usando la constante unificada de nodos por intervalo
  const NODOS_POR_INTERVALO = NODOS_PARA_ACTUALIZACION;
  
  // Calcular el progreso dentro del intervalo actual (0-1)
  const progresoEnIntervalo = Math.min(horaActual / NODOS_POR_INTERVALO, 1);
  const tiempoTranscurrido = duracionTotal * progresoEnIntervalo;
  
  // Calcular la fecha exacta sumando el tiempo transcurrido a la fecha de inicio del intervalo
  const fechaExacta = new Date(fechaInicio.getTime() + tiempoTranscurrido);
  
  console.log("🕒 CÁLCULO HORA EXACTA:", {
    fechaInicioIntervalo,
    fechaFinIntervalo,
    duracionTotal: duracionTotal / (1000 * 60 * 60), // en horas
    horaActual,
    progresoEnIntervalo,
    tiempoTranscurrido: tiempoTranscurrido / (1000 * 60 * 60), // en horas
    fechaExacta: fechaExacta.toISOString()
  });
  
  return fechaExacta;
};

const getColorPorcentaje = (porcentaje: number) => {
  if (porcentaje >= 0.7) return '#22c55e'; // verde
  if (porcentaje >= 0.4) return '#eab308'; // amarillo
  return '#f97316'; // naranja
};

/**
 * Componente que muestra la fecha y hora actual de la simulación
 */
const FechaHoraSimulacion: React.FC = () => {
  const {
    fechaHoraSimulacion,
    fechaInicioSimulacion,
    horaActual,
    cargando,
    fechaHoraInicioIntervalo,
    fechaHoraFinIntervalo,
    camiones,
    rutasCamiones
  } = useSimulacion();
  const [tiempoSimulado, setTiempoSimulado] = useState<Date | null>(null);
  const currentDateTime = useCurrentDateTime();

  // Actualizar la hora simulada cuando cambia fechaHoraSimulacion (datos del backend)
  useEffect(() => {
    if (fechaHoraSimulacion && fechaInicioSimulacion) {
      // Extraer solo la fecha sin hora para mostrar la fecha de simulación limpia
      const fechaBase = new Date(fechaHoraSimulacion);
      const fechaSolo = fechaBase.toISOString().split('T')[0]; // Solo YYYY-MM-DD
      
      // Crear una nueva fecha con solo la fecha (hora 00:00:00)
      const fechaLimpia = new Date(fechaSolo + 'T00:00:00.000Z');
      setTiempoSimulado(fechaLimpia);
    }
  }, [fechaHoraSimulacion, fechaInicioSimulacion]);
  
  // Eliminado cálculo de tiempoFormateado porque ya no se usa
  useEffect(() => {
    // Eliminado cálculo de tiempoFormateado porque ya no se usa
  }, [fechaHoraSimulacion, fechaInicioSimulacion]);

  // Calcular capacidad total y disponible
  const capacidadTotal = camiones.reduce((acc, c) => acc + (c.capacidadMaximaGLP || 0), 0);
  const capacidadDisponible = camiones.reduce((acc, c) => acc + (c.capacidadActualGLP || 0), 0);
  const porcentajeDisponible = capacidadTotal > 0 ? capacidadDisponible / capacidadTotal : 0;

  // Calcular GLP en uso (suma de pedidos asignados a camiones / capacidad total)
  const pedidosAsignados = rutasCamiones.flatMap(r => r.pedidos);
  const glpEnUso = pedidosAsignados.reduce((acc, p) => acc + (p.volumenGLPAsignado || 0), 0);
  const porcentajeGLPEnUso = capacidadTotal > 0 ? glpEnUso / capacidadTotal : 0;

  if (cargando) {
    return (
      <div style={styles.fechaContainer}>
        <div style={styles.cargando}>
          <span>Cargando datos de simulación...</span>
        </div>
      </div>
    );
  }

  if (!fechaHoraSimulacion) {
    return (
      <div style={styles.fechaContainer}>
        <div style={styles.cargando}>
          <span>Esperando datos de fecha y hora...</span>
        </div>
      </div>
    );
  }

  // Formatear fecha simulada para mostrar (solo fecha, sin hora)
  const fechaSimulada = tiempoSimulado ? 
    tiempoSimulado.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'numeric',
      year: 'numeric'
    }) : '';
  
  // Calcular hora exacta de simulación usando los intervalos reales
  let horaExacta = "00:00:00";
  if (fechaHoraInicioIntervalo && fechaHoraFinIntervalo && horaActual > 0) {
    try {
      const fechaExacta = calcularHoraExactaSimulacion(
        fechaHoraInicioIntervalo,
        fechaHoraFinIntervalo,
        horaActual
      );
      
      horaExacta = fechaExacta.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    } catch (error) {
      console.warn("Error al calcular hora exacta con intervalos:", error);
      // Fallback al cálculo anterior si no hay intervalos
      if (fechaHoraSimulacion) {
        const fechaBase = new Date(fechaHoraSimulacion);
        const segundosAdicionales = horaActual * SEGUNDOS_POR_NODO;
        const nuevaFecha = new Date(fechaBase.getTime() + segundosAdicionales * 1000);
        horaExacta = nuevaFecha.toLocaleTimeString('es-ES', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
      }
    }
  }
  
  const diaSimulado = tiempoSimulado ? tiempoSimulado.getDate() : '';

  return (
    <div style={styles.fechaContainer}>
      {/* Apartado de capacidad GLP */}
      <div style={{ display: 'flex', gap: 16, marginBottom: 12, flexWrap: 'wrap' }}>
        <div style={{
          background: getColorPorcentaje(porcentajeDisponible),
          color: 'white',
          borderRadius: 8,
          padding: '8px 16px',
          fontWeight: 'bold',
          minWidth: 180,
          textAlign: 'center',
        }}>
          Capacidad GLP disponible:<br />
          {capacidadDisponible.toFixed(2)} / {capacidadTotal.toFixed(2)} m³
        </div>
        <div style={{
          background: getColorPorcentaje(porcentajeGLPEnUso),
          color: 'white',
          borderRadius: 8,
          padding: '8px 16px',
          fontWeight: 'bold',
          minWidth: 180,
          textAlign: 'center',
        }}>
          GLP en uso (pedidos asignados):<br />
          {glpEnUso.toFixed(2)} / {capacidadTotal.toFixed(2)} m³
        </div>
      </div>
      <div style={styles.titulo}>
        <span style={styles.iconoReloj}>🕒</span>
        <span>TIEMPO DE SIMULACIÓN</span>
      </div>
      <div style={styles.contenido}>
        <div style={styles.fechaCompleta}>{fechaSimulada}</div>
        <div style={styles.infoAdicional}>
          <div>
            <span style={styles.etiqueta}>Día: </span>
            <span style={styles.destacado}>{diaSimulado}</span>
          </div>
          <div>
            <span style={styles.etiqueta}>Hora de la simulacion: </span>
            <span style={styles.destacado}>{horaExacta}</span>
          </div>
        </div>
        <div style={styles.infoAdicional}>
          <div>
            <span style={styles.etiqueta}>Hora y fecha Actual: </span>
            <span style={styles.destacado}>{currentDateTime.toLocaleString('es-ES', {
              day: '2-digit',
              month: '2-digit',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            })}</span>
          </div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <span style={{
              background: getColorPorcentaje(porcentajeGLPEnUso),
              color: 'white',
              borderRadius: 8,
              padding: '4px 10px',
              fontWeight: 'bold',
              minWidth: 80,
              textAlign: 'center',
              fontSize: 13
            }}>
              Estado GLP USO: {(porcentajeGLPEnUso * 100).toFixed(0)}%
            </span>
            <span style={{
              background: getColorPorcentaje(porcentajeDisponible),
              color: 'white',
              borderRadius: 8,
              padding: '4px 10px',
              fontWeight: 'bold',
              minWidth: 80,
              textAlign: 'center',
              fontSize: 13
            }}>
              Estado GLP Disponible: {(porcentajeDisponible * 100).toFixed(0)}%
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FechaHoraSimulacion;
