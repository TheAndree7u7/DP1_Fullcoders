import React from 'react';
import { useSimulacion } from '../context/SimulacionContext';

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
  const { fechaHoraSimulacion, diaSimulacion, horaActual, cargando } = useSimulacion();

  // Formatear la fecha para mostrar
  const formatearFecha = (fechaStr: string) => {
    const fecha = new Date(fechaStr);
    return {
      completa: fecha.toLocaleDateString('es-ES', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      }) + ' ' + fecha.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }),
      hora: fecha.getHours() + ':' + fecha.getMinutes().toString().padStart(2, '0'),
      dia: fecha.getDate()
    };
  };

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

  const { completa, hora, dia } = formatearFecha(fechaHoraSimulacion);

  return (
    <div style={styles.fechaContainer}>
      <div style={styles.titulo}>
        <span style={styles.iconoReloj}>🕒</span>
        <span>TIEMPO DE SIMULACIÓN</span>
      </div>
      <div style={styles.contenido}>
        <div style={styles.fechaCompleta}>{completa}</div>
        <div style={styles.infoAdicional}>
          <div>
            <span style={styles.etiqueta}>Día: </span>
            <span style={styles.destacado}>{dia}</span>
          </div>
          <div>
            <span style={styles.etiqueta}>Hora de ejecución: </span>
            <span style={styles.destacado}>{horaActual}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FechaHoraSimulacion;
