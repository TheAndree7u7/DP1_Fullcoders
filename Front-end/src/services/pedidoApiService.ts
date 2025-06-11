import axios from 'axios';

export interface ResumenPedidos {
  total: number;
  porEstado: Record<string, number>;
}

export const getResumenPedidosPorRango = async (
  inicio: string,
  fin: string
): Promise<ResumenPedidos> => {
  const response = await axios.get<ResumenPedidos>(
    'http://localhost:8085/api/pedidos/resumen/rango',
    { params: { inicio, fin } }
  );
  return response.data;
};

