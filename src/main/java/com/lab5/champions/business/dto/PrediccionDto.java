package com.lab5.champions.business.dto;

import java.time.LocalDateTime;

/**
 * Predicción registrada, con los datos de su partido, lista para mostrar.
 * Las fechas ya vienen convertidas a la zona horaria de la aplicación.
 *
 * @param pronostico  texto del resultado elegido, p. ej. "Gana Barcelona" o "Empate"
 * @param evaluacion  "Pendiente", "Marcador exacto", "Acertó resultado" o "Falló"
 */
public record PrediccionDto(
        Long id,
        String autor,
        LocalDateTime fechaRegistro,
        Integer jornada,
        String equipoLocal,
        String equipoVisitante,
        String escudoLocal,
        String escudoVisitante,
        LocalDateTime fechaPartido,
        String pronostico,
        Integer golesLocal,
        Integer golesVisitante,
        Integer golesRealesLocal,
        Integer golesRealesVisitante,
        String evaluacion) {

    public boolean tieneResultadoReal() {
        return golesRealesLocal != null && golesRealesVisitante != null;
    }
}
