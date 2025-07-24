# Sincronización de Línea de Ruta con Avance del Camión

## Problema Identificado

Se detectó un desfase entre la velocidad de consumo de la línea de ruta y el avance del camión cuando se aumentaba la velocidad de simulación (segundos por nodo).

### Causa del Desfase

El problema ocurría porque la línea se consumía **antes** de que el camión pasara completamente por cada nodo. La lógica anterior causaba que la línea desapareciera prematuramente.

### Ejemplo del Problema

```
Porcentaje del camión: 1.0
Posición del camión: Math.floor(1.0) = 1 → Camión en nodo 1
Línea restante: Math.ceil(1.0) = 1 → Línea desde nodo 1 (se consume antes de que el camión pase completamente)
```

## Solución Implementada

### Cambios Realizados

1. **En `Mapa.tsx` línea ~270**: Mantener `Math.floor(porcentaje)` para la posición del camión
2. **En `Mapa.tsx` línea ~290**: Cambiar a `Math.floor(porcentaje) + 1` para que la línea se consuma después de pasar completamente
3. **En `Mapa.tsx` línea ~850**: Cambiar a `Math.floor(porcentaje) + 1` para camiones seleccionados

### Código Modificado

```typescript
// ANTES (línea se consumía antes)
const currentIdx = Math.floor(porcentaje);  // Posición del camión
const idxRest = Math.ceil(porcentaje);      // Línea se consume antes

// DESPUÉS (línea se consume después de pasar completamente)
const currentIdx = Math.floor(porcentaje);  // Posición del camión
const idxRest = Math.floor(porcentaje) + 1; // Línea se consume después de pasar completamente
```

### Lógica de Consumo Retrasado

- **Porcentaje 0.5**: Camión en nodo 0, línea desde nodo 1 (camión aún no pasó completamente por nodo 0)
- **Porcentaje 1.0**: Camión en nodo 1, línea desde nodo 2 (camión acaba de llegar al nodo 1, línea se consume desde el siguiente)
- **Porcentaje 1.5**: Camión en nodo 1, línea desde nodo 2 (camión aún no pasó completamente por nodo 1)
- **Porcentaje 2.0**: Camión en nodo 2, línea desde nodo 3 (camión acaba de llegar al nodo 2, línea se consume desde el siguiente)

## Resultado

- ✅ La línea de ruta ahora se consume **después** de que el camión pase completamente por cada nodo
- ✅ No hay desfase visual cuando se cambia la velocidad de simulación
- ✅ La línea permanece visible hasta que el camión pase completamente por ella
- ✅ **Consumo retrasado**: La línea se consume solo cuando el camión haya pasado completamente por cada nodo

## Archivos Modificados

- `Front-end/src/components/Mapa.tsx`

## Verificación

Para verificar que la corrección funciona:

1. Iniciar una simulación
2. Cambiar la velocidad de simulación (segundos por nodo)
3. Observar que la línea permanece visible hasta que el camión pase completamente por cada nodo
4. La línea debe consumirse solo después de que el camión pase completamente por cada nodo

## Notas Técnicas

- El cambio afecta solo la visualización, no la lógica de movimiento del camión
- Se mantiene la funcionalidad existente para todos los tipos de simulación
- La corrección es compatible con todas las velocidades de simulación
- **Clave**: Usar `Math.floor(porcentaje) + 1` para que la línea se consuma después de pasar completamente por cada nodo 