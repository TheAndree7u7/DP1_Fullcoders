import { useEffect, useRef, useState } from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import type { Coordenada, Pedido } from '../types';
import almacenCentralIcon from '../assets/almacen_central.svg';
import almacenIntermedioIcon from '../assets/almacen_intermedio.svg';
import clienteIcon from '../assets/cliente.svg';

interface CamionVisual {
  id: string;
  color: string;
  ruta: Coordenada[];
  posicion: Coordenada;
  rotacion: number;
}

const GRID_WIDTH = 70;
const GRID_HEIGHT = 50;
const CELL_SIZE = 14;
const SVG_WIDTH = GRID_WIDTH * CELL_SIZE;
const SVG_HEIGHT = GRID_HEIGHT * CELL_SIZE;

const parseCoord = (s: string): Coordenada => {
  const match = s.match(/\((\d+),\s*(\d+)\)/);
  if (!match) throw new Error(`Coordenada inválida: ${s}`);
  return { x: parseInt(match[1]), y: parseInt(match[2]) };
};

const colors = ['#ef4444', '#3b82f6', '#10b981', '#f59e0b'];

const calcularRotacion = (prev: Coordenada, next: Coordenada): number => {
  const dx = next.x - prev.x;
  const dy = next.y - prev.y;
  if (dx === 1) return 0;
  if (dx === -1) return 180;
  if (dy === 1) return 90;
  if (dy === -1) return 270;
  return 0;
};

const Mapa = () => {
  const [camionesVisuales, setCamionesVisuales] = useState<CamionVisual[]>([]);
  const [running, setRunning] = useState(false);
  const [intervalo, setIntervalo] = useState(1000);
  const intervalRef = useRef<number | null>(null);
  const { camiones, rutasCamiones, almacenes, avanzarHora, cargando } = useSimulacion();

  // DEBUG: Verificar que almacenes llega al componente
  console.log('🗺️ MAPA: Almacenes recibidos:', almacenes);

  // Función para obtener los pedidos pendientes (no entregados)
  const getPedidosPendientes = () => {
    const pedidosPendientes: Pedido[] = [];
    
    rutasCamiones.forEach(ruta => {
      const camionActual = camiones.find(c => c.id === ruta.id);
      if (!camionActual) {
        // Si no hay estado del camión, mostrar todos los pedidos
        pedidosPendientes.push(...ruta.pedidos);
        return;
      }

      // Obtener la posición actual del camión en la ruta
      const posicionActual = camionActual.porcentaje;
      
      // Si el camión está entregado, no mostrar ningún pedido de esta ruta
      if (camionActual.estado === 'Entregado') {
        return;
      }

      // Para cada pedido de esta ruta, verificar si ya fue visitado
      ruta.pedidos.forEach(pedido => {
        // Buscar el índice del nodo que corresponde a este pedido
        const indicePedidoEnRuta = ruta.ruta.findIndex(nodo => {
          const coordNodo = parseCoord(nodo);
          return coordNodo.x === pedido.coordenada.x && coordNodo.y === pedido.coordenada.y;
        });

        // Si el pedido está en un nodo que aún no ha sido visitado, mostrarlo
        if (indicePedidoEnRuta === -1 || indicePedidoEnRuta > posicionActual) {
          pedidosPendientes.push(pedido);
        }
      });
    });

    return pedidosPendientes;
  };

  const pedidosPendientes = getPedidosPendientes();
  console.log('👥 MAPA: Pedidos pendientes (clientes):', pedidosPendientes);
  console.log('🚚 MAPA: Estado de camiones:', camiones);

  useEffect(() => {
    const iniciales = rutasCamiones.map((info, idx) => {
      const ruta = info.ruta.map(parseCoord);
      return {
        id: info.id,
        color: colors[idx % colors.length],
        ruta,
        posicion: ruta[0],
        rotacion: 0,
      };
    });
    setCamionesVisuales(iniciales);
  }, [rutasCamiones]);

  useEffect(() => {
    if (running) {
      intervalRef.current = window.setInterval(() => {
        avanzarHora();
      }, intervalo);
    } else {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    }
    return () => {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    };
  }, [running, intervalo, avanzarHora]);

  useEffect(() => {
    setCamionesVisuales((prevCamiones) =>
      prevCamiones.map(camion => {
        const nuevo = camiones.find(c => c.id === camion.id);
        if (!nuevo) return camion;
        const nuevaCoord = parseCoord(nuevo.ubicacion);
        const rot = calcularRotacion(camion.posicion, nuevaCoord);
        
        // Encontrar el índice de la posición actual en la ruta
        const rutaActual = camion.ruta;
        const posicionActual = nuevaCoord;
        const indiceActual = rutaActual.findIndex(
          (punto: Coordenada) => punto.x === posicionActual.x && punto.y === posicionActual.y
        );
        
        // Filtrar la ruta para mostrar solo los puntos que faltan por recorrer
        const rutaRestante = rutaActual.slice(indiceActual);
        
        return {
          ...camion,
          posicion: nuevaCoord,
          rotacion: rot,
          ruta: rutaRestante
        };
      })
    );
  }, [camiones]);

  if (cargando) {
    return <p>Cargando simulación...</p>;
  }

  return (
    <div className="flex flex-col items-center gap-2">
      <svg
        width={SVG_WIDTH}
        height={SVG_HEIGHT}
        className="border border-gray-500 bg-white rounded-xl"
      >
        {/* Grid */}
        {[...Array(GRID_WIDTH + 1)].map((_, i) => (
          <line key={`v-${i}`} x1={i * CELL_SIZE} y1={0} x2={i * CELL_SIZE} y2={SVG_HEIGHT} stroke="#d1d5db" strokeWidth={1} />
        ))}
        {[...Array(GRID_HEIGHT + 1)].map((_, i) => (
          <line key={`h-${i}`} x1={0} y1={i * CELL_SIZE} x2={SVG_WIDTH} y2={i * CELL_SIZE} stroke="#d1d5db" strokeWidth={1} />
        ))}

        {/* Clientes/Pedidos */}
        {pedidosPendientes.map(pedido => {
          console.log('👤 MAPA: Renderizando cliente:', pedido.codigo, 'en posición:', pedido.coordenada);
          return (
            <g key={pedido.codigo}>
              <image
                href={clienteIcon}
                x={pedido.coordenada.x * CELL_SIZE - 15}
                y={pedido.coordenada.y * CELL_SIZE - 15}
                width={30}
                height={30}
              />
              <text
                x={pedido.coordenada.x * CELL_SIZE}
                y={pedido.coordenada.y * CELL_SIZE + 25}
                textAnchor="middle"
                fontSize="10"
                fill="#dc2626"
                fontWeight="bold"
                stroke="#fff"
                strokeWidth="0.5"
              >
                {pedido.codigo}
              </text>
            </g>
          );
        })}

        {/* Almacenes */}
        {almacenes.map(almacen => {
          console.log('🏪 MAPA: Renderizando almacén:', almacen.nombre, 'en posición:', almacen.coordenada);
          return (
            <g key={almacen.id}>
              <image
                href={almacen.tipo === 'CENTRAL' ? almacenCentralIcon : almacenIntermedioIcon}
                x={almacen.coordenada.x * CELL_SIZE - 20}
                y={almacen.coordenada.y * CELL_SIZE - 20}
                width={40}
                height={40}
              />
              <text
                x={almacen.coordenada.x * CELL_SIZE}
                y={almacen.coordenada.y * CELL_SIZE + 30}
                textAnchor="middle"
                fontSize="12"
                fill={almacen.tipo === 'CENTRAL' ? '#2563eb' : '#16a34a'}
                fontWeight="bold"
                stroke="#fff"
                strokeWidth="0.5"
              >
                {almacen.nombre}
              </text>
            </g>
          );
        })}

        {/* Rutas de camiones */}
        {camionesVisuales.map(camion => (
          <polyline
            key={`ruta-${camion.id}`}
            fill="none"
            stroke={camion.color}
            strokeWidth={2}
            strokeDasharray="4 2"
            points={camion.ruta.map((p: Coordenada) => `${p.x * CELL_SIZE},${p.y * CELL_SIZE}`).join(' ')}
          />
        ))}

        {/* Camiones */}
        {camionesVisuales.map(camion => {
          const { posicion, rotacion, color } = camion;
          const cx = posicion.x * CELL_SIZE;
          const cy = posicion.y * CELL_SIZE;
          return (
            <g
              key={camion.id}
              transform={`translate(${cx}, ${cy}) rotate(${rotacion})`}
              style={{ transition: 'transform 0.8s linear' }}
            >
              <rect x={-6} y={-4} width={12} height={8} rx={2} fill={color} stroke="black" strokeWidth={0.5} />
              <circle cx={-4} cy={5} r={1.5} fill="black" />
              <circle cx={4} cy={5} r={1.5} fill="black" />
            </g>
          );
        })}
      </svg>

      <div className="flex items-center gap-4 mt-2">
        <button
          onClick={() => setRunning(prev => !prev)}
          className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-1 rounded"
        >
          {running ? 'Pausar' : 'Iniciar'}
        </button>
        <label className="flex items-center gap-1 text-sm">
          Velocidad:
          <input
            type="number"
            min={100}
            step={100}
            value={intervalo}
            onChange={e => setIntervalo(parseInt(e.target.value))}
            className="border rounded px-2 py-0.5 w-20"
          />
          ms
        </label>
      </div>
    </div>
  );
};

export default Mapa;