# Diccionario de Rutas de Camiones

## Descripción

El sistema ahora incluye un diccionario completo de rutas de camiones que permite acceder fácilmente a la información detallada de cada nodo en la ruta de cada camión, incluyendo coordenadas y tipos de nodos.

## Estructura del Diccionario

### Interfaces Principales

```typescript
interface NodoRutaCompleto {
  coordenada: Coordenada;  // { x: number, y: number }
  tipo: string;           // Tipo de nodo (NORMAL, PEDIDO, AVERIA_AUTOMATICA_T1, etc.)
  indice: number;         // Posición en la ruta
}

interface RutaCamionCompleta {
  idCamion: string;
  ruta: NodoRutaCompleto[];
  puntoDestino: Coordenada;
  pedidos: Pedido[];
}

interface DiccionarioRutasCamiones {
  [idCamion: string]: RutaCamionCompleta;
}
```

## Tipos de Nodos Disponibles

Según el backend, los tipos de nodos disponibles son:

- `NORMAL` - Nodo normal de ruta
- `PEDIDO` - Nodo donde se entrega un pedido
- `ALMACEN` - Nodo de almacén
- `ALMACEN_RECARGA` - Nodo de almacén para recarga
- `CAMION_AVERIADO` - Nodo donde hay un camión averiado
- `AVERIA_AUTOMATICA_T1` - Nodo con avería automática tipo 1
- `AVERIA_AUTOMATICA_T2` - Nodo con avería automática tipo 2
- `AVERIA_AUTOMATICA_T3` - Nodo con avería automática tipo 3

## Uso en el Contexto

### Acceso al Diccionario

```typescript
import { useSimulacion } from "../context/SimulacionContext";

const { diccionarioRutasCamiones } = useSimulacion();
```

### Funciones Disponibles

#### 1. Verificar si un camión está en un nodo de avería

```typescript
const { verificarCamionEnNodoAveria } = useSimulacion();

// Verificar si el camión está actualmente en un nodo de avería
const estaEnNodoAveria = verificarCamionEnNodoAveria("CAMION_001", 0.5);
// Retorna: true si está en nodo de avería, false en caso contrario
```

#### 2. Obtener todos los nodos de avería en la ruta de un camión

```typescript
const { obtenerNodosAveriaEnRuta } = useSimulacion();

// Obtener todos los nodos de avería en la ruta del camión
const nodosAveria = obtenerNodosAveriaEnRuta("CAMION_001");
// Retorna: Array de NodoRutaCompleto con nodos de avería
```

## Ejemplos de Uso

### Ejemplo 1: Obtener información completa de la ruta de un camión

```typescript
const { diccionarioRutasCamiones } = useSimulacion();

const rutaCamion = diccionarioRutasCamiones["CAMION_001"];
if (rutaCamion) {
  console.log(`Camión ${rutaCamion.idCamion} tiene ${rutaCamion.ruta.length} nodos`);
  
  // Mostrar todos los nodos con sus tipos
  rutaCamion.ruta.forEach((nodo, index) => {
    console.log(`Nodo ${index}: (${nodo.coordenada.x},${nodo.coordenada.y}) - Tipo: ${nodo.tipo}`);
  });
}
```

### Ejemplo 2: Detectar camiones en nodos de avería

```typescript
const { diccionarioRutasCamiones, camiones, verificarCamionEnNodoAveria } = useSimulacion();

const camionesEnAveria = camiones.filter(camion => {
  return verificarCamionEnNodoAveria(camion.id, camion.porcentaje);
});

console.log(`Camiones en nodos de avería: ${camionesEnAveria.length}`);
```

### Ejemplo 3: Analizar rutas con averías

```typescript
const { diccionarioRutasCamiones, obtenerNodosAveriaEnRuta } = useSimulacion();

Object.entries(diccionarioRutasCamiones).forEach(([idCamion, rutaCompleta]) => {
  const nodosAveria = obtenerNodosAveriaEnRuta(idCamion);
  
  if (nodosAveria.length > 0) {
    console.log(`Camión ${idCamion} tiene ${nodosAveria.length} nodos de avería:`);
    nodosAveria.forEach(nodo => {
      console.log(`  - Posición ${nodo.indice}: (${nodo.coordenada.x},${nodo.coordenada.y}) - ${nodo.tipo}`);
    });
  }
});
```

### Ejemplo 4: Crear un mapa de calor de nodos de avería

```typescript
const { diccionarioRutasCamiones, obtenerNodosAveriaEnRuta } = useSimulacion();

const mapaCalorAverias = new Map<string, number>();

Object.keys(diccionarioRutasCamiones).forEach(idCamion => {
  const nodosAveria = obtenerNodosAveriaEnRuta(idCamion);
  
  nodosAveria.forEach(nodo => {
    const coordenada = `${nodo.coordenada.x},${nodo.coordenada.y}`;
    mapaCalorAverias.set(coordenada, (mapaCalorAverias.get(coordenada) || 0) + 1);
  });
});

// Mostrar coordenadas con más averías
const coordenadasMasAfectadas = Array.from(mapaCalorAverias.entries())
  .sort((a, b) => b[1] - a[1])
  .slice(0, 5);

console.log("Coordenadas con más averías:", coordenadasMasAfectadas);
```

## Ventajas del Nuevo Sistema

1. **Acceso Directo**: Acceso inmediato a la información completa de rutas por ID de camión
2. **Información Detallada**: Cada nodo incluye coordenadas, tipo e índice
3. **Funciones Utilitarias**: Funciones predefinidas para casos de uso comunes
4. **Detección de Averías**: Fácil identificación de camiones en nodos de avería
5. **Análisis Completo**: Permite análisis detallado de rutas y patrones

## Integración con el Backend

El diccionario se genera automáticamente a partir de los datos que llegan del backend en la estructura:

```json
{
  "cromosoma": [
    {
      "camion": {
        "codigo": "TA01",
        "estado": "DISPONIBLE",
        "tipo": "TA",
        "fila": 8,
        "columna": 12,
        "capacidadMaximaGLP": 1000,
        "capacidadActualGLP": 800,
        "tara": 5.5,
        "pesoCarga": 2.3,
        "pesoCombinado": 7.8,
        "combustibleMaximo": 200,
        "combustibleActual": 150,
        "velocidadPromedio": 70,
        "distanciaMaxima": 500
      },
      "nodos": [
        {
          "coordenada": { "x": 12, "y": 8 },
          "tipo": "NORMAL"
        },
        {
          "coordenada": { "x": 13, "y": 8 },
          "tipo": "AVERIA_AUTOMATICA_T1"
        },
        {
          "coordenada": { "x": 14, "y": 9 },
          "tipo": "PEDIDO"
        }
      ],
      "destino": { "x": 15, "y": 10 },
      "pedidos": [
        {
          "codigo": "P001",
          "coordenada": { "x": 14, "y": 9 },
          "volumenGLPAsignado": 50,
          "estado": "PLANIFICADO"
        }
      ]
    }
  ]
}
```

## Notas Importantes

- El diccionario se actualiza automáticamente cada vez que se reciben nuevos datos del backend
- Las funciones de verificación usan el porcentaje de avance actual del camión
- Los tipos de nodos corresponden exactamente a los definidos en el enum `TipoNodo` del backend
- El sistema es compatible con el sistema de averías automáticas existente

## Troubleshooting

### Problemas Comunes

1. **El diccionario está vacío:**
   - Verificar que se hayan recibido datos del backend
   - Comprobar que `rutasCamiones` tenga datos antes de generar el diccionario
   - Revisar la consola para errores de parsing de coordenadas

2. **Errores de tipos de nodos:**
   - Verificar que `tiposNodos` esté presente en las rutas
   - Los tipos deben coincidir con los del enum `TipoNodo`

3. **Funciones no detectan averías:**
   - Verificar que el porcentaje de avance del camión sea correcto
   - Comprobar que los tipos de nodos incluyan `AVERIA_AUTOMATICA_T1`, `T2`, o `T3`

### Componente de Prueba

Se incluye un componente `TestDiccionarioRutas` para verificar el funcionamiento:

```typescript
import TestDiccionarioRutas from '../components/TestDiccionarioRutas';

// Usar en cualquier vista para debug
<TestDiccionarioRutas />
```

Este componente muestra:
- Total de camiones con rutas
- Camiones con nodos de avería
- Camiones actualmente en nodo de avería
- Estructura completa del diccionario 