# Recálculo de Algoritmo Genético después de Averías

## Descripción

Esta funcionalidad permite recalcular automáticamente el algoritmo genético después de que ocurra una avería en un camión, usando la fecha y hora actual de la simulación para obtener una nueva planificación optimizada.

## Flujo de Funcionamiento

### 1. Ocurre una Avería
- El usuario hace clic en un camión y selecciona el tipo de avería (1, 2, o 3)
- Se pausa inmediatamente la simulación
- Se detiene el polling de paquetes
- Se marca el camión como averiado en el contexto local

### 2. Captura del Estado
- Se captura el estado completo de la simulación en el momento de la avería
- Se incluye información de camiones, rutas, almacenes, bloqueos y tiempo
- Se envía la avería al backend con el estado completo

### 3. Recálculo del Algoritmo
- Se calcula la fecha y hora exacta actual de la simulación
- Se llama al endpoint `/api/simulacion/mejor` con esta fecha
- El backend ejecuta el algoritmo genético considerando:
  - El camión averiado (no disponible)
  - Los pedidos pendientes
  - Los bloqueos activos
  - La fecha actual de simulación

### 4. Aplicación de la Nueva Solución
- Se recibe la nueva solución del backend
- Se actualizan las rutas de los camiones
- Se reinicia el progreso de los camiones (porcentaje = 0)
- **CRÍTICO**: Se calcula el tiempo transcurrido desde el inicio del intervalo actual
- Se establece la hora actual basada en el tiempo transcurrido (no se reinicia a 0)
- Se actualizan las fechas de los intervalos
- Se calculan los nodos restantes para la próxima actualización

### 5. Reanudación y Polling Correcto
- Se reanuda automáticamente la simulación
- **CRÍTICO**: Se actualiza la fecha de inicio de simulación con la fecha final del nuevo paquete
- **CRÍTICO**: El polling continúa desde la fecha correcta (no desde la fecha inicial)
- Los camiones continúan con la nueva planificación desde el punto temporal correcto

## Archivos Modificados

### 1. `src/services/simulacionApiService.ts`
- **Nueva función**: `recalcularAlgoritmoDespuesAveria(fechaHoraActual: string)`
- Llama al endpoint `/api/simulacion/mejor` con la fecha actual de simulación
- Maneja errores y respuestas del backend
- **Corregido**: Formato de fecha LocalDateTime (sin zona horaria)

### 2. `src/context/SimulacionContext.tsx`
- **Nueva función**: `aplicarNuevaSolucionDespuesAveria(data: IndividuoConBloqueos)`
- Aplica la nueva solución recalculada al contexto
- **CRÍTICO**: Calcula el tiempo transcurrido desde el inicio del intervalo actual
- **CRÍTICO**: Establece la hora actual basada en el tiempo transcurrido (no reinicia a 0)
- **CRÍTICO**: Calcula los nodos restantes para la próxima actualización
- Actualiza rutas, camiones, bloqueos y fechas
- Mantiene la continuidad temporal de la simulación

### 3. `src/context/simulacion/types.ts`
- **Actualizado**: `SimulacionContextType`
- Agregada la nueva función al tipo del contexto

### 4. `src/components/mapa/utils/averias.ts`
- **Modificada**: `handleAveriar()`
- Agregado el recálculo automático después de la avería
- **CRÍTICO**: Captura la fecha final del nuevo paquete generado
- **CRÍTICO**: Pasa la fecha correcta a `pasarAlSiguientePaquete()`
- Integración con la nueva funcionalidad
- Manejo de errores mejorado

### 5. `src/components/Mapa.tsx`
- **Actualizado**: Llamadas a `handleAveriar()`
- Agregado el parámetro `aplicarNuevaSolucionDespuesAveria`
- **CRÍTICO**: Agregado el parámetro `setFechaInicioSimulacion`
- Integración completa con el contexto

### 6. `src/context/simulacion/utils/tiempo.ts`
- **Nueva función**: `formatearFechaParaBackend(fecha: Date | string)`
- Convierte fechas al formato LocalDateTime compatible con el backend
- **Corregida**: `calcularTimestampSimulacion()` para usar formato correcto

## Beneficios

1. **Planificación Dinámica**: La simulación se adapta automáticamente a las averías
2. **Optimización Continua**: El algoritmo genético recalcula rutas considerando el estado actual
3. **Transparencia**: El usuario ve inmediatamente el impacto de la avería en la planificación
4. **Continuidad**: La simulación continúa sin interrupciones manuales
5. **Precisión Temporal**: La simulación continúa desde el punto temporal correcto, no desde el inicio
6. **Polling Correcto**: El sistema continúa cargando paquetes desde la fecha correcta después de la avería

## Consideraciones Técnicas

### Formato de Fecha
- **Backend espera**: `LocalDateTime` en formato `YYYY-MM-DDTHH:mm:ss` (sin zona horaria)
- **Frontend envía**: Formato `LocalDateTime` compatible usando `formatearFechaParaBackend()`
- **Ejemplo correcto**: `2025-01-01T07:12:00` (no `2025-01-01T07:12:00.000Z`)

### Fecha y Hora de Simulación
- Se usa `calcularTimestampSimulacion()` para obtener la fecha exacta actual
- Combina la fecha base con la hora calculada de la simulación
- Asegura que el recálculo use el momento correcto
- **Formato garantizado**: Compatible con `LocalDateTime.parse()` del backend

### Continuidad Temporal (CRÍTICO)
- **Problema anterior**: La simulación se reiniciaba desde hora 0 después de una avería
- **Solución implementada**: 
  - Calcula el tiempo transcurrido desde el inicio del intervalo actual
  - Establece la hora actual basada en ese tiempo transcurrido
  - Calcula los nodos restantes para la próxima actualización
  - Mantiene la continuidad temporal sin saltos

### Polling Correcto después de Averías (CRÍTICO)
- **Problema anterior**: Después de una avería, el polling volvía a usar la fecha inicial de simulación
- **Solución implementada**:
  - Captura la fecha final del nuevo paquete generado después de la avería
  - Actualiza la fecha de inicio de simulación con esa fecha final
  - El polling continúa desde la fecha correcta para cargar el siguiente paquete
  - Evita que se recarguen paquetes ya procesados

### Cálculo de Tiempo Transcurrido
```javascript
// Ejemplo de cálculo:
const tiempoTranscurridoEnIntervalo = Math.floor(
  (inicioNuevoIntervalo.getTime() - inicioIntervaloActual.getTime()) / (1000 * 60)
); // Convertir a minutos

const NODOS_POR_INTERVALO = 25; // 25 nodos por intervalo de 30 minutos
const nodosTranscurridos = Math.floor((tiempoTranscurridoEnIntervalo / 30) * NODOS_POR_INTERVALO);
const nuevaHoraActual = Math.max(0, nodosTranscurridos);
```

### Estado de Camiones
- Los camiones averiados se excluyen automáticamente del algoritmo
- Los pedidos se reasignan a camiones disponibles
- Se mantiene la consistencia del estado
- **Los camiones reinician su progreso (porcentaje = 0)** pero la simulación temporal continúa

### Manejo de Errores
- Si falla el recálculo, la avería se registra correctamente
- Se muestra una advertencia al usuario
- La simulación puede continuar con la planificación anterior

## Uso

La funcionalidad se activa automáticamente cuando:
1. El usuario hace clic en un camión en el mapa
2. Selecciona "Avería tipo 1", "Avería tipo 2" o "Avería tipo 3"
3. El sistema procesa la avería y recalcula automáticamente
4. La simulación continúa desde el punto temporal correcto
5. **El polling continúa desde la fecha correcta** para cargar el siguiente paquete

No se requiere intervención manual adicional - todo el proceso es automático y transparente para el usuario.

## Logs y Debugging

El sistema genera logs detallados en la consola:
- `🧬 RECALCULANDO`: Inicio del recálculo
- `🔄 NUEVA SOLUCIÓN`: Aplicación de nueva solución con cálculos temporales
- `✅ NUEVA SOLUCIÓN`: Confirmación de éxito
- `❌ ERROR`: Errores durante el proceso
- `📅 TIMESTAMP SIMULACIÓN`: Información del formato de fecha usado
- `🔄 NUEVA SOLUCIÓN: Cálculo de nueva hora actual`: Detalles del cálculo temporal
- `📅 SIGUIENTE PAQUETE: Actualizando fecha de inicio de simulación`: Corrección del polling
- `✅ SIGUIENTE PAQUETE: Fecha de inicio de simulación actualizada`: Confirmación de corrección

Estos logs ayudan a monitorear y debuggear el funcionamiento del sistema.

## Correcciones de Formato

### Problema Original
- El frontend enviaba fechas en formato ISO con zona horaria: `2025-01-01T07:12:00.000Z`
- El backend esperaba formato LocalDateTime sin zona horaria: `2025-01-01T07:12:00`

### Solución Implementada
- Nueva función `formatearFechaParaBackend()` para convertir fechas al formato correcto
- Corrección en `calcularTimestampSimulacion()` para no agregar zona horaria
- Actualización de `obtenerSiguientePaquete()` para usar el formato correcto
- Validación de formato en la función de utilidad

## Correcciones de Continuidad Temporal

### Problema Original
- Después de una avería, la simulación se reiniciaba desde hora 0
- Se perdía el progreso temporal del intervalo actual
- La simulación recorría todo el paquete desde el inicio

### Solución Implementada
- Cálculo del tiempo transcurrido desde el inicio del intervalo actual
- Establecimiento de la hora actual basada en el tiempo transcurrido
- Cálculo de nodos restantes para la próxima actualización
- Mantenimiento de la continuidad temporal sin saltos
- Los camiones reinician su progreso pero la simulación temporal continúa

## Correcciones de Polling después de Averías

### Problema Original
- Después de una avería, el polling volvía a usar la fecha inicial de simulación (`2025-01-01T00:00`)
- Se recargaban paquetes ya procesados
- La simulación no continuaba desde el punto correcto

### Solución Implementada
- Captura de la fecha final del nuevo paquete generado después de la avería
- Actualización de la fecha de inicio de simulación con esa fecha final
- El polling continúa desde la fecha correcta para cargar el siguiente paquete
- Evita recargar paquetes ya procesados
- Mantiene la continuidad correcta de la simulación 