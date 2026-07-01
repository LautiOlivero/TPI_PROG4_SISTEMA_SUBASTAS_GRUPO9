package com.prog.tpi.sistema_subastas.controllers;

import com.prog.tpi.sistema_subastas.dtos.request.DisputaRequestDTO;
import com.prog.tpi.sistema_subastas.dtos.response.DisputaResponseDTO;
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

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DisputaResponseDTO> crearDisputa(@Valid @RequestBody DisputaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disputaService.crearDisputa(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DisputaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(disputaService.listarTodas());
    }

    @PatchMapping("/{id}/resolver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisputaResponseDTO> resolverDisputa(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String resolucion = body.get("resolucion");
        if (resolucion == null || resolucion.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(disputaService.resolverDisputa(id, resolucion));
    }
}
