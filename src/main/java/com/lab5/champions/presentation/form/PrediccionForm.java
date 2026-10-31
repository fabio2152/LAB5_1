package com.lab5.champions.presentation.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos del formulario de predicción. Las validaciones de formato se hacen aquí;
 * las reglas de negocio (coherencia ganador/marcador, partido por jugar) en la capa de negocio.
 */
public class PrediccionForm {

    @NotBlank(message = "Indica tu nombre")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String autor;

    @NotNull(message = "Elige un resultado")
    @Pattern(regexp = "LOCAL|EMPATE|VISITANTE", message = "Resultado no válido")
    private String resultado;

    @NotNull(message = "Obligatorio")
    @Min(value = 0, message = "Mínimo 0")
    @Max(value = 20, message = "Máximo 20")
    private Integer golesLocal;

    @NotNull(message = "Obligatorio")
    @Min(value = 0, message = "Mínimo 0")
    @Max(value = 20, message = "Máximo 20")
    private Integer golesVisitante;

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(Integer golesLocal) {
        this.golesLocal = golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(Integer golesVisitante) {
        this.golesVisitante = golesVisitante;
    }
}
