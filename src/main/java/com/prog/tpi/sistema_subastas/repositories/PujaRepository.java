package com.prog.tpi.sistema_subastas.repositories;

import com.prog.tpi.sistema_subastas.models.Puja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PujaRepository extends JpaRepository<Puja, Long> {
    List<Puja> findBySubastaIdOrderByFechaPujaDesc(Long subastaId);
}
