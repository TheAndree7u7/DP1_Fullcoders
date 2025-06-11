import React, { useState } from 'react';
import { ChevronRight, ChevronDown } from 'lucide-react';
import TablaPedidos from './TablaPedidos';
import CardsCamiones from './CardCamion';

interface RightMenuProps {
  expanded: boolean;
  setExpanded: (value: boolean) => void;
}

const RightMenu: React.FC<RightMenuProps> = ({ expanded, setExpanded }) => {
  const [showCamiones, setShowCamiones] = useState(true);
  const [showPedidos, setShowPedidos] = useState(true);

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
        <div className="flex items-center justify-between cursor-pointer" onClick={() => setShowCamiones(!showCamiones)}>
          <div className="text-md font-medium">Estado de los camiones</div>
          {showCamiones ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
        </div>
        {showCamiones && <CardsCamiones />}

        <div className="flex items-center justify-between cursor-pointer" onClick={() => setShowPedidos(!showPedidos)}>
          <div className="text-md font-medium">Datos de los pedidos</div>
          {showPedidos ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
        </div>
        {showPedidos && <TablaPedidos />}
      </div>
    </div>
  );
};

export default RightMenu;
