package com.prog.tpi.sistema_subastas.repositories;

import com.prog.tpi.sistema_subastas.models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioDestinoIdOrderByFechaDesc(Long usuarioId);
}
