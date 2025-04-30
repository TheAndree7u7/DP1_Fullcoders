# Documentación de Archivos de Datos

Este directorio contiene los archivos de entrada utilizados por la aplicación PLG para realizar la optimización de rutas, simulación y análisis predictivo.

## Estructura de Directorios

### Directorio: `almacenes/`
Contiene la información sobre los almacenes y centros de distribución de GLP.

#### Archivo: `almacenes.txt`
**Formato:** CSV delimitado por tabulaciones
**Estructura:**
```
ID_ALMACEN    NOMBRE           LATITUD     LONGITUD    CAPACIDAD    STOCK_ACTUAL    TIPO
ALM001        CD Lima Centro   -12.0464    -77.0428    50000        32500          PRINCIPAL
ALM002        CD Callao        -12.0219    -77.1288    35000        20120          SECUNDARIO
```

**Campos:**
- `ID_ALMACEN`: Identificador único del almacén
- `NOMBRE`: Nombre descriptivo del almacén
- `LATITUD`: Coordenada geográfica (latitud)
- `LONGITUD`: Coordenada geográfica (longitud)
- `CAPACIDAD`: Capacidad máxima de almacenamiento (en Kg)
- `STOCK_ACTUAL`: Stock disponible actualmente (en Kg)
- `TIPO`: Categoría del almacén (PRINCIPAL, SECUNDARIO, TEMPORAL)

### Directorio: `averias/`
Contiene el historial de averías de los vehículos.

#### Archivo: `averias.v1.txt`
**Formato:** CSV delimitado por comas
**Estructura:**
```
FECHA,ID_CAMION,DESCRIPCION,TIPO_AVERIA,TIEMPO_REPARACION,COSTO
2025-03-15,CAM003,Falla en sistema de frenos,MECANICA,4.5,850.00
2025-03-18,CAM007,Problema eléctrico en tablero,ELECTRICA,2.0,320.50
```

**Campos:**
- `FECHA`: Fecha en que ocurrió la avería (formato YYYY-MM-DD)
- `ID_CAMION`: Identificador del camión afectado
- `DESCRIPCION`: Descripción textual del problema
- `TIPO_AVERIA`: Clasificación (MECANICA, ELECTRICA, HIDRAULICA, NEUMATICOS, OTRAS)
- `TIEMPO_REPARACION`: Tiempo que tomó reparar en horas
- `COSTO`: Costo de la reparación en soles

### Directorio: `bloqueos/`
Contiene información sobre bloqueos de carreteras y rutas.

#### Archivo: `202504.bloqueadas`
**Formato:** Texto estructurado
**Estructura:**
```
INICIO: 2025-04-02 08:00
FIN: 2025-04-02 18:00
TRAMO: -12.0464,-77.0428|-12.0530,-77.0512
MOTIVO: Obras de mantenimiento vial

INICIO: 2025-04-05 00:00
FIN: 2025-04-07 23:59
TRAMO: -12.1012,-77.0134|-12.1089,-77.0214|-12.1134,-77.0310
MOTIVO: Desastre natural - Derrumbe en carretera
```

**Campos:**
- `INICIO`: Fecha y hora de inicio del bloqueo
- `FIN`: Fecha y hora estimada de finalización
- `TRAMO`: Segmento bloqueado definido por coordenadas (puntos separados por |)
- `MOTIVO`: Descripción de la causa del bloqueo

### Directorio: `camiones/`
Contiene la información de la flota de vehículos disponibles.

#### Archivo: `camiones.txt`
**Formato:** CSV delimitado por punto y coma
**Estructura:**
```
ID;PLACA;MARCA;MODELO;AÑO;CAPACIDAD;RENDIMIENTO;ESTADO;ALMACEN_BASE;KILOMETRAJE
CAM001;ABC-123;HINO;GH 1828;2023;8000;9.5;ACTIVO;ALM001;15420
CAM002;DEF-456;VOLVO;FM 11;2022;10000;8.3;ACTIVO;ALM002;23450
```

**Campos:**
- `ID`: Identificador único del camión
- `PLACA`: Número de placa del vehículo
- `MARCA`: Marca del camión
- `MODELO`: Modelo específico
- `AÑO`: Año de fabricación
- `CAPACIDAD`: Capacidad de carga en Kg
- `RENDIMIENTO`: Rendimiento de combustible (Km/galón)
- `ESTADO`: Estado operativo (ACTIVO, MANTENIMIENTO, INACTIVO, AVERIADO)
- `ALMACEN_BASE`: ID del almacén donde está asignado
- `KILOMETRAJE`: Kilometraje actual del vehículo

### Directorio: `mantenimientos/`
Contiene la programación de mantenimientos preventivos.

#### Archivo: `mantpreventivo.txt`
**Formato:** Texto estructurado con delimitadores
**Estructura:**
```
ID_MANT|CAM005|2025-04-15|2025-04-15|PREVENTIVO_BASICO|4|350.00|ALM001
ID_MANT|CAM002|2025-04-18|2025-04-19|PREVENTIVO_COMPLETO|12|1200.00|ALM002
```

**Campos:**
- `ID_MANT`: Identificador del mantenimiento
- `ID_CAMION`: Identificador del camión
- `FECHA_INICIO`: Fecha programada de inicio
- `FECHA_FIN`: Fecha programada de finalización
- `TIPO`: Tipo de mantenimiento
- `DURACION`: Duración estimada en horas
- `COSTO`: Costo estimado del mantenimiento
- `UBICACION`: Almacén donde se realizará

### Directorio: `pedidos/`
Contiene los archivos de pedidos mensuales de clientes.

#### Archivo: `ventas202504.txt`
**Formato:** CSV con encabezados
**Estructura:**
```
ID_PEDIDO,FECHA,CLIENTE,DIRECCION,LATITUD,LONGITUD,CANTIDAD,PRIORIDAD,VENTANA_HORARIA_INICIO,VENTANA_HORARIA_FIN
PED20250401001,2025-04-01,Restaurante El Fogón,"Av. Arequipa 1520, Lince",-12.0830,-77.0315,250,ALTA,08:00,12:00
PED20250401002,2025-04-01,Hospital Central,"Jr. Huancavelica 890, Cercado",-12.0472,-77.0369,500,URGENTE,09:00,11:00
```

**Campos:**
- `ID_PEDIDO`: Identificador único del pedido
- `FECHA`: Fecha de entrega programada
- `CLIENTE`: Nombre del cliente
- `DIRECCION`: Dirección física del punto de entrega
- `LATITUD`: Coordenada geográfica del punto de entrega
- `LONGITUD`: Coordenada geográfica del punto de entrega
- `CANTIDAD`: Cantidad de GLP solicitada en Kg
- `PRIORIDAD`: Nivel de prioridad (BAJA, MEDIA, ALTA, URGENTE)
- `VENTANA_HORARIA_INICIO`: Hora más temprana para entrega
- `VENTANA_HORARIA_FIN`: Hora límite para la entrega

## Notas Importantes

1. **Formatos de Fecha y Hora:**
   - Fechas: YYYY-MM-DD (ISO 8601)
   - Horas: HH:MM formato 24 horas
   - Fechas y horas combinadas: YYYY-MM-DD HH:MM

2. **Coordenadas Geográficas:**
   - Se utiliza el sistema WGS84 (estándar para GPS)
   - Formato decimal con signo negativo para latitudes sur y longitudes oeste
   - Precisión de 4 decimales

3. **Procesamiento de Archivos:**
   - Los archivos son leídos al inicio de la aplicación
   - Cualquier cambio requiere reiniciar la aplicación o activar la recarga manual
   - Se conservan copias de respaldo con fecha en los nombres antes de modificaciones