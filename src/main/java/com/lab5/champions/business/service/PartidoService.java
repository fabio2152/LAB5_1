package com.lab5.champions.business.service;

import com.lab5.champions.business.dto.PartidoDto;
import com.lab5.champions.business.exception.PartidoNoEncontradoException;
import com.lab5.champions.data.entity.Partido;
import com.lab5.champions.data.external.EventoApi;
import com.lab5.champions.data.external.TheSportsDbClient;
import com.lab5.champions.data.repository.PartidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
    /** Código de TheSportsDB para "Not Started". */
    private static final String ESTADO_POR_JUGAR = "NS";

    private final TheSportsDbClient apiClient;
    private final PartidoRepository partidoRepository;
    private final ZoneId zonaHoraria;

    public PartidoService(TheSportsDbClient apiClient,
                          PartidoRepository partidoRepository,
                          @Value("${app.zona-horaria}") String zonaHoraria) {
        this.apiClient = apiClient;
        this.partidoRepository = partidoRepository;
        this.zonaHoraria = ZoneId.of(zonaHoraria);
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

    /**
     * Partidos almacenados ordenados por fecha. Si se indica una jornada, solo los de esa jornada.
     */
    public List<PartidoDto> listarPartidos(Integer jornada) {
        List<Partido> partidos = jornada == null
                ? partidoRepository.findAllByOrderByFechaHoraAsc()
                : partidoRepository.findByJornadaOrderByFechaHoraAsc(jornada);
        return partidos.stream().map(this::aDto).toList();
    }

    public List<Integer> listarJornadas() {
        return partidoRepository.findJornadas();
    }

    public PartidoDto obtenerPartido(Long id) {
        return partidoRepository.findById(id)
                .map(this::aDto)
                .orElseThrow(() -> new PartidoNoEncontradoException(id));
    }

    /** Solo se puede predecir un partido que no ha comenzado (estado NS y fecha futura). */
    static boolean admitePrediccion(Partido partido) {
        return ESTADO_POR_JUGAR.equals(partido.getEstado())
                && (partido.getFechaHora() == null
                || partido.getFechaHora().isAfter(LocalDateTime.now(ZoneOffset.UTC)));
    }

    private PartidoDto aDto(Partido p) {
        return new PartidoDto(
                p.getId(),
                p.getJornada(),
                p.getEquipoLocal(),
                p.getEquipoVisitante(),
                p.getEscudoLocal(),
                p.getEscudoVisitante(),
                aZonaLocal(p.getFechaHora()),
                p.getEstadio(),
                p.getGolesLocal(),
                p.getGolesVisitante(),
                p.getEstado(),
                describirEstado(p.getEstado()),
                ESTADO_POR_JUGAR.equals(p.getEstado()),
                admitePrediccion(p));
    }

    /** Las fechas se guardan en UTC; se muestran en la zona horaria configurada (por defecto, Lima). */
    private LocalDateTime aZonaLocal(LocalDateTime fechaHoraUtc) {
        if (fechaHoraUtc == null) {
            return null;
        }
        return fechaHoraUtc.atZone(ZoneOffset.UTC).withZoneSameInstant(zonaHoraria).toLocalDateTime();
    }

    private static String describirEstado(String estado) {
        if (estado == null) {
            return "Sin información";
        }
        return switch (estado) {
            case ESTADO_POR_JUGAR -> "Por jugar";
            case "FT" -> "Finalizado";
            case "AET" -> "Final (prórroga)";
            case "AP", "PEN" -> "Final (penales)";
            case "1H", "2H", "HT", "ET", "LIVE" -> "En juego";
            case "PST" -> "Postergado";
            case "CANC" -> "Cancelado";
            default -> estado;
        };
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
