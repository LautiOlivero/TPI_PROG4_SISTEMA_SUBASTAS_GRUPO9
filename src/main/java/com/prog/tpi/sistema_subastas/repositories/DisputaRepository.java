package com.prog.tpi.sistema_subastas.repository;

import com.prog.tpi.sistema_subastas.models.Disputa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputaRepository extends JpaRepository<Disputa, Long> {
}
