# Flujo de Recálculo Dinámico de Averías

## Descripción General

El sistema de recálculo dinámico permite manejar averías de camiones en tiempo real durante la simulación, invalidando paquetes futuros y generando nuevas soluciones óptimas que consideren el estado actual del sistema.

## Flujo Completo del Recálculo

### 1. **Detección de Avería**
- Usuario hace clic en un camión en el mapa
- Sistema obtiene la posición actual del camión
- Frontend envía avería al endpoint `/api/averias/averiar-camion-dinamico`

### 2. **Creación de Avería (Backend)**
- Se crea la avería en la base de datos
- El camión pasa a estado `INMOVILIZADO_POR_AVERIA`
- Se actualiza la coordenada del camión según la posición reportada

### 3. **⏸️ PAUSA INMEDIATA DE LA SIMULACIÓN**
- **La simulación principal se DETIENE inmediatamente** con `Simulacion.pausarPorAveria()`
- Se muestra indicador visual "Pausada por avería" en el frontend
- La simulación NO puede continuar hasta que el paquete parche esté listo

### 4. **Inicio del Recálculo Dinámico**
- Se activa `RecalculoDinamico.iniciarRecalculo()`
- Sistema guarda el estado global de todos los camiones
- Se obtiene el paquete temporal actual (basado en `GestorHistorialSimulacion.getIndicePaqueteActual()`)

### 5. **Invalidación de Paquetes Futuros**
- Se eliminan todos los paquetes posteriores al paquete actual del historial
- Los paquetes invalidados ya no son válidos porque no consideran la avería
- Se registra cuántos paquetes fueron invalidados

### 6. **Liberación de Pedidos**
- Los pedidos del camión averiado se liberan
- Pasan de `PLANIFICADOS` a `POR_ATENDER`
- Cambian su estado a `REGISTRADO`
- Quedan disponibles para ser reasignados a otros camiones

### 7. **Generación de Paquete Parche**
- Se calcula el tiempo restante del paquete actual
- Se genera un "paquete parche" que cubre desde el momento de la avería hasta el final del paquete actual
- Este paquete utiliza el algoritmo genético para optimizar las rutas considerando la avería
- El paquete parche se agrega al historial con `GestorHistorialSimulacion.agregarPaqueteParche()`

### 8. **▶️ REANUDACIÓN AUTOMÁTICA**
- Una vez generado el paquete parche, se llama a `Simulacion.notificarPaqueteParcheDisponible()`
- **La simulación se reanuda automáticamente**
- **El paquete parche se consume INMEDIATAMENTE**
- Se oculta el indicador "Pausada por avería" en el frontend

### 9. **Generación de Paquetes Completos**
- Se genera al menos un paquete completo adicional (como especifica el usuario)
- Este paquete tiene duración normal (ej: 2 horas) y comienza donde termina el paquete parche
- Se ejecuta el algoritmo genético para optimizar las rutas
- Los paquetes completos se agregan al historial normal

### 10. **Aplicación de Soluciones**
- Se aplica el estado final de los camiones usando `CamionStateApplier.aplicarEstadoFinalCamiones()`
- Las nuevas rutas se integran en el sistema
- Los camiones no averiados continúan con sus nuevas rutas optimizadas

### 11. **Continuación de la Simulación**
- La simulación continúa normalmente con los nuevos paquetes generados
- El frontend consume los paquetes parche y completos como parte del flujo normal
- El sistema puede generar más paquetes según sea necesario

## Ejemplo de Flujo Temporal

```
Situación: Avería ocurre en paquete 7 a las 15:30 (el paquete 7 va de 14:00 a 16:00)

ANTES de la avería:
- Paquete 7: 14:00 - 16:00 ✅ (parcialmente consumido)
- Paquete 8: 16:00 - 18:00 ❌ (invalidado)
- Paquete 9: 18:00 - 20:00 ❌ (invalidado)

DESPUÉS de la avería:
- Paquete 7: 14:00 - 16:00 ✅ (parcialmente consumido)
- Paquete Parche: 15:30 - 16:00 🩹 (nuevo - cubre tiempo restante)
- Paquete 8: 16:00 - 18:00 📦 (nuevo - paquete completo)
- Paquete 9: 18:00 - 20:00 📦 (se generará según demanda)
```

## Clases Involucradas

### Backend
- **`AveriaController`**: Endpoint `/averiar-camion-dinamico`
- **`AveriaService`**: Método `activarRecalculoDinamico()`
- **`RecalculoDinamico`**: Lógica principal del recálculo
- **`GestorHistorialSimulacion`**: Manejo de paquetes e invalidación
- **`CamionStateApplier`**: Aplicación de estados finales
- **`Simulacion`**: Métodos de pausa y reanudación:
  - `pausarPorAveria()`: Pausa la simulación
  - `notificarPaqueteParcheDisponible()`: Reanuda la simulación
  - `estaPausadaPorAveria()`: Verifica estado de pausa
- **`SimulacionController`**: Endpoint `/pausada-por-averia`

### Frontend
- **`averiaApiService.ts`**: Funciones:
  - `averiarCamionDinamico()`: Crea avería con recálculo
  - `estaPausadaPorAveria()`: Consulta estado de pausa
- **`Mapa.tsx`**: Función `handleAveriar()` actualizada
- **`SimulacionContext.tsx`**: Contexto de estado global con estado de pausa
- **`ControlSimulacion.tsx`**: Indicador visual de pausa por avería

## Características Clave

✅ **Invalidación Automática**: Los paquetes futuros se invalidan automáticamente
✅ **Paquetes Parche**: Cubren el tiempo restante del paquete actual
✅ **Liberación de Pedidos**: Los pedidos quedan disponibles para reasignación
✅ **Optimización Continua**: Se usa algoritmo genético para cada nuevo paquete
✅ **Integración Seamless**: Los nuevos paquetes se integran en el flujo normal
✅ **Estado Global**: Se mantiene el estado de todos los camiones
✅ **Procesamiento Asíncrono**: No bloquea la simulación principal
✅ **⏸️ Pausa Automática**: La simulación se pausa inmediatamente al detectar avería
✅ **▶️ Reanudación Inmediata**: La simulación se reanuda automáticamente cuando el paquete parche está listo
✅ **🎯 Consumo Inmediato**: El paquete parche se consume inmediatamente sin esperas
✅ **👁️ Feedback Visual**: Indicador visual en el frontend que muestra el estado de pausa

## Consideraciones de Rendimiento

- El recálculo se ejecuta en un hilo separado usando `CompletableFuture`
- Se evita la regeneración innecesaria de paquetes
- Los snapshots de camiones se mantienen en memoria para acceso rápido
- La invalidación de paquetes es una operación O(n) eficiente

## Logs y Monitoreo

El sistema genera logs detallados para seguimiento:
- `🔄 RECÁLCULO DINÁMICO INICIADO`
- `📸 GUARDANDO ESTADO GLOBAL DE CAMIONES`
- `🗑️ PAQUETES INVALIDADOS`
- `🔓 LIBERANDO PEDIDOS DEL CAMIÓN AVERIADO`
- `🩹 GENERANDO PAQUETE PARCHE`
- `📦 GENERANDO PAQUETES COMPLETOS ADICIONALES`
- `✅ Recálculo con paquete parche completado exitosamente`

## Casos de Uso Soportados

1. **Avería TI1**: Camión se inmoviliza temporalmente
2. **Avería TI2**: Camión requiere traslado a taller
3. **Avería TI3**: Camión queda fuera de servicio
4. **Múltiples Averías**: Sistema puede manejar averías concurrentes
5. **Averías en Diferentes Paquetes**: Cada paquete se maneja independientemente

## Flujo de Pausa Completo

```
🕐 15:30:00 - Ocurre avería en camión C001
⏸️ 15:30:01 - Simulación principal se PAUSA inmediatamente
🔄 15:30:02 - Inicia recálculo dinámico en segundo plano
📸 15:30:03 - Guarda estado de todos los camiones
🗑️ 15:30:04 - Invalida paquetes futuros
🔓 15:30:05 - Libera pedidos del camión averiado
🩹 15:30:06 - Genera paquete parche (15:30-16:00)
▶️ 15:30:07 - Simulación se REANUDA automáticamente
🎯 15:30:08 - Paquete parche se consume INMEDIATAMENTE
📦 15:30:09 - Genera paquetes completos adicionales
✅ 15:30:10 - Simulación continúa normalmente
```

## Próximas Mejoras

- Persistencia de snapshots en base de datos
- Optimización de memoria para historiales largos
- Métricas de rendimiento del recálculo
- Interfaz visual para monitoreo de recálculos
- Rollback automático en caso de errores críticos
- Notificaciones push para averías críticas 