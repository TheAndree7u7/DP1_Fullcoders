import React from 'react';
import ControlSimulacion from '../ControlSimulacion';
import IndicadorPaqueteActual from '../IndicadorPaqueteActual';

interface ControlPanelProps {
  expanded: boolean;
  onToggle: () => void;
}

const ControlPanel: React.FC<ControlPanelProps> = ({ expanded, onToggle }) => (
  <div className="px-4 py-2">
    <div className="flex items-center justify-between mb-2">
      <button
        onClick={onToggle}
        className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
        aria-expanded={expanded}
        aria-label={expanded ? 'Ocultar Control de Simulación' : 'Mostrar Control de Simulación'}
      >
        {expanded ? '🔼 Ocultar Control' : '🔽 Mostrar Control de Simulación'}
      </button>
    </div>
    
    {expanded && (
      <div className="transition-all duration-300">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="lg:col-span-2">
            <ControlSimulacion />
          </div>
          <div className="lg:col-span-1">
            <IndicadorPaqueteActual 
              variant="detailed" 
              showProgress={true} 
              showTime={true}
            />
          </div>
        </div>
      </div>
    )}
  </div>
);

export default ControlPanel;
