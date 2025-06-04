# Guía de Iniciación al Back-end

## Estructura del Proyecto Back-end

El Back-end está construido con Spring Boot (Java), siguiendo una arquitectura en capas y patrones de diseño.

### Estructura de Carpetas y Archivos

#### Archivos Raíz
- **`pom.xml`**
  - Configuración de Maven
  - Dependencias del proyecto
  - Plugins y configuraciones de build

- **`mvnw` y `mvnw.cmd`**
  - Wrapper de Maven
  - Scripts para ejecutar Maven sin instalación

#### Estructura Principal (`src/main/java/com/plg/`)

##### 1. `config/`
Configuraciones de la aplicación:
- Configuraciones de Spring Boot
- Configuraciones de seguridad
- Configuraciones de base de datos

##### 2. `controller/`
Controladores REST:
- **`SimulacionController.java`**
  - Endpoints de simulación
  - Manejo de peticiones de simulación
  - Validación de datos de simulación

##### 3. `dto/`
Objetos de Transferencia de Datos:
- Estructuras de datos para transferencia
- Mapeo entre entidades y DTOs
- Validaciones de datos

##### 4. `entity/`
Entidades de la base de datos:
- **`Almacen.java`**
  - Gestión de almacenes
  - Ubicaciones y capacidades
  - Relaciones con pedidos

- **`AsignacionCamion.java`**
  - Asignación de camiones a rutas
  - Gestión de turnos
  - Estado de asignación

- **`Averia.java`**
  - Registro de averías
  - Estado de reparación
  - Impacto en rutas

- **`Bloqueo.java`**
  - Gestión de bloqueos de ruta
  - Tiempos y duraciones
  - Alternativas de ruta

- **`Camion.java`**
  - Información de camiones
  - Estado y capacidad
  - Historial de mantenimiento

- **`Coordenada.java`**
  - Ubicaciones geográficas
  - Puntos de ruta
  - Cálculos de distancia

- **`EntregaParcial.java`**
  - Gestión de entregas parciales
  - Estado de entrega
  - Registro de incidencias

- **`EstadoCamion.java`**
  - Estados posibles de camiones
  - Transiciones de estado
  - Validaciones

- **`EstadoPedido.java`**
  - Estados de pedidos
  - Flujo de trabajo
  - Validaciones

- **`Mapa.java`**
  - Representación del mapa
  - Nodos y conexiones
  - Cálculos de ruta

- **`Mantenimiento.java`**
  - Registro de mantenimientos
  - Programación
  - Historial

- **`Nodo.java`**
  - Puntos de la red
  - Conexiones
  - Propiedades

- **`NodoRuta.java`**
  - Rutas entre nodos
  - Distancias
  - Restricciones

- **`Pedido.java`**
  - Gestión de pedidos
  - Estado y seguimiento
  - Asignaciones

- **`TipoAlmacen.java`**
  - Tipos de almacenes
  - Características
  - Restricciones

- **`TipoCamion.java`**
  - Tipos de camiones
  - Capacidades
  - Restricciones

- **`TipoIncidente.java`**
  - Clasificación de incidentes
  - Impacto
  - Procedimientos

- **`TipoNodo.java`**
  - Tipos de nodos
  - Propiedades
  - Restricciones

- **`TipoTurno.java`**
  - Gestión de turnos
  - Horarios
  - Asignaciones

##### 5. `factory/`
Patrones Factory:
- Creación de objetos
- Implementaciones de interfaces
- Patrones de diseño

##### 6. `utils/`
Utilidades:
- Clases de ayuda
- Funciones comunes
- Herramientas de desarrollo

##### 7. `PlgApplication.java`
- Punto de entrada de la aplicación
- Configuración principal
- Inicialización de Spring Boot

#### Recursos (`src/main/resources/`)
- **`application.properties`**
  - Configuraciones de la aplicación
  - Variables de entorno
  - Propiedades del sistema

#### Tests (`src/test/`)
- Tests unitarios
- Tests de integración
- Configuraciones de prueba

## Orden Recomendado para Entender el Código

### 1. Configuración Inicial
1. **`pom.xml`**
   - Dependencias y configuraciones
   - Versiones de las librerías
   - Plugins necesarios

2. **`PlgApplication.java`**
   - Punto de entrada
   - Configuración principal
   - Inicialización

3. **`application.properties`**
   - Configuraciones del sistema
   - Variables de entorno
   - Conexiones a servicios

### 2. Capa de Datos
1. **`entity/`**
   - Modelos de datos
   - Relaciones
   - Anotaciones JPA

2. **`dto/`**
   - Estructuras de transferencia
   - Mapeos
   - Validaciones

### 3. Lógica de Negocio
1. **`factory/`**
   - Patrones de creación
   - Implementaciones
   - Interfaces

2. **`utils/`**
   - Herramientas comunes
   - Funciones de ayuda
   - Utilidades

### 4. Capa de Presentación
1. **`controller/`**
   - Endpoints REST
   - Manejo de peticiones
   - Respuestas HTTP

## Tecnologías y Herramientas

- **Spring Boot** - Framework principal
- **Java** - Lenguaje de programación
- **Maven** - Gestión de dependencias
- **JPA/Hibernate** - Persistencia de datos
- **Spring Security** - Seguridad
- **JUnit** - Testing

## Comandos Importantes

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run

# Ejecutar tests
mvn test

# Generar documentación
mvn javadoc:javadoc
```

## Convenciones de Código

1. **Nombrado de Clases**
   - Controladores: `*Controller`
   - Servicios: `*Service`
   - Repositorios: `*Repository`
   - Entidades: `*Entity`
   - DTOs: `*DTO`

2. **Estructura de Paquetes**
   - Separación por capas
   - Nombres descriptivos
   - Organización lógica

3. **Documentación**
   - JavaDoc en todas las clases
   - Comentarios explicativos
   - README actualizado

## Buenas Prácticas

1. **Arquitectura**
   - Separación de capas
   - Inyección de dependencias
   - Principios SOLID

2. **Seguridad**
   - Validación de datos
   - Manejo de excepciones
   - Autenticación y autorización

3. **Rendimiento**
   - Caché cuando sea necesario
   - Consultas optimizadas
   - Manejo de recursos

## Debugging

1. **Herramientas**
   - Spring Boot DevTools
   - Postman/Insomnia
   - IDE Debugger

2. **Logging**
   - Log4j/SLF4J
   - Niveles de log apropiados
   - Trazas de error

## Recursos Adicionales

- [Documentación de Spring Boot](https://spring.io/projects/spring-boot)
- [Documentación de Maven](https://maven.apache.org/guides/)
- [Documentación de JPA](https://docs.oracle.com/javaee/7/api/javax/persistence/package-summary.html) 