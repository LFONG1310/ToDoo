# Acuerdos para las próximas etapas

Estos requisitos se registran para conservar el contexto del equipo. No representan funcionalidades ya implementadas por el merge de notificaciones.

- Base de datos prevista: MySQL, administrada con HeidiSQL. Todavía no se ha creado ni aprobado el esquema definitivo.
- Usuarios con cuentas y autenticación. La API de notificaciones requiere una sesión del servidor con el atributo `usuarioId`; el login futuro debe establecerlo tras validar las credenciales y renovar la sesión.
- Proyectos con miembros del equipo y sus tareas. Validar permisos y pertenencia al proyecto desde el backend.
- Cada tarea tendrá título, descripción, responsable, prioridad alta/media/baja y fecha límite.
- Comentarios para discutir dentro del sistema. La propuesta permite comentarios del proyecto y comentarios asociados a una tarea.
- Crear una notificación para la cuenta del nuevo responsable al asignarle una tarea. Una reasignación notifica al nuevo responsable; editar otros campos sin cambiarlo no genera otro aviso.
- Guardar la asignación y su notificación en la misma transacción. La rama integrada ofrece `NotificacionService.notificarAsignacion`; invocarlo desde el futuro servicio de tareas después de comprobar permisos. No hay todavía un servicio de tareas que haga esa llamada.
- La bandeja consulta la API al entrar y al pulsar Actualizar. Las notificaciones en tiempo real mediante polling, SSE o WebSocket siguen pendientes de decisión.
- Los datos actuales del tablero y proyectos siguen en `localStorage`; su migración a MySQL requiere las futuras API de usuarios, proyectos, tareas y comentarios.

## Configuración integrada

Se conservan Java 25, el proyecto Maven y las rutas del frontend existentes. El punto de arranque de Spring es `com.todoo.TodooApplication`; el anterior `com.todoo.main.MainApplication` delega en él para conservar el acceso desde el IDE.

MySQL usa Connector/J con las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`. No guardar credenciales en Git. Los tiempos se almacenan e interpretan en UTC. Las pruebas usan H2 en memoria, no una base MySQL real.

Ejecutar desde `Backend/main`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

El segundo comando requiere un servidor y un esquema configurados. No se ejecuta SQL automáticamente (`spring.sql.init.mode=never`). El frontend usa el proxy `/api` a `127.0.0.1:8080` durante el desarrollo. Sin backend o sesión válidos, muestra un error y permite reintentar o ir a `/login`.

## Propuesta SQL

`Backend/main/database/propuesta_mysql.sql` es una propuesta para una base nueva y vacía en MySQL 8.4. No se ha ejecutado ni validado contra un servidor MySQL real. Requiere revisar el esquema con el equipo antes de usarlo. No ejecutar además `001_notificaciones.sql`: ambos definen la misma tabla y el archivo 001 se conserva para las pruebas aisladas del módulo.

La propuesta requiere responsable y fecha límite por tarea, y usa IDs enteros compatibles con la tabla de notificaciones integrada. Los usuarios se desactivan y los proyectos/tareas se archivan para conservar las referencias y discusiones. La pertenencia se conserva mediante `activo` en los miembros; el servicio futuro deberá comprobar ese estado.

No se agregan triggers de notificación: duplicarían los avisos si el servicio de tareas llama también a `notificarAsignacion`. La creación automática debe tener un único responsable, en este diseño el servicio transaccional del backend. El esquema prepara el almacenamiento, pero por sí solo no implementa el flujo de asignación ni la autenticación.

## Filtros del tablero (rama `Filtros`)

El tablero filtra por prioridad, responsable y "solo mis tareas" sobre las tareas de `localStorage`. El backend ya expone la consulta equivalente, pero el frontend todavía no la usa.

- `GET /api/proyectos/{id}/tareas` acepta `prioridad` (`alta`, `media`, `baja`), `responsableId` y `soloMias=true`. Devuelve las tareas no archivadas del proyecto ordenadas por estado, posición e id, con el responsable como `{ id, nombre }`.
- `GET /api/proyectos/{id}/responsables` lista los miembros activos del proyecto para el selector.
- Ambas rutas exigen el atributo de sesión `usuarioId` (401 sin sesión) y que el usuario sea miembro activo del proyecto (404 en caso contrario, para no revelar proyectos ajenos). Un filtro inválido devuelve 400.
- `soloMias` usa siempre el usuario de la sesión. Si además se envía otro `responsableId`, la respuesta es una lista vacía.

Todo el SQL está en `com.todoo.tareas.TareaRepository` y sigue `database/propuesta_mysql.sql` (`tareas`, `miembros_proyecto`, `usuarios`). Si el esquema definitivo cambia nombres de tablas o columnas, solo ese archivo y `src/test/resources/tareas_schema.sql` deben ajustarse. Las pruebas usan H2; la consulta no se ha ejecutado contra un MySQL real.

Para conectar el tablero cuando existan la base, el login y la API para crear y editar tareas:

1. Reemplazar `MIEMBROS_EQUIPO` por `getResponsables(proyectoId)` y `USUARIO_ACTUAL_ID` por el usuario de la sesión (`constants/tareas.js`).
2. Cargar las tareas con `getTareas(proyectoId, filtros)` de `services/tareasApi.js` en lugar de `cargarTablero`. La búsqueda de texto sigue aplicándose en el navegador con `filtrarTareas`.
3. `adaptarTarea` convierte la respuesta al formato del tablero. Las tarjetas pueden mostrar `responsableNombre` en lugar de buscar el nombre en `MIEMBROS_EQUIPO`.
4. El frontend usa `PROYECTO_PRINCIPAL.id = 'principal'`; la API espera un id numérico de `proyectos`.
