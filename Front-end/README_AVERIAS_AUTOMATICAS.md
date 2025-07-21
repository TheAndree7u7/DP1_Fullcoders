# Funcionalidad de Averías Automáticas

## Descripción

Esta funcionalidad permite que ocurran averías automáticas en camiones durante la simulación, siguiendo criterios específicos y configurables.

## Características Principales

### 1. Configuración Flexible
- **Activación/Desactivación**: Se puede activar o desactivar completamente la funcionalidad
- **Frecuencia**: Configurable cada cuántos paquetes debe ocurrir una avería
- **Momento de ocurrencia**: Entre un porcentaje mínimo y máximo del tiempo del intervalo
- **Prioridad de selección**: Opción para priorizar camiones con menor capacidad

### 2. Criterios de Selección de Camiones
- ✅ Solo camiones en estado "En Ruta"
- ✅ No en nodos de tipo PEDIDO
- ✅ No averiados recientemente por esta funcionalidad
- ✅ Con menor capacidad GLP (si está habilitada la prioridad)

### 3. Tipos de Avería
- **Tipo 1**: Avería menor
- **Tipo 2**: Avería media  
- **Tipo 3**: Avería grave

## Archivos Implementados

### 1. Configuración (`src/config/constants.ts`)
```typescript
export const AVERIAS_AUTOMATICAS_CONFIG = {
  ACTIVADO: true,                    // Activar/desactivar
  PAQUETES_PARA_AVERIA: 2,          // Cada cuántos paquetes
  PORCENTAJE_MINIMO_TIEMPO: 0.05,   // 5% del intervalo
  PORCENTAJE_MAXIMO_TIEMPO: 0.35,   // 35% del intervalo
  PRIORIDAD_CAPACIDAD_MINIMA: true, // Menor capacidad primero
  ESTADOS_VALIDOS: ['En Ruta'],     // Estados válidos
  TIPOS_NODO_VALIDOS: ['NORMAL', 'ALMACEN', 'INTERMEDIO', 'ALMACEN_RECARGA']
}
```

### 2. Lógica Principal (`src/context/simulacion/autoAverias.ts`)
- `inicializarEstadoAveriasAutomaticas()`: Inicializa el estado
- `incrementarContadorPaquetes()`: Incrementa contador y verifica si debe ocurrir avería
- `obtenerCamionesCandidatosAveria()`: Filtra camiones candidatos
- `calcularMomentoAveria()`: Calcula momento exacto dentro del intervalo
- `verificarYEjecutarAveriaAutomatica()`: Función principal de verificación y ejecución

### 3. Integración en Contexto (`src/context/SimulacionContext.tsx`)
- Estado de averías automáticas agregado
- Lógica integrada en función `avanzarHora()`
- Contador de paquetes automático

### 4. Componente de Control (`src/components/ControlAveriasAutomaticas.tsx`)
- Interfaz para configurar parámetros
- Visualización del estado actual
- Información detallada de criterios

## Flujo de Funcionamiento

1. **Inicialización**: Se crea el estado de averías automáticas
2. **Conteo**: Cada vez que avanza la simulación, se incrementa el contador de paquetes
3. **Verificación**: Se verifica si debe ocurrir una avería según la configuración
4. **Selección**: Se filtran camiones candidatos según criterios
5. **Ejecución**: Se ejecuta la avería usando la funcionalidad existente
6. **Actualización**: Se actualiza el estado y se registra el camión averiado

## Ejemplo de Uso

### Configuración Básica
```typescript
// En constants.ts
export const AVERIAS_AUTOMATICAS_CONFIG = {
  ACTIVADO: true,
  PAQUETES_PARA_AVERIA: 2,  // Cada 2 paquetes
  PORCENTAJE_MINIMO_TIEMPO: 0.05,  // 5%
  PORCENTAJE_MAXIMO_TIEMPO: 0.35,  // 35%
  // ... resto de configuración
}
```

### Resultado Esperado
- Cada 2 paquetes de simulación
- Entre 5% y 35% del tiempo del intervalo
- Camión en ruta con menor capacidad seleccionado
- Avería ejecutada automáticamente
- Notificación mostrada al usuario

## Integración con Funcionalidad Existente

La funcionalidad se integra perfectamente con:
- ✅ Sistema de averías manuales existente
- ✅ Recalculo de algoritmo genético
- ✅ Notificaciones y toasts
- ✅ Estados de simulación
- ✅ Controles de pausa/reanudación

## Configuración Avanzada

### Modificar Frecuencia
```typescript
PAQUETES_PARA_AVERIA: 3  // Cada 3 paquetes
```

### Cambiar Rango de Tiempo
```typescript
PORCENTAJE_MINIMO_TIEMPO: 0.10,  // 10%
PORCENTAJE_MAXIMO_TIEMPO: 0.50,  // 50%
```

### Desactivar Prioridad por Capacidad
```typescript
PRIORIDAD_CAPACIDAD_MINIMA: false  // Selección aleatoria
```

## Notas Importantes

1. **Compatibilidad**: Funciona con toda la funcionalidad existente
2. **Rendimiento**: Lógica optimizada para no afectar el rendimiento
3. **Configuración**: Se puede modificar sin reiniciar la aplicación
4. **Logs**: Incluye logs detallados para debugging
5. **Notificaciones**: Informa al usuario cuando ocurre una avería automática

## Troubleshooting

### Averías no ocurren
- Verificar que `ACTIVADO: true`
- Comprobar que hay camiones "En Ruta"
- Verificar que no están en nodos PEDIDO

### Averías muy frecuentes
- Aumentar `PAQUETES_PARA_AVERIA`
- Verificar contador de paquetes

### Camiones incorrectos seleccionados
- Revisar criterios de selección
- Verificar estado de camiones
- Comprobar configuración de prioridad 