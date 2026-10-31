package com.lab5.champions.presentation.controller;

import com.lab5.champions.business.exception.PartidoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Traduce las excepciones de la capa de negocio a respuestas web.
 */
@ControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(PartidoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String partidoNoEncontrado(PartidoNoEncontradoException e, Model model) {
        model.addAttribute("mensaje", e.getMessage());
        return "error/no-encontrado";
    }
}
