# LoggerUtil - Ejemplos de uso

Esta clase permite registrar logs en consola y en archivos separados por servicio. Puedes usarla para tener trazabilidad de diferentes partes de tu backend.

## Ejemplo básico (logs generales)
```java
LoggerUtil.log("Mensaje informativo");
LoggerUtil.logWarning("Mensaje de advertencia");
LoggerUtil.logError("Mensaje de error");
LoggerUtil.logAlways("Este mensaje SIEMPRE se muestra, aunque ON_LOGS_ON esté en false");
```

## Logs en archivos por servicio

### Algoritmo Genético
```java
LoggerUtil.logAlgoritmoGenetico("Inicio del algoritmo genético");
LoggerUtil.logAlgoritmoGeneticoError("Error crítico en el algoritmo genético");
```
Esto crea o escribe en el archivo `AlgoritmoGenetico.log`.

### Simulación
```java
LoggerUtil.logSimulacion("Simulación iniciada");
LoggerUtil.logSimulacionError("Error en la simulación");
```
Esto crea o escribe en el archivo `Simulacion.log`.

### Servicio personalizado (genérico)
```java
LoggerUtil.logServicio("Pedidos", "Procesando pedido #12345", "INFO");
LoggerUtil.logServicio("Pedidos", "Error al validar pedido #12345", "ERROR");
```
Esto crea o escribe en el archivo `Pedidos.log`.

## Notas
- Los logs de archivos por servicio NO se muestran en consola, solo en el archivo correspondiente.
- Puedes crear tantos archivos de log como servicios necesites, solo cambiando el primer parámetro.
- Los logs generales (log, logWarning, logError, logAlways) respetan la variable ON_LOGS_ON, excepto logAlways.
