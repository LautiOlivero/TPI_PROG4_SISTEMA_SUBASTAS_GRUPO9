package com.prog.tpi.sistema_subastas.services;

import com.prog.tpi.sistema_subastas.dtos.response.NotificacionResponseDTO;
import com.prog.tpi.sistema_subastas.exceptions.ReglaNegocioException;
import com.prog.tpi.sistema_subastas.models.Notificacion;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.repositories.NotificacionRepository;
import com.prog.tpi.sistema_subastas.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarMisNotificaciones(Usuario usuario) {
        return notificacionRepository
                .findByUsuarioDestinoIdOrderByFechaDesc(usuario.getId())
                .stream()
                .map(DtoMapper::toNotificacionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificacionResponseDTO marcarComoLeida(Long id, Usuario usuario) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));

        if (!notificacion.getUsuarioDestino().getId().equals(usuario.getId())) {
            throw new ReglaNegocioException("No podés marcar como leída una notificación que no es tuya.");
        }

        notificacion.setLeido(true);
        notificacionRepository.save(notificacion);
        return DtoMapper.toNotificacionDTO(notificacion);
    }
}
