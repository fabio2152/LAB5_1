package com.lab5.champions.business.dto;

/**
 * Datos que la presentación envía para registrar una predicción.
 *
 * @param resultado "LOCAL", "EMPATE" o "VISITANTE"
 */
public record NuevaPrediccion(
        Long partidoId,
        String autor,
        String resultado,
        Integer golesLocal,
        Integer golesVisitante) {
}
