# ToDoo: tablero de tareas

Frontend de un tablero Kanban con React, Vite y Tailwind CSS. Disponible en `/kanban`, desde el menú **Tablero** o el botón **Abrir tablero de tareas** del inicio.

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

## Almacenamiento y alcance

Este es un tablero de frontend, sin integración con la API ni autenticación. Las tareas se guardan en `localStorage` con la clave `todoo.kanban.tareas`, compartidas por las pestañas de la misma dirección, navegador y perfil. Abrir otra dirección o puerto usa un almacenamiento distinto. Las pestañas abiertas simultáneamente no sincronizan sus cambios en tiempo real.

Si falla el guardado, los cambios siguen visibles en la pestaña y aparece un aviso con una opción para reintentar. Si hay datos dañados, se recuperan las tareas válidas sin escribir sobre el almacenamiento al cargar; el siguiente cambio o **Guardar tablero actual** guarda el contenido mostrado. Borrar los datos del navegador elimina las tareas locales.

El arrastre utiliza la función nativa del navegador; en dispositivos donde no esté disponible se puede usar **Estado**, **Subir** y **Bajar**. Las pantallas de autenticación y las tarjetas de ejemplo de proyectos pertenecen al resto de la aplicación.

## Verificación manual

1. Crear dos tareas y comprobar los contadores. Rechazar un título formado solo por espacios.
2. Editar una tarea: cancelar mantiene el contenido; guardar actualiza título y descripción.
3. Cambiar de columna con el selector y mediante arrastre. Probar una columna vacía y soltar antes de otra tarjeta.
4. Cambiar el orden con Subir/Bajar; en los extremos el botón correspondiente está deshabilitado.
5. Buscar por descripción y comprobar las coincidencias. Crear con una búsqueda activa y verificar que la tarea nueva aparece.
6. Cancelar una eliminación y después confirmarla. Recargar y comprobar tareas, estados, orden y eliminaciones.
7. Navegar por Inicio, Proyectos y Tablero sin perder las tareas.

Las diez pruebas automáticas cubren recuperación sin sobrescribir datos dañados, IDs duplicados, validación de campos, cuota excedida y reintento, persistencia del orden, movimiento entre columnas y destinos inexistentes.

## Commits y reversión

El desarrollo está separado en commits en la rama `Tablero`. Consulta `git log --oneline` para identificar un paso. Para deshacer un commit conservando el historial:

```sh
git revert <hash-del-commit>
```

Si un paso posterior depende del que se quiere retirar, revierte primero los pasos posteriores relacionados. No es necesario borrar ni reescribir el historial.
