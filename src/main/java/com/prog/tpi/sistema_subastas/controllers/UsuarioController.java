package com.prog.tpi.sistema_subastas.controllers;

import com.prog.tpi.sistema_subastas.dtos.response.UsuarioResponseDTO;
import com.prog.tpi.sistema_subastas.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfil() {
        return ResponseEntity.ok(usuarioService.obtenerPerfilActual());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PatchMapping("/{id}/bloquear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstadoBloqueo(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.cambiarEstadoBloqueo(id));
    }
}
