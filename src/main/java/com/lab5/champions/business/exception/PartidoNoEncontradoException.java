package com.lab5.champions.business.exception;

public class PartidoNoEncontradoException extends RuntimeException {

    public PartidoNoEncontradoException(Long id) {
        super("No existe el partido con id " + id);
    }
}
