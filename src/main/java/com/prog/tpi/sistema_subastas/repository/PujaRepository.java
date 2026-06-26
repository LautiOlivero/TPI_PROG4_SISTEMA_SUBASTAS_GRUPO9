package com.prog.tpi.sistema_subastas.repository;

import com.prog.tpi.sistema_subastas.models.Puja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PujaRepository extends JpaRepository<Puja, Long> {
}
