# ToDoo: tablero de tareas y proyectos

Frontend con React, Vite y Tailwind CSS. El tablero está disponible en `/kanban`, desde el menú **Tablero** o **Abrir tablero** en la tarjeta del proyecto. La vista de proyectos está en `/proyectos`, accesible desde el menú **Proyectos** o **Ver proyectos** en el inicio.

## Ejecutar

Desde `Frontend`, con Node.js y pnpm instalados:

```sh
pnpm install
pnpm dev
```

Abre la dirección local que muestra Vite y entra a `/kanban`.

```sh
pnpm test     # Pruebas de recuperación, guardado y movimiento
pnpm lint     # Revisión de todo el frontend
pnpm build    # Compilación para producción
pnpm preview  # Vista local de la compilación
```

## Funcionalidad

- Crear tareas con título obligatorio de hasta 100 caracteres y descripción opcional de hasta 500.
- Editar y guardar una tarea, o cancelar los cambios con el botón o Escape.
- Eliminar tareas con confirmación; cancelar conserva la tarea.
- Cambiar entre **Por hacer**, **En progreso** y **Hecho** mediante el selector de estado.
- Arrastrar desde **⠿ Arrastrar tarea**: soltar sobre una tarjeta la coloca antes de ella; soltar en el espacio de una columna la coloca al final.
- Ordenar con **Subir** y **Bajar**, también disponibles con teclado y en pantallas táctiles. Durante una búsqueda, se ordenan las tarjetas visibles.
- Buscar por título o descripción sin distinguir mayúsculas. Los contadores muestran las coincidencias respecto al total de la columna.
- Recuperar las tareas, sus estados y su orden después de recargar o navegar fuera del tablero.

Al crear una tarea se limpia la búsqueda para mostrarla en **Por hacer**. La página muestra los estados vacíos y los avisos de acciones.

## Organización

```text
src/
├── pages/Home/KanbanPage.jsx      # Estado y composición del tablero
├── components/Tareas/
│   ├── TareaCard.jsx             # Tarjeta, edición, confirmación y controles
│   └── TareaForm.jsx             # Formulario para crear y editar
├── components/Nav/NavBar.jsx     # Navegación compartida
├── services/tareasStorage.js     # Lectura, validación y guardado local
└── utils/moverTarea.js           # Movimiento y posición de una tarjeta
tests/
├── tareasStorage.test.js
└── moverTarea.test.js
```

Se conservan las carpetas existentes de autenticación, inicio y proyectos.

## Visualización de proyectos

`pages/Home/ProyectosPage.jsx` presenta el proyecto del tablero actual mediante `components/Proyectos/ProyectoCard.jsx`, también reutilizado en el inicio. La tarjeta recibe el proyecto y sus tareas por props; los datos de ejemplo anteriores se reemplazaron por las tareas guardadas del tablero.

- Resumen con cantidad de proyectos, tareas, completadas y pendientes.
- Tarjeta con nombre, descripción, estado, barra de avance y contadores por columna.
- Búsqueda por nombre o descripción, combinable con el filtro de estado.
- Mensaje sin coincidencias con opción para limpiar los filtros.
- Detalle desplegable con títulos, descripciones y estados de las tareas.
- Enlace directo al tablero para administrar sus tareas.
- **Actualizar proyectos** vuelve a leer las tareas guardadas, por ejemplo después de cambios hechos en otra pestaña. Entrar de nuevo a la página o recargarla también actualiza el resumen. Los filtros se conservan al pulsar Actualizar.

El estado del proyecto se calcula a partir de las tareas:

| Condición | Estado |
| --- | --- |
| No tiene tareas | Sin tareas |
| Todas están en Por hacer | Por comenzar |
| Hay tareas en progreso o algunas completadas | En progreso |
| Todas están en Hecho | Completado |

El porcentaje corresponde a tareas completadas sobre el total. Solo llega al 100 % cuando todas están hechas. El resumen general cuenta todas las tareas, aunque un filtro oculte el proyecto.

La fuente actual contiene un único proyecto vinculado al tablero existente, definido en `constants/proyectos.js`. No se han creado proyectos ficticios ni persistencia de múltiples tableros. Esta entrega cubre visualización; la creación, edición y eliminación de proyectos y su integración con una API quedan fuera de este alcance.

La lógica de los contadores está en `utils/resumenProyecto.js`, con pruebas en `tests/resumenProyecto.test.js`. Para verificar la vista, crea dos tareas en el tablero y marca una como Hecho: `/proyectos` debe mostrar 50 %, una completada y una pendiente. Comprueba también los filtros, el detalle y Actualizar después de completar la segunda tarea desde otra pestaña.

## Notificaciones

La rama `notificaciones` se integra conservando las rutas existentes de inicio, autenticación, proyectos y tablero. La bandeja está en `/notificaciones` y se puede abrir desde la navegación compartida.

- Bandeja ordenada de más reciente a más antigua, con filtros Todas y Sin leer.
- Campana desplegable con contador y lectura desde el panel.
- Marcar como leída actualiza la bandeja y la campana; Escape cierra el desplegable y devuelve el foco.
- Estados vacíos y mensajes de confirmación.

La rama `notificaciones-backend` añade la API y `useNotifications`. La bandeja consulta `GET /api/notificaciones`, obtiene el token CSRF y guarda la lectura mediante `PATCH /api/notificaciones/{id}/leida`. Las solicitudes usan la sesión del servidor; el futuro login debe establecer `usuarioId` tras autenticar al usuario. Un 401 dirige a `/login`, y los fallos de conexión muestran un error con opción para actualizar. El proxy de desarrollo envía `/api` a `127.0.0.1:8080`.

El backend está configurado para MySQL mediante `DB_URL`, `DB_USER` y `DB_PASSWORD`. La base aún debe definirse y crearse; Spring no ejecuta el SQL automáticamente. Las pruebas del backend usan H2 en memoria. Los próximos requisitos y las instrucciones de configuración se conservan en `docs/requisitos-pendientes.md`, en la raíz del repositorio.

Se conservó `pages/Auth/LoginPage.jsx` y se descartó su copia en `pages/LoginPage.jsx`. También se conservaron la documentación previa y el `.gitignore` de la raíz. La campana se añadió como contenido opcional de `NavBar` en la página de notificaciones.

La integración inicial de la interfaz se comprobó en el navegador con datos temporales para verificar orden, filtros, lectura desde bandeja y campana, contadores y Escape. La integración del backend añade pruebas para sesión, CSRF, aislamiento entre usuarios, lectura persistente y creación transaccional del aviso al asignar. La llamada desde el futuro servicio de tareas todavía está pendiente.

## Almacenamiento y alcance

Este es un tablero de frontend, sin integración con la API ni autenticación. Las tareas se guardan en `localStorage` con la clave `todoo.kanban.tareas`, compartidas por las pestañas de la misma dirección, navegador y perfil. Abrir otra dirección o puerto usa un almacenamiento distinto. Las pestañas abiertas simultáneamente no sincronizan sus cambios en tiempo real.

Si falla el guardado, los cambios siguen visibles en la pestaña y aparece un aviso con una opción para reintentar. Si hay datos dañados, se recuperan las tareas válidas sin escribir sobre el almacenamiento al cargar; el siguiente cambio o **Guardar tablero actual** guarda el contenido mostrado. Borrar los datos del navegador elimina las tareas locales.

El arrastre utiliza la función nativa del navegador; en dispositivos donde no esté disponible se puede usar **Estado**, **Subir** y **Bajar**. Las pantallas de autenticación pertenecen al resto de la aplicación.

## Verificación manual

1. Crear dos tareas y comprobar los contadores. Rechazar un título formado solo por espacios.
2. Editar una tarea: cancelar mantiene el contenido; guardar actualiza título y descripción.
3. Cambiar de columna con el selector y mediante arrastre. Probar una columna vacía y soltar antes de otra tarjeta.
4. Cambiar el orden con Subir/Bajar; en los extremos el botón correspondiente está deshabilitado.
5. Buscar por descripción y comprobar las coincidencias. Crear con una búsqueda activa y verificar que la tarea nueva aparece.
6. Cancelar una eliminación y después confirmarla. Recargar y comprobar tareas, estados, orden y eliminaciones.
7. Navegar por Inicio, Proyectos y Tablero sin perder las tareas.

Las diez pruebas automáticas cubren recuperación sin sobrescribir datos dañados, IDs duplicados, validación de campos, cuota excedida y reintento, persistencia del orden, movimiento entre columnas y destinos inexistentes.

Otras cuatro pruebas verifican el resumen de proyectos: sin tareas, por comenzar, avance parcial y completado. Cinco pruebas cubren las solicitudes del cliente de notificaciones, incluyendo sesión, token CSRF y errores. En total, `pnpm test` ejecuta 19 pruebas. Desde `Backend/main`, `mvnw.cmd test` ejecuta otras 10 pruebas con H2.

## Commits y reversión

El desarrollo está separado en commits en la rama `Tablero`. Consulta `git log --oneline` para identificar un paso. Para deshacer un commit conservando el historial:

```sh
git revert <hash-del-commit>
```

Si un paso posterior depende del que se quiere retirar, revierte primero los pasos posteriores relacionados. No es necesario borrar ni reescribir el historial.
