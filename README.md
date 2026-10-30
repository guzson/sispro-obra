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

Usuarios de prueba (se crean solos la primera vez):
- admin@sispro.local / Admin1234
- supervisor@sispro.local / Supervisor123
- trabajador@sispro.local / Trabajador123

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

No es una solución de producción: deshabilitar `sispro.demo.enabled` y endurecer la seguridad antes de publicar.
