import React, { useState, useEffect } from 'react';
import { useSimulacion } from '../context/SimulacionContext';

// Constante que define cuánto tiempo (en segundos) representa cada nodo en la simulación
const SEGUNDOS_POR_NODO = 36;

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
 * Componente que muestra la fecha y hora actual de la simulación
 */
const FechaHoraSimulacion: React.FC = () => {
  const { fechaHoraSimulacion, horaActual, cargando } = useSimulacion();
  const [tiempoSimulado, setTiempoSimulado] = useState<Date | null>(null);
  
  // Esta función ya no se usa, pero la mantenemos por si necesitamos formatear fechas en el futuro
  
  // Actualizar la hora simulada cuando cambia fechaHoraSimulacion (datos del backend)
  useEffect(() => {
    if (fechaHoraSimulacion) {
      // Cuando recibimos una nueva fecha del backend, actualizamos nuestra referencia
      const fechaBase = new Date(fechaHoraSimulacion);
      setTiempoSimulado(fechaBase);
    }
  }, [fechaHoraSimulacion]);
  
  // Actualizar la hora simulada cuando avanza la simulación (horaActual)
  useEffect(() => {
    if (fechaHoraSimulacion && horaActual > 0) {
      // Calculamos el tiempo según el nodo actual (siguiendo el avance de la simulación)
      const fechaBase = new Date(fechaHoraSimulacion);
      // Calculamos segundos adicionales solo para el incremento local desde la última actualización del backend
      const segundosAdicionales = horaActual * SEGUNDOS_POR_NODO;
      
      // Crea nueva fecha sumando los segundos
      const nuevaFecha = new Date(fechaBase.getTime() + segundosAdicionales * 1000);
      setTiempoSimulado(nuevaFecha);
    }
  }, [horaActual, fechaHoraSimulacion]);

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

  // Formatear fecha simulada para mostrar
  const fechaSimulada = tiempoSimulado ? 
    tiempoSimulado.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'numeric',
      year: 'numeric'
    }) + ' ' + 
    tiempoSimulado.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }) : '';
  
  // Formatos más específicos para elementos individuales
  const horaSimulada = tiempoSimulado ? 
    tiempoSimulado.getHours().toString().padStart(2, '0') + ':' + 
    tiempoSimulado.getMinutes().toString().padStart(2, '0') + ':' + 
    tiempoSimulado.getSeconds().toString().padStart(2, '0') : '';
  
  const diaSimulado = tiempoSimulado ? tiempoSimulado.getDate() : '';

  return (
    <div style={styles.fechaContainer}>
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
            <span style={styles.etiqueta}>Hora en tiempo real: </span>
            <span style={styles.destacado}>{horaSimulada}</span>
          </div>
        </div>
        <div style={styles.infoAdicional}>
          <div>
            <span style={styles.etiqueta}>Nodo actual: </span>
            <span style={styles.destacado}>{horaActual}</span>
          </div>
          <div>
            <span style={styles.etiqueta}>Segundos por nodo: </span>
            <span style={styles.destacado}>{SEGUNDOS_POR_NODO}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FechaHoraSimulacion;
