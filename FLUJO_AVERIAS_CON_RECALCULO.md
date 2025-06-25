# Flujo de Averías con Recálculo Automático de Rutas

## Descripción General

Este documento describe la funcionalidad implementada que permite que cuando se registre una avería en un camión, el sistema pueda automáticamente volver a ejecutar la simulación y solicitar una nueva solución del backend desde el frontend.

## Componentes Implementados

### Backend

#### 1. AveriaService.java
- **Método nuevo**: `agregarConNuevaSimulacion(AveriaRequest request)`
- **Funcionalidad**: 
  - Crea la avería normalmente usando `agregar()`
  - Si es tipo TI2 o TI3, solicita nueva simulación usando `gaTriggerQueue`
  - Retorna la avería creada

#### 2. AveriaController.java
- **Endpoint nuevo**: `POST /api/averias/averiar-camion-con-nueva-simulacion`
- **Funcionalidad**:
  - Usa `agregarConNuevaSimulacion()` para crear avería y solicitar recálculo
  - Cambia estado del camión a `EN_MANTENIMIENTO_POR_AVERIA`
  - Retorna la avería creada

### Frontend

#### 1. averiaApiService.ts
- **Servicio nuevo**: `averiarCamionConNuevaSimulacion()`
- **Funcionalidad**: 
  - Consume el endpoint `/averiar-camion-con-nueva-simulacion`
  - Envía código camión, tipo incidente y fecha

#### 2. SimulacionContext.tsx
- **Función nueva**: `recargarSimulacionPorAveria()`
- **Funcionalidad**:
  - Marca estado como esperando actualización
  - Recarga datos del backend (`cargarDatos(false)`)
  - Reinicia contador de nodos

#### 3. Mapa.tsx (modificado)
- **Función modificada**: `handleAveriar()`
- **Funcionalidad**:
  - Usa `averiarCamionConNuevaSimulacion()` en lugar de `averiarCamionTipo()`
  - Para averías TI2/TI3: espera 2 segundos y llama `recargarSimulacionPorAveria()`
  - Muestra toasts informativos sobre el progreso

## Flujo Completo

### Caso: Avería Tipo TI1 (Menor)
1. Usuario hace clic en camión y selecciona "Avería Tipo 1"
2. Frontend llama `averiarCamionConNuevaSimulacion()`
3. Backend crea avería pero NO solicita nueva simulación (TI1 es menor)
4. Camión se marca como averiado en UI
5. **No se recalculan rutas** (correcto para TI1)

### Caso: Avería Tipo TI2/TI3 (Significativa)
1. Usuario hace clic en camión y selecciona "Avería Tipo 2" o "Tipo 3"
2. Frontend llama `averiarCamionConNuevaSimulacion()`
3. Backend:
   - Crea la avería
   - Detecta que es TI2/TI3
   - Automáticamente solicita nueva simulación via `gaTriggerQueue`
4. Frontend:
   - Marca camión como averiado en UI
   - Muestra toast "Recalculando rutas..."
   - Espera 2 segundos (para que backend procese)
   - Llama `recargarSimulacionPorAveria()`
   - Recarga datos del backend (que incluyen las nuevas rutas)
   - Muestra toast "Rutas recalculadas exitosamente"

## Diferencias entre Endpoints

### `/averiar-camion` (Endpoint original)
- Solo registra la avería
- Cambia estado del camión
- **No solicita nueva simulación**
- Usar para: Reportes manuales donde no se quiere recálculo automático

### `/averiar-camion-con-nueva-simulacion` (Endpoint nuevo)
- Registra la avería
- Cambia estado del camión  
- **Automáticamente solicita nueva simulación para TI2/TI3**
- Usar para: Flujo normal de la aplicación donde se quiere recálculo automático

## Configuración de Tiempos

- **Espera antes de recargar**: 2 segundos
  - Permite que el backend procese la nueva simulación
  - Configurable en `setTimeout()` en `Mapa.tsx`

- **Duración de toasts**:
  - Error: 3 segundos
  - Avería registrada: 5 segundos  
  - Recalculando: 3 segundos
  - Éxito recálculo: 3 segundos

## Manejo de Errores

### Si falla el registro de avería
- Muestra toast de error
- No intenta recargar simulación
- Estado del camión no cambia

### Si falla la recarga de simulación
- La avería sí se registró correctamente
- Muestra toast de advertencia explicando la situación
- Usuario puede recargar manualmente o continuar

## Testing

### Para probar el flujo:
1. Ejecutar backend y frontend
2. Hacer clic en un camión en el mapa
3. Seleccionar "Avería Tipo 2" o "Tipo 3"
4. Observar:
   - Toast de avería registrada
   - Toast de "Recalculando rutas..."
   - Después de ~2 segundos: toast de "Rutas recalculadas"
   - Las rutas en el mapa se actualizan (si el algoritmo genera una solución diferente)

### Para verificar que funciona:
- Consola del backend debe mostrar: "🔄 Nueva simulación solicitada debido a avería TI2/TI3..."
- Consola del frontend debe mostrar: "🔄 Recargando simulación debido a avería..."
- Network tab debe mostrar llamadas a ambos endpoints

## Código Clave

### Backend - Solicitar nueva simulación:
```java
com.plg.utils.Simulacion.gaTriggerQueue.offer(new Object(), 1, java.util.concurrent.TimeUnit.SECONDS);
```

### Frontend - Recargar datos:
```typescript
await recargarSimulacionPorAveria();
```

### Frontend - Integración completa:
```typescript
// Reportar avería con recálculo automático
await averiarCamionConNuevaSimulacion(camionId, tipo, fechaHoraReporte);

// Para TI2/TI3: esperar y recargar
if (tipo === 2 || tipo === 3) {
  setTimeout(async () => {
    await recargarSimulacionPorAveria();
  }, 2000);
}
```

## Notas Técnicas

- La función `gaTriggerQueue` es parte del sistema de algoritmo genético existente
- El recálculo de rutas depende del algoritmo genético generar una solución diferente
- Si el algoritmo genera la misma solución, las rutas no cambiarán visualmente
- El sistema es resiliente: si falla la recarga, la avería se registra correctamente
