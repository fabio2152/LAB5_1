package com.lab5.champions.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Partido de la Champions League obtenido de TheSportsDB y guardado en la tabla "partido".
 */
@Entity
@Table(name = "partido")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** idEvent de TheSportsDB; evita duplicar el partido al volver a sincronizar. */
    @Column(name = "id_externo", nullable = false, unique = true, length = 20)
    private String idExterno;

    @Column(nullable = false, length = 20)
    private String temporada;

    private Integer jornada;

    @Column(name = "equipo_local", nullable = false, length = 100)
    private String equipoLocal;

    @Column(name = "equipo_visitante", nullable = false, length = 100)
    private String equipoVisitante;

    @Column(name = "escudo_local", length = 300)
    private String escudoLocal;

    @Column(name = "escudo_visitante", length = 300)
    private String escudoVisitante;

    /** Fecha y hora del partido en UTC. */
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(length = 150)
    private String estadio;

    /** Goles reales; null mientras el partido no se ha jugado. */
    @Column(name = "goles_local")
    private Integer golesLocal;

    @Column(name = "goles_visitante")
    private Integer golesVisitante;

    /** Estado según la API: NS (no iniciado), FT (finalizado), AP (penales), etc. */
    @Column(length = 20)
    private String estado;

    public Long getId() {
        return id;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public Integer getJornada() {
        return jornada;
    }

    public void setJornada(Integer jornada) {
        this.jornada = jornada;
    }

    public String getEquipoLocal() {
        return equipoLocal;
    }

    public void setEquipoLocal(String equipoLocal) {
        this.equipoLocal = equipoLocal;
    }

    public String getEquipoVisitante() {
        return equipoVisitante;
    }

    public void setEquipoVisitante(String equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }

    public String getEscudoLocal() {
        return escudoLocal;
    }

    public void setEscudoLocal(String escudoLocal) {
        this.escudoLocal = escudoLocal;
    }

    public String getEscudoVisitante() {
        return escudoVisitante;
    }

    public void setEscudoVisitante(String escudoVisitante) {
        this.escudoVisitante = escudoVisitante;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstadio() {
        return estadio;
    }

    public void setEstadio(String estadio) {
        this.estadio = estadio;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
