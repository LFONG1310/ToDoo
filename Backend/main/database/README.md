# Base de datos de ToDoo (MySQL 8 + HeidiSQL)

El backend usa **MySQL 8.0.16 o superior** (se necesita para que se respeten las restricciones `CHECK`).
Todas las fechas se guardan en **UTC**; el frontend las muestra en la hora local de cada persona.

## Orden de los scripts

| Script | Qué hace | Cuándo |
| --- | --- | --- |
| `000_crear_base.sql` | Crea la base `todoo` y el usuario `todoo` | Una vez, conectado como `root` |
| `001_notificaciones.sql` | Tabla `notificaciones` | Una vez, sobre la base `todoo` |
| `002_usuarios_tableros.sql` | `usuarios`, `tableros`, `tablero_miembros` | Una vez |
| `003_comentarios_tablero.sql` | `comentarios_tablero` (conversación de cada tablero) | Una vez |
| `datos_prueba.sql` | Usuarios y tableros de ejemplo | Solo en desarrollo |

## Paso a paso en HeidiSQL

1. Abre HeidiSQL y crea una sesión **MySQL (TCP/IP)** a `127.0.0.1`, puerto `3306`, con tu usuario `root`.
2. Abre `000_crear_base.sql` (*Archivo → Cargar archivo SQL…*), cambia `CambiaEstaContrasena`
   por una contraseña tuya y ejecútalo con **F9**.
3. En el panel izquierdo selecciona la base **todoo** (si no aparece, pulsa F5).
4. Carga y ejecuta, en orden, `001`, `002` y `003`.
5. Para probar, ejecuta también `datos_prueba.sql`. Crea tres usuarios con la contraseña `ToDoo2026!`:
   - `luis@todoo.dev` (propietario de *Proyecto final*)
   - `diego@todoo.dev` (propietario de *Diseno*)
   - `andrea@todoo.dev` (solo está en *Proyecto final*)

## Conectar el backend

Crea `Backend/main/application-local.properties` (Git lo ignora) copiando
`application-local.properties.example` y pon la contraseña del paso 2:

```properties
DB_PASSWORD=tu_contrasena
```

También puedes usar las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.

## Agregar usuarios reales

Las contraseñas nunca se guardan en texto plano. Para generar el hash de una nueva contraseña,
desde `Backend/main`:

```bash
mvnw compile
java -cp target/classes com.todoo.seguridad.ContrasenaHasher "LaContrasena"
```

Copia el resultado (empieza con `pbkdf2_sha256$`) en la columna `contrasena_hash` del usuario.
Para que alguien participe en la conversación de un tablero, agrégalo a `tablero_miembros`.

## Modelo

```
usuarios 1──* tablero_miembros *──1 tableros
    │                                 │
    └──────* comentarios_tablero *────┘
```

- Solo los miembros de un tablero pueden leer y escribir su conversación.
- El autor puede editar o eliminar su comentario; el `PROPIETARIO` del tablero puede eliminar cualquiera.
- Al borrar un tablero se borran sus miembros y comentarios (`ON DELETE CASCADE`).
- Los usuarios no se borran: se desactivan con `activo = FALSE` y su historial se conserva.
