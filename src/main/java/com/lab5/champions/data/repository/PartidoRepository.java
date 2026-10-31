package com.lab5.champions.data.repository;

import com.lab5.champions.data.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    Optional<Partido> findByIdExterno(String idExterno);

    List<Partido> findAllByOrderByFechaHoraAsc();
}
