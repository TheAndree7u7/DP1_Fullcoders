import React from 'react';
import { Play, Pause } from 'lucide-react';

interface ControlAnimacionProps {
  estaAnimando: boolean;
  onToggle: () => void;
  disabled?: boolean;
}

const ControlAnimacion: React.FC<ControlAnimacionProps> = ({
  estaAnimando,
  onToggle,
  disabled = false
}) => {
  return (
    <button
      onClick={onToggle}
      disabled={disabled}
      className={`
        flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium
        transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed
        ${estaAnimando 
          ? 'bg-red-500 hover:bg-red-600 text-white' 
          : 'bg-green-500 hover:bg-green-600 text-white'
        }
      `}
      title={estaAnimando ? 'Pausar animación' : 'Iniciar animación'}
    >
      {estaAnimando ? <Pause size={16} /> : <Play size={16} />}
      {estaAnimando ? 'Pausar' : 'Animar'}
    </button>
  );
};

export default ControlAnimacion;
