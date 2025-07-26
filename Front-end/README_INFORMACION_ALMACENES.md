# Información Detallada de Almacenes

## Descripción

Se ha implementado una funcionalidad mejorada para mostrar información detallada de los almacenes cuando se hace clic en ellos en el mapa. Ahora el modal de almacén incluye:

1. **Lista de Pedidos Programados**: Muestra todos los pedidos que están asignados a camiones que pasan por el almacén seleccionado.
2. **Lista de Unidades de Transporte**: Muestra todos los camiones que están asignados al almacén (pasan por él en sus rutas).

## Funcionalidades Implementadas

### 📦 Lista de Pedidos Programados

- **Contador**: Muestra el número total de pedidos programados para el almacén
- **Información detallada**: Para cada pedido se muestra:
  - Código del pedido
  - Coordenadas de destino
  - Volumen de GLP asignado
  - Fecha de registro
  - Estado del pedido (PENDIENTE, EN_TRANSITO, ENTREGADO, RETRASO)
- **Scroll**: Si hay muchos pedidos, se muestra un scroll vertical
- **Enlace al Panel**: Botón "Ver Panel" que lleva al panel de pedidos del menú derecho

### 🚛 Lista de Unidades de Transporte

- **Contador**: Muestra el número total de camiones asignados al almacén
- **Información detallada**: Para cada camión se muestra:
  - ID del camión
  - Ubicación actual
  - Capacidad de GLP (actual/máxima)
  - Nivel de combustible (actual/máximo)
  - Estado del camión (Disponible, En Ruta, Averiado, etc.)
- **Scroll**: Si hay muchos camiones, se muestra un scroll vertical
- **Enlace al Panel**: Botón "Ver Panel" que lleva al panel de camiones del menú derecho

## Cómo Usar

1. **Hacer clic en un almacén** en el mapa
2. **Ver información básica** del almacén (estado, capacidad GLP)
3. **Revisar pedidos programados** en la sección correspondiente
4. **Revisar camiones asignados** en la sección correspondiente
5. **Usar los enlaces** "Ver Panel" para acceder a las vistas detalladas

## Archivos Modificados

### Nuevos Archivos
- `Front-end/src/components/mapa/utils/almacenUtils.ts` - Funciones utilitarias para obtener información de almacenes

### Archivos Modificados
- `Front-end/src/components/Mapa.tsx` - Modal de almacén mejorado
- `Front-end/src/components/RightMenu.tsx` - Agregados atributos data-panel para enlaces
- `Front-end/src/components/mapa/utils/index.ts` - Exportaciones de nuevas utilidades

## Funciones Utilitarias

### `obtenerPedidosAsignadosAlAlmacen`
Obtiene los pedidos que están asignados a camiones que pasan por el almacén especificado.

### `obtenerCamionesAsignadosAlAlmacen`
Obtiene los camiones que están asignados al almacén especificado (pasan por él en sus rutas).

### `formatearFecha`
Formatea las fechas para mostrar en la interfaz de usuario.

## Características Técnicas

- **Responsive**: El modal se ajusta automáticamente para no salirse de la pantalla
- **Scroll interno**: Las listas tienen scroll independiente para manejar muchos elementos
- **Estados visuales**: Los pedidos y camiones muestran su estado con colores diferenciados
- **Navegación integrada**: Enlaces directos a los paneles correspondientes del menú derecho
- **Optimización**: Solo se calculan los datos cuando se abre el modal

## Estados de Pedidos

- 🟢 **ENTREGADO**: Pedido completado exitosamente
- 🔵 **EN_TRANSITO**: Pedido en camino con camión asignado
- 🔴 **RETRASO**: Pedido retrasado por avería del camión
- 🟡 **PENDIENTE**: Pedido esperando ser procesado

## Estados de Camiones

- 🟢 **Disponible**: Camión listo para nuevas asignaciones
- 🔵 **En Ruta**: Camión en movimiento hacia su destino
- 🔴 **Averiado**: Camión con problemas técnicos
- ⚫ **Otros**: Estados adicionales según la simulación 