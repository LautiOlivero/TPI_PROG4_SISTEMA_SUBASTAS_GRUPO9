package com.prog.tpi.sistema_subastas.controllers;

import com.prog.tpi.sistema_subastas.dtos.request.DisputaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.DisputaResponseDTO;
import com.prog.tpi.sistema_subastas.models.Usuario;
import com.prog.tpi.sistema_subastas.security.SecurityUtils;
import com.prog.tpi.sistema_subastas.services.DisputaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disputas")
public class DisputaController {

    private final DisputaService disputaService;

    public DisputaController(DisputaService disputaService) {
        this.disputaService = disputaService;
    }

    // POST /api/disputas → abrir disputa (ganador o vendedor)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DisputaResponseDTO> crearDisputa(@Valid @RequestBody DisputaRequestDTO dto) {
        Usuario solicitante = SecurityUtils.getUsuarioActual();
        DisputaResponseDTO creada = disputaService.crearDisputa(dto, solicitante);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // GET /api/disputas → listar todas (solo ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DisputaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(disputaService.listarTodas());
    }

    // GET /api/disputas/{id} → ver detalle (solo ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(disputaService.obtenerPorId(id));
    }

    // PATCH /api/disputas/{id}/resolver → resolver disputa (solo ADMIN)
    @PatchMapping("/{id}/resolver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputaResponseDTO> resolver(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String resolucion = body.get("resolucionAdministrativa");
        if (resolucion == null || resolucion.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(disputaService.resolver(id, resolucion));
    }
}
