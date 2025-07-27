import React from "react";

interface BienvenidaProps {
  titulo?: string;
  descripcion?: string;
}

const Bienvenida: React.FC<BienvenidaProps> = ({ 
  titulo = "Bienvenido al Sistema de Simulación",
  descripcion = "Selecciona el tipo de simulación que deseas ejecutar para analizar diferentes escenarios logísticos"
}) => {
  return (
    <div className="text-center mb-12">
      <h2 className="text-4xl font-bold text-gray-900 mb-4">
        {titulo}
      </h2>
      <p className="text-xl text-gray-600 max-w-2xl mx-auto">
        {descripcion}
      </p>
    </div>
  );
};

export default Bienvenida; 