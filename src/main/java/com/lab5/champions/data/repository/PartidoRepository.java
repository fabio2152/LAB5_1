package com.lab5.champions.data.repository;

import com.lab5.champions.data.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    Optional<Partido> findByIdExterno(String idExterno);

    List<Partido> findAllByOrderByFechaHoraAsc();

    List<Partido> findByJornadaOrderByFechaHoraAsc(Integer jornada);

    @Query("select distinct p.jornada from Partido p where p.jornada is not null order by p.jornada")
    List<Integer> findJornadas();
}
