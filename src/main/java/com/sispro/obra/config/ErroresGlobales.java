package com.sispro.obra.config;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Registros que no existen (orElseThrow) -> página 404 genérica, sin detalles técnicos. */
@ControllerAdvice
public class ErroresGlobales {
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String noEncontrado() { return "error"; }
}
