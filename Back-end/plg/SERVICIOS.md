# Servicio: Actualizar datos principales de un camión

**Endpoint:** `POST /api/camiones/actualizar-datos`

Actualiza la coordenada, combustible actual, capacidad actual de GLP y estado de un camión, identificándolo por su código.

## Ejemplo de uso en Postman

- **URL:** `http://localhost:8080/api/camiones/actualizar-datos`
- **Método:** POST
- **Headers:**
    - Content-Type: application/json
- **Body (raw, JSON):**
```json
{
  "codigo": "CAMION001",
  "coordenada": {
    "fila": 5,
    "columna": 8
  },
  "combustibleActual": 120.5,
  "capacidadActualGLP": 30.0,
  "estado": "OPERATIVO"
}
```

- **Respuesta exitosa:**
```json
{
  "codigo": "CAMION001",
  "coordenada": {
    "fila": 5,
    "columna": 8
  },
  "combustibleActual": 120.5,
  "capacidadActualGLP": 30.0,
  "estado": "OPERATIVO"
  // ...otros campos del camión
}
```

> Cambia los valores según el camión y estado que desees actualizar.

---

# Servicio: Agregar nueva avería

**Endpoint:** `POST /api/averias`

Permite registrar una nueva avería indicando solo el código del camión y el tipo de incidente.

## Ejemplo de uso en Postman

- **URL:** `http://localhost:8080/api/averias`
- **Método:** POST
- **Headers:**
    - Content-Type: application/json
- **Body (raw, JSON):**
```json
{
  "codigoCamion": "CAMION001",
  "tipoIncidente": "TI1",
  "fechaHoraOcurrencia": "2025-06-23T10:00:00",
}
```

- **Respuesta exitosa:**
```json
{
  "id": 1,
  "codigoCamion": "CAMION001",
  "tipoIncidente": "TI1",
  "fechaHoraOcurrencia": "2025-06-23T10:00:00",
  "fechaHoraDisponible": "2025-06-23T14:00:00",
  "tiempoReparacionEstimado": 4,
  // ...otros campos calculados de la avería
}
```

> Cambia los valores según el camión y tipo de incidente que desees registrar.

---

# Servicio: Actualizar estado de un pedido

**Endpoint:** `POST /api/pedidos/actualizar-estado`

Permite actualizar únicamente el estado de un pedido, identificándolo por su código.

## Ejemplo de uso en Postman

- **URL:** `http://localhost:8080/api/pedidos/actualizar-estado`
- **Método:** POST
- **Headers:**
    - Content-Type: application/json
- **Body (raw, JSON):**
```json
{
  "codigo": "PEDIDO001",
  "estado": "ENTREGADO"
}
```

- **Respuesta exitosa:**
```json
{
  "codigo": "PEDIDO001",
  "estado": "ENTREGADO"
  // ...otros campos del pedido
}
```

> Cambia los valores según el pedido y estado que desees actualizar.
