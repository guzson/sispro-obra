# SISPRO-OBRA (Java 17+ / Spring Boot 3.4 / PostgreSQL)

Inicio de sesión con roles, usuarios, obras, etapas, tareas, incidencias, comentarios, reportes y auditoría.

## Preparación
1. Crear la base en PostgreSQL (pgAdmin o psql): `CREATE DATABASE sispro_obra ENCODING 'UTF8' TEMPLATE template0;`
   (también está en `database/crear_bd.sql`).
2. Revisar `src/main/resources/application.properties`. Por defecto usa
   `jdbc:postgresql://localhost:5432/sispro_obra`, usuario `postgres`, clave `root`.
   Para no guardar la clave en el archivo, definir las variables de entorno `DB_PASSWORD`
   (y opcionalmente `DB_USER`, `DB_URL`).
3. Abrir la carpeta con `pom.xml` en NetBeans y ejecutar **Clean and Build**, luego `spring-boot:run`
   (o por consola: `mvn clean spring-boot:run`).
4. Entrar a http://localhost:8080/login

## Usuarios iniciales
Por seguridad el **modo demo está apagado por defecto** (`sispro.demo.enabled=false`) y la pantalla de login ya no muestra credenciales.

- **Primer arranque sin administradores:** se crea un administrador con el correo `ADMIN_CORREO` (por defecto `admin@sispro.local`)
  y la clave `ADMIN_CLAVE`. Si no defines `ADMIN_CLAVE`, se genera una clave aleatoria que aparece **una sola vez** en la consola
  (busca `Administrador inicial creado`). Cámbiala después.
- **Para clases o pruebas:** arrancar con la variable de entorno `DEMO_ENABLED=true`. Crea admin@sispro.local / supervisor@sispro.local /
  trabajador@sispro.local con contraseñas conocidas (ver `AdminInitializer`) y datos de ejemplo. No usar fuera de clase.
- Si tu base ya tenía usuarios demo de versiones anteriores, siguen existiendo: cambia sus contraseñas o elimínalos desde pgAdmin.

## Crear obra e incidencia
Iniciar sesión como administrador. En Obras -> Nueva obra completar nombre, dirección, inicio, fin posterior al inicio
y elegir un ingeniero residente (p. ej. `Supervisor Demo`). Sin ingeniero la obra queda en BORRADOR y no acepta incidencias.
En Incidencias -> Nueva incidencia elegir una obra ACTIVA, título, descripción, etapa y tipo.
Si el tipo es FALTA_MATERIALES, ingresar una cantidad positiva.

## Solución de problemas
- `FATAL: password authentication failed` / `Connection refused`: revisar clave en `DB_PASSWORD` o `application.properties` y que PostgreSQL esté iniciado en el puerto 5432.
- `database "sispro_obra" does not exist`: crear la base (paso 1).
- `Port 8080 was already in use`: cerrar la ejecución anterior o iniciar con la variable `PORT=9090`.
- Errores `violates check constraint` o `null value in column ... violates not-null` en una base creada por una versión anterior:
  reiniciar el esquema (borra los datos) con `DROP SCHEMA public CASCADE; CREATE SCHEMA public;` y volver a arrancar.
- Si NetBeans muestra errores raros de compilación: Clean and Build y, si persiste, borrar la carpeta `target/`.

## Seguridad (correcciones de la auditoría cruzada S14)
| Hallazgo | Corrección aplicada |
| --- | --- |
| H-1 Credenciales de admin visibles / usuarios demo activos | Se quitó la línea de `login.html`; `sispro.demo.enabled=false` por defecto; administrador inicial por variables de entorno o clave aleatoria. |
| H-2 Sin bloqueo por intentos fallidos | `LoginAttemptService` + `AuthenticationFailureHandler` en `SecurityConfig`: 5 fallos bloquean la cuenta 15 minutos (campos `intentosFallidos` / `bloqueadoHasta`). Se registra en Auditoría (LOGIN_FALLIDO, CUENTA_BLOQUEADA, LOGIN_OK). |
| H-3 El registro revela correos existentes | `UsuarioService.registrarPublico` responde igual exista o no el correo. El login usa un único mensaje para usuario inexistente, clave incorrecta y cuenta bloqueada. |
| H-4 Errores técnicos de la BD visibles | `WebController.mensaje()` muestra un mensaje genérico y deja el detalle solo en el log. Página `error.html` genérica y `server.error.include-*=never`. |

Mejoras adicionales: contraseña de 8 a 64 caracteres; `show-sql=false`; sesión con expiración de 30 min, cookie HttpOnly/SameSite;
logout invalida la sesión y borra la cookie; cabeceras CSP y Referrer-Policy (scripts movidos a `static/js`); exportación CSV
protegida contra inyección de fórmulas; la lista de usuarios en el detalle de obra solo se carga para administrador y supervisor.

## Revisión cruzada de requerimientos (S13) — cambios en el código
| Issue | Cambio aplicado |
| --- | --- |
| #9 RF13 | Una obra cerrada queda en **solo lectura**: no acepta etapas, tareas, avance, asignaciones, comentarios, anulaciones ni cambios de estado, tanto en la pantalla como en el servidor. Sale del listado de obras (hay un enlace "Ver obras cerradas") y del panel de inicio. |
| #9 RF21 | El servidor rechaza reasignar una incidencia **resuelta o anulada**; el responsable elegido debe pertenecer a la obra y se muestra un mensaje en pantalla. |
| #9 RF24 | El **responsable asignado** (por ejemplo el trabajador/subcontratista) ya puede cambiar el estado de su incidencia. No se puede pasar a Asignada/En proceso/Resuelta **sin responsable**. Se guarda la fecha de cada cambio y el movimiento queda en Auditoría (CAMBIAR_ESTADO). |
| #9 RF34 | Elegir un responsable (o etapa) que no es de la obra muestra un mensaje claro en vez de un 403. La lista de responsables solo incluye a los integrantes de la obra. |
| #9 RF39 | "Actividad de incidencias" muestra las **5 más recientes**; el total de usuarios solo lo ve el administrador; el saludo usa el nombre, no el correo. |
| #7 RF21/RF24/RF40 | La incidencia guarda `fechaAsignacion`, `fechaResolucion`, `actualizada` y quién la registró. Los **Reportes** muestran incidencias por semana y tiempo promedio de respuesta y de resolución. |
| #6 RF51 | Nuevo: **editar** nombre, teléfono y rol de un usuario y **desactivar/activar** cuentas (`/usuarios`). Un usuario desactivado no puede iniciar sesión ni seguir usando una sesión abierta. No se puede desactivarse a sí mismo, cambiarse el rol ni dejar el sistema sin administrador activo. El autorregistro `/registro` (rol Trabajador) queda como flujo alterno de RF01. |
| #3 RF27 | La prioridad se calcula automáticamente al registrar la incidencia (paso 5 de RF15/RF16/RF17); quien asigna puede ajustarla a mano. Tabla de puntajes (regla de negocio, editable en `IncidenciaService.calcularPrioridad`): ERROR_EJECUCION y REPROCESO 4, RETRASO 3, FALTA_MATERIALES 2 (+1 desde 20 unidades, +2 desde 50, +3 desde 100), OTRA 1. 5 o más = CRÍTICA, 4 = ALTA, 2-3 = MEDIA, 1 = BAJA. |
| RF16 | La cantidad de materiales solo se guarda cuando el tipo es FALTA_MATERIALES. |

Los issues #1, #2, #4, #5 y #8 son cambios del documento de requerimientos (Excel) y no del código: fusiones de RF11 en RF06, RF19/RF20 como pasos de RF15-17, RF27/RF29 como pasos, nuevo criterio de RF43, RF44 dentro de RF40 y la columna "rol en la app".

Pruebas automáticas: `SeguridadTest.java` (auditoría de seguridad) y `RequerimientosTest.java` (revisión de requerimientos), en `src/test/java/com/sispro/obra/service/`.

Si publicas con HTTPS, define `COOKIE_SECURE=true`. Cambia también la clave de la base de datos (`DB_PASSWORD`).
