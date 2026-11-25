package com.sispro.obra.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Las entidades de este proyecto usan campos públicos (sin getters/setters).
 * Spring MVC por defecto solo enlaza propiedades JavaBean, por lo que los
 * formularios llegaban vacíos (nombre/fechas nulos -> "campos obligatorios") y
 * th:field fallaba al abrir el formulario de incidencias.
 *
 * Se habilita el acceso directo a campos y, para evitar "mass assignment"
 * (que alguien envíe id, estado, rol, etc. por su cuenta), se listan
 * explícitamente los campos que cada formulario puede modificar.
 */
@ControllerAdvice
public class BindingConfig {

    @InitBinder("obra")
    void obra(WebDataBinder b) {
        b.initDirectFieldAccess();
        b.setAllowedFields("id", "nombre", "direccion", "latitud", "longitud", "fechaInicio", "fechaFin");
    }

    @InitBinder("incidencia")
    void incidencia(WebDataBinder b) {
        b.initDirectFieldAccess();
        b.setAllowedFields("titulo", "descripcion", "etapa", "tipo", "cantidadMaterial");
    }

    @InitBinder("usuario")
    void usuario(WebDataBinder b) {
        b.initDirectFieldAccess();
        b.setAllowedFields("nombre", "correo", "telefono", "rol");
    }
}
