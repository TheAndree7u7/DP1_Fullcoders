import { useEffect, useRef, useState } from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import type { Coordenada, Pedido } from '../types';
import almacenCentralIcon from '../assets/almacen_central.svg';
import almacenIntermedioIcon from '../assets/almacen_intermedio.svg';
import clienteIcon from '../assets/cliente.svg';
import { averiarCamionTipo } from '../services/averiaApiService';

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

// Parametrización del grosor de línea de bloqueos
const BLOQUEO_STROKE_WIDTH = 4;

const parseCoord = (s: string): Coordenada => {
  const match = s.match(/\((\d+),\s*(\d+)\)/);
  if (!match) throw new Error(`Coordenada inválida: ${s}`);
  return { x: parseInt(match[1]), y: parseInt(match[2]) };
};

const colors = [
  '#ef4444', '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6',
  '#ec4899', '#22d3ee', '#a3e635', '#eab308', '#f43f5e',
  '#06b6d4', '#84cc16', '#e879f9', '#4ade80', '#f97316',
  '#c084fc', '#2dd4bf', '#fde047', '#facc15', '#7dd3fc'
];

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
  const [intervalo, setIntervalo] = useState(300);
  const intervalRef = useRef<number | null>(null);
  const { camiones, rutasCamiones, almacenes, avanzarHora, cargando, bloqueos } = useSimulacion();
  const [tooltipCamion, setTooltipCamion] = useState<string | null>(null);
  const [tooltipPos, setTooltipPos] = useState<{x: number, y: number} | null>(null);
  const [averiando, setAveriando] = useState<string | null>(null);

  // DEBUG: Verificar que almacenes llega al componente
  //console.log('🗺️ MAPA: Almacenes recibidos:', almacenes);

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
  //console.log('👥 MAPA: Pedidos pendientes (clientes):', pedidosPendientes);
  //console.log('🚚 MAPA: Estado de camiones:', camiones);

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
    // Rebuild visuals whenever routes or truck states change
    setCamionesVisuales(() =>
      rutasCamiones.map((info, idx) => {
        const rutaCoords = info.ruta.map(parseCoord);
        const estadoCamion = camiones.find(c => c.id === info.id);
        // Determine current and previous positions
        const currentPos = estadoCamion ? parseCoord(estadoCamion.ubicacion) : rutaCoords[0];
        let prevPos = rutaCoords[0];
        if (estadoCamion && estadoCamion.porcentaje > 0) {
          const prevIdx = Math.min(rutaCoords.length - 1, Math.floor(estadoCamion.porcentaje));
          prevPos = rutaCoords[prevIdx];
        }
        const rot = calcularRotacion(prevPos, currentPos);
        // Compute remaining path
        const porcentaje = estadoCamion ? estadoCamion.porcentaje : 0;
        const idxRest = Math.ceil(porcentaje);
        const rutaRestante = rutaCoords.slice(idxRest);
        return {
          id: info.id,
          color: colors[idx % colors.length],
          ruta: rutaRestante,
          posicion: currentPos,
          rotacion: rot
        } as CamionVisual;
      })
    );
  }, [camiones, rutasCamiones]);

  // Función para manejar click en camión
  const handleCamionClick = (camionId: string, evt: React.MouseEvent<SVGGElement, MouseEvent>) => {
    setTooltipCamion(camionId);
    setTooltipPos({ x: evt.clientX, y: evt.clientY });
  };

  const handleAveriar = async (camionId: string, tipo: number) => {
    setAveriando(camionId + '-' + tipo);
    try {
      await averiarCamionTipo(camionId, tipo);
      alert(`Camión averiado correctamente (Tipo ${tipo})`);
    } catch {
      alert('Error al averiar el camión');
    } finally {
      setAveriando(null);
      setTooltipCamion(null);
    }
  };

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

        {/* Bloqueos */}
        {bloqueos && bloqueos.map((bloqueo, idx) => (
          <polyline
            key={`bloqueo-${idx}`}
            fill="none"
            stroke="#dc2626"
            strokeWidth={BLOQUEO_STROKE_WIDTH}
            strokeLinecap="round"
            strokeLinejoin="round"
            points={bloqueo.coordenadas.map(coord => `${coord.x * CELL_SIZE},${coord.y * CELL_SIZE}`).join(' ')}
          />
        ))}

        {/* Clientes/Pedidos */}
        {pedidosPendientes.map(pedido => {
          //console.log('👤 MAPA: Renderizando cliente:', pedido.codigo, 'en posición:', pedido.coordenada);
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
          //console.log('🏪 MAPA: Renderizando almacén:', almacen.nombre, 'en posición:', almacen.coordenada);
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
        {camionesVisuales
          .filter(camion => camiones.find(c => c.id === camion.id)?.estado !== 'Entregado' && camion.ruta.length > 1)
          .map(camion => (
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
        {camionesVisuales
          .filter(camion => camiones.find(c => c.id === camion.id)?.estado !== 'Entregado')
          .map(camion => {
             const { posicion, rotacion, color } = camion;
             const cx = posicion.x * CELL_SIZE;
             const cy = posicion.y * CELL_SIZE;
             return (
               <g
                 key={camion.id}
                 transform={`translate(${cx}, ${cy}) rotate(${rotacion})`}
                 style={{ transition: 'transform 0.8s linear', cursor: 'pointer' }}
                 onClick={evt => handleCamionClick(camion.id, evt)}
               >
                 <rect x={-6} y={-4} width={12} height={8} rx={2} fill={color} stroke="black" strokeWidth={0.5} />
                 <circle cx={-4} cy={5} r={1.5} fill="black" />
                 <circle cx={4} cy={5} r={1.5} fill="black" />
               </g>
             );
          })}
      </svg>

      {/* Tooltip para camión */}
      {tooltipCamion && tooltipPos && (
        <div
          style={{
            position: 'fixed',
            left: tooltipPos.x + 10,
            top: tooltipPos.y + 10,
            background: 'white',
            border: '1px solid #ccc',
            borderRadius: 8,
            padding: 12,
            zIndex: 1000,
            boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
          }}
        >
          <div className="mb-2 font-bold">Camión: {tooltipCamion}</div>
          <div className="flex flex-col gap-2">
            <button
              className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded"
              disabled={averiando === tooltipCamion + '-1'}
              onClick={() => handleAveriar(tooltipCamion, 1)}
            >
              {averiando === tooltipCamion + '-1' ? 'Averiando...' : 'Avería tipo 1'}
            </button>
            <button
              className="bg-orange-500 hover:bg-orange-600 text-white px-3 py-1 rounded"
              disabled={averiando === tooltipCamion + '-2'}
              onClick={() => handleAveriar(tooltipCamion, 2)}
            >
              {averiando === tooltipCamion + '-2' ? 'Averiando...' : 'Avería tipo 2'}
            </button>
            <button
              className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
              disabled={averiando === tooltipCamion + '-3'}
              onClick={() => handleAveriar(tooltipCamion, 3)}
            >
              {averiando === tooltipCamion + '-3' ? 'Averiando...' : 'Avería tipo 3'}
            </button>
          </div>
          <button
            className="mt-2 text-gray-500 hover:text-black"
            onClick={() => setTooltipCamion(null)}
          >
            Cerrar
          </button>
        </div>
      )}

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
