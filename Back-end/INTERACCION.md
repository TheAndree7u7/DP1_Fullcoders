# Interacción Frontend-Backend

## Descripción General
Este documento detalla la interacción entre el frontend (React/TypeScript) y el backend (Java/Spring Boot) del sistema de simulación de rutas de camiones.

## Endpoints y Servicios

### 1. Simulación API
**Base URL**: `http://localhost:8085/api/simulacion`

#### Endpoints:
- `GET /mejor`
  - **Propósito**: Obtiene el mejor individuo (solución) para la simulación
  - **Headers Requeridos**:
    ```
    Accept: application/json
    Content-Type: application/json
    ```
  - **Respuesta**: Objeto `Individuo` que contiene:
    ```typescript
    interface Individuo {
      cromosoma: {
        camion: {
          codigo: string;
          capacidad: number;
        };
        nodos: Array<{
          coordenada: {
            x: number;
            y: number;
          };
          tipo: string;
        }>;
        destino: {
          x: number;
          y: number;
        };
        pedidos: Array<{
          id: string;
          origen: {
            x: number;
            y: number;
          };
          destino: {
            x: number;
            y: number;
          };
          peso: number;
          volumen: number;
          prioridad: number;
        }>;
      }[];
      fitness: number;
    }
    ```
  - **Códigos de Respuesta**:
    - 200: Éxito
    - 204: No hay datos disponibles
    - 400: Error en la solicitud
    - 500: Error interno del servidor

### 2. Almacenes API
**Base URL**: `http://localhost:8085/api/almacenes`

#### Endpoints:
- `GET /`
  - **Propósito**: Obtiene la lista de almacenes disponibles
  - **Headers Requeridos**:
    ```
    Accept: application/json
    Content-Type: application/json
    ```
  - **Respuesta**: Array de objetos `AlmacenBackend`
    ```typescript
    interface AlmacenBackend {
      coordenada: {
        fila: number;    // Coordenada Y en el sistema del backend
        columna: number; // Coordenada X en el sistema del backend
      };
      nombre: string;
      tipo: 'CENTRAL' | 'SECUNDARIO';
      activo: boolean;
    }
    ```
  - **Transformación Frontend**:
    ```typescript
    interface Almacen {
      id: string;        // Nombre transformado a formato URL
      nombre: string;    // Nombre original
      tipo: 'CENTRAL' | 'INTERMEDIO';
      coordenada: {
        x: number;       // Columna del backend
        y: number;       // Fila del backend
      };
      activo: boolean;
    }
    ```

## Flujo de Datos Detallado

### 1. Inicialización del Sistema
1. **Carga de Almacenes**
   - Frontend hace petición GET a `/api/almacenes`
   - Backend responde con lista de almacenes
   - Frontend transforma coordenadas (fila,columna) → (x,y)
   - Se almacena en estado global mediante React Context

2. **Carga de Simulación Inicial**
   - Frontend hace petición GET a `/api/simulacion/mejor`
   - Backend procesa y devuelve el mejor individuo
   - Frontend transforma datos:
     ```typescript
     // Transformación de rutas
     const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen) => ({
       id: gen.camion.codigo,
       ruta: gen.nodos.map(n => `(${n.coordenada.x},${n.coordenada.y})`),
       puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
       pedidos: gen.pedidos,
     }));
     ```

### 2. Simulación en Tiempo Real
1. **Actualización de Estado Local**
   ```typescript
   const nuevosCamiones = camiones.map((camion) => {
     const ruta = rutasCamiones.find(r => r.id === camion.id);
     if (!ruta) return camion;

     const siguientePaso = camion.porcentaje + INCREMENTO_PORCENTAJE;
     const rutaLength = ruta.ruta.length;
     const nuevaUbicacion = ruta.ruta[siguientePaso];

     if (siguientePaso >= rutaLength) {
       // Verificar si realmente está entregado (todos los pedidos + en almacén central + GLP vacío)
       const todosLosPedidosEntregados = ruta.pedidos.every(pedido => {
         const indicePedidoEnRuta = ruta.ruta.findIndex(nodo => 
           parseCoord(nodo).x === pedido.coordenada.x && 
           parseCoord(nodo).y === pedido.coordenada.y
         );
         return indicePedidoEnRuta <= siguientePaso;
       });
       
       const estaEnAlmacenCentral = nuevaUbicacion === '(0,0)' || nuevaUbicacion === '(0, 0)';
       const glpVacio = camion.capacidadActualGLP <= 0.1;
       
       if (todosLosPedidosEntregados && estaEnAlmacenCentral && glpVacio) {
         return { ...camion, estado: 'Entregado', porcentaje: rutaLength - 1 };
       } else {
         return { ...camion, estado: 'En Camino', porcentaje: rutaLength - 1, ubicacion: nuevaUbicacion };
       }
     }

     return {
       ...camion,
       porcentaje: siguientePaso,
       ubicacion: nuevaUbicacion,
     };
   });
   ```

2. **Sincronización con Backend**
   - Se mantiene contador de nodos restantes: `NODOS_PARA_ACTUALIZACION = 50`
   - Cada hora de simulación decrementa el contador
   - Cuando contador llega a 0:
     - Se marca estado como `esperandoActualizacion`
     - Se actualiza estado local
     - Se incrementa hora actual
     - Se solicita nueva actualización al backend

## Estructura de Datos Detallada

### Frontend
```typescript
// Estado de un camión en la simulación
interface CamionEstado {
  id: string;           // Identificador único del camión
  ubicacion: string;    // Coordenadas actuales en formato "(x,y)"
  porcentaje: number;   // Progreso de la ruta (0-100)
  estado: 'En Camino' | 'Entregado';
}

// Ruta completa de un camión
interface RutaCamion {
  id: string;           // Identificador del camión
  ruta: string[];       // Array de coordenadas que forman la ruta
  puntoDestino: string; // Coordenadas del punto final
  pedidos: Pedido[];    // Lista de pedidos asignados
}

// Pedido individual
interface Pedido {
  id: string;
  origen: {
    x: number;
    y: number;
  };
  destino: {
    x: number;
    y: number;
  };
  peso: number;
  volumen: number;
  prioridad: number;
}
```

### Backend
```typescript
// Estructura de almacén en el backend
interface AlmacenBackend {
  coordenada: {
    fila: number;    // Coordenada Y
    columna: number; // Coordenada X
  };
  nombre: string;
  tipo: 'CENTRAL' | 'SECUNDARIO';
  activo: boolean;
}

// Estructura de individuo en el backend
interface IndividuoBackend {
  cromosoma: {
    camion: {
      codigo: string;
      capacidad: number;
    };
    nodos: Array<{
      coordenada: {
        x: number;
        y: number;
      };
      tipo: string;
    }>;
    destino: {
      x: number;
      y: number;
    };
    pedidos: Array<{
      id: string;
      origen: {
        x: number;
        y: number;
      };
      destino: {
        x: number;
        y: number;
      };
      peso: number;
      volumen: number;
      prioridad: number;
    }>;
  }[];
  fitness: number;
}
```

## Manejo de Errores Detallado

### Frontend
1. **Validación de Respuestas**
   ```typescript
   if (!contentType || !contentType.includes("application/json")) {
     throw new Error("La respuesta del servidor no es JSON válido");
   }
   ```

2. **Manejo de Estados Vacíos**
   ```typescript
   if (response.status === 204) {
     throw new Error("No hay datos disponibles en este momento");
   }
   ```

3. **Logging Detallado**
   ```typescript
   console.log("Respuesta recibida:", {
     status: response.status,
     statusText: response.statusText,
     headers: Object.fromEntries(response.headers.entries())
   });
   ```

### Backend
1. **Validación de Entradas**
   - Verificación de tipos de datos
   - Validación de rangos de coordenadas
   - Comprobación de existencia de recursos

2. **Manejo de Excepciones**
   - Excepciones específicas para cada tipo de error
   - Respuestas HTTP apropiadas
   - Mensajes de error descriptivos

## Sincronización Detallada

### Sistema de Actualización
1. **Contador de Nodos**
   ```typescript
   const NODOS_PARA_ACTUALIZACION = 50;
   const INCREMENTO_PORCENTAJE = 1;
   ```

2. **Control de Estado**
   ```typescript
   const [nodosRestantesAntesDeActualizar, setNodosRestantesAntesDeActualizar] = useState<number>(NODOS_PARA_ACTUALIZACION);
   const [esperandoActualizacion, setEsperandoActualizacion] = useState<boolean>(false);
   ```

3. **Lógica de Actualización**
   ```typescript
   if (quedan <= 0) {
     setEsperandoActualizacion(true);
     setCamiones(nuevosCamiones);
     setHoraActual(prev => prev + 1);
     await cargarDatos(false);
   }
   ```

## Consideraciones Técnicas Detalladas

### 1. CORS
```typescript
// Configuración necesaria en el backend
@CrossOrigin(origins = "http://localhost:8085")
```

### 2. Transformación de Coordenadas
```typescript
// Backend a Frontend
const coordenadaFrontend = {
  x: coordenadaBackend.columna,
  y: coordenadaBackend.fila
};

// Frontend a Backend
const coordenadaBackend = {
  fila: coordenadaFrontend.y,
  columna: coordenadaFrontend.x
};
```

### 3. Gestión de Estado
- Uso de React Context para estado global
- Estados locales para componentes específicos
- Memoización de cálculos costosos

### 4. Optimización de Rendimiento
- Actualización por lotes de nodos
- Caché de datos frecuentemente accedidos
- Minimización de re-renders
- Debouncing de actualizaciones 