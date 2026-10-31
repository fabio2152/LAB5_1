package com.lab5.champions.data.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Envoltorio de la respuesta JSON de TheSportsDB: {@code {"events": [...]}}. "events" es null si no hay partidos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RespuestaEventos(List<EventoApi> events) {
}
