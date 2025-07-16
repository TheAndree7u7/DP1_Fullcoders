import React from 'react';

type Panel = 'camiones' | 'bloqueos';
type ElementoResaltado = {
  tipo: 'camion' | 'pedido' | 'almacen';
  id: string;
} | null;

interface ElementHighlightIndicatorProps {
  elemento: ElementoResaltado;
  panel: Panel;
  onClear: () => void;
}

const ElementHighlightIndicator: React.FC<ElementHighlightIndicatorProps> = ({ 
  elemento, 
  panel, 
  onClear 
}) => {
  if (!elemento) return null;

  const positionClass = panel === 'camiones' 
    ? "absolute top-4 left-4 z-20" 
    : "absolute top-20 left-4 z-20";
  
  const marginStyle = panel === 'camiones' ? { marginTop: '280px' } : {};
  const widthClass = panel === 'camiones' ? 'w-32' : '';

  const getElementLabel = () => {
    const baseLabel = elemento.tipo === 'camion' ? 'Camión' : 
                     elemento.tipo === 'pedido' ? 'Pedido' : 'Almacén';
    return panel === 'bloqueos' ? `${baseLabel} seleccionado` : baseLabel;
  };

  const getButtonText = () => {
    return panel === 'bloqueos' ? 'Limpiar selección' : 'Limpiar';
  };

  return (
    <div className={positionClass} style={marginStyle}>
      <div className={`bg-amber-100 border border-amber-300 rounded-lg p-3 shadow-lg ${widthClass}`}>
        <div className="flex items-center gap-2 mb-2">
          <div className="w-3 h-3 bg-amber-500 rounded-full animate-pulse"></div>
          <span className="text-xs font-semibold text-amber-800">
            {getElementLabel()}
          </span>
        </div>
        <div className="text-xs text-amber-700 mb-2 font-bold">
          {elemento.id}
        </div>
        <button
          onClick={onClear}
          className="text-xs bg-amber-500 hover:bg-amber-600 text-white px-2 py-1 rounded w-full transition-colors"
          aria-label={`Limpiar selección de ${elemento.tipo} ${elemento.id}`}
        >
          {getButtonText()}
        </button>
      </div>
    </div>
  );
};

export default ElementHighlightIndicator;
