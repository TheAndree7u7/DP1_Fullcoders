import React, { useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react'; // o usa un SVG tuyo

const RightMenu: React.FC = () => {
  const [expanded, setExpanded] = useState(true);

  return (
    <div
      className={`transition-all duration-300 bg-white rounded-xl p-4 shadow-md h-full ${expanded ? 'w-64' : 'w-10'
        } flex flex-col`}
    >
      <button
        onClick={() => setExpanded(!expanded)}
        className="self-end mb-2"
        title={expanded ? "Ocultar menú" : "Mostrar menú"}
      >
        {expanded ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
      </button>

      {expanded && (
        <div className="flex flex-col gap-4">
          <div className="text-md font-medium">Estado de los camiones</div>
          <div className="text-md font-medium">Datos de los pedidos</div>
          <div className="text-md font-medium">Métricas de rendimiento</div>
          <div className="text-md font-medium">Indicadores de camiones</div>
        </div>
      )}
    </div>
  );
};

export default RightMenu;
