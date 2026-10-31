package com.lab5.champions.data.repository;

import com.lab5.champions.data.entity.Prediccion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrediccionRepository extends JpaRepository<Prediccion, Long> {

    /** Todas las predicciones (más recientes primero) junto con su partido en una sola consulta. */
    @EntityGraph(attributePaths = "partido")
    List<Prediccion> findAllByOrderByFechaRegistroDesc();
}
