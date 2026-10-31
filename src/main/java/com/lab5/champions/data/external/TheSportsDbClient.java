package com.lab5.champions.data.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Cliente HTTP de la API pública TheSportsDB (capa de acceso a datos externa).
 * <p>
 * La clave gratuita limita el número de peticiones por minuto, por eso se consulta jornada por jornada
 * con una pausa entre llamadas y se reintenta cuando la API responde 429 (Too Many Requests). Si una jornada
 * sigue fallando se omite, para guardar al menos las demás.
 */
@Component
public class TheSportsDbClient {

    private static final Logger log = LoggerFactory.getLogger(TheSportsDbClient.class);
    private static final int MAX_REINTENTOS = 3;

    private final RestClient restClient;
    private final String leagueId;
    private final String season;
    private final List<Integer> jornadas;
    private final long pausaMs;

    public TheSportsDbClient(@Value("${sportsdb.base-url}") String baseUrl,
                             @Value("${sportsdb.api-key}") String apiKey,
                             @Value("${sportsdb.league-id}") String leagueId,
                             @Value("${sportsdb.season}") String season,
                             @Value("${sportsdb.rounds}") List<Integer> jornadas,
                             @Value("${sportsdb.delay-ms}") long pausaMs) {
        this.restClient = RestClient.create(baseUrl + "/" + apiKey);
        this.leagueId = leagueId;
        this.season = season;
        this.jornadas = jornadas;
        this.pausaMs = pausaMs;
    }

    /** Recupera todos los partidos de las jornadas configuradas para la liga y temporada. */
    public List<EventoApi> obtenerPartidos() {
        List<EventoApi> partidos = new ArrayList<>();
        for (int i = 0; i < jornadas.size(); i++) {
            if (i > 0) {
                pausar(pausaMs);
            }
            List<EventoApi> deLaJornada = obtenerPartidosDeJornada(jornadas.get(i));
            log.info("TheSportsDB: jornada {} de {} -> {} partidos", jornadas.get(i), season, deLaJornada.size());
            partidos.addAll(deLaJornada);
        }
        return partidos;
    }

    private List<EventoApi> obtenerPartidosDeJornada(int jornada) {
        for (int intento = 1; ; intento++) {
            try {
                RespuestaEventos respuesta = restClient.get()
                        .uri("/eventsround.php?id={liga}&r={jornada}&s={temporada}", leagueId, jornada, season)
                        .retrieve()
                        .body(RespuestaEventos.class);
                return respuesta == null || respuesta.events() == null ? List.of() : respuesta.events();
            } catch (HttpClientErrorException.TooManyRequests | ResourceAccessException e) {
                // 429 o corte de conexión: la API gratuita bloquea temporalmente al superar el límite
                if (intento == MAX_REINTENTOS) {
                    log.error("TheSportsDB: se omite la jornada {} tras {} intentos ({})",
                            jornada, MAX_REINTENTOS, e.getMessage());
                    return List.of();
                }
                log.warn("TheSportsDB: jornada {} falló ({}), reintento {} de {}",
                        jornada, e.getMessage(), intento, MAX_REINTENTOS - 1);
                pausar(pausaMs * 10);
            }
        }
    }

    private static void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Sincronización interrumpida", e);
        }
    }
}
