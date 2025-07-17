import { useEffect, useRef, useState } from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import type { Coordenada } from '../types';
import almacenCentralIcon from '../assets/almacen_central.svg';
import almacenIntermedioIcon from '../assets/almacen_intermedio.svg';
import clienteIcon from '../assets/cliente.svg';

import { CAMION_COLORS, ESTADO_COLORS } from '../config/colors';
import { ChevronDown, ChevronUp } from 'lucide-react';
import { 
  parseCoord, 
  calcularRotacion, 
  getPedidosPendientes,
  handleAveriar 
} from './mapa/utils';
import { formatearCapacidadGLP, formatearCombustible } from '../utils/validacionCamiones';

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





interface MapaProps {
  elementoResaltado?: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null;
}

const Mapa: React.FC<MapaProps> = ({ elementoResaltado }) => {
  const [camionesVisuales, setCamionesVisuales] = useState<CamionVisual[]>([]);
  const [running, setRunning] = useState(false);
  const [intervalo, setIntervalo] = useState(300);
  const intervalRef = useRef<number | null>(null);
  const { 
    camiones, 
    rutasCamiones, 
    almacenes, 
    avanzarHora, 
    cargando, 
    bloqueos, 
    marcarCamionAveriado,  
    iniciarContadorTiempo, 
    setSimulacionActiva, 
    simulacionActiva,
    setPollingActivo,
    horaActual,
    horaSimulacion,
    fechaHoraSimulacion,
    fechaInicioSimulacion,
    diaSimulacion,
    tiempoRealSimulacion,
    tiempoTranscurridoSimulado
  } = useSimulacion();
  // Estado para el tooltip (hover)
  const [tooltipCamion, setTooltipCamion] = useState<string | null>(null);
  const [tooltipPos, setTooltipPos] = useState<{x: number, y: number} | null>(null);
  // Estado para el modal fijo (click)
  const [clickedCamion, setClickedCamion] = useState<string | null>(null);
  const [clickedPos, setClickedPos] = useState<{x: number, y: number} | null>(null);
  const [averiando, setAveriando] = useState<string | null>(null);
  // Estado para la leyenda desplegable
  const [leyendaVisible, setLeyendaVisible] = useState(false);
  // Estados para el modal de almacenes
  const [clickedAlmacen, setClickedAlmacen] = useState<string | null>(null);
  const [clickedAlmacenPos, setClickedAlmacenPos] = useState<{x: number, y: number} | null>(null);

  // DEBUG: Verificar que almacenes llega al componente
  // console.log('🗺️ MAPA: Almacenes recibidos:', almacenes);

  const pedidosPendientes = getPedidosPendientes(rutasCamiones, camiones);
  //console.log('👥 MAPA: Pedidos pendientes (clientes):', pedidosPendientes);
  //console.log('🚚 MAPA: Estado de camiones:', camiones);

  // Removido: useEffect duplicado que causaba conflictos

  useEffect(() => {
    if (running && simulacionActiva) {
      intervalRef.current = window.setInterval(() => {
        avanzarHora();
      }, intervalo);
    } else {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    }
    return () => {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    };
  }, [running, intervalo, avanzarHora, simulacionActiva]);

  useEffect(() => {
    // Rebuild visuals whenever routes or truck states change
    const nuevosVisuales = rutasCamiones.map((info, idx) => {
      // Filtrar valores undefined o null de la ruta
      const rutaValida = info.ruta.filter(nodo => nodo && typeof nodo === 'string');
      const rutaCoords = rutaValida.map(parseCoord);
      
      // Asegurar que hay al menos una coordenada válida
      if (rutaCoords.length === 0) {
        // console.warn('🚨 Ruta vacía para camión:', info.id);
        rutaCoords.push({ x: 0, y: 0 }); // Coordenada por defecto
      }
      
      const estadoCamion = camiones.find(c => c.id === info.id);
      
      // Determinar posición actual y dirección
      let currentPos = rutaCoords[0]; // Posición por defecto
      
      if (estadoCamion && estadoCamion.ubicacion && typeof estadoCamion.ubicacion === 'string') {
        currentPos = parseCoord(estadoCamion.ubicacion);
      }
      
      let rotacion = 0;
      
      if (estadoCamion && rutaCoords.length > 1) {
        const porcentaje = estadoCamion.porcentaje;
        const currentIdx = Math.floor(porcentaje);
        
        // Si hay un siguiente nodo en la ruta, calcular dirección hacia él
        if (currentIdx + 1 < rutaCoords.length) {
          const nextPos = rutaCoords[currentIdx + 1];
          rotacion = calcularRotacion(currentPos, nextPos);
        } else if (currentIdx > 0) {
          // Si estamos en el último nodo, usar la dirección del último movimiento
          const prevPos = rutaCoords[currentIdx - 1];
          rotacion = calcularRotacion(prevPos, currentPos);
        }
      }
      
      // Compute remaining path
      const porcentaje = estadoCamion ? estadoCamion.porcentaje : 0;
      const idxRest = Math.ceil(porcentaje);
      const rutaRestante = rutaCoords.slice(idxRest);
      
      return {
        id: info.id,
        color: CAMION_COLORS[idx % CAMION_COLORS.length],
        ruta: rutaRestante,
        posicion: currentPos,
        rotacion: rotacion
      } as CamionVisual;
    });

    // Eliminar duplicados basándose en el ID
    const visualesUnicos = nuevosVisuales.filter((visual, index, array) => 
      array.findIndex(v => v.id === visual.id) === index
    );

    setCamionesVisuales(visualesUnicos);
  }, [camiones, rutasCamiones]);

  // Función handleAveriar movida a mapa/utils/averias.ts

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
                <svg width="16" height="12" viewBox="0 0 16 12" className="border border-gray-300 rounded">
                  <rect x="2" y="4" width="10" height="4" rx="0.5" fill="#3b82f6" stroke="black" strokeWidth="0.3" />
                  <rect x="10" y="5" width="3" height="2" rx="0.3" fill="#3b82f6" stroke="black" strokeWidth="0.3" />
                  <circle cx="4" cy="9" r="1" fill="black" />
                  <circle cx="8" cy="9" r="1" fill="black" />
                  <circle cx="11" cy="9" r="1" fill="black" />
                  <polygon points="13,6 12,5.5 12,6.5" fill="white" stroke="black" strokeWidth="0.2" />
                </svg>
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
              onClick={(evt) => {
                // Cerrar modales si se hace click en un área vacía del mapa
                if (evt.target === evt.currentTarget) {
                  setClickedCamion(null);
                  setClickedAlmacen(null);
                }
              }}
            >
            {/* Fondo invisible para capturar clicks */}
            <rect 
              x={0} 
              y={0} 
              width={SVG_WIDTH} 
              height={SVG_HEIGHT} 
              fill="transparent"
              onClick={() => {
                setClickedCamion(null);
                setClickedAlmacen(null);
              }}
            />
            
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
              const esResaltado = elementoResaltado?.tipo === 'pedido' && elementoResaltado?.id === pedido.codigo;
              return (
                <g key={pedido.codigo}>
                  {/* Círculo de resaltado para pedidos */}
                  {esResaltado && (
                    <circle
                      cx={pedido.coordenada.x * CELL_SIZE}
                      cy={pedido.coordenada.y * CELL_SIZE}
                      r={25}
                      fill="none"
                      stroke="#f59e0b"
                      strokeWidth={3}
                      strokeDasharray="4 2"
                      opacity={0.8}
                    >
                      <animate
                        attributeName="r"
                        values="20;30;20"
                        dur="2s"
                        repeatCount="indefinite"
                      />
                    </circle>
                  )}
                  
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
              // console.log('🏪 MAPA: Renderizando almacén:', almacen.nombre, 'en posición:', almacen.coordenada);
              const esResaltado = elementoResaltado?.tipo === 'almacen' && elementoResaltado?.id === almacen.id;
              return (
                <g key={almacen.id} style={{ cursor: 'pointer' }}>
                  {/* Círculo de resaltado para almacenes */}
                  {esResaltado && (
                    <circle
                      cx={almacen.coordenada.x * CELL_SIZE}
                      cy={almacen.coordenada.y * CELL_SIZE}
                      r={30}
                      fill="none"
                      stroke="#10b981"
                      strokeWidth={3}
                      strokeDasharray="6 3"
                      opacity={0.8}
                    >
                      <animate
                        attributeName="r"
                        values="25;35;25"
                        dur="2s"
                        repeatCount="indefinite"
                      />
                      <animate
                        attributeName="opacity"
                        values="0.6;1;0.6"
                        dur="2s"
                        repeatCount="indefinite"
                      />
                    </circle>
                  )}
                  
                  <image
                    href={almacen.tipo === 'CENTRAL' ? almacenCentralIcon : almacenIntermedioIcon}
                    x={almacen.coordenada.x * CELL_SIZE - 20}
                    y={almacen.coordenada.y * CELL_SIZE - 20}
                    width={40}
                    height={40}
                    onClick={evt => {
                      if (!clickedAlmacen) {
                        setClickedAlmacen(almacen.id);
                        setClickedAlmacenPos({ x: evt.clientX, y: evt.clientY });
                        // Actualizar información de almacenes para tener datos frescos
                        
                      }
                    }}
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
                    onClick={evt => {
                      if (!clickedAlmacen) {
                        setClickedAlmacen(almacen.id);
                        setClickedAlmacenPos({ x: evt.clientX, y: evt.clientY });
                        // Actualizar información de almacenes para tener datos frescos
                     
                      }
                    }}
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
              .map((camion, index) => (
                <polyline
                  key={`ruta-${camion.id}-${index}`}
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
              .map((camion, index) => {
                 const estadoCamion = camiones.find(c => c.id === camion.id);
                 const esAveriado = estadoCamion?.estado === 'Averiado';
                 const esEnMantenimiento = estadoCamion?.estado === 'En Mantenimiento';
                 const esResaltado = elementoResaltado?.tipo === 'camion' && elementoResaltado?.id === camion.id;
                 const { posicion, rotacion, color } = camion;
                 // Rojo para averiados, negro para mantenimiento, color original para otros estados
                 const colorFinal = esAveriado ? ESTADO_COLORS.AVERIADO : esEnMantenimiento ? ESTADO_COLORS.MANTENIMIENTO : color;
                 const cx = posicion.x * CELL_SIZE;
                 const cy = posicion.y * CELL_SIZE;
                 return (
                   <g key={`camion-${camion.id}-${index}`}>
                     <g
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
                       {/* Círculo de resaltado que se mueve con el camión */}
                       {esResaltado && (
                         <circle
                           key={`resaltado-${camion.id}`}
                           cx={0}
                           cy={0}
                           r={25}
                           fill="none"
                           stroke="#f59e0b"
                           strokeWidth={3}
                           strokeDasharray="8 4"
                           opacity={0.9}
                           style={{ transition: 'all 0.8s linear' }}
                         >
                           <animateTransform
                             attributeName="transform"
                             type="rotate"
                             values="0 0 0;360 0 0"
                             dur="4s"
                             repeatCount="indefinite"
                           />
                           <animate
                             attributeName="opacity"
                             values="0.7;1;0.7"
                             dur="1.5s"
                             repeatCount="indefinite"
                           />
                         </circle>
                       )}
                       
                       {/* Cuerpo principal del camión */}
                       <rect key={`cuerpo-${camion.id}`} x={-8} y={-3} width={16} height={6} rx={1} fill={colorFinal} stroke="black" strokeWidth={0.5} />
                       
                       {/* Cabina del camión (frente) */}
                       <rect key={`cabina-${camion.id}`} x={6} y={-2} width={4} height={4} rx={0.5} fill={colorFinal} stroke="black" strokeWidth={0.5} />
                       
                       {/* Ruedas */}
                       <circle key={`rueda1-${camion.id}`} cx={-5} cy={4} r={1.5} fill="black" />
                       <circle key={`rueda2-${camion.id}`} cx={2} cy={4} r={1.5} fill="black" />
                       <circle key={`rueda3-${camion.id}`} cx={7} cy={4} r={1.5} fill="black" />
                       
                       {/* Indicador de dirección (flecha) */}
                       <polygon 
                         key={`flecha-${camion.id}`}
                         points="10,0 8,-1.5 8,1.5" 
                         fill="white" 
                         stroke="black" 
                         strokeWidth={0.3}
                       />
                       
                       {/* Líneas de detalle del camión */}
                       <line key={`linea1-${camion.id}`} x1={-6} y1={-1} x2={4} y2={-1} stroke="black" strokeWidth={0.3} opacity={0.6} />
                       <line key={`linea2-${camion.id}`} x1={-6} y1={1} x2={4} y2={1} stroke="black" strokeWidth={0.3} opacity={0.6} />
                       
                       {esAveriado && (
                         <text key={`averia-${camion.id}`} x={0} y={-10} textAnchor="middle" fontSize="8" fill="#dc2626" fontWeight="bold">
                           💥
                         </text>
                       )}
                     </g>
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
                  Capacidad GLP: {formatearCapacidadGLP(camion.capacidadActualGLP, camion.capacidadMaximaGLP)}<br />
                  Combustible: {formatearCombustible(camion.combustibleActual, camion.combustibleMaximo)}<br />
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
                  Capacidad GLP: {formatearCapacidadGLP(camion.capacidadActualGLP, camion.capacidadMaximaGLP)}<br />
                  Combustible: {formatearCombustible(camion.combustibleActual, camion.combustibleMaximo)}<br />
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
                    onClick={() => handleAveriar(clickedCamion, 1, marcarCamionAveriado, setAveriando, setClickedCamion, setSimulacionActiva, {
                      horaActual,
                      horaSimulacion,
                      fechaHoraSimulacion,
                      fechaInicioSimulacion,
                      diaSimulacion,
                      tiempoRealSimulacion,
                      tiempoTranscurridoSimulado,
                      camiones,
                      rutasCamiones,
                      almacenes,
                      bloqueos
                    }, setPollingActivo)}
                  >
                    {averiando === clickedCamion + '-1' ? 'Averiando...' : 'Avería tipo 1'}
                  </button>
                  <button
                    className="bg-orange-500 hover:bg-orange-600 text-white px-3 py-1 rounded disabled:opacity-50"
                    disabled={averiando === clickedCamion + '-2'}
                    onClick={() => handleAveriar(clickedCamion, 2, marcarCamionAveriado, setAveriando, setClickedCamion, setSimulacionActiva, {
                      horaActual,
                      horaSimulacion,
                      fechaHoraSimulacion,
                      fechaInicioSimulacion,
                      diaSimulacion,
                      tiempoRealSimulacion,
                      tiempoTranscurridoSimulado,
                      camiones,
                      rutasCamiones,
                      almacenes,
                      bloqueos
                    }, setPollingActivo)}
                  >
                    {averiando === clickedCamion + '-2' ? 'Averiando...' : 'Avería tipo 2'}
                  </button>
                  <button
                    className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded disabled:opacity-50"
                    disabled={averiando === clickedCamion + '-3'}
                    onClick={() => handleAveriar(clickedCamion, 3, marcarCamionAveriado, setAveriando, setClickedCamion, setSimulacionActiva, {
                      horaActual,
                      horaSimulacion,
                      fechaHoraSimulacion,
                      fechaInicioSimulacion,
                      diaSimulacion,
                      tiempoRealSimulacion,
                      tiempoTranscurridoSimulado,
                      camiones,
                      rutasCamiones,
                      almacenes,
                      bloqueos
                    }, setPollingActivo)}
                  >
                    {averiando === clickedCamion + '-3' ? 'Averiando...' : 'Avería tipo 3'}
                  </button>
                </div>
              )}
              <button
                className="mt-2 bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded w-full"
                onClick={() => {
                  // Aquí se activa el menú inferior para mostrar la ruta
                  const event = new CustomEvent('mostrarRutaCamion', { detail: { camionId: clickedCamion } });
                  window.dispatchEvent(event);
                  setClickedCamion(null);
                }}
              >
                📍 Mostrar ruta del camión
              </button>
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

      {/* Modal para almacén (click) */}
      {clickedAlmacen && clickedAlmacenPos && (
        (() => {
          const almacen = almacenes.find(a => a.id === clickedAlmacen);
          
          if (!almacen) return null;

          const porcentajeGLP = almacen.capacidadMaximaGLP > 0 
            ? (almacen.capacidadActualGLP / almacen.capacidadMaximaGLP) * 100 
            : 0;

          // Calcular posición del modal para que se vea completo
          const modalWidth = 280;
          const modalHeight = 180;
          const viewportWidth = window.innerWidth;
          const viewportHeight = window.innerHeight;
          
          let modalLeft = clickedAlmacenPos.x + 10;
          let modalTop = clickedAlmacenPos.y + 10;
          
          // Ajustar horizontalmente si se sale de la pantalla
          if (modalLeft + modalWidth > viewportWidth) {
            modalLeft = clickedAlmacenPos.x - modalWidth - 10;
          }
          
          // Ajustar verticalmente si se sale de la pantalla
          if (modalTop + modalHeight > viewportHeight) {
            modalTop = clickedAlmacenPos.y - modalHeight - 10;
            // Si aún se sale por arriba, centrarlo verticalmente respecto al click
            if (modalTop < 0) {
              modalTop = Math.max(10, clickedAlmacenPos.y - modalHeight / 2);
            }
          }

          return (
            <div
              className="fixed bg-white border border-gray-300 rounded-lg shadow-lg z-50 overflow-hidden"
              style={{
                left: modalLeft,
                top: modalTop,
                width: modalWidth,
                maxHeight: modalHeight
              }}
              onClick={(e) => e.stopPropagation()}
            >
              {/* Header */}
              <div className="p-3 bg-blue-600 text-white">
                <div className="font-bold text-sm">{almacen.nombre}</div>
                <div className="text-xs opacity-90">
                  {almacen.tipo === 'CENTRAL' ? 'Almacén Central' : 'Almacén Secundario'} • ({almacen.coordenada.x}, {almacen.coordenada.y})
                </div>
              </div>
              
              {/* Content */}
              <div className="p-3">
                {/* Estado */}
                <div className="mb-3 text-center">
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    almacen.activo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  }`}>
                    {almacen.activo ? '🟢 Activo' : '🔴 Inactivo'}
                  </span>
                </div>
                
                {/* GLP Info */}
                <div className="mb-3">
                  <div className="flex justify-between items-center mb-1">
                    <span className="text-xs font-medium text-gray-700">Gas Licuado (GLP)</span>
                    <span className="text-xs font-bold text-blue-600">{porcentajeGLP.toFixed(1)}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2 mb-2">
                    <div 
                      className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${Math.min(100, Math.max(0, porcentajeGLP))}%` }}
                    ></div>
                  </div>
                  <div className="text-xs text-gray-600 text-center">
                    {almacen.capacidadActualGLP.toFixed(1)} / {almacen.capacidadMaximaGLP.toFixed(1)} m³
                  </div>
                </div>

                {/* Info adicional para almacenes secundarios */}
                {almacen.tipo === 'SECUNDARIO' && (
                  <div className="mb-3 p-2 bg-green-50 rounded text-center">
                    <div className="text-xs text-green-700">
                      🔄 Recarga automática a las 00:00
                    </div>
                  </div>
                )}
              </div>

              {/* Footer */}
              <div className="p-2 bg-gray-50 border-t">
                <button
                  className="w-full bg-gray-500 hover:bg-gray-600 text-white py-1 px-3 rounded text-xs transition-colors"
                  onClick={() => setClickedAlmacen(null)}
                >
                  Cerrar
                </button>
              </div>
            </div>
          );
        })()
      )}

      {/* Controles del mapa */}
      <div className="flex items-center gap-4 mt-2 justify-center">
        <button
          onClick={() => {
            if (!running) {
              // Solo iniciar el contador cuando se presiona "Iniciar" por primera vez
              iniciarContadorTiempo();
            }
            setRunning(prev => !prev);
          }}
          className={`px-4 py-1 rounded text-white ${
            !simulacionActiva && running 
              ? 'bg-yellow-500 hover:bg-yellow-600' 
              : 'bg-blue-500 hover:bg-blue-600'
          }`}
        >
          {!simulacionActiva && running 
            ? 'Pausado (Avería)' 
            : running 
              ? 'Pausar' 
              : 'Iniciar'
          }
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