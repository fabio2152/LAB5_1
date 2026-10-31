package com.lab5.champions.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Predicción registrada por un usuario, asociada a un partido (tabla "prediccion").
 */
@Entity
@Table(name = "prediccion")
public class Prediccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Partido al que pertenece la predicción (FK partido_id). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    /** Nombre de quien hace la predicción. */
    @Column(nullable = false, length = 80)
    private String autor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ResultadoPrediccion resultado;

    @Column(name = "goles_local", nullable = false)
    private Integer golesLocal;

    @Column(name = "goles_visitante", nullable = false)
    private Integer golesVisitante;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void alRegistrar() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public ResultadoPrediccion getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoPrediccion resultado) {
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
