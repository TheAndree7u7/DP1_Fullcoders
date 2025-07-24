# Averías Automáticas en el Frontend

## Descripción

Esta funcionalidad permite que los camiones se marquen automáticamente como averiados cuando recorren nodos con estados de avería automática (`AVERIA_AUTOMATICA_T1`, `AVERIA_AUTOMATICA_T2`, `AVERIA_AUTOMATICA_T3`), **sin necesidad de recalcular el algoritmo genético**, y además registra la avería en el backend.

## Funcionamiento

### 1. Detección de Nodos con Averías Automáticas

El backend ya incluye nodos con averías automáticas en la respuesta del algoritmo genético. Estos nodos tienen tipos específicos:
- `AVERIA_AUTOMATICA_T1`: Avería automática tipo 1
- `AVERIA_AUTOMATICA_T2`: Avería automática tipo 2  
- `AVERIA_AUTOMATICA_T3`: Avería automática tipo 3

### 2. Procesamiento en el Frontend

Cuando se reciben los datos del backend, el frontend procesa los tipos de nodos y los almacena en la propiedad `tiposNodos` de cada ruta de camión.

### 3. Detección Automática Durante la Simulación

Durante el avance de la simulación, cada vez que un camión se mueve:

1. **Se verifica** si el siguiente nodo en su ruta es un nodo de avería automática
2. **Si es así**, el camión se marca inmediatamente como "Averiado"
3. **Se registra la avería** en el backend llamando al endpoint `averiarCamionConEstado` con:
   - `camionId`: ID del camión averiado
   - `tipo`: 1 para T1, 2 para T2, 3 para T3
   - `fechaHoraReporte`: Timestamp actual de la simulación
   - `estadoCompleto`: Estado completo de la simulación al momento de la avería

### 4. Mapeo de Tipos

| Tipo de Nodo | Tipo de Avería | ID |
|--------------|----------------|----|
| `AVERIA_AUTOMATICA_T1` | T1 | 1 |
| `AVERIA_AUTOMATICA_T2` | T2 | 2 |
| `AVERIA_AUTOMATICA_T3` | T3 | 3 |

## Archivos Modificados

### 1. **Tipos (`src/types.ts`)**
```typescript
export enum TipoNodo {
  // ... tipos existentes ...
  AVERIA_AUTOMATICA_T1 = 'AVERIA_AUTOMATICA_T1',
  AVERIA_AUTOMATICA_T2 = 'AVERIA_AUTOMATICA_T2',
  AVERIA_AUTOMATICA_T3 = 'AVERIA_AUTOMATICA_T3'
}
```

### 2. **Tipos de Simulación (`src/context/simulacion/types.ts`)**
```typescript
export interface RutaCamion {
  // ... propiedades existentes ...
  tiposNodos?: string[]; // Tipos de nodos correspondientes a cada posición
}
```

### 3. **Contexto de Simulación (`src/context/SimulacionContext.tsx`)**
- Actualizada función `aplicarSolucionPrecargada` para procesar tipos de nodos
- Actualizada función `aplicarNuevaSolucionDespuesAveria` para procesar tipos de nodos

### 4. **Lógica de Camiones (`src/context/simulacion/camionLogic.ts`)**
- Nueva función `detectarAveriaAutomatica` que detecta nodos con averías automáticas
- Actualizada función `avanzarCamion` para detectar y manejar averías automáticas
- Actualizada función `avanzarTodosLosCamiones` para pasar el estado de simulación

### 5. **Averías Automáticas (`src/components/mapa/utils/averiasAutomaticas.ts`)**
- Nueva función `handleAveriaAutomatica` que:
  - Extrae el tipo de avería (T1, T2, T3) del tipo de nodo
  - Captura el estado completo de la simulación
  - Llama al endpoint `averiarCamionConEstado` con los parámetros correctos
  - Muestra notificaciones informativas

### 6. **Avance de Hora (`src/context/simulacion/avanceHora.ts`)**
- Actualizada función `avanzarHora` para pasar el estado de simulación completo

## Flujo de Ejecución

1. **Inicio de Simulación**: Se cargan los datos del backend incluyendo tipos de nodos
2. **Avance de Camiones**: En cada paso de la simulación:
   - Se verifica si el siguiente nodo es de avería automática
   - Si es así, se marca el camión como "Averiado"
   - Se registra la avería en el backend
   - Se muestra notificación al usuario
3. **Continuación**: La simulación continúa sin recalcular el algoritmo genético

## Ventajas

✅ **Sin Recalculo**: No se ejecuta el algoritmo genético después de una avería automática
✅ **Registro Completo**: Las averías se registran en el backend con toda la información necesaria
✅ **Detección Automática**: No requiere intervención manual del usuario
✅ **Notificaciones**: El usuario es informado de las averías automáticas
✅ **Tipos Correctos**: Se mapean correctamente los tipos T1, T2, T3

## Logs y Debugging

La funcionalidad incluye logs detallados para debugging:
- Detección de averías automáticas
- Captura del estado de simulación
- Envío al backend
- Confirmación de procesamiento exitoso

## Notificaciones

Se muestran notificaciones toast informativas:
- **Info**: Cuando se detecta una avería automática
- **Error**: Si hay problemas al procesar la avería 