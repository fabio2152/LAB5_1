package com.lab5.champions.presentation.controller;

import com.lab5.champions.business.dto.NuevaPrediccion;
import com.lab5.champions.business.dto.PartidoDto;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Registro de predicciones para un partido.
 */
@Controller
@RequestMapping("/partidos/{partidoId}/predicciones")
public class PrediccionController {

    private static final String VISTA_FORMULARIO = "predicciones/formulario";

    private final PartidoService partidoService;
    private final PrediccionService prediccionService;

    public PrediccionController(PartidoService partidoService, PrediccionService prediccionService) {
        this.partidoService = partidoService;
        this.prediccionService = prediccionService;
    }

    @GetMapping("/nueva")
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

    @PostMapping
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
