package com.prog.tpi.sistema_subastas.repositories;

import com.prog.tpi.sistema_subastas.models.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
}
