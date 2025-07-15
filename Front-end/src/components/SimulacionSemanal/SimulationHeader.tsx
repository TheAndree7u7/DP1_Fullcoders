import React from 'react';
import IndicadorPaqueteActual from '../IndicadorPaqueteActual';

interface SimulationHeaderProps {
  tiempoTranscurrido: number;
  nodoActual: number;
  tiempoSimulado: Date | null;
  botonControl?: React.ReactNode;
}

const SimulationHeader: React.FC<SimulationHeaderProps> = ({ 
  tiempoTranscurrido, 
  nodoActual, 
  tiempoSimulado,
  botonControl
}) => {
  const formatearTiempoTranscurrido = (segundos: number): string => {
    const horas = Math.floor(segundos / 3600);
    const minutos = Math.floor((segundos % 3600) / 60);
    const seg = segundos % 60;
    return `${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${seg.toString().padStart(2, '0')}`;
  };

  return (
    <div className="bg-[#1E293B] text-white py-2 px-4 flex justify-between items-center">
      <h1 className="font-bold">
        Ejecución Semanal - {formatearTiempoTranscurrido(tiempoTranscurrido)}
      </h1>
      <div className="flex items-center gap-4">
        <div className="bg-[#334155] rounded-lg px-3 py-1">
          <IndicadorPaqueteActual 
            variant="compact" 
            showProgress={false} 
            showTime={false} 
          />
        </div>
        {/* Botón de control si se proporciona */}
        {botonControl}
      </div>
    </div>
  );
};

export default SimulationHeader;
