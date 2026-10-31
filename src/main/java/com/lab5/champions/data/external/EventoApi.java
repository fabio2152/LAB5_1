package com.lab5.champions.data.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Partido ("event") tal como lo devuelve TheSportsDB. Solo se mapean los campos que usa la aplicación;
 * la API envía todos los valores como texto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoApi(
        String idEvent,
        String strSeason,
        String intRound,
        String strHomeTeam,
        String strAwayTeam,
        String strHomeTeamBadge,
        String strAwayTeamBadge,
        String strTimestamp,
        String dateEvent,
        String strTime,
        String strVenue,
        String intHomeScore,
        String intAwayScore,
        String strStatus) {
}
