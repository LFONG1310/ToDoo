# Base de datos de ToDoo (MySQL 8 + HeidiSQL)

El esquema es la **propuesta del equipo**: [`propuesta_mysql.sql`](propuesta_mysql.sql)
(usuarios, proyectos, miembros_proyecto, tareas, comentarios y notificaciones).
Todas las fechas se guardan en **UTC**; el frontend las muestra en la hora local de cada persona.

| Script | Qué hace | Cuándo |
| --- | --- | --- |
| `propuesta_mysql.sql` | Crea la base `todoo` y todas las tablas | Una vez, en una base vacía |
| `datos_prueba.sql` | Usuarios, proyectos y comentarios de ejemplo | Solo en desarrollo |
| `001_notificaciones.sql` | Tabla de notificaciones aislada | **No** ejecutarlo junto con la propuesta: solo lo usan las pruebas del módulo |

## Paso a paso en HeidiSQL

1. Crea una sesión **MySQL (TCP/IP)** a `127.0.0.1`, puerto `3306`, con tu usuario `root`.
2. Abre `propuesta_mysql.sql` (*Archivo → Cargar archivo SQL…*) y ejecútalo con **F9**.
3. Crea el usuario que usa el backend (cambia la contraseña):
   ```sql
   CREATE USER IF NOT EXISTS 'todoo'@'localhost' IDENTIFIED BY 'CambiaEstaContrasena';
   GRANT SELECT, INSERT, UPDATE, DELETE ON todoo.* TO 'todoo'@'localhost';
   ```
4. Para probar, ejecuta `datos_prueba.sql`. Crea tres usuarios con la contraseña `ToDoo2026!`:
   - `luis@todoo.dev`: administrador de *Mi tablero de tareas* (proyecto 1, el del tablero `/kanban`)
   - `diego@todoo.dev`: miembro del proyecto 1 y administrador de *Diseno*
   - `andrea@todoo.dev`: solo es miembro del proyecto 1

## Conectar el backend

Copia `Backend/main/application-local.properties.example` como `application-local.properties`
(Git lo ignora) y pon la contraseña del paso 3. También puedes usar las variables de entorno
`DB_URL`, `DB_USER` y `DB_PASSWORD`.

## Agregar usuarios reales

Las contraseñas nunca se guardan en texto plano. Para generar el valor de `password_hash`,
desde `Backend/main`:

```bash
mvnw compile
java -cp target/classes com.todoo.seguridad.ContrasenaHasher "LaContrasena"
```

Copia el resultado (empieza con `pbkdf2_sha256$`) en `usuarios.password_hash`. Para que alguien
participe en la conversación de un proyecto, agrégalo a `miembros_proyecto` con `activo = TRUE`.

## Comentarios

- La conversación del tablero son las filas de `comentarios` con `tarea_id` NULL. Las que tienen
  `tarea_id` (comentarios de una tarea) quedan listas para cuando exista la API de tareas.
- Solo los miembros activos del proyecto pueden leer y escribir.
- El autor puede editar o eliminar su comentario; un `administrador` puede eliminar cualquiera.
- Un comentario se marca como editado cuando `fecha_actualizacion` es posterior a `fecha_creacion`.

## Pruebas

Las pruebas usan H2 en modo MySQL con `src/test/resources/todoo_schema.sql`, que es una copia de la
propuesta sin `CREATE DATABASE`/`USE`. `EsquemaPruebasTest` falla si las dos dejan de coincidir; si
cambias la propuesta, actualiza también esa copia.
