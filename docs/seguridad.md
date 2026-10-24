# Seguridad y usuarios

La autenticación devuelve un JWT. En las rutas protegidas, envía el token con
`Authorization: Bearer <token>`. Las contraseñas se almacenan con BCrypt; las
respuestas de usuarios nunca incluyen el hash. En el perfil normal, `JWT_SECRET`
es obligatorio y debe contener al menos 32 bytes aleatorios. La clave del perfil
demo y la de pruebas son solo para esos entornos.

## Inicializar el primer administrador

El primer administrador se crea al iniciar el backend cuando están configuradas
las variables `ADMIN_USERNAME`, `ADMIN_PASSWORD` (mínimo 12 caracteres) y
`ADMIN_EMAIL`. En macOS o Linux:

```bash
export DB_PASSWORD='contraseña-de-mysql'
export JWT_SECRET="$(openssl rand -base64 48)"
export ADMIN_USERNAME='admin'
export ADMIN_PASSWORD='cambia-esta-clave-segura'
export ADMIN_EMAIL='admin@puntoutil.local'
./mvnw spring-boot:run
```

Las migraciones Flyway crean la tabla `usuarios`. Si el usuario o el correo ya
existen, el proceso de inicio no los reemplaza.

## Probar el login con Postman

Inicia el perfil demo e importa la colección `docs/postman-seguridad.json` en
Postman. Ejecuta la colección completa para comprobar el login, `/api/auth/me`,
el rechazo sin token y el acceso autorizado del administrador a `/api/usuarios`.
La colección usa `admin` / `DemoAdmin2026!` del perfil demo.

Envía `POST http://localhost:8080/api/auth/login` con `Content-Type:
application/json`:

```json
{
  "usuario": "admin",
  "contrasena": "cambia-esta-clave-segura"
}
```

La respuesta incluye `token`, `tipo: "Bearer"`, usuario y rol. Copia el token
como Bearer Token en Postman. `GET /api/auth/me` devuelve el usuario autenticado.

Para crear un usuario, llama `POST /api/usuarios` con un token de administrador:

```json
{
  "usuario": "vendedor1",
  "nombre": "Vendedor de prueba",
  "email": "vendedor1@puntoutil.local",
  "contrasena": "ClaveSegura2026",
  "rol": "VENDEDOR"
}
```

El administrador también puede listar y consultar usuarios con `GET /api/usuarios`
y `GET /api/usuarios/{id}`, actualizarlos con `PUT /api/usuarios/{id}` y
desactivarlos con `DELETE /api/usuarios/{id}`. La baja es lógica.

## Permisos

| Ruta/operación | Acceso |
| --- | --- |
| `POST /api/auth/login`, `GET /api/estado` | Público |
| `GET /api/productos/**`, `GET /api/categorias/**` | Público |
| Cambios en productos, categorías e inventario | `ADMINISTRADOR` |
| `GET /api/inventario/movimientos/producto/**` | `ADMINISTRADOR`, `VENDEDOR` |
| `GET` y `POST /api/ventas/**` | `ADMINISTRADOR`, `VENDEDOR` |
| Actualizar o anular ventas | Solo `ADMINISTRADOR` |
| `GET /api/pedidos/**` | `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE` |
| `POST /api/pedidos/**` | `ADMINISTRADOR`, `CLIENTE` |
| Cambios y anulaciones de pedidos | `ADMINISTRADOR`, `VENDEDOR` para actualizar; solo `ADMINISTRADOR` para eliminar |
| `/api/usuarios/**` | `ADMINISTRADOR` |

## Modo de demostración

Con `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo` se usa H2 en memoria,
con dos productos de muestra y el usuario `admin` / `DemoAdmin2026!`. Los datos
se eliminan al detener la aplicación; este perfil no debe usarse en producción.
