package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.response.NotificacionResponseDTO;
import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.Notificacion;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.NotificacionRepository;
import com.prog.tpi.sistema_subastas.security.SecurityUtils;
import com.prog.tpi.sistema_subastas.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    public void enviarNotificacion(Usuario destino, String mensaje) {
        Notificacion notificacion = Notificacion.builder()
                .usuarioDestino(destino)
                .mensaje(mensaje)
                .leido(false)
                .fecha(Instant.now())
                .build();
        notificacionRepository.save(notificacion);
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> obtenerMisNotificaciones() {
        Usuario usuarioActual = SecurityUtils.getUsuarioActual();
        return notificacionRepository.findAll().stream()
                .filter(n -> n.getUsuarioDestino().getId().equals(usuarioActual.getId()))
                .sorted((n1, n2) -> n2.getFecha().compareTo(n1.getFecha()))
                .map(DtoMapper::toNotificacionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificacionResponseDTO marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Notificación no encontrada."));

        Usuario usuarioActual = SecurityUtils.getUsuarioActual();
        if (!notificacion.getUsuarioDestino().getId().equals(usuarioActual.getId())) {
            throw new ReglaNegocioException("No puedes marcar como leída una notificación que no te pertenece.");
        }

        notificacion.setLeido(true);
        Notificacion guardada = notificacionRepository.save(notificacion);
        return DtoMapper.toNotificacionDTO(guardada);
    }
}
