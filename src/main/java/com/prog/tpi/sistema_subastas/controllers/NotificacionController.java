package com.prog.tpi.sistema_subastas.controllers;

import com.prog.tpi.sistema_subastas.dtos.response.NotificacionResponseDTO;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.security.SecurityUtils;
import com.prog.tpi.sistema_subastas.services.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    // GET /api/notificaciones/mis → mis notificaciones
    @GetMapping("/mis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionResponseDTO>> miasNotificaciones() {
        Usuario usuario = SecurityUtils.getUsuarioActual();
        return ResponseEntity.ok(notificacionService.listarMisNotificaciones(usuario));
    }

    // PATCH /api/notificaciones/{id}/leer → marcar como leída
    @PatchMapping("/{id}/leer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificacionResponseDTO> marcarLeida(@PathVariable Long id) {
        Usuario usuario = SecurityUtils.getUsuarioActual();
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id, usuario));
    }
}
