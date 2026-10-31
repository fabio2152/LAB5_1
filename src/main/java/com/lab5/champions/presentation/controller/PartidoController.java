package com.lab5.champions.presentation.controller;

import com.lab5.champions.business.dto.PartidoDto;
import com.lab5.champions.business.service.PartidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Pantalla con el listado de partidos almacenados en la base de datos.
 */
@Controller
public class PartidoController {

    private final PartidoService partidoService;

    public PartidoController(PartidoService partidoService) {
        this.partidoService = partidoService;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/partidos";
    }

    @GetMapping("/partidos")
    public String listar(@RequestParam(required = false) Integer jornada, Model model) {
        List<PartidoDto> partidos = partidoService.listarPartidos(jornada);
        model.addAttribute("partidos", partidos);
        model.addAttribute("jornadas", partidoService.listarJornadas());
        model.addAttribute("jornadaSeleccionada", jornada);
        model.addAttribute("totalPorJugar", partidos.stream().filter(PartidoDto::porJugar).count());
        return "partidos/lista";
    }
}
