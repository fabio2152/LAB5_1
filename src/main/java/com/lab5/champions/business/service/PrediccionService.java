package com.lab5.champions.business.service;

import com.lab5.champions.business.dto.NuevaPrediccion;
import com.lab5.champions.business.dto.PrediccionDto;
import com.lab5.champions.business.exception.PartidoNoEncontradoException;
import com.lab5.champions.business.exception.PrediccionInvalidaException;
import com.lab5.champions.data.entity.Partido;
import com.lab5.champions.data.entity.Prediccion;
import com.lab5.champions.data.entity.ResultadoPrediccion;
import com.lab5.champions.data.repository.PartidoRepository;
import com.lab5.champions.data.repository.PrediccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio de las predicciones: valida y registra la predicción asociada a su partido.
 */
@Service
public class PrediccionService {

    static final int MAX_GOLES = 20;
    private static final String EVALUACION_PENDIENTE = "Pendiente";

    private final PrediccionRepository prediccionRepository;
    private final PartidoRepository partidoRepository;
    private final PartidoService partidoService;

    public PrediccionService(PrediccionRepository prediccionRepository,
                             PartidoRepository partidoRepository,
                             PartidoService partidoService) {
        this.prediccionRepository = prediccionRepository;
        this.partidoRepository = partidoRepository;
        this.partidoService = partidoService;
    }

    /** Todas las predicciones registradas, de la más reciente a la más antigua. */
    @Transactional(readOnly = true)
    public List<PrediccionDto> listarPredicciones() {
        return prediccionRepository.findAllByOrderByFechaRegistroDesc().stream()
                .map(this::aDto)
                .toList();
    }

    /**
     * Reglas:
     * <ul>
     *   <li>El partido debe existir y no haber comenzado.</li>
     *   <li>El autor es obligatorio y los goles van de 0 a {@value #MAX_GOLES}.</li>
     *   <li>El ganador (o empate) indicado debe coincidir con el marcador estimado.</li>
     * </ul>
     *
     * @return id de la predicción registrada
     */
    @Transactional
    public Long registrar(NuevaPrediccion datos) {
        Partido partido = partidoRepository.findById(datos.partidoId())
                .orElseThrow(() -> new PartidoNoEncontradoException(datos.partidoId()));

        if (!PartidoService.admitePrediccion(partido)) {
            throw new PrediccionInvalidaException("El partido ya comenzó o finalizó; solo se puede predecir partidos por jugar.");
        }

        String autor = datos.autor() == null ? "" : datos.autor().trim();
        if (autor.isEmpty()) {
            throw new PrediccionInvalidaException("Indica tu nombre.");
        }
        validarGoles(datos.golesLocal());
        validarGoles(datos.golesVisitante());

        ResultadoPrediccion resultado = aResultado(datos.resultado());
        ResultadoPrediccion segunMarcador = resultadoDelMarcador(datos.golesLocal(), datos.golesVisitante());
        if (resultado != segunMarcador) {
            throw new PrediccionInvalidaException("El marcador " + datos.golesLocal() + " - " + datos.golesVisitante()
                    + " no corresponde a \"" + describir(resultado, partido) + "\".");
        }

        Prediccion prediccion = new Prediccion();
        prediccion.setPartido(partido);
        prediccion.setAutor(autor);
        prediccion.setResultado(resultado);
        prediccion.setGolesLocal(datos.golesLocal());
        prediccion.setGolesVisitante(datos.golesVisitante());
        return prediccionRepository.save(prediccion).getId();
    }

    private PrediccionDto aDto(Prediccion pr) {
        Partido p = pr.getPartido();
        boolean finalizado = PartidoService.estaFinalizado(p);
        return new PrediccionDto(
                pr.getId(),
                pr.getAutor(),
                partidoService.aZonaLocal(pr.getFechaRegistro()),
                p.getJornada(),
                p.getEquipoLocal(),
                p.getEquipoVisitante(),
                p.getEscudoLocal(),
                p.getEscudoVisitante(),
                partidoService.aZonaLocal(p.getFechaHora()),
                describir(pr.getResultado(), p),
                pr.getGolesLocal(),
                pr.getGolesVisitante(),
                finalizado ? p.getGolesLocal() : null,
                finalizado ? p.getGolesVisitante() : null,
                evaluar(pr, p, finalizado));
    }

    /** Compara la predicción con el resultado real una vez finalizado el partido. */
    private static String evaluar(Prediccion pr, Partido p, boolean finalizado) {
        if (!finalizado) {
            return EVALUACION_PENDIENTE;
        }
        if (pr.getGolesLocal().equals(p.getGolesLocal()) && pr.getGolesVisitante().equals(p.getGolesVisitante())) {
            return "Marcador exacto";
        }
        return pr.getResultado() == resultadoDelMarcador(p.getGolesLocal(), p.getGolesVisitante())
                ? "Acertó resultado"
                : "Falló";
    }

    private static void validarGoles(Integer goles) {
        if (goles == null || goles < 0 || goles > MAX_GOLES) {
            throw new PrediccionInvalidaException("Los goles deben estar entre 0 y " + MAX_GOLES + ".");
        }
    }

    private static ResultadoPrediccion aResultado(String valor) {
        try {
            return ResultadoPrediccion.valueOf(valor);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new PrediccionInvalidaException("Elige ganador local, empate o ganador visitante.");
        }
    }

    private static ResultadoPrediccion resultadoDelMarcador(int golesLocal, int golesVisitante) {
        if (golesLocal > golesVisitante) {
            return ResultadoPrediccion.LOCAL;
        }
        return golesLocal < golesVisitante ? ResultadoPrediccion.VISITANTE : ResultadoPrediccion.EMPATE;
    }

    static String describir(ResultadoPrediccion resultado, Partido partido) {
        return switch (resultado) {
            case LOCAL -> "Gana " + partido.getEquipoLocal();
            case VISITANTE -> "Gana " + partido.getEquipoVisitante();
            case EMPATE -> "Empate";
        };
    }
}
