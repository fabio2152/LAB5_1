package com.lab5.champions.presentation.controller;

import com.lab5.champions.business.dto.NuevaPrediccion;
import com.lab5.champions.business.dto.PartidoDto;
import com.lab5.champions.business.dto.PrediccionDto;
import com.lab5.champions.business.exception.PrediccionInvalidaException;
import com.lab5.champions.business.service.PartidoService;
import com.lab5.champions.business.service.PrediccionService;
import com.lab5.champions.presentation.form.PrediccionForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Registro de predicciones para un partido y consulta de todas las predicciones.
 */
@Controller
public class PrediccionController {

    private static final String VISTA_FORMULARIO = "predicciones/formulario";

    private final PartidoService partidoService;
    private final PrediccionService prediccionService;

    public PrediccionController(PartidoService partidoService, PrediccionService prediccionService) {
        this.partidoService = partidoService;
        this.prediccionService = prediccionService;
    }

    @GetMapping("/predicciones")
    public String listar(Model model) {
        List<PrediccionDto> predicciones = prediccionService.listarPredicciones();
        model.addAttribute("predicciones", predicciones);
        model.addAttribute("totalPendientes", predicciones.stream()
                .filter(p -> !p.tieneResultadoReal())
                .count());
        return "predicciones/lista";
    }

    @GetMapping("/partidos/{partidoId}/predicciones/nueva")
    public String formulario(@PathVariable Long partidoId, Model model, RedirectAttributes redirect) {
        PartidoDto partido = partidoService.obtenerPartido(partidoId);
        if (!partido.admitePrediccion()) {
            redirect.addFlashAttribute("error", "Ese partido ya comenzó o finalizó; solo se puede predecir partidos por jugar.");
            return redirectAJornada(partido);
        }
        model.addAttribute("partido", partido);
        model.addAttribute("form", new PrediccionForm());
        return VISTA_FORMULARIO;
    }

    @PostMapping("/partidos/{partidoId}/predicciones")
    public String registrar(@PathVariable Long partidoId,
                            @Valid @ModelAttribute("form") PrediccionForm form,
                            BindingResult errores,
                            Model model,
                            RedirectAttributes redirect) {
        PartidoDto partido = partidoService.obtenerPartido(partidoId);
        model.addAttribute("partido", partido);
        if (errores.hasErrors()) {
            return VISTA_FORMULARIO;
        }
        try {
            prediccionService.registrar(new NuevaPrediccion(partidoId, form.getAutor(), form.getResultado(),
                    form.getGolesLocal(), form.getGolesVisitante()));
        } catch (PrediccionInvalidaException e) {
            errores.reject("prediccion.invalida", e.getMessage());
            return VISTA_FORMULARIO;
        }
        redirect.addFlashAttribute("mensaje", "Predicción registrada: " + partido.equipoLocal() + " "
                + form.getGolesLocal() + " - " + form.getGolesVisitante() + " " + partido.equipoVisitante());
        return redirectAJornada(partido);
    }

    private static String redirectAJornada(PartidoDto partido) {
        return partido.jornada() == null ? "redirect:/partidos" : "redirect:/partidos?jornada=" + partido.jornada();
    }
}
