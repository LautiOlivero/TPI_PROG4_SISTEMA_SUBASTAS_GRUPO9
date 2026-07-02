package com.prog.tpi.sistema_subastas.repositories;

import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
import com.prog.tpi.sistema_subastas.models.Subasta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubastaRepository extends JpaRepository<Subasta, Long> {

    // Carga todas las asociaciones lazy en una sola query (evita N+1)
    @EntityGraph(attributePaths = {"producto", "producto.categoria", "producto.vendedor", "vendedor", "ganadorActual"})
    @Override
    List<Subasta> findAll();

    @EntityGraph(attributePaths = {"producto", "producto.categoria", "producto.vendedor", "vendedor", "ganadorActual"})
    @Override
    Optional<Subasta> findById(Long id);

    // Para el scheduler: PUBLICADA cuya fechaInicio ya pasó → activar
    List<Subasta> findByEstadoAndFechaInicioBefore(EstadoSubasta estado, Instant fecha);

    // Para el scheduler: ACTIVA cuya fechaCierre ya pasó → cerrar
    List<Subasta> findByEstadoAndFechaCierreBefore(EstadoSubasta estado, Instant fecha);
}
