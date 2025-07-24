# 🚀 Guía de Uso - TestSimulacionController con Postman

Esta guía te ayudará a importar y usar la colección de Postman para probar el `TestSimulacionController`.

## 📁 Archivos Incluidos

1. **`TestSimulacionController_Postman_Collection.json`** - Colección principal con todos los endpoints
2. **`TestSimulacionController_Postman_Environment.json`** - Variables de entorno
3. **`README_POSTMAN_TEST_SIMULACION.md`** - Esta guía

## 🔧 Configuración Inicial

### Paso 1: Importar la Colección
1. Abre Postman
2. Haz clic en **"Import"** en la esquina superior izquierda
3. Selecciona el archivo `TestSimulacionController_Postman_Collection.json`
4. Haz clic en **"Import"**

### Paso 2: Importar el Environment
1. Haz clic en **"Import"** nuevamente
2. Selecciona el archivo `TestSimulacionController_Postman_Environment.json`
3. Haz clic en **"Import"**

### Paso 3: Seleccionar el Environment
1. En la esquina superior derecha, haz clic en el dropdown de environments
2. Selecciona **"TestSimulacionController - Environment"**

## 🎯 Endpoints Disponibles

### 1. **Ejecutar Simulación en Bucle**
- **Método**: POST
- **URL**: `{{baseUrl}}/api/test-simulacion/ejecutar-bucle`
- **Descripción**: Inicia simulaciones automáticas entre dos fechas

**Parámetros**:
- `fechaInicio`: Fecha de inicio (formato: yyyy-MM-ddTHH:mm:ss)
- `fechaFin`: Fecha de fin (formato: yyyy-MM-ddTHH:mm:ss)
- `intervaloMinutos`: Intervalo entre simulaciones (opcional, default: 30)

### 2. **Cancelar Simulación**
- **Método**: POST
- **URL**: `{{baseUrl}}/api/test-simulacion/cancelar`
- **Descripción**: Cancela la simulación en bucle en proceso

### 3. **Obtener Estado**
- **Método**: GET
- **URL**: `{{baseUrl}}/api/test-simulacion/estado`
- **Descripción**: Obtiene el estado actual de la simulación

### 4. **Obtener Logs**
- **Método**: GET
- **URL**: `{{baseUrl}}/api/test-simulacion/logs`
- **Descripción**: Obtiene el historial de logs detallado

### 5. **Obtener Estadísticas**
- **Método**: GET
- **URL**: `{{baseUrl}}/api/test-simulacion/estadisticas`
- **Descripción**: Obtiene las estadísticas detalladas de pedidos de todas las simulaciones ejecutadas

### 6. **Obtener Última Estadística**
- **Método**: GET
- **URL**: `{{baseUrl}}/api/test-simulacion/estadisticas/ultima`
- **Descripción**: Obtiene las estadísticas de pedidos de la última simulación ejecutada

### 7. **Obtener Resumen de Estadísticas**
- **Método**: GET
- **URL**: `{{baseUrl}}/api/test-simulacion/estadisticas/resumen`
- **Descripción**: Obtiene un resumen completo de todas las estadísticas de pedidos en formato legible

## 🧪 Ejemplos de Pruebas

### Ejemplo 1: Prueba Rápida (6 horas)
1. Abre **"Ejemplo 3: Simulación Rápida (15 min)"**
2. Haz clic en **"Send"**
3. Verifica que recibas una respuesta exitosa
4. Usa **"Verificar Estado"** para monitorear el progreso

### Ejemplo 2: Prueba Diaria Completa
1. Abre **"Ejemplo 1: Simulación Diaria (30 min)"**
2. Haz clic en **"Send"**
3. Monitorea con **"Verificar Estado"** cada 30 segundos
4. Revisa los logs con **"Ver Logs Recientes"**

### Ejemplo 3: Prueba Intensiva (Mensual)
⚠️ **ADVERTENCIA**: Esta prueba ejecuta 720 simulaciones y puede tomar mucho tiempo.

1. Abre **"Ejemplo 4: Simulación Mensual (1 hora)"**
2. Haz clic en **"Send"**
3. Monitorea el progreso regularmente
4. Usa **"Cancelar Simulación"** si necesitas detenerla

## 📊 Monitoreo en Tiempo Real

### Verificar Progreso
```bash
# En Postman, usa estos endpoints alternadamente:
GET {{baseUrl}}/api/test-simulacion/estado
GET {{baseUrl}}/api/test-simulacion/logs
```

### Analizar Estadísticas de Pedidos
```bash
# Para análisis detallado:
GET {{baseUrl}}/api/test-simulacion/estadisticas

# Para ver solo la última simulación:
GET {{baseUrl}}/api/test-simulacion/estadisticas/ultima

# Para un resumen completo y legible:
GET {{baseUrl}}/api/test-simulacion/estadisticas/resumen
```

### Respuestas Esperadas

**Estado**:
```json
"En proceso: SÍ | Total simulaciones ejecutadas: 15"
```

**Logs**:
```json
[
  "[2025-01-01T10:30:00] 🚀 Simulación en bucle iniciada",
  "[2025-01-01T10:30:01] 📅 Rango: 2025-01-01T00:00:00 hasta 2025-01-02T00:00:00",
  "[2025-01-01T10:30:02] ⏱️ Intervalo: 30 minutos",
  "[2025-01-01T10:30:03] 🔄 Ejecutando simulación para: 2025-01-01T00:00:00",
  "[2025-01-01T10:30:05] ✅ Simulación #1 completada para: 2025-01-01T00:00:00",
  "[2025-01-01T10:30:05] 📦 Pedidos procesados: 15",
  "[2025-01-01T10:30:05] 🧬 Fitness: 1250.5"
]
```

## ⚠️ Consideraciones Importantes

### 1. **Una Simulación a la Vez**
- Solo se puede ejecutar una simulación en bucle simultáneamente
- Si intentas iniciar otra, recibirás un error 409 (Conflict)

### 2. **Recursos del Sistema**
- Las simulaciones intensivas consumen CPU y memoria
- Monitorea el rendimiento de tu sistema
- Considera usar intervalos más grandes para pruebas largas

### 3. **Tiempo de Ejecución**
- **Simulación rápida (6h)**: ~5-10 minutos
- **Simulación diaria (24h)**: ~20-30 minutos
- **Simulación semanal (7 días)**: ~2-3 horas
- **Simulación mensual (30 días)**: ~8-12 horas

### 4. **Logs Volátiles**
- Los logs se mantienen en memoria
- Se pierden al reiniciar la aplicación
- Máximo 1000 entradas de log

## 🔄 Flujo de Trabajo Recomendado

### Para Pruebas de Desarrollo
1. **Inicia** con "Simulación Rápida (15 min)"
2. **Monitorea** el estado cada 30 segundos
3. **Revisa** los logs para verificar el progreso
4. **Cancela** si necesitas detener la prueba

### Para Pruebas de Integración
1. **Inicia** con "Simulación Diaria (30 min)"
2. **Monitorea** el estado cada 2-3 minutos
3. **Revisa** los logs periódicamente
4. **Espera** a que termine completamente

### Para Pruebas de Rendimiento
1. **Inicia** con "Simulación Semanal (2 horas)"
2. **Monitorea** el rendimiento del sistema
3. **Revisa** los logs para detectar problemas
4. **Cancela** si el sistema se sobrecarga

## 🛠️ Solución de Problemas

### Error 409: "Ya hay una simulación en proceso"
**Solución**: Usa **"Cancelar Simulación"** antes de iniciar una nueva

### Error 400: "La fecha de inicio debe ser anterior a la fecha de fin"
**Solución**: Verifica que `fechaInicio` sea anterior a `fechaFin`

### Error 500: "Error al iniciar simulación en bucle"
**Solución**: 
1. Verifica que el servidor esté ejecutándose
2. Revisa los logs del servidor
3. Asegúrate de que los archivos de datos estén disponibles

### Simulación muy lenta
**Solución**:
1. Aumenta el `intervaloMinutos`
2. Reduce el rango de fechas
3. Monitorea el uso de CPU y memoria

## 📝 Personalización

### Modificar Variables de Entorno
1. Haz clic en el icono de engranaje (⚙️) en la esquina superior derecha
2. Selecciona **"TestSimulacionController - Environment"**
3. Modifica los valores según tus necesidades

### Crear Nuevos Ejemplos
1. Duplica un request existente
2. Modifica los parámetros en el body
3. Cambia el nombre y descripción
4. Guarda la colección

## 🎉 ¡Listo para Probar!

Ahora tienes todo lo necesario para probar el `TestSimulacionController` de manera eficiente. 

**¡Recuerda siempre monitorear el progreso y cancelar si es necesario!** 