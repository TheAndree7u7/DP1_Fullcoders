import React from 'react';

const TablaPedidos: React.FC = () => {
  const headers = ["Número", "Cantidad", "Ubicación", "Estado"];
  const data = [
    ["123456789", "10", "(10,2)", "En camino"],
    ["987654321", "5", "(9,4)", "En camino"],
    ["345678901", "2", "(8,6)", "En camino"],
    ["234567890", "1", "(7,8)", "En camino"],
    ["234567890", "1", "(7,8)", "En espera"],
    ["456789012", "3", "(6,10)", "Entregado"],
  ];

  return (
    <div className="overflow-auto rounded-lg border border-gray-200">
      <table className="min-w-full table-auto text-sm text-left">
        <thead className="bg-gray-100">
          <tr>
            {headers.map((header, index) => (
              <th key={index} className="px-4 py-2 font-semibold text-gray-700">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((fila, filaIndex) => (
            <tr key={filaIndex} className="border-t border-gray-100 hover:bg-gray-50">
              {fila.map((celda, celdaIndex) => (
                <td key={celdaIndex} className="px-4 py-2 text-gray-700">
                  {celda}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default TablaPedidos;
