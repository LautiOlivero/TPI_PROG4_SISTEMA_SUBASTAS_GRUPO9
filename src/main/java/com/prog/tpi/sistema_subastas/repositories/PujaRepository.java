package com.prog.tpi.sistema_subastas.repositories;

import com.prog.tpi.sistema_subastas.models.Puja;
import com.prog.tpi.sistema_subastas.models.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PujaRepository extends JpaRepository<Puja, Long> {

    List<Puja> findBySubastaOrderByFechaPujaDesc(Subasta subasta);

    Optional<Puja> findTopBySubastaOrderByMontoOfertadoDesc(Subasta subasta);

    boolean existsBySubastaId(Long subastaId);

    List<Puja> findBySubastaIdOrderByFechaPujaDesc(Long subastaId);
}
