import React from "react";

interface GestionPedidosProps {
  titulo?: string;
  descripcion?: string;
  textoBoton?: string;
  onNavegar: () => void;
}

const GestionPedidos: React.FC<GestionPedidosProps> = ({ 
  titulo = "Gestión de Pedidos",
  descripcion = "Agrega nuevos pedidos de GLP al sistema de simulación de forma individual o mediante archivos",
  textoBoton = "📦 Agregar Pedidos",
  onNavegar
}) => {
  return (
    <div className="mt-8 text-center">
      <div className="bg-white rounded-lg shadow-md p-6 max-w-2xl mx-auto">
        <h3 className="text-lg font-semibold text-gray-900 mb-3">
          {titulo}
        </h3>
        <p className="text-gray-600 text-sm mb-4">
          {descripcion}
        </p>
        <button
          onClick={onNavegar}
          className="bg-gradient-to-r from-purple-500 to-purple-600 text-white py-3 px-8 rounded-lg font-semibold text-sm transition-all duration-300 hover:shadow-lg hover:scale-105"
        >
          {textoBoton}
        </button>
      </div>
    </div>
  );
};

export default GestionPedidos; 