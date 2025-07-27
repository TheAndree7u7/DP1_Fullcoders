import React from "react";

interface StatusMessageProps {
  mensaje: {
    texto: string;
    tipo: 'success' | 'error' | 'info';
  } | null;
}

const StatusMessage: React.FC<StatusMessageProps> = ({ mensaje }) => {
  if (!mensaje) return null;

  const getIcon = (tipo: string) => {
    switch (tipo) {
      case 'success': return '✅';
      case 'error': return '❌';
      case 'info': return '⏳';
      default: return 'ℹ️';
    }
  };

  const getStyles = (tipo: string) => {
    switch (tipo) {
      case 'success': 
        return 'bg-green-100 text-green-800 border border-green-200';
      case 'error': 
        return 'bg-red-100 text-red-800 border border-red-200';
      case 'info': 
        return 'bg-blue-100 text-blue-800 border border-blue-200';
      default: 
        return 'bg-gray-100 text-gray-800 border border-gray-200';
    }
  };

  return (
    <div className={`mb-6 p-4 rounded-lg max-w-2xl mx-auto ${getStyles(mensaje.tipo)}`}>
      <div className="flex items-center justify-center">
        <span className="mr-2">{getIcon(mensaje.tipo)}</span>
        <span className="font-medium">{mensaje.texto}</span>
      </div>
    </div>
  );
};

export default StatusMessage; 