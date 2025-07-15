import type { Coordenada } from '../types';
import almacenCentralIcon from '../assets/almacen_central.svg';
import almacenIntermedioIcon from '../assets/almacen_intermedio.svg';
import clienteIcon from '../assets/cliente.svg';


// Tipos locales para elementos del mapa
export interface CamionVisual {
  id: string;
  color: string;
  ruta: Coordenada[];
  posicion: Coordenada;
  rotacion: number;
  // Campos opcionales para animación
  posicionInterpolada?: Coordenada;
  enMovimiento?: boolean;
}

export interface Pedido {
  codigo: string;
  coordenada: Coordenada;
}

export interface Almacen {
  id: string;
  nombre: string;
  tipo: string;
  coordenada: Coordenada;
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
}

export interface Bloqueo {
  coordenadas: Coordenada[];
}

// Nueva interfaz de props, ahora incluye todos los arrays necesarios
interface MapaProps {
  elementoResaltado?: { tipo: 'camion' | 'pedido' | 'almacen'; id: string } | null;
  camiones: CamionVisual[];
  pedidos: Pedido[];
  almacenes: Almacen[];
  bloqueos: Bloqueo[];
}

// Componente para la leyenda
const Legend: React.FC = () => (
  <div className="bg-white rounded-lg shadow-md border border-gray-200 p-2 w-32 flex-shrink-0">
    <span className="flex items-center justify-between w-full text-left text-xs font-semibold text-gray-800 mb-2">
      LEYENDA
    </span>
    <div className="space-y-1.5">
      {/* Se reutiliza código similar para cada ítem */}
      <div className="flex items-center gap-1.5">
        <img src={almacenCentralIcon} alt="Almacén Central" className="w-4 h-4" />
        <span className="text-xs text-gray-700">A. Central</span>
      </div>
      <div className="flex items-center gap-1.5">
        <img src={almacenIntermedioIcon} alt="Almacén Intermedio" className="w-4 h-4" />
        <span className="text-xs text-gray-700">A. Intermedio</span>
      </div>
      <div className="flex items-center gap-1.5">
        <img src={clienteIcon} alt="Cliente" className="w-4 h-4" />
        <span className="text-xs text-gray-700">Cliente</span>
      </div>
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
      <div className="flex items-center gap-1.5">
        <div className="w-4 h-0.5 border-t border-dashed border-blue-500"></div>
        <span className="text-xs text-gray-700">Ruta</span>
      </div>
      <div className="flex items-center gap-1.5">
        <div className="w-4 h-0.5 bg-red-600 rounded-full"></div>
        <span className="text-xs text-gray-700">Bloqueos</span>
      </div>
    </div>
  </div>
);

// Componente para renderizar el grid
const Grid: React.FC = () => (
  <>
    {[...Array(GRID_WIDTH + 1)].map((_, i) => (
      <line key={`v-${i}`} x1={i * CELL_SIZE} y1={0} x2={i * CELL_SIZE} y2={SVG_HEIGHT} stroke="#d1d5db" strokeWidth={1} />
    ))}
    {[...Array(GRID_HEIGHT + 1)].map((_, i) => (
      <line key={`h-${i}`} x1={0} y1={i * CELL_SIZE} x2={SVG_WIDTH} y2={i * CELL_SIZE} stroke="#d1d5db" strokeWidth={1} />
    ))}
  </>
);

// Componente que agrupa los elementos SVG (bloqueos, pedidos, almacenes, rutas y camiones)
const SVGItems: React.FC<{
  elementoResaltado?: MapaProps['elementoResaltado'];
  camiones: CamionVisual[];
  pedidos: Pedido[];
  almacenes: Almacen[];
  bloqueos: Bloqueo[];
}> = ({ elementoResaltado, camiones, pedidos, almacenes, bloqueos }) => (
  <>
    {/* Bloqueos */}
    {bloqueos.map((bloqueo, idx) => (
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
    {/* Pedidos */}
    {pedidos.map(pedido => {
      const isHighlighted = elementoResaltado?.tipo === 'pedido' && elementoResaltado.id === pedido.codigo;
      return (
        <g key={pedido.codigo}>
          {isHighlighted && (
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
              <animate attributeName="r" values="20;30;20" dur="2s" repeatCount="indefinite" />
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
      const isHighlighted = elementoResaltado?.tipo === 'almacen' && elementoResaltado.id === almacen.id;
      return (
        <g key={almacen.id} style={{ cursor: 'pointer' }}>
          {isHighlighted && (
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
              <animate attributeName="r" values="25;35;25" dur="2s" repeatCount="indefinite" />
              <animate attributeName="opacity" values="0.6;1;0.6" dur="2s" repeatCount="indefinite" />
            </circle>
          )}
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
    {camiones.map((camion, idx) => (
      <polyline
        key={`ruta-${camion.id}-${idx}`}
        fill="none"
        stroke={camion.color}
        strokeWidth={2}
        strokeDasharray="4 2"
        points={camion.ruta.map(p => `${p.x * CELL_SIZE},${p.y * CELL_SIZE}`).join(' ')}
      />
    ))}
    {/* Camiones */}
    {camiones.map((camion, idx) => {
      const isHighlighted = elementoResaltado?.tipo === 'camion' && elementoResaltado.id === camion.id;
      // Usar posición interpolada si está disponible, sino usar posición base
      const posicionActual = camion.posicionInterpolada || camion.posicion;
      const cx = posicionActual.x * CELL_SIZE;
      const cy = posicionActual.y * CELL_SIZE;
      return (
        <g key={`camion-${camion.id}-${idx}`} transform={`translate(${cx}, ${cy}) rotate(${camion.rotacion})`}>
          {isHighlighted && (
            <circle
              cx={0}
              cy={0}
              r={25}
              fill="none"
              stroke="#f59e0b"
              strokeWidth={3}
              strokeDasharray="8 4"
              opacity={0.9}
            >
              <animateTransform attributeName="transform" type="rotate" values="0 0 0;360 0 0" dur="4s" repeatCount="indefinite" />
              <animate attributeName="opacity" values="0.7;1;0.7" dur="1.5s" repeatCount="indefinite" />
            </circle>
          )}
          <rect x={-8} y={-3} width={16} height={6} rx={1} fill={camion.color} stroke="black" strokeWidth={0.5} />
          <rect x={6} y={-2} width={4} height={4} rx={0.5} fill={camion.color} stroke="black" strokeWidth={0.5} />
          <circle cx={-5} cy={4} r={1.5} fill="black" />
          <circle cx={2} cy={4} r={1.5} fill="black" />
          <circle cx={7} cy={4} r={1.5} fill="black" />
          <polygon points="10,0 8,-1.5 8,1.5" fill="white" stroke="black" strokeWidth={0.3} />
          <line x1={-6} y1={-1} x2={4} y2={-1} stroke="black" strokeWidth={0.3} opacity={0.6} />
          <line x1={-6} y1={1} x2={4} y2={1} stroke="black" strokeWidth={0.3} opacity={0.6} />
        </g>
      );
    })}
  </>
);

const GRID_WIDTH = 70;
const GRID_HEIGHT = 50;
const CELL_SIZE = 14;
const SVG_WIDTH = GRID_WIDTH * CELL_SIZE;
const SVG_HEIGHT = GRID_HEIGHT * CELL_SIZE;
const BLOQUEO_STROKE_WIDTH = 4;

const Mapa: React.FC<MapaProps> = ({ elementoResaltado, camiones, pedidos, almacenes, bloqueos }) => {
  console.log("🗺️ Componente Mapa recibió props:", {
    camiones: camiones?.length || 0,
    pedidos: pedidos?.length || 0,
    almacenes: almacenes?.length || 0,
    bloqueos: bloqueos?.length || 0,
    samples: {
      camion: camiones?.[0],
      pedido: pedidos?.[0],
      almacen: almacenes?.[0]
    }
  });

  return (
    <div className="w-full h-full flex flex-col">
      <div className="flex items-start gap-3 flex-1">
        <Legend />
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
              <rect x={0} y={0} width={SVG_WIDTH} height={SVG_HEIGHT} fill="transparent" />
              <Grid />
              <SVGItems elementoResaltado={elementoResaltado} camiones={camiones} pedidos={pedidos} almacenes={almacenes} bloqueos={bloqueos} />
            </svg>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Mapa;