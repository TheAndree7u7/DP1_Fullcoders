import React from 'react';
import { useNavigate } from 'react-router-dom';

const ModuloEnDesarrollo: React.FC<{ titulo: string }> = ({ titulo }) => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-lg text-center max-w-md">
        <div className="mb-4">
          <div className="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <span className="text-2xl">🚧</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-800 mb-2">{titulo}</h1>
          <p className="text-gray-600">Este módulo está en desarrollo y estará disponible próximamente.</p>
        </div>
        <button
          onClick={() => navigate('/')}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Volver al inicio
        </button>
      </div>
    </div>
  );
};

export default ModuloEnDesarrollo;
