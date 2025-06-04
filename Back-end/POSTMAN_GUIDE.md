# Guía de Endpoints para Postman

Este archivo resume las URL principales para probar el backend con Postman. Se
asume que la aplicación corre en `http://localhost:8085`. Todos los endpoints
permiten **CORS** para facilitar las pruebas.

### Pedidos
- **GET `http://localhost:8085/api/pedidos`**
  Lista todos los pedidos cargados en memoria.
- **GET `http://localhost:8085/api/pedidos/resumen`**
  Devuelve un resumen con el total y el número de pedidos por estado.
- **GET `http://localhost:8085/api/pedidos/rango?inicio=YYYY-MM-DDTHH:MM&fin=YYYY-MM-DDTHH:MM`**
  Lista los pedidos cuyo `fechaRegistro` se encuentra en el rango indicado.
  Ejemplo:
  `http://localhost:8085/api/pedidos/rango?inicio=2025-02-01T00:00&fin=2025-02-01T04:00`
  devuelve los pedidos registrados entre la medianoche y las 4&nbsp;a.m. del
  1&nbsp;de febrero de&nbsp;2025.
- **POST `http://localhost:8085/api/pedidos`**
  Crea un nuevo pedido. Ejemplo de cuerpo JSON:
  ```json
  {
    "x": 10,
    "y": 5,
    "volumenGLP": 50,
    "horasLimite": 8
  }
  ```

### Camiones
- **GET `http://localhost:8085/api/camiones`**
  Lista los camiones existentes.
- **GET `http://localhost:8085/api/camiones/resumen`**
  Muestra un resumen por tipo de camión.
- **POST `http://localhost:8085/api/camiones`**
  Crea un camión. Cuerpo de ejemplo:
  ```json
  {
    "tipo": "TB",
    "operativo": true,
    "x": 12,
    "y": 8
  }
  ```

### Almacenes
- **GET `http://localhost:8085/api/almacenes`**
  Devuelve la lista de almacenes.
- **GET `http://localhost:8085/api/almacenes/resumen`**
  Resumen por tipo de almacén.
- **POST `http://localhost:8085/api/almacenes`**
  Crea un almacén. Ejemplo:
  ```json
  {
    "tipo": "SECUNDARIO",
    "x": 20,
    "y": 20,
    "capacidadMaxGLP": 200,
    "capacidadMaxCombustible": 100
  }
  ```
