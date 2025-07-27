import React from "react";

interface InformacionSistemaProps {
  titulo?: string;
  caracteristicas?: Array<{ icono: string; texto: string }>;
}

const InformacionSistema: React.FC<InformacionSistemaProps> = ({ 
  titulo = "Información del Sistema",
  caracteristicas = [
    { icono: "📈", texto: "Monitoreo en tiempo real" },
    { icono: "🗺️", texto: "Visualización de rutas" },
    { icono: "📊", texto: "Métricas de rendimiento" }
  ]
}) => {
  return (
    <div className="mt-12 text-center">
      <div className="bg-white rounded-lg shadow-md p-6 max-w-4xl mx-auto">
        <h3 className="text-lg font-semibold text-gray-900 mb-3">
          {titulo}
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-sm text-gray-600">
          {caracteristicas.map((caracteristica, index) => (
            <div key={index} className="flex items-center justify-center">
              <span className="mr-2">{caracteristica.icono}</span>
              <span>{caracteristica.texto}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default InformacionSistema; 