# API REST para Bloqueos, Averías y Mantenimientos

## Resumen de Implementación

Se han creado los repositorios, servicios y controladores para gestionar **Bloqueos**, **Averías** y **Mantenimientos** siguiendo las mejores prácticas de programación.

## Estructura Creada

### DTOs de Request
- `BloqueoRequest.java` - Para crear nuevos bloqueos
- `AveriaRequest.java` - Para crear nuevas averías
- `MantenimientoRequest.java` - Para crear nuevos mantenimientos

### Repositorios
- `BloqueoRepository.java` - Gestión de datos de bloqueos
- `AveriaRepository.java` - Gestión de datos de averías
- `MantenimientoRepository.java` - Gestión de datos de mantenimientos

### Servicios
- `BloqueoService.java` - Lógica de negocio para bloqueos
- `AveriaService.java` - Lógica de negocio para averías
- `MantenimientoService.java` - Lógica de negocio para mantenimientos

### Controladores REST
- `BloqueoController.java` - API REST para bloqueos
- `AveriaController.java` - API REST para averías
- `MantenimientoController.java` - API REST para mantenimientos

## Endpoints Disponibles

### Bloqueos (`/api/bloqueos`)
- `GET /api/bloqueos` - Listar todos los bloqueos
- `POST /api/bloqueos` - Crear un nuevo bloqueo
- `GET /api/bloqueos/activos` - Listar bloqueos activos
- `GET /api/bloqueos/rango?inicio={inicio}&fin={fin}` - Listar bloqueos por rango de fechas
- `GET /api/bloqueos/resumen` - Resumen estadístico de bloqueos

### Averías (`/api/averias`)
- `GET /api/averias` - Listar todas las averías
- `POST /api/averias` - Crear una nueva avería
- `GET /api/averias/activas` - Listar averías activas
- `GET /api/averias/rango?inicio={inicio}&fin={fin}` - Listar averías por rango de fechas
- `GET /api/averias/resumen` - Resumen estadístico de averías

### Mantenimientos (`/api/mantenimientos`)
- `GET /api/mantenimientos` - Listar todos los mantenimientos
- `POST /api/mantenimientos` - Crear un nuevo mantenimiento
- `GET /api/mantenimientos/mes/{mes}` - Listar mantenimientos por mes
- `GET /api/mantenimientos/fecha?dia={dia}&mes={mes}` - Listar mantenimientos por fecha específica
- `GET /api/mantenimientos/resumen` - Resumen estadístico de mantenimientos

## Características Implementadas

### Validaciones
- Validación de datos de entrada en todos los endpoints
- Validación de formatos de fecha
- Validación de códigos de camión existentes
- Validación de rangos de valores (día, mes, etc.)

### Manejo de Errores
- Respuestas HTTP apropiadas (200, 201, 400, 500)
- Mensajes de error descriptivos
- Manejo de excepciones personalizadas

### Funcionalidades Avanzadas
- Filtrado por fechas para bloqueos y averías
- Filtrado por mes y día para mantenimientos
- Resúmenes estadísticos con agrupaciones
- Consulta de elementos activos
- Búsqueda por camión específico

### Buenas Prácticas Aplicadas
- Separación de responsabilidades (Repository-Service-Controller)
- Inyección de dependencias
- DTOs para transferencia de datos
- Documentación JavaDoc
- Manejo de excepciones centralizado
- Validaciones en capas de servicio
- Uso de builders para construcción de entidades

## Ejemplo de Uso

### Crear un nuevo bloqueo
```json
POST /api/bloqueos
{
  "fechaInicio": "2025-01-15T08:00:00",
  "fechaFin": "2025-01-15T18:00:00",
  "coordenadas": [
    {"x": 10, "y": 5},
    {"x": 11, "y": 5},
    {"x": 12, "y": 5}
  ]
}
```

### Crear una nueva avería
```json
POST /api/averias
{
  "codigoCamion": "TA01",
  "turno": "T1",
  "tipoIncidente": "TI2",
  "fechaInicio": "2025-01-15T10:30:00",
  "fechaFin": "2025-01-15T14:30:00"
}
```

### Crear un nuevo mantenimiento
```json
POST /api/mantenimientos
{
  "dia": 15,
  "mes": 1,
  "codigoCamion": "TA01"
}
```

## Notas Técnicas

- Todos los repositorios trabajan con datos en memoria (DataLoader)
- Los servicios implementan validaciones completas
- Los controladores manejan CORS para permitir acceso desde el frontend
- Se utilizan las entidades existentes del proyecto sin modificaciones
- Compatible con la arquitectura existente del proyecto
