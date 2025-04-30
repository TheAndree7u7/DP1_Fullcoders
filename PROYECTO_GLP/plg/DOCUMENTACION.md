# Documentación del Proyecto PLG

## Descripción General
Este proyecto implementa una solución para la gestión logística de gas licuado de petróleo (GLP) con capacidades de optimización de rutas, simulación en tiempo real y análisis predictivo.

## Estructura del Proyecto

### Archivos de Configuración
- **application.properties**: Configuración principal de Spring Boot
- **application-test-algorithm.properties**: Configuración específica para pruebas de algoritmos
- **logback-spring.xml**: Configuración del sistema de logs

### Directorios Principales
- **src/main/java/com/plg/**: Código fuente Java de la aplicación
- **src/main/resources/**: Recursos y archivos de configuración
- **src/main/resources/data/**: Datos de entrada para la aplicación
- **src/main/resources/static/**: Archivos estáticos para la interfaz web
- **src/test/**: Pruebas unitarias e integración 

### Datos de Entrada
- **almacenes/**: Definición de almacenes y centros de distribución
- **averias/**: Registros históricos de averías de vehículos
- **bloqueos/**: Información sobre bloqueos de carreteras y rutas
- **camiones/**: Información sobre la flota de camiones disponibles
- **mantenimientos/**: Programación de mantenimientos preventivos
- **pedidos/**: Archivos de pedidos mensuales de clientes

## Módulos Principales

### 1. Módulo de Optimización de Rutas
Implementa algoritmos para la optimización de rutas de distribución de GLP considerando múltiples variables:
- Demanda de clientes
- Disponibilidad de flota
- Bloqueos de carreteras
- Mantenimientos programados
- Consumo de combustible
- Tiempos de entrega

### 2. Módulo de Simulación en Tiempo Real
Permite simular el funcionamiento del sistema de distribución en tiempo real:
- Visualización de movimiento de vehículos
- Actualización de estados de entregas
- Simulación de incidentes y averías
- Respuesta a eventos inesperados

### 3. Módulo de Análisis Predictivo
Utiliza datos históricos para realizar predicciones sobre:
- Demanda futura
- Averías potenciales
- Tiempos de entrega esperados
- Optimización de inventarios

### 4. Interfaz Web
Proporciona una visualización gráfica interactiva:
- Mapa con rutas optimizadas
- Dashboard de indicadores clave
- Control de simulación en tiempo real
- Configuración de parámetros del sistema

## Scripts de Ejecución
- **run-algorithm-test.bat/sh**: Script para ejecutar pruebas de algoritmos
- **test-algorithm.sh**: Script para evaluación de algoritmos específicos

## Tecnologías Utilizadas
- **Spring Boot**: Framework principal de desarrollo
- **H2 Database**: Base de datos en memoria para desarrollo
- **WebSockets**: Comunicación en tiempo real con el frontend
- **Jackson**: Procesamiento de JSON
- **Hibernate/JPA**: Persistencia de datos
- **Maven**: Gestión de dependencias y construcción

## Archivos de Registro (Logs)
Los archivos de log se almacenan en el directorio `logs/` y están organizados por componente:
- **affinity-propagation.log**: Logs del algoritmo de agrupamiento
- **genetico.log**: Logs del algoritmo genético para optimización
- **mapa-reticular.log**: Logs del sistema de mapeo de rutas
- **simulacion-tiempo-real.log**: Logs de la simulación en tiempo real

## Control de Versiones
La estrategia de versionamiento se describe en `VERSIONAMIENTO.md`, siguiendo el modelo Semantic Versioning (SemVer).