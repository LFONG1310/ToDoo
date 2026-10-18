# ToDoo

Proyecto universitario.

Gestor de tareas por proyectos. Los miembros de cada proyecto pueden conversar en su tablero sin salir del sistema.

- **Frontend:** React 19 + Vite + Tailwind (`Frontend/`)
- **Backend:** Spring Boot 4 + JDBC (`Backend/main/`)
- **Base de datos:** MySQL 8 (administrada con HeidiSQL)

## Puesta en marcha

1. **Base de datos.** Sigue [`Backend/main/database/README.md`](Backend/main/database/README.md):
   ejecuta `propuesta_mysql.sql` en HeidiSQL, crea el usuario `todoo` y, para probar, `datos_prueba.sql`.
2. **Backend.** Copia `Backend/main/application-local.properties.example` como
   `application-local.properties`, pon tu `DB_PASSWORD` y ejecuta desde `Backend/main`:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   Queda escuchando en `http://127.0.0.1:8080`.
3. **Frontend.** Desde `Frontend/`:
   ```bash
   pnpm install
   pnpm dev
   ```
   Abre `http://localhost:5173`. Vite redirige `/api` al backend.
4. Entra en `/login` con `luis@todoo.dev` / `ToDoo2026!` (datos de prueba) y abre **Tablero**.
   Debajo de las columnas está la **conversación del equipo**. Para ver a dos personas
   conversando, entra con `diego@todoo.dev` en otra ventana privada.

## Pruebas

```powershell
cd Backend/main; .\mvnw.cmd test   # backend: integración con H2 usando el esquema de la propuesta
cd Frontend; pnpm test              # servicios y utilidades del frontend
cd Frontend; pnpm lint
```

## API

Todas las rutas, salvo `POST /api/auth/login`, requieren sesión. Las que modifican datos
requieren además el encabezado `X-CSRF-TOKEN`, que se obtiene con `GET /api/csrf`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/auth/login` | Inicia sesión (`{correo, contrasena}`) |
| GET | `/api/auth/sesion` | Usuario de la sesión actual |
| POST | `/api/auth/logout` | Cierra la sesión |
| GET | `/api/csrf` | Token CSRF de la sesión |
| GET | `/api/proyectos` | Proyectos donde el usuario es miembro activo |
| GET | `/api/proyectos/{id}` | Detalle del proyecto, mi rol y sus miembros |
| GET | `/api/proyectos/{id}/tareas` | Tareas con filtros (`prioridad`, `responsableId`, `soloMias`) |
| GET | `/api/proyectos/{id}/responsables` | Miembros activos para el selector de responsable |
| GET | `/api/proyectos/{id}/comentarios` | Conversación del tablero: últimos 200, del más antiguo al más reciente |
| POST | `/api/proyectos/{id}/comentarios` | Publica un comentario (`{contenido}`, máx. 1000 caracteres) |
| PATCH | `/api/proyectos/{id}/comentarios/{cid}` | Edita un comentario propio |
| DELETE | `/api/proyectos/{id}/comentarios/{cid}` | Elimina (autor o administrador del proyecto) |
| GET | `/api/notificaciones` | Notificaciones del usuario |
| PATCH | `/api/notificaciones/{id}/leida` | Marca una notificación como leída |

Un proyecto del que no eres miembro activo responde **404**, para no revelar que existe.
