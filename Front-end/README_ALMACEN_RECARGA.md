# Funcionalidad de Recarga en Nodos ALMACEN_RECARGA

## Descripción

Esta funcionalidad permite que los camiones recarguen GLP y combustible cuando pasan por nodos de tipo `ALMACEN_RECARGA` en su ruta. Los nodos de tipo `ALMACEN_RECARGA` son almacenes secundarios que actúan como puntos de recarga intermedios.

## Comportamiento

### Recarga de GLP
- **Verificación de capacidad**: El sistema verifica si el camión necesita GLP y si el almacén tiene disponibilidad
- **Recarga inteligente**: 
  - Si el almacén tiene suficiente GLP, recarga todo lo que le falta al camión
  - Si el almacén no tiene suficiente, recarga solo lo disponible
- **Actualización de inventario**: Se reduce la capacidad del almacén según lo recargado

### Recarga de Combustible
- **Recarga completa**: Siempre se recarga al máximo de la capacidad del camión
- **Combustible infinito**: Los almacenes tienen combustible infinito (como especifica el requerimiento)

## Implementación Técnica

### Backend
- Los nodos de tipo `ALMACEN_RECARGA` se identifican en `GenDto.obtenerTipoNodo()`
- Se marcan como `ALMACEN_RECARGA` los nodos que están en `gen.getAlmacenesIntermedios()`
- La lógica de recarga se ejecuta en `Camion.actualizarRuta()` cuando el camión pasa por estos nodos

### Frontend
- **Tipo agregado**: Se agregó `ALMACEN_RECARGA` al enum `TipoNodo`
- **Función de verificación**: `verificarNodoAlmacenRecarga()` identifica si un nodo es de tipo `ALMACEN_RECARGA`
- **Función de recarga**: `recargarCamionEnAlmacenRecarga()` maneja la lógica de recarga específica
- **Integración**: Se integra en `avanzarCamion()` después de verificar recarga en almacén central

## Flujo de Ejecución

1. **Detección**: Cuando un camión avanza en su ruta, se verifica si el nodo actual es de tipo `ALMACEN_RECARGA`
2. **Verificación**: Se comprueba si hay un almacén secundario en las coordenadas del nodo
3. **Recarga GLP**: Se calcula cuánto GLP necesita el camión y cuánto puede proveer el almacén
4. **Recarga Combustible**: Se recarga el combustible al máximo
5. **Logging**: Se registra la operación de recarga con detalles

## Coordenadas de Almacenes Secundarios

Actualmente se identifican los siguientes almacenes secundarios como `ALMACEN_RECARGA`:
- Almacén Secundario 1: (42, 42)
- Almacén Secundario 2: (3, 63)

## Logs de Debug

La funcionalidad incluye logs detallados para debugging:
```
⛽ RECARGA GLP: Camión TA01 recargó 15.50 m³ de GLP en almacén Almacén Secundario 1 (disponible: 160.00 → 144.50 m³)
⛽ RECARGA COMBUSTIBLE: Camión TA01 recargó combustible al máximo en almacén Almacén Secundario 1
```

## Consideraciones Futuras

- **Sincronización con Backend**: Actualmente la reducción de capacidad del almacén es solo visual. En una implementación completa, debería sincronizarse con el backend
- **Persistencia**: Los cambios en la capacidad de los almacenes deberían persistir entre sesiones
- **Validaciones**: Agregar validaciones adicionales para casos edge (almacén inactivo, camión lleno, etc.) 