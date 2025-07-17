import React from 'react';
import { useSimulacion } from '../context/SimulacionContext';

/**
 * @component IndicadorPaqueteActual
 * @description Muestra información del paquete que se está consumiendo actualmente en el mapa
 */
const IndicadorPaqueteActual: React.FC = () => {
  const { 
    fechaHoraInicioIntervalo, 
    fechaHoraFinIntervalo, 
    paqueteActualConsumido,
    obtenerInfoPaqueteActual 
  } = useSimulacion();

  // Obtener información del paquete actual usando la función helper
  const infoPaquete = obtenerInfoPaqueteActual();

  // Formatear fechas para mostrar
  const formatearFecha = (fecha: string | null) => {
    if (!fecha) return 'No disponible';
    try {
      const date = new Date(fecha);
      return date.toLocaleString('es-ES', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    } catch {
      return 'Fecha inválida';
    }
  };

  // Calcular duración del paquete
  const calcularDuracion = () => {
    if (!fechaHoraInicioIntervalo || !fechaHoraFinIntervalo) return 'No disponible';
    try {
      const inicio = new Date(fechaHoraInicioIntervalo);
      const fin = new Date(fechaHoraFinIntervalo);
      const duracionMs = fin.getTime() - inicio.getTime();
      const duracionHoras = duracionMs / (1000 * 60 * 60);
      return `${duracionHoras.toFixed(1)} horas`;
    } catch {
      return 'Error al calcular';
    }
  };

    return (
    <div className="indicador-paquete-actual">
      <h3>📦 Paquete Actual en Consumo</h3>
      
      <div className="info-paquete">
        <div className="campo">
          <strong>Número de Paquete:</strong>
          <span>{paqueteActualConsumido}</span>
        </div>
        
        <div className="campo">
          <strong>Fecha de Inicio:</strong>
          <span>{formatearFecha(fechaHoraInicioIntervalo)}</span>
        </div>

        <div className="campo">
          <strong>Fecha de Fin:</strong>
          <span>{formatearFecha(fechaHoraFinIntervalo)}</span>
        </div>
        
        <div className="campo">
          <strong>Duración:</strong>
          <span>{calcularDuracion()}</span>
        </div>
      </div>

      <div className="info-raw">
        <details>
          <summary>Información Técnica del Paquete</summary>
          <pre>
            {JSON.stringify(infoPaquete, null, 2)}
          </pre>
        </details>
      </div>

             <style>{`
         .indicador-paquete-actual {
           background: #f8f9fa;
           border: 1px solid #dee2e6;
           border-radius: 8px;
           padding: 16px;
           margin: 16px 0;
           font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
         }

         .indicador-paquete-actual h3 {
           margin: 0 0 16px 0;
           color: #495057;
           font-size: 18px;
           border-bottom: 2px solid #007bff;
           padding-bottom: 8px;
         }

         .info-paquete {
           display: grid;
           gap: 12px;
         }

         .campo {
           display: flex;
           justify-content: space-between;
           align-items: center;
           padding: 8px 12px;
           background: white;
           border-radius: 6px;
           border-left: 4px solid #007bff;
         }

         .campo strong {
           color: #495057;
           font-weight: 600;
         }

         .campo span {
           color: #6c757d;
           font-family: 'Courier New', monospace;
           font-size: 14px;
         }

         .info-raw {
           margin-top: 16px;
         }

         .info-raw details {
           background: white;
           border-radius: 6px;
           padding: 8px;
         }

         .info-raw summary {
           cursor: pointer;
           color: #007bff;
           font-weight: 600;
           padding: 8px;
         }

         .info-raw pre {
           background: #f8f9fa;
           border: 1px solid #dee2e6;
           border-radius: 4px;
           padding: 12px;
           margin: 8px 0 0 0;
           font-size: 12px;
           overflow-x: auto;
         }
       `}</style>
    </div>
  );
};

export default IndicadorPaqueteActual; 