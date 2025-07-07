# 📄 Configuración de Logging - Backend

## ✅ **Sistema Configurado**

He configurado el sistema de logging para que **todos los logs se guarden en un archivo** que se elimina en cada ejecución de la aplicación.

## 📁 **Ubicación del Archivo de Logs**

```
Back-end/logs/application.log
```

## 🔧 **Características del Sistema**

### ✅ **Eliminación Automática**
- El archivo `application.log` se **elimina automáticamente** en cada ejecución
- No se acumulan logs de ejecuciones anteriores

### ✅ **Captura Completa**
- **System.out.println()** → Se guarda en el archivo
- **System.err.println()** → Se guarda en el archivo
- **Logs de Spring Boot** → Se guarda en el archivo
- **Logs personalizados** → Se guarda en el archivo
- **Logs de la aplicación** → Se guarda en el archivo

### ✅ **Doble Salida**
- **Consola**: Sigues viendo los logs en la terminal
- **Archivo**: Todos los logs se guardan en `logs/application.log`

## 📋 **Formato del Archivo de Logs**

```
2025-01-07 12:30:45.123 INFO 12345 --- [main] com.plg.service.AveriaService : 🚛💥 BACKEND: Procesando avería
2025-01-07 12:30:45.124 INFO 12345 --- [main] SYSTEM_OUT : ✅ BACKEND: Avería creada exitosamente
2025-01-07 12:30:45.125 ERROR 12345 --- [main] SYSTEM_ERR : ❌ BACKEND: Error en proceso
```

## 🛠️ **Archivos Modificados**

### 1. **logback-spring.xml**
```xml
<!-- Appender para archivo que se borra en cada ejecución -->
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>logs/application.log</file>
    <append>false</append> <!-- false = sobrescribe en cada ejecución -->
    <encoder>
        <pattern>${FILE_LOG_PATTERN}</pattern>
    </encoder>
</appender>
```

### 2. **LoggingConfig.java** (Nuevo)
- Redirige `System.out` y `System.err` al sistema de logging
- Mantiene la salida por consola
- Configura automáticamente al iniciar la aplicación

### 3. **Directorio logs/**
- Creado automáticamente
- Contiene `.gitignore` para evitar subir logs al repositorio

## 🚀 **Uso**

### **Automático**
No necesitas hacer nada especial. Al ejecutar la aplicación:

```bash
cd Back-end/plg
mvn spring-boot:run
```

### **Durante la Ejecución**
- Todos los logs aparecen en la consola **Y** se guardan en el archivo
- El archivo se actualiza en tiempo real

### **Después de la Ejecución**
- Revisa el archivo: `Back-end/logs/application.log`
- Contiene TODOS los logs de la última ejecución

## 📊 **Niveles de Log Configurados**

| Componente | Nivel | Descripción |
|------------|-------|-------------|
| `com.plg.*` | DEBUG | Todos los logs de la aplicación |
| `SYSTEM_OUT` | INFO | System.out.println() |
| `SYSTEM_ERR` | ERROR | System.err.println() |
| `org.springframework` | INFO | Logs de Spring Boot |
| `ROOT` | INFO | Todos los demás logs |

## 🔍 **Verificación**

Para verificar que funciona correctamente:

1. **Ejecuta la aplicación**
2. **Verifica que existe el archivo**: `Back-end/logs/application.log`
3. **Ejecuta una operación** (como crear una avería)
4. **Revisa el archivo** para confirmar que los logs se están guardando

## ⚠️ **Notas Importantes**

- El archivo se **sobrescribe completamente** en cada ejecución
- Los logs anteriores se **pierden** al reiniciar la aplicación
- El directorio `logs/` está en `.gitignore` para evitar subir logs al repositorio
- La configuración mantiene **ambas salidas**: consola y archivo

## 🎯 **Resultado Final**

**Antes**: Los logs solo aparecían en la consola
**Después**: Los logs aparecen en la consola **Y** se guardan en `logs/application.log`

✅ **Configuración completa y funcionando** 