# Control de Logs de Camiones

## Descripción
Se ha implementado un sistema de control para los logs específicos de camiones en el archivo `SimulacionContext.tsx`.

## Configuración
En el archivo `Front-end/src/context/SimulacionContext.tsx`, línea 130, encontrarás la variable de configuración:

```typescript
const LOGS_CAMIONES_HABILITADOS = true; // Cambiar a false para desactivar logs de camiones
```

## Logs Controlados
Los siguientes logs de camiones están controlados por esta variable:

1. **🚛 Llegada de camión**: `Camión ${id} llegó a (x,y) - Entregando X pedidos`
2. **⛽ GLP antes de entrega**: `GLP antes de entrega: X.XX`
3. **📋 Detalles del pedido**: `Pedido: {objeto del pedido}`
4. **⬇️ Reducción de GLP**: `Reduciendo X GLP del camión ${id}`
5. **✅ GLP después de entrega**: `GLP después de entrega: X.XX`
6. **📊 Pesos actualizados**: `Camión ${id} pesos actualizados: {pesoCarga, pesoCombinado}`
7. **⚠️ Pedido sin volumen**: `Pedido sin volumenGLPAsignado: {pedido}`
8. **Camión sin pedidos**: `Camión ${id} no tiene pedidos asignados`
9. **📋 Transición de rutas**: `TRANSICIÓN: Rutas aplicadas desde solución precargada con X camiones`

## Uso
- **Para activar logs**: `LOGS_CAMIONES_HABILITADOS = true`
- **Para desactivar logs**: `LOGS_CAMIONES_HABILITADOS = false`

## Nota
Esta configuración solo afecta a los logs específicos de camiones del frontend. Los logs del backend (como el log de posición actualizada de camiones) no se ven afectados por esta variable.

### Logs del Backend
En el archivo `Back-end/plg/src/main/java/com/plg/utils/Simulacion.java`, línea 277-280, existe un log que muestra:
- 🚛 Posición actualizada del camión
- Combustible actual
- GLP actual
- Distancia máxima

Este log del backend no está controlado por la variable `LOGS_CAMIONES_HABILITADOS` del frontend. 