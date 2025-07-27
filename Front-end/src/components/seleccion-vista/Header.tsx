import React from "react";
import logo from "../../assets/logo.png";

interface HeaderProps {
  titulo?: string;
  version?: string;
}

const Header: React.FC<HeaderProps> = ({ 
  titulo = "Sistema de Simulación Logística",
  version = "Versión 2.0" 
}) => {
  return (
    <div className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-4">
          <div className="flex items-center">
            <img src={logo} alt="Logo" className="h-8 w-auto mr-3" />
            <h1 className="text-2xl font-bold text-gray-900">{titulo}</h1>
          </div>
          <div className="text-sm text-gray-500">
            {version}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Header; 