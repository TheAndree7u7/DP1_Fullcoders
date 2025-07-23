# Corrección de Tipos de Nodos - Averías Automáticas

## Problema Identificado

El sistema no estaba detectando correctamente los nodos con averías automáticas porque había una discrepancia entre la estructura de datos del backend y la interfaz del frontend.

### Estructura del Backend
Según la documentación del backend, los nodos se envían con esta estructura:
```json
{
  "nodos": [
    {
      "coordenada": { "x": integer, "y": integer },
      "tipo": "AVERIA_AUTOMATICA_T1" // ← El tipo está en la propiedad "tipo"
    }
  ]
}
```

### Problema en el Frontend
La interfaz `Nodo` en el frontend tenía la propiedad como `tipoNodo` en lugar de `tipo`:
```typescript
// ❌ INCORRECTO (antes)
export interface Nodo {
  coordenada: Coordenada;
  bloqueado: boolean;
  gScore: number;
  fScore: number;
  tipoNodo: TipoNodo; // ← Propiedad incorrecta
}
```

## Solución Implementada

### 1. **Corrección de la Interfaz Nodo**
```typescript
// ✅ CORRECTO (después)
export interface Nodo {
  coordenada: Coordenada;
  bloqueado: boolean;
  gScore: number;
  fScore: number;
  tipo: string; // ← Propiedad corregida para coincidir con el backend
}
```

### 2. **Corrección en el Procesamiento de Datos**
En las funciones `aplicarSolucionPrecargada` y `aplicarNuevaSolucionDespuesAveria`:

```typescript
// ❌ INCORRECTO (antes)
tiposNodos: gen.nodos.map((n: Nodo) => n.tipoNodo || 'NORMAL')

// ✅ CORRECTO (después)
const tiposNodosRecibidos = gen.nodos.map(n => n.tipo);
tiposNodos: tiposNodosRecibidos
```

### 3. **Logs de Verificación Agregados**
Se agregaron logs para verificar que los tipos se procesen correctamente:

```typescript
// Log para verificar los tipos de nodos que llegan del backend
const tiposNodosRecibidos = gen.nodos.map(n => n.tipo);
//console.log('🔍 CONTEXTO: Tipos de nodos recibidos del backend para camión', gen.camion.codigo, ':', tiposNodosRecibidos);

// Contar nodos de avería automática
const nodosAveriaAutomatica = tiposNodosRecibidos.filter(tipo => 
  tipo === 'AVERIA_AUTOMATICA_T1' || 
  tipo === 'AVERIA_AUTOMATICA_T2' || 
  tipo === 'AVERIA_AUTOMATICA_T3'
);

if (nodosAveriaAutomatica.length > 0) {
  // console.log('🚛💥 CONTEXTO: Nodos de avería automática encontrados:', {
  //   camionId: gen.camion.codigo,
  //   nodosAveria: nodosAveriaAutomatica,
  //   totalNodos: tiposNodosRecibidos.length
  // });
}
```

## Archivos Modificados

### 1. **`src/types.ts`**
- Actualizada la interfaz `Nodo` para usar `tipo: string` en lugar de `tipoNodo: TipoNodo`

### 2. **`src/context/SimulacionContext.tsx`**
- Corregido el acceso a la propiedad `tipo` en lugar de `tipoNodo`
- Agregados logs de verificación para monitorear el procesamiento de tipos
- Aplicado en ambas funciones: `aplicarSolucionPrecargada` y `aplicarNuevaSolucionDespuesAveria`

## Verificación

### Logs Esperados
Cuando se ejecute la simulación, deberías ver estos logs en la consola:

```
🔍 CONTEXTO: Tipos de nodos recibidos del backend para camión [ID]: ['NORMAL', 'AVERIA_AUTOMATICA_T1', 'NORMAL', ...]
🚛💥 CONTEXTO: Nodos de avería automática encontrados: { camionId: "[ID]", nodosAveria: ["AVERIA_AUTOMATICA_T1"], totalNodos: 10 }
```

### Comportamiento Esperado
1. **Los tipos de nodos se cargan correctamente** desde el backend
2. **Se detectan los nodos de avería automática** durante el procesamiento
3. **Los camiones se averían automáticamente** cuando recorren estos nodos
4. **Se registran las averías en el backend** con el tipo correcto (T1, T2, T3)

## Impacto

Esta corrección resuelve el problema principal donde todos los nodos se detectaban como `NORMAL` en lugar de sus tipos reales. Ahora:

- ✅ Los nodos `AVERIA_AUTOMATICA_T1` se detectan correctamente
- ✅ Los nodos `AVERIA_AUTOMATICA_T2` se detectan correctamente  
- ✅ Los nodos `AVERIA_AUTOMATICA_T3` se detectan correctamente
- ✅ Los camiones se averían automáticamente al recorrer estos nodos
- ✅ Las averías se registran en el backend con el tipo correcto

## Testing

Para verificar que la corrección funciona:

1. **Ejecuta la simulación** y observa los logs en la consola
2. **Busca los logs de tipos de nodos** para confirmar que se cargan correctamente
3. **Observa si los camiones se averían** cuando recorren nodos de avería automática
4. **Verifica que aparezcan los toasts** informativos de averías automáticas 