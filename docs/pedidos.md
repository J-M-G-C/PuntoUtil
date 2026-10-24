# Modulo de Pedidos - PuntoUtil

Modulo de **pedidos en linea para recojo en tienda**. Permite registrar un pedido, generar un codigo de seguimiento, gestionar sus estados, descargar un PDF de confirmacion y descontar stock **una sola vez** al confirmar la entrega.

> El PDF generado es un **documento de confirmacion de pedido**. No es boleta, factura ni comprobante SUNAT.

## 1. Alcance implementado

| Requerimiento | Implementacion |
| --- | --- |
| Registrar pedidos en linea para recojo en tienda | `POST /api/pedidos` con datos del cliente y detalle de productos |
| Generar codigo de seguimiento | Codigo unico `PU-AAAAMMDD-XXXX` generado con `SecureRandom` |
| Gestionar estados | `PENDIENTE`, `LISTO`, `ENTREGADO`, `CANCELADO` con transiciones controladas |
| Generar PDF de confirmacion | `GET /api/pedidos/{id}/pdf` y `GET /api/pedidos/seguimiento/{codigo}/pdf` (OpenPDF) |
| Validar stock y descontarlo una sola vez al confirmar la entrega | Se valida al registrar y se vuelve a validar con bloqueo pesimista al entregar; el descuento ocurre solo en la transicion a `ENTREGADO` |
| Probar con Postman | Coleccion `docs/postman-pedidos.json` con 26 requests y pruebas automaticas |

## 2. Archivos principales

### Base de datos
- `src/main/resources/db/migration/V4__crear_pedidos.sql`

Crea las tablas `pedidos` y `pedido_detalles`, con claves foraneas a `usuarios` y `productos`, restricciones de estado y de cantidades positivas.

### Entidades y repositorios
- `entity/EstadoPedido.java`
- `entity/Pedido.java`
- `entity/PedidoDetalle.java`
- `repository/PedidoRepository.java`
- `repository/PedidoDetalleRepository.java`

### DTOs
- `dto/pedido/PedidoRequest.java`
- `dto/pedido/PedidoDetalleRequest.java`
- `dto/pedido/ActualizarEstadoPedidoRequest.java`
- `dto/pedido/PedidoResponse.java`
- `dto/pedido/PedidoDetalleResponse.java`

### Logica y API
- `service/PedidoService.java`
- `controller/PedidoController.java`
- `pom.xml` (dependencia `com.github.librepdf:openpdf`)

### Pruebas
- `src/test/java/pe/edu/cibertec/puntoutil/PedidoIntegrationTest.java`
- `docs/postman-pedidos.json`

## 3. API

Base: `/api/pedidos`

| Metodo | Ruta | Roles | Descripcion |
| --- | --- | --- | --- |
| `POST` | `/api/pedidos` | `ADMINISTRADOR`, `CLIENTE` | Registra un pedido y devuelve el codigo de seguimiento |
| `GET` | `/api/pedidos` | `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE` | Lista pedidos; acepta `?estado=PENDIENTE` |
| `GET` | `/api/pedidos/{id}` | `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE` | Consulta un pedido por ID |
| `GET` | `/api/pedidos/seguimiento/{codigo}` | `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE` | Consulta un pedido por codigo de seguimiento |
| `PATCH` | `/api/pedidos/{id}/estado` | `ADMINISTRADOR`, `VENDEDOR` | Cambia el estado indicando `{ "estado": "LISTO" }` |
| `PATCH` | `/api/pedidos/{id}/entregar` | `ADMINISTRADOR`, `VENDEDOR` | Confirma la entrega y descuenta stock |
| `PATCH` | `/api/pedidos/{id}/cancelar` | `ADMINISTRADOR`, `VENDEDOR` | Cancela el pedido sin mover stock |
| `GET` | `/api/pedidos/{id}/pdf` | `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE` | Descarga el PDF de confirmacion |
| `GET` | `/api/pedidos/seguimiento/{codigo}/pdf` | `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE` | Descarga el PDF por codigo |

> Cuando el usuario autenticado tiene rol `CLIENTE`, solo puede consultar sus propios pedidos. Un pedido de otro cliente responde `403`.

### Ejemplo de registro

```http
POST /api/pedidos
Authorization: Bearer <token de cliente>
Content-Type: application/json
```

```json
{
  "nombreCliente": "Ana Torres",
  "dni": "76543210",
  "telefono": "987654321",
  "correo": "ana@example.com",
  "observaciones": "Recojo por la tarde",
  "detalles": [
    { "productoId": 1, "cantidad": 5 }
  ]
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "codigoSeguimiento": "PU-20261023-MBHR",
  "nombreCliente": "Ana Torres",
  "dni": "76543210",
  "telefono": "987654321",
  "correo": "ana@example.com",
  "clienteUsuario": "cliente.pedidos",
  "estado": "PENDIENTE",
  "total": 27.50,
  "fechaCreacion": "2026-10-23T21:20:00",
  "fechaActualizacion": null,
  "fechaEntrega": null,
  "observaciones": "Recojo por la tarde",
  "detalles": [
    {
      "id": 1,
      "productoId": 1,
      "codigoProducto": "PED-123456",
      "nombreProducto": "Cuaderno de pruebas",
      "cantidad": 5,
      "precioUnitario": 5.50,
      "subtotal": 27.50
    }
  ]
}
```

## 4. Estados y transiciones

```text
PENDIENTE ──> LISTO ──> ENTREGADO
    │           │
    └───────────┴──> CANCELADO
```

| Estado actual | Estados permitidos |
| --- | --- |
| `PENDIENTE` | `LISTO`, `CANCELADO` |
| `LISTO` | `ENTREGADO`, `CANCELADO` |
| `ENTREGADO` | Ninguno (estado final) |
| `CANCELADO` | Ninguno (estado final) |

Reglas adicionales:
- No se permite repetir el mismo estado (`409 Conflict`).
- No se permite entregar un pedido cancelado o ya entregado (`409 Conflict`).
- Al cancelar, el stock **no** se modifica porque el descuento ocurre recien al entregar.

## 5. Stock: validacion y descuento unico

1. **Al registrar**: se valida que cada producto exista, este activo y tenga stock suficiente. El stock **no** se descuenta.
2. **Al confirmar la entrega**: se bloquea la fila del pedido con `PESSIMISTIC_WRITE`; si otra confirmacion simultanea ya lo entrego, la segunda recibe `409`.
3. Se bloquean los productos con `findByIdParaMovimiento` (`PESSIMISTIC_WRITE`) en orden de ID para evitar deadlocks.
4. Se valida todo el stock antes de modificar existencias.
5. Se descuenta el stock y se registra un movimiento `SALIDA` en `movimientos_inventario` por cada producto.
6. La operacion es transaccional: si falla un producto, no se descuenta ninguno.

Esto garantiza que el stock se descuente **exactamente una vez** por pedido.

## 6. PDF de confirmacion

El PDF incluye:
- Titulo "PUNTO UTIL - CONFIRMACION DE PEDIDO".
- Codigo de seguimiento, fecha de registro, estado y fecha de entrega (si existe).
- Datos del cliente.
- Tabla de productos con cantidad, precio unitario y subtotal.
- Total.
- Observaciones.
- Leyenda: **"Documento de confirmacion de pedido. No constituye boleta ni factura SUNAT."**

Respuesta: `200 OK`, `Content-Type: application/pdf` y `Content-Disposition: attachment`.

## 7. Configuracion y ejecucion

### Base de datos MySQL

Aplicar las migraciones Flyway (incluida `V4__crear_pedidos.sql`) al iniciar la aplicacion:

```powershell
$env:DB_PASSWORD="tu_password"
$env:JWT_SECRET="una_clave_larga_y_segura_de_al_menos_32_caracteres"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="ClaveSegura2026"
$env:ADMIN_EMAIL="admin@puntoutil.test"
.\mvnw.cmd spring-boot:run
```

### Pruebas automatizadas

```powershell
.\mvnw.cmd test
```

Resultado esperado: **18 pruebas, 0 fallos**. Incluye 7 pruebas de integracion del modulo de pedidos.

## 8. Pruebas con Postman

1. Importar `docs/postman-pedidos.json` en Postman.
2. Verificar la variable `baseUrl` (por defecto `http://localhost:8080`).
3. Ajustar `adminUsuario` y `adminContrasena` a un administrador existente.
4. Ejecutar la coleccion completa con **Run collection**.

Orden de la coleccion:

1. **Preparacion**: login admin, creacion de vendedor/cliente, login de ambos y creacion de un producto con stock 20.
2. **Flujo principal**: registrar pedido, comprobar que el stock no cambio, consultar por ID y por codigo, listar y filtrar, marcar `LISTO`, descargar PDF, confirmar entrega y verificar el descuento de stock.
3. **Reglas de negocio y errores**: segunda entrega (`409`), cancelacion sin mover stock, entrega de pedido cancelado (`409`), stock insuficiente (`409`), detalle vacio (`400`), cliente intentando cambiar estado (`403`) y consulta sin token (`401`).

### Evidencia de la ultima ejecucion

Se ejecuto la API real en `http://localhost:8080` y se validaron las 27 comprobaciones equivalentes a la coleccion:

```text
OK: registrar pedido (201)
OK: estado inicial PENDIENTE
OK: total calculado S/ 27.50
OK: codigo de seguimiento generado (PU-20261023-MBHR)
OK: stock intacto al registrar (20)
OK: descargar PDF (200)
OK: PDF valido (%PDF)
OK: cambiar a LISTO (200)
OK: confirmar entrega (200)
OK: stock descontado una vez (20 -> 15)
OK: segunda entrega rechazada (409)
OK: stock no se descuenta dos veces (15)
OK: cancelacion no altera stock (15)
OK: pedido sin stock rechazado (409)
OK: sin token rechazado (401)
OK: cliente no puede cambiar estado (403)
```

## 9. Codigos de respuesta

| Codigo | Caso |
| --- | --- |
| `201` | Pedido registrado |
| `200` | Consulta, cambio de estado, cancelacion, entrega o PDF correctos |
| `400` | Datos invalidos (sin nombre, sin detalles, cantidad menor a 1, correo invalido) |
| `401` | Sin token o token invalido |
| `403` | Rol sin permiso o cliente consultando un pedido ajeno |
| `404` | Pedido o producto no encontrado |
| `409` | Stock insuficiente, transicion de estado invalida o entrega repetida |

## 10. Rama de trabajo

```text
feature/pedidos
```
