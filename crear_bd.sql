-- SISPRO-OBRA · PostgreSQL
-- Ejecutar en pgAdmin (Query Tool) o con psql conectado a la base "postgres":
--     psql -U postgres -f database/crear_bd.sql
--
-- No hace falta crear tablas ni insertar usuarios: Hibernate crea las tablas
-- (spring.jpa.hibernate.ddl-auto=update) y AdminInitializer inserta los usuarios
-- de demostración con contraseñas BCrypt en el primer arranque.

CREATE DATABASE sispro_obra
    WITH ENCODING = 'UTF8'
         TEMPLATE = template0;

-- ---------------------------------------------------------------------------
-- SOLO SI la base ya existía de una versión anterior y las altas fallan con
-- errores de "check constraint" / "not null", conéctate a sispro_obra
-- (\c sispro_obra) y reinicia el esquema (BORRA todos los datos):
--
--   DROP SCHEMA public CASCADE;
--   CREATE SCHEMA public;
--
-- Luego vuelve a iniciar la aplicación para que recree las tablas.
-- ---------------------------------------------------------------------------
