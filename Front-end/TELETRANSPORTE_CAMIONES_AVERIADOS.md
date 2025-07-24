# Teletransporte de Camiones Averiados

## Descripción

Esta funcionalidad permite que los camiones averiados puedan "teletransportarse" a su nueva posición en el cromosoma recalculado después de una avería, manteniendo su estado de "Averiado" pero apareciendo en la nueva ubicación asignada por el algoritmo genético.

## Problema Resuelto

**Antes**: Cuando un camión tenía una avería y se recalculaba la ruta, el camión se quedaba en su posición anterior y no se actualizaba visualmente en el mapa ni en la tabla, aunque el backend había recalculado su nueva posición.

**Ahora**: Los camiones averiados se teletransportan automáticamente a su nueva posición en el cromosoma recalculado, manteniendo su estado de "Averiado" pero apareciendo en la ubicación correcta.

## Funcionamiento

### 1. Detección de Camión Averiado
- Cuando se aplica una nueva solución después de una avería, el sistema verifica si algún camión tenía estado "Averiado" anteriormente
- Se compara el estado anterior con el nuevo cromosoma recibido del backend

### 2. Lógica de Teletransporte
```typescript
if (anterior && anterior.estado === "Averiado") {
  // Si el camión estaba averiado, teletransportarlo a su nueva posición
  ubicacion = ruta.ruta[0]; // Primera posición de la nueva ruta
  porcentaje = 0; // Reiniciar progreso
  console.log(`🚛💥 TELETRANSPORTE: Camión ${ruta.id} averiado teletransportado de ${anterior.ubicacion} a ${ubicacion}`);
} else {
  // Para camiones no averiados, mantener lógica anterior
  ubicacion = anterior?.ubicacion ?? ruta.ruta[0];
  porcentaje = 0;
}
```

### 3. Mantenimiento del Estado
```typescript
if (anterior && anterior.estado === "Averiado") {
  // Si el camión estaba averiado, mantenerlo como averiado pero en nueva posición
  estadoFrontend = "Averiado";
  console.log(`🚛💥 ESTADO: Camión ${ruta.id} mantiene estado 'Averiado' en nueva posición ${ubicacion}`);
}
```

## Archivos Modificados

### 1. `src/context/SimulacionContext.tsx`
- **Función**: `aplicarNuevaSolucionDespuesAveria()`
  - Agregada lógica de teletransporte para camiones averiados
  - Mantiene estado "Averiado" en nueva posición
  - Logs informativos para debugging

- **Función**: `aplicarSolucionPrecargada()`
  - Aplicada la misma lógica para consistencia
  - Asegura que el teletransporte funcione en todas las transiciones

## Beneficios

1. **Visualización Correcta**: Los camiones averiados aparecen en su nueva posición asignada por el algoritmo
2. **Estado Consistente**: Mantienen su estado de "Averiado" mientras están en la nueva ubicación
3. **Debugging Mejorado**: Logs informativos permiten rastrear el teletransporte
4. **Experiencia de Usuario**: El mapa y la tabla muestran la información correcta y actualizada

## Casos de Uso

### Escenario 1: Avería Tipo 1 (Menor)
- Camión se avería en posición (5,3)
- Algoritmo recalcula y asigna nueva posición (8,2)
- Camión aparece como "Averiado" en (8,2)

### Escenario 2: Avería Tipo 2/3 (Mayor)
- Camión se avería en posición (10,7)
- Algoritmo recalcula y asigna nueva posición (2,1)
- Camión aparece como "Averiado" en (2,1)

### Escenario 3: Múltiples Camiones
- Varios camiones averiados se teletransportan simultáneamente
- Cada uno mantiene su estado "Averiado" en su nueva posición

## Logs de Debugging

El sistema genera logs informativos para facilitar el debugging:

```
🚛💥 TELETRANSPORTE: Camión CAM001 averiado teletransportado de (5,3) a (8,2)
🚛💥 ESTADO: Camión CAM001 mantiene estado 'Averiado' en nueva posición (8,2)
```

## Consideraciones Técnicas

1. **Reinicio de Progreso**: El porcentaje de progreso se reinicia a 0 para camiones teletransportados
2. **Consistencia de Estado**: Se mantiene el estado "Averiado" independientemente de la nueva posición
3. **Compatibilidad**: La funcionalidad es compatible con todos los tipos de avería (1, 2, 3)
4. **Rendimiento**: No afecta el rendimiento de la simulación

## Pruebas Recomendadas

1. **Avería Individual**: Probar con un solo camión averiado
2. **Múltiples Averías**: Probar con varios camiones averiados simultáneamente
3. **Diferentes Tipos**: Probar con todos los tipos de avería (1, 2, 3)
4. **Posiciones Extremas**: Probar con posiciones muy separadas en el mapa
5. **Recuperación**: Verificar que el estado se mantiene correctamente hasta la recuperación 