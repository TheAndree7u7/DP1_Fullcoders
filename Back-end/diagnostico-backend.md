# Diagnóstico del Backend - PLG Application

## Problemas Identificados y Soluciones

### 1. **Problema Principal: Ejecución Síncrona en el Arranque**
**Problema**: La simulación se ejecutaba de forma síncrona en `PlgApplication.run()`, bloqueando la inicialización del servidor.

**Solución**: 
- ✅ Modificado para ejecución asíncrona con `@Async`
- ✅ El servidor ahora se inicializa completamente antes de ejecutar la simulación

### 2. **Problema de Content-Type: null**
**Problema**: El controlador no especificaba explícitamente el Content-Type en las respuestas.

**Solución**:
- ✅ Agregado `produces = MediaType.APPLICATION_JSON_VALUE`
- ✅ Uso de `ResponseEntity` con Content-Type explícito
- ✅ Manejo de errores con respuestas JSON válidas

### 3. **Logs Deshabilitados**
**Problema**: Los logs estaban en nivel `warn`, dificultando el diagnóstico.

**Solución**:
- ✅ Habilitados logs detallados en `application.properties`
- ✅ Nivel DEBUG para Spring y la aplicación
- ✅ Logs SQL habilitados

### 4. **Manejo de Errores Mejorado**
**Problema**: Errores no manejados causaban respuestas inconsistentes.

**Solución**:
- ✅ Manejo de excepciones específicas en el controlador
- ✅ Timeouts configurables (10 segundos)
- ✅ Códigos de estado HTTP apropiados
- ✅ Mensajes de error descriptivos

## Endpoints de Diagnóstico

### 1. **Estado del Backend**
```bash
GET http://localhost:8085/api/simulacion/status
```
**Respuesta esperada**:
```json
{
  "status": "running",
  "timestamp": 1234567890,
  "simulacionConfigurada": true,
  "pedidosPorAtender": 5,
  "pedidosPlanificados": 10,
  "pedidosEntregados": 15
}
```

### 2. **Mejor Individuo**
```bash
GET http://localhost:8085/api/simulacion/mejor
```
**Respuestas posibles**:
- `200 OK`: Datos de simulación válidos
- `503 Service Unavailable`: Simulación no configurada
- `408 Request Timeout`: Timeout en procesamiento
- `500 Internal Server Error`: Error interno

## Comandos de Diagnóstico

### 1. **Verificar si el servidor está corriendo**
```bash
curl -X GET http://localhost:8085/api/simulacion/status
```

### 2. **Verificar logs del servidor**
```bash
# En la consola donde ejecutas el backend, deberías ver:
🚀 Iniciando aplicación PLG...
📊 Configurando simulación...
✅ Servidor iniciado correctamente en puerto 8085
🔄 Iniciando simulación en segundo plano...
```

### 3. **Verificar endpoints disponibles**
```bash
curl -X GET http://localhost:8085/api/almacenes
curl -X GET http://localhost:8085/api/camiones
curl -X GET http://localhost:8085/api/pedidos
```

## Pasos para Solucionar Problemas

### 1. **Si el backend no inicia**
1. Verificar que el puerto 8085 esté libre
2. Verificar que Java 17+ esté instalado
3. Ejecutar: `mvn clean install`
4. Ejecutar: `mvn spring-boot:run`

### 2. **Si el frontend no puede conectar**
1. Verificar que el backend esté corriendo en puerto 8085
2. Verificar la variable de entorno `VITE_API_BASE_URL`
3. Probar el endpoint de estado primero

### 3. **Si hay errores de Content-Type**
1. Verificar que el controlador use `MediaType.APPLICATION_JSON_VALUE`
2. Verificar que las respuestas usen `ResponseEntity`
3. Revisar logs del servidor para errores

### 4. **Si la simulación no funciona**
1. Verificar que los archivos de datos existan
2. Verificar logs de inicialización
3. Probar el endpoint de estado

## Variables de Entorno Requeridas

### Frontend (.env)
```env
VITE_API_BASE_URL=http://localhost:8085/api
```

### Backend (application.properties)
```properties
server.port=8085
logging.level.com.plg=DEBUG
```

## Archivos de Datos Requeridos

Verificar que existan estos archivos en `src/main/resources/data/`:
- `pedidos/ventas202502.txt`
- `mantenimientos/mantpreventivo.txt`
- `bloqueos/202502.bloqueos.txt`
- `averias/averias.v1.txt`
- `almacenes/almacenes.txt`
- `camiones/camiones.txt` 