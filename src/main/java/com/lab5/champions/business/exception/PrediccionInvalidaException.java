package com.lab5.champions.business.exception;

/**
 * Se lanza cuando una predicción no cumple las reglas de negocio (partido ya iniciado,
 * marcador incoherente con el ganador indicado, etc.).
 */
public class PrediccionInvalidaException extends RuntimeException {

    public PrediccionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
