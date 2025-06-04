# Orden de Ejecución y Almacenamiento - Back-end

## 1. Inicio de la Aplicación (Spring Boot Run)

### 1.1. Carga de Configuración
1. **`pom.xml`**
   - Carga de dependencias Maven
   - Configuración de plugins
   - Definición de propiedades del proyecto

2. **`application.properties`**
   - Carga de variables de entorno
   - Configuración de la base de datos
   - Configuración de servidor
   - Configuración de CORS
   - Configuración de logging

3. **`PlgApplication.java`**
   - Punto de entrada de la aplicación
   - Inicialización de Spring Boot
   - Escaneo de componentes

### 1.2. Inicialización de Componentes
1. **Configuración (`config/`)**
   - Carga de configuraciones de seguridad
   - Configuración de CORS
   - Configuración de JPA/Hibernate
   - Configuración de Swagger/OpenAPI

2. **Entidades (`entity/`)**
   - Inicialización de modelos JPA
   - Creación de tablas en base de datos
   - Validación de esquemas

3. **Repositorios**
   - Inicialización de interfaces JPA
   - Conexión con base de datos
   - Configuración de queries

4. **Servicios (`service/`)**
   - Inicialización de lógica de negocio
   - Configuración de transacciones
   - Inyección de dependencias

5. **Controladores (`controller/`)**
   - Registro de endpoints REST
   - Configuración de mapeos
   - Inicialización de validadores

## 2. Flujo de Ejecución en Runtime

### 2.1. Procesamiento de Peticiones
1. **Entrada de Petición**
   - Recepción en `SimulacionController.java`
   - Validación de headers
   - Verificación de CORS

2. **Validación de Datos**
   - Validación de DTOs
   - Verificación de tipos
   - Comprobación de restricciones

3. **Procesamiento de Negocio**
   - Ejecución de lógica en servicios
   - Manejo de transacciones
   - Procesamiento de reglas de negocio

4. **Persistencia de Datos**
   - Operaciones en base de datos
   - Manejo de transacciones
   - Actualización de entidades

5. **Respuesta**
   - Formateo de respuesta
   - Manejo de errores
   - Envío al cliente

### 2.2. Ciclos de Vida de Entidades

#### Pedidos
1. Creación en `Pedido.java`
2. Validación de datos
3. Asignación a camión
4. Actualización de estado
5. Registro de entregas

#### Camiones
1. Inicialización en `Camion.java`
2. Asignación de rutas
3. Control de estado
4. Registro de mantenimientos
5. Actualización de ubicación

#### Almacenes
1. Configuración en `Almacen.java`
2. Gestión de inventario
3. Control de capacidad
4. Registro de operaciones

## 3. Almacenamiento de Datos

### 3.1. Base de Datos
- **Tablas Principales**
  1. `pedidos`
  2. `camiones`
  3. `almacenes`
  4. `rutas`
  5. `mantenimientos`

- **Tablas de Relación**
  1. `pedidos_camiones`
  2. `almacenes_rutas`
  3. `camiones_mantenimientos`

### 3.2. Caché
- **Spring Cache**
  1. Caché de rutas
  2. Caché de estados
  3. Caché de configuraciones

### 3.3. Archivos Temporales
- **Logs**
  1. `application.log`
  2. `error.log`
  3. `access.log`

- **Archivos de Configuración**
  1. `application.properties`
  2. `logback.xml`
  3. `swagger-config.json`

## 4. Ciclos de Mantenimiento

### 4.1. Tareas Programadas
1. Limpieza de logs
2. Optimización de base de datos
3. Verificación de conexiones
4. Actualización de caché

### 4.2. Monitoreo
1. Métricas de rendimiento
2. Estado de servicios
3. Uso de recursos
4. Alertas de sistema

## 5. Cierre de Aplicación

### 5.1. Proceso de Shutdown
1. Finalización de transacciones activas
2. Cierre de conexiones a base de datos
3. Liberación de recursos
4. Guardado de estado

### 5.2. Limpieza
1. Cierre de sesiones
2. Liberación de memoria
3. Finalización de threads
4. Cierre de logs 