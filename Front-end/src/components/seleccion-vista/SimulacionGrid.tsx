import React from "react";
import SimulacionCard, { OpcionSimulacion } from "./SimulacionCard";

interface SimulacionGridProps {
  opciones: OpcionSimulacion[];
  cargando: string | null;
  onSeleccionar: (opcion: OpcionSimulacion) => void;
}

const SimulacionGrid: React.FC<SimulacionGridProps> = ({ 
  opciones, 
  cargando, 
  onSeleccionar 
}) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl w-full">
      {opciones.map((opcion) => (
        <SimulacionCard
          key={opcion.id}
          opcion={opcion}
          cargando={cargando}
          onSeleccionar={onSeleccionar}
        />
      ))}
    </div>
  );
};

export default SimulacionGrid; 