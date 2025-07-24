# Control de Averías Automáticas

## Descripción

Esta funcionalidad permite activar o desactivar las averías automáticas en los camiones durante la simulación mediante una constante de configuración. Cuando están activadas, los camiones se marcarán automáticamente como averiados cuando recorran nodos específicos con averías automáticas programadas.

## Funcionalidades

### 1. Control mediante Constante

- **Configuración Simple**: Una constante que controla si las averías automáticas están activadas
- **Sin Interfaz Visual**: No se muestra ningún control en pantalla
- **Fácil Modificación**: Solo cambiar el valor de la constante para activar/desactivar

### 2. Integración con la Simulación

- **Constante Global**: La configuración se mantiene en un archivo de constantes
- **Persistencia**: La configuración se mantiene durante toda la sesión de simulación
- **Tiempo Real**: Los cambios se aplican inmediatamente sin necesidad de reiniciar la simulación

## Componentes Implementados

### 1. constants.ts

Archivo de constantes de configuración que controla las averías automáticas según el tipo de simulación:

```typescript
/**
 * @constant AVERIAS_AUTOMATICAS_POR_TIPO
 * @description Controla si las averías automáticas están activadas según el tipo de simulación
 * 
 * DIARIA: Las averías automáticas están desactivadas (simulación en tiempo real)
 * SEMANAL: Las averías automáticas están activadas (simulación semanal)
 * COLAPSO: Las averías automáticas están activadas (simulación de colapso)
 */
export const AVERIAS_AUTOMATICAS_POR_TIPO = {
  DIARIA: false,    // Desactivadas para simulación diaria
  SEMANAL: true,    // Activadas para simulación semanal
  COLAPSO: true     // Activadas para simulación de colapso
};

/**
 * @function obtenerAveriasAutomaticasActivas
 * @description Obtiene si las averías automáticas están activadas según el tipo de simulación
 */
export const obtenerAveriasAutomaticasActivas = (tipoSimulacion: string): boolean => {
  return AVERIAS_AUTOMATICAS_POR_TIPO[tipoSimulacion as keyof typeof AVERIAS_AUTOMATICAS_POR_TIPO] ?? false;
};
```

**Características:**
- Configuración por tipo de simulación
- Función helper para obtener el estado
- Documentación clara del propósito
- Sin dependencias externas
- Configuración centralizada

### 2. Lógica de Detección Actualizada

La función `detectarAveriaAutomatica` ahora incluye el parámetro de control:

```typescript
export const detectarAveriaAutomatica = (
  camion: CamionEstado,
  ruta: RutaCamion,
  siguientePaso: number,
  averiasAutomaticasActivas: boolean = false
): { debeAveriarse: boolean; tipoAveria?: string }
```

### 3. Lógica de Detección Actualizada

La función `detectarAveriaAutomatica` ahora incluye el parámetro de control:

```typescript
export const detectarAveriaAutomatica = (
  camion: CamionEstado,
  ruta: RutaCamion,
  siguientePaso: number,
  averiasAutomaticasActivas: boolean = false
): { debeAveriarse: boolean; tipoAveria?: string }
```

**Comportamiento:**
- Si `averiasAutomaticasActivas` es `false`, no se detectan averías automáticas
- Si `averiasAutomaticasActivas` es `true`, se detectan normalmente

## Flujo de Funcionamiento

### 1. Configuración por Tipo de Simulación

1. Modificar el objeto `AVERIAS_AUTOMATICAS_POR_TIPO` en `src/config/constants.ts`
2. Configurar cada tipo de simulación según se necesite:
   - `DIARIA: false` - Desactivadas para simulación en tiempo real
   - `SEMANAL: true` - Activadas para simulación semanal
   - `COLAPSO: true` - Activadas para simulación de colapso

### 2. Detección Automática

1. El sistema detecta automáticamente el tipo de simulación actual
2. Se llama a `obtenerAveriasAutomaticasActivas(tipoSimulacion)` para obtener el estado
3. Si está activado para el tipo actual, se detectan nodos con averías automáticas
4. Si se detecta un nodo de avería automática, el camión se marca como "Averiado"
5. Se registra la avería en el backend con las coordenadas exactas

### 3. Estados de los Camiones

- **Normal**: Los camiones funcionan normalmente
- **Averiado**: Los camiones se marcan como averiados cuando recorren nodos de avería automática
- **En Mantenimiento**: Los camiones averiados pueden ser trasladados al taller para reparación

## Integración en el Sistema

La función `obtenerAveriasAutomaticasActivas` se integra en todo el sistema de simulación:

```typescript
// En el contexto de simulación
import { obtenerAveriasAutomaticasActivas } from "../config/constants";

// Se obtiene el estado según el tipo de simulación actual
const averiasActivas = obtenerAveriasAutomaticasActivas(tipoSimulacion);

// Se pasa a la función de avance de hora
await avanzarHoraUtil(
  // ... otros parámetros ...
  averiasActivas
);

// En la lógica de camiones
const { debeAveriarse, tipoAveria } = detectarAveriaAutomatica(
  camion, 
  ruta, 
  siguientePaso, 
  averiasActivas
);
```

## Ventajas

1. **Configuración Simple**: Una sola constante controla toda la funcionalidad
2. **Sin Interfaz Visual**: No interfiere con la interfaz de usuario
3. **Fácil Modificación**: Solo cambiar un valor para activar/desactivar
4. **Configuración Centralizada**: Todas las constantes en un solo archivo
5. **Flexibilidad**: Permite simular escenarios con y sin averías automáticas

## Casos de Uso

### Escenario 1: Simulación Diaria (Tiempo Real)
- Configuración: `DIARIA: false`
- Los camiones funcionan normalmente sin averías automáticas
- Útil para monitoreo en tiempo real sin interrupciones

### Escenario 2: Simulación Semanal
- Configuración: `SEMANAL: true`
- Los camiones se averiarán automáticamente en nodos específicos
- Útil para análisis de robustez del sistema de rutas

### Escenario 3: Simulación de Colapso
- Configuración: `COLAPSO: true`
- Los camiones se averiarán automáticamente en nodos específicos
- Útil para probar escenarios extremos y contingencia

### Escenario 4: Comparación de Escenarios
- Cambiar la configuración por tipo para comparar resultados
- Útil para análisis de impacto de averías en diferentes contextos

## Archivos Modificados

1. **Front-end/src/config/constants.ts** - Nuevo archivo de constantes
2. **Front-end/src/context/simulacion/camionLogic.ts** - Lógica de detección actualizada
3. **Front-end/src/context/simulacion/avanceHora.ts** - Flujo de simulación actualizado
4. **Front-end/src/context/SimulacionContext.tsx** - Contexto actualizado para usar constante

## Próximas Mejoras

1. **Persistencia en LocalStorage**: Guardar el estado de las averías automáticas
2. **Configuración por Tipo**: Controlar diferentes tipos de averías por separado
3. **Estadísticas**: Mostrar estadísticas de averías automáticas
4. **Notificaciones**: Notificaciones cuando se activan averías automáticas 