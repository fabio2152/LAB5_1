package com.lab5.champions.business.dto;

import java.time.LocalDateTime;

/**
 * Datos de un partido que la capa de negocio entrega a la presentación.
 * La fecha ya viene convertida a la zona horaria de la aplicación.
 */
public record PartidoDto(
        Long id,
        Integer jornada,
        String equipoLocal,
        String equipoVisitante,
        String escudoLocal,
        String escudoVisitante,
        LocalDateTime fechaHora,
        String estadio,
        Integer golesLocal,
        Integer golesVisitante,
        String estado,
        String estadoDescripcion,
        boolean porJugar) {

    public boolean tieneMarcador() {
        return golesLocal != null && golesVisitante != null;
    }
}
