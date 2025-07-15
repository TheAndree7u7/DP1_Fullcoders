# Visualización de Elementos del Mapa - Documentación Técnica

## Descripción General

Se ha implementado un sistema completo para mostrar automáticamente en el mapa todos los elementos del paquete obtenido (almacenes, pedidos, camiones y sus rutas). La implementación sigue principios MVVM y Clean Code con separación clara de responsabilidades.

## Arquitectura Implementada

### 1. Contexto de Mapa (Model/State Management)
- **`MapaContext.tsx`**: Contexto especializado para datos del mapa
- **`MapaContextDefinition.ts`**: Definición separada del contexto para Fast Refresh
- **Responsabilidades**:
  - Transformar datos del paquete a formato compatible con el mapa
  - Calcular rutas, colores y estados de los elementos
  - Gestionar estado de carga y errores específicos del mapa

### 2. Hooks Especializados (ViewModel)
- **`useMapaContext.ts`**: Conjunto de hooks para interacción con el contexto
  - `useMapa()`: Hook principal
  - `useMapaState()`: Solo estado del mapa
  - `useMapaActions()`: Solo acciones del mapa
  - `useMapaStats()`: Estadísticas calculadas
  - `useMapaElementos()`: Elementos formateados para el mapa

### 3. Componentes de Visualización (View)
- **`EstadisticasMapa.tsx`**: Estadísticas en tiempo real
- **`RutasCamiones.tsx`**: Visualización detallada de rutas
- Integración en **`SimulacionDiaria.tsx`**

## Transformación de Datos

### Flujo de Datos
```
Paquete (Individuo) → MapaContext → Elementos Transformados → Componente Mapa
```

### Transformaciones Implementadas

#### 1. Camiones
```typescript
interface CamionMapa {
  id: string;
  codigo: string;
  color: string;        // Calculado por estado
  ruta: Coordenada[];   // Extraída de gen.rutaFinal
  posicion: Coordenada; // Posición actual del camión
  rotacion: number;     // Calculada hacia el destino
  destino: Coordenada | null;
  pedidosAsignados: string[];
  estado: string;
  progreso: number;     // Progreso en la ruta (0-100)
}
```

**Lógica de Colores por Estado:**
- `DISPONIBLE`: Verde (#10B981)
- `EN_RUTA`: Azul (#3B82F6)
- `ENTREGANDO_GLP_A_CLIENTE`: Púrpura (#8B5CF6)
- `EN_MANTENIMIENTO`: Gris (#6B7280)
- `SIN_COMBUSTIBLE`: Rojo (#EF4444)

#### 2. Pedidos
```typescript
interface PedidoMapa {
  codigo: string;
  coordenada: Coordenada;
  estado: string;
  volumenGLP: number;
  horasLimite: number;
  camionAsignado: string | null; // ID del camión asignado
}
```

**Extracción desde el Paquete:**
- Se obtienen de `paquete.cromosoma[].pedidos[]`
- Se elimina duplicación usando Map con código como clave
- Se asigna el camión correspondiente de cada gen

#### 3. Almacenes
```typescript
interface AlmacenMapa {
  id: string;
  nombre: string;
  tipo: string;
  coordenada: Coordenada;
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
  capacidadCombustible: number;
  capacidadMaximaCombustible: number;
  esCentral: boolean;
}
```

#### 4. Bloqueos
```typescript
interface BloqueoMapa {
  coordenadas: Coordenada[];
  activo: boolean;
  fechaInicio: string;
  fechaFin: string;
}
```

## Componentes de UI

### EstadisticasMapa
**Funcionalidad:**
- Muestra estadísticas en tiempo real de camiones y pedidos
- Dos variantes: `compact` y `detailed`
- Cálculos automáticos de progreso y distribución por estado

**Métricas Calculadas:**
- Total de camiones y distribución por estado
- Total de pedidos: asignados vs sin asignar
- Porcentaje de progreso de asignación
- Estados de loading, error y datos

### RutasCamiones
**Funcionalidad:**
- Lista expandible de todos los camiones con sus rutas
- Información detallada: posición, destino, progreso, pedidos asignados
- Visualización de ruta paso a paso (primeros 10 puntos)
- Integración con selección de camiones en el mapa

**Características:**
- Colores por estado de camión
- Progreso visual con barras
- Formato compacto para coordenadas
- Scroll independiente para listas largas

## Integración Automática

### Actualización en Tiempo Real
```typescript
// En MapaContext.tsx
useEffect(() => {
  if (paqueteActual && almacenes.length > 0) {
    // Transformar datos automáticamente
    actualizarDatosMapa(paqueteActual, almacenes, bloqueosTransformados);
  }
}, [paqueteActual, almacenes, bloqueos, actualizarDatosMapa]);
```

### Flujo de Actualización
1. **Usuario inicia simulación** → `ControlSimulacion`
2. **Se obtiene el paquete** → `usePaqueteService`
3. **El contexto detecta cambios** → `MapaContext`
4. **Se transforman los datos** → Funciones de transformación
5. **El mapa se actualiza** → `useMapaElementos`
6. **Las estadísticas se recalculan** → `useMapaStats`

## Estados Manejados

### 1. Loading States
- **Contexto de paquetes**: Mientras se obtiene el paquete
- **Contexto de mapa**: Mientras se transforman los datos
- **Componentes**: Skeleton loaders específicos

### 2. Error States
- **Errores de conexión**: No se puede obtener el paquete
- **Errores de transformación**: Datos del paquete inválidos
- **Errores de renderizado**: Problemas en los componentes

### 3. Empty States
- **Sin paquete**: No hay datos para mostrar
- **Sin camiones**: Paquete sin cromosoma válido
- **Sin rutas**: Camiones sin rutas definidas

## Optimizaciones de Rendimiento

### 1. Memoización
```typescript
// En MapaContext.tsx
const transformarCamiones = useCallback((paquete: Individuo): CamionMapa[] => {
  // Lógica de transformación memoizada
}, []);
```

### 2. Contextos Separados
- **SimulacionContext**: Estado general de simulación
- **PaqueteContext**: Estado específico de paquetes
- **MapaContext**: Estado específico del mapa
- Evita re-renders innecesarios

### 3. Hooks Especializados
- Separación de responsabilidades
- Reutilización en múltiples componentes
- Cálculos optimizados

## Configuración y Uso

### 1. Estructura de Providers
```tsx
<SimulacionProvider>
  <PaqueteProvider>
    <MapaProvider>
      {/* Aplicación */}
    </MapaProvider>
  </PaqueteProvider>
</SimulacionProvider>
```

### 2. Uso en Componentes
```tsx
// Obtener elementos para el mapa
const elementosMapa = useMapaElementos();

// Usar en el componente Mapa
<Mapa 
  camiones={elementosMapa.camiones}
  pedidos={elementosMapa.pedidos}
  almacenes={elementosMapa.almacenes}
  bloqueos={elementosMapa.bloqueos}
/>
```

### 3. Estadísticas y Rutas
```tsx
// Estadísticas compactas en la barra superior
<EstadisticasMapa variant="compact" className="text-white" />

// Rutas detalladas en panel expandible
<RutasCamiones 
  camionSeleccionado={camionId}
  onCamionSeleccionado={setCamionId}
/>
```

## Casos de Uso Cubiertos

### 1. Carga Inicial
- Usuario inicia simulación
- Se carga paquete automáticamente
- Elementos aparecen en mapa instantáneamente

### 2. Carga Manual
- Usuario selecciona fecha específica
- Se carga paquete para esa fecha
- Mapa se actualiza con nuevos datos

### 3. Exploración Interactiva
- Usuario expande panel de control
- Ve estadísticas detalladas en tiempo real
- Explora rutas de camiones específicos

### 4. Selección de Elementos
- Usuario hace clic en camión en el mapa
- Se resalta en la lista de rutas
- Se muestra información detallada

## Próximas Mejoras Sugeridas

### 1. Funcionalidades Avanzadas
- **Animación de rutas**: Mostrar progreso animado de camiones
- **Filtros avanzados**: Por estado, tipo de pedido, etc.
- **Búsqueda**: Encontrar camiones/pedidos específicos
- **Exportación**: Guardar rutas y estadísticas

### 2. Optimizaciones
- **Virtualización**: Para listas muy largas de camiones
- **Lazy loading**: Cargar rutas bajo demanda
- **Cache inteligente**: Evitar recálculos innecesarios
- **WebWorkers**: Procesamiento en background

### 3. Visualización Mejorada
- **Heatmaps**: Densidad de pedidos por área
- **Métricas adicionales**: Tiempo estimado, eficiencia
- **Comparación temporal**: Entre diferentes paquetes
- **Alertas**: Problemas detectados automáticamente

## Debugging y Troubleshooting

### Problemas Comunes
1. **Mapa vacío**: Verificar que el paquete tenga cromosoma válido
2. **Rutas no aparecen**: Verificar que los genes tengan rutaFinal
3. **Estadísticas incorrectas**: Verificar transformación de datos
4. **Rendimiento lento**: Verificar cantidad de elementos y optimizaciones

### Herramientas de Debug
- React DevTools para contextos
- Console logs en transformaciones
- Network tab para APIs
- Performance tab para optimizaciones
