# Funcionalidad de Paquetes en Simulación

## Descripción General

Se ha implementado una funcionalidad completa para mostrar paquetes en pantalla durante la simulación. La funcionalidad sigue las mejores prácticas de Clean Code y arquitectura MVVM, separando responsabilidades entre contextos, servicios y componentes.

## Arquitectura Implementada

### 1. Servicios (API Layer)
- **`simulacionApiService.ts`**: 
  - `obtenerMejorIndividuo(fecha: string)`: Llama al endpoint `/simulacion/mejor?fecha=YYYY-MM-DDTHH:MM:SS`
  - Manejo de errores robusto con tipos específicos
  - Reintento automático para conexiones fallidas

### 2. Contexto (State Management)
- **`PaqueteContext.tsx`**: Contexto especializado para manejo de paquetes
  - Estado: `paqueteActual`, `isLoading`, `error`, `ultimaActualizacion`
  - Acciones: `setPaquete`, `setLoading`, `setError`, `limpiarPaquete`
  - Reducer pattern para actualizaciones de estado predecibles

### 3. Hooks Personalizados (ViewModel)
- **`usePaqueteContext.ts`**: Hook principal y hooks especializados
  - `usePaquete()`: Hook principal
  - `usePaqueteState()`: Solo estado
  - `usePaqueteActions()`: Solo acciones
  - `usePaqueteInfo()`: Información derivada (totales, fechas)

- **`usePaqueteService.ts`**: Lógica de negocio para paquetes
  - `cargarPaquete(fecha)`: Carga un paquete específico
  - `cargarPaqueteConReintento()`: Carga con reintentos automáticos

### 4. Componentes (View Layer)
- **`PaqueteDisplay.tsx`**: Componente reutilizable para mostrar paquetes
  - Variantes: `compact`, `detailed`, `card`
  - Estados: loading, error, vacío, éxito
  - Responsive y accesible

- **`CargadorPaquete.tsx`**: Componente para carga manual de paquetes
  - Validación de fechas
  - Feedback visual del estado
  - Manejo de errores específicos

## Flujo de Funcionamiento

### 1. Inicio de Simulación
```typescript
// En ControlSimulacion.tsx
const handleIniciar = async () => {
  // 1. Iniciar simulación
  const respuesta = await iniciarSimulacionApi(fechaFormateada);
  
  // 2. Cargar paquete automáticamente
  await cargarPaquete(fechaFormateada);
}
```

### 2. Obtención del Paquete
```typescript
// En usePaqueteService.ts
const cargarPaquete = async (fecha: string) => {
  const paquete = await obtenerMejorIndividuo(fecha);
  setPaquete(paquete);
}
```

### 3. Visualización en Pantalla
```typescript
// En SimulacionDiaria.tsx
<PaqueteDisplay variant="compact" showHeader={false} />
<PaqueteDisplay variant="detailed" showHeader={true} />
```

## Componentes de UI

### PaqueteDisplay
```typescript
interface PaqueteDisplayProps {
  variant?: 'compact' | 'detailed' | 'card';
  showHeader?: boolean;
  className?: string;
}
```

**Variantes:**
- `compact`: Muestra solo información básica (ej: "5 camiones • 12 pedidos")
- `detailed`: Información completa con métricas y fechas
- `card`: Diseño tipo tarjeta con visualización destacada

### Estados Manejados
1. **Loading**: Skeleton loaders durante carga
2. **Error**: Mensajes de error específicos con estilo visual distintivo
3. **Vacío**: Mensaje cuando no hay paquete disponible
4. **Éxito**: Datos del paquete con información relevante

## Datos del Paquete

### Estructura del Individuo (Paquete)
```typescript
interface Individuo {
  cromosoma: Gen[];
  fechaHoraSimulacion: string;
  fechaHoraInicioIntervalo?: string;
  fechaHoraFinIntervalo?: string;
  tipoIndividuo: TipoIndividuo;
  fitness?: number;
}
```

### Información Extraída
- **Total de camiones**: `cromosoma.length`
- **Total de pedidos**: Suma de pedidos en todos los genes
- **Fecha de simulación**: `fechaHoraSimulacion`
- **Rutas**: Coordenadas y destinos de cada camión

## Integración con el Mapa

Los pedidos del paquete se integran automáticamente con el componente Mapa:

```typescript
const pedidosMapa = paqueteActual?.cromosoma?.flatMap(gen => 
  gen.pedidos?.map(pedido => ({
    codigo: pedido.codigo,
    coordenada: pedido.coordenada
  })) || []
) || [];
```

## Configuración Necesaria

### 1. Variables de Entorno
```env
VITE_API_BASE_URL=http://localhost:8085/api
```

### 2. Providers en App.tsx
```typescript
<SimulacionProvider>
  <PaqueteProvider>
    {/* Aplicación */}
  </PaqueteProvider>
</SimulacionProvider>
```

## Manejo de Errores

### Tipos de Errores Manejados
1. **Conexión**: "Error de conexión: No se pudo conectar con el servidor"
2. **Servidor**: Errores HTTP con códigos específicos
3. **Datos**: "La respuesta del servidor no es JSON válido"
4. **Vacío**: Estado 204 - No Content

### Estrategias de Recuperación
- Reintentos automáticos con backoff exponencial
- Fallback a estados anteriores
- Mensajes de error específicos para el usuario
- Carga manual como alternativa

## Características de Rendimiento

### Optimizaciones Implementadas
1. **useCallback**: Para funciones que se pasan a componentes hijos
2. **Contextos separados**: Evita re-renders innecesarios
3. **Estados derivados**: Cálculos memoizados en hooks
4. **Lazy loading**: Componentes se renderizan solo cuando es necesario

### Patrón de Actualización
```typescript
// Automática después de iniciar simulación
iniciarSimulacion() → cargarPaquete() → mostrarEnMapa()

// Manual por usuario
fechaSeleccionada → cargarPaquete() → mostrarEnMapa()
```

## Próximas Mejoras Sugeridas

1. **Polling automático**: Actualización periódica de paquetes
2. **Cache inteligente**: Evitar llamadas innecesarias
3. **Histórico de paquetes**: Navegación entre diferentes momentos
4. **Exportar datos**: Funcionalidad para guardar información del paquete
5. **Métricas avanzadas**: KPIs calculados en tiempo real

## Pruebas Recomendadas

1. **Caso exitoso**: Iniciar simulación y verificar carga automática de paquete
2. **Carga manual**: Usar el componente CargadorPaquete con diferentes fechas
3. **Manejo de errores**: Simular fallos de conexión y errores del servidor
4. **Estados vacíos**: Verificar comportamiento sin paquetes disponibles
5. **Responsive**: Probar en diferentes tamaños de pantalla
