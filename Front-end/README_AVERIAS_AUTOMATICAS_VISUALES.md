# Averías Automáticas Visuales

## Descripción

Esta funcionalidad permite que cuando un camión detecte el primer nodo de avería automática (T1, T2, T3) en su ruta, se muestre visualmente como averiado en el mapa y permanezca en ese nodo específico hasta el próximo paquete, **sin registrar la avería en el backend**.

## Características

### 🔍 Detección Automática
- **Detección en tiempo real**: Cuando un camión llega a un nodo con tipo `AVERIA_AUTOMATICA_T1`, `AVERIA_AUTOMATICA_T2` o `AVERIA_AUTOMATICA_T3`
- **Primer nodo**: Solo se detecta en el primer nodo de avería automática que encuentre el camión
- **Estado local**: El camión se marca como "Averiado" solo en el frontend

### 🚛 Comportamiento del Camión
- **Inmovilidad**: Una vez detectada la avería, el camión no avanza más
- **Posición fija**: Permanece en el nodo exacto donde se detectó la avería
- **Hasta próximo paquete**: Solo se "recupera" cuando llegue el siguiente paquete de datos

### 🎨 Visualización Especial
- **Color distintivo**: Rojo más intenso (`#b91c1c`) para distinguir de averías normales
- **Efecto visual**: Círculo punteado animado alrededor del camión
- **Tooltip informativo**: Muestra el tipo de avería automática (T1, T2, T3)
- **Leyenda actualizada**: Nueva entrada en la leyenda del mapa

## Implementación Técnica

### Archivos Modificados

1. **`Front-end/src/context/simulacion/camionLogic.ts`**
   - `detectarAveriaAutomatica()`: Detecta nodos de avería automática
   - `avanzarCamion()`: Marca camión como averiado y lo mantiene en posición

2. **`Front-end/src/components/Mapa.tsx`**
   - Detección visual de averías automáticas
   - Efectos visuales especiales
   - Colores diferenciados
   - Tooltips informativos

3. **`Front-end/src/config/constants.ts`**
   - Activación de averías automáticas para todos los tipos de simulación

### Flujo de Funcionamiento

1. **Avance del camión**: El camión avanza normalmente por su ruta
2. **Detección**: Al llegar a un nodo con tipo de avería automática, se detecta
3. **Marcado**: Se marca como "Averiado" localmente (sin backend)
4. **Inmovilidad**: El camión se detiene en ese nodo
5. **Visualización**: Se muestra con efectos visuales especiales
6. **Recuperación**: Al llegar el próximo paquete, el camión vuelve a su estado normal

## Configuración

### Activación/Desactivación

```typescript
// En Front-end/src/config/constants.ts
export const AVERIAS_AUTOMATICAS_POR_TIPO = {
  DIARIA: true,     // Activado para simulación diaria
  SEMANAL: true,    // Activado para simulación semanal
  COLAPSO: true     // Activado para simulación de colapso
};
```

### Tipos de Nodos de Avería

- `AVERIA_AUTOMATICA_T1`: Avería automática tipo 1
- `AVERIA_AUTOMATICA_T2`: Avería automática tipo 2  
- `AVERIA_AUTOMATICA_T3`: Avería automática tipo 3

## Logs y Debugging

### Logs de Detección
```
🚛💥 DETECCIÓN: Primer nodo de avería automática detectado: {
  camionId: "CAM001",
  tipoNodo: "AVERIA_AUTOMATICA_T1",
  paso: 5,
  porcentaje: 5.2
}
```

### Logs de Marcado
```
🚛🔴 CAMION_LOGIC: Marcando camión como averiado automáticamente (SOLO VISUAL): {
  camionId: "CAM001",
  tipoAveria: "AVERIA_AUTOMATICA_T1",
  nodoAveria: "(15,20)",
  porcentaje: 5.2
}
```

### Logs de Inmovilidad
```
🚛🔴 CAMION_LOGIC: Camión averiado, manteniendo en posición: {
  camionId: "CAM001",
  ubicacion: "(15,20)",
  porcentaje: 5.2
}
```

## Ventajas

1. **Sin impacto en backend**: No se registran averías innecesarias
2. **Visualización clara**: Fácil identificación de averías automáticas
3. **Comportamiento realista**: Los camiones se detienen en nodos problemáticos
4. **Configuración flexible**: Se puede activar/desactivar por tipo de simulación
5. **Debugging mejorado**: Logs detallados para seguimiento

## Uso

1. **Iniciar simulación**: Las averías automáticas están activadas por defecto
2. **Observar camiones**: Los camiones que lleguen a nodos de avería automática se detendrán
3. **Identificar averías**: Buscar camiones con color rojo intenso y efectos visuales
4. **Ver tooltips**: Pasar el mouse sobre camiones averiados para ver el tipo de avería
5. **Esperar recuperación**: Los camiones se recuperarán automáticamente en el próximo paquete

## Notas Importantes

- **Solo visual**: No se registra nada en el backend
- **Temporal**: Los camiones se recuperan automáticamente
- **Primer nodo**: Solo se detecta en el primer nodo de avería encontrado
- **Configurable**: Se puede desactivar cambiando la configuración 