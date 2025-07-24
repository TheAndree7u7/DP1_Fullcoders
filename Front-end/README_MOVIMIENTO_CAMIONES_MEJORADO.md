# Mejoras en el Movimiento y Rotación de Camiones

## Problema Identificado

El sistema anterior tenía los siguientes problemas en el movimiento de camiones:

1. **Movimiento "saltado"**: Los camiones se movían directamente de nodo a nodo sin interpolación
2. **Rotación abrupta**: Los cambios de dirección eran instantáneos y poco naturales
3. **Falta de suavidad**: El movimiento no se veía fluido en el mapa reticular

## Solución Implementada

### 1. Interpolación de Posición

Se implementó un sistema de interpolación lineal que calcula la posición exacta del camión entre nodos basándose en su porcentaje de progreso. Se agregó suavizado (easing) para hacer el movimiento más natural y evitar variaciones bruscas.

#### Funciones Nuevas:

- **`interpolarPosicion(from, to, factor)`**: Interpola entre dos coordenadas usando un factor (0.0 a 1.0)
- **`calcularPosicionInterpolada(rutaCoords, porcentaje)`**: Calcula la posición interpolada del camión en la ruta
- **`calcularPosicionInterpoladaMejorada(rutaCoords, porcentaje)`**: Calcula la posición interpolada con suavizado para movimiento más natural

#### Ejemplo de Interpolación:

```typescript
// Si el camión está en porcentaje 1.5, significa que está entre el nodo 1 y 2
// con un factor de interpolación de 0.5
const posicion = calcularPosicionInterpolada(rutaCoords, 1.5);
// Resultado: posición exacta a la mitad del camino entre nodo 1 y 2
```

### 2. Rotación Mejorada (Algoritmo de Camino Más Corto)

Se implementó un sistema de rotación basado en el algoritmo de camino más corto que:

- **Separa el cálculo del ángulo de la animación**
- **Usa direcciones cardinales absolutas** (0°, 90°, 180°, 270°)
- **Calcula el delta más corto** entre ángulos para evitar giros innecesarios
- **Implementa look-ahead** para iniciar el giro antes de llegar a la esquina
- **Elimina completamente el "baile"** de los camiones

#### Funciones Nuevas:

- **`direction(from, to)`**: Determina la dirección cardinal entre dos puntos
- **`shortestDelta(angleA, angleB)`**: Calcula el delta más corto entre dos ángulos (-180 a +180)
- **`buildSteps(path)`**: Construye pasos de ruta con ángulos absolutos
- **`calcularAnguloInicial(rutaCoords, anguloInicialForzado?)`**: Calcula el ángulo inicial del camión
- **`calcularRotacionOptimizada(rutaCoords, porcentaje, anguloActual)`**: Calcula rotación usando el algoritmo de camino más corto
- **`calcularRotacionConLookAhead(rutaCoords, porcentaje, anguloActual, anguloInicialForzado?)`**: Calcula rotación con look-ahead para giros suaves

#### Lógica de Rotación (Algoritmo de Camino Más Corto):

```typescript
// 1. Calcular ángulo inicial
const anguloInicial = calcularAnguloInicial(rutaCoords, anguloInicialForzado);

// 2. Mapear cada tramo a su ángulo cardinal
const HEADING = { E: 0, S: 90, W: 180, N: 270 };

// 3. Calcular delta con shortestDelta
const delta = shortestDelta(anguloActual, anguloObjetivo);

// 4. Aplicar el delta más corto
const nuevoAngulo = anguloActual + delta;

// 5. Look-ahead: iniciar giro a mitad del segmento actual
if (factorInterpolacion >= 0.5) {
  // Empezar a girar hacia el siguiente ángulo
}
```

#### Cálculo de Ángulo Inicial:

```typescript
// Caso 1: Ruta con ≥2 puntos - inferir dirección inicial
if (rutaCoords.length >= 2) {
  return direction(rutaCoords[0], rutaCoords[1]);
}

// Caso 2: Ángulo forzado - usar el proporcionado
if (anguloInicialForzado !== undefined) {
  return anguloInicialForzado;
}

// Caso 3: Por defecto - mirar al Este (0°)
return 0;
```

### 3. Transiciones CSS Mejoradas

Se optimizaron las transiciones CSS para un movimiento más natural:

```css
transition: transform 0.6s cubic-bezier(0.4, 0.0, 0.2, 1)
will-change: transform
```

#### Beneficios:

- **Curva de aceleración suave**: `cubic-bezier(0.4, 0.0, 0.2, 1)` proporciona una aceleración/desaceleración natural
- **Duración optimizada**: 0.6s es el tiempo ideal para el movimiento en tiempo real
- **Optimización de rendimiento**: `will-change: transform` mejora el rendimiento de las animaciones

## Archivos Modificados

### 1. `Front-end/src/components/mapa/utils/camiones.ts`

**Funciones Agregadas:**
- `interpolarPosicion()`
- `calcularPosicionInterpolada()`
- `calcularPosicionInterpoladaMejorada()`
- `direction()` - Dirección cardinal entre puntos
- `shortestDelta()` - Delta más corto entre ángulos
- `buildSteps()` - Construir pasos de ruta
- `calcularAnguloInicial()` - Cálculo de ángulo inicial del camión
- `calcularRotacionOptimizada()` - Algoritmo de camino más corto
- `calcularRotacionConLookAhead()` - Rotación con look-ahead

### 2. `Front-end/src/components/mapa/utils/index.ts`

**Exportaciones Agregadas:**
- Todas las nuevas funciones de interpolación y rotación

### 3. `Front-end/src/components/Mapa.tsx`

**Cambios Principales:**
- Reemplazo de la lógica de posición fija por interpolación con suavizado
- Implementación del algoritmo de camino más corto para rotaciones
- Sistema de look-ahead para giros suaves en esquinas
- **Cálculo inteligente de ángulo inicial** basado en la ruta
- Estado de ángulos por camión para tracking consistente
- Mejora de transiciones CSS

## Resultados Esperados

### Antes:
- ✅ Camiones se mueven de nodo a nodo
- ❌ Movimiento "saltado" y poco natural
- ❌ Rotación abrupta
- ❌ Transiciones lineales

### Después:
- ✅ Movimiento fluido y natural
- ✅ Interpolación suave entre nodos
- ✅ Rotación gradual y realista
- ✅ Transiciones con aceleración natural
- ✅ Mejor experiencia visual

## Configuración Técnica

### Parámetros de Interpolación:
- **Factor de interpolación**: Calculado como `porcentaje - Math.floor(porcentaje)`
- **Límites**: Factor siempre entre 0.0 y 1.0
- **Precisión**: Posición calculada con precisión de punto flotante
- **Suavizado**: Función de easing cuadrática para movimiento más natural

### Parámetros de Transición:
- **Duración**: 0.6 segundos
- **Función de temporización**: `cubic-bezier(0.4, 0.0, 0.2, 1)`
- **Propiedades**: Solo `transform` para optimizar rendimiento

## Compatibilidad

- ✅ Compatible con todas las velocidades de simulación
- ✅ Funciona con todos los tipos de simulación (DIARIA, SEMANAL, COLAPSO)
- ✅ Mantiene sincronización con la línea de ruta
- ✅ Compatible con el sistema de averías automáticas

## Pruebas Recomendadas

1. **Movimiento Básico**: Verificar que los camiones se muevan suavemente
2. **Cambios de Dirección**: Confirmar que las rotaciones sean graduales
3. **Diferentes Velocidades**: Probar con distintas velocidades de simulación
4. **Rutas Complejas**: Verificar comportamiento en rutas con muchos cambios de dirección
5. **Rendimiento**: Monitorear que no haya degradación de rendimiento

## Corrección de Problemas de Consistencia

### Problema Identificado:
- Movimiento inconsistente cuando los camiones van de derecha a izquierda vs izquierda a derecha
- Variaciones en la rotación que causaban movimientos "saltados"
- **"Baile" de camiones**: Los camiones giraban 270° en lugar de 90° cuando cambiaban de dirección
- **Falta de separación entre cálculo de ángulo y animación**

### Solución Implementada:
- **Separación de cálculo y animación**: El ángulo se calcula independientemente de la animación
- **Direcciones cardinales absolutas**: Uso de ángulos fijos (0°, 90°, 180°, 270°) para consistencia
- **Algoritmo de camino más corto**: `shortestDelta()` calcula siempre el giro más corto
- **Look-ahead**: Los giros inician a mitad del segmento actual para suavidad
- **Cálculo inteligente de ángulo inicial**: El camión empieza mirando en la dirección correcta de su ruta
- **Estado de ángulos por camión**: Tracking consistente del ángulo actual de cada camión
- **Eliminación completa del "baile"**: No más giros de 270° en lugar de 90°

## Casos de Uso del Ángulo Inicial

### 1. **Ruta con ≥2 puntos** (Inferencia automática)
```typescript
// El camión empieza mirando hacia donde va a moverse
const ruta = [{x:4,y:2}, {x:4,y:5}, {x:1,y:5}];
// Resultado: El camión empieza mirando al Sur (90°) porque va de (4,2) a (4,5)
```

### 2. **Ruta de 1 solo punto** (Ángulo forzado)
```typescript
// El camión ya está en pantalla y necesitamos especificar su orientación
const ruta = [{x:7,y:3}];
const anguloInicial = 270; // Mirar al Norte
// Resultado: El camión empieza mirando al Norte (270°)
```

### 3. **Ruta de 1 solo punto** (Por defecto)
```typescript
// Si no se especifica ángulo inicial, usa 0° (Este)
const ruta = [{x:5,y:5}];
// Resultado: El camión empieza mirando al Este (0°)
```

## Notas de Implementación

- La interpolación se calcula en tiempo real en cada frame
- Las funciones están optimizadas para evitar cálculos innecesarios
- Se mantiene compatibilidad con el sistema existente
- Los logs de debug están disponibles para troubleshooting
- Se asegura consistencia en todas las direcciones de movimiento
- **El ángulo inicial se calcula una sola vez** al inicio del movimiento del camión 