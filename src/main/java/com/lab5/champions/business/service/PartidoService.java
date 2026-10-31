package com.lab5.champions.business.service;

import com.lab5.champions.data.entity.Partido;
import com.lab5.champions.data.external.EventoApi;
import com.lab5.champions.data.external.TheSportsDbClient;
import com.lab5.champions.data.repository.PartidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lógica de negocio de los partidos: sincronización con la API pública y consulta del listado.
 */
@Service
public class PartidoService {

    private static final Logger log = LoggerFactory.getLogger(PartidoService.class);

    private final TheSportsDbClient apiClient;
    private final PartidoRepository partidoRepository;

    public PartidoService(TheSportsDbClient apiClient, PartidoRepository partidoRepository) {
        this.apiClient = apiClient;
        this.partidoRepository = partidoRepository;
    }

    /**
     * Descarga los partidos de la API y los guarda en la base de datos. Si un partido ya existe
     * (mismo idExterno) se actualiza (marcador, estado, fecha) en lugar de duplicarse.
     *
     * @return cantidad de partidos guardados
     */
    public int sincronizarPartidos() {
        List<EventoApi> eventos = apiClient.obtenerPartidos();

        Map<String, Partido> existentes = partidoRepository.findAll().stream()
                .collect(Collectors.toMap(Partido::getIdExterno, Function.identity()));

        List<Partido> aGuardar = new ArrayList<>();
        for (EventoApi evento : eventos) {
            if (evento.idEvent() == null || evento.strHomeTeam() == null || evento.strAwayTeam() == null) {
                continue;
            }
            Partido partido = existentes.getOrDefault(evento.idEvent(), new Partido());
            copiarDatos(evento, partido);
            existentes.put(evento.idEvent(), partido);
            aGuardar.add(partido);
        }

        partidoRepository.saveAll(aGuardar);
        log.info("Sincronización completa: {} partidos guardados ({} en total en la BD)",
                aGuardar.size(), partidoRepository.count());
        return aGuardar.size();
    }

    public List<Partido> listarPartidos() {
        return partidoRepository.findAllByOrderByFechaHoraAsc();
    }

    private static void copiarDatos(EventoApi evento, Partido partido) {
        partido.setIdExterno(evento.idEvent());
        partido.setTemporada(evento.strSeason());
        partido.setJornada(aEntero(evento.intRound()));
        partido.setEquipoLocal(evento.strHomeTeam());
        partido.setEquipoVisitante(evento.strAwayTeam());
        partido.setEscudoLocal(evento.strHomeTeamBadge());
        partido.setEscudoVisitante(evento.strAwayTeamBadge());
        partido.setFechaHora(aFechaHora(evento));
        partido.setEstadio(evento.strVenue());
        partido.setGolesLocal(aEntero(evento.intHomeScore()));
        partido.setGolesVisitante(aEntero(evento.intAwayScore()));
        partido.setEstado(evento.strStatus());
    }

    private static Integer aEntero(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Usa strTimestamp (UTC); si no viene, combina dateEvent + strTime. */
    private static LocalDateTime aFechaHora(EventoApi evento) {
        try {
            if (evento.strTimestamp() != null && !evento.strTimestamp().isBlank()) {
                return LocalDateTime.parse(evento.strTimestamp().trim());
            }
            if (evento.dateEvent() != null && !evento.dateEvent().isBlank()) {
                LocalTime hora = evento.strTime() == null || evento.strTime().isBlank()
                        ? LocalTime.MIDNIGHT
                        : LocalTime.parse(evento.strTime().trim());
                return LocalDate.parse(evento.dateEvent().trim()).atTime(hora);
            }
        } catch (DateTimeParseException e) {
            log.warn("Fecha no válida para el partido {}: {}", evento.idEvent(), e.getMessage());
        }
        return null;
    }
}
