import React from 'react';
import { ChevronRight } from 'lucide-react';
import TablaPedidos from './TablaPedidos';
import MetricasRendimiento from './MetricasRendimiento';
import CardsCamiones from './CardCamion';

interface RightMenuProps {
  expanded: boolean;
  setExpanded: (value: boolean) => void;
}

const RightMenu: React.FC<RightMenuProps> = ({ expanded, setExpanded }) => {
  if (!expanded) return null;

  return (
    <div className="transition-all duration-300 bg-white rounded-xl p-4 shadow-md h-full flex flex-col">
      <button
        onClick={() => setExpanded(false)}
        className="self-end mb-2"
        title="Ocultar menú"
      >
        <ChevronRight size={12} />
      </button>

      <div className="flex flex-col gap-4 overflow-y-auto">
        <div className="text-md font-medium">Estado de los camiones</div>
        <CardsCamiones />
        <div className="text-md font-medium">Datos de los pedidos</div>
        <TablaPedidos />
        <div className="text-md font-medium">Métricas de rendimiento</div>
        <MetricasRendimiento />
        <div className="text-md font-medium">Indicadores de camiones</div>
      </div>
    </div>
  );
};

export default RightMenu;
