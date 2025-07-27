import React from "react";

interface FooterProps {
  texto?: string;
}

const Footer: React.FC<FooterProps> = ({ 
  texto = "© 2025 Sistema de Simulación Logística - Desarrollado para DP1 Fullcoders" 
}) => {
  return (
    <div className="bg-white border-t border-gray-200 py-4">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center text-sm text-gray-500">
          {texto}
        </div>
      </div>
    </div>
  );
};

export default Footer; 