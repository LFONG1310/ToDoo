# ToDoo

Gestor de tareas por tableros. Los responsables de cada tablero pueden conversar en él sin salir del sistema.

- **Frontend:** React 19 + Vite + Tailwind (`Frontend/`)
- **Backend:** Spring Boot 4 + JDBC (`Backend/main/`)
- **Base de datos:** MySQL 8 (administrada con HeidiSQL)

## Puesta en marcha

1. **Base de datos.** Sigue [`Backend/main/database/README.md`](Backend/main/database/README.md):
   crea la base con `000_crear_base.sql`, ejecuta `001`, `002` y `003` y, para probar, `datos_prueba.sql`.
2. **Backend.** Copia `Backend/main/application-local.properties.example` como
   `application-local.properties`, pon tu `DB_PASSWORD` y ejecuta desde `Backend/main`:
   ```bash
   mvnw spring-boot:run      # en Windows: mvnw.cmd spring-boot:run
   ```
   Queda escuchando en `http://127.0.0.1:8080`.
3. **Frontend.** Desde `Frontend/`:
   ```bash
   pnpm install
   pnpm dev
   ```
   Abre `http://localhost:5173`. Vite redirige `/api` al backend.
4. Entra con `luis@todoo.dev` / `ToDoo2026!` (datos de prueba) y abre **Proyecto final**.
   Para ver la conversación entre dos personas, entra con `diego@todoo.dev` en otra ventana privada.

## Pruebas

```bash
cd Backend/main && mvnw test     # backend: integración con H2 usando los mismos scripts SQL
cd Frontend && pnpm test         # servicios del frontend
cd Frontend && pnpm lint
```

## API

Todas las rutas (salvo `POST /api/auth/login`) requieren sesión. Las que modifican datos
requieren además el encabezado `X-CSRF-TOKEN`, que se obtiene con `GET /api/csrf`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/auth/login` | Inicia sesión (`{correo, contrasena}`) |
| GET | `/api/auth/sesion` | Usuario de la sesión actual |
| POST | `/api/auth/logout` | Cierra la sesión |
| GET | `/api/csrf` | Token CSRF de la sesión |
| GET | `/api/tableros` | Tableros donde participa el usuario |
| GET | `/api/tableros/{id}` | Detalle del tablero y sus responsables |
| GET | `/api/tableros/{id}/comentarios` | Últimos 200 comentarios, del más antiguo al más reciente |
| POST | `/api/tableros/{id}/comentarios` | Publica un comentario (`{texto}`, máx. 1000 caracteres) |
| PATCH | `/api/tableros/{id}/comentarios/{cid}` | Edita un comentario propio |
| DELETE | `/api/tableros/{id}/comentarios/{cid}` | Elimina (autor o propietario del tablero) |
| GET | `/api/notificaciones` | Notificaciones del usuario |
| PATCH | `/api/notificaciones/{id}/leida` | Marca una notificación como leída |

Un tablero del que no eres miembro responde **404**, para no revelar que existe.
