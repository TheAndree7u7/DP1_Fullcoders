import React from "react";

export interface OpcionSimulacion {
  id: string;
  titulo: string;
  descripcion: string;
  ruta: string;
  imagen: string;
  color: string;
  icono: string;
  tipoSimulacion: string;
}

interface SimulacionCardProps {
  opcion: OpcionSimulacion;
  cargando: string | null;
  onSeleccionar: (opcion: OpcionSimulacion) => void;
}

const SimulacionCard: React.FC<SimulacionCardProps> = ({ 
  opcion, 
  cargando, 
  onSeleccionar 
}) => {
  const estaCargando = cargando === opcion.id;

  return (
    <div
      onClick={() => onSeleccionar(opcion)}
      className="group cursor-pointer transform transition-all duration-300 hover:scale-105 hover:shadow-2xl"
    >
      <div className="bg-white rounded-2xl shadow-lg overflow-hidden border border-gray-200 hover:border-gray-300 transition-all duration-300">
        {/* Header de la tarjeta */}
        <div className={`bg-gradient-to-r ${opcion.color} p-6 text-white`}>
          <div className="flex items-center justify-between">
            <span className="text-3xl">{opcion.icono}</span>
            <div className="text-right">
              <h3 className="text-xl font-bold">{opcion.titulo}</h3>
            </div>
          </div>
        </div>

        {/* Imagen */}
        <div className="p-6 bg-gray-50">
          <img
            src={opcion.imagen}
            alt={opcion.titulo}
            className="w-full h-48 object-contain rounded-lg group-hover:opacity-80 transition-opacity duration-300"
          />
        </div>

        {/* Descripción */}
        <div className="p-6">
          <p className="text-gray-600 text-sm leading-relaxed mb-4">
            {opcion.descripcion}
          </p>
          
          {/* Botón de acción */}
          <button 
            disabled={estaCargando}
            className={`w-full bg-gradient-to-r ${opcion.color} text-white py-3 px-6 rounded-lg font-semibold text-sm transition-all duration-300 transform group-hover:translate-y-[-2px] group-hover:shadow-lg ${
              estaCargando ? 'opacity-50 cursor-not-allowed' : ''
            }`}
          >
            {estaCargando ? (
              <div className="flex items-center justify-center">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                Configurando...
              </div>
            ) : (
              opcion.tipoSimulacion === 'DIARIA' ? 'Iniciar Automáticamente' : 'Iniciar Simulación'
            )}
          </button>
        </div>

        {/* Indicador de hover */}
        <div className={`h-1 bg-gradient-to-r ${opcion.color} transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300`}></div>
      </div>
    </div>
  );
};

export default SimulacionCard; 