package com.prog.tpi.sistema_subastas.repositories;

import com.prog.tpi.sistema_subastas.models.EstadoSubasta;
import com.prog.tpi.sistema_subastas.models.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SubastaRepository extends JpaRepository<Subasta, Long> {

    @EntityGraph(attributePaths = {"producto", "producto.categoria", "producto.vendedor", "vendedor", "ganadorActual"})
    @Query("SELECT s FROM Subasta s")
    List<Subasta> findAllConDetalles();

    @EntityGraph(attributePaths = {"producto", "producto.categoria", "producto.vendedor", "vendedor", "ganadorActual"})
    List<Subasta> findByVendedorId(Long vendedorId);

    // Para el scheduler: PUBLICADA cuya fechaInicio ya pasó → activar
    List<Subasta> findByEstadoAndFechaInicioBefore(EstadoSubasta estado, Instant fecha);

    // Para el scheduler: ACTIVA cuya fechaCierre ya pasó → cerrar
    List<Subasta> findByEstadoAndFechaCierreBefore(EstadoSubasta estado, Instant fecha);

}
