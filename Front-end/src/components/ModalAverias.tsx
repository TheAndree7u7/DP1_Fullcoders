const ModalAverias = () => {
  return (
    <div className="flex w-full max-w-2xl flex-col border-2 rounded-2xl p-6 bg-white shadow-lg">
      <div className="text-2xl font-bold mb-8 text-gray-800">
        Registrar Avería
      </div>

      <div className="mb-6">
        <label htmlFor="codigoCamion" className="mb-2 text-lg font-medium text-gray-700 block">
          Código de Camión
        </label>
        <div className="relative flex items-center border border-gray-300 rounded-md focus-within:ring-2 focus-within:ring-blue-500">
          <span className="absolute left-3 text-gray-500">
            {/* SVG para el ícono de búsqueda */}
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
          </span>
          <input
            type="text"
            id="codigoCamion"
            className="pl-10 pr-3 py-2 w-full rounded-md focus:outline-none"
            placeholder="TA01"
          />
        </div>
      </div>

      <div className="mb-8">
        <label htmlFor="tipoIncidente" className="mb-2 text-lg font-medium text-gray-700 block">
          Tipo de Incidente :
        </label>
        <input
          type="number"
          id="tipoIncidente"
          className="p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-32" // Ancho reducido
          placeholder="3"
          min="1" // Puedes ajustar el valor mínimo si es necesario
        />
      </div>

      {/* Botones de acción */}
      <div className="flex justify-between items-center mt-auto"> {/* mt-auto para empujar hacia abajo */}
        <button className="px-6 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 transition-colors duration-200 shadow-md">
          Cancelar
        </button>
        <button className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors duration-200 shadow-md">
          Guardar
        </button>
      </div>
    </div>
  );
};

export default ModalAverias;
