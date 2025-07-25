# Configuración de Logging - Backend

## Problema Identificado
El sistema no estaba guardando logs en el archivo `application.log` debido a una configuración incorrecta en `logback-spring.xml`. Además, los `System.out.println` no se estaban capturando en el archivo de log, y había problemas con la codificación de caracteres (tildes).

## Solución Implementada

### 1. Corrección de logback-spring.xml
Se configuró correctamente el appender `FILE` con rotación y se agregó el appender `CLEAN_FILE` que borra el archivo al inicio de cada ejecución.

### 2. Captura de System.out.println
Se creó la clase `LoggingConfig.java` que redirige `System.out` y `System.err` al sistema de logging de Logback.

```java
@Configuration
public class LoggingConfig {
    @PostConstruct
    public void redirectSystemOut() {
        // Redirige System.out al logger
        System.setOut(new PrintStream(new OutputStream() {
            // Captura cada línea y la envía al logger
        }, true));
        
        // Redirige System.err al logger
        System.setErr(new PrintStream(new OutputStream() {
            // Captura cada línea de error y la envía al logger
        }, true));
    }
}
```

### 3. Configuración de Codificación UTF-8
Se agregó configuración UTF-8 en todos los appenders y en application.properties para manejar correctamente las tildes y caracteres especiales.

```xml
<encoder>
    <charset>UTF-8</charset>
    <pattern>${FILE_LOG_PATTERN}</pattern>
</encoder>
```

```properties
# Encoding Configuration
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true
```

### 4. Configuración Actual
```xml
<!-- Appender que borra el archivo al inicio -->
<appender name="CLEAN_FILE" class="ch.qos.logback.core.FileAppender">
    <file>logs/application.log</file>
    <append>false</append> <!-- Esto hace que se borre el archivo al inicio -->
    <encoder>
        <charset>UTF-8</charset>
        <pattern>${FILE_LOG_PATTERN}</pattern>
    </encoder>
</appender>
```

### 5. Niveles de Log Configurados
- **Root**: INFO
- **com.plg**: DEBUG
- **Spring Framework**: INFO
- **Hibernate**: INFO
- **System.out**: INFO (con prefijo [STDOUT])
- **System.err**: ERROR (con prefijo [STDERR])

## Ubicación de los Logs
- **Archivo principal**: `Back-end/plg/logs/application.log`
- **Archivos rotados**: `Back-end/plg/logs/application.YYYY-MM-DD.log`

## Tipos de Logs Capturados

### 1. Logs del Sistema de Logging
```
2025-01-15 10:30:45.123 [main] INFO  com.plg.PlgApplication - Started PlgApplication
2025-01-15 10:30:45.456 [http-nio-8085-exec-1] DEBUG com.plg.controller.SimulacionController - Iniciando simulación
```

### 2. System.out.println
```
2025-01-15 10:30:45.789 [main] INFO  com.plg.config.LoggingConfig - [STDOUT] Mensaje desde System.out.println
```

### 3. System.err.println
```
2025-01-15 10:30:45.790 [main] ERROR com.plg.config.LoggingConfig - [STDERR] Mensaje de error desde System.err.println
```

## Verificación del Funcionamiento

### 1. Verificar que el directorio logs existe
```powershell
cd Back-end\plg
ls logs
```

### 2. Verificar el contenido del archivo de log
```powershell
Get-Content logs\application.log -Encoding UTF8
```

### 3. Monitorear logs en tiempo real
```powershell
Get-Content logs\application.log -Wait -Encoding UTF8
```

### 4. Verificar logs con filtros
```powershell
# Ver solo logs de error
Get-Content logs\application.log -Encoding UTF8 | Select-String "ERROR"

# Ver logs de System.out
Get-Content logs\application.log -Encoding UTF8 | Select-String "STDOUT"

# Ver logs de un paquete específico
Get-Content logs\application.log -Encoding UTF8 | Select-String "com.plg"
```

## Comandos Útiles

### Limpiar archivo de log
```powershell
Clear-Content logs\application.log
```

### Ver tamaño del archivo
```powershell
(Get-Item logs\application.log).Length
```

### Ver últimos 10 líneas
```powershell
Get-Content logs\application.log -Tail 10 -Encoding UTF8
```

### Usar el script de gestión
```powershell
.\manage-logs.ps1 help
.\manage-logs.ps1 show
.\manage-logs.ps1 tail
.\manage-logs.ps1 watch
```

## Configuración en application.properties
```properties
# Logging Configuration
logging.level.root=INFO
logging.level.org.springframework=INFO
logging.level.com.plg=DEBUG
logging.level.org.springframework.web.socket=INFO
logging.level.org.springframework.boot=INFO
logging.level.org.hibernate=INFO

# Encoding Configuration
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true
```

## Archivos de Configuración
- **Logback**: `src/main/resources/logback-spring.xml`
- **LoggingConfig**: `src/main/java/com/plg/config/LoggingConfig.java`
- **Properties**: `src/main/resources/application.properties`
- **Script de gestión**: `manage-logs.ps1`

## Notas Importantes
1. Los logs se guardan en el directorio `logs/` relativo al directorio de trabajo de la aplicación
2. **El archivo se borra automáticamente al inicio de cada ejecución**
3. El archivo se rota diariamente y se mantienen logs de los últimos 30 días
4. El tamaño máximo total de logs es 3GB
5. En Windows, se usa `withJansi=true` para colores en consola
6. **System.out.println** ahora se captura automáticamente y se guarda en el archivo de log
7. **System.err.println** se captura como errores y se guarda en el archivo de log
8. **Codificación UTF-8** configurada para manejar correctamente tildes y caracteres especiales

## Troubleshooting

### Si no se crean logs:
1. Verificar que el directorio `logs/` existe
2. Verificar permisos de escritura
3. Reiniciar la aplicación después de cambios en logback-spring.xml

### Si los logs no aparecen:
1. Verificar el nivel de logging configurado
2. Asegurar que la aplicación esté generando logs
3. Verificar que no haya errores en la configuración de logback

### Si System.out.println no aparece:
1. Verificar que la clase `LoggingConfig` esté siendo cargada
2. Reiniciar la aplicación completamente
3. Verificar que no haya errores en la consola de Spring Boot

### Si las tildes no se muestran correctamente:
1. Verificar que se use `-Encoding UTF8` en los comandos de PowerShell
2. Asegurar que la configuración UTF-8 esté activa en application.properties
3. Reiniciar la aplicación después de cambios en la configuración de codificación 