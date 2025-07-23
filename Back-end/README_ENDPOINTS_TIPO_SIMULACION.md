# Endpoints para Cambio de Tipo de Simulación

## Resumen

Se han implementado dos nuevos endpoints en el `SimulacionController` para permitir cambiar y consultar el tipo de simulación de parámetros:

1. **POST** `/api/simulacion/cambiar-tipo-simulacion` - Cambia el tipo de simulación
2. **GET** `/api/simulacion/tipo-simulacion-actual` - Consulta el tipo de simulación actual

## Archivos Creados/Modificados

### Nuevos Archivos:
- `src/main/java/com/plg/dto/request/TipoSimulacionRequest.java` - DTO para la solicitud
- `src/main/java/com/plg/dto/response/TipoSimulacionResponse.java` - DTO para la respuesta
- `API_CAMBIO_TIPO_SIMULACION.md` - Documentación completa de la API
- `TestSimulacionController_Postman_Collection_TipoSimulacion.json` - Colección de Postman
- `TestSimulacionController_Postman_Environment_TipoSimulacion.json` - Variables de entorno
- `README_ENDPOINTS_TIPO_SIMULACION.md` - Este archivo

### Archivos Modificados:
- `src/main/java/com/plg/controller/SimulacionController.java` - Agregados los nuevos endpoints

## Tipos de Simulación Disponibles

El sistema soporta tres tipos de simulación definidos en el enum `TipoDeSimulacion`:

- **DIARIA** - Simulación diaria
- **SEMANAL** - Simulación semanal (valor por defecto)
- **COLAPSO** - Simulación de colapso

## Uso de los Endpoints

### 1. Cambiar Tipo de Simulación

```bash
curl -X POST http://localhost:8080/api/simulacion/cambiar-tipo-simulacion \
  -H "Content-Type: application/json" \
  -d '{
    "tipoSimulacion": "DIARIA"
  }'
```

**Respuesta exitosa:**
```json
{
    "tipoSimulacionAnterior": "SEMANAL",
    "tipoSimulacionNuevo": "DIARIA",
    "mensaje": "Tipo de simulación cambiado exitosamente de SEMANAL a DIARIA",
    "exito": true
}
```

### 2. Consultar Tipo Actual

```bash
curl -X GET http://localhost:8080/api/simulacion/tipo-simulacion-actual
```

**Respuesta:**
```json
{
    "tipoSimulacion": "SEMANAL",
    "descripcion": "Simulación semanal - Simula una semana completa de operaciones",
    "timestamp": "2025-01-15T10:30:00"
}
```

## Características Implementadas

### ✅ Validación de Entrada
- Validación de tipo de simulación nulo
- Validación de tipos de simulación válidos (enum)

### ✅ Manejo de Errores
- Respuestas HTTP apropiadas (400, 500)
- Mensajes de error descriptivos
- Logs detallados para auditoría

### ✅ Logs del Sistema
- Registro de todas las operaciones
- Información detallada de cambios
- Trazabilidad completa

### ✅ Documentación
- Documentación completa de la API
- Ejemplos de uso con curl
- Colección de Postman para pruebas

## Impacto en el Sistema

El cambio de tipo de simulación afecta a:

1. **Algoritmo Genético** - Puede cambiar la configuración de parámetros
2. **Averías Automáticas** - Solo se aplican en simulación semanal
3. **Intervalos de Tiempo** - Pueden variar según el tipo
4. **Configuración de Parámetros** - Se ajustan automáticamente

## Pruebas

Para probar los endpoints:

1. **Importar la colección de Postman:**
   - `TestSimulacionController_Postman_Collection_TipoSimulacion.json`

2. **Importar las variables de entorno:**
   - `TestSimulacionController_Postman_Environment_TipoSimulacion.json`

3. **Ejecutar las pruebas en orden:**
   - Consultar tipo actual
   - Cambiar a simulación diaria
   - Verificar el cambio
   - Cambiar a simulación de colapso
   - Probar casos de error

## Consideraciones de Seguridad

- Los endpoints están protegidos con CORS configurado para `*`
- Se recomienda configurar CORS específico para producción
- Los cambios se mantienen en memoria (no persistentes)
- Se registran todos los cambios para auditoría

## Próximos Pasos

1. **Integración con Frontend** - Crear componentes para cambiar tipo de simulación
2. **Persistencia** - Guardar configuración en base de datos
3. **Validaciones Adicionales** - Verificar compatibilidad con datos cargados
4. **Tests Unitarios** - Agregar pruebas automatizadas
5. **Configuración por Usuario** - Permitir diferentes tipos por usuario

## Contacto

Para dudas o problemas con estos endpoints, revisar:
- Logs del sistema para errores
- Documentación en `API_CAMBIO_TIPO_SIMULACION.md`
- Colección de Postman para ejemplos de uso 