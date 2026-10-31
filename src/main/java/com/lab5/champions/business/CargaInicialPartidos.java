package com.lab5.champions.business;

import com.lab5.champions.business.service.PartidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Al iniciar la aplicación descarga los partidos de la API pública y los almacena en la base de datos.
 * Si la API no responde, la aplicación arranca igual y muestra los partidos ya guardados.
 */
@Component
@ConditionalOnProperty(name = "sportsdb.sync-on-startup", havingValue = "true", matchIfMissing = true)
public class CargaInicialPartidos implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicialPartidos.class);

    private final PartidoService partidoService;

    public CargaInicialPartidos(PartidoService partidoService) {
        this.partidoService = partidoService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Cargando partidos de la Champions League desde TheSportsDB...");
        try {
            partidoService.sincronizarPartidos();
        } catch (RuntimeException e) {
            log.error("No se pudieron cargar los partidos desde la API: {}", e.getMessage());
        }
    }
}
