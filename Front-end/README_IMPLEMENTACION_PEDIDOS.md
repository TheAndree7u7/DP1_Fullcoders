# Implementación de Pedidos con Recálculo Automático

## 📋 Descripción

Se ha implementado la funcionalidad para agregar pedidos (individuales o por archivo) con recálculo automático del algoritmo genético, siguiendo el mismo patrón que las averías.

## 🔧 Componentes Modificados

### 1. **Servicio de Archivos** (`src/services/archivosApiService.ts`)
- **`ArchivosApiService.procesarPedidosIndividuales()`**: Procesa pedidos individuales
- **`ArchivosApiService.procesarArchivoPedidos()`**: Procesa archivos completos
- Maneja las llamadas al backend `/api/archivos/pedidos/individuales`

### 2. **Modal de Pedidos** (`src/components/ModalAgregarPedidos.tsx`)
- Integrado con el contexto de simulación
- Función `handleProcesarPedidos()` que:
  - Detiene polling y pausa simulación
  - Envía pedidos al backend
  - Recalcula algoritmo genético
  - Aplica nueva solución
  - Reanuda simulación
  - Muestra notificaciones

## 🔄 Flujo de Procesamiento

### Para Pedidos Individuales:
1. **Usuario llena formulario** y hace clic en "Agregar Pedido"
2. **Validaciones**: Coordenadas, horas límite ≥ 4, volumen GLP > 0
3. **Detener simulación**: Polling y simulación se pausan
4. **Enviar al backend**: `POST /api/archivos/pedidos/individuales`
5. **Recalcular algoritmo**: `GET /api/simulacion/mejor?fecha=...`
6. **Aplicar solución**: Nueva solución se aplica al contexto
7. **Reanudar simulación**: Polling y simulación se reactivan
8. **Notificación**: Toast de éxito/error

### Para Archivos:
1. **Usuario selecciona archivo** y hace clic en "Agregar Archivo"
2. **Validación**: Formato del archivo y contenido
3. **Mismo flujo**: Igual que pedidos individuales

## 📡 Endpoints Utilizados

### Backend:
- `POST /api/archivos/pedidos/individuales`: Procesar pedidos
- `GET /api/simulacion/mejor?fecha=...`: Recalcular algoritmo

### Frontend:
- `ArchivosApiService.procesarPedidosIndividuales()`
- `getMejorIndividuo()` del servicio de simulación
- `aplicarNuevaSolucionDespuesAveria()` del contexto

## 🎯 Funcionalidades Clave

### 1. **Integración con Contexto de Simulación**
```typescript
const { 
  fechaHoraSimulacion, 
  horaSimulacion, 
  aplicarNuevaSolucionDespuesAveria,
  setPollingActivo,
  setSimulacionActiva
} = useSimulacion();
```

### 2. **Cálculo de Timestamp**
```typescript
const timestampSimulacion = calcularTimestampSimulacion(
  fechaHoraSimulacion,
  horaSimulacion
);
```

### 3. **Recálculo y Aplicación**
```typescript
const nuevaSolucion = await getMejorIndividuo(timestampSimulacion || "");
await aplicarNuevaSolucionDespuesAveria(nuevaSolucion);
```

### 4. **Manejo de Estados**
- **Pausar**: `setPollingActivo(false)` y `setSimulacionActiva(false)`
- **Reanudar**: `setSimulacionActiva(true)`
- **Error**: Reanudar automáticamente en caso de error

## 🔔 Notificaciones

### Éxito:
```typescript
toast.success(`✅ Pedidos agregados exitosamente: ${response.mensaje} - Algoritmo recalculado`);
```

### Error:
```typescript
toast.error(`❌ Error al procesar pedidos: ${error.message}`);
```

## 📊 Logs de Consola

### Procesamiento:
- `📦 PROCESANDO PEDIDOS: Iniciando procesamiento...`
- `🛑 DETENIENDO POLLING Y PAUSANDO SIMULACIÓN...`
- `📡 ENVIANDO PEDIDOS AL BACKEND...`
- `🧬 RECALCULANDO ALGORITMO GENÉTICO...`
- `🔄 APLICANDO NUEVA SOLUCIÓN...`
- `▶️ REANUDANDO SIMULACIÓN...`

### Completado:
- `✅ PROCESAMIENTO COMPLETADO: { pedidosAgregados, algoritmoRecalculado, nuevaSolucionAplicada }`

## 🔧 Compatibilidad

### Mantiene Funciones Originales:
- `onAgregarPedido()`: Llamada después del procesamiento
- `onAgregarArchivo()`: Llamada después del procesamiento
- Formato de datos: Sin cambios

### Validaciones Existentes:
- Coordenadas: 0-69 (X), 0-49 (Y)
- Horas límite: ≥ 4 horas
- Volumen GLP: > 0
- Formato archivo: `ventasYYYYMM.txt`

## 🚀 Uso

### Pedido Individual:
1. Abrir modal desde TablaPedidos
2. Seleccionar "Pedido Individual"
3. Llenar formulario
4. Hacer clic en "Agregar Pedido"
5. Esperar procesamiento y recálculo

### Archivo:
1. Abrir modal desde TablaPedidos
2. Seleccionar "Archivo de Pedidos"
3. Arrastrar archivo o hacer clic para seleccionar
4. Validar formato
5. Hacer clic en "Agregar Archivo"
6. Esperar procesamiento y recálculo

## ⚠️ Consideraciones

### 1. **Dependencias**
- `react-toastify`: Para notificaciones
- `useSimulacion`: Contexto de simulación
- `ArchivosApiService`: Servicio de archivos

### 2. **Manejo de Errores**
- Simulación se reanuda automáticamente en caso de error
- Notificaciones informan al usuario
- Logs detallados en consola

### 3. **Performance**
- Polling se detiene durante el procesamiento
- Simulación se pausa para evitar conflictos
- Recálculo usa timestamp actual de simulación

## 🔍 Debugging

### Logs Importantes:
- Timestamp de simulación usado
- Respuesta del backend
- Estado de la nueva solución
- Errores de procesamiento

### Verificación:
- Pedidos aparecen en TablaPedidos
- Algoritmo se recalcula (nuevas rutas)
- Simulación continúa normalmente
- Notificaciones se muestran correctamente 