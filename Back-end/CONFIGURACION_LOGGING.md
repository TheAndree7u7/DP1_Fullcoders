# 📄 Configuración de Logging - Backend

## ✅ **Sistema Configurado**

He configurado el sistema de logging para que **todos los logs se guarden en un archivo** que se elimina en cada ejecución de la aplicación.

## 📁 **Ubicación del Archivo de Logs**

```
[DIRECTORIO_PROYECTO]/Back-end/logs/application.log
```

**Ejemplo:**
```
E:/PROYECTOS/DP1/DP1_2025/DP1_Fullcoders_PRUEBAS/Back-end/logs/application.log
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

### 1. **logback-spring.xml** (✅ Corregido)
```xml
<!-- Definir la ruta del archivo de logs relativa al directorio del proyecto -->
<property name="LOG_FILE" value="${user.dir}/Back-end/logs/application.log"/>

<!-- Appender para archivo que se borra en cada ejecución -->
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>${LOG_FILE}</file>
    <append>false</append> <!-- false = sobrescribe en cada ejecución -->
    <encoder>
        <charset>UTF-8</charset>
        <pattern>${FILE_LOG_PATTERN}</pattern>
    </encoder>
</appender>
```

**Mejoras aplicadas:**
- ✅ Ruta absoluta basada en `${user.dir}` (directorio desde donde se ejecuta Java)
- ✅ Codificación UTF-8 para manejar emojis correctamente
- ✅ Creación automática del directorio de logs

### 2. **LoggingConfig.java** (Nuevo)
- Redirige `System.out` y `System.err` al sistema de logging
- Mantiene la salida por consola
- Configura automáticamente al iniciar la aplicación

### 3. **Directorio logs/**
- Creado automáticamente
- Contiene `.gitignore` para evitar subir logs al repositorio

### 4. **ver_logs.bat** (Nuevo)
```batch
@echo off
echo 📄 Mostrando archivo de logs...
echo 📁 Ubicación: %CD%\Back-end\logs\application.log
type "Back-end\logs\application.log"
```

**Utilidad:** Script para Windows que muestra el contenido del archivo de logs

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
```bash
cd Back-end/plg
mvn spring-boot:run
```

2. **Verifica la ubicación del archivo** (se muestra al iniciar)
```
📄 Todos los logs se guardarán en: E:/PROYECTOS/DP1/DP1_2025/DP1_Fullcoders_PRUEBAS/Back-end/logs/application.log
```

3. **Usa el script de verificación** (Windows)
```bash
# Desde el directorio raíz del proyecto
ver_logs.bat
```

4. **O revisa manualmente el archivo**
```bash
# Verificar que el archivo existe
ls -la Back-end/logs/application.log

# Ver el contenido
cat Back-end/logs/application.log
```

## ⚠️ **Notas Importantes**

- El archivo se **sobrescribe completamente** en cada ejecución
- Los logs anteriores se **pierden** al reiniciar la aplicación
- El directorio `logs/` está en `.gitignore` para evitar subir logs al repositorio
- La configuración mantiene **ambas salidas**: consola y archivo

## 🎯 **Resultado Final**

**Antes**: Los logs solo aparecían en la consola
**Después**: Los logs aparecen en la consola **Y** se guardan en `logs/application.log`

✅ **Configuración completa y funcionando** 