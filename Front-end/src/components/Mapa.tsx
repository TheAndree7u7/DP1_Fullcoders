import { useEffect, useRef, useState } from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import type { Coordenada, Pedido } from '../types';
import almacenCentralIcon from '../assets/almacen_central.svg';
import almacenIntermedioIcon from '../assets/almacen_intermedio.svg';
import clienteIcon from '../assets/cliente.svg';
import { averiarCamionTipo } from '../services/averiaApiService';
import { toast, Bounce } from 'react-toastify';
import { CAMION_COLORS, ESTADO_COLORS } from '../config/colors';
import { ChevronDown, ChevronUp } from 'lucide-react';

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
  const { camiones, rutasCamiones, almacenes, avanzarHora, cargando, bloqueos, marcarCamionAveriado } = useSimulacion();
  // Estado para el tooltip (hover)
  const [tooltipCamion, setTooltipCamion] = useState<string | null>(null);
  const [tooltipPos, setTooltipPos] = useState<{x: number, y: number} | null>(null);
  // Estado para el modal fijo (click)
  const [clickedCamion, setClickedCamion] = useState<string | null>(null);
  const [clickedPos, setClickedPos] = useState<{x: number, y: number} | null>(null);
  const [averiando, setAveriando] = useState<string | null>(null);
  // Estado para la leyenda desplegable
  const [leyendaVisible, setLeyendaVisible] = useState(false);

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
        color: CAMION_COLORS[idx % CAMION_COLORS.length],
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
          color: CAMION_COLORS[idx % CAMION_COLORS.length],
          ruta: rutaRestante,
          posicion: currentPos,
          rotacion: rot
        } as CamionVisual;
      })
    );
  }, [camiones, rutasCamiones]);

  // Eliminar función no usada

  const handleAveriar = async (camionId: string, tipo: number) => {
    setAveriando(camionId + '-' + tipo);
    try {
      const fechaHoraReporte = new Date().toISOString();
      await averiarCamionTipo(camionId, tipo, fechaHoraReporte);
      
      // Marcar el camión como averiado en el contexto
      marcarCamionAveriado(camionId);
      
      // Mostrar toast de éxito
      toast.error(`🚛💥 Camión ${camionId} averiado (Tipo ${tipo})`, {
        position: "top-right",
        autoClose: 5000,
        hideProgressBar: false,
        closeOnClick: false,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      
    } catch {
      toast.error('❌ Error al averiar el camión', {
        position: "top-right",
        autoClose: 3000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
    } finally {
      setAveriando(null);
      setClickedCamion(null);
    }
  };

  if (cargando) {
    return <p>Cargando simulación...</p>;
  }

  return (
    <div className="w-full h-full flex flex-col">
      <div className="flex items-start gap-3 flex-1">
        {/* Leyenda lateral compacta */}
        <div className="bg-white rounded-lg shadow-md border border-gray-200 p-2 w-32 flex-shrink-0">
          <button
            onClick={() => setLeyendaVisible(!leyendaVisible)}
            className="flex items-center justify-between w-full text-left text-xs font-semibold text-gray-800 hover:text-gray-900 mb-2"
          >
            <span>LEYENDA</span>
            {leyendaVisible ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
          </button>
          
          {leyendaVisible && (
            <div className="space-y-1.5">
              {/* Almacén Central */}
              <div className="flex items-center gap-1.5">
                <img src={almacenCentralIcon} alt="Almacén Central" className="w-4 h-4" />
                <span className="text-xs text-gray-700">A. Central</span>
              </div>
              
              {/* Almacén Intermedio */}
              <div className="flex items-center gap-1.5">
                <img src={almacenIntermedioIcon} alt="Almacén Intermedio" className="w-4 h-4" />
                <span className="text-xs text-gray-700">A. Intermedio</span>
              </div>
              
              {/* Cliente */}
              <div className="flex items-center gap-1.5">
                <img src={clienteIcon} alt="Cliente" className="w-4 h-4" />
                <span className="text-xs text-gray-700">Cliente</span>
              </div>
              
              {/* Camión */}
              <div className="flex items-center gap-1.5">
                <div className="w-4 h-3 bg-blue-500 rounded-sm border border-gray-400 relative">
                  <div className="absolute -bottom-0.5 left-0.5 w-0.5 h-0.5 bg-black rounded-full"></div>
                  <div className="absolute -bottom-0.5 right-0.5 w-0.5 h-0.5 bg-black rounded-full"></div>
                </div>
                <span className="text-xs text-gray-700">Camión</span>
              </div>
              
              {/* Ruta */}
              <div className="flex items-center gap-1.5">
                <div className="w-4 h-0.5 border-t border-dashed border-blue-500"></div>
                <span className="text-xs text-gray-700">Ruta</span>
              </div>
              
              {/* Bloqueos */}
              <div className="flex items-center gap-1.5">
                <div className="w-4 h-0.5 bg-red-600 rounded-full"></div>
                <span className="text-xs text-gray-700">Bloqueos</span>
              </div>
              
              {/* Estados de camiones */}
              <div className="pt-1 border-t border-gray-200">
                <div className="text-xs font-medium text-gray-600 mb-1">Estados:</div>
                <div className="space-y-1">
                  <div className="flex items-center gap-1">
                    <div className="w-2 h-2 bg-blue-500 rounded-sm"></div>
                    <span className="text-xs text-gray-700">Normal</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <div className="w-2 h-2 bg-red-500 rounded-sm"></div>
                    <span className="text-xs text-gray-700">Averiado</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <div className="w-2 h-2 bg-gray-800 rounded-sm"></div>
                    <span className="text-xs text-gray-700">Mant.</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Contenedor del mapa */}
        <div className="flex-1 flex flex-col items-center justify-center">
          <div className="w-full max-w-full overflow-auto">
            <svg
              width={SVG_WIDTH}
              height={SVG_HEIGHT}
              className="border border-gray-500 bg-white rounded-xl mx-auto"
              style={{ maxWidth: '100%', height: 'auto' }}
              viewBox={`0 0 ${SVG_WIDTH} ${SVG_HEIGHT}`}
              preserveAspectRatio="xMidYMid meet"
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
              .filter(camion => {
                const estadoCamion = camiones.find(c => c.id === camion.id);
                return estadoCamion?.estado !== 'Entregado' && 
                       estadoCamion?.estado !== 'Averiado' && 
                       camion.ruta.length > 1;
              })
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
                 const estadoCamion = camiones.find(c => c.id === camion.id);
                 const esAveriado = estadoCamion?.estado === 'Averiado';
                 const esEnMantenimiento = estadoCamion?.estado === 'En Mantenimiento';
                 const { posicion, rotacion, color } = camion;
                 // Rojo para averiados, negro para mantenimiento, color original para otros estados
                 const colorFinal = esAveriado ? ESTADO_COLORS.AVERIADO : esEnMantenimiento ? ESTADO_COLORS.MANTENIMIENTO : color;
                 const cx = posicion.x * CELL_SIZE;
                 const cy = posicion.y * CELL_SIZE;
                 return (
                   <g
                     key={camion.id}
                     transform={`translate(${cx}, ${cy}) rotate(${rotacion})`}
                     style={{ transition: 'transform 0.8s linear', cursor: 'pointer' }}
                     onMouseEnter={evt => {
                       // Solo mostrar tooltip si no hay modal activo
                       if (!clickedCamion) {
                         setTooltipCamion(camion.id);
                         setTooltipPos({ x: evt.clientX, y: evt.clientY });
                       }
                     }}
                     onMouseMove={evt => {
                       if (!clickedCamion && tooltipCamion === camion.id) {
                         setTooltipPos({ x: evt.clientX, y: evt.clientY });
                       }
                     }}
                     onMouseLeave={() => {
                       setTooltipCamion(null);
                     }}
                     onClick={evt => {
                       // Solo abrir el modal si no hay otro modal ya abierto
                       if (!clickedCamion) {
                         setClickedCamion(camion.id);
                         setClickedPos({ x: evt.clientX, y: evt.clientY });
                         // Ocultar el tooltip de hover
                         setTooltipCamion(null);
                       }
                     }}
                   >
                     <rect x={-6} y={-4} width={12} height={8} rx={2} fill={colorFinal} stroke="black" strokeWidth={0.5} />
                     <circle cx={-4} cy={5} r={1.5} fill="black" />
                     <circle cx={4} cy={5} r={1.5} fill="black" />
                     {esAveriado && (
                       <text x={0} y={-8} textAnchor="middle" fontSize="8" fill="#dc2626" fontWeight="bold">
                         💥
                       </text>
                     )}
                   </g>
                 );
              })}
          </svg>
          </div>
        </div>
      </div>

      {/* Tooltip para camión (hover) */}
      {tooltipCamion && tooltipPos && (
        (() => {
          const camion = camiones.find(c => c.id === tooltipCamion);
          const ruta = rutasCamiones.find(r => r.id === tooltipCamion);
          const numPedidos = ruta?.pedidos?.length || 0;
          
          return (
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
              {camion && (
                <div className="text-xs mb-2">
                  Estado: {camion.estado}<br />
                  Pedidos asignados: {numPedidos}<br />
                  Capacidad GLP: {camion.capacidadActualGLP.toFixed(2)} / {camion.capacidadMaximaGLP}<br />
                  Combustible: {camion.combustibleActual.toFixed(2)} / {camion.combustibleMaximo}<br />
                  Distancia máxima: {camion.distanciaMaxima.toFixed(2)} km<br />
                  Peso carga: {camion.pesoCarga.toFixed(2)}<br />
                  Peso combinado: {camion.pesoCombinado.toFixed(2)}<br />
                  Tara: {camion.tara}<br />
                  Tipo: {camion.tipo}<br />
                  Velocidad: {camion.velocidadPromedio} km/h<br />
                  Ubicación: {camion.ubicacion}<br />
                  Progreso: {camion.porcentaje}
                </div>
              )}
            </div>
          );
        })()
      )}

      {/* Modal para camión (click) */}
      {clickedCamion && clickedPos && (
        (() => {
          const camion = camiones.find(c => c.id === clickedCamion);
          const ruta = rutasCamiones.find(r => r.id === clickedCamion);
          const numPedidos = ruta?.pedidos?.length || 0;
          const esAveriado = camion?.estado === 'Averiado';
          
          return (
            <div
              style={{
                position: 'fixed',
                left: clickedPos.x + 10,
                top: clickedPos.y + 10,
                background: 'white',
                border: '1px solid #ccc',
                borderRadius: 8,
                padding: 12,
                zIndex: 1000,
                boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
              }}
            >
              <div className="mb-2 font-bold">Camión: {clickedCamion}</div>
              {camion && (
                <div className="text-xs mb-2">
                  Estado: {camion.estado}<br />
                  Pedidos asignados: {numPedidos}<br />
                  Capacidad GLP: {camion.capacidadActualGLP.toFixed(2)} / {camion.capacidadMaximaGLP}<br />
                  Combustible: {camion.combustibleActual.toFixed(2)} / {camion.combustibleMaximo}<br />
                  Distancia máxima: {camion.distanciaMaxima.toFixed(2)} km<br />
                  Peso carga: {camion.pesoCarga.toFixed(2)}<br />
                  Peso combinado: {camion.pesoCombinado.toFixed(2)}<br />
                  Tara: {camion.tara}<br />
                  Tipo: {camion.tipo}<br />
                  Velocidad: {camion.velocidadPromedio} km/h<br />
                  Ubicación: {camion.ubicacion}<br />
                  Progreso: {camion.porcentaje}
                </div>
              )}
              {esAveriado ? (
                <div className="text-red-600 font-bold text-center py-2">
                  🚛💥 CAMIÓN AVERIADO
                </div>
              ) : (
                <div className="flex flex-col gap-2">
                  <button
                    className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded disabled:opacity-50"
                    disabled={averiando === clickedCamion + '-1'}
                    onClick={() => handleAveriar(clickedCamion, 1)}
                  >
                    {averiando === clickedCamion + '-1' ? 'Averiando...' : 'Avería tipo 1'}
                  </button>
                  <button
                    className="bg-orange-500 hover:bg-orange-600 text-white px-3 py-1 rounded disabled:opacity-50"
                    disabled={averiando === clickedCamion + '-2'}
                    onClick={() => handleAveriar(clickedCamion, 2)}
                  >
                    {averiando === clickedCamion + '-2' ? 'Averiando...' : 'Avería tipo 2'}
                  </button>
                  <button
                    className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded disabled:opacity-50"
                    disabled={averiando === clickedCamion + '-3'}
                    onClick={() => handleAveriar(clickedCamion, 3)}
                  >
                    {averiando === clickedCamion + '-3' ? 'Averiando...' : 'Avería tipo 3'}
                  </button>
                </div>
              )}
              <button
                className="mt-2 text-gray-500 hover:text-black"
                onClick={() => setClickedCamion(null)}
              >
                Cerrar
              </button>
            </div>
          );
        })()
      )}

      {/* Controles del mapa */}
      <div className="flex items-center gap-4 mt-2 justify-center">
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