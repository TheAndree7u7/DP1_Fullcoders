# Control de Velocidad de Simulación

## Resumen

Este documento explica cómo se maneja la velocidad de simulación en diferentes tipos de simulación (tiempo real, semanal y colapso) y las mejoras implementadas para unificar el control de velocidad.

## Problema Identificado

Anteriormente existía una inconsistencia en el manejo de la velocidad de simulación:

- **Simulación en tiempo real**: Usaba `62.9` segundos por nodo (configurable)
- **Simulación semanal/colapso**: Usaba `36` segundos por nodo (hardcodeado)

Esto causaba confusión y comportamientos inconsistentes entre diferentes tipos de simulación.

## Solución Implementada

### 1. Constantes Unificadas

Se crearon funciones en `src/context/simulacion/types.ts` para manejar diferentes velocidades:

```typescript
// Constante base para simulación semanal/colapso
export const SEGUNDOS_POR_NODO = (HORAS_POR_ACTUALIZACION * 60 * 60) / NODOS_PARA_ACTUALIZACION; // 36 segundos

// Función para obtener velocidad según tipo de simulación
export const obtenerSegundosPorNodoSegunTipo = (tipoSimulacion: string): number => {
  switch (tipoSimulacion) {
    case 'DIARIA':
      return SEGUNDOS_POR_NODO; // 36 segundos (1 hora / 100 nodos)
    case 'SEMANAL':
      return 0.30;
    case 'COLAPSO':
      return SEGUNDOS_POR_NODO; // Fijo: 36 segundos
    default:
      return SEGUNDOS_POR_NODO;
  }
};

// Función para calcular intervalo según tipo
export const calcularIntervaloSegunTipo = (
  tipoSimulacion: string,
  segundosPorNodoPersonalizado?: number,
  velocidadCamion?: number
): number => {
  const segundosPorNodo = obtenerSegundosPorNodoSegunTipo(tipoSimulacion);
  
  if (tipoSimulacion === 'DIARIA' && segundosPorNodoPersonalizado) {
    return calcularIntervaloTiempoReal(segundosPorNodoPersonalizado, velocidadCamion);
  }
  
  return calcularIntervaloTiempoReal(segundosPorNodo, velocidadCamion);
};
```

### 2. Detección Automática del Tipo de Simulación

El componente `Mapa` detecta automáticamente el tipo de simulación basado en la URL:

```typescript
useEffect(() => {
  const detectarTipoSimulacion = () => {
    const pathname = window.location.pathname;
    if (pathname.includes('/simulacion-semanal') || pathname.includes('/colapso-logistico')) {
      setTipoSimulacion(pathname.includes('/simulacion-semanal') ? 'SEMANAL' : 'COLAPSO');
      const segundosFijos = obtenerSegundosPorNodoSegunTipo(tipoSimulacion);
      setSegundosPorNodo(segundosFijos);
    } else {
      setTipoSimulacion('DIARIA');
    }
  };

  detectarTipoSimulacion();
}, []);
```

### 3. Interfaz Adaptativa

El componente `ControlVelocidad` se adapta según el tipo de simulación:

- **Simulación DIARIA**: Velocidad configurable con controles completos
- **Simulación SEMANAL/COLAPSO**: Velocidad fija con controles deshabilitados

### 4. Corrección de Hardcodeos

Se eliminaron los valores hardcodeados de `36` segundos en:
- `src/context/simulacion/avanceHora.ts`
- Otros archivos que usaban valores fijos

## Tipos de Simulación y Velocidades

### 1. Simulación DIARIA (Tiempo Real)
- **Velocidad por defecto**: 36 segundos por nodo (1 hora / 100 nodos)
- **Configuración**: Completamente configurable
- **Controles disponibles**:
  - Input manual de segundos por nodo
  - Presets de velocidad (Lento, Normal, Rápido, Muy Rápido)
  - Modo dinámico basado en velocidad de camiones
  - Ajuste dinámico según velocidad promedio

### 2. Simulación SEMANAL
- **Velocidad fija**: 36 segundos por nodo
- **Configuración**: No configurable
- **Controles**: Solo informativos, deshabilitados
- **Propósito**: Análisis semanal con velocidad consistente

### 3. Simulación COLAPSO
- **Velocidad fija**: 36 segundos por nodo
- **Configuración**: No configurable
- **Controles**: Solo informativos, deshabilitados
- **Propósito**: Análisis de escenarios de colapso

## Archivos Modificados

### 1. `src/context/simulacion/types.ts`
- Agregadas funciones para manejo de velocidades por tipo
- Nueva función `obtenerSegundosPorNodoSegunTipo()`
- Nueva función `calcularIntervaloSegunTipo()`

### 2. `src/context/simulacion/avanceHora.ts`
- Corregido hardcodeo de `36` segundos
- Uso de constante `SEGUNDOS_POR_NODO`

### 3. `src/components/Mapa.tsx`
- Detección automática del tipo de simulación
- Uso de funciones unificadas para cálculo de intervalos
- Interfaz adaptativa según tipo de simulación

### 4. `src/components/ControlVelocidad.tsx`
- Nueva prop `tipoSimulacion`
- Interfaz adaptativa según tipo
- Controles condicionales
- Información visual del tipo de simulación

## Beneficios de la Implementación

1. **Consistencia**: Velocidades unificadas y predecibles
2. **Claridad**: Interfaz clara sobre qué controles están disponibles
3. **Mantenibilidad**: Código más limpio sin hardcodeos
4. **Flexibilidad**: Fácil agregar nuevos tipos de simulación
5. **Experiencia de usuario**: Controles apropiados según el contexto

## Uso

### Para Desarrolladores

```typescript
// Obtener velocidad según tipo
const velocidad = obtenerSegundosPorNodoSegunTipo('DIARIA'); // 62.9
const velocidad = obtenerSegundosPorNodoSegunTipo('SEMANAL'); // 36

// Calcular intervalo
const intervalo = calcularIntervaloSegunTipo('DIARIA', 30); // Con velocidad personalizada
const intervalo = calcularIntervaloSegunTipo('SEMANAL'); // Con velocidad fija
```

### Para Usuarios

1. **Simulación Diaria**: Usar controles de velocidad para ajustar la velocidad
2. **Simulación Semanal/Colapso**: La velocidad es fija y no configurable
3. **Información**: El panel de control muestra claramente el tipo de simulación

## Consideraciones Futuras

- Agregar más tipos de simulación si es necesario
- Implementar persistencia de configuraciones de velocidad
- Agregar métricas de rendimiento por tipo de simulación
- Considerar velocidades diferentes para diferentes escenarios 